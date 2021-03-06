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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by nn-admin on 18.02.2015.
 */
public class LogFile {

    SharedPreferences sharedPreferences;
    Context context;
    private static final String FOLDER_NAME = "Email2SMS";
    //private static final String FILE_NAME = "Log.csv";
    private String encoding;
    private boolean writeToSDCard;
    private boolean writeLog;

    public LogFile(Context externalContext){
        //Log.d("nibbler", "LogFile Constructor");
        context = externalContext;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedSettingsName), Context.MODE_PRIVATE);
        writeToSDCard = sharedPreferences.getBoolean(context.getString(R.string.writeLogFileToSD), true);
        encoding = sharedPreferences.getString(context.getString(R.string.encodingLogFile), "CP1251");
    }

    public void writeToLog(String info){
        //Log.d("nibbler", "LogFile writeToLog");
        File file = getLogFile();
        if (file == null) return;

        try {
            //Log.d("nibbler", "LogFile try to write string to file");
            FileOutputStream f = new FileOutputStream(file, true);
            OutputStreamWriter os = new OutputStreamWriter(f, encoding);
            os.write(timeStamp() + info + "\r\n");
            os.flush();
            os.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("nibbler", "LogFile writeToLog FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("nibbler", "LogFile writeToLog IOException");
        } catch (IllegalArgumentException e){
            Log.d("nibbler", "LogFile writeToLog IllegalArgumentException");
        }
    }

    private void writeToLogWithoutTimeStamp(String info){
        //Log.d("nibbler", "LogFile writeToLogWithoutTimeStamp");
        File file = getLogFile();
        if (file == null){
            return;
        }

        try {
            //Log.d("nibbler", "LogFile try to write string to file");
            FileOutputStream f = new FileOutputStream(file, true);
            OutputStreamWriter os = new OutputStreamWriter(f, encoding);
            os.write(info);
            os.flush();
            os.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("nibbler", "LogFile writeToLogWithoutTimeStamp FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("nibbler", "LogFile writeToLogWithoutTimeStamp IOException");
        } catch (IllegalArgumentException e){
            Log.d("nibbler", "LogFile writeToLogWithoutTimeStamp IllegalArgumentException");
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
        //Log.d("nibbler", "LogFile getLogFileText");
        String textFromLogFile = "";
        InputStream inputStream = null;
        File file = getLogFile();
        if (file == null){
            return textFromLogFile;
        }

        if (file.exists()) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, encoding));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                int lineCounter = 0;
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(getLogFile()));
                while (lineNumberReader.readLine() != null) lineCounter = lineNumberReader.getLineNumber();
                if (lineCounter > 100) stringBuilder.append("Для просмотра полного лог-файла вышлите его себе на почту (значок письма чуть выше)\n\n");
                LineNumberReader lineNumberReader1 = new LineNumberReader(bufferedReader);
                while ((line = lineNumberReader1.readLine()) != null) {
                    if (lineNumberReader1.getLineNumber() > lineCounter - 100) {
                        line = line.replaceAll("\\s\\D{3,}\\s\\d{4}[;]", " - ").replaceAll("[+]\\S{2,}\\s\\d{4}[;]", " - ").replace(" GMT", "");
                        stringBuilder.append(line + "\n");
                    }
                }
                textFromLogFile = stringBuilder.toString();
                inputStream.close();
            } catch (FileNotFoundException e) {
                Log.d("nibbler", "LogFile getLogFileText FileNotFoundException");
            } catch (IOException e) {
                Log.d("nibbler", "LogFile getLogFileText IOException");
            }
        }
        //Log.d("nibbler", "LogFile textFromLogFile: " + textFromLogFile);
        return textFromLogFile;
    }

    private String getLogFileTextFromSD(boolean fromSD){
        //Log.d("nibbler", "LogFile getLogFileTextFromSD");
        String textFromLogFile = "";
        InputStream inputStream = null;
        File file;
        boolean tempWriteToSD = writeToSDCard;
        writeToSDCard = fromSD;
        file = getLogFile();
        writeToSDCard = tempWriteToSD;

        if (file == null){
            return textFromLogFile;
        }

        if (file.exists()) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, encoding));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                textFromLogFile = stringBuilder.toString();
                inputStream.close();
            } catch (FileNotFoundException e) {
                Log.d("nibbler", "LogFile getLogFileTextFromSD FileNotFoundException");
            } catch (IOException e) {
                Log.d("nibbler", "LogFile getLogFileTextFromSD IOException");
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

    public File getZippedLogFile() {
        //Log.d("nibbler", "LogFile getZippedLogFile");
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getLogFileDirectory() + "/Email2SMS_log.zip"));
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            File[] files = getLogFileDirectory().listFiles();
            Arrays.sort(files);
            if (files.length == 0) return null;
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null && !files[i].getName().contains(".zip")) {
                    ZipEntry zipEntry = new ZipEntry(files[i].getName());
                    try {
                        zipOutputStream.putNextEntry(zipEntry);
                        int len = 0;
                        FileInputStream fileInputStream = new FileInputStream(files[i]);
                        while ((len = fileInputStream.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, len);
                        }
                        fileInputStream.close();
                        zipOutputStream.closeEntry();
                    } catch (IOException e) {
                        Log.d("nibbler", "LogFile getZippedLogFile IOException");
                    }
                }
            }
            try {
                zipOutputStream.close();
                return new File(getLogFileDirectory() + "/Email2SMS_log.zip");
            } catch (IOException e) {
                Log.d("nibbler", "LogFile getZippedLogFile zipOutputStream IOException");
            }
        } catch (FileNotFoundException e) {
            Log.d("nibbler", "LogFile getZippedLogFile FileNotFoundException");
        }

        return null;
    }

    public File getLogFile(){
        //Log.d("nibbler", "LogFile getLogFile directory: " + getLogFileDirectory().getAbsolutePath());
        int month = Calendar.getInstance().get(Calendar.MONTH);
        String monthString;
        if (month < 9) {
            monthString = "0" + (month + 1);
        } else {
            monthString = "" + (month + 1);
        }
        String fileName = "/Email2SMS_log_" + Calendar.getInstance().get(Calendar.YEAR) + "_" + monthString + ".csv";
        File file = new File(getLogFileDirectory(), fileName);
        if (!file.exists()) checkForOldLogFile(getLogFileDirectory());
        return file;
    }

    private File getLogFileDirectory(){
        File dir;
        if (isExternalStorageWritable() && writeToSDCard) {
            File root = android.os.Environment.getExternalStorageDirectory();
            dir = new File(root.getAbsolutePath() + "/" + FOLDER_NAME);
            if (!dir.exists()){
                dir.mkdirs();
            }
        } else {
            dir = context.getFilesDir();
        }
        return dir;
    }

    private void checkForOldLogFile(File directory) {
        //Log.d("nibbler", "LogFile checkForOldLogFile");
        if (sharedPreferences.getBoolean(context.getString(R.string.sendMailIfError), false)) {
            if (sharedPreferences.getBoolean(context.getString(R.string.autoSendLog), false)) {
                if (getLogFileDirectory().listFiles().length != 0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(context.getString(R.string.needToSendLogToEmail), true);
                    editor.apply();
                }
            }
        }

        if (!sharedPreferences.getBoolean(context.getString(R.string.deleteLogOlderThanDays), true)) return;

        File[] files = directory.listFiles();
        Arrays.sort(files);
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().matches("Email2SMS_log_\\d{4}[_]\\d{2}.csv")){
                    //Log.d("nibbler", "LogFile checkForOldLogFile first: " + files[i].getName());
                    files[i].delete();
                }
            }
        }
        files = directory.listFiles();
        Arrays.sort(files);
        //Log.d("nibbler", "LogFile checkForOldLogFile files.length: " + files.length);
        int monthToSave = sharedPreferences.getInt(context.getString(R.string.deleteLogOlderValue), 3);
        if (files.length > monthToSave) {
            for (int i = 0; i < files.length - monthToSave; i++) {
                //Log.d("nibbler", "LogFile checkForOldLogFile second: " + files[i].getName());
                files[i].delete();
            }
        }

    }

    public void changeLogFileFolder(){
        //Log.d("nibbler", "changeLogFileFolder");
        String logPart1 = getLogFileTextFromSD(writeToSDCard);
        String logPart2 = getLogFileTextFromSD(!writeToSDCard);
        StringBuilder stringBuilder = new StringBuilder().append(logPart1).append(logPart2);
        deleteLogFile();
        writeToSDCard = !writeToSDCard;
        deleteLogFile();
        writeToLogWithoutTimeStamp(stringBuilder.toString());
        writeToSDCard = !writeToSDCard;
    }
}
