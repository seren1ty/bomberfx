package au.com.ball41.base.model;

import static au.com.ball41.base.model.Direction.getXForDirection;
import static au.com.ball41.base.model.Direction.getYForDirection;
import au.com.ball41.base.model.Block.Type;
import au.com.ball41.base.view.ViewInterface;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

public class Platform implements PlatformInterface, BotPlatform {

    private ViewInterface mView;

    private int mWidth;
    private int mHeight;
    private Block[][] mGridArray;

    private int mMapType;

    private List<Block> mFixedBlocks;

    private List<Block> mFixedSolidBlocks;

    private Block[][] mPickups;

    private List<MovableBlock> mBombers;

    private final int NUM_OF_PLAYERS;

    private final int NUM_OF_BOTS;

    private final int MIN_BOMBS;

    private final int MAX_BOMBS;

    private String[] mGlobalBombIds;

    private final int MAX_EXPLOSIONS;

    private String[] mGlobalExplosionIds;

    private final int MAX_PICKUPS;

    private int mGlobalPickupIds;

    private Map<Integer, Integer> mScores = new HashMap<Integer, Integer>();

    private boolean mCurrentlyActive;

    private List<Position> mAllBombPositions;

    public Platform(ViewInterface inView,
            int inWidth,
            int inHeight,
            int inNumOfPlayers,
            int inNumOfBots,
            int inNumOfFixedBlocks,
            int inMapType,
            int inMinBombs)
    {
      mCurrentlyActive = true;

        mView = inView;
        mWidth = inWidth;
        mHeight = inHeight;
        mGridArray = new Block[mWidth][mHeight];
        mPickups = new Block[mWidth][mHeight];
        mBombers = new LinkedList<MovableBlock>();
        mFixedBlocks = new LinkedList<Block>();
        mFixedSolidBlocks = new LinkedList<Block>();
        mAllBombPositions = new LinkedList<Position>();

        NUM_OF_PLAYERS = inNumOfPlayers;
        NUM_OF_BOTS = inNumOfBots;
        MAX_EXPLOSIONS = mWidth * mHeight;
        MIN_BOMBS = inMinBombs;
        MAX_PICKUPS = inNumOfPlayers * 10;
        MAX_BOMBS = MIN_BOMBS + MAX_PICKUPS;

        mGlobalBombIds = new String[MAX_BOMBS];
        mGlobalExplosionIds = new String[MAX_EXPLOSIONS];
        mGlobalPickupIds = 0;

        setupPlayers();
        setupBots(false);
        selectFixedSolidBlocks(inMapType);
        setFixedBlocks(inNumOfFixedBlocks/*, inNumOfBots*/);

        for (int i = 0; i < MAX_BOMBS; i ++)
            mGlobalBombIds[i] = "EMPTY";

        for (int j = 0; j < MAX_EXPLOSIONS; j ++)
            mGlobalExplosionIds[j] = "EMPTY";
    }

    public void resetPlatform()
    {
      // TODO When game exits, kill bots

        int previousFixedBlocksSize = mFixedBlocks.size();

        mFixedSolidBlocks = new LinkedList<Block>();
        mFixedBlocks = new LinkedList<Block>();
        mPickups = new Block[mWidth][mHeight];
        mBombers = new LinkedList<MovableBlock>();
        mGlobalBombIds = new String[MAX_BOMBS];
        mGlobalExplosionIds = new String[MAX_EXPLOSIONS];
        mGlobalPickupIds = 0;
        mGridArray = new Block[mWidth][mHeight];
        mAllBombPositions = new LinkedList<Position>();

        setupPlayers();
        setupBots(true);
        selectFixedSolidBlocks(mMapType);
        setFixedBlocks(previousFixedBlocksSize);

        for (int i = 0; i < MAX_BOMBS; i ++)
            mGlobalBombIds[i] = "EMPTY";

        for (int j = 0; j < MAX_EXPLOSIONS; j ++)
            mGlobalExplosionIds[j] = "EMPTY";
    }

  private void stopBots()
  {
    for (MovableBlock block : mBombers)
      {
        if (block.getType() == Type.BOT)
          ((AutonomousBlock) block).stop(); // SUGG try reduce lives instead, i.e. attack()
      }
  }

