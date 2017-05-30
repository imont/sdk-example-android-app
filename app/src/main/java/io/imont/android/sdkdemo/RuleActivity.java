package io.imont.android.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import io.imont.android.sdkdemo.adapters.RuleActionAdapter;
import io.imont.android.sdkdemo.adapters.RuleConditionAdapter;
import io.imont.android.sdkdemo.helpers.ActionConditionHelper;
import io.imont.android.sdkdemo.helpers.ActionParamHelper;
import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.android.sdkdemo.rules.RuleAction;
import io.imont.android.sdkdemo.rules.RuleCondition;
import io.imont.android.sdkdemo.rules.RuleMapper;
import io.imont.android.sdkdemo.utils.Resources;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.rules.Rule;
import io.imont.lion.rules.schema.RuleConditionV1;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static android.widget.AdapterView.INVALID_POSITION;

public class RuleActivity extends AppCompatActivity {

    private List<RuleCondition> conditions = new ArrayList<>();
    private List<RuleAction> actions = new ArrayList<>();

    private RuleConditionAdapter conditionAdapter;
    private RuleActionAdapter actionAdapter;

    private String peerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle params = getIntent().getExtras();
        peerId = params.getString("entityId");

        // Conditions
        ListView conditionList = (ListView) findViewById(R.id.condition_list);
        conditionAdapter = new RuleConditionAdapter(this, conditions, addConditionListener(), removeConditionListener());
        conditionList.setAdapter(conditionAdapter);
        conditionAdapter.notifyDataSetChanged();

        // Actions
        ListView actionList = (ListView) findViewById(R.id.action_list);
        actionAdapter = new RuleActionAdapter(this, actions, addActionListener(), removeActionListener());
        actionList.setAdapter(actionAdapter);
        actionAdapter.notifyDataSetChanged();

