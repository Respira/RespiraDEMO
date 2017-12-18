package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.suredigit.inappfeedback.FeedbackDialog;
 import com.suredigit.inappfeedback.FeedBackItem;
import com.suredigit.inappfeedback.FeedbackSettings;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Dimitri on 5/10/2016.
 */
public class FeedBackDummy extends Activity {

    private FeedbackDialog feedBackDialog;

    private RatingBar ratingBar;
    private int wordcount = 0, lettercount=0;
    private ArrayList<Double> ratiolist = new ArrayList<Double>();
    private TextView currentRatio;
    private ImageView helpicon, feedbackicon,settings,returnicon;
    private ImageView graphicon;
    private ImageView wpmicon;

    private ImageView microphone;
    private boolean listen=false;
    private TextView language;
    private Handler dialog1Handler;



    private ProgressBar progressBar;
    final static String SEND_RATIO = "SEND_RATIO";
    final static String SEND_CLARITY = "SEND_CLARITY";
    final static String BEGIN_END = "BEGIN_END";
    final static String ABORT_ERROR = "ERROR";







    private int mBindFlag;
    private Messenger mServiceMessenger;
    private BroadcastReceiver myReceiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dialog1Handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                Log.e("Message received","LOL");
                Map<Long,Integer> demap= ApplicationWPM.getGraph(getApplicationContext());
                for (Map.Entry<Long,Integer> entry :demap.entrySet() ) {
                    Log.e(entry.getKey()+"",entry.getValue()+"");
                }
           //     saveConversation(ApplicationWPM.getGraph(getApplicationContext()));


                //Display Alert
            }
        };

        /*
        //  Initialise variables
        //
        //
         */
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar.setMax(100);
        progressBar.setProgress(100);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);

        language=(TextView) findViewById(R.id.language);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String preflanguage =sp.getString("language","-1");
        switch(preflanguage){
            case "fr": preflanguage="Français";
                break;
            case "en-uk":preflanguage="English";
                break;
            case "es":preflanguage="Espagnol";
                break;
            case "nl":preflanguage="Nederlands";
                break;
        }

        language.setText(preflanguage);


        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FeedBackServiceDSP.SEND_RATIO);
        intentFilter.addAction(FeedBackServiceDSP.SEND_CLARITY);
        intentFilter.addAction(FeedBackServiceDSP.ABORT_ERROR);
        intentFilter.addAction(FeedBackServiceDSP.BEGIN_END);
        getApplicationContext().registerReceiver(myReceiver, intentFilter);
        currentRatio = (TextView) findViewById(R.id.Currentratio);
        /*
        Start up recognizer service without starting it
         */
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;



        settings= (ImageView) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(FeedBackServiceDSP.class)) {
                    unbindService(mServiceConnection);
                    getApplicationContext().stopService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));
                    onSaveConversation(new Intent(FeedBackDummy.this, Settings.class));
                }else
                {
                    Intent i =new Intent(FeedBackDummy.this, Settings.class);
                    startActivity(i);
                }
                microphone.setImageResource(R.drawable.microphone);
                listen=false;
                currentRatio.setText("");
                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
                progressBar.setProgress(100);

            }
        });


        helpicon =(ImageView) findViewById(R.id.help);
    helpicon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            AlertDialog alertDialog = new AlertDialog.Builder(FeedBackDummy.this).create();
            alertDialog.setTitle("Help");
            alertDialog.setMessage("Respira monitors your speaking and returns the word per minutes."
                    +System.lineSeparator()+"Start by pressing the microphone."
                    +System.lineSeparator()+"Once you start the recognizer you do not have to keep the app open, the app will listen until stopped by pressing the microphone or shut down by the user."
            );
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

        }
    });
        feedbackicon = (ImageView) findViewById(R.id.feedback);
        feedbackicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SUBMIT-CANCEL BUTTONS
                FeedbackSettings feedbackSettings = new FeedbackSettings();
                feedbackSettings.setCancelButtonText("No");
                feedbackSettings.setSendButtonText("Send");
//DIALOG TEXT
                feedbackSettings.setTitle("Send us feedback");
                feedbackSettings.setText("Hey, would you like to give us some feedback so that we can improve your experience?");
                feedbackSettings.setYourComments("Type your feedback here...");
                feedbackSettings.setTitle("Feedback");
