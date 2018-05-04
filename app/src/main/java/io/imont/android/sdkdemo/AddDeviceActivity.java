/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.lion.network.DeviceCandidate;
import io.imont.lion.network.DeviceCredentials;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class AddDeviceActivity extends AppCompatActivity {

    private static final Logger log = LoggerFactory.getLogger(AddDeviceActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle params = getIntent().getExtras();
        final String peerId = params.getString("itemId");

        Button button = (Button) findViewById(R.id.add_button);
        final EditText addressView = (EditText) findViewById(R.id.add_address);
        final EditText portView = (EditText) findViewById(R.id.add_port);
        final EditText userView = (EditText) findViewById(R.id.add_username);
        final EditText passView = (EditText) findViewById(R.id.add_password);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String address = addressView.getText().toString();
                if (address.isEmpty()) {
                    Toast.makeText(AddDeviceActivity.this, "Address is mandatory", Toast.LENGTH_SHORT).show();
                    return;
                }
                final Integer port;
                if (portView.getText() != null && !portView.getText().toString().isEmpty()) {
                    port = Integer.parseInt(portView.getText().toString());
                } else {
                    port = 0;
                }
                final String username = userView.getText().toString();
                final String password = passView.getText().toString();

                final ProgressDialog prog = ProgressDialog.show(AddDeviceActivity.this, "Please wait", "Searching for your device...");

                Observable.from(Arrays.asList("LocalIP", "tplink")).concatMap(
                        new Func1<String, Observable<Device>>() {
                            @Override
                            public Observable<Device> call(final String networkLayer) {
                                return AndroidLionLoader.getLion(AddDeviceActivity.this).flatMap(new Func1<Lion, Observable<Device>>() {
                                    @Override
                                    public Observable<Device> call(final Lion lion) {
                                        return lion.registerDevice(peerId, makeDeviceCandidate(networkLayer, address, port, username, password));
                                    }
                                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Device>>() {
                                    @Override
                                    public Observable<? extends Device> call(Throwable throwable) {
                                        log.debug("Network layer {} could not acquire device at address {}", networkLayer, address);
                                        // FIXME The delay here is because we're picking up the previous error in reply
                                        // FIXME Remove this once the lion peer messenger supports transaction IDs
                                        return Observable.<Device>empty().delay(6, TimeUnit.SECONDS);
                                    }
                                });
                            }
                        }).subscribeOn(Schedulers.io())
                        .first()
                        .subscribe(
                                new Action1<Device>() {
                                    public void call(Device obj) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                prog.dismiss();
                                                Toast.makeText(AddDeviceActivity.this, "Device added", Toast.LENGTH_SHORT).show();
                                                DevicesActivity.intentTo(AddDeviceActivity.this);
                                            }
                                        });
                                    }
                                },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(final Throwable throwable) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                prog.dismiss();
                                                Toast.makeText(AddDeviceActivity.this, "Error adding device: " + throwable + ", " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                        );
            }
        });
    }

    private DeviceCandidate makeDeviceCandidate(final String network, final String address, final int port, final String username, final String password) {
        return new DeviceCandidate.Builder(network, address).port(port).credentials(new DeviceCredentials(username, password)).build();
    }
}
