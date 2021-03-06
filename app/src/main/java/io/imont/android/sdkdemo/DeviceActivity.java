/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import io.imont.android.sdkdemo.adapters.DeviceAttributeAdapter;
import io.imont.android.sdkdemo.utils.CameraHelper;
import io.imont.cairo.EventMetaKey;
import io.imont.cairo.events.*;
import io.imont.ext.org.apache.commons.lang.ArrayUtils;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.lion.device.DeviceRemovalStatus;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import io.imont.mole.client.EventResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.imont.mole.client.GlobalEntityId;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.imont.cairo.events.Hardware.DEVICE_ADDED_EVENT;
import static io.imont.mole.client.EventResult.SyncStatus.SYNCHRONIZED;

public class DeviceActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(DeviceActivity.class);

    private static final String[] TEMP_PICKER_VALUES;
    public static final String DEVICE_ID_PARAM = "deviceId";
    public static final String PEER_ID_PARAM = "peerId";

    private Subscription attributeSubscription;

    static {
        List<String> numbersList = new ArrayList<>();
        for (int x = 3; x <= 32; x++) {
            numbersList.add(String.format(Locale.ENGLISH, "%d.0", x));
            if (x != 32) {
                numbersList.add(String.format(Locale.ENGLISH, "%d.5", x));
            }
        }
        TEMP_PICKER_VALUES = numbersList.toArray(new String[] {});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle params = getIntent().getExtras();
        final GlobalEntityId globalEntityId = GlobalEntityId.with(params.getString(PEER_ID_PARAM), params.getString(DEVICE_ID_PARAM));

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                try {
                    Device device = lion.getAllDevices().get(globalEntityId);
                    Event deviceAdded = device.getState().get(DEVICE_ADDED_EVENT.getFQEventKey());
                    if ("THERMOSTAT".equals(deviceAdded.getValue())) {
                        handleThermostat(lion, globalEntityId);
                    } else {
                        handleOther(globalEntityId);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    Toast.makeText(DeviceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleThermostat(final Lion lion, final GlobalEntityId globalEntityId) {
        setContentView(R.layout.activity_device_thermostat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NumberPicker np = (NumberPicker) findViewById(R.id.temp_picker);
        np.setMaxValue(TEMP_PICKER_VALUES.length-1);
        np.setMinValue(0);
        np.setDisplayedValues(TEMP_PICKER_VALUES);
        np.setWrapSelectorWheel(false);

        np.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(final NumberPicker view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    String value = TEMP_PICKER_VALUES[view.getValue()];
                    lion.getMole().raiseEvent(globalEntityId, Thermostat.HEATING_TARGET_TEMPERATURE_EVENT.getFQEventKey(), 0, value).subscribe(
                            new Action1<EventResult>() {
                                @Override
                                public void call(final EventResult eventResult) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(DeviceActivity.this, eventResult.getSyncStatus() == SYNCHRONIZED
                                                            ? "Request succeeded"
                                                            : "Request failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    });
                                }
                            }
                    );
                }
            }
        });

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                // Do an initial refresh first
                refreshThermostat(globalEntityId);

                // Also refresh if we get events for this device
                attributeSubscription = lion.getMole().events().filter(new Func1<Event, Boolean>() {
                    @Override
                    public Boolean call(final Event event) {
                        return Objects.equals(GlobalEntityId.with(event.getId().getPeerId(), event.getEntityId()), globalEntityId);
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Event>() {
                    @Override
                    public void call(final Event event) {
                        // massively, massively inefficient, but works
                        refreshThermostat(globalEntityId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.error("Error refreshing thermostat values", throwable);
                    }
                });
            }
        });
    }

    private void handleOther(final GlobalEntityId globalEntityId) {
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent metadataActivity = new Intent(this, EventActivity.class);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView attributeList = (ListView) findViewById(R.id.device_attributes);
        final DeviceAttributeAdapter adapter = new DeviceAttributeAdapter(this, new ArrayList<Event>());
        attributeList.setAdapter(adapter);

        attributeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Event stateEvent = adapter.getItem(position);

                Intent intent = new Intent(DeviceActivity.this, AttributeChartActivity.class);
                intent.putExtra("entityId", stateEvent.getEntityId());
                intent.putExtra("eventKey", stateEvent.getKey());
                startActivity(intent);
                return true; // this is important, if we don't return true the short click listener gets invoked too
            }
        });

        attributeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Event stateEvent = adapter.getItem(position);
                if (Objects.equals(stateEvent.getKey(), OnOff.ON_OFF_EVENT.getFQEventKey())) {
                    final Snackbar bar = Snackbar.make(view, "Please wait...", Snackbar.LENGTH_LONG);
                    bar.show();
                    toggleSwitch(stateEvent, attributeList).subscribe(new Action1<EventResult>() {
                        @Override
                        public void call(final EventResult eventResult) {
                            refreshAttributes(adapter, globalEntityId);
                            if (eventResult.getSyncStatus() != SYNCHRONIZED) {
                                Toast.makeText(DeviceActivity.this, "Failed, check connectivity status", Toast.LENGTH_SHORT).show();
                            }
                            Observable.just(1).delay(1, TimeUnit.SECONDS).subscribe(new Action1<Integer>() {
                                @Override
                                public void call(final Integer integer) {
                                    bar.dismiss();
                                }
                            });
                        }
                    });
                } else if (stateEvent.getMetadata().size() > 0) {
                    metadataActivity.putExtra("metadata", new HashMap<>(stateEvent.getMetadata())); // has to be serializable
                    metadataActivity.putExtra("itemId", globalEntityId.getEntityId());
                    startActivity(metadataActivity);
                }
            }
        });

        // Lookup the swipe container view
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                // Do an initial refresh first
                refreshAttributes(adapter, globalEntityId);

                // Setup refresh listener which triggers new data loading
                swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // Your code to refresh the list here.
                        // Make sure you call swipeContainer.setRefreshing(false)
                        // once the network request has completed successfully.
                        refreshAttributes(adapter, globalEntityId);
                        swipeContainer.setRefreshing(false);
                    }
                });

                // Also refresh if we get events for this device
                attributeSubscription = lion.getMole().events().filter(new Func1<Event, Boolean>() {
                    @Override
                    public Boolean call(final Event event) {
                        return Objects.equals(GlobalEntityId.with(event.getId().getPeerId(), event.getEntityId()), globalEntityId);
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Event>() {
                    @Override
                    public void call(final Event event) {
                        // massively, massively inefficient, but works
                        refreshAttributes(adapter, globalEntityId);
                    }
                });
            }
        });
    }

    private Observable<EventResult> toggleSwitch(final Event stateEvent, final View view) {
        return AndroidLionLoader.getLion(this).subscribeOn(Schedulers.io()).flatMap(new Func1<Lion, Observable<EventResult>>() {
            @Override
            public Observable<EventResult> call(final Lion lion) {
                String value = Objects.equals("1", stateEvent.getValue()) ? "0" : "1";
                return lion.getMole().raiseEvent(GlobalEntityId.with(stateEvent.getId().getPeerId(), stateEvent.getEntityId()), stateEvent.getKey(), 0, value);
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attributeSubscription != null) {
            attributeSubscription.unsubscribe();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final Bundle params = getIntent().getExtras();
        final String deviceIdString = params.getString(DEVICE_ID_PARAM);
        final String peerId = params.getString(PEER_ID_PARAM);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                try {
                    boolean isPeer = false;
                    boolean isCamera = false;
                    GlobalEntityId globalEntityId = new GlobalEntityId(peerId, deviceIdString);

                    Device d = lion.getAllDevices().get(globalEntityId);
                    if (d.getType() == Device.Type.PEER) {
                        // this is a peer device
                        isPeer = true;
                    }

                    if (!isPeer) {
                        menu.findItem(R.id.action_add_device).setVisible(false);
                        menu.findItem(R.id.action_find_devices).setVisible(false);
                        menu.findItem(R.id.action_get_telemetry).setVisible(false);
                        menu.findItem(R.id.action_hub_upgrade).setVisible(false);
                        menu.findItem(R.id.action_hub_rules).setVisible(false);
                    }

                    Event evt = lion.getMole().getState(globalEntityId, Video.VIDEO_STREAM_AVAILABLE.getFQEventKey());
                    if (evt != null) {
                        isCamera = true;
                    }

                    if (!isCamera) {
                        menu.findItem(R.id.action_stream_hi).setVisible(false);
                        menu.findItem(R.id.action_stream_lo).setVisible(false);
                    }
                } catch (MoleException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        final Bundle params = getIntent().getExtras();
        final String entityId = params.getString(DEVICE_ID_PARAM);
        final String peerId = params.getString(PEER_ID_PARAM);
        final GlobalEntityId globalEntityId = GlobalEntityId.with(peerId, entityId);


        if (id == R.id.action_find_devices) {
            final Intent findDevicesIntent = new Intent(this, FindDevicesActivity.class);
            findDevicesIntent.putExtra("itemId", entityId);
            startActivity(findDevicesIntent);
            return false;
        } else if(id == R.id.action_hub_rules) {
            final Intent intent = new Intent(this, RulesActivity.class);
            intent.putExtra("entityId", entityId);
            startActivity(intent);
            return false;
        } else if (id == R.id.action_add_device) {
            final Intent addDeviceIntent = new Intent(this, AddDeviceActivity.class);
            addDeviceIntent.putExtra("itemId", entityId);
            startActivity(addDeviceIntent);
            return false;
        } else if (id == R.id.action_remove_device) {
            handleRemoveDevice(peerId, entityId);
            return false;
        } else if (id == R.id.action_get_telemetry) {
            handleRetrieveTelemetry(entityId);
        }

        if (id != R.id.action_stream_hi && id != R.id.action_stream_lo) {
            return false;
        }

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                try {
                    final Event videoEvent = lion.getMole().getState(GlobalEntityId.with(peerId, entityId), Video.VIDEO_STREAM_AVAILABLE.getFQEventKey());
                    final String peerId = videoEvent.getId().getPeerId();

                    EventMetaKey quality = id == R.id.action_stream_hi ? Video.RTSP_PATH_HIGH_QUALITY : Video.RTSP_PATH_LOW_QUALITY;
                    final Map<String, String> videoMeta = videoEvent.getMetadata();

                    String ip = CameraHelper.getIPAddress(globalEntityId, lion.getMole());
                    final int port = Integer.parseInt(videoMeta.get(Video.RTSP_PORT.getMetaKey()));

                    VideoActivity.intentTo(
                            DeviceActivity.this, peerId, ip, port,
                            videoMeta.get(Video.RTSP_USER.getMetaKey()), videoMeta.get(Video.RTSP_PASSWORD.getMetaKey()),
                            videoMeta.get(quality.getMetaKey()), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    );
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });

        return super.onOptionsItemSelected(item);
    }

    private void refreshAttributes(final DeviceAttributeAdapter adapter, final GlobalEntityId globalEntityId) {
        AndroidLionLoader.getLion(this).subscribeOn(Schedulers.io()).flatMap(new Func1<Lion, Observable<Map<String, Event>>>() {
            @Override
            public Observable<Map<String, Event>> call(final Lion lion) {
                Device device = lion.getAllDevices().get(globalEntityId);
                // If the device has just been deleted, we may get a null Device object.
                if (device != null) {
                    return Observable.just(device.getState());
                }
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Map<String, Event>>() {
            @Override
            public void call(final Map<String, Event> attrs) {
                adapter.clear();
                adapter.addAll(attrs.values());
                adapter.notifyDataSetChanged();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.error("Error retrieving latest state for device {} ", globalEntityId, throwable);
            }
        });
    }

    private void refreshThermostat(final GlobalEntityId globalEntityId) {
        AndroidLionLoader.getLion(this).subscribeOn(Schedulers.io()).map(new Func1<Lion, Map<String, Event>>() {
            @Override
            public Map<String, Event> call(final Lion lion) {
                try {
                    return lion.getMole().getState(globalEntityId);
                } catch (MoleException e) {
                    throw new RuntimeException(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Map<String, Event>>() {
            @Override
            public void call(final Map<String, Event> state) {
                TextView temperature = (TextView) findViewById(R.id.current_temperature);
                TextView heatingState = (TextView) findViewById(R.id.heating_state);
                NumberPicker picker = (NumberPicker) findViewById(R.id.temp_picker);
                for (Event evt : state.values()) {
                    if (evt.getKey().equals(Temperature.AMBIENT_TEMPERATURE_EVENT.getFQEventKey())) {
                        String roundedTemp = String.format("%.1f", Float.parseFloat(evt.getValue()));
                        temperature.setText(roundedTemp + "°C");
                    } else if (evt.getKey().equals(Thermostat.HEATING_TARGET_TEMPERATURE_EVENT.getFQEventKey())) {
                        picker.setValue(ArrayUtils.indexOf(TEMP_PICKER_VALUES, evt.getValue()));
                    } else if (evt.getKey().equals(Thermostat.HEATING_STATE_EVENT.getFQEventKey())) {
                        heatingState.setText("1".equals(evt.getValue()) ? "ON" : "OFF");
                    }
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.error("Exception whilst updating thermostat values", throwable);
            }
        });
    }

    private void handleRemoveDevice(final String peerId, final String deviceId) {

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                lion.removeDevice(peerId, deviceId).subscribeOn(Schedulers.io()).timeout(15, TimeUnit.SECONDS).subscribe(
                        new Action1<DeviceRemovalStatus>() {
                            @Override
                            public void call(final DeviceRemovalStatus deviceRemovalStatus) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String msg = "Device removed";
                                        if (!deviceRemovalStatus.isRemoved()) {
                                            msg = "Device removal failed: " + deviceRemovalStatus.getFailureReason();
                                        }
                                        Toast.makeText(DeviceActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        DevicesActivity.intentTo(DeviceActivity.this);
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
                                        String msg = "Device removal failed: " + throwable.getMessage();
                                        Toast.makeText(DeviceActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        DevicesActivity.intentTo(DeviceActivity.this);
                                    }
                                });
                            }
                        });
            }
        });
    }

    private void handleRetrieveTelemetry(final String deviceId) {
        final ProgressDialog pd = ProgressDialog.show(this, "Please wait", "Retrieving telemetry", false);
        AndroidLionLoader.getLion(this).flatMap(new Func1<Lion, rx.Observable<File>>() {
            @Override
            public Observable<File> call(final Lion lion) {
                return lion.getTelemetry(deviceId);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<File>() {
            @Override
            public void call(final File file) {
                pd.dismiss();
                shareFile(deviceId, file);
            }
        });
    }

    private void shareFile(final String deviceId, final File file) {
        String name = String.format(Locale.ENGLISH, "telemetry-%s-%d.zip", deviceId, System.currentTimeMillis());
        File telemetryDir = new File(getFilesDir(), "telemetry");
        if (!telemetryDir.exists()) {
            if (!telemetryDir.mkdir()) {
                Toast.makeText(this, "Gathering failed. (Create Dir)", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        File destination = new File(telemetryDir, name);
        boolean res = file.renameTo(destination);
        if (!res) {
            Toast.makeText(this, "Gathering failed. (Rename)", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri fileUri = FileProvider.getUriForFile(this, "io.imont.android.imontdemo.fileprovider", destination);
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        intentShareFile.setType("application/zip");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri);
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, name);
        intentShareFile.putExtra(Intent.EXTRA_TEXT, name);

        Intent intent = Intent.createChooser(intentShareFile, "Share telemetry");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
    }

}
