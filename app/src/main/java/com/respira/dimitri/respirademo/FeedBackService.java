package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import  com.google.common.base.Stopwatch;
import com.google.common.primitives.Floats;
import com.google.common.reflect.Parameter;
import com.respira.dimitri.respirademo.FeedBack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.respira.dimitri.respirademo.FeedBackDummy.*;
import static com.respira.dimitri.respirademo.Register.JSON;

public class FeedBackService extends Service
{

    private int wordcount = 0, lettercount=0;
    private ArrayList<Double> ratiolist = new ArrayList<Double>();
    private List<String> partialBuffer;
    private long startTime, endTime;
    Double totalTime;
    private String LOG_TAG = "VoiceRecActivity";
    private Map<Long,Integer> timeMap = new TreeMap<Long, Integer>();
    final Stopwatch stopWatch = new Stopwatch();



    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected boolean mIsListening;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static String TAG ="recognitionService";

    final static String SEND_RATIO = "SEND_RATIO";
    final static String SEND_CLARITY = "SEND_CLARITY";

    final static String BEGIN_END = "BEGIN_END";
    final static String ABORT_ERROR = "ERROR";

    SharedPreferences sp;





    @Override
    public void onCreate()
    {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());

         sp = PreferenceManager.getDefaultSharedPreferences(this);
        String language =sp.getString("language","-1");
        mSpeechRecognizerIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES,true);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            boolean receiver = (boolean) intent.getExtras().get("receiver");
            if(receiver){
                Message messageStart = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                try {

                    mServerMessenger.send(messageStart);

                   // am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
                } catch (RemoteException e) {
                    e.getMessage();
                }}
        }catch(Exception e){e.printStackTrace();}



 /* We want this service to continue running until it is explicitly
       stopped, so return sticky. */
        return START_NOT_STICKY;

    }

    public void init()
    {

        if (mSpeechRecognizer == null) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        }else
            {mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());}

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String language =sp.getString("language","-1");


        mSpeechRecognizerIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES,true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);



            }

    protected  class IncomingHandler extends Handler
    {
        private WeakReference<FeedBackService> mtarget;

        IncomingHandler(FeedBackService target)
        {
            mtarget = new WeakReference<FeedBackService>(target);
        }


        @Override
        public void handleMessage(Message msg)
        {

            final FeedBackService target = mtarget.get();

            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        // turn off beep sound
                        target.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
                    }
                    if (!target.mIsListening)
                    {
                        init();
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;

                     //   Log.e(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    target.mSpeechRecognizer.destroy();
                    target.mSpeechRecognizer =null;
                    target.mIsListening = false;

                   // Log.e(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();


        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
        stopWatch.reset();
        ApplicationWPM.saveGraph(timeMap,getApplicationContext());
        sendData(timeMap);
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        private static final String TAG = "SpeechRecognitionListnr";
        private float fPeak =5;
        private boolean bBegin=false;
        long lCheckTime;
        long lTimeout = 700;
        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count down anymore
            startTime = System.currentTimeMillis();
            if(!stopWatch.isRunning())
            {
                stopWatch.start();
            }
            Intent intent = new Intent();
            intent.setAction(BEGIN_END);
            intent.putExtra("BEGIN", true);

            bBegin=true;
            sendBroadcast(intent);
          //  Log.e(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onRmsChanged(float rmsdB) {

            if(bBegin) {
                if (rmsdB > fPeak) {


                    fPeak = rmsdB;
                 //   Log.e("NEW PEAK",String.valueOf(fPeak));
                    lCheckTime = System.currentTimeMillis();
                }else{
                    if(rmsdB>=fPeak/3){
                        lCheckTime = System.currentTimeMillis();
                    }else
                if (System.currentTimeMillis() > lCheckTime + lTimeout ) {
                    //Log.e(TAG, "DONE_LISTENING");
                   // Log.e("peak",String.valueOf(fPeak));
                   // Log.e("END db",String.valueOf(rmsdB));
                   mSpeechRecognizer.stopListening();

                }}
        }}

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            //Log.e(TAG, "onEndOfSpeech");
            bBegin=false;
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    Intent intent = new Intent();
                    intent.setAction(BEGIN_END);
                    intent.putExtra("BEGIN", false);
                    sendBroadcast(intent);//$NON-NLS-1$
                    return null;
                }
            }.doInBackground(null);

        }

        @Override
        public void onError(int error) {

            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            Message messageStop = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {
            if(error==SpeechRecognizer.ERROR_SERVER || error==SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT){
                mServerMessenger.send(messageStop);
                Toast.makeText(getApplicationContext(),"Error, are you connected to the internet?",Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(ABORT_ERROR);
                intent.putExtra("Abort",getErrorText(error));
                sendBroadcast(intent);

            }else {
                Intent intent = new Intent();
                intent.setAction(BEGIN_END);
                intent.putExtra("BEGIN", false);
                sendBroadcast(intent);

                mServerMessenger.send(messageStop);
                mServerMessenger.send(message);
            }
               // Log.e(TAG, "error = " + getErrorText(error)); //$NON-NLS-1$
                } catch (RemoteException e) {
                    Log.e("Error", e.getMessage());
                }


        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(final Bundle partialResults) {

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    wordcount = 0;
                    final List<String> partialResultList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if(partialResultList!=null && partialBuffer!=null) {
                        if (!partialResultList.get(0).equals(partialBuffer.get(0))) {

                            endTime = System.currentTimeMillis();
                            //Log.e("NEW ENDTIME", partialResultList.get(0) + "<>" + partialBuffer.get(0));
                        }
                    }
                    if(partialResultList!=null) {
                        partialBuffer = partialResultList;
                        String result = partialResultList.get(0);
                        // Log.e("TAGG", result);
                        if (!result.isEmpty()) {
                            String[] separated = result.split(" ");

                            for (String word : separated) {
                                if (isNumber(word)) {
                                    lettercount += (word.length() * 4) - word.length();
                                } else {
                                    wordcount++;

                                }
                            }
                            wordcount += lettercount / 5;
                            lettercount = 0;


                        }
                        // Log.e(LOG_TAG, "onPartialResults");
                        //  Log.e(LOG_TAG, String.valueOf(wordcount));
                    }
                        return null;
                }
            }.doInBackground(null);
            }

        @Override
        public void onReadyForSpeech(Bundle params) {

           // Log.e(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(final Bundle results) {
            //Log.e(TAG, "onResults"); //$NON-NLS-1$
            //mSpeechRecognizer.destroy();
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text=matches.get(0);
            Log.e("Result", text);
            //Log.e("Timer", String.valueOf(totalTime));
            //Log.e("Timer", String.valueOf(wordcount));
            Message messageStart = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            Message messageStop = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {

                mServerMessenger.send(messageStop);
                mServerMessenger.send(messageStart);
            } catch (RemoteException e) {
                e.getMessage();
            }


            //Calculate clarity







            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    totalTime = (endTime - startTime)/1000.0;
                    if (totalTime > 0) {
                        Double ratio = Double.valueOf(wordcount) / Double.valueOf(totalTime);
                        //    Log.e("RATIO", String.valueOf(ratio));
                        switch (ratiolist.size()) {
                            case 0:
                                ratiolist.add(ratio);

                                break;
                            case 1:
                                ratiolist.add(ratio);
                                Double ratioAVG = calcRatio(ratiolist);

                                //Do we need to vibrate?
                                compareRatio(ratioAVG);
                                ratiolist.clear();
                                ratiolist.add(ratioAVG);

                                //Save data and send it to BroadcastReceiver
                                timeMap.put(getCurrentTime(), (int) Math.round(ratioAVG * 60));
                                Intent intent = new Intent();
                                intent.setAction(SEND_RATIO);
                                intent.putExtra("DATAPASSED", ratioAVG);
                                sendBroadcast(intent);

                                break;
                        }


                        float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                        float buffer = 0, errors = 0;
                        double result;
                        for (float score : scores) {
                            if (score >= 0) {
                                buffer += score;
                            } else {
                                errors++;
                            }
                        }
                        result = buffer / (scores.length - errors) * 5.0;
                        // Log.e("score", result+"");
                        Intent intent = new Intent();
                        intent.setAction(SEND_CLARITY);
                        intent.putExtra("CLARITY", result);
                        sendBroadcast(intent);
                    }
                    //Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
                    //intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,true);
                    //getApplicationContext().sendOrderedBroadcast(intent, null, new HintReceiver(),
                    //        null, Activity.RESULT_OK, null, null);
                    return null;
                }
            }.doInBackground(null);

