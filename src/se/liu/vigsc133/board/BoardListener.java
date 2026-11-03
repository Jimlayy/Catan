package se.liu.vigsc133.board;
/**
 * Listener interface for receiving updates when the {@link Board} changes state.
 * <p>
 * Implementing classes can register with a {@code Board} instance to be notified
 * whenever significant game events occur, such as:
 * <ul>
 *     <li>Building a road, village, or city</li>
 *     <li>Rolling the dice and assigning resources</li>
 *     <li>Moving the robber</li>
 *     <li>Ending a turn</li>
 * </ul>
 *
 * @see Board#addBoardListener(BoardListener)
 */
public interface BoardListener
{
    /**
     * Invoked whenever the game board's state changes and a visual or logical update may be required.
     */
    void boardChanged();
}
