package game.mvc.controller;

import game.mvc.model.*;
import game.mvc.view.GamePanel;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.List;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension DIM = new Dimension(900, 700); //the dimension of the game-screen.

    private final GamePanel gamePanel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 40; // milliseconds between frames

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;

    private boolean gameOverTriggered = false;

    // UFO spawning variables
    private int nextUFOSpawn = MysteryUFO.MIN_SPAWN_INTERVAL +
            Game.R.nextInt(MysteryUFO.MAX_SPAWN_INTERVAL - MysteryUFO.MIN_SPAWN_INTERVAL);

    //key-codes
    private static final int
            PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77; // m-key mute


    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this); //Game object implements KeyListener

        //fire up the animation thread
        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        //set as daemon so as not to block the main thread from exiting
        animationThread.setDaemon(true);
        animationThread.start();


    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {


        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }

    // Game implements runnable, and must have run method
    @Override
    public void run() {

        // lower animation thread's priority, thereby yielding to the 'Event Dispatch Thread' or EDT
        // thread which listens to keystrokes
        animationThread.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long startTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {

            // Update alien group movement BEFORE drawing
            updateAlienGroup();

            //this call will cause all movables to move() and draw() themselves every ~40ms
            // see GamePanel class for details
            gamePanel.update(gamePanel.getGraphics());

            checkUFOSpawn();

            checkCollisions();
            checkNewLevel();
//            checkFloaters();
            //this method will execute addToGame() and removeFromGame() callbacks on Movable objects
            processGameOpsQueue();
            //keep track of the frame for development purposes
            CommandCenter.getInstance().incrementFrame();

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
                // between frames takes longer than ANIMATION_DELAY, then the difference between startTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;

                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run

    private void updateAlienGroup() {
        List<Alien> aliens = new ArrayList<>();
        for (Movable mov : CommandCenter.getInstance().getMovFoes()) {
            if (mov instanceof Alien) {
                aliens.add((Alien) mov);
            }
        }
        Alien.updateGroup(aliens);
    }

    private void checkUFOSpawn() {
        // Only spawn UFO if game is not paused and player is alive
        if (!CommandCenter.getInstance().isPaused() && !CommandCenter.getInstance().isGameOver()) {
            nextUFOSpawn--;

            if (nextUFOSpawn <= 0) {
                // Check if there's already a UFO on screen
                boolean ufoExists = CommandCenter.getInstance().getMovFoes().stream()
                        .anyMatch(foe -> foe instanceof MysteryUFO);

                if (!ufoExists) {
                    CommandCenter.getInstance().getOpsQueue().enqueue(new MysteryUFO(), GameOp.Action.ADD);

                    // Set next spawn time (randomized)
                    nextUFOSpawn = MysteryUFO.MIN_SPAWN_INTERVAL +
                            Game.R.nextInt(MysteryUFO.MAX_SPAWN_INTERVAL - MysteryUFO.MIN_SPAWN_INTERVAL);
                }
            }
        }
    }

    /*
    TODO The following two methods are an example of the Command design pattern. This approach involves deferring
    mutations to collections (linked lists of Movables) while iterating over them, and then processing the mutations
    later (in the processGameOpsQueue() method below). The Command design pattern decouples the request for an
    operation from the  execution of the operation itself. We do this because mutating a data structure while iterating it
    is dangerous and may lead to null-pointer or array-index-out-of-bounds exceptions, or other erroneous behavior.
     */

    private void checkCollisions() {

        //This has order-of-growth of O(FOES * FRIENDS)
        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {

                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
                    //enqueue the friend
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                    //enqueue the foe
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                }
            }//end inner for
        }//end outer for

        //check for collisions between laserCannon and alien bullets
        LaserCannon laserCannon = CommandCenter.getInstance().getLaserCannon();
        Point pntLaserCannonCenter = CommandCenter.getInstance().getLaserCannon().getCenter();
        int radLaserCannon = laserCannon.getRadius();

        // Skip collision checks if laser cannon is waiting to respawn (off-screen)
        if (laserCannon.getRespawnTimer() > 0) {
            return;
        }

        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof AlienBullet) {
                Point pntBulletCenter = movFoe.getCenter();
                int radBullet = movFoe.getRadius();

                if (pntLaserCannonCenter.distance(pntBulletCenter) < (radLaserCannon + radBullet)) {
                    // Laser cannon hit by alien bullet - remove bullet and laserCannon
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                    CommandCenter.getInstance().getOpsQueue().enqueue(laserCannon, GameOp.Action.REMOVE);
                }
            }
        }//end for

        // Check for collisions between PLAYER bullets and bricks
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            // Only check bullets, not other FRIEND objects
            if (!(movFriend instanceof Bullet)) continue;

            Point pntBulletCenter = movFriend.getCenter();
            int radBullet = movFriend.getRadius();

            for (Movable movDebris : CommandCenter.getInstance().getMovDebris()) {
                // Only check bricks
                if (!(movDebris instanceof Brick)) continue;

                Point pntBrickCenter = movDebris.getCenter();
                int radBrick = movDebris.getRadius();

                // Detect collision
                if (pntBulletCenter.distance(pntBrickCenter) < (radBullet + radBrick)) {
                    // Remove  brick
                    CommandCenter.getInstance().getOpsQueue().enqueue(movDebris, GameOp.Action.REMOVE);
                    // Remove neighboring bricks within damage radius
                    final int DAMAGE_RADIUS = 15; // Adjust this to remove more/fewer bricks
                    for (Movable neighbor : CommandCenter.getInstance().getMovDebris()) {
                        if (neighbor instanceof Brick && neighbor != movDebris) {
                            double distance = pntBrickCenter.distance(neighbor.getCenter());
                            if (distance < DAMAGE_RADIUS) {
                                CommandCenter.getInstance().getOpsQueue().enqueue(neighbor, GameOp.Action.REMOVE);
                            }
                        }
                    }
                    // Remove bullet
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);

                    // Break inner loop since this bullet is destroyed
                    break;
                }
            }
        }

        // Check alien bullets against bricks
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            // Only check alien bullets
            if (!(movFoe instanceof AlienBullet)) continue;

            Point pntBulletCenter = movFoe.getCenter();
            int radBullet = movFoe.getRadius();

            for (Movable movDebris : CommandCenter.getInstance().getMovDebris()) {
                // Only check bricks
                if (!(movDebris instanceof Brick)) continue;

                Point pntBrickCenter = movDebris.getCenter();
                int radBrick = movDebris.getRadius();

                // Detect collision
                if (pntBulletCenter.distance(pntBrickCenter) < (radBullet + radBrick)) {
                    /// Remove  brick
                    CommandCenter.getInstance().getOpsQueue().enqueue(movDebris, GameOp.Action.REMOVE);
                    // Remove neighboring bricks within damage radius
                    final int DAMAGE_RADIUS = 15; // Adjust this to remove more/fewer bricks
                    for (Movable neighbor : CommandCenter.getInstance().getMovDebris()) {
                        if (neighbor instanceof Brick && neighbor != movDebris) {
                            double distance = pntBrickCenter.distance(neighbor.getCenter());
                            if (distance < DAMAGE_RADIUS) {
                                CommandCenter.getInstance().getOpsQueue().enqueue(neighbor, GameOp.Action.REMOVE);
                            }
                        }
                    }
                    // Remove bullet
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);

                    // Break inner loop since this bullet is destroyed
                    break;
                }
            }
        }

        // Check if any alien has reached the laser cannon level (game over condition)
        if (!gameOverTriggered) {
            int laserCannonY = CommandCenter.getInstance().getLaserCannon().getCenter().y;
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                if (movFoe instanceof Alien) {
                    if (movFoe.getCenter().y >= laserCannonY - 30) { // 30 pixel buffer
                        // Alien reached the bottom - Game Over!
                        gameOverTriggered = true;
                        CommandCenter.getInstance().setNumlaserCannons(0); // Triggers game over
                        SoundLoader.playSound("kapow.wav");
                        break;
                    }
                }
            }
        }



    }//end meth


    //This method adds and removes movables to/from their respective linked-lists.
    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {

            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();

            //given team, determine which linked-list this object will be added-to or removed-from
            LinkedList<Movable> list;
            Movable mov = gameOp.getMovable();
            switch (mov.getTeam()) {
                case FOE:
                    list = CommandCenter.getInstance().getMovFoes();
                    break;
                case FRIEND:
                    list = CommandCenter.getInstance().getMovFriends();
                    break;
                case FLOATER:
                    list = CommandCenter.getInstance().getMovFloaters();
                    break;
                case DEBRIS:
                default:
                    list = CommandCenter.getInstance().getMovDebris();
            }

            //pass the appropriate linked-list from above
            //this block will execute the addToGame() or removeFromGame() callbacks in the Movable models.
            GameOp.Action action = gameOp.getAction();
            if (action == GameOp.Action.ADD) {
                mov.addToGame(list);
            } else if (action == GameOp.Action.REMOVE) {
                mov.removeFromGame(list);
            }
        }//end while
    }


