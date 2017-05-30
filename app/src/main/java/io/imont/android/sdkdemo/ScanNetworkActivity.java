package io.imont.android.sdkdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import io.imont.android.sdkdemo.adapters.FoundCandidatesAdapter;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.ferret.client.FerretClient;
import io.imont.ferret.client.mesh.Candidate;
import io.imont.ferret.client.mesh.JoinResult;
import io.imont.lion.Lion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScanNetworkActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(ScanNetworkActivity.class);

    private Executor joinOfferExecutor = Executors.newSingleThreadExecutor();

    private ProgressDialog joinProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_network);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent devicesListIntent = new Intent(this, DevicesActivity.class);

        final ProgressBar spinner = (ProgressBar) findViewById(R.id.scan_network_progress);

        final ListView deviceList = (ListView) findViewById(R.id.found_device_list);

        final FoundCandidatesAdapter adapter = new FoundCandidatesAdapter(this, new ArrayList<Candidate>());
        adapter.setNotifyOnChange(true);
        deviceList.setAdapter(adapter);

        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                lion.getFerret().startDiscovering().onBackpressureBuffer().subscribe(new Action1<Candidate>() {
                    @Override
                    public void call(final Candidate candidate) {
                        System.out.println("GOT CANDIDATE: " + candidate);
                    }
                });
                lion.getFerret().startDiscovering().subscribe(
                        new Action1<Candidate>() {
                            @Override
                            public void call(final Candidate candidate) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.add(candidate);
                                        spinner.setVisibility(View.GONE);
                                    }
                                });
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable throwable) {
                                logger.error("Error while discovering", throwable);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ScanNetworkActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                logger.error("Discovery finished abruptly");
                            }
                        }
                );
            }
        });

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                joinOfferExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Candidate candidate = adapter.getItem(position);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                joinProgress = ProgressDialog.show(ScanNetworkActivity.this, "", "Please wait. Adding peer...", true);
                            }
                        });
                        AndroidLionLoader.getLion(ScanNetworkActivity.this).map(new Func1<Lion, FerretClient>() {
                            @Override
                            public FerretClient call(final Lion lion) {
                                return lion.getFerret();
                            }
                        }).subscribe(new Action1<FerretClient>() {
                            @Override
                            public void call(final FerretClient ferret) {
                                ferret.offerPair(candidate).subscribe(new Action1<JoinResult>() {
                                    @Override
                                    public void call(final JoinResult joinResult) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (joinProgress != null) {
                                                    joinProgress.dismiss();
                                                }
                                                String message = "Added!";
                                                if (!joinResult.isJoined()) {
                                                    message = "Failed to add: " + joinResult.getErrorReason();
                                                }
                                                Toast.makeText(ScanNetworkActivity.this, message, Toast.LENGTH_SHORT).show();
                                                startActivity(devicesListIntent);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });


    }
}