//            returnedText.setText(text);






        }


    }


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mServerMessenger.getBinder();

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


                //returnedRatio.setText("Average WPM(2): " + Math.round((avg*60)*100)/100);
                return avg;
            }
        }
        return null;
    }
    public long getCurrentTime() {
        if (stopWatch.isRunning()) {
            long x= (stopWatch.elapsed(TimeUnit.SECONDS)+(stopWatch.elapsed(TimeUnit.MINUTES)*60+(stopWatch.elapsed(TimeUnit.MILLISECONDS)/1000)));
           // Log.e("TIME",String.valueOf(x));
            return x;
        }else{return 0;}
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

                            if(sp.getBoolean("vibrate",false)) {


                                /*
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, 60, 0);
                                final MediaPlayer mp = MediaPlayer.create(FeedBackService.this, R.raw.sound);

                                mp.prepare();
                                mp.start();
                                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {

                                        mp.reset();
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                                    }
                                });
                                */

                                // pass the number of millseconds fro which you want to vibrate the phone here we
                                // have passed 2000 so phone will vibrate for 2 seconds.

                                v.vibrate(200);
                            }


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
           // Log.e("NOT TO FAST", String.valueOf(ratio*60));
        }

    }

    public void sendData(final Map<Long, Integer> data){
        String email="dimi@debeste.com";
        String token="FRmKuTHxyV6sSwPDF9vD";
        String description="Saving conversation onsave API";
        String dataString="{";
        for (Map.Entry<Long, Integer> entry : data.entrySet()) {
            dataString+= "["+entry.getKey()+":"+entry.getValue()+"]";
        }
        dataString+="}";
        String postUrl= "http://voiceable.herokuapp.com/api/respira/v1/recordings";
        String postBody="{" +
                "    \"recording\":{" +
                "        \"data\":\""+dataString+"\"," +
                "        \"description\":\""+description+"\"" +
                "    }}";
        Log.e(postBody,postUrl);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, postBody);
        Request request = new Request.Builder()
                .url(postUrl)
                .addHeader("Content-type","application/json")
                .addHeader("X-User-Email",email)
                .addHeader("X-User-Token",token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e(e.getMessage(),"fuck");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonresponse;
                try {
                    if (response.isSuccessful()) {
                        Log.e("TAG", response.message());
                        jsonresponse = response.body().string();
                        Log.e("resp",jsonresponse);
                    }else{
                        Log.e("TAG", response.message());
                        jsonresponse = response.body().string();
                        Log.e("resp",jsonresponse);

                    }

               /* Message msg = Message.obtain(); // Creates an new Message instance
                msg.obj = jsonresponse; // Put the string into Message, into "obj" field.
                msg.setTarget(jsonHandler); // Set the Handler
                msg.sendToTarget();*/
                }catch (NullPointerException e)
                {
                    Log.e("TAGEXCEPTION",e.getMessage());
                    e.printStackTrace();
                }

            }
        });
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