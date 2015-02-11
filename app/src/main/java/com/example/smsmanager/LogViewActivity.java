package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by nn-admin on 30.01.2015.
 */
public class LogViewActivity extends Activity{

    TextView logTextView;
    final Context context = this;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logview_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "This is LogView onCreate");

        logTextView = (TextView)findViewById(R.id.logViewTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
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
        builder.setMessage("Для получения лог-файла в настройках в разделе \"Настройки POP3\" нужно включить опцию \"Отправлять журнал работы по почте\" и на указанный адрес почты выслать письмо с темой \"log\". В ответ придет письмо с вложением в виде CSV файла. Лог-файл хранится либо в папке приложения, либо на карте памяти в папке /Email2SMS/Log.csv.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }
}
