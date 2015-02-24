package com.nibbler.email2sms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by nn-admin on 30.01.2015.
 */
public class SettingsActivity extends Activity{

    ListView listView;
    ArrayList<String> values = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "This is Settings onCreate");
        final Context context = this;

        listView = (ListView)findViewById(R.id.settingsListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int selectedPosition = position;

                Intent intent = null;
                switch (selectedPosition) {
                    case 0:
                        intent = new Intent(context, GeneralSettingsActivity.class);
                        break;
                    case 1:
                        intent = new Intent(context, POP3SettingsActivity.class);
                        break;
                    case 2:
                        intent = new Intent(context, AddressesListActivity.class);
                        break;
                    case 3:
                        intent = new Intent(context, ExcludedListActivity.class);
                        break;
                    case 4:
                        intent = new Intent(context, UnreadSymbolsActivity.class);
                        break;
                    case 5:
                        intent = new Intent(context, TimetableActivity.class);
                        break;
                    case 6:
                        intent = new Intent(context, SystemNotificationActivity.class);
                        break;
                    default:
                        Log.d("nibbler", "something is wrong in ListView settings");
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                }


            }
        });

        adapter.addAll(
                "Общие настройки",
                "Настройки почты",
                "Адреса получателей",
                "Список исключений",
                "Нечитаемые знаки",
                "Расписание",
                "Системные уведомления"
        );

        adapter.notifyDataSetChanged();
    }
}
