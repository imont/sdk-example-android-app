/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.imont.android.sdkdemo.adapters.FoundDevicesAdapter;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.Lion;
import io.imont.lion.network.DeviceCandidate;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FindDevicesActivity extends AppCompatActivity implements View.OnClickListener {

    private final Logger log = LoggerFactory.getLogger(FindDevicesActivity.class);

    private String peerId;
    private Subscription activeSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle params = getIntent().getExtras();
        peerId = params.getString("itemId");

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.find_devices_progress_bar);
        final TextView noResultsTv = (TextView) findViewById(R.id.find_devices_no_results);
        final Button finishButton = (Button) findViewById(R.id.finish_discovery);

        finishButton.setOnClickListener(this);

        assert progressBar != null;
        assert noResultsTv != null;

        final ListView devicesList = (ListView) findViewById(R.id.found_device_list);
        final FoundDevicesAdapter adapter = new FoundDevicesAdapter(this, peerId, new ArrayList<DeviceCandidate>(), this);
        devicesList.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        activeSub = AndroidLionLoader.getLion(this).flatMap(new Func1<Lion, Observable<DeviceCandidate>>() {
            @Override
            public Observable<DeviceCandidate> call(final Lion lion) {
                return lion.discover(peerId);
            }
        }).subscribeOn(Schedulers.io()).timeout(60, SECONDS).onErrorResumeNext(new Func1<Throwable, Observable<? extends DeviceCandidate>>() {
            @Override
            public Observable<? extends DeviceCandidate> call(final Throwable throwable) {
                if (throwable instanceof TimeoutException) {
                    return Observable.empty();
                }
                log.error("Error finding devices:", throwable);
                return Observable.error(throwable);
            }
        }).subscribe(
                new Action1<DeviceCandidate>() {
                    @Override
                    public void call(final DeviceCandidate deviceCandidate) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // This is a bit horrible.  IP devices are device candidates but many can't be automatically acquired due
                                // to specific nuances of the device.
                                if (!deviceCandidate.getNetwork().equals("LocalIP")) {
                                    adapter.add(deviceCandidate);
                                }
                            }
                        });
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        if (!(throwable instanceof TimeoutException)) {
                            log.error("Error finding devices:", throwable);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    noResultsTv.setText("Error: " + throwable);
                                    noResultsTv.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FindDevicesActivity.this, "Scan finished", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                if (adapter.isEmpty()) {
                                    noResultsTv.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
        );

    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.finish_discovery && peerId != null) {
            tearDown();
        }
    }

    @Override
    public void onBackPressed() {
        tearDown();
    }

    private void tearDown() {
        AndroidLionLoader.getLion(this).subscribeOn(Schedulers.io()).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                lion.stopDiscovering(peerId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FindDevicesActivity.this, DevicesActivity.class));
                    }
                });
            }
        });
        if (activeSub != null) {
            activeSub.unsubscribe();
        }
    }
}
