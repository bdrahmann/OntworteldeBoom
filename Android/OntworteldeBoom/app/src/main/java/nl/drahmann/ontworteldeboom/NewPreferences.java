package nl.drahmann.ontworteldeboom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Bernard on 5-7-2016.
 */
public class NewPreferences  extends PreferenceActivity {
   //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("treePreferences");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    protected void onStop (){
        super.onStop();  // Always call the superclass method first
        // lees preferences uit om ze door te sturen naar Arduino
        SharedPreferences appPrefs = getSharedPreferences("treePreferences",MODE_PRIVATE);

        MainActivity.cbxSMS = appPrefs.getBoolean("checkboxPref",false); // de sms checkbox
        Log.d(MainActivity.TAG, " P36 checkbox= " + MainActivity.cbxSMS);

        SharedPreferences.Editor prefsEditor = appPrefs.edit();     // het telefoonnummer
        MainActivity.txtTelNumber = appPrefs.getString("editTelPref", "voer telefoonnummer in");





        // einde lees preferences
    }



}

/*  mogelijke controle op input
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    Your_Pref = (EditTextPreference) getPreferenceScreen().findPreference("Your_Pref");

    Your_Pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Boolean rtnval = true;
            if (Your_Test) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Invalid Input");
                builder.setMessage("Something's gone wrong...");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
                rtnval = false;
            }
            return rtnval;
        }
    });
}

 */

