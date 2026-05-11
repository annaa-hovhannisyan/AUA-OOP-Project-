public class TaxTile extends Tile {

    private int taxAmount;
    private String color;

    public TaxTile(String name, int position, int taxAmount, String color) {
        super(name, position);
        this.taxAmount = taxAmount;
        this.color = color;
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on " + getName()
                + ". Pay $" + taxAmount + " in taxes.");
        player.subtractMoney(taxAmount);
    }

    public int getTaxAmount() { return taxAmount; }
    public String getColor()  { return color; }
}