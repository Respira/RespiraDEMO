package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by Dimitri on 7/02/2017.
 */

public class PhoneCallSaver extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSaveDialog("Save this phone call conversation?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        saveConversation(ApplicationWPM.getGraph(getApplicationContext()));
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        finish();
                        break;
                }
            }
        });
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
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Save", okListener)
                .setNegativeButton("Don't save", okListener)
                .create()
                .show();
    }
    public void saveConversation(Map<Long, Integer> zinnen)  {

        Log.e("Save conversation","go");
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
            Toast.makeText(getApplicationContext(),"Conversation saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Save conversation",e.getMessage());
        }
        // Log.e("TAG",read_file(getApplicationContext(),file ));
    }
}
