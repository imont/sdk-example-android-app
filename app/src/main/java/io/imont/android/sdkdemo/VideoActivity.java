/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 22/07/2016.
 */
package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;
import io.imont.android.sdkdemo.utils.CameraHelper;
import io.imont.android.sdkdemo.video.IMONTMediaController;
import io.imont.android.sdkdemo.video.MyIjkVideoView;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.cairo.EventMetaKey;
import io.imont.cairo.events.Video;
import io.imont.ext.org.apache.commons.io.IOUtils;
import io.imont.ferret.client.utils.Resource;
import io.imont.lion.Lion;
import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.mediaplayer.media.AndroidMediaController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VideoActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(VideoActivity.class);

    public static final int MAX_RETRY_COUNT = 5;

    private Uri mVideoUri;

    private MyIjkVideoView mVideoView;

    private boolean mBackPressed;

    private ProgressDialog loadingDialog;

    private Resource<Integer> openedSocket;

    public static Intent newIntent(Context context, String peerId, String ip, int port,
                                   String username, String password, String path, int orientation) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("peerId", peerId);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("path", path);
        intent.putExtra("orientation", orientation);
        return intent;
    }

    public static Intent newIntentToFirstCamera(final Context context) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("firstCamera", true);
        intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return intent;
    }

    public static void intentTo(Context context, String peerId, String ip, int port,
                                String username, String password, String path, int orientation) {
        context.startActivity(newIntent(context, peerId, ip, port, username, password, path, orientation));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_player);

        loadingDialog = ProgressDialog.show(this, "", "Loading video. Please wait...", true);

        boolean firstCamera = getIntent().getBooleanExtra("firstCamera", false);
        if (firstCamera) {
            AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
                @Override
                public void call(final Lion lion) {
                    try {
                        MoleClient mc = lion.getMole();
                        List<String> devices = mc.getAllEntityIds();
                        for (String d : devices) {
                            Event evt = mc.getState(d, Video.VIDEO_STREAM_AVAILABLE.getFQEventKey());
                            if (evt != null) {
                                final Event videoEvent = mc.getState(d, Video.VIDEO_STREAM_AVAILABLE.getFQEventKey());
                                final String peerId = videoEvent.getId().getPeerId();

                                EventMetaKey quality = Video.RTSP_PATH_LOW_QUALITY;
                                final Map<String, String> videoMeta = videoEvent.getMetadata();
                                final String ip = CameraHelper.getIPAddress(d, mc);
                                final int port = Integer.parseInt(videoMeta.get(Video.RTSP_PORT.getMetaKey()));

                                show(peerId, ip, port, videoMeta.get(Video.RTSP_USER.getMetaKey()), videoMeta.get(Video.RTSP_PASSWORD.getMetaKey()), videoMeta.get(quality.getMetaKey()));

                                return;
                            }
                        }
                    } catch (MoleException e) {
                        logger.warn(e.getMessage(), e);
                        Toast.makeText(VideoActivity.this, "Error playing video", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {
            final String peerId = getIntent().getStringExtra("peerId");
            final String ip = getIntent().getStringExtra("ip");
            final int port = getIntent().getIntExtra("port", 0);
            final String username = getIntent().getStringExtra("username");
            final String password = getIntent().getStringExtra("password");
            final String path = getIntent().getStringExtra("path");

            show(peerId, ip, port, username, password, path);
        }
    }

    private void show(final String peerId, final String ip, final int port, final String username, final String password, final String path) {
        AndroidLionLoader.getLion(this).flatMap(new Func1<Lion, Observable<Resource<Integer>>>() {
            @Override
            public Observable<Resource<Integer>> call(final Lion lion) {
                return lion.getFerret().openSocket(peerId, ip, port, true);
            }
        }).subscribe(new Action1<Resource<Integer>>() {
                         @Override
                         public void call(final Resource<Integer> socket) {
                             openedSocket = socket;
                             String mVideoPath = String.format(Locale.ENGLISH, "rtsp://%s:%s@localhost:%d%s",
                                     username, password, socket.getValue(), path);

                             Intent intent = getIntent();
                             String intentAction = intent.getAction();
                             if (!TextUtils.isEmpty(intentAction)) {
                                 if (intentAction.equals(Intent.ACTION_VIEW)) {
                                     mVideoPath = intent.getDataString();
                                 } else if (intentAction.equals(Intent.ACTION_SEND)) {
                                     mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                         String scheme = mVideoUri.getScheme();
                                         if (TextUtils.isEmpty(scheme)) {
                                             logger.error("Null unknown scheme\n");
                                             finish();
                                             return;
                                         }
                                         if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                                             mVideoPath = mVideoUri.getPath();
                                         } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                                             logger.error("Can not resolve content below Android-ICS\n");
                                             finish();
                                             return;
                                         } else {
                                             logger.error("Unknown scheme " + scheme + "\n");
                                             finish();
                                             return;
                                         }
                                     }
                                 }
                             }

                             // init player
                             final String finalMVideoPath = mVideoPath;
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     IjkMediaPlayer.loadLibrariesOnce(null);
                                     IjkMediaPlayer.native_profileBegin("libijkplayer.so");

                                     startVideo(finalMVideoPath);
                                 }
                             });
                         }
                     },
                new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        logger.warn("Error streaming video: {}", throwable.getMessage());
                        loadingDialog.dismiss();
                        Toast.makeText(VideoActivity.this, "Error establishing connection", Toast.LENGTH_SHORT).show();
                        VideoActivity.this.finish();
                    }
                }
        );
    }

    private void startVideo(final String mVideoPath) {
        AndroidMediaController mMediaController = new IMONTMediaController(this, false);

        mVideoView = (MyIjkVideoView) findViewById(R.id.my_video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                logger.debug("Video Playback finished");
                finish();
            }
        });
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                //finish();
                loadingDialog.dismiss();
                int attempt = getIntent().getIntExtra("attempt", 0);
                if (attempt < MAX_RETRY_COUNT) {
                    logger.warn("Got error playing video, retrying. Retry: {}", attempt);
                    getIntent().putExtra("attempt", ++attempt);
                    recreate();
                } else {
                    Toast.makeText(VideoActivity.this, "Unable to connect to camera, please try again", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final IMediaPlayer iMediaPlayer) {
                loadingDialog.dismiss();
            }
        });
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            logger.error("Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        logger.debug("Stopping video view");
        super.onStop();

        if (mVideoView == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("Closing socket resource");
                IOUtils.closeQuietly(openedSocket);
            }
        }, "SocketCloseThread").start();


        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onBackPressed() {
        logger.debug("Back pressed");
        mBackPressed = true;

        super.onBackPressed();
    }
}
