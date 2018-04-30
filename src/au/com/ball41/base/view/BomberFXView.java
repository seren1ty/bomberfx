package au.com.ball41.base.view;

import static javafx.application.Platform.runLater;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import au.com.ball41.base.model.Block.Type;
import au.com.ball41.base.model.Direction;
import au.com.ball41.base.model.Platform;
import au.com.ball41.base.model.PlatformInterface;

public class BomberFXView implements Initializable, ViewInterface
{
    // view elements
    @FXML
    public AnchorPane gamePane;

    @FXML
    public AnchorPane detailsPane;

    @FXML
    public Label player1Score;

    @FXML
    public Label player2Score;

    @FXML
    public Label player3Score;

    @FXML
    public Label player4Score;

    // instance variables
    private PlatformInterface platform;

    private int mNumOfActivePlayers = 2;
    private int mNumOfActiveBots = 2;
    private int mNumOfActivePickups = 20;
    private int mNumOfActiveSolidBricks = 25;
    private int mNumOfActiveBricks = 20; // 72;
    private int mNumOfActiveBombs = 22;
    private int mNumOfActiveExplosions = 121;

    private List<Circle> bombers = new LinkedList<Circle>();
    private List<Circle> bombs = new LinkedList<Circle>();
    private List<Polygon> explosions = new LinkedList<Polygon>();
    private List<Rectangle> solidBricks = new LinkedList<Rectangle>();
    private List<Rectangle> bricks = new LinkedList<Rectangle>();
    private List<ImageView> pickups = new LinkedList<ImageView>();
    private List<Label> scoreFields = new LinkedList<Label>();

    private Image pickupFlameImage;
    private Image pickupBombImage;

    public void initialize(URL location, ResourceBundle resources)
    {
        System.out.println("BomberFXView.initialize()");

        initialise();
    }

    private void initialise()
    {
        if (platform == null)
        {
            platform = new Platform(this, 11, 11, mNumOfActivePlayers, mNumOfActiveBots, mNumOfActiveBricks, 1, 2);

            setupScores(mNumOfActiveBots);
        }
        else
        {
            runLater(new Runnable() {
                public void run()
                {
                    gamePane.getChildren().clear();
                }
            });

            clearViewElements();

            platform.resetPlatform();
        }

        setupSolidBricks();
        setupBombers(mNumOfActiveBots);
        setupBombs();
        setupExplosions();
        setupDestructableBricks();
        setupPickups();
    }

