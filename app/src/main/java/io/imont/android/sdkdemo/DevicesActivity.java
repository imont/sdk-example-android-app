package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import io.imont.android.sdkdemo.adapters.DeviceListAdapter;
import io.imont.android.sdkdemo.utils.MoleDeviceListComparator;
import io.imont.cairo.events.Hardware;
import io.imont.ferret.client.status.ConnectivityEvent;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DevicesActivity extends AppCompatActivity {

    public static void intentTo(Context context) {
        context.startActivity(new Intent(context, DevicesActivity.class));
    }

    private Subscription eventsSubscription;

    ListView deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final View deviceListToolBar = getLayoutInflater().inflate(R.layout.device_list_toolbar, null);
        deviceListToolBar.setVisibility(View.GONE);
        toolbar.addView(deviceListToolBar);

        final Intent deviceActivityIntent = new Intent(this, DeviceActivity.class);

        deviceList = (ListView) findViewById(R.id.device_list);
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        final DeviceListAdapter adapter = new DeviceListAdapter(DevicesActivity.this, new ArrayList<Device>());
        adapter.setNotifyOnChange(false);
        deviceList.setAdapter(adapter);

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Device item = adapter.getItem(position);
                deviceActivityIntent.putExtra("itemId", item.getId().getEntityId());
                startActivity(deviceActivityIntent);
            }
        });

        // Lookup the swipe container view
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refreshDeviceList(adapter);
                swipeContainer.setRefreshing(false);

            }
        });

        AndroidLionLoader.getLion(this).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                refreshDeviceList(adapter);

                // The debounce is needed to only receive the latest events - we actually don't care much about the history here
                eventsSubscription = lion.getFerret().eventStream().debounce(50, MILLISECONDS).subscribe(
                        new Action1<ConnectivityEvent>() {
                            @Override
                            public void call(final ConnectivityEvent connectivityEvent) {
                                updateConnectionStatus(connectivityEvent, deviceListToolBar, adapter);
                            }
                        }
                );
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
        if (eventsSubscription != null) {
            eventsSubscription.unsubscribe();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_events) {
            Intent eventsIntent = new Intent(this, ConnectivityActivity.class);
            startActivity(eventsIntent);
            return true;
        } else if (id == R.id.action_event_log) {
            final Intent eventLogIntent = new Intent(DevicesActivity.this, EventLogActivity.class);
            startActivity(eventLogIntent);
        } else if (id == R.id.action_scan_network) {
            Intent scanIntent = new Intent(this, ScanNetworkActivity.class);
            startActivity(scanIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshDeviceList(final DeviceListAdapter adapter) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Please wait, loading your devices...", true);
        AndroidLionLoader.getLion(this).observeOn(Schedulers.io()).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                final List<Device> allDevices = new ArrayList<>(lion.getAllDevices().values());
                Collections.sort(allDevices, new MoleDeviceListComparator(lion.getPeerId()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.addAll(allDevices);
                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    private void refreshConnectionInformation(final DeviceListAdapter adapter) {
        adapter.refreshConnectionInfo();
    }

    private Map<String, String> getHubVersions(final MoleClient client) throws MoleException {
        Map<String, String> res = new HashMap<>();
        for (String deviceId : client.getAllEntityIds()) {  //TODO Once we have upgrades working, we will need to check for any upgrade events as well.
            Event deviceAddedEvent = client.getState(deviceId, Hardware.DEVICE_ADDED_EVENT.getFQEventKey());
            if (deviceAddedEvent != null && deviceAddedEvent.getValue().equals("HUB")) {
                res.put(deviceId, deviceAddedEvent.getMetadata().get(Hardware.DEVICE_ADDED_FIRMWARE_VERSION_META.getMetaKey()));
            }
        }
        return res;
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    private void updateConnectionStatus(final ConnectivityEvent evt, final View toolbar, final DeviceListAdapter adapter) {
        switch (evt.getType()) {
            case P2P_ATTEMPT:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.setVisibility(View.VISIBLE);
                        refreshConnectionInformation(adapter);
                    }
                });
                break;
            case P2P_PEER_FOUND:
                /*
                 * FIXME older ferret clients sometimes don't emit CONNECTION_ESTABLISHED
                 */
            case CONNECTION_ESTABLISHED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.setVisibility(View.GONE);
                        refreshConnectionInformation(adapter);
                    }
                });
                break;
        }
    }
}
