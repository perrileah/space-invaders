package game.mvc.model;

import game.mvc.controller.SoundLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Bullet extends Sprite {



    public Bullet(LaserCannon laserCannon) {

        setTeam(Team.FRIEND);
        setColor(Color.GREEN);

        setExpiry(20);
        setRadius(6);


        //everything is relative to the laserCannon ship that fired the bullet
        // Position bullet at the top of the laser cannon
        Point cannonTop = new Point(laserCannon.getCenter().x, laserCannon.getCenter().y - laserCannon.getRadius());
        setCenter(cannonTop);

        //set the bullet orientation to fire straight up (270 degrees or -90 degrees)
        setOrientation(270); // or -90, both point upward

        final double FIRE_POWER = 35.0;
        // Fire straight up (negative Y direction)
        double vectorX = 0; // No horizontal movement
        double vectorY = -FIRE_POWER; // Negative Y moves upward

        //fire force: just the fire-vector (no cannon inertia for vertical shooting)
        setDeltaX(vectorX);
        setDeltaY(vectorY);

        //we have a reference to the laserCannon passed into the constructor. Let's create some kick-back.
        //fire kick-back on the laserCannon: inertia - fire-vector / some arbitrary divisor
        final double KICK_BACK_DIVISOR = 36.0;
        laserCannon.setDeltaX(laserCannon.getDeltaX() - vectorX / KICK_BACK_DIVISOR);
        laserCannon.setDeltaY(laserCannon.getDeltaY() - vectorY / KICK_BACK_DIVISOR);

        //define the points on a cartesian grid - simple line for space invaders
        List<Point> listPoints = new ArrayList<>();
        listPoints.add(new Point(0, 6)); //top of line
        listPoints.add(new Point(0, -6)); //bottom of line

        setCartesians(listPoints.toArray(new Point[0]));




    }


    @Override
    public void draw(Graphics g) {
           renderVector(g);
    }

    @Override
    public void addToGame(LinkedList<Movable> list) {
        super.addToGame(list);
        SoundLoader.playSound("shoot.wav");

    }
}
