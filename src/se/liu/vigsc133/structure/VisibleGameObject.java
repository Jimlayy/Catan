package se.liu.vigsc133.structure;

import java.awt.*;
/**
 * Interface representing a game object that can be drawn on the screen.
 * <p>
 * Classes implementing this interface must provide a {@code draw} method,
 * which is responsible for rendering the object using a {@link Graphics2D} context.
 */
public interface VisibleGameObject
{
    void draw(Graphics2D g);
}
