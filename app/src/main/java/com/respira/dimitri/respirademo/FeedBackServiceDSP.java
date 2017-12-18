package com.respira.dimitri.respirademo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Stopwatch;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.StopAudioProcessor;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class FeedBackServiceDSP extends Service
{

    private int wordcount = 0, lettercount=0;
    private static ArrayList<Double> ratiolist = new ArrayList<Double>();
    private static List<String> partialBuffer;
    private double startTime=0, endTime=0, timebuffer;
    Double totalTime;

    private Map<Double,Integer> timeMap = new TreeMap<Double, Integer>();
    private static int SAMPLE_RATE =48000, skipdata=0, numSyllables=0;
    String LOG_TAG="tag";
    private static int buffersize;
    private static   ArrayList<Double> audioRaw = new ArrayList<>();
    private static LineGraphSeries<DataPoint> seriesDB,seriesKlank,seriesKlan;

    private static AudioDispatcher dispatcher;
    final Stopwatch stopWatch = new Stopwatch();




    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected boolean mIsListening, isSpeaking=false;
    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static String TAG ="recognitionService";
    private static Wave dataWave;
    final static String SEND_RATIO = "SEND_RATIO";
    final static String SEND_CLARITY = "SEND_CLARITY";

    final static String BEGIN_END = "BEGIN_END";
    final static String ABORT_ERROR = "ERROR";

    SharedPreferences sp;





    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("CREATE", "");
        dataWave = new Wave();
        isSpeaking=false;


        seriesKlank = new LineGraphSeries<DataPoint>();
        seriesKlan = new LineGraphSeries<DataPoint>();

        buffersize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT);
        if (buffersize < 768) {
            //Smallest buffer is 16ms
            buffersize = 768;
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, buffersize, 0);
        } else {
            //Overlap to get 16ms steps
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, buffersize, buffersize - 768);
        }
        Log.e("buffer", buffersize + "");



        //dispatcher.addAudioProcessor(new LowPassFS(50, 48000));
        dispatcher.addAudioProcessor(new GainProcessor(0){
            @Override
            public boolean process(AudioEvent audioEvent) {
                //Check for valid data
                if (audioEvent.getdBSPL() > -100) {
                    seriesKlank.appendData(new DataPoint(audioEvent.getTimeStamp(), audioEvent.getdBSPL()), true, 50000, true);
                    dataWave.addDouble(audioEvent.getdBSPL());
                    //peak when not speaking
                    Log.e("SOUND", "START "+audioEvent.getdBSPL());
                    if (!isSpeaking && audioEvent.getdBSPL() > -60) {
                        onStartSpeech();
                        startTime = audioEvent.getTimeStamp();
                        isSpeaking = true;
                        endTime = 0;
                        Log.e("SOUND", "START "+startTime);
                    } else if (isSpeaking && audioEvent.getdBSPL() > -60) {
                        endTime = 0;
                    } else if (isSpeaking && audioEvent.getdBSPL() <= -60) {
                        if (endTime == 0) {
                            endTime = audioEvent.getTimeStamp();
                        } else
                            if (endTime + 2 < audioEvent.getTimeStamp())
                            totalTime = endTime - startTime;
                        endTime = 0;
                        startTime = 0;
                        isSpeaking = false;
                        if(totalTime>2) {
                            numSyllables = dataWave.getSyllables(seriesKlank, seriesKlan);
                            Log.e("SILENCE", " "+ endTime);
                            Log.e("Time", totalTime + "");
                            Log.e("Syllables", numSyllables + "");
                            onStopSpeech(numSyllables);

                        }
                        dataWave = new Wave();
                    }
                }
                return false;
            }

            @Override
            public void processingFinished() {

            }
        });


    }






    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("command", buffersize + "");
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




            }

    protected  class IncomingHandler extends Handler
    {
        private WeakReference<FeedBackServiceDSP> mtarget;

        IncomingHandler(FeedBackServiceDSP target)
        {
            mtarget = new WeakReference<FeedBackServiceDSP>(target);
        }


        @Override
        public void handleMessage(Message msg)
        {

            final FeedBackServiceDSP target = mtarget.get();

            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:

                    Log.e("Service connected","LISTENING");

                    if (!target.mIsListening)
                    {
                        new Thread(dispatcher,"audio").start();
                        target.mIsListening = true;

                        Log.e(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:

                    new Thread(dispatcher,"audio").stop();


                    target.mIsListening = false;

                    Log.e(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();


        if (dispatcher != null)
        {
            dispatcher.stop();
        }
        stopWatch.reset();
       // ApplicationWPM.saveGraph(timeMap,getApplicationContext());

    }

    public void onStopSpeech(final int syl){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Log.e("SOUND", "SILENCE");
                Log.e("Syllables", syl + "");
                sendSyllables(syl);
                Intent intent = new Intent();
                intent.setAction(BEGIN_END);
                intent.putExtra("BEGIN", false);
                sendBroadcast(intent);//$NON-NLS-1$
                return null;
            }
        }.doInBackground(null);
    }
    public void onStartSpeech(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Intent intent = new Intent();
                intent.setAction(BEGIN_END);
                intent.putExtra("BEGIN", true);
                sendBroadcast(intent);//$NON-NLS-1$
                return null;
            }
        }.doInBackground(null);
    }
     public void sendSyllables(final double syll){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Intent intent = new Intent();
                intent.setAction(SEND_RATIO);
                intent.putExtra("DATAPASSED", syll);
                sendBroadcast(intent);
                return null;
            }
        }.doInBackground(null);
    }








    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mServerMessenger.getBinder();

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
    public static boolean inRange(double first, double second)
    {
        if (first-second<4&&first-second>-4){
            return true;
        }else{
            return false;
        }
    }
    private Double calculateMedian(ArrayList<Double> list){
        ArrayList<Double> listje=list;
        // Collections.sort(listje);
        Double median =-60.0;
        //median=listje.get(listje.size()/2);
        //Log.e("Median",median+"");
        return median;
    }

}