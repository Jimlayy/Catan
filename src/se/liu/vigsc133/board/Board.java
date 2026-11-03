package se.liu.vigsc133.board;

import se.liu.vigsc133.structure.VisibleGameObject;
import se.liu.vigsc133.player.DevelopmentCard;
import se.liu.vigsc133.player.Player;
import se.liu.vigsc133.structure.City;
import se.liu.vigsc133.structure.Road;
import se.liu.vigsc133.structure.Settlement;
import se.liu.vigsc133.structure.Village;
import se.liu.vigsc133.player.BuildCost;
import se.liu.vigsc133.player.Resource;
import se.liu.vigsc133.player.DevelopmentDeck;
import se.liu.vigsc133.player.TurnOrder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

/**
 * The game state and rules of a Catan game.
 * <p>
 * {@code Board} is the central model class that the user interface talks to. It keeps
 * track of everything that changes during a game, such as the buildings and roads
 * that have been placed, the current dice roll, the robber and whether the game is
 * over, and it enforces the rules for changing that state:
 * <ul>
 *     <li>Building roads, villages and cities, including where they may be placed
 *     and what they cost (see {@link BuildCost})</li>
 *     <li>Rolling the dice and giving resources to players with buildings next to
 *     the tiles whose number was rolled</li>
 *     <li>Handling a roll of seven: players with too many cards discard half, and the
 *     current player moves the robber and steals from an adjacent player</li>
 *     <li>Buying and playing development cards</li>
 *     <li>Ending turns and detecting when a player has won</li>
 * </ul>
 * <p>
 * Parts with a single, separate responsibility are delegated to other classes:
 * the board's fixed geometry (tiles and building locations) to
 * {@link BoardLayout}, the players and turn order to {@link TurnOrder}, and the
 * development card deck to {@link DevelopmentDeck}. The effect of each development
 * card is implemented by the card itself (see {@link DevelopmentCard#play}).
 * <p>
 * Whenever the game state changes, all registered {@link BoardListener}s are notified
 * so that the user interface can update. Everything that should be drawn on screen is
 * available through {@link #getVisibleGameObjects()}.
 *
 * @see BoardLayout
 * @see TurnOrder
 * @see DevelopmentDeck
 */
public class Board
{
    private static final Logger LOGGER = Logger.getLogger(Board.class.getName());
    private static final Random RND = new Random();
    private static final int DIE_SIDES = 6;
    /** The robber moves on the most likely total of two dice, which is one more than the number of sides. */
    private static final int ROBBER_ROLL = DIE_SIDES + 1;
    /**
     * Players with more resource cards than this must discard half of them when the robber
     * is rolled. The rule in Catan happens to use the same number as the robber roll; the two
     * are unrelated.
     */
    private static final int DISCARD_THRESHOLD = 7;
    private static final int WIN_POINTS = 10;

    private final BoardLayout layout;
    private final TurnOrder turnOrder;
    private final DevelopmentDeck developmentDeck = new DevelopmentDeck();
    private final List<Settlement> settlements = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private final List<BoardListener> listeners = new ArrayList<>();
    private final List<VisibleGameObject> visibleGameObjects = new ArrayList<>();
    private boolean diceRolled = false;
    private int currentDiceRoll = 0;
    private boolean moveRobber = false;
    private Player winner = null;

    public Board(int width, int height, List<Player> players) {
	this.turnOrder = new TurnOrder(players);
	this.layout = new BoardLayout(width, height);
	visibleGameObjects.addAll(layout.getTiles());
	placeStartingVillages();
    }

    public void addBoardListener(BoardListener bl) {
	listeners.add(bl);
    }

    private void notifyListeners() {
	for(BoardListener bl : listeners) {
	    bl.boardChanged();
	}
    }