    public boolean isActive()
    {
      // TODO This does not work
      return mCurrentlyActive;
    }

    public boolean move(int inBlockId, Direction inDirection)
    {
        MovableBlock bomber = mBombers.get(inBlockId);

        if (!bomber.isAlive() || !bomber.hasWaitedLongEnough())
            return(false);

        int currX = bomber.getCurrentX();
        int currY = bomber.getCurrentY();

        return performMove(bomber, currX, currY, getXForDirection(currX, inDirection), getYForDirection(currY, inDirection));
    }

    private synchronized boolean performMove(MovableBlock inBlock, int inOldX, int inOldY, int inNewX, int inNewY)
    {
        if (isAvailable(inNewX, inNewY))
        {
            inBlock.setCurrentPosition(inNewX, inNewY);

            mGridArray[inNewX][inNewY] = inBlock;

            if (!(mGridArray[inOldX][inOldY] instanceof ActionBlock))
                mGridArray[inOldX][inOldY] = null;

            return(true);
        }
        else if (isValidPosition(inNewX, inNewY) && mGridArray[inNewX][inNewY].getType() == Type.EXPLOSION)
        {
            ActionBlock explosion = (ActionBlock) mGridArray[inNewX][inNewY];

            attackBomber(inBlock, explosion.getBomberIdNum());
        }
        else if (isValidPosition(inNewX, inNewY) &&
                 (mGridArray[inNewX][inNewY].getType() == Type.PICKUP_FLAME ||
                  mGridArray[inNewX][inNewY].getType() == Type.PICKUP_BOMB))
        {
            Block pickup = mGridArray[inNewX][inNewY];

            inBlock.addPickup(pickup.getType(), pickup.getIdNum());

            inBlock.setCurrentPosition(inNewX, inNewY);

            mGridArray[inNewX][inNewY] = inBlock;

            if (!(mGridArray[inOldX][inOldY] instanceof ActionBlock))
                mGridArray[inOldX][inOldY] = null;

            mView.hidePickup(pickup.getIdNum());

            return(true);
        }

        return(false);
    }

    private boolean isAvailable(int inX, int inY)
    {
        return(isValidPosition(inX, inY) && mGridArray[inX][inY] == null);
    }

    public boolean isValidPosition(int inX, int inY)
    {
        return(inX >= 0 && inY >= 0 && inX < mWidth && inY < mHeight);
    }

    private int getWidth()
    {
        return(mWidth);
    }

    private int getHeight()
    {
        return(mHeight);
    }

    private void setupPlayers()
    {
        if (NUM_OF_PLAYERS <= 2)
        {
            MovableBlock player1 = new MovableBlock(0, 0, Type.PLAYER, 0, 5, Color.BLUE);
            mBombers.add(player1);
            mGridArray[0][0] = player1;
            MovableBlock player2 = new MovableBlock(mWidth - 1, mHeight - 1, Type.PLAYER, 1, 5, Color.GREEN);
            mBombers.add(player2);
            mGridArray[mWidth - 1][mHeight - 1] = player2;
        }
    }

    private MovableBlock getPlayer(int inPlayerId)
    {
        return(mBombers.get(inPlayerId));
    }

    private void setupBots(boolean inIsReset)
    {
      if (inIsReset)
        stopBots();

      if (NUM_OF_BOTS >= 1)
        {
            AutonomousBlock bot1 = new AutonomousBlock(this, mWidth - 1, 0, Type.BOT, 2, 5, Color.ORANGE);
            mBombers.add(bot1);
            mGridArray[mWidth - 1][0] = bot1;
        }

        if (NUM_OF_BOTS >= 2)
        {
            AutonomousBlock bot2 = new AutonomousBlock(this, 0, mHeight - 1, Type.BOT, 3, 5, Color.MAGENTA);
            mBombers.add(bot2);
            mGridArray[0][mHeight - 1] = bot2;
        }
    }

