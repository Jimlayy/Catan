package se.liu.vigsc133.structure;

import se.liu.vigsc133.player.Player;

import java.awt.*;
/**
 * Represents a city structure built by a player in the game.
 * <p>
 * A city generates 2 resources per production event and is drawn as a square on the board.
 */
public class City extends Settlement
{
    private static final int CITY_SIZE = 30;

    public City(Player owner, Point position) {
	super(owner, position);
    }

    @Override public int getGenerateAmount(){
	return 2;
    }

    @Override
    public void draw(Graphics2D g2d) {
	g2d.setColor(getOwner().getColor());
	g2d.fillRect(getPosition().x - CITY_SIZE/2, getPosition().y - CITY_SIZE/2, CITY_SIZE, CITY_SIZE);
    }
}
