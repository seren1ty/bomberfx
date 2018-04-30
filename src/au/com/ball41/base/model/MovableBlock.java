package au.com.ball41.base.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.ball41.base.internal.BomberConstants;

public class MovableBlock extends Block
{
    private static final long MIN_MOVE_WAITTIME = 100;

    private float mSpeed = 100;

    private Color mTeamColor;

    private int mMaxBombs = 2;

    private int mLives = 1;

    private Long mLastMoveTime = null;

    private Map<Integer, ActionBlock> mBombs = new HashMap<Integer, ActionBlock>();

    private Map<Type, List<Integer>> mPickups = new HashMap<Type, List<Integer>>();

    public MovableBlock(int inCurrentX, int inCurrentY, Type inType, int inIdNum, float inSpeed, Color inColor)
    {
        super(inIdNum, inCurrentX, inCurrentY, inType);

        mSpeed = inSpeed;
        mTeamColor = inColor;
    }

    public ActionBlock dropBomb(Platform inController, int inGlobalIdNum)
    {
        if (mBombs.size() == getMaxBombs())
            return(null);

        ActionBlock bomb = new ActionBlock(getCurrentX(), getCurrentY(), Type.BOMB, inController, inGlobalIdNum, getIdNum(),
                                           getBombsExtraReach());

        mBombs.put(inGlobalIdNum, bomb);

        return(bomb);
    }

    public int getBombsReach()
    {
        return BomberConstants.MIN_REACH + getBombsExtraReach();
    }

    private int getBombsExtraReach()
    {
        return mPickups.get(Type.PICKUP_FLAME) != null ? mPickups.get(Type.PICKUP_FLAME).size() : 0;
    }

    public ActionBlock getBomb(int inBombIdNum)
    {
        return(mBombs.get(new Integer(inBombIdNum)));
    }

    public int getBombCount()
    {
        return(mBombs.size());
    }

    private int getMaxBombs()
    {
        if (mPickups.get(Type.PICKUP_BOMB) != null)
            return(mMaxBombs + mPickups.get(Type.PICKUP_BOMB).size());

        return(mMaxBombs);
    }

    public void explodeBomb(int inBombIdNum)
    {
        System.out.println("=== MovableBlock.explodeBomb() - bomber: " + getIdNum() + ", bomb: " + inBombIdNum
                           + " removed from Map");
        mBombs.remove(new Integer(inBombIdNum));
    }

    public void setSpeed(float inSpeed)
    {
        mSpeed = inSpeed;
    }

    public float getSpeed()
    {
        return(mSpeed);
    }

    public void setTeamColor(Color inTeamColor)
    {
        mTeamColor = inTeamColor;
    }

    public Color getTeamColor()
    {
        return(mTeamColor);
    }

    public void attack()
    {
        decreaseLives();
    }

    private void increaseLives()
    {
        mLives++;
    }

    private void decreaseLives()
    {
        mLives--;
    }

    public boolean isAlive()
    {
        return(mLives > 0);
    }

    public void addPickup(Type inPickupType, Integer inPickupIndex)
    {
        if (mPickups.containsKey(inPickupType))
        {
            mPickups.get(inPickupType).add(inPickupIndex);
        }
        else
        {
            List<Integer> pickupIndexes = new ArrayList<Integer>();
            pickupIndexes.add(inPickupIndex);

            mPickups.put(inPickupType, pickupIndexes);
        }
    }

    @Override
    public void setCurrentPosition(int inX, int inY)
    {
        super.setCurrentPosition(inX, inY);

        mLastMoveTime = System.currentTimeMillis();
    }

    public boolean hasWaitedLongEnough()
    {
        if (mLastMoveTime == null || (System.currentTimeMillis() - mLastMoveTime) >= MIN_MOVE_WAITTIME)
            return(true);

        return(false);
    }
}
