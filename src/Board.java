import java.util.ArrayList;
import java.util.List;

public class Board {
    private Bank bank;
    public Board(Bank bank) {
        this.bank = bank;
        tiles = new ArrayList<>();
        initializeBoard();
    }
    private List<Tile> tiles;
    public static final int BOARD_SIZE = 40;
    public Board() {
        tiles = new ArrayList<>();
        initializeBoard();
    }
    private void initializeBoard() {
        tiles.add(new GoTile());  
        tiles.add(new Property("KFC", 1, 60, 2, "Red"));
        tiles.add(new CommunityChestTile("Community Chest", 2));
        tiles.add(new Property("McDonald's", 3, 60, 4, "Red"));
        tiles.add(new TaxTile("Income Tax", 4, 200, "Taupe Brown"));
        tiles.add(new MetroStationTile("Marshal Baghramyan", 5, "Beige")); 
        tiles.add(new Property("Coffe House", 6, 100, 6, "Light Blue"));
        tiles.add(new ChanceTile("Chance", 7, 0));
        tiles.add(new Property("Cofix", 8, 100, 6, "Light Blue"));
        tiles.add(new Property("Gotcha", 9, 120, 8, "Light Blue"));
        tiles.add(new FreeTile("Cascade", 10));
        tiles.add(new Property("Zara", 11, 140, 10, "Gray"));
        tiles.add(new TaxTile("Electric Company", 12, 150, "Taupe Brown"));
        tiles.add(new Property("Bershka", 13, 140, 10, "Gray"));
        tiles.add(new Property("Pull & Bear", 14, 160, 12, "Gray"));
        tiles.add(new MetroStationTile("Yeritasardakan", 15, "Beige"));
        tiles.add(new Property("Yerevan Mall", 16, 180, 14, "Orange"));
        tiles.add(new CommunityChestTile("Community Chest", 17));
        tiles.add(new Property("Dalma Mall", 18, 180, 14, "Orange"));
        tiles.add(new Property("Mega Mall", 19, 200, 16, "Orange"));
        tiles.add(new JailTile("Jail", 20));
        tiles.add(new Property("Chanel", 21, 220, 18, "Pink"));
        tiles.add(new ChanceTile("Chance", 22));
        tiles.add(new Property("Dior", 23, 220, 18, "Pink"));
        tiles.add(new Property("Gucci", 24, 240, 20, "Pink"));
        tiles.add(new MetroStationTile("Republic Square", 25, "Beige"));
        tiles.add(new Property("Yerevan Hotel", 26, 260, 22, "Green"));
        tiles.add(new Property("Dilijan Hotel", 27, 260, 22, "Green"));
        tiles.add(new TaxTile("Water Works", 28, 150, "Taupe Brown"));
        tiles.add(new Property("Tsaghkadzor Hotel", 29, 280, 24, "Green"));
        tiles.add(new FreeTile("Republic Square", 30));
        tiles.add(new Property("Paris", 31, 300, 26, "Yellow"));
        tiles.add(new Property("New York", 32, 300, 26, "Yellow"));
        tiles.add(new CommunityChestTile("Community Chest", 33));
        tiles.add(new Property("Oxford University", 34, 320, 28, "Dark Blue"));
        tiles.add(new MetroStationTile("Zoravar Andranik", 35, "Beige"));
        tiles.add(new ChanceTile("Chance", 36));
        tiles.add(new Property("Harvard University", 37, 350, 35, "Dark Blue"));
        tiles.add(new TaxTile("Luxury Tax", 38, 100, "Taupe Brown"));
        tiles.add(new Property("MIT University", 39, 400, 40, "Dark Blue"));
    }
    public Tile getTile(int position) {
        return tiles.get(position);
    }
    public int getSize() {
        return tiles.size();
    }
}
