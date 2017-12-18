package com.respira.dimitri.respirademo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitri on 27/01/2017.
 */

public class MySimpleExpandableListAdapter extends SimpleExpandableListAdapter{

    ViewHolderItem holder;
    Context mContext;
    public MySimpleExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData, int groupLayout, String[] groupFrom, int[] groupTo, List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
        super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
     mContext = context;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return super.getGroupView(groupPosition, isExpanded, convertView, parent);

    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(convertView==null){
            RelativeLayout superView=(RelativeLayout) super.getChildView(groupPosition, 0, isLastChild, convertView, parent);
            TextView txt =(TextView) superView.findViewById(R.id.textTITEL);
            if(txt.getTag()==null) {
                ExpandableListView p =(ExpandableListView) parent;
                for (int i = 1; i < p.getChildCount() ; i++) {
                    p.collapseGroup(i);
                }
                View child = LayoutInflater.from(mContext).inflate(R.layout.stats_graph, null);
                superView.addView(child);

                String filename = txt.getText().toString()+".txt";

                    Log.e("filenam", filename);
                    GraphView graphView = (GraphView) superView.findViewById(R.id.graph);
                    Log.e(graphView.getClass().toString(), "null?");
                    Viewport viewport = graphView.getViewport();
                    viewport.setYAxisBoundsManual(true);
                    viewport.setXAxisBoundsManual(true);
                    viewport.setBackgroundColor(Color.rgb(239, 239, 239));
                    viewport.setMinY(0);
                    viewport.setMinX(0);

                    String path = mContext.getFilesDir().toString() + "/savefiles/";
                    File file = new File(path, filename);


                    Conversation c = new Conversation(file);
                    LineGraphSeries<DataPoint> deserie = new LineGraphSeries<DataPoint>();

                    if (c.getDatamap() != null) {
                        for (Map.Entry<Double, Integer> x : c.getDatamap().entrySet()) {
                            Log.e(String.valueOf(x.getKey()), String.valueOf(x.getValue()));
                            addEntry(x.getValue(), x.getKey(), deserie);
                        }

                        viewport.setMaxY(deserie.getHighestValueY() + 10);
                        viewport.setMaxX(deserie.getHighestValueX() + 2);
                        deserie.setColor(Color.BLUE);
                        deserie.setDrawDataPoints(true);
                        deserie.setDataPointsRadius(10);
                        deserie.setThickness(10);

                        graphView.addSeries(deserie);
                        deserie = null;
                        TextView goal = (TextView) superView.findViewById(R.id.goal);
                        if (goal != null) {
                            goal.setText(c.getGoal() + "");
                        } else {
                            Log.e("ERROR", "NULLVALUE");
                        }
                        TextView avg = (TextView) superView.findViewById(R.id.avg);
                        if (avg != null) {
                            avg.setText(c.getAverage() + "");
                        } else {
                            Log.e("ERROR", "NULLVALUE");
                        }
                    } else {
                        Log.e("nodata", "error");
                    }
                    txt.setTag("Me");
                    return superView;
                }}else {
            return super.getChildView(groupPosition, 0, isLastChild, convertView, parent);
        }            return super.getChildView(groupPosition, 0, isLastChild, convertView, parent);
    }


/*

            convertView.setTag(holder);

            //View papa =(View) getGroupView(groupPosition,true,convertView,parent);
             holder.layout.findViewById(R.id.textTITEL);








        return convertView;
*/



    private void addEntry(int valueWPM, Double date, LineGraphSeries<DataPoint> series) {
            // here, we choose to display max 10 points on the viewport and we scroll to end
            if(date>0 && valueWPM>0) {
                series.appendData(new DataPoint(date, valueWPM), true, 100);
            }
        }

    static class ViewHolderItem{
        RelativeLayout layout;
    }
    static class Groupholder
    {
        RelativeLayout layout;
    }

    @Override
    public Object getGroup(int groupPosition) {
        String path = mContext.getFilesDir().toString() + "/savefiles/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> filenames = new ArrayList<String>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                //Log.e(file.getName(), "filefound");
                new Conversation(file);
                filenames.add(file.getName().substring(0, file.getName().length()));
            }
        }
        return filenames.get(groupPosition);
    }



    }

