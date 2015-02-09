package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class ExcludedListActivity extends Activity {

    ListView listView;
    ArrayList<String> values = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    EditText editText;
    final Context context = this;

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

    }

    public void addExcludedButtonOnClick(View view){
        Log.d("nibbler", "addExcludedButton onClick");
        values.add(editText.getText().toString());
        editText.setText("");
        adapter.notifyDataSetChanged();
    }
}
