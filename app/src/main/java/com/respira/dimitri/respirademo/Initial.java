package com.respira.dimitri.respirademo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Dimitri on 23/01/2017.
 */

public class Initial extends Activity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int REQUEST_CODE_AUDIO = 1;
    private static final int REQUEST_CODE_PHONE = 2;
    private String TAG = "Callback",jsonresponse="zllzm";
    private ProgressBar progressBar;

    private TextView loading;
    private static Handler dialog1Handler, dialog2Handler,jsonHandler;
    private SharedPreferences sharedPreferences;
    private boolean dontshow;
    private static boolean wpmSet=false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        progressBar = (ProgressBar) findViewById(R.id.progressbarINIT);
        loading = (TextView) findViewById(R.id.loading);
        progressBar.setIndeterminate(true);

        dialog1Handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                Log.e("Message received","LOL");
                setLanguage();


                //Display Alert
            }
        };
        dialog2Handler= new Handler(){
        public void handleMessage(Message msg)
        {
            Toast.makeText(getApplicationContext(),"You can always change preferences in Settings", Toast.LENGTH_LONG).show();
            sharedPreferences.edit().putBoolean("firstuse",false).apply();
            sharedPreferences.edit().putString("speechsettings",null).apply();
            sharedPreferences.edit().putString("reset",null).apply();
            sharedPreferences.edit().putBoolean("vibrate",false).apply();
            sharedPreferences.edit().putBoolean("intercept",false).apply();

        Log.e("firstuse","done");


            if(ApplicationWPM.getEmail(getApplicationContext())==null&& ApplicationWPM.getPassword(getApplicationContext())==null){
            startActivity(new Intent(Initial.this, Start.class));
            finish();
        }else
            {
                sendToApi(ApplicationWPM.getEmail(getApplicationContext()),ApplicationWPM.getPassword(getApplicationContext()));
            }
        }};

        jsonHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String x = (String) msg.obj;
                if(x.length()>0)
                {
                    Log.e(jsonresponse,"LOL");
                    JsonObject jsonObject = jsonFromString(jsonresponse);
                    Log.e(jsonObject.size()+"",jsonObject.isEmpty()+"");


                    if(jsonObject.size()>2){

                        String token = jsonObject.get("authentication_token").toString();
                        Log.e(jsonObject.isNull("authentication_token")+"",token);
                        token = token.substring(1,token.length()-1);
                        Log.e("token:", token);
                        startActivity(new Intent(Initial.this, Start.class));
                        finish();
                        ApplicationWPM.saveToken(token,getApplicationContext());

                    }else{
                        Log.e(jsonObject.get("errors").toString(),jsonObject.get("errors").getValueType().name());

                        JsonObject errorJson = jsonFromString(jsonObject.get("errors").toString());
                        Log.e(errorJson.size()+"",errorJson.isEmpty()+"");
                        Log.e(errorJson.get("email").toString(),errorJson.get("email").getValueType().name());
                        Toast.makeText(getApplicationContext(),"NOT ABLE TO LOGIN",Toast.LENGTH_LONG).show();
                    }

                    // Log.e(jsonObject.getJsonString("errors").getString(),jsonObject.getJsonString("email").getString());
                }

                return false;
            }
        });



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
                Log.e("Time","");
                if(checkAndRequestPermissions()){
                    Log.e("Perm", "done");

                        boolean firstuse = sharedPreferences.getBoolean("firstuse",true);

                    if(firstuse)
                    {

                        setWPM();

                    }else
                    {
                        Log.e("NOTfirstuse","done");
                        startActivity(new Intent(Initial.this, Start.class));
                        finish();
                    }



                }
            }
        }, 1500);

    }



    private  boolean checkAndRequestPermissions() {

        int audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int phonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storageReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int blePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        int accesFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (blePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (blePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }
        if (accesFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (storageReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (phonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //if (accesFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
        //    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        //}
        if (!listPermissionsNeeded.isEmpty()) {
            loading.setText("Asking permissions");
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_CODE_AUDIO);
            Log.e("Voorbij request","done");
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.e(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_CODE_AUDIO: {
                Log.e(TAG,"REQUEST audio called");
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.BLUETOOTH_ADMIN, PackageManager.PERMISSION_GRANTED);
                //perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.BLUETOOTH, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length ==2) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED                           //&& perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.e(TAG, "Phone state,audio permission granted");
                        ApplicationWPM.savePermission(true,getApplicationContext());
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(100);
                        loading.setText("We're ready!");
                        boolean firstuse = sharedPreferences.getBoolean("firstuse",true);

                        if(firstuse)
                        {

                            setWPM();


                        }else
                        {
                            Log.e("NOTfirstuse2","done");
                            startActivity(new Intent(Initial.this, Start.class));
                            finish();
                        }

                        //startActivity(new Intent(this, FeedBack.class));


                    }

                } else {
                        Log.e(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN) ||
                                 ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK(" Permissions required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    ApplicationWPM.savePermission(false,getApplicationContext());
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.

                                                    ApplicationWPM.savePermission(false,getApplicationContext());
                                                    //System.exit(0);
                                                    break;
                                            }
                                        }
                                    });

                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            if(!checkAndRequestPermissions()) {
                               /* Toast.makeText(this, "Enable permissions first", Toast.LENGTH_LONG)
                                        .show();*/
                            }
                            //System.exit(0);
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .setCancelable(false)
                .create()
                .show();
    }
    private void showDialogFirstRun(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Go to Settings", okListener)
                .setNegativeButton("I got it, don't show anymore", okListener)
                .create()
                .show();
    }


    private void setWPM(){
        loading.setText("Setting WPM");
    final Dialog d = new Dialog(Initial.this);
    d.setContentView(R.layout.dialog);
        d.setCanceledOnTouchOutside(false);
    TextView title =(TextView) d.findViewById(R.id.titledialogwpm);
        title.setText("Select your preferred\nwords per minute\n(standard is 90)");
    Button b1 = (Button) d.findViewById(R.id.button1);
    final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPickerDialog);
    np.setMaxValue(130); // max value 100
    np.setMinValue(60);   // min value 0
    np.setValue(90);
    //np.setWrapSelectorWheel(false);
    b1.setOnClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
        ApplicationWPM.SaveInt(np.getValue(), getApplicationContext());
            Log.e("WPM",ApplicationWPM.LoadInt(getApplicationContext())+"");
            wpmSet=true;
            d.dismiss();
            new Thread()
            {
                public void run()
                {
                    //Logic
                    Log.e("MESSAGE","SENT");
                    dialog1Handler.sendEmptyMessage(0);
                }
            }.run();
    }
    });
       d.create();
        d.show();
}

    private void setLanguage(){
        loading.setText("Setting up language");
        CharSequence[] items = { "English", "Francais", "Espagnol", "Nederlands" };
        final CharSequence[] entryValues = {"en" ,"fr","es","nl"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please choose your language");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("language", entryValues[item].toString());
                editor.commit();
                Log.e(sharedPreferences.getString("language","NIKS"),"Current lang");
                new Thread()
                {
                    public void run()
                    {
                        //Logic
                        Log.e("MESSAGE","SENT");
                        dialog2Handler.sendEmptyMessage(0);
                    }
                }.run();
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public void sendToApi(final String email, final String pass){
        String postUrl= "http://voiceable.herokuapp.com/users.json";
        String postBody="{" +
                "    \"user\":{" +
                "        \"email\":\""+email+"\"," +
                "        \"password\":\""+pass+"\"" +
                "    }}";
        Log.e(postBody,postUrl);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, postBody);
        Request request = new Request.Builder()
                .url(postUrl)
                .addHeader("Content-type","application/json")
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

                    Message msg = Message.obtain(); // Creates an new Message instance
                    msg.obj = jsonresponse; // Put the string into Message, into "obj" field.
                    msg.setTarget(jsonHandler); // Set the Handler
                    msg.sendToTarget();
                }catch (NullPointerException e)
                {
                    Log.e("TAGEXCEPTION",e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }

    private static JsonObject jsonFromString(String jsonObjectStr) {

        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        return object;
    }



}
