package nl.drahmann.ontworteldeboom;

        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.v4.app.ListFragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.nio.channels.FileChannel;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;

public class FragmentTab5  extends ListFragment
{
    static final String TAG = "BDR";

    private SQLiteDatabase newDB;
    private ArrayList<String> results = new ArrayList();
    private String tableName = "tree";
    public TextView dbTekst;
    public TextView exportdbtekst;
    public Button toondb;
    public Button deletedb;
    public Button exportdb;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the view from fragment_5
        View mijnView = inflater.inflate(R.layout.fragment_5, container, false);

        String myTag = getTag();
        ((MainActivity)getActivity()).setTabFragment5(myTag);

        dbTekst = ((TextView)mijnView.findViewById(R.id.tvdbTekst));
        exportdbtekst = ((TextView)mijnView.findViewById(R.id.tvdbExportTekst));
        toondb = ((Button)mijnView.findViewById(R.id.btnToonDB));
        toondb.setOnClickListener(ToonDB);
        deletedb = ((Button)mijnView.findViewById(R.id.btnDeleteDB));
        deletedb.setOnClickListener(DeleteDB);
        exportdb = ((Button)mijnView.findViewById(R.id.btnExportDB));
        exportdb.setOnClickListener(ExportDB);

        return mijnView;
    }

    View.OnClickListener ToonDB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openAndQueryDatabase();
            displayResultList();
        }
    };

    View.OnClickListener DeleteDB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteDatabase();
        }
    };

    View.OnClickListener ExportDB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            exportDatabase();
        }
    };

    public void deleteDatabase() {
        newDB = new MyDBHandler(getActivity(), null, null, 1).getWritableDatabase();
        try {
            File data = Environment.getDataDirectory();
            // Log.d(TAG, "data:  " + data );
            String currentDBPath = newDB.getPath();
            // Log.d(TAG, "currentDBPath:  " + currentDBPath );
            File currentDB = new File(currentDBPath);
            // Log.d(TAG, "currentDB:  " + currentDB );
            if (currentDB.exists()) {
                newDB.deleteDatabase(currentDB);
                Log.d(TAG, "FT5_97 DataBase: " + currentDB + " deleted:  " );
                exportdbtekst.setText( "DataBase: " + currentDB + " is deleted:  ");
            }
            else {
                Log.d(TAG, "FT_101 currentDB:  " + currentDB + " bestaat niet.");
                exportdbtekst.setText("DB:  " + currentDB + " bestaat niet.");
            }
        } catch (Exception e) {
            Log.d(TAG, "delete database:  " + newDB + " mislukt." );
            exportdbtekst.setText(" delete database:  " + newDB + " mislukt.");
        }
    }

    public void exportDatabase() {
        newDB = new MyDBHandler(getActivity(), null, null, 1).getWritableDatabase();
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            //Log.d(TAG, "sd:  " + sd );
            //Log.d(TAG, "data:  " + data );
            if (sd.canWrite()) {
                String currentDBPath = newDB.getPath();
                String backupDBPath = "backup_Ontworteldeboom.db";
                // Log.d(TAG, "currentDBPath:  " + currentDBPath );
                // Log.d(TAG, "backupDBPath:  " + backupDBPath );

                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                // Log.d(TAG, "currentDB:  " + currentDB );
                // Log.d(TAG, "backupDB:  " + backupDB );

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.d(TAG, "backup DataBase aangemaakt:  " + backupDB);
                    exportdbtekst.setText("backupDB aangemaakt:  " + backupDB);
                }
                else {
                    Log.d(TAG, "currentDB:  " + currentDB + " bestaat niet.");
                    exportdbtekst.setText("DB:  " + currentDB + " bestaat niet.");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "exportdatabase:  " + newDB + " mislukt." );
            exportdbtekst.setText("exportdatabase:  " + newDB + " mislukt.");
        }

    }

    private void openAndQueryDatabase()
    {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        try
        {
            results.clear();
            newDB = new MyDBHandler(getActivity(), null, null, 1).getWritableDatabase();
            Cursor localCursor = newDB.rawQuery("SELECT * FROM tree", null);
            if ((localCursor != null) && (localCursor.moveToFirst()))
            {
                boolean bool;
                do
                {
                    int i = localCursor.getInt(localCursor.getColumnIndex("code"));
                    long l = localCursor.getLong(localCursor.getColumnIndex("datetime"));
                    int j = localCursor.getInt(localCursor.getColumnIndex("waarde1"));
                    int k = localCursor.getInt(localCursor.getColumnIndex("waarde2"));
                    String strdate = localSimpleDateFormat.format(Long.valueOf(l));
                    String opm = localCursor.getString(localCursor.getColumnIndex("opmerking"));
                    results.add("code: " + i + ", Datum: " + strdate + ", W1: " + j + ", W2: " + k + ", Opmerking: " + opm);
                    bool = localCursor.moveToNext();
                } while (bool);
            }
            return;
        }
        catch (SQLiteException localSQLiteException)
        {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
            return;
        }
        finally {}
    }

    private void displayResultList() {

        exportdbtekst.setText("");
        dbTekst.setText("This data is retrieved from the database ");
        setListAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, results));
        getListView().setTextFilterEnabled(true);
    }

    public void onResume()
    {
        super.onResume();
        Log.d("BDR", " onResume in FragmentTab5 is aangeroepen");
    }
}



