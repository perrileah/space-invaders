package game.mvc.model;

import game.mvc.controller.CommandCenter;
import game.mvc.controller.Game;
import game.mvc.controller.GameOp;

import java.awt.*;

public class AlienBullet extends Sprite {

    public AlienBullet(Alien alien) {
        setTeam(Team.FOE);

        // Random colors for bullets
        Color[] bulletColors = {Color.PINK, Color.GREEN, Color.RED};
        setColor(bulletColors[Game.R.nextInt(bulletColors.length)]);

        setExpiry(200);
        setRadius(8);

        // Start at alien's position
        setCenter(new Point(alien.getCenter().x, alien.getCenter().y));

        // Fall straight down
        setDeltaX(0);
        setDeltaY(8);

        // Squiggly shape points
        setCartesians(new Point[]{
                new Point(8, 0),
                new Point(5, -3),
                new Point(3, 2),
                new Point(0, -2),
                new Point(-3, 3),
                new Point(-5, -2),
                new Point(-8, 0)
        });
    }

    @Override
    public void draw(Graphics g) {
        renderVector(g);
    }

    @Override
    public void move() {
        super.move();

        // Remove only if off screen
        if (getCenter().y > Game.DIM.height + 20) {
            CommandCenter.getInstance().getOpsQueue().enqueue(this, GameOp.Action.REMOVE);
        }
    }
}
