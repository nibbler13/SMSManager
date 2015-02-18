package com.nibbler.email2sms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Created by nn-admin on 18.02.2015.
 */
public class LogFile {

    SharedPreferences sharedPreferences;
    Context context;
    private static final String FOLDER_NAME = "Email2SMS";
    private static final String FILE_NAME = "Log.csv";
    private static final String ENCODING = "CP1251";
    private boolean writeToSDCard;

    public LogFile(Context externalContext){
        Log.d("nibbler", "LogFile Constructor");
        context = externalContext;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedSettingsName), Context.MODE_PRIVATE);
        writeToSDCard = sharedPreferences.getBoolean(context.getString(R.string.writeLogFileToSD), true);
    }

    public void writeToLog(String info){
        Log.d("nibbler", "LogFile writeToLog: " + info);
        File file = getLogFile();
        if (file == null){
            return;
        }
        /*if (info.endsWith("\r\n")){
            info = info.substring(0, info.length()-1);
        }*/
        try {
            Log.d("nibbler", "LogFile try to write string to file");
            FileOutputStream f = new FileOutputStream(file, true);
            OutputStreamWriter os = new OutputStreamWriter(f, ENCODING);
            os.write(timeStamp() + info + "\r\n");
            os.flush();
            os.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("nibbler", "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("nibbler", "IOException");
        } catch (IllegalArgumentException e){
            Log.d("nibbler", "IllegalArgumentException");
        }
    }

    public String timeStamp(){
        Calendar c = Calendar.getInstance();
        return c.getTime().toString() + ";";
    }

    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public String getLogFileText(){
        Log.d("nibbler", "LogFile getLogFileText");
        String textFromLogFile = "";
        InputStream inputStream = null;
        File file = getLogFile();
        if (file == null){
            return textFromLogFile;
        }

        if (file.exists()) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "CP1251"));
                String line = "";
                Calendar calendar = Calendar.getInstance();
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.substring(4);
                    line = line.replace(" " + Integer.toString(calendar.get(Calendar.YEAR)) + ";", " - ").replace(" GMT", "");
                    stringBuilder.append(line + "\n");
                }
                textFromLogFile = stringBuilder.toString();

                inputStream.close();
            } catch (FileNotFoundException e) {
                Log.d("nibbler", "LogFile FileNotFoundException");
            } catch (IOException e) {
                Log.d("nibbler", "LogFile IOException");
            }
        }
        return textFromLogFile;
    }

    public boolean deleteLogFile(){
        File file = getLogFile();
        if (file == null){
            return false;
        }
        if (file.exists() && file.delete()) {
            return true;
        }
        return false;
    }

    public File getLogFile(){
        File file = null;
        Log.d("nibbler", "LogFile getLogFile isExternalStorageWritable: " + isExternalStorageWritable() + " writeToSDCard: " + writeToSDCard);
        if (isExternalStorageWritable() && writeToSDCard) {
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/" + FOLDER_NAME);
            if (!dir.exists()){
                dir.mkdirs();
            }
            file = new File(dir, "/" + FILE_NAME);
            return file;
        }

        file = new File(context.getFilesDir(), FILE_NAME);
        if (file.exists()) {
            return file;
        }

        Log.d("nibbler", "LogFile getLogFile return null");
        return file;
    }
}
