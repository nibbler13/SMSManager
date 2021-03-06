package com.nibbler.email2sms;

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
        strings = new HashSet<String>(sharedPreferences.getStringSet(getString(R.string.addresses_list), new HashSet<String>(Arrays.asList(new String[]{""}))));

        if (strings.size() > 0){
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

    public void addAddressesButtonOnClick(View view){
        Log.d("nibbler", "addAddressesButton onClick");
        if (editText.getText().toString().isEmpty()){
            return;
        }
        if (!editText.getText().toString().contains("=")){
            Toast.makeText(getApplicationContext(), "Строка не содержит символ '='", Toast.LENGTH_LONG).show();
            return;
        }
        String[] separatedString = editText.getText().toString().split("=", 2);
        Log.d("nibbler", "0: " + separatedString[0] + " 1: " + separatedString[1]);
        if (separatedString[0].length() == 0 || separatedString[0].trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Левая часть выражения не может быть пустой", Toast.LENGTH_LONG).show();
            return;
        }
        if (!separatedString[1].matches("^[0-9]{7,20}$") || separatedString[1].contains("=")){
            Toast.makeText(getApplicationContext(), "Правая часть выражения не соответствует требованиям (можно только цифры, длиной от 7 до 20)", Toast.LENGTH_LONG).show();
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
        builder.setMessage("Если в теме сообщения будет присутствовать строка из списка, то СМС сообщение будет отправлено на указанный номер. Строка может быть набором любых символов, после строки ставится знак = и далее номер телефона. Например \"нижний=89601811873\".").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

    public void onPause() {
        super.onPause();
        Log.d("nibbler", "Addresses onPause");

        strings.clear();
        if (adapter.getCount() > 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                strings.add(adapter.getItem(i));
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(getString(R.string.addresses_list), strings).apply();
    }
}
