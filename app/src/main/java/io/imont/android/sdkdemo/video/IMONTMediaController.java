/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import io.imont.android.sdkdemo.R;
import io.imont.cairo.events.Hardware;
import io.imont.cairo.events.OnOff;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.lion.api.DeviceId;
import io.imont.mole.client.Event;
import io.imont.mole.client.EventResult;
import io.imont.mole.client.GlobalEntityId;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import tv.danmaku.ijk.mediaplayer.media.AndroidMediaController;

import java.util.Map;
import java.util.Objects;

public class IMONTMediaController extends AndroidMediaController {

    private Context context;

    private ImageButton button;

    public IMONTMediaController(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public IMONTMediaController(final Context context, final boolean useFastForward) {
        super(context, useFastForward);
        this.context = context;
    }

    public IMONTMediaController(final Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void setAnchorView(final View view) {
        super.setAnchorView(view);

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.RIGHT|Gravity.TOP;

        View v = makeButton();
        addView(v, frameParams);
    }

    private View makeButton() {
        button = new ImageButton(context);
        button.setImageResource(R.drawable.lighbulb);

        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                handleButtonPress();
            }
        });

        return button;
    }

    private void handleButtonPress() {
        AndroidLionLoader.getLion(context).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                for (Map.Entry<DeviceId, Device> entry : lion.getAllDevices().entrySet()) {
                    Event hardwareEvent = entry.getValue().getLatestEvent(Hardware.DEVICE_ADDED_EVENT.getFQEventKey());
                    // Only control actual smartplugs, this is intentional
                    if (Objects.equals(hardwareEvent.getValue(), OnOff.ON_OFF_FEATURE.getEventKey())) {
                        Event ev = entry.getValue().getLatestEvent(OnOff.ON_OFF_EVENT.getFQEventKey());
                        if (ev != null) {
                            String targetValue = invertValue(ev.getValue());
                            GlobalEntityId id = GlobalEntityId.with(ev.getId().getPeerId(), ev.getEntityId());
                            lion.getMole().raiseEvent(id, OnOff.ON_OFF_EVENT.getFQEventKey(), 0, targetValue)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<EventResult>() {
                                        @Override
                                        public void call(final EventResult eventResult) {
                                            switch (eventResult.getSyncStatus()) {
                                                case SYNCHRONIZED:
                                                    Toast.makeText(context, "Request succeeded", Toast.LENGTH_SHORT).show();
                                                    break;
                                                default:
                                                    Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show();
                                                    break;
                                            }
                                        }
                                    });
                        }
                    }
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
