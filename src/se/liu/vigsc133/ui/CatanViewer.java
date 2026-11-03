package se.liu.vigsc133.ui;

import se.liu.vigsc133.board.Board;
import se.liu.vigsc133.board.BoardListener;
import se.liu.vigsc133.board.Hexagon;
import se.liu.vigsc133.player.DevelopmentCard;
import se.liu.vigsc133.player.Player;
import se.liu.vigsc133.structure.Settlement;
import se.liu.vigsc133.player.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.StringJoiner;

/**
 * {@code CatanViewer} is the primary graphical user interface (GUI) class for rendering and interacting
 * with a Settlers of Catan game. It provides players with controls to perform game actions such as
 * rolling dice, building roads, villages, and cities, buying and playing development cards, and ending turns.
 *
 * <p>
 * The viewer displays the game board, player resources, development cards, and the current dice roll.
 * It uses Java Swing components including {@link JFrame}, {@link JLayeredPane}, and {@link JButton}
 * to render the GUI, and listens for updates from the game model via the {@link BoardListener} interface.
 * </p>
 *
 * <p>
 * Interactive elements such as buttons are dynamically added and removed from the GUI to represent
 * the current state of the game and valid player actions. When the board state changes, the UI updates
 * accordingly, including highlighting valid building positions and displaying player statistics.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Renders and updates game visuals based on board state.</li>
 *     <li>Handles all player interactions via buttons and dialogs.</li>
 *     <li>Updates player resource and point labels in real time.</li>
 *     <li>Provides dialogs for selecting resources and development cards.</li>
 * </ul>
 *
 * @see Board
 * @see CatanComponent
 * @see Hexagon
 * @see Settlement
 * @see Player
 * @see DevelopmentCard
 */
public class CatanViewer implements BoardListener
{
    // Resource paths are passed to the class loader, where "/" is always the separator
    private static final String ICON_IMAGE = "images/icon.png";
    private static final int ELEMENT_SIZE = 10;

    private final Board board;
    private final List<JButton> placementButtons = new ArrayList<>();
    private JFrame frame = null;
    private final JLayeredPane layeredPane = new JLayeredPane();
    private JLabel pointsLabel = null;
    private final Map<Resource, JLabel> resourceLabels = new EnumMap<>(Resource.class);
    private JLabel devCardLabel = null;
    private JLabel currentDiceRollLabel = null;
    private JLabel currentPlayerLabel = null;
    private final ImageIcon icon;

    public CatanViewer(final Board board) throws FileNotFoundException {
	this.board = board;
	this.icon = ImageLoader.load(ICON_IMAGE);
	board.addBoardListener(this);
    }

    private void clearPlacementButtons() {
	for (JButton button : placementButtons) {
	    layeredPane.remove(button);
	}
	placementButtons.clear();
	layeredPane.revalidate();
	layeredPane.repaint();
    }

    @Override public void boardChanged() {
	if (board.isGameOver()) {
	    JOptionPane.showMessageDialog(frame, board.getWinner().getName() + " won!",
					  "Game over", JOptionPane.INFORMATION_MESSAGE);
	    frame.dispose();
	    return;
	}

	updateLabels();

	if (board.isMoveRobber()){
	    moveRobber();
	}
	frame.revalidate();
	frame.repaint();
    }

    private void updateLabels() {
	Player currentPlayer = board.getCurrentPlayer();
	pointsLabel.setText("Points: " + currentPlayer.getPoints());
	currentPlayerLabel.setText("Turn: " + currentPlayer.getName());
	currentPlayerLabel.setForeground(currentPlayer.getColor());
	currentDiceRollLabel.setText("Current Dice Roll: " + board.getCurrentDiceRoll());

	for (Map.Entry<Resource, JLabel> entry : resourceLabels.entrySet()) {
	    Resource resource = entry.getKey();
	    entry.getValue().setText(resource.getDisplayName() + ": " + currentPlayer.getResourceAmount(resource));
	}

	StringJoiner cards = new StringJoiner(", ", "Development Cards: ", "");
	for (DevelopmentCard card : currentPlayer.getDevelopmentCards()) {
	    cards.add(card.toString());
	}
	devCardLabel.setText(cards.toString());
    }

