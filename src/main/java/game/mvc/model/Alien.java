package game.mvc.model;

import game.mvc.controller.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class Alien extends Sprite {

    private int alienType; // 10, 20, or 40 point aliens
    private static int groupDirection = 1; // 1 for right, -1 for left
    private static boolean shouldDropDown = false;
    private static int globalAnimationFrame = 0; // Shared animation counter
    private static final int ANIMATION_SPEED = 30; // Frames between animation changes
    private static int shootDelay = 0; // frames since last shot
    private static final int MIN_SHOOT_INTERVAL = 20; // lower number, more frequent shots

    // Image states for different alien types and animation frames
    public enum ImageState {
        ALIEN_10_POINT_1,    // 10 point alien frame 1
        ALIEN_10_POINT_2,    // 10 point alien frame 2
        ALIEN_20_POINT_1,    // 20 point alien frame 1
        ALIEN_20_POINT_2,    // 20 point alien frame 2
        ALIEN_40_POINT_1,    // 40 point alien frame 1
        ALIEN_40_POINT_2     // 40 point alien frame 2
    }

    public Alien(int type, int x, int y) {
        setTeam(Team.FOE);
        setRadius(20);
        alienType = type;

        // Position the alien
        setCenter(new Point(x, y));

        // Load appropriate alien images based on point value
        Map<ImageState, BufferedImage> alienImages = new HashMap<>();

        switch(type) {
            case 1: // 10 point aliens
                alienImages.put(ImageState.ALIEN_10_POINT_1, ImageLoader.getImage("imgs/aliens/10PointAlien1.png"));
                alienImages.put(ImageState.ALIEN_10_POINT_2, ImageLoader.getImage("imgs/aliens/10PointAlien2.png"));
                setColor(Color.PINK); // fallback color
                break;
            case 2: // 20 point aliens
                alienImages.put(ImageState.ALIEN_20_POINT_1, ImageLoader.getImage("imgs/aliens/20PointAlien1.png"));
                alienImages.put(ImageState.ALIEN_20_POINT_2, ImageLoader.getImage("imgs/aliens/20PointAlien2.png"));
                setColor(Color.GREEN); // fallback color (green)
                break;
            case 3: // 40 point aliens
                alienImages.put(ImageState.ALIEN_40_POINT_1, ImageLoader.getImage("imgs/aliens/40PointAlien1.png"));
                alienImages.put(ImageState.ALIEN_40_POINT_2, ImageLoader.getImage("imgs/aliens/40PointAlien2.png"));
                setColor(Color.ORANGE); // fallback color
                break;
        }

        setRasterMap(alienImages);

        // Aliens move as a group
        setDeltaX(0);
        setDeltaY(0);
    }

    // Static method to update all aliens as a group
    public static void updateGroup(List<Alien> aliens) {
        if (aliens.isEmpty()) return;

        // Increment animation frame once per update
        globalAnimationFrame++;

        // Decrement shoot delay
        if (shootDelay > 0) {
            shootDelay--;
        }

        // Random alien shoots
        if (shootDelay == 0 && Game.R.nextInt(100) < 3) { // 3% chance per frame
            // Any alien can shoot
            Alien shooter = aliens.get(Game.R.nextInt(aliens.size()));
            CommandCenter.getInstance().getOpsQueue().enqueue(
                    new AlienBullet(shooter), GameOp.Action.ADD);
            shootDelay = MIN_SHOOT_INTERVAL;
        }

        final int MOVE_SPEED = 2;
        final int DROP_DISTANCE = 20;

        // Check if any alien has hit the edge
        boolean edgeHit = false;
        for (Alien alien : aliens) {
            int x = alien.getCenter().x;
            if ((groupDirection > 0 && x >= Game.DIM.width - 30) || 
                (groupDirection < 0 && x <= 30)) {
                edgeHit = true;
                break;
            }
        }

        // If edge hit, reverse direction and flag for drop
        if (edgeHit) {
            groupDirection *= -1;
            shouldDropDown = true;
        }

        // Move all aliens
        for (Alien alien : aliens) {
            Point c = alien.getCenter();
            
            if (shouldDropDown) {
                // Drop down
                alien.setCenter(new Point(c.x, c.y + DROP_DISTANCE));
            } else {
                // Move horizontally
                alien.setCenter(new Point(c.x + groupDirection * MOVE_SPEED, c.y));
            }
        }

        // Reset drop flag after all aliens have dropped
        if (shouldDropDown) {
            shouldDropDown = false;
        }
    }

    @Override
    public void move() {
        // movement handled by updateGroup()
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img = null;

        // Determine which animation frame to show (flips every ANIMATION_SPEED frames)
        boolean useFrame2 = (globalAnimationFrame / ANIMATION_SPEED) % 2 == 1;

        // Get the appropriate image based on alien type and animation frame
        switch(alienType) {
            case 1: // 10 point aliens
                img = useFrame2 ?
                        getRasterMap().get(ImageState.ALIEN_10_POINT_2) :
                        getRasterMap().get(ImageState.ALIEN_10_POINT_1);
                break;
            case 2: // 20 point aliens
                img = useFrame2 ?
                        getRasterMap().get(ImageState.ALIEN_20_POINT_2) :
                        getRasterMap().get(ImageState.ALIEN_20_POINT_1);
                break;
            case 3: // 40 point aliens
                img = useFrame2 ?
                        getRasterMap().get(ImageState.ALIEN_40_POINT_2) :
                        getRasterMap().get(ImageState.ALIEN_40_POINT_1);
                break;
        }

        if (img != null) {
            renderRaster((Graphics2D) g, img);
        } else {
            // Fallback: draw colored rectangles if images fail to load
            Point c = getCenter();
            switch(alienType) {
                case 1: g.setColor(Color.PINK); break;   // 10 points
                case 2: g.setColor(Color.GREEN); break;  // 20 points
                case 3: g.setColor(Color.ORANGE); break; // 40 points
            }
            g.fillRect(c.x - 15, c.y - 10, 30, 20);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        super.removeFromGame(list);

        // Spawn colored debris when alien is destroyed
        CommandCenter.getInstance().getOpsQueue().enqueue(new AlienDebris(this), GameOp.Action.ADD);

        // Award points based on alien type
        long points = 0;
        switch(alienType) {
            case 1: points = 10; break;  // Pink
            case 2: points = 20; break;  // Green
            case 3: points = 40; break;  // Orange
        }
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + points);

        // Play sound effect
        SoundLoader.playSound("invaderkilled.wav");
    }

    public int getAlienType() {
        return alienType;
    }

    public static void resetGroupMovement() {
        groupDirection = 1;
        shouldDropDown = false;
        globalAnimationFrame = 0; // Reset animation when starting new level
        shootDelay = 0;
    }
}
