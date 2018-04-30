package au.com.ball41.base.model;

import static au.com.ball41.base.model.Direction.DOWN;
import static au.com.ball41.base.model.Direction.LEFT;
import static au.com.ball41.base.model.Direction.RIGHT;
import static au.com.ball41.base.model.Direction.UP;
import static au.com.ball41.base.model.Direction.getXForDirection;
import static au.com.ball41.base.model.Direction.getYForDirection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.ball41.base.model.Path.PathResult;

public class AutonomousBlock extends MovableBlock implements Runnable
{
    enum DirectionPriorityType
    {
        NORMAL(1), PICKUP(5), TARGET(10);

        private int mPriorityIncreaseFactor;

        private DirectionPriorityType(int inPriorityIncreaseFactor)
        {
            mPriorityIncreaseFactor = inPriorityIncreaseFactor;
        }

        public int getPriorityIncreaseFactor()
        {
            return mPriorityIncreaseFactor;
        }
    }

    private class DirectionPriority
    {
        private Map<Direction, Integer> mDirectionCount = new HashMap<Direction, Integer>();

        public DirectionPriority()
        {
            mDirectionCount.put(Direction.UP, 0);
            mDirectionCount.put(Direction.RIGHT, 0);
            mDirectionCount.put(Direction.DOWN, 0);
            mDirectionCount.put(Direction.LEFT, 0);
        }

        public void increase(Direction inDirection)
        {
            increase(inDirection, DirectionPriorityType.NORMAL);
        }

        public void increase(Direction inDirection, DirectionPriorityType inPriorityType)
        {
            mDirectionCount.put(inDirection, mDirectionCount.get(inDirection) + inPriorityType.getPriorityIncreaseFactor());
        }

        public int getCount(Direction inDirection)
        {
            return mDirectionCount.get(inDirection);
        }

        public Direction getDirectionOfPriority(Set<Direction> inDangerousDirections, Priority inPriority)
        {
            Direction highestDirection = null;

            for (Direction currDir : mDirectionCount.keySet())
            {
                if (inDangerousDirections.contains(currDir))
                    continue;

                if (highestDirection == null)
                {
                    highestDirection = currDir;
                    continue;
                }

                if (mDirectionCount.get(highestDirection) < mDirectionCount.get(currDir))
                    highestDirection = currDir;
                else if (mDirectionCount.get(highestDirection) == mDirectionCount.get(currDir))
                    highestDirection = (Math.random() * 10.0) > 5.0 ? highestDirection : currDir;
            }

            if (inPriority == Priority.IN_DANGER || inPriority == Priority.ATTACKING)
                return highestDirection;
            else
                return (Math.random() * 10.0) > 7.0 ? highestDirection : null;
        }
    }

    private enum Priority
    {
        IN_DANGER, ATTACKING, PICKUP, EXPLORE, DROP_BOMB;
    }

    private class BotState
    {
        private Priority mPriority;

        private DirectionPriority mDirectionPriority;

        private Set<Direction> mDangerousDirections;

        private Set<Direction> mBlockedDirections;

        public BotState()
        {
            mPriority = Priority.EXPLORE;
            mDirectionPriority = new DirectionPriority();
            mDangerousDirections = new HashSet<Direction>();
            mBlockedDirections = new HashSet<Direction>();
        }

        public void setPriority(Priority priority)
        {
            mPriority = priority;
        }

        public Priority getPriority()
        {
            return mPriority;
        }

        public DirectionPriority getDirectionPriority()
        {
            return mDirectionPriority;
        }

        void addDangerousDirection(Direction inDirection)
        {
            mDangerousDirections.add(inDirection);
        }

        public Set<Direction> getDangerousDirections()
        {
            return mDangerousDirections;
        }

        public boolean isSafe(Direction inDirection)
        {
            return !getDangerousDirections().contains(inDirection);
        }

        private void addBlockedDirection(Direction inDirection)
        {
            mBlockedDirections.add(inDirection);
        }

        public boolean isNotBlockedAlready(Direction inDirection)
        {
            return !mBlockedDirections.contains(inDirection);
        }
    }

    private static final int PAUSE_TIME = 250;

    private static final int MAX_DEPTH = 20;

    private static final List<Direction> DIRECTIONS = new ArrayList<Direction>(Arrays.asList(UP, RIGHT, DOWN, LEFT));

    private BotPlatform mPlatform;

    private boolean mActive;

    private int mLastBombIdNum;

    private boolean mCarefulModeActive;

    private Map<Direction, Path> mPathOptions;

