package com.respira.dimitri.respirademo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 24/12/2016.
 */

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        LinearLayout linearLayout = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settingscreen, linearLayout, false);

        toolbar.setTitle("Settings");
        toolbar.setTitleTextColor(Color.WHITE);
        linearLayout.addView(toolbar, 0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                finish();
            }
        });
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();


    }


    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            final ListPreference listPreference = (ListPreference) findPreference("language");
            final Preference settings = (Preference) findPreference("speechsettings");
            final Preference vibrate = (Preference) findPreference("vibrate");
            final Preference reset = (Preference) findPreference("reset");
            final Preference intercept = (Preference) findPreference("intercept");
            final Preference introduction = (Preference) findPreference("intro");
            // THIS IS REQUIRED IF YOU DON'T HAVE 'entries' and 'entryValues' in your XML
            setListPreferenceData(listPreference);

            listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setListPreferenceData(listPreference);
                    return false;
                }
            });
            settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    openSpeechRecognitionSettings();
                    return false;
                }
            });
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    deleteConversations();

                    return false;
                }
            });
            vibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.e(preference.getKey(),newValue.toString());

                    return true;
                }
            });
            intercept.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.e(preference.getKey(),newValue.toString());
                    return true;
                }
            });

            introduction.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), ScreenSlidePagerActivity.class),10);
                    return false;
                }
            });


        }

        protected static void setListPreferenceData(ListPreference lp) {

            CharSequence[] entries = {"English(UK)", "Francais", "Espagnol", "Nederlands"};
            CharSequence[] entryValues = {"en-uk", "fr", "es", "nl"};
            lp.setEntries(entries);
            lp.setDefaultValue("1");
            lp.setEntryValues(entryValues);
        }

        public boolean openSpeechRecognitionSettings() {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            boolean started = false;
            ComponentName[] components = new ComponentName[]{
                    new ComponentName("com.google.android.voicesearch", "com.google.android.voicesearch.VoiceSearchPreferences"),
                    new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences"),
                    new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences")
            };
            for (ComponentName componentName : components) {
                try {
                    intent.setComponent(componentName);
                    startActivity(intent);
                    started = true;
                    break;
                } catch (final Exception e) {
                    Log.e("Error", e.toString());
                }
            }
            return started;
        }

        public void deleteConversations() {

            String path = getActivity().getFilesDir().toString() + "/savefiles/";
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            ArrayList<String> filenames = new ArrayList<String>();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        //Log.e(file.getName(), "filefound");
                        file.delete();

                    }
                }
            Toast.makeText(getActivity(),"Conversations deleted", Toast.LENGTH_SHORT).show();
            }else
            {
                Toast.makeText(getActivity(),"No conversations to delete...", Toast.LENGTH_SHORT).show();
            }
        }


    }


}

