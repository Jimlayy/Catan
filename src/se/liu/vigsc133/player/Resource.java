package se.liu.vigsc133.player;

/**
 * The five kinds of resource cards a {@link Player} can hold.
 * <p>
 * Resources are produced by the hexagon tiles on the board (see
 * {@link se.liu.vigsc133.board.HexType}) and are spent on buildings and
 * development cards according to {@link BuildCost}.
 */
public enum Resource
{
    LUMBER("Lumber"), WOOL("Wool"), BRICK("Brick"), ORE("Ore"), GRAIN("Grain");

    private final String displayName;

    Resource(final String displayName) {
	this.displayName = displayName;
    }

    /**
     * @return the name shown to the user
     */
    public String getDisplayName() {
	return displayName;
    }

    @Override public String toString() {
	return displayName;
    }
}