package au.com.ball41.base.model;

public class Block
{
    public enum Type
    {
        PLAYER, BOT, FIXED, FIXED_SOLID, BOMB, EXPLOSION, PICKUP_FLAME, PICKUP_BOMB
    }

    private int mIdNum;
    private int mCurrentX;
    private int mCurrentY;
    private Type mType;

    public Block(int inIdNum, int inCurrentX, int inCurrentY, Type inType)
    {
        mIdNum = inIdNum;
        mCurrentX = inCurrentX;
        mCurrentY = inCurrentY;
        mType = inType;
    }

    public int getIdNum()
    {
        return(mIdNum);
    }

    public int getCurrentX()
    {
        return(mCurrentX);
    }

    public int getCurrentY()
    {
        return(mCurrentY);
    }

    public void setCurrentPosition(int inX, int inY)
    {
        mCurrentX = inX;
        mCurrentY = inY;
    }

    public Type getType()
    {
        return(mType);
    }

    public void setType(Type inType)
    {
        mType = inType;
    }
}
