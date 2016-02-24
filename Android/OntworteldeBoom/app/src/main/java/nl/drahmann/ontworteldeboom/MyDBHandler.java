package nl.drahmann.ontworteldeboom;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteDatabase.CursorFactory;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

public class MyDBHandler
        extends SQLiteOpenHelper
{
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_DATE_TIME = "datetime";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_OPMERKING = "opmerking";
    public static final String COLUMN_WAARDE1 = "waarde1";
    public static final String COLUMN_WAARDE2 = "waarde2";
    private static final String DATABASE_NAME = "treeDB.db";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_TREE = "tree";

    public MyDBHandler(Context paramContext, String paramString, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt)
    {
        super(paramContext, "treeDB.db", paramCursorFactory, 4);
    }

    public void addTree(Tree paramTree)
    {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("code", Integer.valueOf(paramTree.getCode()));
        localContentValues.put("datetime", Long.valueOf(paramTree.getDateTime()));
        localContentValues.put("waarde1", Float.valueOf(paramTree.getWaarde1()));
        localContentValues.put("waarde2", Float.valueOf(paramTree.getWaarde2()));
        localContentValues.put("opmerking", paramTree.getOpmerking());
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        Log.d("addTree", paramTree.toString());
        localSQLiteDatabase.insert("tree", null, localContentValues);
        localSQLiteDatabase.close();
    }

    public boolean deleteTree(int paramInt)
    {
        boolean bool = false;
        Object localObject = "Select * FROM tree WHERE code =  \"" + paramInt + "\"";
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        localObject = localSQLiteDatabase.rawQuery((String)localObject, null);
        Tree localTree = new Tree();
        if (((Cursor)localObject).moveToFirst())
        {
            localTree.setID(Integer.parseInt(((Cursor)localObject).getString(0)));
            localSQLiteDatabase.delete("tree", "_id = ?", new String[] { String.valueOf(localTree.getID()) });
            ((Cursor)localObject).close();
            bool = true;
        }
        Log.d("deleteTree", localTree.toString());
        localSQLiteDatabase.close();
        return bool;
    }

    public Tree findTree(int paramInt)
    {
        Object localObject = "Select * FROM tree WHERE code =  \"" + paramInt + "\"";
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        Cursor localCursor = localSQLiteDatabase.rawQuery((String)localObject, null);
        localObject = new Tree();
        if (localCursor.moveToFirst())
        {
            localCursor.moveToFirst();
            ((Tree)localObject).setID(Integer.parseInt(localCursor.getString(0)));
            ((Tree)localObject).setCode(Integer.parseInt(localCursor.getString(1)));
            ((Tree)localObject).setDateTime(Long.parseLong(localCursor.getString(2)));
            ((Tree)localObject).setWaarde1(Float.parseFloat(localCursor.getString(3)));
            ((Tree)localObject).setWaarde2(Float.parseFloat(localCursor.getString(4)));
            ((Tree)localObject).setOpmerking(localCursor.getString(5));
            localCursor.close();
            Log.d("findTree", ((Tree)localObject).toString());
        }
        for (;;)
        {
            localSQLiteDatabase.close();
            return (Tree)localObject;
            // localObject = null;
        }
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase)
    {
        paramSQLiteDatabase.execSQL("CREATE TABLE tree(_id INTEGER PRIMARY KEY,code INTEGER,datetime INTEGER,waarde1 INTEGER,waarde2 INTEGER,opmerking TEXT )");
    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
    {
        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS tree");
        onCreate(paramSQLiteDatabase);
    }
}



