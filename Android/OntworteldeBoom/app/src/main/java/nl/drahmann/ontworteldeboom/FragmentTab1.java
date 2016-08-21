package nl.drahmann.ontworteldeboom;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FragmentTab1 extends Fragment {
    static final String TAG = "BDR";

    public TextView Status01;
    public TextView Status02;
    public TextView Status03;
    public TextView Status04;
    public TextView Status05;
    public TextView Status06;
    public TextView Status07;
    public TextView Status08;

    public TextView Opm01;
    public TextView Opm02;
    public ProgressBar toon_sensor_delay;
    public TextView Opm03;
    public TextView Opm04;
    public TextView Opm05;
    public TextView Opm06;
    public TextView Opm07;
    public TextView Opm08;

    public TextView Schijf;
    public TextView Bestand;
    public TextView Sms;
    public TextView Telefoon;
    public ProgressBar toon_vlotter_delay;
    public TextView ProgressVlotterText;
    public TextView ADatum;
    public TextView ATijd;

    public TextView Niveau;
    public TextView TempC;
    public TextView Humidity;
    public TextView Lux;
    public TextView Sensor1;
    public TextView Sensor2;
    public TextView Sensor3;
    public TextView Droog1;
    public TextView Droog2;
    public TextView Droog3;

    public TextView SMSstatus;
    public TextView SMSbericht;

    public ToggleButton Handpomp1;
    public ToggleButton Handpomp2;
    public Button ResetA;

    private Handler vHandler = new Handler();
    private Handler sHandler = new Handler();
    int progressvlotterstatus = 0;
    int progresssensorstatus = 0;
    String S1,S2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the view from fragment_1
        View myView = inflater.inflate(R.layout.fragment_1, container, false);

        String myTag = getTag();
        ((MainActivity)getActivity()).setTabFragment1(myTag);

        Status01 = (TextView)myView.findViewById(R.id.TV01);
        Status02 = (TextView)myView.findViewById(R.id.TV02);
        Status03 = (TextView)myView.findViewById(R.id.TV03);
        Status04 = (TextView)myView.findViewById(R.id.TV04);
        Status05 = (TextView)myView.findViewById(R.id.TV05);
        Status06 = (TextView)myView.findViewById(R.id.TV06);
        Status07 = (TextView)myView.findViewById(R.id.TV07);
        Status08 = (TextView)myView.findViewById(R.id.TV08);

        Opm01 = (TextView)myView.findViewById(R.id.TVO01);
        Opm02 = (TextView)myView.findViewById(R.id.TVO02);
        toon_sensor_delay = (ProgressBar)myView.findViewById(R.id.pbSensorDelay);
        Opm03 = (TextView)myView.findViewById(R.id.TVO03);
        Opm04 = (TextView)myView.findViewById(R.id.TVO04);
        Opm05 = (TextView)myView.findViewById(R.id.TVO05);
        Opm06 = (TextView)myView.findViewById(R.id.TVO06);
        Opm07 = (TextView)myView.findViewById(R.id.TVO07);
        Opm08 = (TextView)myView.findViewById(R.id.TVO08);

        Schijf = (TextView)myView.findViewById(R.id.tvSchijf);
        Bestand = (TextView)myView.findViewById(R.id.tvBestand);
        Sms = (TextView)myView.findViewById(R.id.tvSMS);
        Telefoon = (TextView)myView.findViewById(R.id.tvTelefoon);
        toon_vlotter_delay = (ProgressBar)myView.findViewById(R.id.pbDelay);
        ProgressVlotterText = (TextView)myView.findViewById(R.id.tvprogress);
        ADatum = (TextView)myView.findViewById(R.id.tvADatum);
        ATijd = (TextView)myView.findViewById(R.id.tvATijd);

        Niveau = (TextView)myView.findViewById(R.id.tvNiveau);
        TempC = (TextView)myView.findViewById(R.id.tVTempC);
        Humidity = (TextView)myView.findViewById(R.id.tVHum);
        Lux = (TextView)myView.findViewById(R.id.tVLux);
        Droog1 = (TextView)myView.findViewById(R.id.tVDroog1);

        SMSstatus = (TextView)myView.findViewById(R.id.tvSMSStatus);
        SMSbericht = (TextView)myView.findViewById(R.id.tvSMSbericht);

        Handpomp1 = (ToggleButton)myView.findViewById(R.id.btnhandpomp1);
        Handpomp1.setOnClickListener(SetHandpomp);
        Handpomp2 = (ToggleButton)myView.findViewById(R.id.btnhandpomp2);
        Handpomp2.setOnClickListener(SetHandpomp);
        ResetA = (Button)myView.findViewById(R.id.btReset);
        ResetA.setOnClickListener(GoResetArduino);

        return myView;
    }

    View.OnClickListener SetHandpomp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {   // Reset Arduino
            if (Handpomp1.isChecked()) S1 = "1";
            else S1 = "0";
            if (Handpomp2.isChecked()) S2 = "1";
            else S2 = "0";
            ((MainActivity)getActivity()).sendMessage("w" + S1 + S2 +"#");
        }
    };

    View.OnClickListener GoResetArduino = new View.OnClickListener() {
        @Override
        public void onClick(View v) {   // Reset Arduino
            ResetArduino();
        }
    };

    public void ResetArduino() {
        ((MainActivity)getActivity()).sendMessage("z#");	// verzend de waarde naar de arduino
    }

    public void WerkVlotterBarBij() {
        vHandler.post(new Runnable() {
            public void run() {
                toon_vlotter_delay.setProgress(progressvlotterstatus);
            }
        });
    }

    public void WerkSensorBarBij() {
        sHandler.post(new Runnable() {
            public void run() {
                toon_sensor_delay.setProgress(progresssensorstatus);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, " onResume in fragmenttab1 is aangeroepen");
    }
}
