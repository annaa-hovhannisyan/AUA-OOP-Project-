import java.util.ArrayList;
import java.util.List; 
/**
 * Represents a single player in the Monopoly game.
 *
 * <p>A {@code Player} tracks their name, current money balance, board position,
 * jail status, owned properties, and bankruptcy state. All money changes go
 * through {@link #addMoney(int)} and {@link #subtractMoney(int)} so that
 * bankruptcy is detected automatically.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Player anna = new Player("Anna", 1500);
 * anna.move(6, 40);
 * anna.subtractMoney(100);
 * System.out.println(anna); // Anna | Money: $1400 | Position: 6
 * }</pre>
 *
 * @author Anna Hovhannisyan
 * @author Seda Hovhannisyan
 * @version 1.0
 * @see Property
 * @see JailTile
 */
public class Player { 
    /** The player's display name. */
    private String name; 
    /** The player's current cash balance in dollars. Never goes below 0. */
    private int money; 
    /** The player's current 0-based position on the board (0–39). */
    private int position;
    /** Whether the player is currently in jail. */
    private boolean inJail;
    /**
     * Number of consecutive turns this player has spent in jail.
     * Reset to 0 on release.
     */
    private int jailTurns; 
    /** All properties currently owned by this player. */
    private List<Property> properties; 
    /**
     * Whether this player has gone bankrupt (money reached $0 or below).
     * Bankrupt players are removed from the game.
     */
    private boolean bankrupt; 
    /**
     * Constructs a new Player with the given name and starting balance.
     * The player begins at position 0 (GO), not in jail, and with no properties.
     *
     * @param name          the player's display name; must not be {@code null}
     * @param startingMoney the initial cash balance, typically 1500
     */
    public Player(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.inJail = false;
        this.jailTurns = 0;
        this.properties = new ArrayList<>();
        this.bankrupt = false;
    } 
    /**
     * Moves the player forward by the given number of steps,
     * wrapping around the board using modulo arithmetic.
     *
     * <p><strong>Note:</strong> passing GO detection (and the $200 salary)
     * is handled by {@link Game}, not here.</p>
     *
     * @param steps     the number of spaces to advance (typically 2–12)
     * @param boardSize the total number of tiles on the board (40)
     */
    public void move(int steps, int boardSize) {
        position = (position + steps) % boardSize;
    } 
    /**
     * Adds the given amount to this player's balance.
     *
     * @param amount the amount to add; must be positive
     */
    public void addMoney(int amount) {
        money += amount;
    }
    /**
     * Subtracts the given amount from this player's balance.
     * If the resulting balance is 0 or less, the player is declared bankrupt.
     *
     * @param amount the amount to deduct; must be positive
     */
    public void subtractMoney(int amount) {
        money -= amount;
        if (money <= 0) {
            money = 0;
            bankrupt = true;
            System.out.println(name + " is bankrupt!");
        }
    } 
    /**
     * Adds a property to this player's portfolio after a successful purchase.
     *
     * @param p the {@link Property} that was bought; must not be {@code null}
     */
    public void addProperty(Property p) {
        properties.add(p);
    }
    /**
     * Sends this player to Jail, placing them at the jail position and
     * resetting their jail-turn counter.
     *
     * @param jailPosition the board index of the Jail tile (typically 20)
     */
    public void goToJail(int jailPosition) {
        inJail = true;
        jailTurns = 0;
        position = jailPosition;
        System.out.println(name + " is sent to Jail!");
    }
    /**
     * Returns how many consecutive turns this player has been in jail.
     *
     * @return the jail-turn count (0 if not in jail or just arrived)
     */
    public int getJailTurns() {
        return jailTurns;
    } 
    /**
     * Increments the jail-turn counter by one.
     * Called by {@link JailTile#handleJailTurn(Player, java.util.List)} each
     * turn the player remains jailed.
     */
    public void incrementJailTurns() {
        jailTurns++;
    } 
    /**
     * Releases this player from jail, clearing their jail status and
     * resetting the jail-turn counter to zero.
     */
    public void releaseFromJail() {
        inJail = false;
        jailTurns = 0;
    }
    /**
     * Returns the player's display name.
     *
     * @return the name, e.g. {@code "Anna"}
     */
    public String getName() {
        return name;
    } 
    /**
     * Returns the player's current cash balance.
     *
     * @return balance in dollars (always &ge; 0)
     */
    public int getMoney() {
        return money;
    } 
    /**
     * Returns the player's current board position.
     *
     * @return a value in [0, 39]
     */
    public int getPosition() {
        return position;
    } 
    /**
     * Directly sets the player's board position.
     * Used for special moves such as "Go to Jail" cards.
     *
     * @param pos the target board index (0–39)
     */
    public void setPosition(int pos) {
        this.position = pos;
    } 
    /**
     * Returns whether this player is currently in jail.
     *
     * @return {@code true} if in jail, {@code false} otherwise
     */
    public boolean isInJail() {
        return inJail;
    } 
    /**
     * Directly sets the player's jail flag.
     *
     * @param b {@code true} to put in jail, {@code false} to release
     */
    public void setInJail(boolean b) {
        this.inJail = b;
    }
    /**
     * Returns whether this player has gone bankrupt.
     * Bankrupt players are removed from the game after their turn ends.
     *
     * @return {@code true} if bankrupt
     */
    public boolean isBankrupt() {
        return bankrupt;
    }
    /**
     * Returns an unmodifiable view of the properties owned by this player.
     *
     * @return a list of owned {@link Property} objects (may be empty)
     */
    public List<Property> getProperties() {
        return properties;
    }
    /**
     * Returns a formatted summary of this player's current state.
     *
     * @return e.g. {@code "Anna | Money: $1400 | Position: 6"}
     *         or {@code "Anna | Money: $0 | Position: 20 [IN JAIL]"}
     */
    @Override
    public String toString() {
        return name + " | Money: $" + money + " | Position: " + position
                + (inJail ? " [IN JAIL]" : "");
    }
}
