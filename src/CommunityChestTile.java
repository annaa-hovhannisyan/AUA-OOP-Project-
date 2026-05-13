/**
 * Represents a Community Chest tile.
 * Gives the player a random Community Chest card.
 */

public class CommunityChestTile extends Tile {
    private static final CardDeck deck = new CardDeck("community");
    public CommunityChestTile(String name, int position) {
        super(name, position);
    }

    /**
     * Executes the tile action.
     * @param player current player
     */
    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on Community Chest!");
        CardDeck.Card card = deck.draw();
        System.out.println("Card: " + card.getDescription());
        int effect = card.getCashEffect();
        if (effect > 0) {
            player.addMoney(effect);
            System.out.println(player.getName() + " receives $" + effect + ". New balance: $" + player.getMoney());
        } else if (effect < 0) {
            player.subtractMoney(Math.abs(effect));
            System.out.println(player.getName() + " pays $" + Math.abs(effect) + ". New balance: $" + player.getMoney());
        }
    }
}
