package se.liu.vigsc133.structure;

import se.liu.vigsc133.player.Player;

import java.awt.*;

/**
 * Represents a road segment built by a {@link Player} between two corner points on the
 * game board.
 * <p>
 * Roads are drawn as colored lines from {@code start} to {@code end}. Each road belongs
 * to a specific player and lets that player build further roads and villages from it.
 */
public class Road implements VisibleGameObject
{
    private static final int ROAD_WIDTH = 4;
    private static final BasicStroke ROAD_STROKE = new BasicStroke(ROAD_WIDTH);

    private final Player owner;
    private final Point start;
    private final Point end;

    public Road(Player owner, Point start, Point end) {
	this.owner = owner;
	this.start = start;
	this.end = end;
    }

    public Player getOwner() {
	return owner;
    }

    @Override public void draw(final Graphics2D g2d) {
	g2d.setColor(owner.getColor());
	g2d.setStroke(ROAD_STROKE);
	g2d.drawLine(start.x, start.y, end.x, end.y);
    }

    /**
     * @return whether the road starts or ends at the given point
     */
    public boolean hasEndpoint(Point point) {
	return start.equals(point) || end.equals(point);
    }

    /**
     * @return whether the road connects the two points, in either direction
     */
    public boolean connects(Point first, Point second) {
	return hasEndpoint(first) && hasEndpoint(second);
    }
}
