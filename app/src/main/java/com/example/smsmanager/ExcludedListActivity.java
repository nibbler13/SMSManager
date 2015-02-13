package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class ExcludedListActivity extends Activity {

    ListView listView;
    ArrayList<String> values = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Set<String> strings;
    EditText editText;
    final Context context = this;
    SharedPreferences sharedPreferences;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excluded_list_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "ExcludedListActivity activity onCreate");

        editText = (EditText)findViewById(R.id.excludedEditText);

        listView = (ListView)findViewById(R.id.excludedListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int selectedPosition = position;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                values.remove(selectedPosition);
                                adapter.notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Вы действительно хотите удалить элемент '" + values.get(position).toString() + "'?").setPositiveButton("Да", dialogClickListener).setNegativeButton("Нет", dialogClickListener).show();
            }
        });

        sharedPreferences = context.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        strings = new HashSet<String>(sharedPreferences.getStringSet(getString(R.string.excluded_list), new HashSet<String>(Arrays.asList(new String[]{"1", "3", "5"}))));

        if (strings.size() > 0) {
            String[] stringToParse = strings.toArray(new String[0]);
            adapter.addAll(stringToParse);
            adapter.sort(new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            adapter.notifyDataSetChanged();
        }

    }

    public void addExcludedButtonOnClick(View view){
        Log.d("nibbler", "addExcludedButton onClick");
        if (editText.getText().toString().isEmpty()){
            return;
        }
        values.add(editText.getText().toString());
        editText.setText("");
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    public void helpActionBarClicked(MenuItem item){
        Log.d("nibbler", "helpActionBarClicked");

        DialogInterface.OnClickListener dialogClickListenerInfo = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Если сообщение будет содержать хотя бы одну строку из списка, то такое сообщение будет игнорировано и не будет выслано получателю в виде СМС.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

    public void onPause() {
        super.onPause();
        Log.d("nibbler", "excluded onPause");

        strings.clear();
        if (adapter.getCount() > 0) {
            for (int i = 0; i < adapter.getCount(); i++){
                strings.add(adapter.getItem(i));
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(getString(R.string.excluded_list), strings).apply();
    }
}
