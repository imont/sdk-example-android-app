/*
 * Copyright 2017 IMONT Technologies Limited
 */
package io.imont.android.sdkdemo.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import io.imont.android.sdkdemo.R;
import io.imont.cairo.events.OnOff;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.api.Device;
import io.imont.lion.api.DeviceId;
import io.imont.lion.rules.actions.RaiseEventAction;
import io.imont.lion.rules.actions.ToggleSwitchAction;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.Serializable;
import java.util.*;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ActionParamHelper {

    public abstract static class ParamConfigHolder {
        public final View view;

        public ParamConfigHolder(final View view) {
            this.view = view;
        }

        public abstract Serializable[] getParams();
    }

    public static ParamConfigHolder getParamConfigView(final String actionKey, final Context context) {
        if (Objects.equals(actionKey, ToggleSwitchAction.ACTION_KEY)) {
            return handleToggle(context);
        } else if (Objects.equals(actionKey, RaiseEventAction.ACTION_KEY)) {
            return handleRaiseEvent(context);
        }
        return null;
    }

    private static ParamConfigHolder handleToggle(final Context context) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.rule_action_params_device_list, null);
        final Spinner deviceList = (Spinner) view.findViewById(R.id.params_device_list);

        final List<String> devices = new ArrayList<>();
        final ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, devices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceList.setAdapter(deviceAdapter);
        AndroidLionLoader.getLion(context).map(new Func1<Lion, Map<DeviceId, Device>>() {
            @Override
            public Map<DeviceId, Device> call(final Lion lion) {
                return lion.getAllDevices();
            }
        }).subscribe(new Action1<Map<DeviceId, Device>>() {
            @Override
            public void call(final Map<DeviceId, Device> deviceIdDeviceMap) {
                for (Device device : deviceIdDeviceMap.values()) {
                    if (device.getState().get(OnOff.ON_OFF_EVENT.getFQEventKey()) != null) {
                        devices.add(device.getId().getEntityId());
                    }
                }
                deviceAdapter.notifyDataSetChanged();
            }
        });

        return new ParamConfigHolder(view) {
            @Override
            public Serializable[] getParams() {
                return new Serializable[] { devices.get(deviceList.getSelectedItemPosition()) };
            }
        };
    }

    private static ParamConfigHolder handleRaiseEvent(final Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.rule_action_params_raise_event, null);

        final EditText key = (EditText) view.findViewById(R.id.event_key_text);
        final EditText value = (EditText) view.findViewById(R.id.event_value_text);

        return new ParamConfigHolder(view) {
            @Override
            public Serializable[] getParams() {
                return new Serializable[] {key.getText().toString(), value.getText().toString()};
            }
        };
    }
}
