public class ChanceTile extends Tile {
    private static final CardDeck deck = new CardDeck("chance");
    public ChanceTile(String name, int position) {
        super(name, position);
    }
    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on Chance!");
        CardDeck.Card card = deck.draw();
        System.out.println("Card: " + card.getDescription());
        int effect = card.getCashEffect();
        if (effect > 0) {
            player.addMoney(effect);
            System.out.println(player.getName() + " receives $" + effect + ". New balance: $" + player.getMoney());
        } else if (effect < 0) {
            player.subtractMoney(Math.abs(effect));
            System.out.println(player.getName() + " pays $" + Math.abs(effect)
                    + ". New balance: $" + player.getMoney());
        }
    }
}
