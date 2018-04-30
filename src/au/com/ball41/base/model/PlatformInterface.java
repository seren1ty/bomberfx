package au.com.ball41.base.model;


public interface PlatformInterface {

	void resetPlatform();

	int getFixedBlockPosX(int inIdNum);
	int getFixedBlockPosY(int inIdNum);

	int getFixedSolidBlockPosX(int inIdNum);
	int getFixedSolidBlockPosY(int inIdNum);

	int dropBomb(int inBomberIdNum);

	int getBombPosX(int inBomberId, int inBombIdNum);
	int getBombPosY(int inBomberId, int inBombIdNum);

	boolean move(int inBlockId, Direction inDirection);
}
