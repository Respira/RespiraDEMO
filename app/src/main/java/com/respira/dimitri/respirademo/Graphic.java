package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Dimitri on 11/09/2016.
 */
public class Graphic extends Activity {



    private LineGraphSeries<DataPoint> series;
    private LineGraphSeries<DataPoint> lineWPM, lineAVG;private int lastX = 0;
    private int avg=0;
    GraphView graph;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphic);
        graph = (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        if(readFile()){

            series.setTitle("Speaking speed");

            series.setColor(Color.BLUE);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
            series.setThickness(10);

            avg = getAvg(series);
            graph.setTitle("Last conversation duration: " + Math.round(series.getHighestValueX()) + " seconds" + System.lineSeparator() + "Average WPM: " + avg);

            lineWPM = new LineGraphSeries<>();
            lineWPM.setColor(Color.GREEN);
            lineWPM.setDrawDataPoints(true);
            lineWPM.setDataPointsRadius(10);
            lineWPM.setThickness(14);
            createWPMLine(ApplicationWPM.LoadInt(getApplicationContext()));

            lineAVG = new LineGraphSeries<>();
            lineAVG.setColor(Color.MAGENTA);
            lineAVG.setDrawDataPoints(true);
            lineAVG.setDataPointsRadius(1);
            lineAVG.setThickness(10);
            createAVGLine(avg);

            // customize a little bit viewport
            Viewport viewport = graph.getViewport();
            viewport.setYAxisBoundsManual(true);
            viewport.setXAxisBoundsManual(true);
            viewport.setBackgroundColor(Color.rgb(239, 239, 239));
            viewport.setMinY(0);
            viewport.setMinX(0);


            viewport.setMaxY(series.getHighestValueY() + 60);
            viewport.setMaxX(series.getHighestValueX() + 10);
            //viewport.setMaxX(series.getHighestValueX()+20);
            //viewport.setScrollable(true);
            Log.e("We're in", "reading file");

            graph.addSeries(lineWPM);
            graph.addSeries(lineAVG);
            graph.addSeries(series);

        }
    }
    public boolean readFile() {

        Map<Long, Integer> mapInFile =  ApplicationWPM.getGraph(getApplicationContext());

        if (mapInFile != null){
            for ( Map.Entry<Long,Integer> x: mapInFile.entrySet()) {
                Log.e(String.valueOf(x.getKey()),String.valueOf(x.getValue()));
                addEntry(x.getValue(),x.getKey());
            }
            return true;
        } else {
            return false;
        }
    }

    private void addEntry(int valueWPM, long date) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        if(date>0 && valueWPM>0) {
            series.appendData(new DataPoint(date, valueWPM), true, 100);
        }
    }
    private void createWPMLine(int WPM)
    {
        lineWPM.appendData(new DataPoint(0, WPM), true, 100);
        lineWPM.appendData(new DataPoint(series.getHighestValueX()+10, WPM), true, 2);
    }
    private void createAVGLine(int avg)
    {
        lineAVG.appendData(new DataPoint(0, avg), true, 2);
        lineAVG.appendData(new DataPoint(series.getHighestValueX()+10, avg), true, 2);
    }
    private int getAvg(LineGraphSeries<DataPoint> series) {

        double teller=0.0,avg=0.0;
            for (Iterator<DataPoint> iter = series.getValues(0, series.getHighestValueX()); iter.hasNext(); ) {
                DataPoint element = iter.next();
                avg+=element.getY();
                teller++;
           }
        return (int)Math.round((avg/teller));
    }
}
