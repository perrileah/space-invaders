package game.mvc.model;

import java.awt.*;

public class UFOScoreDebris extends Sprite {
    private static final int SHOW_TIME = 60; // frames to show score (1.5 seconds)
    private final int pointValue;

    public UFOScoreDebris(MysteryUFO destroyedUFO) {
        setTeam(Team.DEBRIS);

        // Copy position from destroyed UFO
        setCenter((Point) destroyedUFO.getCenter().clone());
        setDeltaX(0);
        setDeltaY(0);
        setRadius(30);

        // Store the point value to display
        pointValue = destroyedUFO.getPointValue();

        setExpiry(SHOW_TIME);
        setColor(Color.RED); // Match UFO color
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(getColor());
        g.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Draw the point value centered at the UFO's position
        String scoreText = String.valueOf(pointValue);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);

        g.drawString(scoreText,
                getCenter().x - textWidth / 2,
                getCenter().y + fm.getAscent() / 2);
    }

    @Override
    public void move() {
        // Score display doesn't move, but parent handles expiry
        super.move();
    }
}
