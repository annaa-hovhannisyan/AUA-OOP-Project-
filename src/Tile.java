/**
 * Abstract base class representing a single space on the Monopoly board.
 *
 * <p>Every tile on the 40-space board is a subclass of {@code Tile}.
 * Subclasses must implement {@link #landOn(Player)} to define what happens
 * when a player lands on that specific tile type.</p>
 *
 * <p>Tile types in this game:</p>
 * <ul>
 *   <li>{@link GoTile} – position 0, collect $200</li>
 *   <li>{@link Property} – buyable color-group properties</li>
 *   <li>{@link MetroStationTile} – buyable metro stations</li>
 *   <li>{@link TaxTile} – forces the player to pay a fixed amount</li>
 *   <li>{@link ChanceTile} – draws a Chance card</li>
 *   <li>{@link CommunityChestTile} – draws a Community Chest card</li>
 *   <li>{@link JailTile} – Jail / Just Visiting space</li>
 *   <li>{@link FreeTile} – free rest space (no effect)</li>
 * </ul>
 *
 * @author Anna Hovhannisyan
 * @author Seda Hovhannisyan
 * @version 1.0
 * @see Property
 * @see MetroStationTile
 */
public abstract class Tile {
    /** The display name of this tile (e.g. "KFC", "Chance", "GO"). */
    private String name;
    /** The 0-based index of this tile on the board (0–39). */
    private int position;
    /**
     * Constructs a Tile with a given name and board position.
     *
     * @param name     the display name of the tile; must not be {@code null}
     * @param position the 0-based board position (0–39)
     */
    public Tile(String name, int position) {
        this.name = name;
        this.position = position;
    }
    /**
     * Returns the display name of this tile.
     *
     * @return the tile name, e.g. {@code "Harvard University"}
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the 0-based position of this tile on the board.
     *
     * @return an integer in the range [0, 39]
     */
    public int getPosition() {
        return position;
    }
    /**
     * Executes the effect of landing on this tile.
     *
     * <p>Each subclass defines its own behaviour — buying a property,
     * paying tax, drawing a card, going to jail, etc.</p>
     *
     * @param player the {@link Player} who has just landed on this tile;
     *               must not be {@code null}
     */
    public abstract void landOn(Player player);
 
    /**
     * Returns a human-readable representation of this tile.
     *
     * @return a string in the format {@code "Name (position N)"},
     *         e.g. {@code "MIT University (position 39)"}
     */
    @Override
    public String toString() {
        return name + " (position " + position + ")";
    }
}
