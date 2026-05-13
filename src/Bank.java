/**
 * Represents the central bank in the Monopoly game.
 *
 * <p>The {@code Bank} is the single source of truth for all financial
 * transactions. Instead of having tiles manipulate {@link Player} balances
 * directly, every money movement goes through one of the Bank's methods.
 * This makes it easy to track the bank's remaining funds and to extend the
 * game (e.g. mortgages, houses, hotels) without touching tile logic.</p>
 *
 * <p>The bank starts with {@value #INITIAL_FUNDS} dollars — the standard
 * Monopoly starting amount. It can run out of money, at which point further
 * {@link #pay(Player, int)} calls will print a warning instead of paying.</p>
 *
 * <p>Typical flow:</p>
 * <pre>{@code
 * Bank bank = new Bank();
 * bank.payGoSalary(player);      // player passes GO
 * bank.buyProperty(player, prop); // player purchases a property
 * bank.collectTax(player, 200, "Income Tax"); // player lands on tax tile
 * }</pre>
 *
 * @author Anna Hovhannisyan
 * @author Seda Hovhannisyan
 * @version 1.0
 * @see Player
 * @see Property
 */
public class Bank {
    /** The bank's current cash balance in dollars. */
    private int funds; 
    /**
     * The exact amount the Bank starts with.
     * Based on the standard Monopoly rulebook total currency supply.
     */
    public static final int INITIAL_FUNDS = 20580; 
    /**
     * Constructs a new Bank with {@value #INITIAL_FUNDS} dollars.
     */
    public Bank() {
        this.funds = INITIAL_FUNDS;
    } 
    /**
     * Pays a specified amount from the bank directly to a player.
     *
     * <p>If the bank does not have sufficient funds the payment is refused
     * and a message is printed instead.</p>
     *
     * @param player the recipient {@link Player}; must not be {@code null}
     * @param amount the amount to pay; must be positive
     */
    public void pay(Player player, int amount) {
        if (funds >= amount) {
            funds -= amount;
            player.addMoney(amount);
            System.out.println("Bank paid $" + amount + " to " + player.getName()
                    + ". Bank remaining: $" + funds);
        } else {
            System.out.println("Bank is out of money!");
        }
    } 
    /**
     * Collects a specified amount from a player into the bank.
     *
     * <p>Calls {@link Player#subtractMoney(int)} which automatically handles
     * the bankruptcy check. Amounts of zero or less are silently ignored.</p>
     *
     * @param player the payer; must not be {@code null}
     * @param amount the amount to collect; values &le; 0 are ignored
     */
    public void collect(Player player, int amount) {
        if (amount <= 0) return;
        player.subtractMoney(amount);
        funds += amount;
        System.out.println("Bank collected $" + amount + " from " + player.getName()
                + ". Bank remaining: $" + funds);
    } 
    /**
     * Processes a property purchase: transfers the purchase price from the
     * player to the bank and assigns ownership.
     *
     * <p>If the player cannot afford the property the transaction is refused.</p>
     *
     * @param player   the buying {@link Player}; must not be {@code null}
     * @param property the {@link Property} being purchased; must not be {@code null}
     */
    public void buyProperty(Player player, Property property) {
        if (player.getMoney() >= property.getPrice()) {
            collect(player, property.getPrice());
            property.setOwner(player);
            player.addProperty(property);
            System.out.println(player.getName() + " bought " + property.getName()
                    + " for $" + property.getPrice());
        } else {
            System.out.println(player.getName() + " cannot afford " + property.getName());
        }
    } 
    /**
     * Pays the standard GO salary ($200) to a player who has passed or
     * landed on the GO tile.
     *
     * <p>Delegates to {@link #pay(Player, int)} with a fixed amount of 200.</p>
     *
     * @param player the {@link Player} receiving the salary; must not be {@code null}
     */
    public void payGoSalary(Player player) {
        pay(player, 200);
        System.out.println(player.getName() + " collected $200 salary for passing GO!");
    } 
    /**
     * Collects a tax payment from a player, logging the reason for the charge.
     *
     * @param player the {@link Player} paying the tax; must not be {@code null}
     * @param amount the tax amount in dollars; must be positive
     * @param reason a human-readable description (e.g. {@code "Income Tax"})
     */
    public void collectTax(Player player, int amount, String reason) {
        collect(player, amount);
        System.out.println(player.getName() + " paid $" + amount + " for " + reason);
    } 
    /**
     * Collects a $25 metro ticket fee from a player who landed on an unowned
     * metro station.
     *
     * @param player the {@link Player} paying the ticket; must not be {@code null}
     */
    public void collectMetroTicket(Player player) {
        collect(player, 25);
        System.out.println(player.getName() + " paid $25 metro ticket to the bank");
    } 
    /**
     * Applies a card effect to a player: positive amounts are paid out by the
     * bank, negative amounts are collected by the bank.
     *
     * <p>Used by {@link ChanceTile} and {@link CommunityChestTile} after
     * drawing a {@link CardDeck.Card}.</p>
     *
     * @param player the affected {@link Player}; must not be {@code null}
     * @param amount the cash effect (positive = bank pays player,
     *               negative = bank collects from player, 0 = no effect)
     */
    public void applyCard(Player player, int amount) {
        if (amount > 0) {
            pay(player, amount);
        } else if (amount < 0) {
            collect(player, Math.abs(amount));
        }
    } 
    /**
     * Returns the bank's current cash balance.
     *
     * @return remaining funds in dollars
     */
    public int getFunds() {
        return funds;
    }
}
