package se.liu.vigsc133.structure;

import se.liu.vigsc133.player.Player;

import java.awt.*;

/**
 * Abstract base class for buildings that a player places on a corner of the board:
 * {@link Village} and {@link City}.
 * <p>
 * A settlement belongs to a {@link Player} and has a fixed position. By default it
 * produces one resource when an adjacent tile's number is rolled, is drawn as a circle,
 * and cannot be upgraded; subclasses override {@link #getGenerateAmount()},
 * {@link #draw(Graphics2D)} and {@link #canBeUpgraded()} to change this.
 */
public abstract class Settlement implements VisibleGameObject
{
    private static final int VILLAGE_SIZE = 20;

    private final Player owner;
    private final Point position;

    protected Settlement(final Player owner, final Point position) {
	this.owner = owner;
	this.position = position;
    }

    public Player getOwner() {
	return owner;
    }

    public boolean canBeUpgraded() {
	return false;
    }

    @Override public void draw(final Graphics2D g2d) {
	g2d.setColor(getOwner().getColor());
	g2d.fillOval(getPosition().x - VILLAGE_SIZE / 2, getPosition().y - VILLAGE_SIZE / 2, VILLAGE_SIZE, VILLAGE_SIZE);
    }

    public Point getPosition() {
	return position;
    }

    public int getGenerateAmount() {
	return 1;
    }
}