    private void moveRobber() {
	clearPlacementButtons();
	for (Hexagon tile : board.getTiles()) {
	    if (!tile.hasRobber()) {
		Point center = tile.getPos();
		addPlacementButton(center.x, center.y, createMoveRobberListener(center));
	    }
	}
    }

    private ActionListener createMoveRobberListener(Point tilePosition) {
	return e -> {
	    clearPlacementButtons();
	    board.moveRobber(tilePosition);
	};
    }


    private Resource selectResource() {
	Resource[] options = Resource.values();
	Resource selectedResource = null;
	while (selectedResource == null) {
	    selectedResource = (Resource) JOptionPane.showInputDialog(
		    frame, "Choose a resource card to get", "Select Resource",
		    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
	return selectedResource;
    }

    private void setUpLayeredPane() {
	layeredPane.setPreferredSize(new Dimension(board.getWidth(), board.getHeight()));
	frame.add(layeredPane, BorderLayout.CENTER);
    }

    private void setUpComponent() throws FileNotFoundException {
	CatanComponent component = new CatanComponent(board);
	component.setBounds(0, 0, board.getWidth(), board.getHeight());
	board.addBoardListener(component);
	layeredPane.add(component, JLayeredPane.DEFAULT_LAYER);
    }

    private void setUpPlayerInfoPanel() {
	JPanel playerInfoPanel = new JPanel();
	playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.Y_AXIS));
	playerInfoPanel.setBorder(BorderFactory.createEmptyBorder(ELEMENT_SIZE, ELEMENT_SIZE, ELEMENT_SIZE, ELEMENT_SIZE));

	Player currentPlayer = board.getCurrentPlayer();
	pointsLabel = new JLabel("Points: " + currentPlayer.getPoints());
	devCardLabel = new JLabel();
	currentPlayerLabel = new JLabel();

	playerInfoPanel.add(pointsLabel);
	playerInfoPanel.add(devCardLabel);
	playerInfoPanel.add(currentPlayerLabel);

	for (Resource resource : Resource.values()) {
	    JLabel label = new JLabel();
	    resourceLabels.put(resource, label);
	    playerInfoPanel.add(label);
	}

	frame.add(playerInfoPanel, BorderLayout.NORTH);
    }

    private void setUpDiceRollPanel() {
	JPanel diceRollPanel = new JPanel();
	diceRollPanel.setLayout(new BoxLayout(diceRollPanel, BoxLayout.Y_AXIS));
	diceRollPanel.setBorder(BorderFactory.createEmptyBorder(ELEMENT_SIZE, ELEMENT_SIZE, ELEMENT_SIZE, ELEMENT_SIZE));

	currentDiceRollLabel = new JLabel("Current Dice Roll: " + board.getCurrentDiceRoll());
	diceRollPanel.add(currentDiceRollLabel);

	frame.add(diceRollPanel, BorderLayout.EAST);
    }

