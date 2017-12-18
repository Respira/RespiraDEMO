package com.respira.dimitri.respirademo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitri on 25/01/2017.
 */

public class Stats extends Activity {

    private String path;
    int lastClicked=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);


        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.parent);
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settingscreen, linearLayout, false);

        toolbar.setTitle("STATS");
        toolbar.setTitleTextColor(Color.WHITE);
        linearLayout.addView(toolbar, 0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0,0);
                finish();
            }
        });






        final ListView delijst = (ListView) findViewById(android.R.id.list);

        path = getFilesDir().toString() + "/savefiles/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> filenames = new ArrayList<String>();
        if(listOfFiles!=null){
        for (File file : listOfFiles) {
            if (file.isFile()) {
                //Log.e(file.getName(), "filefound");
                new Conversation(file);
                filenames.add(file.getName().substring(0, file.getName().length() - 4));
            }
        }
        } else{
            Toast.makeText(getApplicationContext(),"You need to record a conversation to view statistics",Toast.LENGTH_LONG).show();
            finish();
        }
        final String[] filenamesarray = new String[filenames.size()];
        String[][] topics=new String[filenames.size()][1];
        int counter=0;
        for (String filename : filenames) {
            filenamesarray[counter]= filename;

            counter++;
            Log.e("FILEarray",filename);
        }

        final ArrayAdapter adapter =new ArrayAdapter<String>(this,
                R.layout.stats_element, R.id.heading,
                filenames);
        delijst.setAdapter(adapter);
        delijst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView title =(TextView) view.findViewById(R.id.heading);
                Log.e("Onclick",title.getText().toString());
                String filename = title.getText().toString()+".txt";

                Log.e("filenam", filename);
                GraphView graphView = (GraphView) findViewById(R.id.graph);
                graphView.removeAllSeries();


                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAvg);
                layout.setVisibility(View.VISIBLE);
                graphView.setVisibility(View.VISIBLE);
                Log.e(graphView.getClass().toString(), "null?");
                Viewport viewport = graphView.getViewport();
                viewport.setYAxisBoundsManual(true);
                viewport.setXAxisBoundsManual(true);


                String path = getFilesDir().toString() + "/savefiles/";
                File file = new File(path, filename);


                Conversation c = new Conversation(file);
                LineGraphSeries<DataPoint> deserie = new LineGraphSeries<DataPoint>();

                if (c.getDatamap() != null || !c.getDatamap().isEmpty()) {
                    for (Map.Entry<Double, Integer> x : c.getDatamap().entrySet()) {
                        Log.e(String.valueOf(x.getKey()), String.valueOf(x.getValue()));
                        addEntry(x.getValue(), x.getKey(), deserie);
                }

                    viewport.setMaxY(deserie.getHighestValueY() + 10);
                    viewport.setMaxX(deserie.getHighestValueX() + 2);

                    deserie.setColor(Color.GREEN);
                    deserie.setDrawDataPoints(true);
                    deserie.setDataPointsRadius(10);
                    deserie.setThickness(10);                    viewport.setMinY(deserie.getLowestValueY()-10);


                    graphView.addSeries(deserie);
                    viewport.setMinX(deserie.getLowestValueX()-2);

                    TextView goal = (TextView) findViewById(R.id.goal);
                    if (goal != null) {
                        goal.setText(c.getGoal() + "");
                    } else {
                        Log.e("ERROR", "NULLVALUE");
                    }
                    TextView avg = (TextView) findViewById(R.id.avg);
                    if (avg != null) {
                        avg.setText(c.getAverage() + "");
                    } else {
                        Log.e("ERROR", "NULLVALUE");
                    }
                } else {
                    Log.e("nodata", "error");
                }

            }
        });


            // Set up our adapter





        Log.e("AANTAL CONVERSATIONS",""+adapter.getCount());
        delijst.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
    private void addEntry(int valueWPM, Double date, LineGraphSeries<DataPoint> series) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        if(date>0 && valueWPM>0) {
            series.appendData(new DataPoint(date, valueWPM), true, 100);
        }
    }



}