    private void setupPickups()
    {
        pickupFlameImage = new Image("file:/C:/Users/morgan/Pickups/flame.PNG");
        pickupBombImage = new Image("file:/C:/Users/morgan/Pickups/bomb.PNG");

        for (int index = 0; index < mNumOfActivePickups; index++)
        {
            ImageView pickup = new ImageView();
            pickup.setId("pickup" + index);
            pickup.setTranslateX(-100);
            pickup.setTranslateY(-100);

            pickups.add(index, pickup);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(pickups);
            }
        });
    }

    private void clearViewElements()
    {
        bombers.clear();
        bombs.clear();
        explosions.clear();
        solidBricks.clear();
        bricks.clear();
        pickups.clear();
    }

    private void setupScores(int inNumOfActiveBots)
    {
        scoreFields.add(player1Score);
        scoreFields.add(player2Score);

        if (inNumOfActiveBots >= 1) ;
        scoreFields.add(player3Score);

        if (inNumOfActiveBots >= 2) ;
        scoreFields.add(player4Score);
    }

    private void setupBombers(int inNumOfActiveBots)
    {
        Color blue = new Color(0.0, 0.2, 1.0, 1.0);
        Color green = new Color(0.2, 0.8, 0.0, 1.0);

        Circle bomber1 = new Circle();
        bomber1.setId("bomber1");
        bomber1.setFill(blue);
        bomber1.setStroke(Color.BLACK);
        bomber1.setStrokeWidth(1.5);
        bomber1.setRadius(20.0);
        bomber1.setTranslateX(25);
        bomber1.setTranslateY(25);

        Circle bomber2 = new Circle();
        bomber2.setId("bomber2");
        bomber2.setFill(green);
        bomber2.setStroke(Color.BLACK);
        bomber2.setStrokeWidth(1.5);
        bomber2.setRadius(20.0);
        bomber2.setTranslateX(gamePane.getPrefWidth() - 25);
        bomber2.setTranslateY(gamePane.getPrefHeight() - 25);

        bombers.add(0, bomber1);
        bombers.add(1, bomber2);

        if (inNumOfActiveBots >= 1)
        {
            Color orange = new Color(1.0, 0.5, 0.0, 1.0);

            Circle bomber3 = new Circle();
            bomber3.setId("bomber3");
            bomber3.setFill(orange);
            bomber3.setStroke(Color.BLACK);
            bomber3.setStrokeWidth(1.5);
            bomber3.setRadius(20.0);
            bomber3.setTranslateX(gamePane.getPrefWidth() - 25);
            bomber3.setTranslateY(25);

            bombers.add(bomber3);
        }

        if (inNumOfActiveBots >= 2)
        {
            Color magenta = new Color(0.6, 0.0, 0.8, 1.0);

            Circle bomber4 = new Circle();
            bomber4.setId("bomber4");
            bomber4.setFill(magenta);
            bomber4.setStroke(Color.BLACK);
            bomber4.setStrokeWidth(1.5);
            bomber4.setRadius(20.0);
            bomber4.setTranslateX(25);
            bomber4.setTranslateY(gamePane.getPrefWidth() - 25);

            bombers.add(bomber4);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(bombers);
            }
        });
    }

    private void setupBombs()
    {
        for (int index = 0; index < mNumOfActiveBombs; index++)
        {
            Circle bomb = new Circle();
            bomb.setId("bomb" + index);
            bomb.setCenterX(25);
            bomb.setCenterY(25);
            bomb.setRadius(18);
            bomb.setFill(Color.DARKRED);
            bomb.setTranslateX(-100);
            bomb.setTranslateY(-100);

            bombs.add(index, bomb);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(bombs);
            }
        });
    }

    private void setupExplosions()
    {
        double[] points = new double[] { 30.0, 0.0, 30.0, 15.0, 35.0, 13.0, 33.0, 18.0, 50.0, 22.0, 35.0, 27.0, 38.0, 30.0, 34.0,
                32.0, 45.0, 45.0, 30.0, 37.0, 29.0, 42.0, 23.0, 36.0, 15.0, 50.0, 16.0, 36.0, 10.0, 37.0, 13.0, 27.0, 9.0, 25.0,
                11.0, 23.0, 0.0, 10.0, 17.0, 17.0, 18.0, 11.0, 22.0, 15.0 };

        List<Stop> stops = Arrays.asList(new Stop(0.0, Color.web("#FFFF00")), new Stop(1.0, Color.web("#FF0000")));
        RadialGradient explosionFill = new RadialGradient(0.0, 0.0, 24.0, 26.0, 19.0, false, CycleMethod.NO_CYCLE, stops);

        for (int index = 0; index < mNumOfActiveExplosions; index++)
        {
            Polygon explosion = new Polygon(points);
            explosion.setId("explosion" + index);
            explosion.setFill(explosionFill);
            explosion.setStroke(Color.BLACK);
            explosion.setStrokeWidth(0.6);
            explosion.setTranslateX(-200);
            explosion.setTranslateY(-200);

            explosions.add(index, explosion);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(explosions);
            }
        });
    }

    private void setupDestructableBricks()
    {
        for (int index = 0; index < mNumOfActiveBricks; index++)
        {
            Rectangle destructableBrick = new Rectangle();
            destructableBrick.setId("destructableBrick" + index);
            destructableBrick.setWidth(50.0);
            destructableBrick.setHeight(50.0);
            destructableBrick.setFill(new Color(0.4, 0.2, 0.0, 1.0));
            destructableBrick.setStroke(Color.BLACK);
            destructableBrick.setTranslateX(platform.getFixedBlockPosX(index));
            destructableBrick.setTranslateY(platform.getFixedBlockPosY(index));

            bricks.add(index, destructableBrick);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(bricks);
            }
        });
    }

    private void setupSolidBricks()
    {
        for (int index = 0; index < mNumOfActiveSolidBricks; index++)
        {
            Rectangle solidBrick = new Rectangle();
            solidBrick.setId("solidBrick" + index);
            solidBrick.setWidth(50.0);
            solidBrick.setHeight(50.0);
            solidBrick.setStroke(Color.BLACK);
            solidBrick.setTranslateX(platform.getFixedSolidBlockPosX(index));
            solidBrick.setTranslateY(platform.getFixedSolidBlockPosY(index));

            solidBricks.add(index, solidBrick);
        }

        runLater(new Runnable() {
            public void run()
            {
                gamePane.getChildren().addAll(solidBricks);
            }
        });
    }

    @FXML
    private void newGameEvent(MouseEvent event)
    {
        initialise();
    }

    @FXML
    private void playerEvent(KeyEvent event)
    {

        // Player 1 controls
        if (event.getCode() == KeyCode.W && platform.move(0, Direction.UP))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(0).setTranslateY(bombers.get(0).getTranslateY() - 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.D && platform.move(0, Direction.RIGHT))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(0).setTranslateX(bombers.get(0).getTranslateX() + 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.S && platform.move(0, Direction.DOWN))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(0).setTranslateY(bombers.get(0).getTranslateY() + 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.A && platform.move(0, Direction.LEFT))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(0).setTranslateX(bombers.get(0).getTranslateX() - 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.TAB)
        {
            final int bombIdNum = platform.dropBomb(0);

            showBomb(0, bombIdNum);
        }
        // Player 2 controls
        else if (event.getCode() == KeyCode.UP && platform.move(1, Direction.UP))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(1).setTranslateY(bombers.get(1).getTranslateY() - 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.RIGHT && platform.move(1, Direction.RIGHT))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(1).setTranslateX(bombers.get(1).getTranslateX() + 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.DOWN && platform.move(1, Direction.DOWN))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(1).setTranslateY(bombers.get(1).getTranslateY() + 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.LEFT && platform.move(1, Direction.LEFT))
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombers.get(1).setTranslateX(bombers.get(1).getTranslateX() - 50);
                }
            });
        }
        else if (event.getCode() == KeyCode.ENTER)
        {
            final int bombIdNum = platform.dropBomb(1);

            showBomb(1, bombIdNum);
        }
    }

    public void showBomb(final int inBomberIdNum, final int inBombIdNum)
    {
        if (inBombIdNum > -1)
        {
            runLater(new Runnable() {
                public void run()
                {
                    bombs.get(inBombIdNum).setTranslateX(platform.getBombPosX(inBomberIdNum, inBombIdNum) * 50);
                    bombs.get(inBombIdNum).setTranslateY(platform.getBombPosY(inBomberIdNum, inBombIdNum) * 50);
                }
            });
        }
    }

    public void showPickup(final int inPickupIdNum, final Type inPickupType, final int inPosX, final int inPosY)
    {
        runLater(new Runnable() {
            public void run()
            {
                ImageView pickup = pickups.get(inPickupIdNum);

                if (inPickupType == Type.PICKUP_FLAME)
                    pickup.setImage(pickupFlameImage);
                else
                    pickup.setImage(pickupBombImage);

                pickup.setTranslateX(inPosX * 50);
                pickup.setTranslateY(inPosY * 50);
            }
        });
    }

    public void hidePickup(final int inPickupIdNum)
    {
        runLater(new Runnable() {
            public void run()
            {
                pickups.get(inPickupIdNum).setTranslateX(-200 * (inPickupIdNum + 1));
                pickups.get(inPickupIdNum).setTranslateY(-200 * (inPickupIdNum + 1));
            }
        });
    }

    public void explodeBomb(final int inBombIdNum)
    {
        runLater(new Runnable() {
            public void run()
            {
                bombs.get(inBombIdNum).setTranslateX(-200 * (inBombIdNum + 1));
                bombs.get(inBombIdNum).setTranslateY(-200 * (inBombIdNum + 1));
            }
        });
    }

    public void showExplosion(final int inExplosionIdNum, final int inPosX, final int inPosY)
    {
        runLater(new Runnable() {
            public void run()
            {
                explosions.get(inExplosionIdNum).setTranslateX(inPosX * 50);
                explosions.get(inExplosionIdNum).setTranslateY(inPosY * 50);
            }
        });
    }

    public void hideExplosion(final int inExplosionIdNum)
    {
        runLater(new Runnable() {
            public void run()
            {
                explosions.get(inExplosionIdNum).setTranslateX(-200 * (inExplosionIdNum + 1));
                explosions.get(inExplosionIdNum).setTranslateY(-200 * (inExplosionIdNum + 1));
            }
        });
    }

    public void destroyBomber(final int inBomberIdNum)
    {
        runLater(new Runnable() {
            public void run()
            {
                bombers.get(inBomberIdNum).setTranslateX(-200 * (inBomberIdNum + 1));
                bombers.get(inBomberIdNum).setTranslateY(-200 * (inBomberIdNum + 1));
            }
        });
    }

    public void destroyBlock(final int inBlockIdNum)
    {
        runLater(new Runnable() {
            public void run()
            {
                bricks.get(inBlockIdNum).setTranslateX(-200 * (inBlockIdNum + 1));
                bricks.get(inBlockIdNum).setTranslateY(-200 * (inBlockIdNum + 1));
            }
        });
    }

    public void updateScore(final int inBomberIdNum, final int inScore)
    {
        runLater(new Runnable() {
            public void run()
            {
                scoreFields.get(inBomberIdNum).setText(String.valueOf(inScore));
            }
        });
    }

    public void moveBot(final int inBotIdNum, final int inPosX, final int inPosY)
    {
        runLater(new Runnable() {
            public void run()
            {
                bombers.get(inBotIdNum).setTranslateX(inPosX * 50 + 25);
                bombers.get(inBotIdNum).setTranslateY(inPosY * 50 + 25);
            }
        });
    }
}
