package se.liu.vigsc133.ui;

import se.liu.vigsc133.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Asks the user, through a series of dialogs, how many players will take part
 * in the game and what each player is called.
 * <p>
 * Each player is automatically assigned a unique color. The resulting list of
 * {@link Player} objects is meant to be passed to {@link se.liu.vigsc133.board.Board}
 * when a new game is created. If the user cancels any of the dialogs, an empty
 * list is returned so that the caller can choose not to start the game.
 */
public final class PlayerSetup
{
    private static final int MIN_PLAYERS = 2;
    /** One color per player, so the number of colors is also the maximum number of players. */
    private static final List<Color> PLAYER_COLORS = List.of(Color.RED, Color.BLUE, Color.ORANGE, Color.MAGENTA);

    private PlayerSetup() {}
    /**
     * Asks the user for the number of players and their names.
     *
     * @return the players in turn order, or an empty list if the user cancelled
     */
    public static List<Player> askForPlayers() {
	// Integer[] rather than int[]: JOptionPane needs an array of objects to choose from
	Integer[] countOptions = IntStream.rangeClosed(MIN_PLAYERS, PLAYER_COLORS.size())
		.boxed().toArray(Integer[]::new);
	// Integer rather than int: the dialog returns null when the user cancels
	Integer playerCount = (Integer) JOptionPane.showInputDialog(
		null, "How many players?", "New game", JOptionPane.QUESTION_MESSAGE,
		null, countOptions, countOptions[0]);
	if (playerCount == null) {
	    return List.of();
	}

	List<Player> players = new ArrayList<>();
	for (int i = 0; i < playerCount; i++) {
	    String name = askForName(i + 1);
	    if (name == null) {
		return List.of();
	    }
	    players.add(new Player(name, PLAYER_COLORS.get(i)));
	}
	return players;
    }

    /**
     * Asks for a player's name until a non-empty name is given.
     *
     * @return the name, or null if the user cancelled
     */
    private static String askForName(int playerNumber) {
	while (true) {
	    String name = JOptionPane.showInputDialog(null, "Name of player " + playerNumber + ":");
	    if (name == null) {
		return null;
	    }
	    if (!name.isBlank()) {
		return name.trim();
	    }
	    JOptionPane.showMessageDialog(null, "The name cannot be empty.");
	}
    }
}