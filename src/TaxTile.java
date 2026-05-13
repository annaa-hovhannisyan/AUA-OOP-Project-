/**
 * Represents a tax tile on the board.
 * Deducts money from the player.
 */

public class TaxTile extends Tile {
    private final int taxAmount;
    private final String color;
    public TaxTile(String name, int position, int taxAmount, String color) {
        super(name, position);
        this.taxAmount = taxAmount;
        this.color = color;
    }

    /**
     * Charges tax from the player.
     * @param player current player
     */
    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on " + getName() + ". Pay $" + taxAmount + " in taxes.");
        player.subtractMoney(taxAmount);
    }
    public int getTaxAmount() { 
        return taxAmount; 
    }
    public String getColor() { 
        return color; 
    }
}