        // Save
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.save_rule_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveRule();
            }
        });
    }

    private void saveRule() {
        TextView nameView = (TextView) findViewById(R.id.rule_name);
        final String name = nameView.getText().toString();
        final boolean allOf = Objects.equals("All", ((Spinner) findViewById(R.id.condition_spinner)).getSelectedItem());
        if (name.isEmpty()) {
            Toast.makeText(this, "Please specify a name for your rule", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hasConditions() || !hasActions()) {
            Toast.makeText(this, "Please specify a condition and an action", Toast.LENGTH_SHORT).show();
            return;
        }

        final Snackbar bar = Snackbar.make(nameView, "Please wait...", Snackbar.LENGTH_LONG);
        bar.show();

        AndroidLionLoader.getLion(this).subscribeOn(Schedulers.io()).flatMap(new Func1<Lion, Observable<Rule>>() {
            @Override
            public Observable<Rule> call(final Lion lion) {
                return lion.getRuleEngine(peerId).createRule(name, toRuleCondition(allOf), actions.get(0).getAction(), actions.get(0).getParameters());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(
                new Action1<Rule>() {
                    @Override
                    public void call(final Rule rule) {
                        bar.dismiss();
                        Toast.makeText(RuleActivity.this, "Rule saved", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(RuleActivity.this, RulesActivity.class);
                        i.putExtra("entityId", peerId);
                        startActivity(i);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        bar.dismiss();
                        Toast.makeText(RuleActivity.this, "Failed saving rule: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private boolean hasConditions() {
        for (RuleCondition c : conditions) {
            if (c.getEntityQuery() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActions() {
        for (RuleAction a : actions) {
            if (a.getAction() != null) {
                return true;
            }
        }
        return false;
    }

    private RuleConditionV1 toRuleCondition(boolean allOf) {
        RuleConditionV1 cond = new RuleConditionV1();
        List<RuleConditionV1> sub = new ArrayList<>();
        for (RuleCondition c : conditions) {
            sub.add(new RuleConditionV1().withAllOf(Arrays.asList(c.getAttributeQueryCondition(), c.getEntityQuery().getCondition())));
        }
        if (allOf) {
            cond.withAllOf(sub);
        } else {
            cond.withAnyOf(sub);
        }
        return cond;
    }

    private AdapterView.OnClickListener addConditionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAddConditionPopup();
            }
        };
    }

    private AdapterView.OnClickListener removeConditionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                conditions.remove((int) v.getTag());
                conditionAdapter.notifyDataSetChanged();
            }
        };
    }

    private AdapterView.OnClickListener addActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAddActionPopup();
            }
        };
    }

    private AdapterView.OnClickListener removeActionListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                actions.remove((int) v.getTag());
                actionAdapter.notifyDataSetChanged();
            }
        };
    }

    private void showAddConditionPopup() {
        final AtomicReference<ActionConditionHelper.ParamConfigHolder> paramConfigHolder = new AtomicReference<>();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.rule_add_condition_popup, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        final ListView conditionList = (ListView) findViewById(R.id.condition_list);

        final Spinner devices = (Spinner) popupView.findViewById(R.id.device_selector);
        final Spinner events = (Spinner) popupView.findViewById(R.id.event_selector);
        Button saveButton = (Button) popupView.findViewById(R.id.save_button);
        final LinearLayout paramsLayout = (LinearLayout) popupView.findViewById(R.id.condition_params);

        final List<String> entities = RuleMapper.getTopLevelQueries();
        List<String> spinnerArray = new ArrayList<>();
        for (String q : entities) {
            spinnerArray.add(Resources.lookupStringResource(this, String.format("cond_%s", q.toLowerCase())));
        }
        final ArrayAdapter<String> entityListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        entityListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devices.setAdapter(entityListAdapter);

        final ArrayAdapter<String> eventListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        eventListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        events.setAdapter(eventListAdapter);

        devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                paramsLayout.removeAllViews();
                String q = entities.get(position);
                events.setAdapter(null);
                eventListAdapter.clear();
                List<String> attributeQueries = RuleMapper.getSubQueries(q);
                for (String attr : attributeQueries) {
                    eventListAdapter.add(Resources.lookupStringResource(RuleActivity.this, String.format("cond_%s", attr.toLowerCase())));
                }
                events.setAdapter(eventListAdapter);
                eventListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        events.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                paramsLayout.removeAllViews();
                int selectedEntity = devices.getSelectedItemPosition();
                String entityQuery = entities.get(selectedEntity);
                String attributeQuery = RuleMapper.getSubQueries(entityQuery).get(position);
                ActionConditionHelper.ParamConfigHolder holder = ActionConditionHelper.getParamConfigView(attributeQuery, RuleActivity.this);
                if (holder != null) {
                    paramsLayout.addView(holder.view);
                    paramConfigHolder.set(holder);
                } else {
                    paramConfigHolder.set(null);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                System.out.println("NOTHING");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int selectedEntity = devices.getSelectedItemPosition();
                int selectedEvent = events.getSelectedItemPosition();
                if (selectedEvent == INVALID_POSITION || selectedEntity == INVALID_POSITION) {
                    Toast.makeText(RuleActivity.this, "Please select a rule", Toast.LENGTH_SHORT).show();
                    return;
                }

                String entity = entities.get(selectedEntity);
                String attribute = RuleMapper.getSubQueries(entity).get(selectedEvent);

                AttributeQuery aq;
                ActionConditionHelper.ParamConfigHolder holder = paramConfigHolder.get();
                if (holder != null) {
                    aq = RuleMapper.toAttributeQuery(attribute, holder.getOperator(), holder.getValue());
                } else {
                    aq = RuleMapper.toAttributeQuery(attribute, null, null);
                }
                RuleCondition c = new RuleCondition(RuleMapper.toEntityQuery(entity), aq);

                conditions.add(c);
                conditionAdapter.notifyDataSetChanged();

                popupWindow.dismiss();
            }
        });

        conditionList.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(conditionList, Gravity.CENTER, 0, 0);
            }
        });
    }

    private void refreshAttributeConditions() {

    }

    private void showAddActionPopup() {
        final AtomicReference<ActionParamHelper.ParamConfigHolder> configHolderRef = new AtomicReference<>();
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.rule_add_action_popup, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        final ListView actionList = (ListView) findViewById(R.id.action_list);

        final LinearLayout paramsLayout = (LinearLayout) popupView.findViewById(R.id.action_params);
        paramsLayout.removeAllViews();
        final Spinner actionSpinner = (Spinner) popupView.findViewById(R.id.action_selector);
        final Button saveButton = (Button) popupView.findViewById(R.id.save_button);

        final List<String> spinnerArray = new ArrayList<>();

        final ArrayAdapter<String> actionListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray) {
            @Nullable
            @Override
            public String getItem(final int position) {
                String item = super.getItem(position);
                item = item == null ? "" : item.toLowerCase();
                return Resources.lookupStringResource(RuleActivity.this, String.format("action_%s", item));
            }
        };
        actionListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(actionListAdapter);

        buildActionList().subscribe(
                new Action1<String>() {
                    @Override
                    public void call(final String s) {
                        spinnerArray.add(s);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        // ignore
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        actionListAdapter.notifyDataSetChanged();
                    }
                }
        );

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int selectedAction = actionSpinner.getSelectedItemPosition();
                if (selectedAction == INVALID_POSITION) {
                    Toast.makeText(RuleActivity.this, "Please select a rule", Toast.LENGTH_SHORT).show();
                    return;
                }

                String action = spinnerArray.get(selectedAction);
                Serializable[] params = new Serializable[][] {};
                if (configHolderRef.get() != null) {
                    params = configHolderRef.get().getParams();
                }

                actions.add(new RuleAction(action, params));
                actionAdapter.notifyDataSetChanged();

                popupWindow.dismiss();
            }
        });

        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                paramsLayout.removeAllViews();
                String selected = spinnerArray.get(position);
                ActionParamHelper.ParamConfigHolder childView = ActionParamHelper.getParamConfigView(selected, RuleActivity.this);
                if (childView != null) {
                    paramsLayout.addView(childView.view);
                    configHolderRef.set(childView);
                } else {
                    configHolderRef.set(null);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        actionList.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(actionList, Gravity.CENTER, 0, 0);
            }
        });
    }

    private rx.Observable<String> buildActionList() {
        return AndroidLionLoader.getLion(this).flatMap(new Func1<Lion, rx.Observable<String>>() {
            @Override
            public rx.Observable<String> call(final Lion lion) {
                return lion.getRuleEngine(lion.getPeerId()).getActions();
            }
        });
    }

}