//TOAST MESSAGE
                feedbackSettings.setToast("Thank you so much!");
                feedbackSettings.setToastDuration(Toast.LENGTH_SHORT);  // Default
//RADIO BUTTONS
                feedbackSettings.setRadioButtons(true); // Disables radio buttons
                feedbackSettings.setBugLabel("Bug");
                feedbackSettings.setIdeaLabel("General");

                feedBackDialog = new FeedbackDialog(FeedBackDummy.this,"AF-A1E3CA0F06DE-A4");
                feedBackDialog.show();

            }
        });

        graphicon = (ImageView) findViewById(R.id.graphicon);
        graphicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(FeedBackServiceDSP.class)) {
                    unbindService(mServiceConnection);
                    getApplicationContext().stopService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));
                    onSaveConversation(new Intent(FeedBackDummy.this, Stats.class));
                }else
                {
                  Intent i =new Intent(FeedBackDummy.this, Stats.class);
                    startActivity(i);
                }
                microphone.setImageResource(R.drawable.microphone);
                ratingBar.setNumStars(5);
                ratingBar.setRating(0);
                listen=false;
                currentRatio.setText("");
                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
                progressBar.setProgress(100);

            }
        });
        wpmicon= (ImageView) findViewById(R.id.wpmicon);
        wpmicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(FeedBackDummy.this);
                d.setContentView(R.layout.dialog);
                d.setCanceledOnTouchOutside(false);
                Button b1 = (Button) d.findViewById(R.id.button1);
                final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPickerDialog);
                np.setMaxValue(130); // max value 100
                np.setMinValue(80);   // min value 0
                np.setValue(ApplicationWPM.LoadInt(getApplication()));
                b1.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        ApplicationWPM.SaveInt(np.getValue(), getApplicationContext());
                        d.dismiss();
                    }
                });

                d.show();
            }
        });

        microphone = (ImageView)findViewById(R.id.microphoneicon);
        microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listen)
                {
                    try {
                        if (isMyServiceRunning(FeedBackServiceDSP.class)) {
                            unbindService(mServiceConnection);
                            stopService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));
                    //        onSaveConversation();
                        }
                        microphone.setImageResource(R.drawable.microphone);
                        listen=false;
                        currentRatio.setText("");
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(100);
                        progressBar.setProgress(100);

                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }else{
                try{

                bindService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class), mServiceConnection, mBindFlag);
                    startService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));
                microphone.setImageResource(R.drawable.microphone_green);
                Toast.makeText(getApplicationContext(),"You can close the app if you want, Respira keeps listening", Toast.LENGTH_LONG).show();
                listen=true;
                }catch(Exception e)
                {
                    Log.e(e.getMessage(),e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }}
        });

        returnicon = (ImageView) findViewById(R.id.returnicon);
        returnicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isMyServiceRunning(FeedBackServiceDSP.class)){
                        unbindService(mServiceConnection);
                    stopService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));


                }}
                catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                finish();
            }

        });

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setClickable(false);
        ratingBar.setNumStars(0);

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop()
    {

        super.onStop();


    }
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.e("Service connected","");
            mServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = FeedBackServiceDSP.MSG_RECOGNIZER_START_LISTENING;

            try
            {
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
           Log.d("ellema", "onServiceDisconnected"); //$NON-NLS-1$
            mServiceMessenger = null;

        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String preflanguage =sp.getString("language","-1");
        switch(preflanguage){
            case "fr": preflanguage="Français";
                break;
            case "en-uk":preflanguage="English";
                break;
            case "es":preflanguage="Espagnol";
                break;
             case "nl":preflanguage="Nederlands";
                break;
        }

        language.setText(preflanguage);
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            String action = arg1.getAction();
            Log.e("Receiver","");
            switch (action) {

                case SEND_CLARITY:
                Double clarity = arg1.getDoubleExtra("CLARITY",0.0);
                    ratingBar.setNumStars(5);
                    if(clarity>4.5)
                    {

                        ratingBar.setRating(5);
                    }else if(clarity>3.5){
                        ratingBar.setRating(4);
                    }else if(clarity>2.5){

                        ratingBar.setRating(3);
                    }else if(clarity>1.5){

                        ratingBar.setRating(2);
                    }else{

                        ratingBar.setRating(1);
                    }
                break;
                case SEND_RATIO:
                    Double datapassed = arg1.getDoubleExtra("DATAPASSED", 0.0);
                    Log.e("FROM SERVICE", String.valueOf(datapassed));

                        if (datapassed > 0) {
                       /*  datapassed = ((double) Math.round(datapassed * 100.0) * 60) / 100.0;
                          if (datapassed > ApplicationWPM.LoadInt(getApplicationContext())) {
                                 currentRatio.setTextColor(Color.RED);
                            } else {
                            currentRatio.setTextColor(Color.BLACK);
                        }*/
                        currentRatio.setText("" + datapassed);
                        }
                    break;
                case BEGIN_END:
                    boolean listening = arg1.getBooleanExtra("BEGIN", false);
                    if (listening == true) {
                        progressBar.setIndeterminate(true);
                    } else {
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(100);
                        progressBar.setProgress(100);
                    }
                    break;
                case ABORT_ERROR:
                    Log.e("ABORT", "Network error?");
                    try {
                        unbindService(mServiceConnection);
                        stopService(new Intent(FeedBackDummy.this, FeedBackServiceDSP.class));
                        microphone.setImageResource(R.drawable.microphone);
                        listen = false;
                        currentRatio.setText("");
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(100);
                        progressBar.setProgress(100);

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    }


    @Override
    public void onBackPressed() {

        try {
            unbindService(mServiceConnection);
            stopService(new Intent(this, FeedBackServiceDSP.class));
            onSaveConversation();
            microphone.setImageResource(R.drawable.microphone);
            listen=false;
            currentRatio.setText("");
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress(100);

        }
        catch(IllegalArgumentException e)
        {e.printStackTrace();}
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void onSaveConversation() {
        showSaveDialog("Save this conversation?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog1Handler.sendEmptyMessage(0);
                       // Log.e("SAVE CONVERSATION", ApplicationWPM.getGraph(getApplicationContext()).isEmpty()+"");
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.

                        break;
                }
            }
        });
    }
    private void onSaveConversation(final Intent torun) {
        showSaveDialog("Save this conversation?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog1Handler.sendEmptyMessage(0);
                        startActivity(torun);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        startActivity(torun);
                        break;
                }
            }
        });
    }

    public void saveConversation(Map<Long, Integer> zinnen)  {

        Log.e("Save conversation","go");
        if(zinnen.size()>1){
        FileWriter wr;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        String filename ="conversation"+dateFormat.format(new Date())+".txt";
        String path = getFilesDir()+"/savefiles/";
        final File dir = new File(getFilesDir() + "/savefiles/");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path,filename);

        try{
            if (file.exists())
            {
                wr = new FileWriter(file,true);//if file exists append to file. Works fine.
                Log.e("Writer","filefound");
            }
            else
            {
                file.createNewFile();
                wr = new FileWriter(file);
                Log.e("Writer","newfile");
            }

            wr.write(filename);
            wr.write(System.getProperty( "line.separator" ));
            for (Map.Entry entry : zinnen.entrySet()) {
                wr.write(";;");
                wr.write(entry.getKey().toString());
                wr.write(":");
                wr.write(entry.getValue().toString());
                wr.write(";;");
            }
            wr.write(System.getProperty( "line.separator" ));
            String WPM=""+ApplicationWPM.LoadInt(getApplicationContext());
            wr.write(WPM);
            wr.write(System.getProperty( "line.separator" ));
            wr.write(calcTotalAvg(zinnen).toString());
            wr.flush();
            wr.close();
            Log.e("Writer","DONE");
            Log.e("Writer",file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Save conversation",e.getMessage());
        }}else
        {
            Toast.makeText(this, "Conversation not long enough to save", Toast.LENGTH_SHORT).show();
        }
        // Log.e("TAG",read_file(getApplicationContext(),file ));
    }



    public Double calcTotalAvg(Map<Long, Integer> demap)
    {
        Double avg=0.0;
        ArrayList<Integer> values=new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : demap.entrySet()) {
            values.add(entry.getValue());
        }
        Double total=0.0;
        int counter=1;
        for (Integer value : values) {
            total+=value;
            counter++;
        }
        avg=Math.round((total/counter)*100.0)/100.0;

        return avg;
    }
    private void showSaveDialog(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Save", okListener)
                .setNegativeButton("Don't save", okListener)
                .create()
                .show();
    }
}
