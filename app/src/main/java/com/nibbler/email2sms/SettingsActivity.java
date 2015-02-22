package com.nibbler.email2sms;

import android.app.Activity;
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

        listView = (ListView)findViewById(R.id.settingsListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int selectedPosition = position;

                switch (selectedPosition) {
                    case 0:
                        generalSettingsOnClick();
                        break;
                    case 1:
                        pop3SettingsOnClick();
                        break;
                    case 2:
                        addressesListOnClick();
                        break;
                    case 3:
                        excludedListOnClick();
                        break;
                    case 4:
                        unreadSymbolsOnClick();
                        break;
                    case 5:
                        timetableOnClick();
                        break;
                    default:
                        Log.d("nibbler", "something is wrong in ListView settings");
                        break;
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

    public void pop3SettingsOnClick(){
        Log.d("nibbler", "pop3SettingsOnClick");
        Intent intent = new Intent(this, POP3SettingsActivity.class);
        startActivity(intent);
    }

    public void addressesListOnClick(){
        Log.d("nibbler", "addressesListOnClick");
        Intent intent = new Intent(this, AddressesListActivity.class);
        startActivity(intent);
    }

    public void excludedListOnClick(){
        Log.d("nibbler", "excludedListOnClick");
        Intent intent = new Intent(this, ExcludedListActivity.class);
        startActivity(intent);
    }

    public void timetableOnClick(){
        Log.d("nibbler", "timetableOnClick");
        Intent intent = new Intent(this, TimetableActivity.class);
        startActivity(intent);
    }

    public void generalSettingsOnClick(){
        Log.d("nibbler", "generalSettingsOnClick");
        Intent intent = new Intent(this, GeneralSettingsActivity.class);
        startActivity(intent);
    }

    public void unreadSymbolsOnClick(){
        Log.d("nibbler", "unreadSymbolsButtonOnClick");
        Intent intent = new Intent(this, UnreadSymbolsActivity.class);
        startActivity(intent);
    }
}
