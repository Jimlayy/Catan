package se.liu.vigsc133.player;

import se.liu.vigsc133.board.Board;

import java.util.List;

/**
 * The development cards in a Catan game, each with its own effect.
 * <p>
 * Every card type implements {@link #play} itself, so the game logic never needs
 * to check which card it is dealing with: {@link Board#playDevCard} simply asks the
 * card to apply its effect. Cards that need the player to choose resources (such as
 * Monopoly) state how many through {@link #getResourceChoiceCount()}, which lets the
 * user interface ask for them before the card is played.
 * <p>
 * Victory point cards cannot be played; they give their point as soon as they are
 * bought, through {@link #onBought}. The number of each card in a new deck is given
 * by {@link #getDeckCount()}.
 */
public enum DevelopmentCard
{
    KNIGHT(14, 0) {
        @Override public void play(Board board, Player player, List<Resource> chosenResources) {
            board.startRobberMove();
        }
    },

    VICTORY_POINT(5, 0) {
        @Override public void onBought(Player player) {
            player.addPoints(1);
        }

        @Override public boolean isPlayable() {
            return false;
        }

        @Override public void play(Board board, Player player, List<Resource> chosenResources) {
            throw new IllegalStateException("Victory point cards cannot be played");
        }
    },

    ROAD_BUILDING(2, 0) {
        @Override public void play(Board board, Player player, List<Resource> chosenResources) {
            // Simplification: the player gets the resources for two roads instead of placing them directly
            for (int i = 0; i < FREE_ROAD_COUNT; i++) {
                BuildCost.ROAD.getResources().forEach(player::addResource);
            }
        }
    },

    YEAR_OF_PLENTY(2, 2) {
        @Override public void play(Board board, Player player, List<Resource> chosenResources) {
            for (Resource resource : chosenResources) {
                player.addResource(resource, 1);
            }
        }
    },

    MONOPOLY(2, 1) {
        @Override public void play(Board board, Player player, List<Resource> chosenResources) {
            Resource resource = chosenResources.getFirst();
            int total = 0;
            for (Player other : board.getPlayers()) {
                if (!other.equals(player)) {
                    int amount = other.getResourceAmount(resource);
                    other.removeResource(resource, amount);
                    total += amount;
                }
            }
            player.addResource(resource, total);
        }
    };

    private static final int FREE_ROAD_COUNT = 2;

    private final int deckCount;
    private final int resourceChoiceCount;

    DevelopmentCard(final int deckCount, final int resourceChoiceCount) {
        this.deckCount = deckCount;
        this.resourceChoiceCount = resourceChoiceCount;
    }

    /**
     * Applies the card's effect.
     *
     * @param board           the board the game is played on
     * @param player          the player who plays the card
     * @param chosenResources the resources the player chose, exactly
     *                        {@link #getResourceChoiceCount()} of them
     */
    public abstract void play(Board board, Player player, List<Resource> chosenResources);

    /**
     * Called when a player buys the card. Does nothing unless a card overrides it.
     * <p>
     * This is deliberately not abstract: only victory point cards need to react when
     * bought, so a default that does nothing avoids an empty method in every other card.
     */
    public void onBought(Player player) {
    }

    /**
     * @return whether the card can be played from the player's hand
     */
    public boolean isPlayable() {
        return true;
    }

    /**
     * @return how many resources the player must choose when playing the card
     */
    public int getResourceChoiceCount() {
        return resourceChoiceCount;
    }

    /**
     * @return how many cards of this type a new deck contains
     */
    public int getDeckCount() {
        return deckCount;
    }
}