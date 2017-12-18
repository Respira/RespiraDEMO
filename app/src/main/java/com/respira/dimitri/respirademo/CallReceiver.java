package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Dimitri on 27/07/2016.
 */
public class CallReceiver extends PhonecallReceiver{



    private Messenger mServiceMessenger;
    int mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
    String TAG = "CallReceiver";


    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.e(TAG,"INCOMING");

     //   Toast.makeText(ctx,"INCOMING CALL",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(sp.getBoolean("intercept",false)) {
            Intent myIntent = new Intent(ctx, FeedBackService.class);
            myIntent.putExtra("receiver", true);
            ctx.startService(myIntent);
            //  i.setAction(FeedBackService.BEGIN_END);
            //ctx.startService(i);

//            Toast.makeText(ctx,"CALL ANSWERED",Toast.LENGTH_SHORT).show();
            Log.e(TAG, "CALLANSWERED");
        }

    }

    @Override
    protected void onIncomingCallEnded(final Context ctx, String number, Date start, Date end) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (sp.getBoolean("intercept", false)) {
            Intent i = new Intent(ctx, FeedBackService.class);
            ctx.stopService(i);

            Intent savefile = new Intent(ctx, PhoneCallSaver.class);
            savefile.addFlags(FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(savefile);
        }

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        Toast.makeText(ctx,"Call made",Toast.LENGTH_SHORT).show();
        if(sp.getBoolean("intercept",false)) {
            Log.e("CALL_MADE","INTERCEPT");
            if (ApplicationWPM.loadPermission(ctx)) {
                Intent i = new Intent(ctx, FeedBackService.class);
                i.putExtra("receiver",true);
                ctx.startService(i);


            }
        }else{
            Log.e("CALL_MADE","NO INTERCEPT");
        }
    }

    @Override
    protected void onOutgoingCallEnded(final Context ctx, String number, Date start, Date end) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (sp.getBoolean("intercept", false)) {
            Intent i = new Intent(ctx, FeedBackService.class);
            ctx.stopService(i);


            Intent savefile = new Intent(ctx, PhoneCallSaver.class);
            savefile.addFlags(FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(savefile);
        }


    }
    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }




}
















