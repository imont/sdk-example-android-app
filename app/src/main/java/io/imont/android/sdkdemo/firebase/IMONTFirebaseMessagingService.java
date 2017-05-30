/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 26/08/2016.
 */
package io.imont.android.sdkdemo.firebase;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import io.imont.android.sdkdemo.handlers.ButtonPushedNotificationHandler;
import io.imont.cairo.events.PushButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMONTFirebaseMessagingService extends FirebaseMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(IMONTFirebaseMessagingService.class);

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        logger.debug("From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            logger.debug("Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            logger.debug("Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See showNotification method below.
        showNotification(remoteMessage.getNotification().getBody());
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void showNotification(String messageBody) {
        logger.info("Showing notification to user: {}", messageBody);
        if (PushButton.PUSHED_EVENT.getFQEventKey().equals(messageBody)) {
            ButtonPushedNotificationHandler.showNotification(this);
        }
    }
}
