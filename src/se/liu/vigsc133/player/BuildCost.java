package se.liu.vigsc133.player;

import java.util.Map;

/**
 * The resource cost of everything a {@link Player} can buy: roads, villages,
 * cities and development cards.
 * <p>
 * Keeping the costs here as data, instead of as numbers spread out in the game
 * logic, means that they can be read, compared and changed in one place.
 * Use {@link Player#trySpend(BuildCost)} to pay a cost.
 */
public enum BuildCost
{
    ROAD(Map.of(Resource.LUMBER, 1, Resource.BRICK, 1)),
    VILLAGE(Map.of(Resource.LUMBER, 1, Resource.BRICK, 1, Resource.WOOL, 1, Resource.GRAIN, 1)),
    CITY(Map.of(Resource.ORE, 3, Resource.GRAIN, 2)),
    DEVELOPMENT_CARD(Map.of(Resource.WOOL, 1, Resource.ORE, 1, Resource.GRAIN, 1));

    private final Map<Resource, Integer> resources;

    BuildCost(final Map<Resource, Integer> resources) {
	this.resources = resources;
    }

    /**
     * @return how many of each resource the item costs (an unmodifiable map)
     */
    public Map<Resource, Integer> getResources() {
	return resources;
    }
}