    private boolean hasRoad(Point start, Point end) {
	for (Road road : roads) {
	    if (road.connects(start, end)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isValidVillagePlace(Point point, Player owner) {
	return isFreeFromNearbySettlements(point) && hasRoadEndingAt(point, owner);
    }

    private boolean hasRoadEndingAt(Point point, Player owner) {
	for (Road road : roads) {
	    if (road.hasEndpoint(point) && road.getOwner().equals(owner)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return whether the player may build a road between the two building locations
     */
    public boolean isValidRoadPlace(Point start, Point end, Player player) {
	return layout.acceptableRoadDistance(start, end) && !hasRoad(start, end)
	       && (hasRoadOrSettlement(start, player) || hasRoadOrSettlement(end, player));
    }

    public Iterable<Settlement> getSettlements() {
	return Collections.unmodifiableList(settlements);
    }

    public List<Point> getSettlementLocations() {
	return layout.getSettlementLocations();
    }

    public Iterable<Hexagon> getTiles() {
	return layout.getTiles();
    }

    public int getWidth() {
	return layout.getWidth();
    }

    public int getHeight() {
	return layout.getHeight();
    }

    public Point getCenterPos() {
	return layout.getCenterPos();
    }

    public Player getCurrentPlayer() {
	return turnOrder.getCurrentPlayer();
    }

    public List<Player> getPlayers() {
	return turnOrder.getPlayers();
    }

    public Player getWinner() {
	return winner;
    }

    public Iterable<VisibleGameObject> getVisibleGameObjects() {
	return Collections.unmodifiableList(visibleGameObjects);
    }

    public int getCurrentDiceRoll() {
	return currentDiceRoll;
    }

    public Dimension getPixelSize(double marginInTiles) {
	return layout.getPixelSize(marginInTiles);
    }

    public boolean isMoveRobber() {
	return moveRobber;
    }

    public boolean isGameOver() {
	return winner != null;
    }

    private boolean hasRoadOrSettlement(Point point, Player owner) {
	return hasRoadEndingAt(point, owner)
	       || findSettlementAt(point).filter(settlement -> settlement.getOwner().equals(owner)).isPresent();
    }

    public void buildVillage(Point pos) {
	Player player = getCurrentPlayer();
	if (!isValidVillagePlace(pos, player)) {
	    LOGGER.info(player.getName() + " försökte bygga en by på en ogiltig plats");
	    return;
	}
	if (player.trySpend(BuildCost.VILLAGE)) {
	    addVillage(pos, player);
	    LOGGER.info(player.getName() + " byggde en by vid " + pos);
	    notifyListeners();
	} else {
	    LOGGER.info(player.getName() + " hade inte råd med en by");
	}
    }

    public void buildCity(Point pos) {
	Player player = getCurrentPlayer();
	Optional<Settlement> upgradable = findSettlementAt(pos)
		.filter(Settlement::canBeUpgraded)
		.filter(settlement -> settlement.getOwner().equals(player));
	if (upgradable.isEmpty()) {
	    LOGGER.info(player.getName() + " försökte bygga en stad på en ogiltig plats");
	    return;
	}
	if (!player.trySpend(BuildCost.CITY)) {
	    LOGGER.info(player.getName() + " hade inte råd med en stad");
	    return;
	}

	Settlement village = upgradable.get();
	City newCity = new City(player, pos);
	settlements.remove(village);
	visibleGameObjects.remove(village);
	settlements.add(newCity);
	visibleGameObjects.add(newCity);
	player.addPoints(1);
	LOGGER.info(player.getName() + " byggde en stad vid " + pos);
	notifyListeners();
    }

    private Optional<Settlement> findSettlementAt(Point pos) {
	for (Settlement settlement : settlements) {
	    if (settlement.getPosition().equals(pos)) {
		return Optional.of(settlement);
	    }
	}
	return Optional.empty();
    }

    public void buildRoad(Point start, Point end) {
	Player player = getCurrentPlayer();
	if (!isValidRoadPlace(start, end, player)) {
	    LOGGER.info(player.getName() + " försökte bygga en ogiltig väg");
	    return;
	}
	if (player.trySpend(BuildCost.ROAD)) {
	    Road newRoad = new Road(player, start, end);
	    roads.add(newRoad);
	    visibleGameObjects.add(newRoad);
	    LOGGER.info(player.getName() + " byggde en väg mellan " + start + " och " + end);
	    notifyListeners();
	} else {
	    LOGGER.info(player.getName() + " hade inte råd med en väg");
	}
    }

    public void rollDice() {
	if (!diceRolled) {
	    diceRolled = true;
	    currentDiceRoll = rollDie() + rollDie();
	    LOGGER.info(getCurrentPlayer().getName() + " slog " + currentDiceRoll);
	    assignResources();
	    if (currentDiceRoll == ROBBER_ROLL) {
		rolledSeven();
	    }
	}
	notifyListeners();
    }

    private int rollDie() {
	return RND.nextInt(1, DIE_SIDES + 1);
    }

    private void assignResources() {
	for (Hexagon tile : layout.getTiles()) {
	    if (tile.hasRobber() || tile.getNumberToken() != currentDiceRoll) {
		continue;
	    }
	    for (Settlement settlement : settlements) {
		if (layout.isCornerOf(tile, settlement.getPosition())) {
		    tile.getType().getResource().ifPresent(
			    resource -> settlement.getOwner().addResource(resource, settlement.getGenerateAmount()));
		}
	    }
	}
    }

    private void rolledSeven() {
	for (Player player : getPlayers()) {
	    if (player.getTotalAmountOfResources() > DISCARD_THRESHOLD) {
		int discardAmount = player.getTotalAmountOfResources() / 2;
		List<Resource> discarded = new ArrayList<>();
		for (int i = 0; i < discardAmount; i++) {
		    player.removeRandomResource().ifPresent(discarded::add);
		}
		LOGGER.info(player.getName() + " lade bort " + discarded);
	    }
	}
	moveRobber = true;
	notifyListeners();
    }

    public void moveRobber(Point tilePosition) {
	Hexagon robberTile = null;
	for (Hexagon tile : layout.getTiles()) {
	    if (tile.getPos().equals(tilePosition)) {
		robberTile = tile;
	    }
	}
	if (robberTile == null) {
	    LOGGER.warning("Det finns ingen ruta vid " + tilePosition);
	    return;
	}

	for (Hexagon tile : layout.getTiles()) {
	    tile.setHasRobber(tile.equals(robberTile));
	}
	moveRobber = false;
	LOGGER.info("Rövaren flyttades till " + tilePosition);

	stealFromAdjacentPlayer(robberTile);
	notifyListeners();
    }

    private void stealFromAdjacentPlayer(Hexagon robberTile) {
	Player thief = getCurrentPlayer();
	List<Player> victims = new ArrayList<>();
	for (Settlement settlement : settlements) {
	    Player owner = settlement.getOwner();
	    if (!owner.equals(thief) && !owner.hasNoResources() && !victims.contains(owner)
		&& layout.isCornerOf(robberTile, settlement.getPosition())) {
		victims.add(owner);
	    }
	}
	if (victims.isEmpty()) {
	    return;
	}

	Player victim = victims.get(RND.nextInt(victims.size()));
	victim.removeRandomResource().ifPresent(stolen -> {
	    thief.addResource(stolen, 1);
	    LOGGER.info(thief.getName() + " stal " + stolen + " från " + victim.getName());
	});
    }

    public void endTurn() {
	if (moveRobber) {
	    LOGGER.info(getCurrentPlayer().getName() + " måste flytta rövaren innan turen kan avslutas");
	    notifyListeners();
	    return;
	}
	Optional<Player> possibleWinner = turnOrder.findPlayerWithPoints(WIN_POINTS);
	if (possibleWinner.isPresent()) {
	    winner = possibleWinner.get();
	    LOGGER.info("Spelet slut! Vinnare: " + winner.getName());
	    notifyListeners();
	    return;
	}
	turnOrder.advance();
	LOGGER.info(getCurrentPlayer().getName() + ":s tur");
	diceRolled = false;
	notifyListeners();
    }

    public void buyDevCard() {
	Player player = getCurrentPlayer();
	if (developmentDeck.isEmpty()) {
	    LOGGER.info(player.getName() + " försökte köpa ett utvecklingskort men de var slut");
	} else if (player.trySpend(BuildCost.DEVELOPMENT_CARD)) {
	    DevelopmentCard card = developmentDeck.draw();
	    player.addDevCard(card);
	    card.onBought(player);
	    LOGGER.info(player.getName() + " köpte ett utvecklingskort (" + card + ")");
	}
	notifyListeners();
    }

    public void playDevCard(DevelopmentCard card, List<Resource> chosenResources) {
	Player player = getCurrentPlayer();
	if (!card.isPlayable() || !player.getDevelopmentCards().contains(card)) {
	    LOGGER.info(player.getName() + " försökte spela " + card + " men kunde inte");
	    return;
	}
	if (chosenResources.size() != card.getResourceChoiceCount()) {
	    throw new IllegalArgumentException(card + " kräver " + card.getResourceChoiceCount() + " resurser");
	}
	player.removeDevCard(card);
	card.play(this, player, chosenResources);
	LOGGER.info(player.getName() + " spelade " + card + " med " + chosenResources);
	notifyListeners();
    }

    /**
     * Lets the current player move the robber, as after rolling a seven.
     */
    public void startRobberMove() {
	moveRobber = true;
    }

    private boolean isFreeFromNearbySettlements(Point p) {
	for (Settlement s : settlements) {
	    if (s.getPosition().distance(p) < layout.getMinSettlementDistance()) {
		return false;
	    }
	}
	return true;
    }

    private void addVillage(Point pos, Player owner) {
	Village newVillage = new Village(owner, pos);
	settlements.add(newVillage);
	visibleGameObjects.add(newVillage);
	owner.addPoints(1);
    }

    private void placeStartingVillages() {
	List<Point> candidates = new ArrayList<>(layout.getSettlementLocations());
	Collections.shuffle(candidates);
	for (Player player : getPlayers()) {
	    for (Point p : candidates) {
		if (isFreeFromNearbySettlements(p)) {
		    addVillage(p, player);
		    LOGGER.info(player.getName() + " fick en startby vid " + p);
		    break;
		}
	    }
	}
    }
}