    public AutonomousBlock(BotPlatform inPlatform, int inCurrentX, int inCurrentY, Type inType, int inIdNum, float inSpeed,
                           Color inColor)
    {
        super(inCurrentX, inCurrentY, inType, inIdNum, inSpeed, inColor);

        mLastBombIdNum = -1;
        mPlatform = inPlatform;
        mActive = true;

        new Thread(this).start();
    }

    public void run()
    {
        // Looping to determine next move
        while (platformActive() && isAlive())
            handleBehaviour();
    }

    private boolean platformActive()
    {
        return mActive;
    }

    private void handleBehaviour()
    {
        /*
         * v1
         *
         * check x and y for danger (within a certain range?) if danger/bomb found then move determine reach of bomb check for
         * exits move to an exit (priority 1)
         *
         * if no danger then look for players if player found attack move to within range (priority 2)
         *
         * if no player found then look for pickups if pickup found go for pickup move to pickup (priority 3)
         *
         * if no pickups found then check surrounding blocks (crosshatch, then diagonal) and blowup drop bomb check for exits move
         * to an exit (priority 1)
         */

        /*
         * v2
         *
         * scan whole map generate 3 tiers of direction choices/priorities
         * tier 3: general goal (eg. enemy player or pickup is north of me = up is a priority)
         * tier 2: surrounding area (eg. bomb is ticking on the above adjacent row = up is dangerous)
         *      2c: 3 rows up, right, down, left (lowest tier 2 influence)
         *      2b: 2 rows up, right, down, left
         *      2a: 1 row up, right, down, left (highest tier 2 influence)
         * tier 1: immediate/line of sight (eg. a bomb is ticking left of me on my row = left is out, up is out, move right or down)
         *         use these direction priorities to decide final move
         */

        /**
         *
         Path recognition plot paths to a certain depth add each path to a map with the key being the end goal either a Map of
         * List of possible paths, maybe prioritised OR a Map of the best path to each goal type if tied with another choice, try
         * to reuse/continue on last used path does path lead onto row/column of timer item, then its dangerous is the path blocked
         * how deep is the path (distance to goal) shorter paths would be prioritised only need enough info to decide on one of the
         * 4 initial directions/moves must recalculate path every time a path that meets itself again is invalid
         */

        BotState state = new BotState();

        mPathOptions = new HashMap<Direction, Path>();

        Position currentPos = new Position(getCurrentX(), getCurrentY());

        // Depth of available positions in each direction
        for (Direction direction : DIRECTIONS)
        {
            int nextX = getXForDirection(currentPos.getPosX(), direction);
            int nextY = getYForDirection(currentPos.getPosY(), direction);

            search(new Path(direction), direction, currentPos, new Position(nextX, nextY), true);
        }

        // If only one direction has >= 1 available spaces, activate CAREFUL_MODE
        checkIfCareNeeded(state);
        // When in CAREFUL_MODE, only drop a new bomb after the current one has exploded (i.e. check with getBomb(mLastBombId))

        List<Position> allBombPositions = mPlatform.getAllBombPositions();

        // Choose best direction/path
        for (Position bombPos : allBombPositions)
        {
            if (bombPos.getPosX() == getCurrentX() || bombPos.getPosY() == getCurrentY())
            {
                state.setPriority(Priority.IN_DANGER);

                if (bombPos.getPosX() == getCurrentX())
                {
                    if (bombPos.getPosY() < getCurrentY())
                        state.addDangerousDirection(UP);
                    else
                        state.addDangerousDirection(DOWN);
                }
                else if (bombPos.getPosY() == getCurrentY())
                {
                    if (bombPos.getPosX() < getCurrentX())
                        state.addDangerousDirection(LEFT);
                    else
                        state.addDangerousDirection(RIGHT);
                }

                break;
            }
        }

        Direction bestDirection = null;

        if (state.getPriority() == Priority.IN_DANGER)
        {
            // Determine safest/shortest path to safety
            for (Direction direction : mPathOptions.keySet())
            {
                boolean directionInvalid = false;

                Path path = mPathOptions.get(direction);

                for (Position bombPos : allBombPositions)
                {
                    if (path.getLength() == 0 ||
                        path.containsPosition(bombPos) ||
                        path.getPathPositions().get(path.getLength() - 1).getPosX() == bombPos.getPosX() ||
                        path.getPathPositions().get(path.getLength() - 1).getPosY() == bombPos.getPosY())
                    {
                        directionInvalid = true;
                    }
                }

                if (!directionInvalid)
                {
                    bestDirection = direction;
                    break;
                }
            }
        }
        else
        {
            // Select best path
            for (Direction direction : mPathOptions.keySet())
            {
                Path path = mPathOptions.get(direction);

                if (bestDirection == null || mPathOptions.get(bestDirection).replaceWith(mPathOptions.get(direction)))
                {
                    boolean directionInvalid = false;

                    for (Position bombPos : allBombPositions)
                    {
                        if (path.getLength() == 0 ||
                            //path.containsPosition(bombPos) ||
                            (path.getPathPositions().get(0).getPosX() == bombPos.getPosX() ||
                             path.getPathPositions().get(0).getPosY() == bombPos.getPosY()))
                        {
                            directionInvalid = true;
                        }
                    }

                    if (!directionInvalid)
                        bestDirection = direction;
                }
            }
        }

         //if (state.getPriority() == Priority.DROP_BOMB) { dropBomb(); state.setPriority(Priority.IN_DANGER); }

         /*
         * Direction directionOfPriority = state.getDirectionPriority().getDirectionOfPriority(state.getDangerousDirections(),
         * state.getPriority());
         *
         * if (directionOfPriority != null)
         */

        // If no safe/valid path found, stay put
        if (bestDirection != null)
            move(bestDirection);

        state.setPriority(Priority.EXPLORE);
        state.getDangerousDirections().clear();

        pause();
    }

