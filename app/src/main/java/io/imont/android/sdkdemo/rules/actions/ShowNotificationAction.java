package io.imont.android.sdkdemo.rules.actions;
/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 27/04/2017.
 */

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
import io.imont.lion.rules.Action;
import io.imont.lion.rules.Rule;
import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import rx.functions.Action1;

import java.util.List;
import java.util.Objects;

public class ShowNotificationAction implements Action {

    private final Context parent;

    public ShowNotificationAction(final Context parent) {
        this.parent = parent;
    }

    @Override
    public String key() {
        return "SHOW_ANDROID_NOTIFICATION";
    }

    @Override
    public void perform(final Event event, final Rule rule) throws Exception {

        // FIXME Only live events here
        if (!event.isLive()) {
            return;
        }

        Intent video = VideoActivity.newIntentToFirstCamera(parent);
        video.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent videoIntent = PendingIntent.getActivity(parent, 0, video, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent switchIntent = new Intent(parent, OnOffListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(parent, 0, switchIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(parent)
                        .setSmallIcon(R.drawable.imont_logo)
                        .setContentTitle(getTitle(event))
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentText(getText(event))
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

    private String getTitle(final Event event) {
        switch (event.getKey()) {
            // damn you java and your constant requirements
            case "PUSH_BUTTON>PUSHED":
                return "Button pushed";
            case "MOTION>MOTION_DETECTED":
                return "Motion detected";
            case "OPEN_CLOSED>OPEN_CLOSED":
                return "Door opened";
            case "HARDWARE>PRESENCE":
                return String.format("Device has %s", presentOrAbsent(event));
            case "TEMPERATURE>AMBIENT_TEMPERATURE":
                return "Device temperature reached";
            default:
                return event.getValue();
        }
    }

    private String getText(final Event event) {
        switch (event.getKey()) {
            // damn you java and your constant requirements
            case "PUSH_BUTTON>PUSHED":
                return String.format("Button %s has been pushed", event.getEntityId());
            case "MOTION>MOTION_DETECTED":
                return String.format("Sensor %s has detected motion", event.getEntityId());
            case "OPEN_CLOSED>OPEN_CLOSED":
                return String.format("Sensor %s has been %s", event.getEntityId(), openedOrClosed(event));
            case "HARDWARE>PRESENCE":
                return String.format("Device %s has %s", event.getEntityId(), presentOrAbsent(event));
            case "TEMPERATURE>AMBIENT_TEMPERATURE":
                return String.format("Device %s has temperature %s", event.getEntityId(), event.getValue());
            default:
                return event.getValue();
        }
    }

    private String openedOrClosed(final Event event) {
        return event.getValue().equals("OPEN") ? "opened" : "closed";
    }

    private String presentOrAbsent(final Event event) {
        return Objects.equals("PRESENT", event.getValue()) ? "come back" : "gone missing";
    }

}
