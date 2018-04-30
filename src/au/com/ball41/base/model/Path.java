package au.com.ball41.base.model;

import java.util.LinkedList;
import java.util.List;

import au.com.ball41.base.internal.BomberConstants;

public class Path
{
    public enum PathResult
    {
        FOUND_BOMBER(1),
        FOUND_PICKUP(3),
        REACHED_MAX_DEPTH(BomberConstants.MAX_SEARCH_DEPTH + 1),
        IS_BLOCKED(BomberConstants.MAX_SEARCH_DEPTH + 2),
        IS_INVALID(BomberConstants.MAX_SEARCH_DEPTH + 100);

        private final int mPriority;

        private PathResult(int inPriority)
        {
            mPriority = inPriority;
        }

        public int getPriority()
        {
            return mPriority;
        }
    }

    private Direction mInitialDirection;

    private LinkedList<Position> mPathPositions;

    private PathResult mResult;

    public Path(Direction inInitialDirection)
    {
        mInitialDirection = inInitialDirection;
        mPathPositions = new LinkedList<Position>();
    }

    public Direction getInitialDirection()
    {
        return mInitialDirection;
    }

    public void addPosition(Position inPosition)
    {
        mPathPositions.add(inPosition);
    }

    public boolean containsPosition(Position inPosition)
    {
        return mPathPositions.contains(inPosition);
    }

    public List<Position> getPathPositions()
    {
        return mPathPositions;
    }

    private void setPathPositions(List<Position> inPositions)
    {
        mPathPositions = new LinkedList<Position>(inPositions);
    }

    public int getLength()
    {
        return mPathPositions.size();
    }

    public void setPathResult(PathResult inResult)
    {
        mResult = inResult;
    }

    public PathResult getResult()
    {
        return mResult;
    }

    // TODO Shorter length should have more sway in deciding paths between close decisions (distant bomber vs close pickup)
    public boolean replaceWith(Path inNewPath)
    {
        if (mResult == null || inNewPath.getResult() == null)
            throw new IllegalStateException("Path is not complete - cannot be compared!");

        if ((mResult.getPriority() + getLength()) > (inNewPath.getResult().getPriority() + inNewPath.getLength()))
            return true;
        else if ((mResult.getPriority() + getLength()) < (inNewPath.getResult().getPriority() + inNewPath.getLength()))
            return false;
        else if (mResult.getPriority() > inNewPath.getResult().getPriority())
            return true;
        else if (mResult.getPriority() < inNewPath.getResult().getPriority())
            return false;
        else
        {
            if (getLength() > inNewPath.getLength())
                return true;
            else if (getLength() < inNewPath.getLength())
                return false;
            else
                return (Math.random() * 10.0) > 5.0 ? false : true;
        }
    }

    public static Path clone(Path inPath)
    {
        Path clonedPath = new Path(inPath.getInitialDirection());

        clonedPath.setPathResult(inPath.getResult());
        clonedPath.setPathPositions(inPath.getPathPositions());

        return clonedPath;
    }
}
