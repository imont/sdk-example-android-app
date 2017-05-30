package io.imont.android.sdkdemo.handlers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import io.imont.android.sdkdemo.DevicesActivity;
import io.imont.android.sdkdemo.R;
import io.imont.android.sdkdemo.VideoActivity;
import io.imont.cairo.events.OnOff;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.List;
import java.util.Objects;

import static io.imont.cairo.events.PushButton.PUSHED_EVENT;

public class ButtonPushedNotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ButtonPushedNotificationHandler.class);

    private static Subscription sub;

    public synchronized static void subscribe(final Context context, final MoleClient moleClient) {
        logger.debug("Subscribing to button push events");
        if (sub != null) {
            sub.unsubscribe();
        }
        sub = moleClient.events().filter(new Func1<Event, Boolean>() {
            @Override
            public Boolean call(final Event event) {
                return Objects.equals(event.getKey(), PUSHED_EVENT.getFQEventKey()) && event.isLive();
            }
        }).subscribe(new Action1<Event>() {
            @Override
            public void call(final Event event) {
                logger.debug("Button pushed event received: device: {}, peer: {}, sequence: {}, id: {}",
                        event.getEntityId(),
                        event.getId().getPeerId(),
                        event.getId().getPeerEventSequence(),
                        event.getId().getUuid()
                );
                showNotification(context);
            }

        });
    }

    public static void showNotification(final Context parent) {

        Intent video = VideoActivity.newIntentToFirstCamera(parent);
        video.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent videoIntent = PendingIntent.getActivity(parent, 0, video, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent switchIntent = new Intent(parent, OnOffListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(parent, 0, switchIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(parent)
                        .setSmallIcon(R.drawable.imont_logo)
                        .setContentTitle("Button Pushed")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentText("'Front Door Bell' button has been pushed.")
                        .addAction(R.drawable.notifications_camera, "Watch Live", videoIntent)
                        .addAction(R.drawable.notifications_light, "Light", pendingSwitchIntent);

        // Creates an explicit intent for an HelloActivity in your app
        Intent resultIntent = new Intent(parent, DevicesActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started HelloActivity.
        // This ensures that navigating backward from the HelloActivity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(parent);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DevicesActivity.class);
        // Adds the Intent that starts the HelloActivity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static class OnOffListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AndroidLionLoader.getLion(context).subscribe(new Action1<Lion>() {
                @Override
                public void call(final Lion lion) {
                    try {

                        MoleClient mc = lion.getMole();
                        List<String> devices = mc.getAllEntityIds();
                        for (String d : devices) {
                            Event ev = mc.getState(d, OnOff.ON_OFF_EVENT.getFQEventKey());
                            if (ev != null) {
                                String targetValue = invertValue(ev.getValue());
                                lion.getMole().raiseEvent(ev.getEntityId(), OnOff.ON_OFF_EVENT.getFQEventKey(), 0, targetValue)
                                        .subscribe();
                            }
                        }
                    } catch (MoleException e) {
                        e.printStackTrace();
                    }
                }

                private String invertValue(String value) {
                    if (value.equals("0")) {
                        return "1";
                    } else if (value.equals("1")) {
                        return "0";
                    }
                    return value;
                }
            });
        }
    }
}
