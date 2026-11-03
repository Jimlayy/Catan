package se.liu.vigsc133.player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Random;

/**
 * Represents a player in the Catan game, maintaining all relevant state including:
 * <ul>
 *     <li>Player name and color</li>
 *     <li>Victory points</li>
 *     <li>Resources: lumber, wool, brick, ore, grain</li>
 *     <li>Development cards</li>
 * </ul>
 *
 * This class provides methods to manage the player's resources, earn points, and interact with game mechanics
 * like building or playing development cards.
 */
public class Player
{
    private static final int STARTING_RESOURCE_AMOUNT = 3;
    private static final Random RND = new Random();

    private final String name;
    private final Color color;
    private final Map<Resource, Integer> resources = new EnumMap<>(Resource.class);
    private int points = 0;
    private final List<DevelopmentCard> developmentCards = new ArrayList<>();

    public Player(String name, Color color) {
	this.name = name;
	this.color = color;
	for (Resource resource : Resource.values()) {
	    resources.put(resource, STARTING_RESOURCE_AMOUNT);
	}
    }

    public Color getColor() {
	return color;
    }

    public String getName() {
	return name;
    }

    public int getResourceAmount(Resource resource) {
	return resources.get(resource);
    }

    public int getPoints() {
	return points;
    }

    public int getTotalAmountOfResources() {
	int total = 0;
	for (int amount : resources.values()) {
	    total += amount;
	}
	return total;
    }

    public boolean hasNoResources() {
	return getTotalAmountOfResources() == 0;
    }

    public void addPoints(int points) {
	this.points += points;
    }

    public void addResource(Resource resource, int amount) {
	resources.merge(resource, amount, Integer::sum);
    }

    public void addDevCard(DevelopmentCard card) {
	developmentCards.add(card);
    }

    public List<DevelopmentCard> getDevelopmentCards() {
	return Collections.unmodifiableList(developmentCards);
    }

    /**
     * Removes the given amount of a resource, if the player has enough.
     *
     * @return true if the resource was removed, false if the player had too few
     */
    public boolean removeResource(Resource resource, int amount) {
	if (getResourceAmount(resource) < amount) {
	    return false;
	}
	resources.merge(resource, -amount, Integer::sum);
	return true;
    }

    /**
     * Pays the given cost if the player can afford all of it.
     * Nothing is removed if the player cannot afford the whole cost.
     *
     * @return true if the cost was paid
     */
    public boolean trySpend(BuildCost cost) {
	for (Map.Entry<Resource, Integer> entry : cost.getResources().entrySet()) {
	    if (getResourceAmount(entry.getKey()) < entry.getValue()) {
		return false;
	    }
	}
	for (Map.Entry<Resource, Integer> entry : cost.getResources().entrySet()) {
	    removeResource(entry.getKey(), entry.getValue());
	}
	return true;
    }

    public void removeDevCard(DevelopmentCard card) {
	developmentCards.remove(card);
    }

    /**
     * Removes one of the player's resource cards at random, where every card is equally likely.
     *
     * @return the removed resource, or empty if the player has no resources
     */
    public Optional<Resource> removeRandomResource() {
	int cardIndex = RND.nextInt(Math.max(1, getTotalAmountOfResources()));
	for (Map.Entry<Resource, Integer> entry : resources.entrySet()) {
	    cardIndex -= entry.getValue();
	    if (cardIndex < 0) {
		removeResource(entry.getKey(), 1);
		return Optional.of(entry.getKey());
	    }
	}
	return Optional.empty();
    }
}
