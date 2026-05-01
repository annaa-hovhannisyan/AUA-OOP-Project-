import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<Tile> tiles;
    public static final int BOARD_SIZE = 40;

    public Board() {
        tiles = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        // Position 0: GO
        tiles.add(new GoTile());

        // Position 1-9: properties and taxes
        tiles.add(new Property("Mediterranean Avenue", 1, 60, 2, "Brown"));
        tiles.add(new TaxTile("Community Chest", 2, 0));  // simplified
        tiles.add(new Property("Baltic Avenue", 3, 60, 4, "Brown"));
        tiles.add(new TaxTile("Income Tax", 4, 200));
        // ... continue for all 40 squares
        // Position 10: Jail
        tiles.add(new JailTile());
        // Position 30: Go To Jail
        tiles.add(new GoToJailTile());
        // Fill remaining with basic properties
    }

    public Tile getTile(int position) {
        return tiles.get(position);
    }

    public int getSize() {
        return tiles.size();
    }
}
