package se.liu.vigsc133.board;

import se.liu.vigsc133.structure.VisibleGameObject;

import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * Represents a single hexagonal tile on the Catan board.
 * <p>
 * Each hexagon can have a tile type, a number token, and possibly the robber.
 * It is responsible for rendering itself and tracking its state.
 */
public class Hexagon implements VisibleGameObject
{
    private static final int HEX_SIDES = 6;
    private static final int NUM_TOKEN_SIZE = 30;
    private static final int NUM_TOKEN_OFFSET = NUM_TOKEN_SIZE / 2;
    private static final int FONT_SIZE = 16;
    private static final int TEXT_OFFSET = 10;
    /** 6 and 8 are the most likely rolls apart from 7, so they are shown in red as in the real game. */
    private static final Set<Integer> HOT_NUMBERS = Set.of(6, 8);
    private static final Font NUMBER_FONT = new Font("Arial", Font.BOLD, FONT_SIZE);

    private final HexType type;
    /** Center of the tile, in pixels. */
    private final Point pos;
    /** X coordinates of the corners, in drawing order. */
    private final int[] pointsX = new int[HEX_SIDES];
    /** Y coordinates of the corners, in drawing order. */
    private final int[] pointsY = new int[HEX_SIDES];
    private final Point[] cornerPoints = new Point[HEX_SIDES];
    private int numberToken;
    private boolean hasRobber = false;

    public Hexagon(Point pos, HexType type, int sideLength) {
        this.type = type;
        this.pos = pos;
        this.numberToken = 0;
        // Calculate and add all cornerpoints from given centerpoint
        for (int i = 0; i < HEX_SIDES; i++) {
            int pointX = (int) (getPos().x + sideLength * Math.sin(i * 2 * Math.PI / HEX_SIDES));
            int pointY = (int) (getPos().y + sideLength * Math.cos(i * 2 * Math.PI / HEX_SIDES));
            this.pointsX[i] = pointX;
            this.pointsY[i] = pointY;
            this.cornerPoints[i] = new Point(pointX, pointY);

        }

    }

    public void setNumberToken(int number) {
        numberToken = number;
    }

    public void setHasRobber(final boolean hasRobber) {
        this.hasRobber = hasRobber;
    }

    public int getNumberToken() {
        return numberToken;
    }

    public Point getPos() {
        return pos;
    }

    public HexType getType() {
        return type;
    }

    public List<Point> getCornerPoints() {
        return List.of(cornerPoints);
    }

    public boolean hasRobber() {
        return hasRobber;
    }

    @Override public void draw(final Graphics2D g2d) {
        g2d.setColor(type.getColor());
        g2d.fillPolygon(pointsX, pointsY, HEX_SIDES);

        g2d.setColor(hasRobber ? Color.DARK_GRAY : Color.WHITE);
        g2d.fillOval(pos.x - NUM_TOKEN_OFFSET, pos.y - NUM_TOKEN_OFFSET, NUM_TOKEN_SIZE, NUM_TOKEN_SIZE);

        if (!hasRobber && numberToken != 0) {
            g2d.setColor(HOT_NUMBERS.contains(numberToken) ? Color.RED : Color.BLACK);
            g2d.setFont(NUMBER_FONT);
            g2d.drawString(String.valueOf(numberToken), pos.x - TEXT_OFFSET, pos.y + TEXT_OFFSET);
        }
    }
}
