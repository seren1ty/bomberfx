package au.com.ball41.base.model;

public enum Direction
{
	UP,
	RIGHT,
	DOWN,
	LEFT;
	
	public static int getXForDirection(int inX, Direction inDirection)
	{
		return getXForDirection(inX, inDirection, 1);
	}
	
	public static int getXForDirection(int inX, Direction inDirection, int inDistance)
	{
		if (inDirection == LEFT)
			return inX - inDistance;
		else if (inDirection == RIGHT)
			return inX + inDistance;
		else // (inDirection == UP || inDirection == DOWN)
			return inX;
	}
	
	public static int getYForDirection(int inX, Direction inDirection)
	{
		return getYForDirection(inX, inDirection, 1);
	}
	
	public static int getYForDirection(int inY, Direction inDirection, int inDistance)
	{
		if (inDirection == UP)
			return inY - inDistance;
		else if (inDirection == DOWN)
			return inY + inDistance;
		else // (inDirection == LEFT || inDirection == RIGHT)
			return inY;
	}
}