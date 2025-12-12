package game.mvc.controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/*
Place all .png image assets in src/main/resources/imgs or its subdirectories.
All raster images are loaded from the classpath.
 */
public class ImageLoader {

    private static final Map<String, BufferedImage> IMAGE_MAP = new HashMap<>();

    static {
        // List all image paths you want to load relative to classpath
        String[] imagesToLoad = {
                "imgs/laser/laserCannon.png",
                "imgs/aliens/10PointAlien1.png",
                "imgs/aliens/10PointAlien2.png",
                "imgs/aliens/20PointAlien1.png",
                "imgs/aliens/20PointAlien2.png",
                "imgs/aliens/40PointAlien1.png",
                "imgs/aliens/40PointAlien2.png",
                "imgs/aliens/mysteryUFO.png",
                "imgs/exp/red.png",
                "imgs/exp/green.png",
                "imgs/exp/pink.png",
                "imgs/exp/row-1-column-1.png",
                "imgs/exp/row-1-column-2.png",
                "imgs/exp/row-1-column-3.png",
                "imgs/exp/row-2-column-1.png",
                "imgs/exp/row-2-column-2.png",
                "imgs/exp/row-2-column-3.png",
                "imgs/exp/row-3-column-1.png",
                "imgs/exp/row-3-column-2.png",
                "imgs/exp/row-3-column-3.png"
        };

        for (String path : imagesToLoad) {
            try (InputStream stream = ImageLoader.class.getClassLoader().getResourceAsStream(path)) {
                if (stream == null) {
                    System.err.println("Image not found in resources: " + path);
                    continue;
                }
                BufferedImage img = ImageIO.read(stream);
                IMAGE_MAP.put(path.toLowerCase(), img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Fetch image from the map
    public static BufferedImage getImage(String imagePath) {
        BufferedImage img = IMAGE_MAP.get(imagePath.toLowerCase());
        if (img == null) {
            System.err.println("Loaded image is null for path: " + imagePath);
        }
        return img;
    }
}

