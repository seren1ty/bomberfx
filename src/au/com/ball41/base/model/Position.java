package au.com.ball41.base.model;

public class Position
{
	private int mPosX;
	private int mPosY;
	
	public Position(int inPosX, int inPosY)
	{
		mPosX = inPosX;
		mPosY = inPosY;
	}
	
	public int getPosX()
	{
		return mPosX;
	}
	
	public int getPosY()
	{
		return mPosY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mPosX;
		result = prime * result + mPosY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (mPosX != other.mPosX)
			return false;
		if (mPosY != other.mPosY)
			return false;
		return true;
	}
}