    private void setUpButtons() {
	JPanel buttonPanel = new JPanel(new FlowLayout());
	addActionButton(buttonPanel, "Roll Dice", createRollDiceListener());
	addActionButton(buttonPanel, "Build Road", createBuildRoadListener());
	addActionButton(buttonPanel, "Build Village", createBuildVillageListener());
	addActionButton(buttonPanel, "Build City", createBuildCityListener());
	addActionButton(buttonPanel, "Buy Development Card", createBuyDevCardListener());
	addActionButton(buttonPanel, "Play Development Card", createPlayDevCardListener());
	addActionButton(buttonPanel, "End Turn", createEndTurnListener());
	frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addActionButton(JPanel panel, String text, ActionListener listener) {
	JButton button = new JButton(text);
	button.addActionListener(e -> {
	    if (board.isMoveRobber()) {
		// The robber must be moved before anything else is done, so show its buttons again
		JOptionPane.showMessageDialog(frame, "Move the robber first.");
		moveRobber();
		return;
	    }
	    listener.actionPerformed(e);
	});
	panel.add(button);
    }


    public void show() throws FileNotFoundException {
	this.frame = new JFrame("Catan");
	frame.setLayout(new BorderLayout());
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setIconImage(icon.getImage());

	setUpLayeredPane();
	setUpComponent();
	setUpButtons();
	setUpPlayerInfoPanel();
	setUpDiceRollPanel();
	updateLabels();

	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
    }

    private ActionListener createRollDiceListener() {
	return e -> {
	    clearPlacementButtons();
	    board.rollDice();
	};
    }

    private ActionListener createBuildRoadListener() {
	return e -> {
	    clearPlacementButtons();
	    List<Point> locations = board.getSettlementLocations();
	    Player player = board.getCurrentPlayer();
	    for (int i = 0; i < locations.size(); i++) {
		for (int j = i + 1; j < locations.size(); j++) {
		    Point start = locations.get(i);
		    Point end = locations.get(j);
		    if (board.isValidRoadPlace(start, end, player)) {
			addPlacementButton((start.x + end.x) / 2, (start.y + end.y) / 2, createConstructRoadListener(start, end));
		    }
		}
	    }
	};
    }

    private ActionListener createConstructRoadListener(Point start, Point end) {
	return e -> {
	    clearPlacementButtons();
	    board.buildRoad(start, end);
	};
    }

    private ActionListener createBuildVillageListener() {
	return e -> {
	    clearPlacementButtons();
	    for (Point p : board.getSettlementLocations()) {
		if (board.isValidVillagePlace(p, board.getCurrentPlayer())) {
		    addPlacementButton(p.x, p.y, createConstructVillageListener(p));
		}
	    }
	};
    }

    private ActionListener createConstructVillageListener(Point position) {
	return e -> {
	    clearPlacementButtons();
	    board.buildVillage(position);
	};
    }

    private ActionListener createBuildCityListener() {
	return e -> {
	    clearPlacementButtons();
	    for (Settlement settlement : board.getSettlements()) {
		if (settlement.canBeUpgraded() && settlement.getOwner().equals(board.getCurrentPlayer())) {
		    Point position = settlement.getPosition();
		    addPlacementButton(position.x, position.y, createConstructCityListener(position));
		}
	    }
	};
    }

    private ActionListener createConstructCityListener(Point position) {
	return e -> {
	    clearPlacementButtons();
	    board.buildCity(position);
	};
    }

    private ActionListener createBuyDevCardListener() {
	return e -> board.buyDevCard();
    }

    private ActionListener createPlayDevCardListener() {
	return e -> playDevelopmentCard();
    }

    private void playDevelopmentCard() {
	DevelopmentCard[] options = board.getCurrentPlayer().getDevelopmentCards().stream()
		.filter(DevelopmentCard::isPlayable)
		.distinct()
		.toArray(DevelopmentCard[]::new);

	if (options.length == 0) {
	    JOptionPane.showMessageDialog(frame, "You have no development cards to play.");
	    return;
	}

	DevelopmentCard selectedCard = (DevelopmentCard) JOptionPane.showInputDialog(
		frame, "Choose a development card to play", "Play Development Card",
		JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	if (selectedCard == null) {
	    return;
	}

	List<Resource> chosenResources = new ArrayList<>();
	for (int i = 0; i < selectedCard.getResourceChoiceCount(); i++) {
	    chosenResources.add(selectResource());
	}
	board.playDevCard(selectedCard, chosenResources);
    }

    private ActionListener createEndTurnListener() {
	return e -> {
	    clearPlacementButtons();
	    board.endTurn();
	};
    }

    private void addPlacementButton(int x, int y, ActionListener listener) {
	JButton button = new JButton();
	button.setBounds(x - ELEMENT_SIZE / 2, y - ELEMENT_SIZE / 2, ELEMENT_SIZE, ELEMENT_SIZE);
	button.addActionListener(listener);
	placementButtons.add(button);
	layeredPane.add(button, JLayeredPane.POPUP_LAYER);
    }
}
