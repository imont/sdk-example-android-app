/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 26/08/2016.
 */
package io.imont.android.sdkdemo.firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.Lion;
import io.imont.mole.MoleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.util.HashMap;

public class IMONTFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final Logger logger = LoggerFactory.getLogger(IMONTFirebaseInstanceIDService.class);

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        logger.info("Firebase token refreshed: {}", refreshedToken);
        storeToken(refreshedToken);
    }

    private void storeToken(final String token) {
        AndroidLionLoader.getLion(getApplicationContext()).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                MoleClient mc = lion.getMole();
                try {
                    mc.raiseEvent(mc.getLocalPeerId(), "ANDROID>PUSH_TOKEN", token, new HashMap<String, String>()).subscribe();
                } catch (Exception e) {
                    logger.error("Failed to save new firebase token to DB: {}", e.getMessage());
                }
            }
        });
    }
}
