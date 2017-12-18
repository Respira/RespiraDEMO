package com.respira.dimitri.respirademo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Dimitri on 25/01/2017.
 */

public class Conversation {
    private String naam, datum;
    private Double average;
    private int goal;

    public Map<Double, Integer> getDatamap() {
        return datamap;
    }

    private Map<Double,Integer> datamap= new TreeMap<>();


    public String getNaam() {
        return naam;
    }

    public String getDatum() {
        return datum;
    }

    public Double getAverage() {
        return average;
    }

    public int getGoal() {
        return goal;
    }

    public Conversation(File data)
    {
        read_file(data);
    }
    public String read_file( File savefile) {

        try {


            FileInputStream fis = new FileInputStream(savefile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            String data_conv="";

            String line;
            int counter=1;
            while ((line = bufferedReader.readLine()) != null) {

                switch(counter)
                {
                    case 1: this.naam=line;
                        counter++;
                        this.datum=line.substring(12);
                        break;
                    case 2: data_conv=line;
                        counter++;
                        break;
                    case 3: this.goal=Integer.valueOf(line);
                        counter++;
                        break;
                    case 4: this.average=Double.valueOf(line);
                        counter++;
                        break;
                }
                //Log.e("READ_LINE:", line );
            }
            String[] dots = data_conv.split(";;");
            for (String dot : dots) {
                //Log.e("DATA",dot);
                String[] keyvalue=dot.split(":");
                if(keyvalue.length>1){
                  //  Log.e(keyvalue[0], keyvalue[1]);
                if (!keyvalue[0].isEmpty()||!keyvalue[1].isEmpty())
                    datamap.put(Double.valueOf(keyvalue[0]),Integer.valueOf(keyvalue[1]));

            }}
            fis.close();
            return "GELUKT";
        } catch (FileNotFoundException e) {

            return "FILENOTFOUND";
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        }


        }


}
