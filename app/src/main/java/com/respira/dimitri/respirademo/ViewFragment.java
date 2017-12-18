package com.respira.dimitri.respirademo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Dimitri on 28/04/2017.
 */

public class ViewFragment extends Fragment {

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

           int position =getArguments().getInt("view", 0);
        View rootView;

        if(position==1) {
                rootView =  inflater.inflate(
                        R.layout.introduction2, container, false);
        }else if(position==2) {
                rootView = inflater.inflate(
                        R.layout.introduction3, container, false);
            Button start = (Button) rootView.findViewById(R.id.startRespira);

                start.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                    }

                });


        }else{
            rootView =  inflater.inflate(
                    R.layout.introduction, container, false);
        }

            return rootView;
        }
    }

