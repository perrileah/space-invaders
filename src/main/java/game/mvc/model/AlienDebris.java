package game.mvc.model;

import game.mvc.controller.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class AlienDebris extends Sprite {
    private static final int SHOW_TIME = 3; // frames to show explosion

    public AlienDebris(Alien destroyedAlien) {
        setTeam(Team.DEBRIS);

        // Get color based on alien type
        String color = getColorByType(destroyedAlien.getAlienType());

        // Load explosion image
        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(0, ImageLoader.getImage("imgs/exp/" + color + ".png"));

        setRasterMap(rasterMap);
        setExpiry(SHOW_TIME);

        // Copy position from destroyed alien
        setCenter((Point) destroyedAlien.getCenter().clone());
        setDeltaX(0);
        setDeltaY(0);
        setRadius((int) (destroyedAlien.getRadius() * 1.3));
    }

    private String getColorByType(int type) {
        switch(type) {
            case 1: return "pink";   // 10 points
            case 2: return "green";  // 20 points
            case 3: return "red"; // 40 points
            default: return "white";
        }
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage img = getRasterMap().get(0);
        if (img != null) {
            renderRaster((Graphics2D) g, img);
        }
    }

}
