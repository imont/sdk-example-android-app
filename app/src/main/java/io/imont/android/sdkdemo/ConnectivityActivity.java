/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import io.imont.android.sdkdemo.adapters.ConnEventListAdapter;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.ferret.client.status.ConnectivityEvent;
import io.imont.lion.Lion;
import rx.functions.Action1;

import java.util.ArrayList;

public class ConnectivityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView connList = (ListView) findViewById(R.id.connectivity_events_list);

        final ConnEventListAdapter adapter = new ConnEventListAdapter(this, new ArrayList<ConnectivityEvent>());
        adapter.setNotifyOnChange(false);
        connList.setAdapter(adapter);

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                lion.getFerret().eventStream().subscribe(new Action1<ConnectivityEvent>() {
                    @Override
                    public void call(final ConnectivityEvent connectivityEvent) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(connectivityEvent);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
    }

}
