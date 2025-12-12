package game.mvc.model;

import game.mvc.controller.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MysteryUFO extends Sprite {

    // UFO spawns every 20-30 seconds (randomized)
    public static final int MIN_SPAWN_INTERVAL = Game.FRAMES_PER_SECOND * 20;
    public static final int MAX_SPAWN_INTERVAL = Game.FRAMES_PER_SECOND * 30;

    // Points awarded for hitting UFO (can be randomized: 50, 100, 150, or 300)
    private static final int[] POINT_VALUES = {50, 100, 150, 300};
    private int pointValue;

    // Movement speed
    private static final int UFO_SPEED = 3;

    // Image state for UFO
    public enum ImageState {
        MYSTERY_UFO
    }

    public MysteryUFO() {
        setTeam(Team.FOE);
        setRadius(25);

        // Randomly choose point value
        pointValue = POINT_VALUES[Game.R.nextInt(POINT_VALUES.length)];

        // Position UFO at top of screen, randomly choose left or right entrance
        boolean fromLeft = Game.R.nextBoolean();
        if (fromLeft) {
            setCenter(new Point(-50, 50)); // Start off-screen left
            setDeltaX(UFO_SPEED); // Move right
        } else {
            setCenter(new Point(Game.DIM.width + 50, 50)); // Start off-screen right
            setDeltaX(-UFO_SPEED); // Move left
        }
        setDeltaY(0); // No vertical movement

        // Load UFO image
        Map<MysteryUFO.ImageState, BufferedImage> ufoImages = new HashMap<>();
        BufferedImage img = ImageLoader.getImage("imgs/aliens/mysteryUFO.png");
        System.out.println("Loaded MysteryUFO image: " + img); 
        ufoImages.put(ImageState.MYSTERY_UFO, img);
        setRasterMap(ufoImages);
        setColor(Color.RED); // Fallback color

        // Set expiry to remove UFO if it goes off-screen (about 10 seconds)
        setExpiry(Game.FRAMES_PER_SECOND * 10);
    }

    @Override
    public void move() {
        super.move();

        // Remove UFO when it goes off-screen
        if (getCenter().x < -100 || getCenter().x > Game.DIM.width + 100) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img = getRasterMap().get(ImageState.MYSTERY_UFO);

        if (img != null) {
            renderRaster((Graphics2D) g, img);
        } else {
            // Fallback: red rectangle
            Point c = getCenter();
            g.setColor(Color.RED);
            g.fillRect(c.x - 25, c.y - 10, 50, 20);
            g.setColor(Color.WHITE);
            g.drawString("UFO", c.x - 10, c.y);
        }
    }

    @Override
    public void addToGame(LinkedList<Movable> list) {
        super.addToGame(list);
        // Play UFO sound when it appears
        SoundLoader.playSound("ufo_lowpitch.wav");
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        super.removeFromGame(list);

        // Only award points if UFO was hit
        if (getExpiry() > 0) {
            // Spawn score display
            CommandCenter.getInstance().getOpsQueue().enqueue(new UFOScoreDebris(this), GameOp.Action.ADD);
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + pointValue);
            SoundLoader.playSound("ufo_highpitch.wav"); // Different sound when hit
        }
    }

    public int getPointValue() {
        return pointValue;
    }
}
