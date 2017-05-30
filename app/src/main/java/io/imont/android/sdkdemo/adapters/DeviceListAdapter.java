/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 27/06/2016.
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.cairo.events.*;
import io.imont.ferret.client.FerretMember;
import io.imont.lion.api.Device;
import io.imont.mole.client.Event;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.imont.cairo.events.Hardware.*;
import static io.imont.ferret.client.FerretMember.ConnectionStatus.CONNECTED;

public class DeviceListAdapter extends ArrayAdapter<Device> {

    private final List<Device> devices;

    private Map<String, Drawable> icons;

    private Map<Integer, ViewHolder> cachedViews = new HashMap<>();

    private boolean refreshConnectionInfoOnNextLoad;

    private static class ViewHolder {
        private View rowView;
        private boolean refreshNext;
    }

    public DeviceListAdapter(final Context context, final List<Device> devices) {
        super(context, -1, devices);
        this.devices = devices;

        icons = new HashMap<>();

        icons.put(Hub.HUB_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_rpi, null));
        icons.put("ANDROID_APP", getContext().getResources().getDrawable(R.drawable.devices_android, null));
        icons.put("IPHONE_APP", getContext().getResources().getDrawable(R.drawable.devices_iphone, null));
        icons.put(OnOff.ON_OFF_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_plug, null));
        icons.put(Video.VIDEO.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_camera, null));
        icons.put(PushButton.PUSH_BUTTON_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_button, null));
        icons.put(OpenClosed.OPEN_CLOSED_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_doorsensor, null));
        icons.put(Motion.MOTION_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_motion, null));
        icons.put(Thermostat.THERMOSTAT_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_thermostat, null));
        icons.put(ZigBeeCoordinator.ZIGBEE_COORDINATOR_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_zigbee, null));
        icons.put(Temperature.TEMPERATURE_FEATURE.getEventKey(), getContext().getResources().getDrawable(R.drawable.devices_sensortag, null));
    }

    @Override
    public void clear() {
        super.clear();
        cachedViews.clear();
    }

    public void refreshConnectionInfo() {
        for (ViewHolder vh : cachedViews.values()) {
            vh.refreshNext = true;
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = cachedViews.get(position);
        View rowView;
        if (holder == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.owned_devices_in_list_view, parent, false);
            holder = new ViewHolder();
            holder.rowView = rowView;
            cachedViews.put(position, holder);
        } else {
            if (holder.refreshNext) {
                holder.refreshNext = false;
                rowView = holder.rowView;
            } else {
                return holder.rowView;
            }
        }
        final TextView name = (TextView) rowView.findViewById(R.id.device_name);
        final ImageView img = (ImageView) rowView.findViewById(R.id.device_icon);
        final ImageView bridgedDeviceImg = (ImageView) rowView.findViewById(R.id.bridged_device);
        final TextView description = (TextView) rowView.findViewById(R.id.device_description);

        final Device device = devices.get(position);

        Observable.fromCallable(new Callable<Event>() {
            @Override
            public Event call() throws Exception {
                return device.getLatestEvent(DEVICE_ADDED_EVENT.getFQEventKey());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Event>() {
            @Override
            public void call(final Event deviceAdded) {
                boolean bridged = device.getType() == Device.Type.BRIDGED;

                FerretMember ferretMember = device.getOwningPeer();
                FerretMember.ConnectionStatus status = ferretMember.getConnectionStatus();
                FerretMember.ConnectionProtocol protocol = ferretMember.getConnectionProtocol();

                name.setText(toCamelCase(deviceAdded.getValue()));

                setIcon(deviceAdded, img);

                if (deviceAdded.getMetadata() != null) {
                    String detail = deviceAdded.getMetadata().get(DEVICE_ADDED_MANUFACTURER_META.getMetaKey())
                            + " / "
                            + deviceAdded.getMetadata().get(DEVICE_ADDED_MODEL_META.getMetaKey());
                    description.setText(detail);
                }

                if (bridged) {
                    bridgedDeviceImg.setVisibility(View.VISIBLE);
                }

                if (device.isLocal() && device.getType() == Device.Type.PEER) {
                    name.setText(name.getText() + " (this device)");
                    name.setTextColor(Color.BLACK);
                    description.setTextColor(Color.BLACK);
                } else {
                    switch (status) {
                        case UNKNOWN:
                            name.setTextColor(Color.GRAY);
                            description.setTextColor(Color.GRAY);
                            break;
                        case CONNECTED:
                            name.setTextColor(Color.BLACK);
                            description.setTextColor(Color.BLACK);
                            break;
                        case CONNECTING:
                            name.setTextColor(Color.YELLOW);
                            description.setTextColor(Color.YELLOW);
                            break;
                        case DISCONNECTED:
                            name.setTextColor(Color.RED);
                            description.setTextColor(Color.RED);
                            break;
                    }

                    if (!bridged && status == CONNECTED) {
                        switch (protocol) {
                            case UDP4:
                            case UDP6:
                                name.setText(name.getText() + " (Conn: UDP)");
                                break;
                            case TCP4:
                            case TCP6:
                                name.setText(name.getText() + " (Conn: TCP)");
                                break;
                        }
                    }
                }
            }
        });

        return rowView;
    }

    private void setIcon(final Event deviceAddedEvent, final ImageView view) {
        Drawable img = icons.get(deviceAddedEvent.getValue());
        if (img != null) {
            view.setImageDrawable(img);
        }
    }

    private String toCamelCase(String s){
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString = camelCaseString + toProperCase(part) + " ";
        }
        return camelCaseString.trim();
    }

    private String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}
