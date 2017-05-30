package io.imont.android.sdkdemo;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.HashMap;

import io.imont.android.sdkdemo.adapters.EventAttributesAdapter;
import io.imont.cairo.EventMetaKey;

public class EventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView attributeList = (ListView) findViewById(R.id.device_attributes);

        Bundle params = getIntent().getExtras();
        HashMap<EventMetaKey, String> metadata = (HashMap) params.getSerializable("metadata");

        final EventAttributesAdapter adapter = new EventAttributesAdapter(metadata);
        attributeList.setAdapter(adapter);

        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Nothing to do here as metadata for a specific event can't be updated.
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
