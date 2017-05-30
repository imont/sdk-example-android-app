/*
 * Copyright (C) 2016 IMONT Technologies
 *
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.imont.android.sdkdemo.FindDevicesActivity;
import io.imont.android.sdkdemo.R;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.lion.network.DeviceCandidate;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FoundDevicesAdapter extends ArrayAdapter<DeviceCandidate> {

    private static final Logger log = LoggerFactory.getLogger(FoundDevicesAdapter.class);

    private final String peerId;

    private List<DeviceCandidate> devices;

    private FindDevicesActivity activity;

    public FoundDevicesAdapter(final Context context, final String peerId,
                               final List<DeviceCandidate> devices, FindDevicesActivity activity) {
        super(context, -1, devices);
        this.peerId = peerId;
        this.devices = devices;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.found_device_in_list_view, parent, false);

        ImageView image = (ImageView) rowView.findViewById(R.id.found_device_icon);
        TextView name = (TextView) rowView.findViewById(R.id.found_device_name);
        final TextView description = (TextView) rowView.findViewById(R.id.found_device_description);

        image.setImageDrawable(getContext().getDrawable(R.drawable.devices_bridged));
        final DeviceCandidate dev = devices.get(position);

        name.setText(createName(dev));
        description.setText(createDescription(dev));

        final Button button = (Button) rowView.findViewById(R.id.acquire_device_button);
        if (!dev.getAutoAcquired()) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AndroidLionLoader.getLion(getContext()).subscribe(new Action1<Lion>() {
                        @Override
                        public void call(final Lion lion) {
                            lion.registerDevice(peerId, dev).subscribeOn(Schedulers.io()).limit(1).timeout(30, TimeUnit.SECONDS).subscribe(
                                    new Action1<Device>() {
                                        @Override
                                        public void call(final Device device) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    button.setText("Acquired");
                                                    Toast.makeText(getContext(), dev.getFriendlyName() + " added successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    },
                                    new Action1<Throwable>() {
                                        @Override
                                        public void call(final Throwable throwable) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    log.error("Error acquiring device", throwable);
                                                    button.setText("Error");
                                                    Toast.makeText(getContext(), "Error acquiring device " + dev.getFriendlyName(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                            button.setEnabled(false);
                            button.setText("Acquiring");
                        }
                    });
                }
            });
        } else {
            button.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }

    private String createName(DeviceCandidate dc) {
        String descStr = "";
        if (dc.getDriverSpec().getManufacturer() != null && !dc.getDriverSpec().getManufacturer().equals("")) {
            descStr += dc.getDriverSpec().getManufacturer();
        }
        if (dc.getDriverSpec().getModel() != null && !dc.getDriverSpec().getModel().equals("")) {
            descStr += dc.getDriverSpec().getModel();
        }
        if (dc.getFriendlyName() != null) {
            descStr += dc.getFriendlyName();
        }
        return descStr;
    }

    private String createDescription(DeviceCandidate dc) {
        return String.format("%s: %s", dc.getNetwork(), dc.getAddress());
    }
}
