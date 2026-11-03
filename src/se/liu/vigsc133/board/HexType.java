package se.liu.vigsc133.board;

import se.liu.vigsc133.player.Resource;
import java.awt.*;
import java.util.Optional;

/**
 * The kinds of hexagonal tiles on the Catan board.
 * <p>
 * Each type has a color used when the tile is drawn, and all types except
 * {@link #DESERT} produce a {@link Resource} when their number is rolled.
 */
public enum HexType {
    LUMBER(new Color(47, 19, 13), Resource.LUMBER),
    WOOL(new Color(255, 254, 208), Resource.WOOL),
    BRICK(new Color(171, 80, 62), Resource.BRICK),
    ORE(new Color(115, 115, 115), Resource.ORE),
    GRAIN(new Color(246, 203, 86), Resource.GRAIN),
    DESERT(new Color(247, 220, 144), null);

    private final Color color;
    private final Resource resource;

    HexType(Color color, Resource resource) {
        this.color = color;
        this.resource = resource;
    }

    public Color getColor() {
        return color;
    }

    /**
     * @return the resource this kind of tile produces, or empty for the desert
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }
}