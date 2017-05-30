package io.imont.android.sdkdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import io.imont.android.sdkdemo.rules.actions.ShowNotificationAction;
import io.imont.ferret.client.config.NetworkConfig;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.mole.MoleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import java.util.HashMap;
import java.util.Map;

import static io.imont.cairo.events.Hardware.*;

public class HelloActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(HelloActivity.class);

    public static void intentTo(Context context) {
        context.startActivity(new Intent(context, HelloActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent scanNetworkIntent = new Intent(this, ScanNetworkActivity.class);
        final Intent devicesActivity = new Intent(this, DevicesActivity.class);

        Button newNetwork = (Button) findViewById(R.id.new_network_btn);
        Button existingNetwork = (Button) findViewById(R.id.existing_network_btn);

        newNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(scanNetworkIntent);
            }
        });
        existingNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(devicesActivity);
            }
        });

        NetworkConfig fc = new NetworkConfig();
        fc.setFriendlyName(String.format("Android: %s %s", Build.MANUFACTURER, Build.MODEL));
        fc.setRendezvousHost("r.imont.tech");
        fc.setRendezvousPort(4444);

        System.setProperty("org.ice4j.ipv6.DISABLED", "true");
        AndroidLionLoader.initLion(this, fc);

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                notifyAppStartup(lion.getMole());
            }
        });

        registerAndroidActions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Intent devicesListIntent = new Intent(this, DevicesActivity.class);

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                try {
                    if (lion.getFerret().getMembers().size() > 0) {
                        // existing network
                        startActivity(devicesListIntent);
                    } else {
                        onFinishLoadingLion();
                    }
                } catch (Exception e) {
                    logger.error("Failed to start", e);
                    Toast.makeText(getApplicationContext(), "Failed to startup: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onFinishLoadingLion() {
        Button newNetwork = (Button) findViewById(R.id.new_network_btn);
        Button existingNetwork = (Button) findViewById(R.id.existing_network_btn);

        newNetwork.setVisibility(View.VISIBLE);
        existingNetwork.setVisibility(View.VISIBLE);
    }

    // TODO this should be a driver
    private void notifyAppStartup(final MoleClient client) {
        try {
            String deviceId = client.getLocalPeerId();
            Map<String, String> metadata = new HashMap<>();
            metadata.put(DEVICE_ADDED_MANUFACTURER_META.getMetaKey(), "IMONT");
            metadata.put(DEVICE_ADDED_HARDWARE_VERSION_META.getMetaKey(), "1.0");
            metadata.put(DEVICE_ADDED_MODEL_META.getMetaKey(), "Android App");
            metadata.put(DEVICE_ADDED_MOLE_PEER_ID_META.getMetaKey(), deviceId);
            metadata.put(DEVICE_ADDED_PROTOCOL_META.getMetaKey(), "IP");

            client.raiseEvent(deviceId, DEVICE_ADDED_EVENT.getFQEventKey(), "ANDROID_APP", metadata).subscribe();

        } catch (Exception e) {
            logger.error("Failed to notify mole about startup", e);
        }
    }

    private void registerAndroidActions() {
        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                lion.getRuleEngine(lion.getPeerId()).registerAction(new ShowNotificationAction(HelloActivity.this));
            }
        });
    }
}
