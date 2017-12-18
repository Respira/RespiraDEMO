package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
/**
 * Created by Dimitri on 27/07/2016.
 */
public class FeedBack extends Activity implements RecognitionListener {
    private int wordcount = 0, lettercount=0;
    private ArrayList<Double> ratiolist = new ArrayList<Double>();
    private List<String> partialBuffer;
    private long startTime, endTime, totalTime;
    private TextView   returnedText, currentRatio;
    private static TextView returnedRatio;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent intent;
    private String LOG_TAG = "VoiceRecActivity";
    private Map<Long,Integer> timeMap = new TreeMap<Long, Integer>();
    private Stopwatch stopWatch = new Stopwatch();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.notify(notificationID, showNotification());
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);


        Log.e("Oncreate", "We're in");
        returnedText = (TextView) findViewById(R.id.monitorring);
        currentRatio = (TextView) findViewById(R.id.Currentratio);
        returnedRatio = (TextView) findViewById(R.id.ratio);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        progressBar.setVisibility(View.INVISIBLE);





        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    listen();
                    Log.e("ToggleButton", "listening");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    //speech.stopListening();
                    //speech.cancel();
                    speech.destroy();
                    speech = null;
                    //toggleButton.setChecked(true);
                }
            }
        });
        toggleButton.setChecked(true);
        stopWatch.start();

    }

    private void listen() {
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
        }
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault().getLanguage().trim());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        //intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,true);


        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speech.startListening(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {

        ApplicationWPM.saveGraph(timeMap,getApplicationContext());


        super.onPause();
/*
        if (speech != null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();

        }
        speech = null;*/
        Log.e(LOG_TAG, "destroy_pause");
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.e(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.e(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.e(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        //toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        if (errorCode != SpeechRecognizer.ERROR_CLIENT && SpeechRecognizer.ERROR_NO_MATCH != errorCode) {
            String errorMessage = getErrorText(errorCode);
            toggleButton.setChecked(false);

            Log.e(LOG_TAG, "FAILED " + errorMessage);
            returnedText.setText(errorMessage);
            if(errorMessage=="error from server")
            {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showDialogOK("Internet or Language packs required for this app",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if(!openSpeechRecognitionSettings()) {
                                    System.exit(0);
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                // proceed with logic by disabling the related features or quit the app.
                                System.exit(0);
                                break;
                        }
                    }
                });
                toggleButton.setChecked(true);
            }else {
                Log.e("ERROR ERROR ERROR","DAMN ");
                //speech.startListening(recognizerIntent);


                toggleButton.setChecked(false);
                toggleButton.setChecked(true);
            }
        }
    }

    @Override
    protected void onDestroy() {

        if(speech!=null) {
            speech.destroy();
        }
        ApplicationWPM.saveGraph(timeMap,getApplicationContext());

        //read from file
        try{

            for(Map.Entry<Long,Integer> m :timeMap.entrySet()){
                Log.e("MAP: ",m.getKey()+" : "+m.getValue());
            }
        }catch(Exception e){
            Log.e(e.getMessage(),"READ EXCEPTION");
        }

        super.onDestroy();
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.e(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {


        wordcount = 0;
        final List<String> partialResultList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


            endTime = System.currentTimeMillis();
            partialBuffer = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for ( String result : partialResultList) {
       // Log.e("TAGG", result);
                if (!result.isEmpty()) {
                    String[] separated = result.split(" ");
                    for (String word : separated) {
                        if(isNumber(word)){
                            lettercount+=(word.length()*5)-word.length();
                        }else{

                        lettercount+=word.length();
                    }}
                    wordcount = lettercount/5;
                    lettercount=0;

                }
            }
           // Log.e(LOG_TAG, "onPartialResults");
          //  Log.e(LOG_TAG, String.valueOf(wordcount));
        }


    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.e(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.e(LOG_TAG, "onResults");
        totalTime = (endTime - startTime) / 1000;
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";

        }
        returnedText.setText(text);
        Log.e(LOG_TAG, "Result: " + text);
        Toast.makeText(this, String.valueOf("time: " + totalTime), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, String.valueOf("words: " + wordcount), Toast.LENGTH_SHORT).show();
        if(totalTime>0) {
            Double ratio =  Double.longBitsToDouble(wordcount) / Double.longBitsToDouble(totalTime);
            Log.e("RATIO", String.valueOf(ratio));
            switch(ratiolist.size()) {
                case 0:
                    ratiolist.add(ratio);
                    currentRatio.setText("Ratio:  " + ratio +"  Size Array: "+ratiolist.size());
                    break;
                case 1:
                    ratiolist.add(ratio);
                    currentRatio.setText("Ratio:  " + ratio +"  Size Array: "+ratiolist.size());
                    ratio =calcRatio(ratiolist);
                    ApplicationWPM.setCurrentWPM((int)Math.round(ratio*60), getApplicationContext());
                    compareRatio(ratio);

                    timeMap.put(getCurrentTime(),ApplicationWPM.getCurrentWPM(getApplicationContext()));


                break;
                case 2:
                    ratiolist.clear();
                    ratiolist.add(ratio);
                    currentRatio.setText("Ratio:  " + ratio +"  Size Array: "+ratiolist.size());
                break;


            }




        }
        toggleButton.setChecked(false);
        toggleButton.setChecked(true);
        //toggleButton.setChecked(true);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public static Double calcRatio(ArrayList<Double> delijst) {
        Double som = 0.0;
        if (delijst != null) {
            if (delijst.size() > 0) {
                for (Double xxx : delijst) {

                    som += xxx;
                }
                Double avg = som / delijst.size();

                //avg = Math.round(avg * 100.0) / 100.0;
                returnedRatio.setText("Average WPM(2): " + Math.round((avg*60)*100)/100);
                return avg;
            }
        }
        return null;
    }
    public void compareRatio(Double ratio) {
        int WPM = ApplicationWPM.LoadInt(getApplicationContext());
        if(ratio*60>WPM)
        {
            try {
            Thread vibrateThread= new Thread(new Runnable() {
                public void run() {
                    try {
                        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                        // pass the number of millseconds fro which you want to vibrate the phone here we
                        // have passed 2000 so phone will vibrate for 2 seconds.

                        v.vibrate(200);

                    }
                    catch (Throwable t) {
                        Log.e("Vibration", "Thread  exception "+t);
                    }
                }
            });

            vibrateThread.join();
                vibrateThread.start();


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else
        {
            Log.e("NOT TO FAST", String.valueOf(ratio*60));
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    public Notification showNotification()
    {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.respira);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Respira Speech Buddy");


        return mBuilder.build();

    }
    public Notification updateNotification(int currentWPM)
    {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.respira);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Respira Speech Buddy")
                        .setContentText("WPM: "+ currentWPM);

         return mBuilder.build();

    }


    public boolean openSpeechRecognitionSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean started = false;
        ComponentName[] components = new ComponentName[]{
                new ComponentName("com.google.android.voicesearch", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences"),
                new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences")
        };
        for (ComponentName componentName : components) {
            try {
                intent.setComponent(componentName);
                startActivity(intent);
                started = true;
                break;
            } catch (final Exception e) {
                Log.e("Error",e.toString());
            }
        }
        return started;
    }
    public long getCurrentTime() {
        if (stopWatch.isRunning()) {
            long x= stopWatch.elapsed(TimeUnit.SECONDS)+(stopWatch.elapsed(TimeUnit.MINUTES)*60);
            Log.e("TIME",String.valueOf(x));
            return x;
        }else{return 0;}
    }

    public boolean isNumber(String word)
    {
        try
        {
            double d = Double.parseDouble(word);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}