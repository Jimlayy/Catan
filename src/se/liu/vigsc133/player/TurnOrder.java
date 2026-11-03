package se.liu.vigsc133.player;

import java.util.List;
import java.util.Optional;

/**
 * Keeps track of the players in a game and whose turn it is.
 * <p>
 * Players take turns in the order they were given to the constructor; after the
 * last player, the turn goes back to the first. The list of players cannot be
 * changed once the game has started.
 */
public class TurnOrder
{
    private final List<Player> players;
    private int currentIndex = 0;

    /**
     * @param players the players in turn order; must not be empty
     */
    public TurnOrder(List<Player> players) {
	if (players.isEmpty()) {
	    throw new IllegalArgumentException("A game needs at least one player");
	}
	this.players = List.copyOf(players);
    }

    public Player getCurrentPlayer() {
	return players.get(currentIndex);
    }

    /**
     * Passes the turn to the next player.
     */
    public void advance() {
	currentIndex = (currentIndex + 1) % players.size();
    }

    /**
     * @return all players in turn order (an unmodifiable list)
     */
    public List<Player> getPlayers() {
	return players;
    }

    /**
     * @return the first player with at least the given number of points, if any
     */
    public Optional<Player> findPlayerWithPoints(int points) {
	return players.stream().filter(player -> player.getPoints() >= points).findFirst();
    }
}