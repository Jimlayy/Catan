package se.liu.vigsc133.board;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The fixed geometry of a Catan board: the hexagon tiles and the corner points
 * where players can build.
 * <p>
 * The layout is created once when a game starts. Tiles are placed in a hexagonal
 * pattern around the center of the board using axial coordinates, which are
 * converted to pixel positions. Each tile gets a random {@link HexType} and number
 * token from the standard set, and the robber starts on the desert.
 * <p>
 * Everything that changes during a game (buildings, roads, the robber's later
 * positions) is handled by {@link Board}; this class only knows where things can be.
 */
public class BoardLayout
{
    /** Number of tiles from the center tile to the edge of the board. */
    private static final int RADIUS = 2;
    /** Radius of each hexagon tile, in pixels. */
    private static final int HEX_RADIUS = 60;
    /** Corners closer to each other than this, in pixels, are treated as the same point. */
    private static final int RADIUS_THRESHOLD = 10;
    /** Corners closer than this are the same point, so no road can connect them. */
    private static final int ROAD_MIN_DISTANCE = 10;
    /** Corners farther apart than this are not neighbours, so no road can connect them. */
    private static final int ROAD_MAX_DISTANCE = 100;
    /**
     * Horizontal distance between neighbouring tile centers, measured in tile radii.
     * For a hexagon with a corner pointing up, this is the width of the hexagon: sqrt(3) * radius.
     */
    private static final double HEX_WIDTH_FACTOR = Math.sqrt(3);
    /** Vertical distance between neighbouring tile rows, measured in tile radii. */
    private static final double HEX_ROW_HEIGHT_FACTOR = 1.5;

    private static final List<HexType> TILE_TYPES = List.of(
	    HexType.LUMBER, HexType.LUMBER, HexType.LUMBER, HexType.LUMBER,
	    HexType.GRAIN, HexType.GRAIN, HexType.GRAIN, HexType.GRAIN,
	    HexType.WOOL, HexType.WOOL, HexType.WOOL, HexType.WOOL,
	    HexType.BRICK, HexType.BRICK, HexType.BRICK,
	    HexType.ORE, HexType.ORE, HexType.ORE,
	    HexType.DESERT);

    /**
     * The standard set of number tokens in Catan. These are game data, not magic numbers:
     * each value is the dice roll that makes a tile produce resources.
     */
    private static final List<Integer> NUMBER_TOKENS = List.of(
	    2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12);

    private final Point centerPos;
    private final List<Hexagon> tiles = new ArrayList<>();
    private final List<Point> settlementLocations = new ArrayList<>();
    private final int width;
    private final int height;

    public BoardLayout(int width, int height) {
	this.width = width;
	this.height = height;
	this.centerPos = new Point(width / 2, height / 2);
	createTiles();
	findSettlementLocations();
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    private void createTiles() {
	List<HexType> types = new ArrayList<>(TILE_TYPES);
	List<Integer> tokens = new ArrayList<>(NUMBER_TOKENS);
	Collections.shuffle(types);
	Collections.shuffle(tokens);
	Iterator<HexType> nextType = types.iterator();
	Iterator<Integer> nextToken = tokens.iterator();

	for (int q = -RADIUS; q <= RADIUS; q++) {
	    int firstRow = Math.max(-RADIUS, -q - RADIUS);
	    int lastRow = Math.min(RADIUS, -q + RADIUS);
	    for (int r = firstRow; r <= lastRow; r++) {
		int pixelX = (int) (HEX_RADIUS * HEX_WIDTH_FACTOR * (q + r / 2.0)) + centerPos.x;
		int pixelY = (int) (HEX_RADIUS * HEX_ROW_HEIGHT_FACTOR * r) + centerPos.y;

		HexType type = nextType.next();
		Hexagon tile = new Hexagon(new Point(pixelX, pixelY), type, HEX_RADIUS);
		if (type == HexType.DESERT) {
		    tile.setHasRobber(true);
		} else {
		    tile.setNumberToken(nextToken.next());
		}
		tiles.add(tile);
	    }
	}
    }

    private void findSettlementLocations() {
	// Neighbouring tiles share corners, so only add each corner once
	for (Hexagon tile : tiles) {
	    for (Point corner : tile.getCornerPoints()) {
		boolean alreadyAdded = false;
		for (Point location : settlementLocations) {
		    if (isSame(corner, location)) {
			alreadyAdded = true;
			break;
		    }
		}
		if (!alreadyAdded) {
		    settlementLocations.add(corner);
		}
	    }
	}
    }

    private boolean isSame(Point p1, Point p2) {
	return p1.distance(p2) < RADIUS_THRESHOLD;
    }

    /**
     * @return whether the given building location is one of the tile's corners
     */
    public boolean isCornerOf(Hexagon tile, Point location) {
	for (Point corner : tile.getCornerPoints()) {
	    if (isSame(corner, location)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return whether two corners are exactly one tile edge apart, so that a road can connect them
     */
    public boolean acceptableRoadDistance(Point p1, Point p2) {
	double distance = p1.distance(p2);
	return ROAD_MIN_DISTANCE < distance && distance < ROAD_MAX_DISTANCE;
    }

    /**
     * @return the smallest allowed distance between two settlements
     */
    public int getMinSettlementDistance() {
	return HEX_RADIUS + RADIUS_THRESHOLD;
    }

    public List<Hexagon> getTiles() {
	return Collections.unmodifiableList(tiles);
    }

    public List<Point> getSettlementLocations() {
	return Collections.unmodifiableList(settlementLocations);
    }

    public Point getCenterPos() {
	return centerPos;
    }

    /**
     * @return the size in pixels of an area covering the whole grid plus the given margin (in tiles)
     */
    public Dimension getPixelSize(double marginInTiles) {
	double tilesAcross = 2 * RADIUS + marginInTiles;
	return new Dimension((int) (HEX_WIDTH_FACTOR * HEX_RADIUS * tilesAcross),
			     (int) (HEX_ROW_HEIGHT_FACTOR * HEX_RADIUS * tilesAcross));
    }
}