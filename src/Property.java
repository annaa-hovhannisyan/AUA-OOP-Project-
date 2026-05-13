import java.util.Scanner; 
/**
 * Represents a purchasable color-group property on the Monopoly board.
 *
 * <p>A {@code Property} is one of the 22 buyable tiles grouped by color.
 * When a player lands on an unowned property they may purchase it from the bank.
 * Once owned, all other players who land on it must pay rent to the owner.</p>
 *
 * <p>Color groups and their tiles:</p>
 * <ul>
 *   <li>Red – KFC ($60), McDonald's ($60)</li>
 *   <li>Light Blue – Coffee House ($100), Cofix ($100), Gotcha ($120)</li>
 *   <li>Gray – Zara ($140), Bershka ($140), Pull &amp; Bear ($160)</li>
 *   <li>Orange – Yerevan Mall ($180), Dalma Mall ($180), Mega Mall ($200)</li>
 *   <li>Pink – Chanel ($220), Dior ($220), Gucci ($240)</li>
 *   <li>Green – Yerevan Hotel ($260), Dilijan Hotel ($260), Tsaghkadzor Hotel ($280)</li>
 *   <li>Yellow – Paris ($300), New York ($300)</li>
 *   <li>Dark Blue – Oxford University ($320), Harvard University ($350), MIT University ($400)</li>
 * </ul>
 *
 * @author Anna Hovhannisyan
 * @author Seda Hovhannisyan
 * @version 1.0
 * @see Tile
 * @see Player
 * @see Bank
 */
public class Property extends Tile { 
    /** Purchase price in dollars. */
    private int price; 
    /** Flat rent charged to any non-owner who lands here. */
    private int rent; 
    /**
     * The player who owns this property, or {@code null} if unowned.
     * Ownership is set via {@link #setOwner(Player)} or directly in
     * {@link #landOn(Player)}.
     */
    private Player owner; 
    /**
     * The color group this property belongs to (e.g. {@code "Red"},
     * {@code "Dark Blue"}). Used for display purposes.
     */
    private String colorGroup; 
    /**
     * Constructs a new Property tile.
     *
     * @param name       the display name of the property (e.g. {@code "KFC"})
     * @param position   the 0-based board position (1–39)
     * @param price      the purchase price in dollars
     * @param rent       the rent charged to players who land here while it is owned
     * @param colorGroup the color group label (e.g. {@code "Red"}, {@code "Dark Blue"})
     */
    public Property(String name, int position, int price, int rent, String colorGroup) {
        super(name, position);
        this.price = price;
        this.rent = rent;
        this.owner = null;
        this.colorGroup = colorGroup;
    } 
    /**
     * Handles the effect of a player landing on this property.
     *
     * <ul>
     *   <li>If <strong>unowned</strong>: prompts the player to buy it for
     *       {@code price} dollars. The purchase is confirmed via stdin
     *       ({@code "yes"} / {@code "no"}).</li>
     *   <li>If <strong>owned by another player</strong>: automatically deducts
     *       {@code rent} from the landing player and credits the owner.</li>
     *   <li>If <strong>owned by the landing player</strong>: prints a message
     *       and does nothing.</li>
     * </ul>
     *
     * @param player the {@link Player} who has just landed on this tile
     */
    @Override
    public void landOn(Player player) {
        if (owner == null) {
            System.out.println(player.getName() + " landed on " + getName()
                    + " [" + colorGroup + "]. Price: $" + price);
            System.out.println("Do you want to buy it? (yes/no)");
            Scanner sc = new Scanner(System.in);
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                if (player.getMoney() >= price) {
                    player.subtractMoney(price);
                    player.addProperty(this);
                    this.owner = player;
                    System.out.println(player.getName() + " bought " + getName());
                } else {
                    System.out.println("Not enough money!");
                }
            }
        } else if (!owner.equals(player)) {
            System.out.println(player.getName() + " pays $" + rent
                    + " rent to " + owner.getName());
            player.subtractMoney(rent);
            owner.addMoney(rent);
        } else {
            System.out.println(player.getName() + " owns this property.");
        }
    } 
    /**
     * Assigns an owner to this property.
     * Used by {@link Bank#buyProperty(Player, Property)} to transfer ownership
     * through the bank rather than directly in {@link #landOn(Player)}.
     *
     * @param player the new owner; must not be {@code null}
     */
    public void setOwner(Player player) {
        this.owner = player;
    } 
    /**
     * Returns whether this property has been purchased by any player.
     *
     * @return {@code true} if an owner exists, {@code false} if still available
     */
    public boolean isOwned() {
        return owner != null;
    }
    /**
     * Returns the current owner of this property.
     *
     * @return the owning {@link Player}, or {@code null} if unowned
     */
    public Player getOwner() {
        return owner;
    } 
    /**
     * Returns the purchase price of this property.
     *
     * @return price in dollars
     */
    public int getPrice() {
        return price;
    } 
    /**
     * Returns the flat rent charged when a non-owner lands here.
     *
     * @return rent in dollars
     */
    public int getRent() {
        return rent;
    } 
    /**
     * Returns the color group label for this property.
     *
     * @return e.g. {@code "Red"}, {@code "Dark Blue"}, {@code "Orange"}
     */
    public String getColorGroup() {
        return colorGroup;
    }
}
