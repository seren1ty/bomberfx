/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.com.ball41.base.model;

import static au.com.ball41.base.internal.BomberConstants.MAX_REACH;
import static au.com.ball41.base.internal.BomberConstants.MIN_REACH;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author BAll41
 */
public class ActionBlock extends Block implements Runnable {

    private Platform mController;
    private long mDuration = 0;
    private int mReach = 0;
    private int mBomberIdNum;

    public ActionBlock(int inCurrentX, int inCurrentY, Type inType, Platform inController, int inGlobalIdNum, int inBomberIdNum)
    {
        this(inCurrentX, inCurrentY, inType, inController, inGlobalIdNum, inBomberIdNum, 0);
    }

    public ActionBlock(int inCurrentX, int inCurrentY, Type inType, Platform inController, int inGlobalIdNum, int inBomberIdNum, int inExtraReach)
    {
      super(inGlobalIdNum, inCurrentX, inCurrentY, inType);

        mController = inController;
        mBomberIdNum = inBomberIdNum;

        if (inType == Type.BOMB)
        {
            mDuration = 3000;

            if (MIN_REACH + inExtraReach > MAX_REACH)
              mReach = MAX_REACH;
            else
              mReach = MIN_REACH + inExtraReach;
        }
        else if (inType == Type.EXPLOSION)
        {
            mDuration = 1000;
            mReach = 1;
        }
    }

    private void performAction(Type inType)
    {
        if (inType == Type.BOMB)
        {
            mController.explodeBomb(mBomberIdNum, getIdNum(), mReach, null);
        }

        if (inType == Type.EXPLOSION)
        {
            mController.stopExplosion(getIdNum(), getCurrentX(), getCurrentY());
        }
    }

    public void run() {
        try {
            if (mDuration > 0)
                Thread.sleep(mDuration);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }

        performAction(getType());
    }

    public int getBomberIdNum()
    {
        return(mBomberIdNum);
    }

    public int getReach()
    {
        return(mReach);
    }
}
