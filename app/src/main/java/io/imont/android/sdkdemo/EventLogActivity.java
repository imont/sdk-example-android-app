package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import io.imont.android.sdkdemo.adapters.EventLogAdapter;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.Lion;
import io.imont.mole.Consistency;
import io.imont.mole.client.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventLogActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(EventLogActivity.class);

    private ProgressDialog progressDialog;

    private Executor refreshExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent deviceActivityIntent = new Intent(this, EventActivity.class);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView attributeList = (ListView) findViewById(R.id.event_attributes);
        final EventLogAdapter adapter = new EventLogAdapter(this, new ArrayList<Event>());
        attributeList.setAdapter(adapter);

        attributeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Event event = adapter.getItem(position);
                if (event.getMetadata().size() > 0) {
                    deviceActivityIntent.putExtra("metadata", (HashMap) event.getMetadata());
                    startActivity(deviceActivityIntent);
                }
            }
        });

        // Lookup the swipe container view
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAttributes(adapter);
                swipeContainer.setRefreshing(false);

            }
        });

        final Runnable refreshTask = new Runnable() {
            @Override
            public void run() {
                refreshAttributes(adapter);
            }
        };

        Executors.newSingleThreadExecutor().execute(refreshTask);
    }

    private void refreshAttributes(final EventLogAdapter adapter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = ProgressDialog.show(EventLogActivity.this, "", "Please wait, loading events...", true);
            }
        });
        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                adapter.clear();
                lion.getMole().getEventLog(Consistency.ONLINE, 100).subscribeOn(Schedulers.io()).subscribe(
                        new Action1<Event>() {
                            @Override
                            public void call(final Event event) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(event);
                                    }
                                });
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                logger.error(throwable.getMessage(), throwable);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                        }
                                        Toast.makeText(EventLogActivity.this, "Error retrieving event log, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                );
            }
        });
    }
}

