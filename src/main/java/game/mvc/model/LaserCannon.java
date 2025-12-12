package game.mvc.model;

import game.mvc.controller.*;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Data
public class LaserCannon extends Sprite {

    // ==============================================================
    // FIELDS
    // ==============================================================

    //static fields

    //number of degrees the laserCannon will turn at each animation cycle if the turnState is LEFT or RIGHT
    public final static int TURN_STEP = 11;
    //number of frames that the laserCannon will be protected after a spawn
    public static final int INITIAL_SPAWN_TIME = 48;
    //number of frames laserCannon will be protected after consuming a NewShieldFloater
    public static final int MAX_SHIELD = 200;
    public static final int MAX_NUKE = 600;
    public static final int MIN_RADIUS = 28;
    private int respawnTimer = 0;
    private static final int RESPAWN_DELAY = 60; // 1.5 seconds
    private boolean isInvulnerable = false;

    //images states
    public enum ImageState {
        laserCannon, //normal ship
    }


    //instance fields (getters/setters provided by Lombok @Data above)
    private int shield;

    private int nukeMeter;
    private boolean maxSpeedAttained;

    //showLevel is not germane to the laserCannon. Rather, it controls whether the level is shown in the middle of the
    // screen. However, given that the laserCannon reference is never null, and that a laserCannon is a Movable whose move/draw
    // methods are being called every ~40ms, this is a very convenient place to store this variable.
    private int showLevel;

    /* TODO The enum TurnState as well as the boolean thrusting are examples of the State design pattern. This pattern
    allows an object to change its behavior when its internal state changes. In this case, the boolean thrusting, and
     the TurnState (with values IDLE, LEFT, and RIGHT) affects how the laserCannon moves and draws itself. */
    public enum TurnState {IDLE, LEFT, RIGHT}
    private TurnState turnState = TurnState.IDLE;

    private boolean thrusting;

    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public LaserCannon() {

        setTeam(Team.FRIEND);

        setRadius(MIN_RADIUS);

        // Position at bottom center initially
        setCenter(new Point(Game.DIM.width / 2, Game.DIM.height - 50));
        setOrientation(0); // Always face up


        //We use HashMap which has a seek-time of O(1)
        //See the resources directory in the root of this project for pngs.
        //Using enums as keys is safer b/c we know the value exists when we reference the consts later in code.
        Map<ImageState, BufferedImage> map = new HashMap<>();
        BufferedImage img = ImageLoader.getImage("imgs/laser/laserCannon.png"); // no leading slash
        System.out.println("Loaded image: " + img);  // should now not be null
        map.put(ImageState.laserCannon, img);
        setRasterMap(map);
    }


    // ==============================================================
    // METHODS
    // ==============================================================
    @Override
    public void move() {
        //The laserCannon is a convenient place to decrement the showLevel variable as the laserCannon
        //move() method is being called every frame (~40ms); and the laserCannon reference is never null.
        if (showLevel > 0) showLevel--;

        // Handle respawn timer
        if (respawnTimer > 0) {
            respawnTimer--;
            if (respawnTimer == 0) {
                // Timer finished, respawn now
                respawnLaserCannon();
            }
            return; // Don't process normal movement while waiting to respawn
        }

        // Keep LaserCannon at bottom of screen
        setCenter(new Point(getCenter().x, Game.DIM.height - 50)); // Fixed Y position near bottom

        // Keep LaserCannon within horizontal bounds
        if (getCenter().x < MIN_RADIUS) {
        setCenter(new Point(MIN_RADIUS, getCenter().y));
        } else if (getCenter().x > Game.DIM.width - MIN_RADIUS) {
        setCenter(new Point(Game.DIM.width - MIN_RADIUS, getCenter().y));
        }

    }

    //Since the superclass Spite does not provide an
    // implementation for draw() (contract method from Movable) ,we inherit that contract debt, and therefore must
    // provide an implementation. This is a raster and vector (see drawShield below) implementation of draw().
    @Override
    public void draw(Graphics g) {
        BufferedImage img = getRasterMap().get(ImageState.laserCannon);

        if (img != null) {
            renderRaster((Graphics2D) g, img);
        } else {
            // fallback: yellow box for debugging
            Point c = getCenter();
            g.setColor(Color.YELLOW);
            g.fillRect(c.x - 25, c.y - 25, 50, 50);
            g.setColor(Color.RED);
            g.drawString("IMG NULL", c.x, c.y - 20);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        //The laserCannon is never actually removed from the game-space; instead we decrement numlaserCannons
        // Prevent multiple hits in same frame
        if (isInvulnerable) {
            return;
        }

        isInvulnerable = true; // Mark as hit
        SoundLoader.playSound("kapow.wav");
        // Spawn debris at current position before respawning
        CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(this), GameOp.Action.ADD);

        // Decrement lives and check if game over
        CommandCenter.getInstance().setNumlaserCannons(CommandCenter.getInstance().getNumlaserCannons() - 1);

        // If game is over, don't respawn
        if (CommandCenter.getInstance().isGameOver()) {
            return;
        }

        // Hide the laser cannon off-screen and start respawn timer
        setCenter(new Point(-1000, -1000)); // Move off-screen
        respawnTimer = RESPAWN_DELAY;
    }


    public void respawnLaserCannon(){
        SoundLoader.playSound("shipspawn.wav");
    
        // Position at bottom center of screen
        setCenter(new Point(Game.DIM.width / 2, Game.DIM.height - 50));
        setOrientation(0); // Always face up
        setDeltaX(0);
        setDeltaY(0);
        setRadius(LaserCannon.MIN_RADIUS);
        respawnTimer = 0;
        isInvulnerable = false;
    }

} //end class
