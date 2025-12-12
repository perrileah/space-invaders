package game.mvc.model;

import game.mvc.controller.CommandCenter;
import game.mvc.controller.Game;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;

//Sprite has a lot of bloat that we don't need to simply render a star field.
//This class demonstrates how we can use the Movable interface without extending Sprite.
@Data
public class Star implements Movable{

    private Point center;
    private Color color;

    public Star() {
        //center is some random point in the game space
        center = new Point(Game.R.nextInt(Game.DIM.width), Game.R.nextInt(Game.DIM.height));
        int bright = Game.R.nextInt(226); //Stars are muted at max brightness of 225 out of 255
        color = new Color(bright, bright, bright); //some grey value
    }

    //The following methods are contract methods from Movable. We need all of them to satisfy the contract.
    @Override
    public void draw(Graphics g) {

        g.setColor(color);
        g.drawOval(center.x, center.y, getRadius(), getRadius());

    }

    @Override
    public Point getCenter() {
        return center;
    }

    @Override
    public int getRadius() {
        return 1;
    }

    @Override
    public Team getTeam() {
        return Team.DEBRIS;
    }




    @Override
    public void move() {

        //if laserCannon position is NOT fixed (e.g. FREE_FLY), return
        if (!CommandCenter.getInstance().islaserCannonPositionFixed()) return;

        //else, laserCannon position is fixed, and the stars must move to orient player in laserCannon-fixed-play

            //right-bounds reached
        if (center.x > Game.DIM.width) {
            center.x = 1;
         //left-bounds reached
        } else if (center.x < 0) {
            center.x = Game.DIM.width - 1;
            //bottom-bounds reached
        } else if (center.y > Game.DIM.height) {
            center.y = 1;
            //top-bounds reached
        } else if (center.y < 0) {
            center.y = Game.DIM.height - 1;
            //in-bounds
        } else {
            //move star in opposite direction of laserCannon.
            center.x = (int) Math.round(center.x - CommandCenter.getInstance().getLaserCannon().getDeltaX());
            center.y = (int) Math.round(center.y - CommandCenter.getInstance().getLaserCannon().getDeltaY());
        }



    }


    @Override
    public void addToGame(LinkedList<Movable> list) {
        list.add(this);
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
       list.remove(this);
    }



}
