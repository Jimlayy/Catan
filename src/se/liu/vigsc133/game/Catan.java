package se.liu.vigsc133.game;

import se.liu.vigsc133.ui.CatanViewer;
import se.liu.vigsc133.board.Board;
import se.liu.vigsc133.player.Player;
import se.liu.vigsc133.ui.PlayerSetup;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.List;

/**
 * Entry point for the Catan game application.
 * <p>
 * Starting the program sets up logging to the file {@value #LOG_FILE}, asks the user
 * for the players through {@link PlayerSetup}, creates a {@link Board} with those
 * players and opens the game window through {@link CatanViewer}. If the user cancels
 * the player setup, the program exits without starting a game.
 *
 * @see Board
 * @see CatanViewer
 * @see PlayerSetup
 */
public class Catan
{
    private static final Logger LOGGER = Logger.getLogger(Catan.class.getName());
    private static final String LOG_FILE = "catan.log";
    private static final int BOARD_WIDTH = 1000;
    private static final int BOARD_HEIGHT = 800;

    /**
     * The main method used to start the Catan game.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
	setUpLogging();
	SwingUtilities.invokeLater(Catan::startGame);
    }

    private static void startGame() {
	List<Player> players = PlayerSetup.askForPlayers();
	if (players.isEmpty()) {
	    LOGGER.info("Spelet avbröts vid val av spelare");
	    return;
	}
	LOGGER.info("Startar Catan med " + players.size() + " spelare");

	try {
	    CatanViewer viewer = new CatanViewer(new Board(BOARD_WIDTH, BOARD_HEIGHT, players));
	    viewer.show();
	} catch (FileNotFoundException e) {
	    // The game cannot be drawn without its images, so it is not reasonable to continue
	    LOGGER.log(Level.SEVERE, "Spelets bilder saknas", e);
	    JOptionPane.showMessageDialog(null,
					  "The game could not start because some of its files are missing:\n"
					  + e.getMessage(),
					  "Catan", JOptionPane.ERROR_MESSAGE);
	}
    }

    private static void setUpLogging() {
	try {
	    FileHandler fileHandler = new FileHandler(LOG_FILE, true);
	    fileHandler.setFormatter(new SimpleFormatter());
	    Logger.getLogger("").addHandler(fileHandler);
	} catch (IOException e) {
	    // The game works without a log file, so continue and log to the console only
	    LOGGER.log(Level.WARNING, "Kunde inte skapa loggfil " + LOG_FILE, e);
	}
    }
}