    private void search(Path inPath, Direction inDirection, Position inCurrentPos, Position inFuturePos, boolean inFirstPosition)
    {
        Path clonedPath = Path.clone(inPath);
        // Determine if position results in end of path
        PathResult pathResult = determineIfPathHasReachedAResult(clonedPath, inFuturePos);

        if (pathResult != null)
        {
            if (pathResult != PathResult.IS_INVALID)
            {
                clonedPath.setPathResult(pathResult);
                saveBestPath(clonedPath);
            }

            return;
        }
        else
        // Accept the new position
        {
            clonedPath.addPosition(inFuturePos);
        }

        // Continue search
        for (Direction newDirection : DIRECTIONS)
        {
            // We do not want to include paths that double-back on themselves
            // if (directionsAreOpposite(inDirection, newDirection) && !inFirstPosition)
            //     continue;

            int nextX = getXForDirection(inFuturePos.getPosX(), newDirection);
            int nextY = getYForDirection(inFuturePos.getPosY(), newDirection);

            search(clonedPath, newDirection, inFuturePos, new Position(nextX, nextY), false);
        }
    }

    private boolean directionsAreOpposite(Direction directionA, Direction directionB)
    {
        if ((directionA == UP && directionB == DOWN) ||
            (directionA == DOWN && directionB == UP) ||
            (directionA == LEFT && directionB == RIGHT) ||
            (directionA == RIGHT && directionB == LEFT))
            return true;

        return false;
    }

    private PathResult determineIfPathHasReachedAResult(Path inPath, Position inNextPos)
    {
        // Reject invalid paths

        // Path is invalid: REJECT (i.e. loop)
        if (inPath.containsPosition(inNextPos))
            return PathResult.IS_INVALID;

        // Path is invalid: REJECT (i.e. loop to self)
        if (inNextPos.getPosX() == getCurrentX() && inNextPos.getPosY() == getCurrentY())
            return PathResult.IS_INVALID;

        // Path is invalid: REJECT (i.e. leads to illegal move)
        if (!mPlatform.isValidPosition(inNextPos.getPosX(), inNextPos.getPosY()))
            return PathResult.IS_INVALID;

        if (inPath.getLength() == MAX_DEPTH)
            return PathResult.REACHED_MAX_DEPTH;

        Block blockAtPosition = mPlatform.getBlockAtPosition(inNextPos.getPosX(), inNextPos.getPosY());

        if (blockAtPosition == null) // Available
            return null;

        // Path is invalid: REJECT (i.e. Leads to danger)
        if (blockAtPosition.getType() == Type.BOMB || blockAtPosition.getType() == Type.EXPLOSION)
            return PathResult.IS_INVALID;

        if (blockAtPosition.getType() == Type.PICKUP_BOMB || blockAtPosition.getType() == Type.PICKUP_FLAME)
            return PathResult.FOUND_PICKUP;

        if (blockAtPosition.getType() == Type.PLAYER || blockAtPosition.getType() == Type.BOT)
            return PathResult.FOUND_BOMBER;

        if (blockAtPosition.getType() == Type.FIXED || blockAtPosition.getType() == Type.FIXED_SOLID)
            return PathResult.IS_BLOCKED;

        // If unknown block type then error...
        System.out.println("UNKNOWN BLOCK TYPE: " + blockAtPosition.getType());

        throw new RuntimeException("Unknown type: " + blockAtPosition.getType());
    }

