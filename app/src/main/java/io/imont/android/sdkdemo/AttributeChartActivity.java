package io.imont.android.sdkdemo;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import io.imont.android.sdkdemo.utils.TimeSeriesDBHelper;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.view.LineChartView;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AttributeChartActivity extends AppCompatActivity {

    private static final int MAX_PAGES = 50;
    private static final int PAGE_SIZE = 100;

    private String entityId;
    private String eventKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle params = getIntent().getExtras();
        entityId = params.getString("entityId");
        eventKey = params.getString("eventKey");

        final Button lastHourBtn = (Button) findViewById(R.id.last_hour_btn);
        final Button lastDayBtn = (Button) findViewById(R.id.last_day_btn);
        final Button lastMonthBtn = (Button) findViewById(R.id.last_month_btn);

        lastHourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadChart(new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(60)), "%H:%M");
            }
        });

        lastDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadChart(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)), "%d %H");
            }
        });

        lastMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadChart(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)), "%d");
            }
        });

        // Last hour by default
        loadChart(new Date(System.currentTimeMillis() - (60 * 60 * 1000)), "%H:%M");
    }

    private void loadChart(final Date fromDate, final String groupBy) {
        final TimeSeriesDBHelper dbHelper = new TimeSeriesDBHelper(this);
        final Snackbar bar = Snackbar.make(findViewById(R.id.last_day_btn), "Please wait...", Snackbar.LENGTH_INDEFINITE);
        bar.show();

        final AtomicBoolean status = new AtomicBoolean(false);
        latestSequence(entityId, eventKey)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Long, Observable<Event>>() {
            @Override
            public Observable<Event> call(final Long latest) {
                return Observable.range(0, MAX_PAGES).takeUntil(finished(status)).flatMap(new Func1<Integer, Observable<Event>>() {
                    @Override
                    public Observable<Event> call(final Integer page) {
                        long to = latest - (page * PAGE_SIZE);
                        if (to <= 0) {
                            return Observable.empty();
                        }
                        long from = latest - ((page + 1) * PAGE_SIZE);
                        if (from < 0) {
                            from = 0;
                        }
                        return fetch(entityId, eventKey, from, to);
                    }
                });
            }
        }).doAfterTerminate(hideSnackbar(bar)).subscribe(
                consume(fromDate, status, dbHelper),
                handleErrors(),
                complete(dbHelper, groupBy)
        );
    }

    private Observable<Long> latestSequence(final String entityId, final String eventKey) {
        return AndroidLionLoader.getLion(this).map(new Func1<Lion, Long>() {
            @Override
            public Long call(final Lion lion) {
                try {
                    Event latest = lion.getMole().getState(entityId, eventKey);
                    if (latest != null) {
                        return latest.getId().getPeerKeySequence();
                    }
                } catch (MoleException e) {
                    throw new RuntimeException(e);
                }
                throw new IllegalArgumentException("No such entity / event");
            }
        });
    }

    private Observable<Event> fetch(final String entityId, final String eventKey, final long from, final long to) {
        return AndroidLionLoader.getLion(this).flatMap(new Func1<Lion, Observable<Event>>() {
            @Override
            public Observable<Event> call(final Lion lion) {
                try {
                    return Observable.from(lion.getMole().getHistoryForEvent(entityId, eventKey, from, to));
                } catch (MoleException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Func1<Integer, Boolean> finished(final AtomicBoolean status) {
        return new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(final Integer integer) {
                return status.get();
            }
        };
    }

    private Action1<Event> consume(final Date fromDate, final AtomicBoolean status, final TimeSeriesDBHelper dbHelper) {
        return new Action1<Event>() {
            @Override
            public void call(final Event event) {
                if (event.getReportedDate().before(fromDate)) {
                    status.set(true);
                    return;
                }
                dbHelper.addData(event.getReportedDate(), Double.valueOf(event.getValue()));
            }
        };
    }

    private Action1<Throwable> handleErrors() {
        return new Action1<Throwable>() {
            @Override
            public void call(final Throwable throwable) {
                Toast.makeText(AttributeChartActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Action0 complete(final TimeSeriesDBHelper dbHelper, final String groupBy) {
        return new Action0() {
            @Override
            public void call() {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                final LineChartView view = (LineChartView) findViewById(R.id.chart);

                                final List<PointValue> values = new ArrayList<>();
                                final List<AxisValue> axisValues = new ArrayList<>();

                                int point = 0;
                                Cursor crs = dbHelper.fetch(groupBy);
                                while (crs.moveToNext()) {
                                    values.add(new PointValue(point, crs.getFloat(1)));
                                    axisValues.add(new AxisValue(point).setLabel(crs.getString(0)));
                                    point++;
                                }
                                Line line = new Line(values).setColor(Color.BLUE).setCubic(true);
                                List<Line> lines = new ArrayList<>();
                                lines.add(line);

                                LineChartData data = new LineChartData();
                                data.setLines(lines);
                                data.setAxisXBottom(new Axis(axisValues).setHasLines(true).setMaxLabelChars(3).setHasTiltedLabels(true));
                                data.setAxisYLeft(new Axis().setHasLines(true).setInside(true));

                                view.setLineChartData(data);
                            }
                        }
                );
            }
        };
    }

    private Action0 hideSnackbar(final Snackbar bar) {
        return new Action0() {
            @Override
            public void call() {
                bar.dismiss();
            }
        };
    }

}
