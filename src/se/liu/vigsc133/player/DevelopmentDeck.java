package se.liu.vigsc133.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The shuffled deck that players buy {@link DevelopmentCard}s from.
 * <p>
 * A new deck contains {@link DevelopmentCard#getDeckCount()} cards of each type in
 * random order. Cards are drawn from the top with {@link #draw()} until the deck is
 * empty; drawn cards never return to the deck.
 */
public class DevelopmentDeck
{
    private final List<DevelopmentCard> cards = new ArrayList<>();

    public DevelopmentDeck() {
	for (DevelopmentCard card : DevelopmentCard.values()) {
	    for (int i = 0; i < card.getDeckCount(); i++) {
		cards.add(card);
	    }
	}
	Collections.shuffle(cards);
    }

    public boolean isEmpty() {
	return cards.isEmpty();
    }

    /**
     * Removes and returns the top card.
     *
     * @throws IllegalStateException if the deck is empty
     */
    public DevelopmentCard draw() {
	if (cards.isEmpty()) {
	    throw new IllegalStateException("The development deck is empty");
	}
	return cards.removeFirst();
    }
}