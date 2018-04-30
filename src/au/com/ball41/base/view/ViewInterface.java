/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.com.ball41.base.view;

import au.com.ball41.base.model.Block.Type;

/**
 *
 * @author BAll41
 */
public interface ViewInterface
{
    void explodeBomb(int inIdNum);

    void showExplosion(int inIdNum, int inPosX, int inPosY);

    void hideExplosion(int inIdNum);

    void destroyBomber(int inIdNum);

    void destroyBlock(int inIdNum);

    void updateScore(int inIdNum, int inScore);

	void showPickup(int inIdNum, Type inPickupType, int inPosX, int inPosY);
	
	void hidePickup(int inIdNum);

	void moveBot(int inBotIdNum, int inPosX, int inPosY);
	
	void showBomb(final int inBomberIdNum, final int inBombIdNum);
}