    private void selectFixedSolidBlocks(int inMapType)
    {
        // Prepare maps
        String map1 =  "...........|" +
                       ".#.#.#.#.#.|" +
                       "...........|" +
                       ".#.#.#.#.#.|" +
                       "...........|" +
                       ".#.#.#.#.#.|" +
                       "...........|" +
                       ".#.#.#.#.#.|" +
                       "...........|" +
                       ".#.#.#.#.#.|" +
                       "...........";

        Map<Integer, String> maps = new HashMap<Integer, String>();
        maps.put(1, map1);

        mMapType = inMapType;

        setFixedSolidBlocks(maps.get(inMapType));
    }

    private void setFixedSolidBlocks(String inMap)
    {
        int blockId = 0;

        StringTokenizer tokens = new StringTokenizer(inMap, "|");

        for (int currY = 0; tokens.hasMoreTokens(); currY ++)
        {
            String row = tokens.nextToken();

            for (int currX = 0; currX < row.length(); currX ++)
            {
                if (row.charAt(currX) == '#')
                {
                    Block fixedSolidBlock = new Block(blockId ++, currX, currY, Type.FIXED_SOLID);
                    mGridArray[currX][currY] = fixedSolidBlock;
                    mFixedSolidBlocks.add(fixedSolidBlock);
                }
            }
        }
    }

    public int getFixedSolidBlockPosX(int inIdNum)
    {
        return(((mFixedSolidBlocks.get(inIdNum).getCurrentX() + 1) * 50) - 50);
    }

    public int getFixedSolidBlockPosY(int inIdNum)
    {
        return(((mFixedSolidBlocks.get(inIdNum).getCurrentY() + 1) * 50) - 50);
    }

    private void setFixedBlocks(int inNumOfBlocks)
    {
        Random randomNumberGenerator = new Random();
        for (int index = 0; index < inNumOfBlocks; index++)
        {
            int posX = 0;
            int posY = 0;

            do
            {
                // Loop until we have a position we can use.
                posX = randomNumberGenerator.nextInt(11);
                if (posX < 2)
                {
                    posY = randomNumberGenerator.nextInt(11);
                    while(posY < 2 || (NUM_OF_BOTS >= 2 && posY > 8))
                    {
                        posY = randomNumberGenerator.nextInt(11);
                    }
                }
                else if (posX > 8)
                {
                    posY = randomNumberGenerator.nextInt(11);
                    while(posY > 8 || (NUM_OF_BOTS >= 1 && posY < 2))
                    {
                        posY = randomNumberGenerator.nextInt(11);
                    }
                }
                else
                {
                    posY = randomNumberGenerator.nextInt(11);
                }
            }
            while (!isAvailable(posX, posY));

            Block fixedBlock = new Block(index, posX, posY, Type.FIXED);

            mGridArray[posX][posY] = fixedBlock;
            mFixedBlocks.add(fixedBlock);
        }
    }

    public int getFixedBlockPosX(int inIdNum)
    {
        return(((mFixedBlocks.get(inIdNum).getCurrentX() + 1) * 50) - 50);
    }

    public int getFixedBlockPosY(int inIdNum)
    {
        return(((mFixedBlocks.get(inIdNum).getCurrentY() + 1) * 50) - 50);
    }

    public synchronized int dropBomb(int inBomberIdNum)
    {
        MovableBlock bomber = mBombers.get(inBomberIdNum);

        if (!bomber.isAlive() || mGridArray[bomber.getCurrentX()][bomber.getCurrentY()] instanceof ActionBlock)
            return(-1);

        int globalBombIdNum = getNextGlobalBombId();

        ActionBlock bomb = bomber.dropBomb(this, globalBombIdNum);

        if (bomb == null)
            return(-1);

        setGlobalBombIdNum(bomb.getIdNum(), "TAKEN");

        mAllBombPositions.add(new Position(bomber.getCurrentX(), bomber.getCurrentY()));

        mGridArray[bomber.getCurrentX()][bomber.getCurrentY()] = bomb;

        new Thread(bomb).start();

        return(globalBombIdNum);
    }

    public void showBomb(int inBomberIdNum, int inBombIdNum)
    {
        mView.showBomb(inBomberIdNum, inBombIdNum);
    }

