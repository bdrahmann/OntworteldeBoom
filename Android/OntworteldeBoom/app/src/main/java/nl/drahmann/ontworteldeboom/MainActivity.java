/*
    Standaard applicatie om een Android device via Bluetooth te verbinden met een Arduino.
    Gebasserd op BluetoothChatService voorbeeld van Android.
    Alle noodzakelijke classes en res files staan in een library.
    De afhandeling van het BT verkeer gebeurt in BluetoothChatFragment.
    Het aboutscherm wordt opgeroepen via een aboutlibrary.
    Er zijn drie tabs toegevoegd die met swipe bestuurd worden.

    Gemaakt door: BHJ Drahmann
    Datum: 31-1-2016
*/

/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package nl.drahmann.ontworteldeboom;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import nl.drahmann.about.About;
import nl.drahmann.mybtlibrary.chat.BluetoothChatService;
import nl.drahmann.mybtlibrary.chat.Constants;

/**
 * In deze class wordt de methode ProcesInput uitgevoerd.

 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "Main";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    String TabFragment1;
    String TabFragment2;
    String TabFragment3;
    String TabFragment4;
    String TabFragment5;


    public void setTabFragment1(String t) {TabFragment1 = t; }
    public String getTabFragment1() {return TabFragment1; }
    public void setTabFragment2(String t) {TabFragment2 = t; }
    public String getTabFragment2() {return TabFragment2; }
    public void setTabFragment3(String t) {TabFragment3 = t; }
    public String getTabFragment3() {return TabFragment3; }
    public void setTabFragment4(String t) {TabFragment4 = t; }
    public String getTabFragment4() {return TabFragment4; }
    public void setTabFragment5(String t) {TabFragment5 = t; }
    public String getTabFragment5() {return TabFragment5; }

    public int pompnr = 0;      // Hulpvariable in case
    public String active_log;   // het activieve logbestand

    public boolean firstrun = true;

    MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
    ArrayList<String> sdfiles = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // viewpager om de fragments te besturen.
        // Locate the viewpager in activity_main.xml
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        // Set the ViewPagerAdapter into ViewPager
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(5);  // zo blijven er 5 fragments in het werkgeheugen

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
           Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //activity.finish();
        }
    }

    public void ProcesInput(String data) { // verwerkt de input uit de Arduino
		 /*
		 * verwerkt de input uit de Arduino De ingelezen informatie bestaat uit
		 * een code van twee posities met daarna direct de informatie de codes:
		 * 01:
		 * 02 Arduino send vlotterstand
		 * 03 Arduino send datum
		 * 04 Arduino send waarde humidity
		 * 05 Arduino send waarde Temperatuur in C
		 * 06 Arduino send bericht toestand schijf
		 * 07 Arduino send bericht toestand file
		 * 08 Arduino send tijd
		 * 09 Arduino send waarde Lux
		 * 10 Arduino send waarde SMScode
		 * 11 Arduino send files
		 * 12 Arduino send inhoud files
		 * 13 Arduino send deleted filename
		 * 14 Arduino send telefoonnummer
		 * 15 Arduino send PompStatus
		 * 16 Arduino send pompnummer
		 * 17 Arduino send waarde sensor1
		 * 18 Arduino send waarde sensor2
		 * 19 Arduino send waarde sensor3
		 * 20 Arduino send gemiddelde waarde sensor1
		 * 21 Arduino send gemiddelde waarde sensor2
		 * 22 Arduino send gemiddelde waarde sensor3
		 * 23 Arduino send Drooginfo sensor1
		 * 24 Arduino send Drooginfo sensor2
		 * 25 Arduino send Drooginfo sensor3
		 * 26 Arduino send vaste gegevens
		 * 27 Arduino send SMS status
		 * 28 Arduino send SMS bericht
		 * 29 Arduino send status vlotterdelay
		 * 30 Arduino send status sensordelay
		 * 31 Arduino send status handpompen
		 *

		 */

        // opzoeken van fragment1
        String TabOfFragment1 = (this).getTabFragment1();
        FragmentTab1 fragment1 = (FragmentTab1)this
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragment1);
        // opzoeken van fragment2
        String TabOfFragment2 = (this).getTabFragment2();
        FragmentTab2 fragment2 = (FragmentTab2)this
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragment2);
        // opzoeken van fragment3
        String TabOfFragment3 = (this).getTabFragment3();
        FragmentTab3 fragment3 = (FragmentTab3)this
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragment3);
        // opzoeken van fragment4
        String TabOfFragment4 = (this).getTabFragment4();
        FragmentTab4 fragment4 = (FragmentTab4)this
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragment4);
        // opzoeken van fragment5
        String TabOfFragment5 = (this).getTabFragment5();
        FragmentTab5 fragment5 = (FragmentTab5)this
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragment5);

        int i;
        int j;

        if (firstrun){  // alleen de eerste keer
            sendMessage("y#");
            Log.d(TAG, " M188 firstrun = " + firstrun);
            firstrun = false;
        }

        // eerst het ingelezen record opdelen in code en informatie
        String s = "";  // de kode in string vorm
        int kode = 0;   // de kode numeriek
        if (data.length() < 2) {
            GeefFoutboodschap(data.substring(0)); // dit komt soms bij de start van een lopende Arduino
            Log.d(TAG, " M134 datastring is te kort = " + data);
            Log.d(TAG, " M135 data.length() = " + data.length());
            return;
        }

        try {
            s = data.substring(0, 2); // eerste 2 posities bevat de kode
        } catch (StringIndexOutOfBoundsException siobe) { // omzetten is niet gelukt
            Log.d(TAG, " M225 omzetten kodestring niet gelukt. data = " + data);
            return;
        }

        String info = data.substring(2); // de recordinformatie
        if (s.equals("15"))
        Log.d(TAG, " M229 kode = " + s + " info = " + info);
        try {
            kode = Integer.parseInt(s); // omzetten naar integer
        } catch (NumberFormatException ex) { // omzetten is niet gelukt
            GeefFoutboodschap(s);
        }

        switch (kode) {
            case 1:
                //connected = true;
                break;
            case 2: // geef de stand van de vlotter weer
                if (info.equals("L")) { // het niveau staat te laag
                    fragment1.Niveau.setTextSize(14); // veld Niveau highlighted rood maken
                    fragment1.Niveau.setBackgroundColor(0xFFFF0000);
                    fragment1.Niveau.setText("LEEG");
                } else {
                    fragment1.Niveau.setTextSize(14);
                    fragment1.Niveau.setBackgroundColor(0xFF00FF00);
                    fragment1.Niveau.setText("VOLDOENDE");
                }
                break;
            case 3: // Arduino send datum
                fragment1.ADatum.setText(info);
                fragment3.txtDatumTijd.setText(info);
                sendMessage("s#");        // vraag om de gegevens van de schijf en file
                break;
            case 4: // set de humidity in het veld
                fragment1.Humidity.setText(info);
                break;
            case 5: // set de temperatuur in het veld
                fragment1.TempC.setText(info);
                break;
            case 6: // set de schijfinfo in het veld
                fragment1.Schijf.setText(info);
                break;
            case 7: // set de bestandsinfo in het veld
                active_log = info;
                fragment1.Bestand.setText(active_log);
                sendMessage("u#");        // vraag om de Pompstatus
                break;
            case 8: // Arduino send tijd
                fragment1.ATijd.setText(info);
                fragment3.txtDatumTijd.setText(fragment1.ADatum.getText() + " " + info);
                break;
            case 9: // set de Light info in het veld
                fragment1.Lux.setText(info);
                break;
            case 10:    // smscode
                fragment1.Sms.setText(info);
                if (info.equals("ja")) fragment3.SMS.setChecked(true);
                else fragment3.SMS.setChecked(false);
                break;
            case 11:    // files
                sdfiles.clear();
                i = 0;
                j = 0;
                int k = 0;
                int m = info.length();
                while (j < m) {
                    k = info.indexOf(',', k + 1);
                    sdfiles.add(info.substring(j, k));
                    j = k + 1;
                    i += 1;
                }
                fragment4.canclick = true;
                fragment4.setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, sdfiles));
                fragment4.adddb.setVisibility(View.INVISIBLE);
                fragment4.delfile.setVisibility(View.INVISIBLE);

                break;
            case 12:    // inhoud files
                fragment4.progressBar.setVisibility(View.VISIBLE);
                fragment4.progressBar.setMax(fragment4.selFileRecords / 34);
                if (!info.equals("einde")) {
                    fragment4.progressStatus += 1;
                    this.sdfiles.add(info);
                    fragment4.progressBar.setProgress(fragment4.progressStatus);
                    return;
                }
                fragment4.progressBar.setVisibility(View.INVISIBLE);
                fragment4.canclick = false;
                fragment4.adddb.setVisibility(View.VISIBLE);
                fragment4.delfile.setVisibility(View.VISIBLE);
                fragment4.getListView().setVisibility(View.VISIBLE);
                fragment4.setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, sdfiles));
                break;
            case 13:       // deleted filename
                fragment4.tekst.setText(info);
                fragment4.tekst.setVisibility(View.VISIBLE);
                fragment4.getListView().setVisibility(View.INVISIBLE);
                fragment4.adddb.setVisibility(View.INVISIBLE);
                fragment4.delfile.setVisibility(View.INVISIBLE);
                break;
            case 14:    // telefoonnummer
                fragment1.Telefoon.setText(info);
                fragment3.txtTelNumber.setText(info);
                break;
            case 15:    // set de PompStatus
                Integer x = Integer.parseInt(info);
                // set alle velden op grijs

                fragment1.Status01.setBackgroundColor(0xE0009688);
                fragment1.Status02.setBackgroundColor(0xE0009688);
                fragment1.Status03.setBackgroundColor(0xE0009688);
                fragment1.Status04.setBackgroundColor(0xE0009688);
                fragment1.Status05.setBackgroundColor(0xE0009688);
                fragment1.Status06.setBackgroundColor(0xE0009688);
                fragment1.Status07.setBackgroundColor(0xE0009688);
                fragment1.Status08.setBackgroundColor(0xE0009688);

                // set alle opmerkingsvelden uit
                fragment1.Opm01.setVisibility(View.INVISIBLE);
                fragment1.Opm02.setVisibility(View.INVISIBLE);
                fragment1.Opm03.setVisibility(View.INVISIBLE);
                fragment1.Opm04.setVisibility(View.INVISIBLE);
                fragment1.Opm05.setVisibility(View.INVISIBLE);
                fragment1.Opm06.setVisibility(View.INVISIBLE);
                fragment1.Opm07.setVisibility(View.INVISIBLE);
                fragment1.Opm08.setVisibility(View.INVISIBLE);

                // set het x veld op groen
                switch (x) {
                    case 1:
                        fragment1.Status01.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm01.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        fragment1.Status02.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm02.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        fragment1.Status03.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm03.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        fragment1.Status04.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm04.setVisibility(View.VISIBLE);
                        String opm4 = "Pomp " + pompnr + " in gebruik";
                        fragment1.Opm04.setText(opm4);
                        break;
                    case 5:
                        fragment1.Status05.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm05.setVisibility(View.VISIBLE);
                        break;
                    case 6:
                        fragment1.Status06.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm06.setVisibility(View.VISIBLE);
                        break;
                    case 7:
                        fragment1.Status07.setBackgroundColor(0xFF00FF00);
                        fragment1.Opm07.setVisibility(View.VISIBLE);
                        break;
                    case 8:
                        fragment1.Status08.setBackgroundColor(0xFFFF0000);
                        fragment1.Opm08.setVisibility(View.VISIBLE);
                        break;
                }

                break;
            case 16:        // set het Pompnummer
                try {
                    pompnr = Integer.parseInt(info);
                } catch (NumberFormatException ex) { // omzetten is niet gelukt
                    GeefFoutboodschap(info);
                }
                break;
            case 17:    // set de Sensor 1 info in het veld
                fragment1.Sensor1.setText(info);
                fragment2.sensor1 = Integer.parseInt(info);
                break;
            case 18:  // set de Sensor 2 info in het veld
                fragment1.Sensor2.setText(info);
                fragment2.sensor2 = Integer.parseInt(info);
                break;
            case 19: // set de Sensor 3 info in het veld
                fragment1.Sensor3.setText(info);
                fragment2.sensor3 = Integer.parseInt(info);
                fragment2.onSensorChanged();
                break;
            case 20:    // set de gem Sensor 1 info in het veld

                break;
            case 21:  // set de gem Sensor 2 info in het veld

                break;
            case 22: // set de gem Sensor 3 info in het veld

                break;
            case 23:    // set de droog 1 info in het veld
                fragment1.Droog1.setText(info);
                break;
            case 24:  // set de droog 2 info in het veld
                fragment1.Droog2.setText(info);
                break;
            case 25: // set de droog 3 info in het veld
                fragment1.Droog3.setText(info);
                break;
            case 26:    // lees de settings in
                // opsplitsen info in afzonderlijke velden. Ze zijn gescheiden door een "$"
                int b = 0;    // beginpositie in string
                int e;    // eindpositie in string

                // drooglevel1
                e = info.indexOf("$");
                fragment3.txtDrooglevel1.setText(info.substring(b, e));
                // drooglevel2
                b = e + 1;    // het nieuwe begin
                e = info.indexOf("$", b);
                fragment3.txtDrooglevel2.setText(info.substring(b, e));
                // drooglevel3
                b = e + 1;    // het nieuwe begin
                e = info.indexOf("$", b);
                fragment3.txtDrooglevel3.setText(info.substring(b, e));
                // droogltijd
                b = e + 1;    // het nieuwe begin
                e = info.indexOf("$", b);
                fragment3.txtDroogtijd.setText(info.substring(b, e));
                // druppelspeling
                b = e + 1;    // het nieuwe begin
                e = info.indexOf("$", b);
                fragment3.txtDruppelspeling.setText(info.substring(b, e));
                // samples
                b = e + 1;    // het nieuwe begin
                e = info.indexOf("$", b);
                fragment3.txtSamples.setText(info.substring(b, e));
                // vlotterdelay
                b = e + 1;    // het nieuwe begin
                fragment3.txtVlotterdelay.setText(info.substring(b));

                break;
            case 27:        // SMS Status
                fragment1.SMSstatus.setText(info);
                break;
            case 28:        // SMS bericht
                fragment1.SMSbericht.setText(info);
                break;
            case 29:        // status van de vlotterdelay. Om statusbar bij te werken
                int eerste = 0;
                int tweede = info.indexOf("$");
                int Status = Integer.parseInt(info.substring(eerste, tweede));
                eerste = tweede + 1;
                int max = Integer.parseInt(info.substring(eerste));
                if (Status == 0)  {
                    fragment1.toon_vlotter_delay.setVisibility(View.VISIBLE);
                    fragment1.ProgressVlotterText.setVisibility(View.VISIBLE);
                    fragment1.toon_vlotter_delay.setMax(max);
                }
                fragment1.progressvlotterstatus = Status;
                fragment1.WerkVlotterBarBij();
                if (Status == max) {
                    fragment1.toon_vlotter_delay.setVisibility(View.INVISIBLE);
                    fragment1.ProgressVlotterText.setVisibility(View.INVISIBLE);
                }
                break;

            case 30:        // status van de sensordelay. Om statusbar bij te werken
                eerste = 0;
                tweede = info.indexOf("$");
                Status = Integer.parseInt(info.substring(eerste, tweede));
                eerste = tweede + 1;
                max = Integer.parseInt(info.substring(eerste));
                if (Status < 5)  {  // moet eigenlijk 0 zijn, maar dat is niet altijd zo
                    fragment1.toon_sensor_delay.setVisibility(View.VISIBLE);
                    fragment1.Opm03.setVisibility(View.VISIBLE);
                    fragment1.toon_sensor_delay.setMax(max);
                }
                fragment1.progresssensorstatus = Status;
                fragment1.WerkSensorBarBij();
                if (Status == max) {
                    fragment1.toon_sensor_delay.setVisibility(View.INVISIBLE);
                    fragment1.Opm03.setVisibility(View.INVISIBLE);
                }
                break;

            case 31:        // status handpompen
                if (info.substring(0,1).equals("0")) fragment1.Handpomp1.setChecked(false);
                else fragment1.Handpomp1.setChecked(true);
                if (info.substring(1).equals("0")) fragment1.Handpomp2.setChecked(false);
                else fragment1.Handpomp2.setChecked(true);
                break;

            // volgende case
            //
            default:
                // de rest is een onbekende kode
                GeefFoutboodschap(s);
        }

    }

    private void GeefFoutboodschap(String s) { // afvangen van verkeerde Arduino kode
        new AlertDialog.Builder(this)
                .setTitle("Onbekende kode")
                .setMessage(
                        "Arduino heeft een onbekende kode " + s
                                + " opgestuurd. Het programma kan wel doorgaan. ")
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // finish(); // of andere actie mogelijk
                            }
                        }).show();
    }

    /*----------------------------------------------------------------------------------------------------------
    hier  een copie van BluetoothChat
    This controls Bluetooth to communicate with other devices.
    */

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    public void  ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        // Log.d(TAG, "message ontvangen in sendMessage = " + message);
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            Log.d(TAG, "message send  = " + message);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        /*FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        */
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        /*FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        */
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        public String totaalMessage = "";
        @Override
        public void handleMessage(Message msg) {
            // FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            // mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    // Make string totaalMessage from the ReadMessage until delimiter "#"
                    // Log.d(TAG, "ReadMessage = " + readMessage);
                    totaalMessage = totaalMessage + readMessage;
                    if(totaalMessage.contains("#")) { // end of information from Arduino
                        int i = totaalMessage.indexOf("#");
                        String Result = totaalMessage.substring(0,i);
                        ProcesInput(Result);    // hier result doorgeven aan Main
                        // Log.d(TAG, "Result = " + Result);
                        totaalMessage = totaalMessage.substring(i+1);   // met de rest van de string beginnen
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }

            /*
            case R.id.action_settings: // toon instelscherm
                Intent i = new Intent(this, PreferenceActivity.class);
                startActivity(i);
                return true;

             */

            case R.id.menu_info: {
                About.show(( this), getString(R.string.about),
                        getString(R.string.close));
                return true;
            }

        }
        return false;
    }


}
