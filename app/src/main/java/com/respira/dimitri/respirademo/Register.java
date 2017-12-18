package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by Dimitri on 5/08/2017.
 */

public class Register extends Activity{



    private EditText email,password,username;
    private TextView feedback;
    private Button register, login;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static String jsonresponse="nothing";
    private static Handler jsonHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction_account);
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
                            feedback.setText("REGISTER SUCCESS");
                            String token = jsonObject.get("authentication_token").toString();
                                    Log.e(jsonObject.isNull("authentication_token")+"",token);
                            token = token.substring(1,token.length()-1);
                            Log.e("token:", token);
                            ApplicationWPM.saveEmail(email.getText().toString(),getApplicationContext());
                            ApplicationWPM.savePassword(password.getText().toString(),getApplicationContext());
                            ApplicationWPM.saveToken(token,getApplicationContext());
                            ApplicationWPM.saveUser(username.getText().toString(),getApplicationContext());
                            finish();
                        }else{
                            Log.e(jsonObject.get("error").toString(),jsonObject.get("error").getValueType().name());

                           /*JsonObject errorJson = jsonFromString(jsonObject.get("errors").toString());
                            Log.e(errorJson.size()+"",errorJson.isEmpty()+"");
                            Log.e(errorJson.get("email").toString(),errorJson.get("email").getValueType().name());
                            feedback.setText(errorJson.get("email").toString());
                            */
                           Log.e("TAG", "");
                        }

                       // Log.e(jsonObject.getJsonString("errors").getString(),jsonObject.getJsonString("email").getString());
                    }

                return false;
            }
        });




        email =(EditText) findViewById(R.id.email);
        password =(EditText) findViewById(R.id.pwd);
        username =(EditText) findViewById(R.id.username);
        feedback =(TextView) findViewById(R.id.feedbackDB);

        register =(Button) findViewById(R.id.buttonregister);
        login =(Button) findViewById(R.id.buttonlogin);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mail= email.getText().toString();
                final String pwd= password.getText().toString();
                final String user= username.getText().toString();

                if(mail.equals("")||pwd.equals("")||user.equals(""))
                {
                    feedback.setText("PLEASE ENTER EMAIL AND PASSWORD");
                }else if(pwd.length()<10){
                    feedback.setText("PASSWORD TOO SHORT");
                }else if (!isValidEmailAddress(mail))
                {
                    feedback.setText("NOT A VALID EMAIL ADDRESS");
                }else {

                    feedback.setText("VALIDATING");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendToApi(mail, pwd,user);
                        }}).run();
                            //                        Log.e("data", jsonresponse);
                }
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mail= email.getText().toString();
                final String pwd= password.getText().toString();
                if(mail.equals("")||pwd.equals(""))
                {
                    feedback.setText("PLEASE ENTER EMAIL AND PASSWORD");
                }else if(pwd.length()<10){
                    feedback.setText("PASSWORD TOO SHORT");
                }else if (!isValidEmailAddress(mail))
                {
                    feedback.setText("NOT A VALID EMAIL ADDRESS");
                }else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            doLogin (mail, pwd);
                        }}).run();
                    //                        Log.e("data", jsonresponse);
                }
            }
        });
    }
    public void sendToApi(final String email, final String pass,final String user){
        String postUrl= "https://dashboard-respira.herokuapp.com/users.json";
        String postBody="{" +
                "    \"user\":{" +
                "        \"email\":\""+email+"\"," +
                "        \"password\":\""+pass+"\"," +
                "        \"username\":\""+user+"\"" +
                "    }}";
        Log.e("URL:",postUrl);
        Log.e("DATA:",postBody);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .addHeader("Content-Type","application/json")
                .method("POST",body)
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
                        call.cancel();

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




    public void doLogin(final String email, final String pass){
        String postUrl= "https://dashboard-respira.herokuapp.com/users/sign_in.json";
        String postBody="{" +
                "    \"user\":{" +
                "        \"email\":\""+email+"\"," +
                "        \"password\":\""+pass+"\"" +
                "    }}";
        Log.e(postBody,postUrl);
        RequestBody body = RequestBody.create(JSON, postBody);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        OkHttpClient client = builder.build();
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
                finish();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if  (response.isSuccessful()) {
                        Log.e("TAG", response.message());
                        jsonresponse = response.body().string();
                        Log.e("resp",jsonresponse);
                         ApplicationWPM.saveEmail(email,getApplicationContext());
                         ApplicationWPM.savePassword(pass,getApplicationContext());
                        String token = jsonFromString(jsonresponse).get("authentication_token").toString();
                        token = token.substring(1,token.length()-1);
                        ApplicationWPM.saveToken(token,getApplicationContext());
                        Log.e("token",token);
                        finish();

                    }else{
                        Log.e("TAG", response.message());
                        jsonresponse = response.body().string();
                        Log.e("resp",jsonresponse);

                        finish();
                    }


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

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}
