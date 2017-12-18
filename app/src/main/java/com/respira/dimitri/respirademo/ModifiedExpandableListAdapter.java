package com.respira.dimitri.respirademo;

/**
 * Created by Dimitri on 26/01/2017.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ModifiedExpandableListAdapter extends BaseExpandableListAdapter{
    private Context mContext;
    private String[] mTopics;
    private String[][] mNotes;

    public ModifiedExpandableListAdapter(Context context, String[] topics, String[][] notes){
        super();
        mContext = context;
        mTopics=topics;
        mNotes=notes;

    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mNotes[groupPosition][childPosition];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        View row = convertView;

        if(row==null){
            row= LayoutInflater.from(mContext).inflate(R.layout.stats_graph,null);
        }

        //Assign the note text
        //row.setText(mNotes[groupPosition][childPosition]);

        //Make the note italic
        //row.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);

        //indent the child element a bit
        //row.setPadding(20, 0, 0, 0);
        return row;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return mNotes[groupPosition].length;
    }

    @Override //original return type was Object
    public String[] getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return mNotes[groupPosition];
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mNotes.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        TextView row = (TextView)convertView;
        if(row == null){
            row=new TextView(mContext);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200);
        params.gravity= Gravity.END;
        row.setPadding(150,50,0,0);
        row.setLayoutParams(params);



        row.setTypeface(Typeface.DEFAULT_BOLD);
        row.setTextSize(16);
        row.setText(mTopics[groupPosition]);
      //  Log.e(mTopics[groupPosition],"title");
        return row;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }
}
