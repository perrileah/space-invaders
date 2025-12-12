package game.mvc.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Brick extends Sprite {

    public Brick(Point position, int size) {
        setTeam(Team.DEBRIS); // Neutral - can be hit by both player and aliens
        setCenter(position);
        setRadius(size / 2);
        setColor(Color.GREEN);

        // Square shape
        List<Point> points = new ArrayList<>();
        int half = size / 2;
        points.add(new Point(-half, -half)); // Top-left
        points.add(new Point(half, -half));  // Top-right
        points.add(new Point(half, half));   // Bottom-right
        points.add(new Point(-half, half));  // Bottom-left

        setCartesians(points.toArray(new Point[0]));
    }

    @Override
    public void draw(Graphics g) {
        renderVector(g);
    }

    @Override
    public void move() {
        // Bricks don't move
    }
}
