package nl.drahmann.ontworteldeboom;

/**
 * Created by Bernard on 14-2-2016.
 */

public class Tree
{
    private int _code;
    private long _datetime;
    private int _id;
    private String _opmerking;
    private float _waarde1;
    private float _waarde2;

    public Tree() {}

    public Tree(int paramInt1, int paramInt2, long paramLong, float paramFloat1, float paramFloat2, String paramString)
    {
        this._id = paramInt1;
        this._code = paramInt2;
        this._datetime = paramLong;
        this._waarde1 = paramFloat1;
        this._waarde2 = paramFloat2;
        this._opmerking = paramString;
    }

    public Tree(int paramInt, long paramLong, float paramFloat1, float paramFloat2, String paramString)
    {
        this._code = paramInt;
        this._datetime = paramLong;
        this._waarde1 = paramFloat1;
        this._waarde2 = paramFloat2;
        this._opmerking = paramString;
    }

    public int getCode()
    {
        return this._code;
    }

    public long getDateTime()
    {
        return this._datetime;
    }

    public int getID()
    {
        return this._id;
    }

    public String getOpmerking()
    {
        return this._opmerking;
    }

    public float getWaarde1()
    {
        return this._waarde1;
    }

    public float getWaarde2()
    {
        return this._waarde2;
    }

    public void setCode(int paramInt)
    {
        this._code = paramInt;
    }

    public void setDateTime(long paramLong)
    {
        this._datetime = paramLong;
    }

    public void setID(int paramInt)
    {
        this._id = paramInt;
    }

    public void setOpmerking(String paramString)
    {
        this._opmerking = paramString;
    }

    public void setWaarde1(float paramFloat)
    {
        this._waarde1 = paramFloat;
    }

    public void setWaarde2(float paramFloat)
    {
        this._waarde1 = paramFloat;
    }

    public String toString()
    {
        return "Tree [_id= " + this._id + ", code= " + this._code + ", datetime= " + this._datetime + ", waarde1= " + this._waarde1 + ", waarde2= " + this._waarde2 + ", opmerking= " + this._opmerking + "]";
    }
}


/* Location:              C:\Users\Bernard\Documents\AndroidReverseEngeneering\dex2jar-2.0\dex2jar-2.0\nl.drahmann.megauptree-dex2jar.jar!\nl\drahmann\megauptree\Tree.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */
