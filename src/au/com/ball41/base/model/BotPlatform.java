package au.com.ball41.base.model;

import java.util.List;

public interface BotPlatform
{
	boolean isActive();

	boolean isValidPosition(int inX, int inY);
	
	Block getBlockAtPosition(int inX, int inY);

	boolean moveBot(int idNum, int inCurrX, int inCurrY, Direction inDirection);

	int dropBomb(int idNum);
	
	void showBomb(final int inBomberIdNum, final int inBombIdNum);
	
	List<Position> getAllBombPositions();
}
