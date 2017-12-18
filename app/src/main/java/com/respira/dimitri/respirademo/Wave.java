package com.respira.dimitri.respirademo;

import android.os.AsyncTask;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Dimitri on 25/08/2017.
 */

public class Wave {
    private int skipdata=0;
private ArrayList<Double> data;
public Wave(){
this.data=new ArrayList<Double>();

}
public void addDouble(Double x)
{
this.data.add(x);
}

public int getSyllables(LineGraphSeries<DataPoint> input, LineGraphSeries<DataPoint> output)
    {

        int numSyll=0;
        Double median=calculateMedian(this.data);
        for (int i = 0; i < this.data.size(); i++) {
            //  Log.e("LOOPING","???");
            if (i > 5) {
                //Log.e("LOOPING",audioRaw.get(i)+"");
                if (skipdata > 0) {
                    skipdata--;

                    // Log.e("SKIPPING","data");
                } else {

                    if (
                                     this.data.get(i - 4) <= this.data.get(i - 3)//16MS
                                    && this.data.get(i - 3) <= this.data.get(i - 2)//32MS
                                    && this.data.get(i - 2) >= this.data.get(i - 1)//48MS
                                    && this.data.get(i - 1) >= this.data.get(i)//64MS
                                    //   && inRange(audioRaw.get(i - 2) , audioRaw.get(i))//60MS
                                    && this.data.get(i - 2)> median
                                    //&& this.data.get(i - 2)-this.data.get(i - 4)>2
                            ) {
                        //     Log.e("Peak",audioRaw.get(i - 2)+"");
                    /*    Log.e("FOUND SYLLABLE", "" + this.data.get(i - 6));
                        Log.e("data", "" + this.data.get(i - 12));
                        Log.e("data", "" + this.data.get(i - 10));
                        Log.e("data", "" + this.data.get(i - 8));
                        Log.e("data", "" + this.data.get(i - 6));
                        Log.e("data", "" + this.data.get(i - 4));
                        Log.e("data", "" + this.data.get(i - 2));
                        Log.e("data", "" + this.data.get(i));*/
                        skipdata = 5;
                        numSyll++;
                        Iterator<DataPoint> it = input.getValues(0, input.getHighestValueX());
                        while (it.hasNext()) {
                            DataPoint dp = it.next();
                            if (dp.getY() == this.data.get((i - 2))) {
                                output.appendData(dp, true, 50000, false);

                            }
                        }
                    }

                }
            }
        }
return numSyll;
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
        Log.e("Median",median+"");
        return median;
    }
    }