    private void saveBestPath(Path inPath)
    {
        // Only save best path for each from direction
        // Shorter paths to the same goal should have priority

        Path path = mPathOptions.get(inPath.getInitialDirection());

        if (path == null || path.replaceWith(inPath))
        {
            Path clonedPath = Path.clone(inPath);

            mPathOptions.put(inPath.getInitialDirection(), clonedPath);
        }
    }

    private void checkIfCareNeeded(BotState inState)
    {
        int numOfRoomyDirections = 0;

        for (Direction currDirection : DIRECTIONS)
        {
            if (inState.getDirectionPriority().getCount(currDirection) >= 2)
                numOfRoomyDirections++;
        }

        if (numOfRoomyDirections <= 1)
            mCarefulModeActive = true;
        else
            mCarefulModeActive = false;
    }

    private void dropBomb()
    {
        if (mCarefulModeActive && getBomb(mLastBombIdNum) != null)
            return;

        System.out.println("Bomb dropped by bot: " + getIdNum());

        mLastBombIdNum = mPlatform.dropBomb(getIdNum());

        mPlatform.showBomb(getIdNum(), mLastBombIdNum);
    }

    private void scout(BotState inState, Direction inDirection, int inDepth)
    {
        // direction is dangerous OR direction was previously blocked
        if (inState.isSafe(inDirection) && inState.isNotBlockedAlready(inDirection))
        {
            int newX = getXForDirection(getCurrentX(), inDirection, inDepth);
            int newY = getYForDirection(getCurrentY(), inDirection, inDepth);

            if (!mPlatform.isValidPosition(newX, newY))
            {
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - NOT_VALID");
                return;
            }

            Block blockAtPosition = mPlatform.getBlockAtPosition(newX, newY);

            if (blockAtPosition == null) // Available
            {
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - NULL_SPACE");
                inState.getDirectionPriority().increase(inDirection);
            }
            else if (blockAtPosition.getType() == Type.PICKUP_BOMB || blockAtPosition.getType() == Type.PICKUP_FLAME)
            {
                if (inState.getPriority() == Priority.IN_DANGER)
                    return;

                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - FOUND_PICKUP");
                // inState.setPriority(Priority.PICKUP);
                inState.getDirectionPriority().increase(inDirection, DirectionPriorityType.PICKUP);
            }
            else if (blockAtPosition.getType() == Type.PLAYER || blockAtPosition.getType() == Type.BOT)
            {
                if (inState.getPriority() == Priority.IN_DANGER)
                    return;

                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - FOUND_TARGET");

                if (inDepth < getBombsReach())
                {
                    inState.setPriority(Priority.DROP_BOMB);
                }
                else
                {
                    inState.setPriority(Priority.ATTACKING);
                    inState.getDirectionPriority().increase(inDirection, DirectionPriorityType.TARGET);
                }
            }
            else if (blockAtPosition.getType() == Type.BOMB)
            {
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - FOUND_BOMB");
                inState.setPriority(Priority.IN_DANGER);
                inState.addDangerousDirection(inDirection);
            }
            else if (blockAtPosition.getType() == Type.EXPLOSION)
            {
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - FOUND_EXPLOSION");
                inState.addDangerousDirection(inDirection);
            }
            else if (blockAtPosition.getType() == Type.FIXED)
            {
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - BREAKABLE_BLOCK");

                if (inDepth < getBombsReach())
                    inState.setPriority(Priority.DROP_BOMB);
                else
                    inState.addBlockedDirection(inDirection);
            }
            else
            {
                // If fixed solid block, add to blocked directions...
                System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - SOLID_BLOCK");
                inState.addBlockedDirection(inDirection);
            }
        }
        else
        {
            System.out.println("Depth: " + inDepth + ", Direction: " + inDirection + " - DIRECTION_OUT");
        }
    }

    private void move(Direction inDirection)
    {
        if (inDirection == null)
            return;

        System.out.println("Moving bot: " + getIdNum());

        mPlatform.moveBot(getIdNum(), getCurrentX(), getCurrentY(), inDirection);
    }

    private void pause()
    {
        try
        {
            Thread.sleep(PAUSE_TIME);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
            Logger.getLogger(ActionBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop()
    {
        mActive = false;
    }
}