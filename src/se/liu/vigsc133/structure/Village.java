package se.liu.vigsc133.structure;

import se.liu.vigsc133.player.Player;

import java.awt.*;
/**
 * Represents a village structure built by a player in the game.
 * <p>
 * A village generates 1 resource per production event and is drawn as a circle on the board.
 */
public class Village extends Settlement
{
    public Village(Player owner, Point position) {
	super(owner, position);
    }

    @Override public boolean canBeUpgraded() {
	return true;
    }
}
