package se.liu.vigsc133.ui;

import se.liu.vigsc133.board.Board;
import se.liu.vigsc133.board.BoardListener;
import se.liu.vigsc133.board.HexType;
import se.liu.vigsc133.board.Hexagon;
import se.liu.vigsc133.structure.Road;
import se.liu.vigsc133.structure.Settlement;
import se.liu.vigsc133.structure.VisibleGameObject;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

/**
 * {@code CatanComponent} is a custom Swing component that renders the graphical
 * representation of a Settlers of Catan game board.
 *
 * <p>
 * This component listens for changes in the {@link Board} model via the
 * {@link BoardListener} interface and updates the visual state accordingly.
 * It draws the background image, resource hex tiles, number tokens, roads, and settlements.
 * </p>
 *
 * <p>
 * The background image is scaled and positioned once, when the component is created,
 * so that it is centered on the hexagon grid. Everything else is drawn on top of it
 * by asking each {@link VisibleGameObject} from the board to draw itself, so this
 * component does not need to know how tiles, roads or buildings look.
 * </p>
 *
 * @see Board
 * @see Hexagon
 * @see Road
 * @see Settlement
 * @see HexType
 */
public class CatanComponent extends JComponent implements BoardListener
{
    // Resource paths are passed to the class loader, where "/" is always the separator
    private static final String BACKGROUND_IMAGE = "images/board.png";
    /**
     * Extra width and height (in tiles) added around the hexagon grid, so that the background
     * image's sea frames the outermost tiles. The value was found by testing what fits the
     * image.
     */
    private static final double BACKGROUND_SCALE_MARGIN = Math.E;

    private final Board board;
    private final Image background;
    private final Point backgroundPosition;

    public CatanComponent(final Board board) throws FileNotFoundException {
        this.board = board;

        Dimension size = board.getPixelSize(BACKGROUND_SCALE_MARGIN);
        int width = size.width;
        int height = size.height;

        Image original = ImageLoader.load(BACKGROUND_IMAGE).getImage();
        this.background = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        Point center = board.getCenterPos();
        this.backgroundPosition = new Point(center.x - (width - 1) / 2, center.y - (height - 1) / 2);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(background, backgroundPosition.x, backgroundPosition.y, this);
        for (VisibleGameObject obj : board.getVisibleGameObjects()) {
            obj.draw(g2d);
        }
    }

    @Override public void boardChanged() {
        this.repaint();
    }
}
