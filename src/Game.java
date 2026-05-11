import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Game {
    private Board board;
    private Dice dice;
    private List<Player> players;
    private int currentPlayerIndex;
    private Scanner scanner;
    private Bank bank;  // added
    public Game() {
        bank = new Bank();
        board = new Board(bank);
        dice = new Dice();
        players = new ArrayList<>();
        currentPlayerIndex = 0;
        scanner = new Scanner(System.in);
    }

    public void setup() {
        System.out.print("Enter number of players (2-4): ");
        int numPlayers = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            Player player = new Player(name, 1500);
            bank.collect(player, 0); // register player with bank (optional)
            players.add(player);
        }
    }
    public void play() {
        System.out.println("Game started!");
        while (!isGameOver()) {
            takeTurn(players.get(currentPlayerIndex));
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            players.removeIf(Player::isBankrupt);
        }
        announceWinner();
    }
    private void takeTurn(Player player) {
        System.out.println("\n--- " + player.getName() + "'s turn ---");
        System.out.println(player);
        System.out.println("Press ENTER to roll dice...");
        scanner.nextLine();
        int steps = dice.roll();
        int oldPosition = player.getPosition();  // save position before moving
        player.move(steps, board.getSize());
        int newPosition = player.getPosition();
        if (newPosition < oldPosition || newPosition == 0) {
            bank.payGoSalary(player);
        }
        Tile currentTile = board.getTile(newPosition);
        System.out.println("Landed on: " + currentTile.getName());
        currentTile.landOn(player);
    }
    private boolean isGameOver() {
        return players.size() == 1;
    }
    private void announceWinner() {
        if (!players.isEmpty()) {
            System.out.println("\n🏆 " + players.get(0).getName() + " wins!" + " Final balance: $" + players.get(0).getMoney());
            System.out.println("Bank remaining funds: $" + bank.getFunds());
        }
    }
    public Bank getBank() {
        return bank;
    }
}
