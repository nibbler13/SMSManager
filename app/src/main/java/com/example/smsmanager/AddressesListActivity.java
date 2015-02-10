package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class AddressesListActivity extends Activity {

    ListView listView;
    ArrayList<String> values = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Set<String> strings;
    EditText editText;
    final Context context = this;
    SharedPreferences sharedPreferences;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addresses_list_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "AddressesListActivity activity onCreate");

        editText = (EditText)findViewById(R.id.addressesEditText);

        listView = (ListView)findViewById(R.id.addressesListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int selectedPosition = position;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
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
        strings = sharedPreferences.getStringSet(getString(R.string.addresses_list), new HashSet<String>(Arrays.asList(new String[]{"1", "3", "5"})));

        Log.d("nibbler", "strings length: " + Integer.toString(strings.size()));
        if (strings.size() > 0){
            String[] stringToParse = strings.toArray(new String[0]);Log.d("nibbler", "stringToParseLength:" + Integer.toString(stringToParse.length));
            for (int i = 0; i < strings.size(); i++) {
                Log.d("nibbler", "element " + Integer.toString(i) + " contain: " + stringToParse[i]);
                adapter.add(stringToParse[i]);
            }
            adapter.sort(new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            adapter.notifyDataSetChanged();
        }
    }

    public void addAddressesButtonOnClick(View view){
        Log.d("nibbler", "addAddressesButton onClick");
        if (editText.getText().toString().equalsIgnoreCase("")){
            return;
        }
        if (!editText.getText().toString().contains("=")){
            Toast.makeText(getApplicationContext(), "Введенная строка не содержит символ '='", Toast.LENGTH_LONG).show();
            return;
        }
        String[] separatedString = editText.getText().toString().split("=", 2);
        Log.d("nibbler", "0: " + separatedString[0] + " 1: " + separatedString[1]);
        if (separatedString[0].length() == 0 || separatedString[0].trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Левая часть выражения не может быть пустой", Toast.LENGTH_LONG).show();
            return;
        }
        if (!separatedString[1].matches("^[0-9]{7,20}$") || separatedString[1].contains("=")){
            Toast.makeText(getApplicationContext(), "Правая часть выражения не соответствует требованиям (можно только цифры)", Toast.LENGTH_LONG).show();
            return;
        }
        values.add(editText.getText().toString());
        editText.setText("");
        adapter.notifyDataSetChanged();
    }

    public void addressesInfoOnClick(View view){
        Log.d("nibbler", "addressesInfoOnClick");

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
        builder.setMessage("Если в теме сообщения будет присутствовать строка из списка, то СМС сообщение будет отправлено на указанный номер. Строка может быть набором любых символов, после строки ставится знак = и далее номер телефона. Например \"нижний=89601811873\"").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }
}