    public void explodeBomb(int inBomberIdNum, int inBombIdNum, int inReach, Direction inIgnoreDirection)
    {
        System.out.println("Controller: explodeBomb() - bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", reach: " + inReach);

        ActionBlock explodingBomb = mBombers.get(inBomberIdNum).getBomb(inBombIdNum);

        if (explodingBomb == null)
        {
            System.out.println(">>>>> bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", already exploded!");
            return;
        }

        int posX = explodingBomb.getCurrentX();
        int posY = explodingBomb.getCurrentY();

        if (mBombers.get(inBomberIdNum).getCurrentX() == posX && mBombers.get(inBomberIdNum).getCurrentY() == posY)
            attackBomber(mBombers.get(inBomberIdNum), inBomberIdNum);

        startExplosion(posX, posY, inBomberIdNum);

        boolean explodeUpBlocked = false;
        boolean explodeRightBlocked = false;
        boolean explodeDownBlocked = false;
        boolean explodeLeftBlocked = false;

        for (int i = 1; i <= inReach; i ++)
        {
            // Explode UP
            int upPosY = posY - i;
            if (inIgnoreDirection != Direction.UP && !explodeUpBlocked && upPosY >= 0)
            {
                System.out.println("=== explodeBomb() - bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", reach(so far): " + i + "\n"
                                 + "=== posX: " + posX + ", posY: " + upPosY + " - Direction: UP");
                Block block = mGridArray[posX][upPosY];

                if (block == null)
                {
                    startExplosion(posX, upPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.FIXED_SOLID)
                {
                    explodeUpBlocked = true;
                }
                else if (block.getType() == Type.FIXED)
                {
                    explodeUpBlocked = true;
                    generateRandomPickup(posX, upPosY);
                    attackBlock(block);
                    startExplosion(posX, upPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.PLAYER || block.getType() == Type.BOT)
                {
                    attackBomber((MovableBlock) block, inBomberIdNum);
                    startExplosion(posX, upPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.BOMB)
                {
                    explodeUpBlocked = true;
                    ActionBlock attackedBomb = (ActionBlock) block;
                    explodeBomb(attackedBomb.getBomberIdNum(), attackedBomb.getIdNum(), attackedBomb.getReach(), Direction.DOWN);
                }
            }

            // Explode RIGHT
            int rightPosX = posX + i;
            if (inIgnoreDirection != Direction.RIGHT && !explodeRightBlocked && rightPosX < mWidth)
            {
                System.out.println("=== explodeBomb() - bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", reach(so far): " + i + "\n"
                                 + "=== posX: " + rightPosX + ", posY: " + posY + " - Direction: RIGHT");
                Block block = mGridArray[rightPosX][posY];

                if (block == null)
                {
                    startExplosion(rightPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.FIXED_SOLID)
                {
                    explodeRightBlocked = true;
                }
                else if (block.getType() == Type.FIXED)
                {
                    explodeRightBlocked = true;
                    generateRandomPickup(rightPosX, posY);
                    attackBlock(block);
                    startExplosion(rightPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.PLAYER || block.getType() == Type.BOT)
                {
                    attackBomber((MovableBlock) block, inBomberIdNum);
                    startExplosion(rightPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.BOMB)
                {
                    explodeRightBlocked = true;
                    ActionBlock attackedBomb = (ActionBlock) block;
                    explodeBomb(attackedBomb.getBomberIdNum(), attackedBomb.getIdNum(), attackedBomb.getReach(), Direction.LEFT);
                }
            }

            // Explode DOWN
            int downPosY = posY + i;
            if (inIgnoreDirection != Direction.DOWN && !explodeDownBlocked && downPosY < mHeight)
            {
                System.out.println("=== explodeBomb() - bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", reach(so far): " + i + "\n"
                                 + "=== posX: " + posX + ", posY: " + downPosY + " - Direction: DOWN");
                Block block = mGridArray[posX][downPosY];

                if (block == null)
                {
                    startExplosion(posX, downPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.FIXED_SOLID)
                {
                    explodeDownBlocked = true;
                }
                else if (block.getType() == Type.FIXED)
                {
                    explodeDownBlocked = true;
                    generateRandomPickup(posX, downPosY);
                    attackBlock(block);
                    startExplosion(posX, downPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.PLAYER || block.getType() == Type.BOT)
                {
                    attackBomber((MovableBlock) block, inBomberIdNum);
                    startExplosion(posX, downPosY, inBomberIdNum);
                }
                else if (block.getType() == Type.BOMB)
                {
                    explodeDownBlocked = true;
                    ActionBlock attackedBomb = (ActionBlock) block;
                    explodeBomb(attackedBomb.getBomberIdNum(), attackedBomb.getIdNum(), attackedBomb.getReach(), Direction.UP);
                }
            }

            // Explode LEFT
            int leftPosX = posX - i;
            if (inIgnoreDirection != Direction.LEFT && !explodeLeftBlocked && leftPosX >= 0)
            {
                System.out.println("=== explodeBomb() - bomber:" + inBomberIdNum + ", bomb: " + inBombIdNum + ", reach(so far): " + i + "\n"
                                 + "=== posX: " + leftPosX + ", posY: " + posY + " - Direction: LEFT");

                Block block = mGridArray[leftPosX][posY];

                if (block == null)
                {
                    startExplosion(leftPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.FIXED_SOLID)
                {
                    explodeLeftBlocked = true;
                }
                else if (block.getType() == Type.FIXED)
                {
                    explodeLeftBlocked = true;
                    generateRandomPickup(leftPosX, posY);
                    attackBlock(block);
                    startExplosion(leftPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.PLAYER || block.getType() == Type.BOT)
                {
                    attackBomber((MovableBlock) block, inBomberIdNum);
                    startExplosion(leftPosX, posY, inBomberIdNum);
                }
                else if (block.getType() == Type.BOMB)
                {
                    explodeLeftBlocked = true;
                    ActionBlock attackedBomb = (ActionBlock) block;
                    explodeBomb(attackedBomb.getBomberIdNum(), attackedBomb.getIdNum(), attackedBomb.getReach(), Direction.RIGHT);
                }
            }
        }

        // Cleanup Bomber (if survived)
        if (mBombers.get(inBomberIdNum) != null)
            mBombers.get(inBomberIdNum).explodeBomb(inBombIdNum);

        // Cleanup Platform
        mGlobalBombIds[inBombIdNum] = "EMPTY";

        mAllBombPositions.remove(new Position(explodingBomb.getCurrentX(), explodingBomb.getCurrentY()));

        // Cleanup View
        mView.explodeBomb(inBombIdNum);
    }

    private void attackBomber(MovableBlock inAttackedBomber, int inBomberIdNumOfBombOwner)
    {
        inAttackedBomber.attack();

        if (!inAttackedBomber.isAlive())
        {
            // Cleanup Bomber
            mGridArray[inAttackedBomber.getCurrentX()][inAttackedBomber.getCurrentY()] = null;
            inAttackedBomber.setCurrentPosition(-1, -1);

            // Cleanup View
            mView.destroyBomber(inAttackedBomber.getIdNum());

            checkForWinnerAndUpdateScore();
        }
    }

    private void checkForWinnerAndUpdateScore()
    {
        List<MovableBlock> aliveBombers = new LinkedList<MovableBlock>();
        for (int i = 0; i < mBombers.size(); i++)
        {
            if (mBombers.get(i).isAlive())
                aliveBombers.add(mBombers.get(i));
        }

        if (aliveBombers.size() == 1)
            increaseScore(aliveBombers.get(0).getIdNum());
    }

    private void attackBlock(Block inAttackedBlock)
    {
        mGridArray[inAttackedBlock.getCurrentX()][inAttackedBlock.getCurrentY()] = null;

        mView.destroyBlock(inAttackedBlock.getIdNum());
    }

    private Block generateRandomPickup(int inPosX, int inPosY)
    {
      Block randomPickup = null;

      if (mGlobalPickupIds < MAX_PICKUPS)
      {
        Random randomNumberGenerator = new Random();

        int nextInt = randomNumberGenerator.nextInt(30);

        if (nextInt <= 10)
        {
          int randomInt = randomNumberGenerator.nextInt(2);

          Type pickupType = Type.PICKUP_FLAME;

          if (randomInt == 1)
            pickupType = Type.PICKUP_BOMB;

          randomPickup = new Block(mGlobalPickupIds ++, inPosX, inPosY, pickupType);

          mPickups[inPosX][inPosY] = randomPickup;
        }
      }

      return(randomPickup);
  }

  private void startExplosion(int inPosX, int inPosY, int inBomberIdNum)
    {
        int explosionIdNum = createNextGlobalExplosionId();

        ActionBlock explosion = new ActionBlock(inPosX, inPosY, Type.EXPLOSION, this, explosionIdNum, inBomberIdNum);

        mGridArray[inPosX][inPosY] = explosion;

        new Thread(explosion).start();

        mView.showExplosion(explosionIdNum, inPosX, inPosY);
    }

    void stopExplosion(int inExplosionIdNum, int inPosX, int inPosY)
    {
        mGlobalExplosionIds[inExplosionIdNum] = "EMPTY";

        mView.hideExplosion(inExplosionIdNum);

        // Check if a pickup needs display after fixed block explosion is hidden
        Block pickupToDisplay = mPickups[inPosX][inPosY];

        mGridArray[inPosX][inPosY] = pickupToDisplay;

        if (pickupToDisplay != null)
        {
          System.out.println("Platform.attackBlock() - pickup dropped - id: " + pickupToDisplay.getIdNum() + ", type: " + pickupToDisplay.getType());

          mPickups[inPosX][inPosY] = null;

          mView.showPickup(pickupToDisplay.getIdNum(), pickupToDisplay.getType(), pickupToDisplay.getCurrentX(), pickupToDisplay.getCurrentY());
        }
    }

    private void increaseScore(int inBomberIdNum)
    {
        if (mBombers.get(inBomberIdNum).isAlive())
        {
            if (mScores.get(inBomberIdNum) != null)
                mScores.put(inBomberIdNum, mScores.get(inBomberIdNum) + 1);
            else
                mScores.put(inBomberIdNum, 1);

            mView.updateScore(inBomberIdNum, mScores.get(inBomberIdNum));
        }
    }

    public int getBombPosX(int inBomberIdNum, int inBombIdNum)
    {
        return(mBombers.get(inBomberIdNum).getBomb(inBombIdNum).getCurrentX());
    }

    public int getBombPosY(int inBomberIdNum, int inBombIdNum)
    {
        return(mBombers.get(inBomberIdNum).getBomb(inBombIdNum).getCurrentY());
    }

    private void setGlobalBombIdNum(int inGlobalIdNum, String inValue)
    {
        System.out.println("Setting globalId: " + inGlobalIdNum + " with value: " + inValue);
        mGlobalBombIds[inGlobalIdNum] = inValue;
    }

    private int getNextGlobalBombId()
    {
        int nextAvailableId = 0;

        for (; nextAvailableId < MAX_BOMBS; nextAvailableId ++)
        {
            if (mGlobalBombIds[nextAvailableId].equals("EMPTY"))
                break;
        }

        return(nextAvailableId);
    }

    private int createNextGlobalExplosionId()
    {
        int nextAvailableId = 0;

        for (; nextAvailableId < MAX_EXPLOSIONS; nextAvailableId ++)
        {
            if (mGlobalExplosionIds[nextAvailableId].equals("EMPTY"))
            {
                mGlobalExplosionIds[nextAvailableId] = "TAKEN";

                break;
            }
        }

        return(nextAvailableId);
    }

    public Block getBlockAtPosition(int inX, int inY)
    {
        if (!isValidPosition(inX, inY))
            return null;

        return mGridArray[inX][inY];
    }

    public synchronized boolean moveBot(int inBotId, int inCurrX, int inCurrY, Direction inDirection)
    {
        System.out.println("Platform.moveBot() - moving id: " + inBotId + " in direction: " + inDirection);

        MovableBlock bot = mBombers.get(inBotId);

        if (!bot.isAlive() || !bot.hasWaitedLongEnough())
            return(false);

        int newX = getXForDirection(inCurrX, inDirection);
        int newY = getYForDirection(inCurrY, inDirection);

        if (performMove(bot, inCurrX, inCurrY, newX, newY))
            mView.moveBot(inBotId, newX, newY);

        return true;
    }

    public List<Position> getAllBombPositions()
    {
        return mAllBombPositions;
    }
}
