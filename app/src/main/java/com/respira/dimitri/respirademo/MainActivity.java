
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.respira.dimitri.respirademo;


import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.respira.dimitri.respirademo.DeviceListActivity;
import com.respira.dimitri.respirademo.R;
import com.respira.dimitri.respirademo.UartService;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.action;
import static android.R.attr.breadCrumbShortTitle;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "MainActivity";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private Stopwatch stopWatch, stopWatchBPM;
    private int waves_left = 1;
    private Double newMax=0.0, newMin = 0.0;
    private boolean inhale = false;

    TextView txtWavesleft, currentWPM, txtAantalBreaths;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect;

    /*********************************************
     * Graph
     *
     $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
    private GraphView graph;
    private Viewport viewport;
    private LineGraphSeries<DataPoint> series;
    private ImageView breathing;

    private static ProgressBar progressBar;
    private CheckBox checkbox;

 /*_________________________________________

        Gedeelte menu
        ___________________________________________
         */

    private ImageView helpicon, feedbackicon,settings,returnicon;
    private Handler dialog1Handler;
    private FeedbackDialog feedBackDialog;
    /*_________________________________________

        Gedeelte speechrecognizer
        ___________________________________________
         */
    private ImageView speechButton, bleButton;
    private int mBindFlag;
    private Messenger speechServiceMessenger;
    private BroadcastReceiver myReceiver;
    private AudioManager amanager;
    private boolean isRecording=false;
    final static String SEND_RATIO = "SEND_RATIO";
    final static String BEGIN_END = "BEGIN_END";
    final static String ABORT_ERROR = "ERROR";

    /*_________________________________________

            Gedeelte speechrecognizer
            ___________________________________________
             */
    private int aantal_breaths = 0, breaths_buffer=0;
    private TextView  bpm;
    private ArrayList<Double> totalbreaths=new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        //SERVICE SPEECHRECOGNIZER

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        speechButton= (ImageView) findViewById(R.id.speechButton);
        bleButton= (ImageView) findViewById(R.id.blebutton);
        //btnSend=(Button) findViewById(R.id.sendButton);
        //edtMessage = (EditText) findViewById(R.id.sendText);

        txtWavesleft = (TextView) findViewById(R.id.wavesLeft);
        txtAantalBreaths = (TextView) findViewById(R.id.breaths);
        breathing = (ImageView) findViewById(R.id.breathing);
        bpm = (TextView) findViewById(R.id.bpm);
        checkbox = (CheckBox) findViewById(R.id.checkBox);
        graph = (GraphView) findViewById(R.id.graph);


        stopWatch = new Stopwatch();
        stopWatchBPM = new Stopwatch();


        series = new LineGraphSeries<DataPoint>();
        series.setThickness(20);

        viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);


        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"", "","","",""});
        staticLabelsFormatter.setVerticalLabels(new String[] {"","","","", ""});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        //viewport.setMinY(0);
        //viewport.setMinX(0);
        /*_________________________________________

        Gedeelte menu
        ___________________________________________
        */
          dialog1Handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                Log.e("Message received","LOL");
                Map<Long,Integer> demap= ApplicationWPM.getGraph(getApplicationContext());
                for (Map.Entry<Long,Integer> entry :demap.entrySet() ) {
                    Log.e(entry.getKey()+"",entry.getValue()+"");
                }
                saveConversation(ApplicationWPM.getGraph(getApplicationContext()));


                //Display Alert
            }
        };
        returnicon = (ImageView) findViewById(R.id.returniconble);
        returnicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMyServiceRunning(FeedBackService.class)) {
                    unbindService(feedbackServiceConnection);
                    stopService(new Intent(MainActivity.this,FeedBackService.class));
                }
                if(mState==UART_PROFILE_CONNECTED)
                {
                    stopBLE();
                }
                finish();

            }
        });


        settings= (ImageView) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isMyServiceRunning(FeedBackService.class)) {
                    try {
                        unbindService(mServiceConnection);
                    }catch( IllegalArgumentException e){
                        e.printStackTrace();
                    }
                    getApplicationContext().stopService(new Intent(MainActivity.this, FeedBackService.class));
                    onSaveConversation(new Intent(MainActivity.this, com.respira.dimitri.respirademo.Settings.class));

                }else
                {
                    Intent i =new Intent(MainActivity.this, com.respira.dimitri.respirademo.Settings.class);
                    startActivity(i);
                }
               //DISCONNECT
                        if (mDevice != null) {
                            mService.disconnect();

                        }



            }
        });


        helpicon =(ImageView) findViewById(R.id.help);
        helpicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Help");
                alertDialog.setMessage("Respira monitors your speaking and returns the word per minutes."
                        +System.lineSeparator()+"Your breaths per minutes are also very important, try to breath deep at a steady pace."
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

                feedBackDialog = new FeedbackDialog(MainActivity.this,"AF-A1E3CA0F06DE-A4");
                feedBackDialog.show();

            }
        });

        bleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.e(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                        if(mState==UART_PROFILE_CONNECTED){
                            stopBLE();
                        }else{
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    }}
            }
        });
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording){

                    startRecording();
                speechButton.setImageResource(R.drawable.microphone_green);
                isRecording = true;
            }else{
                    stopRecording();
                    speechButton.setImageResource(R.drawable.microphone);
                    isRecording = false;
                }
        }

        });




        ///////////////////////
        //SPEECHRECOGNIZER
        /////////////////////////
        currentWPM = (TextView) findViewById(R.id.wpm);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_UART);




        /*
        START PROGRAM
         */
        service_init();
        if (!mBtAdapter.isEnabled()) {
            Log.e(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }


                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                //Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                //startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                //Disconnect button pressed
                /*if (mDevice != null) {
                    mService.disconnect();

                }*/



        //CHECKBOX

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    graph.setVisibility(View.VISIBLE);
                }else{
                    graph.setVisibility(View.INVISIBLE);
                }
            }
        });

        /* Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }
        });
     */
        // Set initial UI state

    }

    private final ServiceConnection feedbackServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            speechServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = FeedBackService.MSG_RECOGNIZER_START_LISTENING;

            try
            {
                speechServiceMessenger.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ellema", "onServiceDisconnected"); //$NON-NLS-1$
            speechServiceMessenger = null;

        }

    };


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.e(TAG, "onServiceConnected mService= " + mService);

            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };



    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Receiver", action);
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {

                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.e(TAG, "UART_CONNECT_MSG");
                bleButton.setImageResource(R.drawable.bluetooth_on);
                //edtMessage.setEnabled(true);
                //btnSend.setEnabled(true);

                //btnConnectDisconnect.setVisibility(View.GONE);
                //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                //stopWatch.start();
                stopWatchBPM.start();

                mState = UART_PROFILE_CONNECTED;
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.e(TAG, "UART_DISCONNECT_MSG");

                        mState = UART_PROFILE_DISCONNECTED;

                        stopBLE();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
               //SUPER BELANGRIJK
                //LAAT DATA AFKOMEN
                mService.enableTXNotification();
                if(!stopWatch.isRunning()){
                    stopWatch.reset();
                    stopWatch.start();
                }
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                Log.e("Receiver", "DATA_AVAILABLE");
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                try {
                    String text = new String(txValue, "UTF-8");

                    String[] fulldata = text.split(",");
                    String pattern = "[-]{0,1}+[0-9]{1}+[.]+[0-9]{2}";

                    if (fulldata.length > 0) {

                        for (String data : fulldata) {
                            //      Log.e("Data received: ", data);
                            if (data.matches(pattern) && data.length() <= 4 && Double.valueOf(data) > 0) {

                                Double buf =Double.parseDouble(data);

                                if(buf>newMax) {
                                    newMax = buf;
                                    viewport.setMaxY(newMax);
                                }
                                updateGraph(buf);
                            }
                        }}
                            //Log.e(String.valueOf(Double.parseDouble(data[0])), String.valueOf(Double.parseDouble(data[1])));
                } catch (Exception e) {
                    mService.disconnect();


                    showMessage("Device disconnected");
                    series = new LineGraphSeries<DataPoint>();
                    series.setThickness(30);

                    graph.removeAllSeries();

                    viewport = graph.getViewport();
                    viewport.setYAxisBoundsManual(true);
                    viewport.setXAxisBoundsManual(true);
                    //viewport.setMinY(0);
                    //viewport.setMinX(0);
                    bpm.setText("#");
                    currentWPM.setText("#");
                    txtAantalBreaths.setText("#");
                    breaths_buffer = 0;
                    totalbreaths.clear();
                    stopWatchBPM.reset();
                    checkbox.setChecked(false);
                }


            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };


    public void updateGraph(final Double valueD) {
        try {
            // Log.e("Graph update", "Running "+valueD);
            //addEntry(series1,key);
            if (series != null) {
                breathing.requestLayout();
                breathing.getLayoutParams().height = (int) Math.round(340 +  valueD/newMax*200);
                breathing.getLayoutParams().width = (int) Math.round(340 + valueD/newMax*200);
                Log.e("Params",""+(300 +  valueD/newMax*100));
                if (!series.isEmpty()) {
                    int size = Iterators.size(series.getValues(series.getLowestValueX(), series.getHighestValueX()));

                    if (size > 30) {
                        Log.e("SIZE",">20");

                        series = dropData(series, 10);
                        viewport.setMaxX(series.getHighestValueX()+1);
                        viewport.setMinX(series.getLowestValueX()-1);

                        addEntry(series, valueD);
                        Iterator<DataPoint> iter = series.getValues(series.getLowestValueX(),series.getHighestValueX());

                        ArrayList<Double> values = new ArrayList<>();
                        while (iter.hasNext())
                        {
                            values.add(iter.next().getY());
                           // Log.e("Value -> ",""+iter.next().getY());
                        }


                        if (inhale == false) {
                        //             Log.e("SIZE OK","COMPARE");
                            if (values.get(values.size() - 1) >= values.get(values.size() - 2) && values.get(values.size() - 2) > values.get(values.size() - 3)) {
                                Log.e("inhale", "beginning");
                                inhale=true;
                                // Log.e(data, String.valueOf(values.get(values.size() - 1)));

                                // Log.e("inhale", String.valueOf(values.get(0)) + "<" + String.valueOf(values.get(2)) + "&&" + String.valueOf(values.get(2)) + "<" + String.valueOf(values.get(4)));

                                aantal_breaths++;
                                breaths_buffer++;
                                //Log.e("Time",getCurrentTime(stopWatchBPM)+"");
                                Log.e("Buffer",breaths_buffer+"");
                                totalbreaths.add(getCurrentTime(stopWatchBPM));
                                Log.e("Size", totalbreaths.size()+"");
                                txtAantalBreaths.setText(aantal_breaths + "");

                                if (totalbreaths.size()>4){
                                    Log.e("Breath",">10");
                                    Double BPM=breaths_buffer/totalbreaths.get(totalbreaths.size()-1)*60.0;
                                    Log.e("BPM",BPM+"");
                                    totalbreaths.clear();
                                    breaths_buffer=0;
                                    BPM=Math.round(BPM*100.0)/100.0;
                                    bpm.setText(BPM+"");
                                    if (stopWatchBPM.isRunning()) {
                                        stopWatchBPM.reset();
                                        stopWatchBPM.start();
                                    }else
                                    {
                                        stopWatchBPM.start();
                                    }
                                }

                            }

                        } else {

                            if (values.get(values.size() - 1) <= values.get(values.size() - 2) && values.get(values.size() - 2) < values.get(values.size() - 5)) {





                                Log.e("exhale", "beginning");
                                //Log.e("inhale", String.valueOf(values.get(values.size() - 1)) + ">" + String.valueOf(values.get(values.size() - 3)) + "&&" + String.valueOf(values.get(values.size() - 3)) + ">" + String.valueOf(values.get(values.size() - 5)));

                                inhale = false;
                                if (values.get(values.size() - 5) < newMax * 0.9) {
                                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    // v.vibrate(200);
                                    //Toast.makeText(getApplication(),"VIBRATE: "+values.get(values.size()-5)+" < "+String.valueOf(newMax*0.9),Toast.LENGTH_SHORT ).show();
                                    //   Log.e("Vibrate", data + "<" + String.valueOf(newMax * 0.9));
                                }
                            }
                        }





                    } else {
                        addEntry(series, valueD);


                    }
                } else {
                    addEntry(series, valueD);

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graph.onDataChanged(true, true);
                    }
                });


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public LineGraphSeries dropData(LineGraphSeries deseries, int aantal) {

        Iterator iter = deseries.getValues(deseries.getHighestValueX() - aantal, deseries.getHighestValueX());

        deseries = new LineGraphSeries<DataPoint>();
        deseries.setThickness(20);

        while (iter.hasNext()) {
            //Log.e(iter.toString(),"Datapoint");
            deseries.appendData((DataPoint) iter.next(), true, 100);

        }
        return deseries;
    }

    public void addEntry(LineGraphSeries<DataPoint> serie, Double key) {
        serie.appendData(new DataPoint(getCurrentTime(stopWatch), key), true, 100);
        graph.removeAllSeries();
        graph.addSeries(serie);
      /*  if (serie.getHighestValueX() > 2) {
            viewport.setMaxX(serie.getHighestValueX() + 1);
            viewport.setMinX(serie.getLowestValueX() - 1);

        }
*/
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());


        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FeedBackService.SEND_RATIO);
        intentFilter.addAction(FeedBackService.ABORT_ERROR);
        intentFilter.addAction(FeedBackService.BEGIN_END);

        getApplicationContext().registerReceiver(myReceiver, intentFilter);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
            mService.stopSelf();
            mService = null;
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        try {
            unbindService(mServiceConnection);
            unbindService(feedbackServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.e(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);

                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has been turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.e(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    public Double getCurrentTime(Stopwatch stopWatch) {
        if (stopWatch.isRunning()) {
            Double x = Double.parseDouble(String.valueOf(stopWatch.elapsed(TimeUnit.SECONDS) % 60 + (stopWatch.elapsed(TimeUnit.MINUTES) * 60))) + ((Double.parseDouble(String.valueOf(stopWatch.elapsed(TimeUnit.MILLISECONDS))) % 1000.0) / 1000.0);
            //Log.e("TIME",String.valueOf(x));
            return x;
        } else {
            return 0.0;
        }
    }

    @Override
    public void onBackPressed() {

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



    public void startRecording(){
        Intent service = new Intent(this, FeedBackService.class);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
        getApplication().startService(service);
        bindService(service, feedbackServiceConnection, mBindFlag);
        if(stopWatch.isRunning()) {
            stopWatch.reset();
            stopWatch.start();
        }else
            {
                stopWatch.start();
            }
    }

    public void stopRecording()
    {

        currentWPM.setText("");
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(100);
        showMessage("Recognizer disconnected");
        currentWPM.setText("#");
        stopWatch.reset();
        stopService(new Intent(this,FeedBackService.class));
        onSaveConversation();
    }

    public void startBLE(){
        viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMinX(0);
    }


    public void stopBLE()
    {
        mState=UART_PROFILE_DISCONNECTED;
        bleButton.setImageResource(R.drawable.bluetooth_off);
        mService.close();
        if (stopWatchBPM.isRunning()) {
            stopWatchBPM.stop();
        }
        showMessage("Device disconnected");
        series = new LineGraphSeries<DataPoint>();
        series.setThickness(30);

        series.setBackgroundColor(Color.GREEN);
        graph.removeAllSeries();
        bpm.setText("#");
        txtAantalBreaths.setText("#");

        stopWatchBPM.reset();
        checkbox.setChecked(false);

    }

        class MyReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                String action = arg1.getAction();
                Log.e("Receiver", "DATAPASSED");
                switch (action) {
                    case SEND_RATIO:
                        Double datapassed = arg1.getDoubleExtra("DATAPASSED", 0.0);
                        Log.e("FROM SERVICE", String.valueOf(datapassed));

                        if (datapassed > 0) {
                            datapassed = ((double) Math.round(datapassed * 100.0) * 60) / 100.0;
                            if (datapassed > ApplicationWPM.LoadInt(getApplicationContext())) {
                                currentWPM.setTextColor(Color.RED);
                            } else {
                                currentWPM.setTextColor(Color.BLACK);
                            }
                            currentWPM.setText("" + datapassed);
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
                            stopService(new Intent(MainActivity.this, FeedBackService.class));


                            currentWPM.setText("");
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
    }
