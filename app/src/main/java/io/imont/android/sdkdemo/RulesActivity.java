/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import io.imont.android.sdkdemo.adapters.RulesAdapter;
import io.imont.lion.Lion;
import io.imont.lion.android.AndroidLionLoader;
import io.imont.lion.rules.Rule;
import rx.functions.Action1;

import java.util.ArrayList;

public class RulesActivity extends AppCompatActivity {

    private String peerId;
    private RulesAdapter rulesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Bundle params = getIntent().getExtras();
        peerId = params.getString("entityId");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_rule_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RulesActivity.this, RuleActivity.class);
                i.putExtra("entityId", peerId);
                startActivity(i);
            }
        });

        ListView rulesList = (ListView) findViewById(R.id.rules_list);
        rulesAdapter = new RulesAdapter(this, new ArrayList<Rule>());
        rulesList.setAdapter(rulesAdapter);

        reloadRulesList();

        rulesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                confirmDeleteRule(position);
                return true;
            }
        });
    }

    private void confirmDeleteRule(final int position) {
        final Rule rule = rulesAdapter.getItem(position);
        if (rule == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Please confirm")
                .setMessage(String.format("Are you sure you want to delete rule '%s'?",rule.getName()));

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AndroidLionLoader.getLion(RulesActivity.this).subscribe(new Action1<Lion>() {
                    @Override
                    public void call(final Lion lion) {
                        lion.getRuleEngine(peerId).deleteRule(rule.getId()).subscribe();
                        reloadRulesList();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void reloadRulesList() {
        AndroidLionLoader.getLion(this).subscribe(new Action1<Lion>() {
            @Override
            public void call(final Lion lion) {
                rulesAdapter.clear();
                rulesAdapter.addAll(lion.getRuleEngine(peerId).getAllRules());
                rulesAdapter.notifyDataSetChanged();
            }
        });
    }

}