//    private void spawnShieldFloater() {
//
//        if (CommandCenter.getInstance().getFrame() % ShieldFloater.SPAWN_SHIELD_FLOATER == 0) {
//            CommandCenter.getInstance().getOpsQueue().enqueue(new ShieldFloater(), GameOp.Action.ADD);
//        }
//    }

//    private void spawnNukeFloater() {
//
//        if (CommandCenter.getInstance().getFrame() % NukeFloater.SPAWN_NUKE_FLOATER == 0) {
//            CommandCenter.getInstance().getOpsQueue().enqueue(new NukeFloater(), GameOp.Action.ADD);
//        }
//    }


//    //this method spawns new Large (0) Asteroids
//    private void spawnBigAsteroids(int num) {
//
//        while (num-- > 0) {
//            //Asteroids with size of zero are big
//            CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(0), GameOp.Action.ADD);
//
//        }
//    }

    

    private boolean isLevelClear() {
        //if there are no more Aliens on the screen
        boolean alienFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Alien) {
                alienFree = false;
                break;
            }
        }
        return alienFree;
    }

    private void checkNewLevel() {

        //short-circuit if level not yet cleared
        if (!isLevelClear()) return;

        //currentLevel will be zero at beginning of game
        int level = CommandCenter.getInstance().getLevel();
        //award some points for having cleared the previous level
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + (10_000L * level));

        //center the laserCannon at each level-clear
        CommandCenter.getInstance().getLaserCannon().setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));

        //Set universe according to mod of level - cycle through universes
        int ordinal = level % CommandCenter.Universe.values().length;
        CommandCenter.Universe key = CommandCenter.Universe.values()[ordinal];
        CommandCenter.getInstance().setUniverse(key);
        //players will need radar in the big universes, but they can still toggle it off
        CommandCenter.getInstance().setRadar(ordinal > 1);

        //bump the level up
        level = level + 1;
        CommandCenter.getInstance().setLevel(level);
       
        //spawn the alien formation
        spawnAlienFormation();

        // Spawn 4 bunkers
        spawnBunkers();

        //make laserCannon invincible momentarily in case new asteroids spawn on top of him, and give player
        //time to adjust to new universe and new asteroids in game space.
        CommandCenter.getInstance().getLaserCannon().setShield(LaserCannon.INITIAL_SPAWN_TIME);
        //show "Level: [X] UNIVERSE" in middle of screen
        CommandCenter.getInstance().getLaserCannon().setShowLevel(LaserCannon.INITIAL_SPAWN_TIME);


    }
    
    // Alien formation - 55 aliens
    private void spawnAlienFormation() {
        final int ROWS = 5;
        final int COLS = 11;
        final int ALIEN_SPACING_X = 60;
        final int ALIEN_SPACING_Y = 50;
        final int START_X = 150; // Left margin
        final int START_Y = 100; // Top margin

        // Reset alien group movement
        Alien.resetGroupMovement();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = START_X + (col * ALIEN_SPACING_X);
                int y = START_Y + (row * ALIEN_SPACING_Y);

                // Determine alien type based on your specific formation:
                // Row 0: Orange (40 points)
                // Rows 1-2: Lime (20 points) 
                // Rows 3-4: Pink (10 points)
                int alienType;
                if (row == 0) {
                    alienType = 3; // Orange - top row (40 points)
                } else if (row == 1 || row == 2) {
                    alienType = 2; // Lime - middle 2 rows (20 points)
                } else {
                    alienType = 1; // Pink - bottom 2 rows (10 points)
                }

                System.out.println("Spawning alien type " + alienType + " at (" + x + ", " + y + ")"); // Debug output
                CommandCenter.getInstance().getOpsQueue().enqueue(
                        new Alien(alienType, x, y), GameOp.Action.ADD);
            }
        }
    }

    private void spawnBunkers() {
        int bunkerY = Game.DIM.height - 200; // above player
        int screenWidth = Game.DIM.width;
        int spacing = screenWidth / 5; // Divide screen into 5 sections for 4 bunkers

        // Create 4 bunkers evenly spaced
        for (int i = 1; i <= 4; i++) {
            int x = spacing * i;
            Bunker bunker = new Bunker(x, bunkerY);
            bunker.addToGame();
        }
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        LaserCannon laserCannon = CommandCenter.getInstance().getLaserCannon();
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case LEFT:
                //Move left horizontally
                int newX = laserCannon.getCenter().x - 15; // Adjust speed as needed
                laserCannon.setCenter(new Point(newX, laserCannon.getCenter().y));
                break;
            case RIGHT:
                // Move right horizontally  
                int newXRight = laserCannon.getCenter().x + 15; // Adjust speed as needed
                laserCannon.setCenter(new Point(newXRight, laserCannon.getCenter().y));
                break;
            default:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        LaserCannon laserCannon = CommandCenter.getInstance().getLaserCannon();
        int keyCode = e.getKeyCode();
        //show the key-code in the console
        System.out.println(keyCode);

        if (keyCode == START && CommandCenter.getInstance().isGameOver()) {
            gameOverTriggered = false;
            CommandCenter.getInstance().initGame();
            return;
        }

        switch (keyCode) {
            case FIRE:
                CommandCenter.getInstance().getOpsQueue().enqueue(new Bullet(laserCannon), GameOp.Action.ADD);
                break;
            case PAUSE:
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                System.out.println("Game Paused");
                break;
            case QUIT:
                System.out.println("Thanks for playing!");
                System.exit(0);
                break;
            case MUTE:
                //if music is currently playing, then stop
                if (CommandCenter.getInstance().isThemeMusic()) {
                    SoundLoader.stopSound("spaceinvaders1.wav");
                    System.out.println("Music Muted");
                } else { //else not playing, then play
                    SoundLoader.playSound("spaceinvaders1.wav");
                    System.out.println("Music Playing");
                }
                //toggle the boolean switch
                CommandCenter.getInstance().setThemeMusic(!CommandCenter.getInstance().isThemeMusic());
                break;
            default:
                break;

        }

    }

    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }

}


