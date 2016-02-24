package nl.drahmann.ontworteldeboom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FragmentTab3 extends Fragment {

    public CheckBox SMS;
    public EditText txtTelNumber;
    public EditText txtDrooglevel1;
    public EditText txtDrooglevel2;
    public EditText txtDrooglevel3;
    public EditText txtDroogtijd;
    public EditText txtDruppelspeling;
    public EditText txtSamples;
    public EditText txtVlotterdelay;
    public EditText txtDatumTijd;
    String Arduinoinfo = "";
    public Button Store;
    public Button DateStore;

    static final String TAG = "BDR";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get the view from fragment_3
		View mijnView = inflater.inflate(R.layout.fragment_3, container, false);

        String myTag = getTag();
        ((MainActivity)getActivity()).setTabFragment3(myTag);

        SMS = (CheckBox)mijnView.findViewById(R.id.cbxSMS);
        txtTelNumber = (EditText)mijnView.findViewById(R.id.ettelNummer);
        txtDrooglevel1 = (EditText)mijnView.findViewById(R.id.etdroogLevel1);
        txtDrooglevel2 = (EditText)mijnView.findViewById(R.id.etdroogLevel2);
        txtDrooglevel3 = (EditText)mijnView.findViewById(R.id.etdroogLevel3);
        txtDroogtijd = (EditText)mijnView.findViewById(R.id.etdroogTijd);
        txtDruppelspeling = (EditText)mijnView.findViewById(R.id.etdruppelSpeling);
        txtSamples = (EditText)mijnView.findViewById(R.id.etsamples);
        txtVlotterdelay = (EditText)mijnView.findViewById(R.id.etvlotterDelay);
        txtDatumTijd = (EditText)mijnView.findViewById(R.id.etdatum_tijd);
        Store = (Button)mijnView.findViewById(R.id.btnStore);
        Store.setOnClickListener(GoStoreOpArduino);
        DateStore = (Button)mijnView.findViewById(R.id.btnDatestore);
        DateStore.setOnClickListener(SetDateOpArduino);

		return mijnView;
	}

    View.OnClickListener GoStoreOpArduino = new View.OnClickListener() {
        @Override
        public void onClick(View v) {   // Reset Arduino
            StoreOpArduino();
        }
    };

    View.OnClickListener SetDateOpArduino = new View.OnClickListener() {
        @Override
        public void onClick(View v) {   // Reset Arduino
        // TODO haal androidtime op en stuur naar Arduino
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
            String strDate = sdf.format(now.getTime());
            SendDateToArduino(strDate);
           }
    };

    void SendDateToArduino(String DateTime) {
        ((MainActivity) getActivity()).sendMessage("j" + DateTime +"#");
    }

    public void StoreOpArduino() {
        // eerst controle van de input
        boolean fout = false;
        int x = 0;

        String telnumber = txtTelNumber.getText().toString();
        try {
            x = Integer.parseInt(telnumber); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtTelNumber.setText("foutieve input");
            fout = true;
        }

        String droog1 = txtDrooglevel1.getText().toString();
        try {
            x = Integer.parseInt(droog1); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtDrooglevel1.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 1023) {
            txtDrooglevel1.setText("foutieve input");
            fout = true;
        }
        // en nu 4 posities met voorloopnullen maken
        if (!fout) droog1 = String.format("%04d",x);

        String droog2 = txtDrooglevel2.getText().toString();
        try {
            x = Integer.parseInt(droog2); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtDrooglevel2.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 1023) {
            txtDrooglevel2.setText("foutieve input");
            fout = true;
        }
        if (!fout)droog2 = String.format("%04d",x);

        String droog3 = txtDrooglevel3.getText().toString();
        try {
            x = Integer.parseInt(droog3); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtDrooglevel3.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 1023) {
            txtDrooglevel3.setText("foutieve input");
            fout = true;
        }
        if (!fout)droog3 = String.format("%04d",x);

        String droogtijd = txtDroogtijd.getText().toString();
        try {
            x = Integer.parseInt(droogtijd); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtDroogtijd.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 120) {
            txtDroogtijd.setText("foutieve input");
            fout = true;
        }
        if (!fout)droogtijd = String.format("%04d",x);

        String druppelspeling = txtDruppelspeling.getText().toString();
        try {
            x = Integer.parseInt(druppelspeling); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtDruppelspeling.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 100) {
            txtDruppelspeling.setText("foutieve input");
            fout = true;
        }
        if (!fout)druppelspeling = String.format("%04d",x);

        String samples = txtSamples.getText().toString();
        try {
            x = Integer.parseInt(samples); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtSamples.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 40) {
            txtSamples.setText("foutieve input");
            fout = true;
        }
        if (!fout)samples = String.format("%04d",x);

        String vlotterdelay = txtVlotterdelay.getText().toString();
        try {
            x = Integer.parseInt(vlotterdelay); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            txtVlotterdelay.setText("foutieve input");
            fout = true;
        }
        if (x < 0 || x > 120) {
            txtVlotterdelay.setText("foutieve input");
            fout = true;
        }
        if (!fout)vlotterdelay = String.format("%04d",x);

        if (!fout) { // geen fouten in de input: versturen maar!

            // SMS check bijwerken
            if (SMS.isChecked()) ((MainActivity) getActivity()).sendMessage("a#");
            else ((MainActivity) getActivity()).sendMessage("b#");

            // telefoonnummer bijwerken
            ((MainActivity) getActivity()).sendMessage("t" + telnumber + "#");    // sla telefoonnummer op
            txtTelNumber.setText("");

            // opbouwen van de rest van de Arduinoinfo
            Arduinoinfo = droog1 + "$" + droog2 + "$" + droog3 + "$" + droogtijd + "$" +
                    druppelspeling + "$" + samples + "$" + vlotterdelay;
            ((MainActivity) getActivity()).sendMessage("k" + Arduinoinfo + "#");
            // zet alle velden even op blank om te laten zien dat er communicatie geweest is
            txtDrooglevel1.setText("");
            txtDrooglevel2.setText("");
            txtDrooglevel3.setText("");
            txtDroogtijd.setText("");
            txtDruppelspeling.setText("");
            txtSamples.setText("");
            txtVlotterdelay.setText("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, " onResume in fragmenttab3 is aangeroepen");
    }

}
