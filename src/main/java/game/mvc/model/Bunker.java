package game.mvc.model;

import game.mvc.controller.CommandCenter;
import game.mvc.controller.GameOp;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bunker {

    private static final int BRICK_SIZE = 5; // Size of each brick
    private List<Brick> bricks;

    /**
     * Creates a bunker at the specified position - manages bricks
     */
    public Bunker(int centerX, int centerY) {
        bricks = new ArrayList<>();
        createBunkerShape(centerX, centerY);
    }

    private void createBunkerShape(int centerX, int centerY) {
        // Roughly 21x6 grid pattern

        // Define bunker shape as 2D array (1 = brick, 0 = empty)
        int[][] pattern = {
                {0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
                {0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0},
                {0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1},
                {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1}
        };

        int rows = pattern.length;
        int cols = pattern[0].length;

        // Calculate starting position (top-left corner)
        int startX = centerX - (cols * BRICK_SIZE) / 2;
        int startY = centerY;

        // Create bricks based on pattern
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (pattern[row][col] == 1) {
                    int x = startX + (col * BRICK_SIZE);
                    int y = startY + (row * BRICK_SIZE);
                    Brick brick = new Brick(new Point(x, y), BRICK_SIZE);
                    brick.setColor(Color.GREEN);
                    bricks.add(brick);
                }
            }
        }
    }

    /**
     * Adds all bricks of this bunker to the game
     */
    public void addToGame() {
        for (Brick brick : bricks) {
            CommandCenter.getInstance().getOpsQueue().enqueue(brick, GameOp.Action.ADD);
        }
    }

    /**
     * Returns all bricks in this bunker
     */
    public List<Brick> getBricks() {
        return bricks;
    }
}
