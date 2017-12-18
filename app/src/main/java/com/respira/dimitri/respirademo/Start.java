package com.respira.dimitri.respirademo;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.telecom.Call;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.data;

/**
 * Created by Dimitri on 27/07/2016.
 */
public class Start extends Activity{

    private static final int REQUEST_CODE_AUDIO = 1;
    private String TAG = "Callback";

    private ImageView videobutton, helpicon, settings, feedbackicon;
    private Button confirm, startRecording, btnStartBLE;
    private TextView link,email;
    private int WPM=80, INTRO_REQUEST=69;
    private SharedPreferences sharedPreferences;
    private FeedbackDialog feedBackDialog;
    private boolean dontshow;


    private static final int REQUEST_CODE_EMAIL = 1;


    @Override
    protected void onResume() {
        super.onResume();
        try {
            email.setText(ApplicationWPM.getEmail(getApplicationContext()));
        }catch(NullPointerException e)
        {
            email.setText("NO EMAIL SAVED");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);


        startRecording = (Button) findViewById(R.id.startRecognizer);
        btnStartBLE =(Button) findViewById(R.id.btnBLEstart);
        videobutton = (ImageView) findViewById(R.id.video_start);
        settings= (ImageView) findViewById(R.id.settings);
        helpicon= (ImageView) findViewById(R.id.help);
        feedbackicon= (ImageView) findViewById(R.id.feedback);
        link = (TextView) findViewById(R.id.websitelink);

        email = (TextView) findViewById(R.id.email);



            videobutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://vimeo.com/191140085/f43e6346ea"); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

        //startActivity(new Intent(this, FeedBack.class));

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Start.this, FeedBackDummy.class));
            }
        });

            btnStartBLE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Toast.makeText(getApplicationContext(),"Coming soon!",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Start.this, MainActivity.class));
                }
            });

        settings= (ImageView) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Start.this, com.respira.dimitri.respirademo.Settings.class);
                startActivityForResult(i, 1);
            }
        });


        helpicon =(ImageView) findViewById(R.id.help);
        helpicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(Start.this).create();
                alertDialog.setTitle("Help");
                alertDialog.setMessage("Respira monitors your speaking and returns the word per minutes."+System.lineSeparator()
                        +"A  normal speaking speed is about 90 words per minute."+System.lineSeparator()
                        +"We're also working on a wearable, watch our video!");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
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

                feedBackDialog = new FeedbackDialog(Start.this,"AF-A1E3CA0F06DE-A4");
                feedBackDialog.show();

            }
        });

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.respira.io"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });



        dontshow=(ApplicationWPM.getToken(getApplicationContext())!=null);
        if(dontshow==false) {

           startActivityForResult(new Intent(Start.this,Register.class),INTRO_REQUEST);
//            ApplicationWPM.setDontShow(true,getApplicationContext());
         }





        // ...


        // ...


        }

            //

    private void setPassword(){
        final Dialog d = new Dialog(Start.this);
        d.setContentView(R.layout.dialogemail);
        d.setCanceledOnTouchOutside(false);

        Button b1 = (Button) d.findViewById(R.id.button1);
        final EditText mail = (EditText) d.findViewById(R.id.email);
        final EditText pwd = (EditText) d.findViewById(R.id.pwd);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Log.e("WPM", ApplicationWPM.LoadInt(getApplicationContext()) + "");
                d.dismiss();
                Log.e(mail.getText().toString(),pwd.getText().toString());
            }});
        d.create();
        d.show();
    }


    @Override
    public void onBackPressed() {

    }
}

