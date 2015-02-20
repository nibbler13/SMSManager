package com.nibbler.email2sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by nn-admin on 30.01.2015.
 */
public class LogViewActivity extends Activity{

    TextView logTextView;
    LogFile logFile;
    final Context context = this;
    final android.os.Handler handler = new android.os.Handler();
    String textFromLogFile;
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Log.d("nibbler", "logViewActivity runnable.run");
            updateResultsInUi();
        }
    };


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logview_layout);
        logFile = new LogFile(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Log.d("nibbler", "This is LogView onCreate");

        logTextView = (TextView)findViewById(R.id.logViewTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());

        Thread thread = new Thread(){
            public void run(){
                //Log.d("nibbler", "logViewActivity thread.run");
                textFromLogFile = logFile.getLogFileText();
                handler.post(runnable);
            }
        };
        thread.start();

    }

    private void updateResultsInUi(){
        //Log.d("nibbler", "logViewActivity updateResultsInUi");
        if (!textFromLogFile.equalsIgnoreCase("")){
            logTextView.setText(textFromLogFile);
        } else {
            logTextView.setText("Лог-файл не найден");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_view_menu, menu);
        return true;
    }

    public void helpActionBarClicked(MenuItem item){
        //Log.d("nibbler", "helpActionBarClicked");

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
        builder.setMessage("Для удаленного получения лог-файла в настройках в разделе \"Настройки POP3\" нужно включить опцию \"Отправлять журнал работы по почте\" и на указанный адрес почты выслать письмо с темой \"log\". В ответ придет письмо с вложением в виде CSV файла. Лог-файл хранится либо в папке приложения, либо на карте памяти в папке /Email2SMS/Log.csv.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

    public void senLogActionBarClicked(MenuItem item){
        //Log.d("nibbler", "logViewActivity senLogToEmailClicked");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Отправить лог-файл");
        alert.setMessage("Укажите адрес e-mail на который нужно выслать лог-файл");

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint("example@company.com");
        alert.setView(editText);

        alert.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = editText.getText().toString();
                Log.d("nibbler", "logView editText:" + value);
                ////
                //ОТПРАВКА ФАЙЛА
                ////
            }
        });

        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void removeActionBarOnClicked(MenuItem item){
        //Log.d("nibbler", "logViewActivity removeActionBarOnClicked");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (logFile.deleteLogFile()){
                            Toast.makeText(getApplicationContext(), "Лог-файл удален", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Не удалось удалить файл", Toast.LENGTH_LONG).show();
                        }
                        textFromLogFile = logFile.getLogFileText();
                        updateResultsInUi();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Вы действительно хотите очистить журнал работы? Это так же удалит лог-файл в памяти устройства.").setPositiveButton("Да, очистить", dialogClickListener).setNegativeButton("Нет", dialogClickListener).show();
    }
}
