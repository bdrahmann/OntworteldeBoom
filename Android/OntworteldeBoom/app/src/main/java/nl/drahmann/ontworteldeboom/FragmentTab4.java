package nl.drahmann.ontworteldeboom;

        import android.os.Bundle;

        import android.os.Handler;
        import android.support.v4.app.ListFragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

public class FragmentTab4  extends ListFragment
{
    static final String TAG = "BDR";

    public Button toondir;
    public Button adddb;
    public Button delfile;

    public ProgressBar progressBar;
    public TextView tekst;

    int b;      // beginpositie
    int e;      // eindpositie
    int l;      // aantal files
    int lregel;
    int l_records;  // aantal aangemaakte DB records

    int code;           // recordcode
    long datumtijd;     // record datum en tijd
    float w1;           // record w1
    float w2;           // record w2. niet altijd aanwezig
    String opmerking;   // record opmerking. niet altijd aanwezig

    private Handler mHandler = new Handler();

    int progressStatus = 0;
    String selFile;             // geselecteerde file in de list
    int selFileRecords = 0;
    String selFileRegel;
    boolean canclick = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Get the view from fragment_4
        View myView = inflater.inflate(R.layout.fragment_4, container, false);

        String myTag = getTag();
        ((MainActivity)getActivity()).setTabFragment4(myTag);

        toondir = ((Button)myView.findViewById(R.id.btnToon));
        toondir.setOnClickListener(ToonDir);
        adddb = ((Button)myView.findViewById(R.id.btnAdd));
        adddb.setOnClickListener(AddDb);
        delfile = ((Button)myView.findViewById(R.id.btnDelete));
        delfile.setOnClickListener(DelFile);
        progressBar = ((ProgressBar)myView.findViewById(R.id.pbVoortgang));
        tekst = ((TextView)myView.findViewById(R.id.tvTekst));

        return myView;
    }

    View.OnClickListener ToonDir = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            ((MainActivity) getActivity()).sendMessage("f#");
            progressStatus = 0;
            getListView().setVisibility(View.VISIBLE);
            tekst.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener AddDb = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // test of geselecteerde file de file in gebruik is
            if (selFile.equals(((MainActivity) getActivity()).active_log)) { // als de geselecteerde file de logfile is
                tekst.setVisibility(View.VISIBLE);
                tekst.setText("Dit is de actieve LOG-file. Toevoegen aan DB niet mogelijk.");
                adddb.setVisibility(View.INVISIBLE);
            }
            else {
                getListView().setVisibility(View.INVISIBLE);
                l = ((MainActivity) getActivity()).sdfiles.size();   // aantal files
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(l);
                progressStatus = 0;
                l_records = 0;
                MaakRecords();
            }
        }
    };

    View.OnClickListener DelFile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getListView().setVisibility(View.VISIBLE);
            ((MainActivity)getActivity()).sendMessage("q" + selFile + "#");
            ((MainActivity)getActivity()).sdfiles.clear();
        }
    };

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (!this.canclick) return;

        selFileRegel = ((MainActivity)getActivity()).sdfiles.get(position);
        selFile = selFileRegel.substring(0, 12);
        selFileRecords = Integer.parseInt(selFileRegel.substring(13));

        ((MainActivity) getActivity()).sendMessage("g" + selFile + "#");
        getListView().setVisibility(View.INVISIBLE);
        progressStatus = 0;
        ((MainActivity)getActivity()).sdfiles.clear();
    }

    private String LeesVeld(int position)
    {
        b = e+1;
        e = (((MainActivity)getActivity()).sdfiles.get(position)).indexOf(',', e + 1);  // scheidingskomma
        // Log.d(TAG, "b = " + b + " e = "  + e);
        if (e == -1)  e = lregel-1;   // einde bereikt
        // Log.d(TAG, "lregel = " + lregel + " e = "  + e);
        String str = (((MainActivity)getActivity()).sdfiles.get(position)).substring(b, e);
        // Log.d(TAG, "str = " + str );
        return str;
    }

    public void MaakRecords()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                for (int i=0;i<l;i++)       // voor alle records
                {
                    lregel = (((MainActivity) getActivity()).sdfiles.get(i)).length(); // lengte record
                    b = 0;
                    e = -1;
                    w2 = 0.0F;
                    opmerking = "";

                    code = Integer.parseInt(LeesVeld(i));   // code inlezen
                    // Log.d(TAG, "code = "  + code);

                    Object localObject = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        localObject = ((SimpleDateFormat) localObject).parse(LeesVeld(i));  // datum inlezen
                        datumtijd = ((Date) localObject).getTime();
                        // Log.d(TAG, "datumtijd = "  + datumtijd);
                    } catch (ParseException e1) {
                        datumtijd = 0L;
                        Log.d("BDR", " Tab4_148 datumconversiefout = ");
                    }

                    try {
                        w1 = Float.parseFloat(LeesVeld(i)); // w1 inlezen
                        // Log.d(TAG, "w1 = "  + w1);
                    } catch (NumberFormatException e2) {
                        w1 = -1.0F;
                        System.out.println("NumberFormatException: " + e2.getMessage());
                        Log.d("BDR", " Tab4_158 conversiefout w1 = " + w1);
                    }

                    if (e < lregel-1) {      // er is nog meer in te lezen
                        try {
                            w2 = Float.parseFloat(LeesVeld(i)); // w2 inlezen
                            // Log.d(TAG, "w2 = "  + w2);
                        } catch (NumberFormatException e3) {
                            w2 = -1.0F;
                            System.out.println("NumberFormatException: " + e3.getMessage());
                            Log.d("BDR", " Tab4_167 conversiefout w2 = " + w2);
                        }
                        if (e < lregel-1) opmerking = LeesVeld(i);        // opmerking inlezen

                    }   // alles is nu ingelezen

                    l_records = l_records + 1;
                    Tree tree = new Tree(code, datumtijd, w1, w2, opmerking);
                    ((MainActivity) getActivity()).dbHandler.addTree(tree);  // database bijwerken
                    progressStatus += 1;    // progressbar bijwerken
                    mHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });

                }   //  volgende record

                mHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            getListView().setVisibility(View.VISIBLE);
                            ((MainActivity)getActivity()).sdfiles.clear();
                            tekst.setVisibility(View.VISIBLE);
                            tekst.setText(l_records + " records toegevoegd aan DataBase");
                            canclick = false;
                        }
            });
            }
        }).start();
    }




    public void onResume()
    {
        super.onResume();
        Log.d("BDR", " onResume in FragmentTab4 is aangeroepen");
    }
}



