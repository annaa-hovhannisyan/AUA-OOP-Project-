/**
 * Main entry point. Creates the Swing window, side panel, and wires
 * GameState + SwingGame + BoardPanel together.
 *
 * Compile all files together and run:
 *   javac *.java
 *   java MonopolyApp
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class MonopolyApp {

    /**
     * Tracks whether the end-of-game winner dialog has already been shown.
     * Prevents the dialog from appearing more than once when fireChange()
     * is called multiple times after the game ends.
     */
    private static boolean winDialogShown = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MonopolyApp::launch);
    }

    private static void launch() {
        String[] names = showSetupDialog();
        if (names == null) System.exit(0);   // user cancelled

        // --- Build model layer ---
        GameState state = new GameState();
        SwingGame game  = new SwingGame(state, List.of(names));

        // --- Board panel (left) ---
        BoardPanel board = new BoardPanel(state.getTiles(), state.getPlayers(), state);

        // --- Side panel (right) ---
        JPanel side = buildSidePanel(state, game, board);

        // --- Main frame ---
        JFrame frame = new JFrame("AUA Monopoly");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 0));
        frame.getContentPane().setBackground(new Color(0x3a, 0x5a, 0x40));
        frame.add(board, BorderLayout.CENTER);
        frame.add(side,  BorderLayout.EAST);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        state.addChangeListener(() -> {
            board.repaint();
            refreshSide(state, side);

            if (state.isGameOver() && !winDialogShown) {
                winDialogShown = true;
                if (state.getBankFunds() <= 0) {
                    // Bank-empty ending: winner is the player with the most properties.
                    showBankEmptyWinnerDialog(frame, state, game);
                } else {
                    // Normal ending: all other players went bankrupt.
                    showBankruptWinnerDialog(frame, state);
                }
            }
        });
    }

    /**
     * Shows a dialog when the bank runs out of money.
     * The winner is the non-bankrupt player who owns the most properties
     * (Properties + MetroStationTiles). All players are listed in descending
     * property-count order below the headline, with bankrupt players shown
     * at the bottom.
     *
     * @param parent the parent frame for the modal dialog
     * @param state  the shared game state
     * @param game   the game logic driver, used to count each player's properties
     */
    private static void showBankEmptyWinnerDialog(JFrame parent, GameState state, SwingGame game) {
        // Sort active players: descending by property count, then by cash balance.
        List<Player> sorted = state.getPlayers().stream()
                .filter(p -> !p.isBankrupt())
                .sorted(Comparator
                        .comparingInt((Player p) -> game.countAllProperties(p))
                        .thenComparingInt(Player::getMoney)
                        .reversed())
                .toList();

        if (sorted.isEmpty()) return;

        Player winner     = sorted.get(0);
        String winnerName = winner.getName();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0xf1, 0xdc, 0xa7));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        // Trophy icon
        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(trophy);
        panel.add(Box.createVerticalStrut(8));

        JLabel headline = new JLabel(winnerName + " won. Congratulations!", SwingConstants.CENTER);
        headline.setFont(new Font("Arial", Font.BOLD, 20));
        headline.setForeground(new Color(0x3a, 0x5a, 0x40));
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(headline);
        panel.add(Box.createVerticalStrut(4));

        // Sub-note explaining why the game ended
        JLabel bankNote = new JLabel("(The bank ran out of money)", SwingConstants.CENTER);
        bankNote.setFont(new Font("Arial", Font.ITALIC, 12));
        bankNote.setForeground(new Color(0x88, 0x66, 0x33));
        bankNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bankNote);
        panel.add(Box.createVerticalStrut(18));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xb5, 0x84, 0x63));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(14));

        // One row per active player in descending property-count order.
        // Format: "{Name}: {N} properties"
        for (int i = 0; i < sorted.size(); i++) {
            Player p   = sorted.get(i);
            int    cnt = game.countAllProperties(p);
            panel.add(makePropertyRow(p, cnt, i, state));
            panel.add(Box.createVerticalStrut(6));
        }

        // Bankrupt players listed below a second divider with 0 properties.
        List<Player> bankrupt = state.getPlayers().stream()
                .filter(Player::isBankrupt)
                .toList();
        if (!bankrupt.isEmpty()) {
            panel.add(Box.createVerticalStrut(4));
            JSeparator sep2 = new JSeparator();
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep2.setForeground(new Color(0xcc, 0xcc, 0xcc));
            panel.add(sep2);
            panel.add(Box.createVerticalStrut(6));
            for (Player p : bankrupt) {
                JPanel row = new JPanel(new BorderLayout(20, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
                JLabel nameLabel = new JLabel("      " + p.getName() + " [bankrupt]");
                nameLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                nameLabel.setForeground(Color.GRAY);
                JLabel propLabel = new JLabel("0 properties", SwingConstants.RIGHT);
                propLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                propLabel.setForeground(Color.GRAY);
                row.add(nameLabel, BorderLayout.WEST);
                row.add(propLabel, BorderLayout.EAST);
                panel.add(row);
                panel.add(Box.createVerticalStrut(4));
            }
        }

        JOptionPane.showMessageDialog(
                parent,
                panel,
                "Game Over – Bank is Empty!",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Shows a dialog when the game ends because all other players went bankrupt.
     * Displays a simple congratulations message naming the last surviving player.
     *
     * @param parent the parent frame for the modal dialog
     * @param state  the shared game state, used to read the winner's name
     */
    private static void showBankruptWinnerDialog(JFrame parent, GameState state) {
        String winnerName = state.getWinnerName();
        if (winnerName == null) return;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0xf1, 0xdc, 0xa7));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        // Trophy icon
        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(trophy);
        panel.add(Box.createVerticalStrut(10));

        // Headline: "{WinnerName} won. Congratulations!"
        JLabel headline = new JLabel(winnerName + " won. Congratulations!", SwingConstants.CENTER);
        headline.setFont(new Font("Arial", Font.BOLD, 20));
        headline.setForeground(new Color(0x3a, 0x5a, 0x40));
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(headline);
        panel.add(Box.createVerticalStrut(4));

        // Sub-note explaining how the game ended
        JLabel note = new JLabel("(All other players went bankrupt)", SwingConstants.CENTER);
        note.setFont(new Font("Arial", Font.ITALIC, 12));
        note.setForeground(new Color(0x88, 0x66, 0x33));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(note);

        JOptionPane.showMessageDialog(
                parent,
                panel,
                "Game Over!",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Builds a single player row for the bank-empty winner dialog.
     * Shows a crown icon for rank 0, the player's name, and their property count.
     *
     * @param p the player
     * @param cnt number of properties owned by the player
     * @param rank position in the sorted list (0 = winner)
     * @param state game state, used to look up the player's original index for colour
     * @return a configured JPanel row
     */
    private static JPanel makePropertyRow(Player p, int cnt, int rank, GameState state) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        int originalIdx = state.getPlayers().indexOf(p);
        Color dotColor  = (originalIdx >= 0 && originalIdx < BoardPanel.PLAYER_COLORS.length)
                ? BoardPanel.PLAYER_COLORS[originalIdx]
                : Color.GRAY;

        JLabel nameLabel = new JLabel((rank == 0 ? "👑  " : "      ") + p.getName());
        nameLabel.setFont(new Font("Arial", rank == 0 ? Font.BOLD : Font.PLAIN, 14));
        nameLabel.setForeground(rank == 0 ? new Color(0x3a, 0x5a, 0x40) : new Color(0x44, 0x44, 0x44));

        JLabel propLabel = new JLabel(cnt + " propert" + (cnt == 1 ? "y" : "ies"),
                SwingConstants.RIGHT);
        propLabel.setFont(new Font("Arial", rank == 0 ? Font.BOLD : Font.PLAIN, 14));
        propLabel.setForeground(dotColor);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(propLabel, BorderLayout.EAST);
        return row;
    }

    /**
     * Shows a simple input dialog for players to enter their names before the game starts.
     *
     * @return an array of four player name strings, or null if the user cancelled
     */
    private static String[] showSetupDialog() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        JTextField[] fields = new JTextField[4];
        String[] defaults = {"Alice", "Bob", "Carol", "Dave"};
        for (int i = 0; i < 4; i++) {
            p.add(new JLabel("Player " + (i + 1) + ":"));
            fields[i] = new JTextField(defaults[i], 12);
            p.add(fields[i]);
        }
        int r = JOptionPane.showConfirmDialog(null, p,
                "AUA Monopoly – Player Setup", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        String[] names = new String[4];
        for (int i = 0; i < 4; i++) {
            names[i] = fields[i].getText().trim();
            if (names[i].isEmpty()) names[i] = "Player " + (i + 1);
        }
        return names;
    }

    /**
     * Builds the right-hand side panel containing player cards, action buttons,
     * the bank balance label, and the scrolling game log. The panel is built
     * once and its named child components are updated by {@link #refreshSide}
     * on every state change.
     *
     * @param state the shared game state
     * @param game the game logic driver, used to wire button actions
     * @param board the board panel (passed through from launch for structural symmetry)
     * @return the fully constructed side panel
     */
    private static JPanel buildSidePanel(GameState state, SwingGame game, BoardPanel board) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(0x2c, 0x3e, 0x30));
        side.setPreferredSize(new Dimension(240, 724));
        side.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        // Title
        JLabel title = new JLabel("AUA MONOPOLY");
        title.setForeground(new Color(0xf1, 0xdc, 0xa7));
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(title);
        side.add(Box.createVerticalStrut(10));

        // Player cards area (tagged so refreshSide can replace its contents)
        JPanel playersPanel = new JPanel();
        playersPanel.setName("playersPanel");
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);
        side.add(playersPanel);
        side.add(Box.createVerticalStrut(10));

        // Action buttons
        JButton rollBtn = new JButton("🎲  Roll Dice");
        JButton buyBtn = new JButton("✅  Buy");
        JButton skipBtn = new JButton("❌  Skip");
        styleButton(rollBtn, new Color(0xe8, 0xac, 0x65));
        styleButton(buyBtn, new Color(0x9b, 0xc4, 0x95));
        styleButton(skipBtn, new Color(0xe0, 0x7a, 0x5f));
        rollBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyBtn .setAlignmentX(Component.CENTER_ALIGNMENT);
        skipBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        rollBtn.addActionListener(e -> game.doRoll());
        buyBtn .addActionListener(e -> game.doBuy(true));
        skipBtn.addActionListener(e -> game.doBuy(false));

        side.add(rollBtn);
        side.add(Box.createVerticalStrut(4));
        side.add(buyBtn);
        side.add(Box.createVerticalStrut(4));
        side.add(skipBtn);
        side.add(Box.createVerticalStrut(10));

        // Buy prompt label — shows the pending purchase offer, or blank otherwise
        JLabel promptLbl = new JLabel(" ");
        promptLbl.setName("promptLbl");
        promptLbl.setForeground(new Color(0xf1, 0xdc, 0xa7));
        promptLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        promptLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(promptLbl);
        side.add(Box.createVerticalStrut(8));

        // Bank funds label
        JLabel bankLbl = new JLabel("Bank: $" + state.getBankFunds());
        bankLbl.setName("bankLbl");
        bankLbl.setForeground(new Color(0xb5, 0xd5, 0xb5));
        bankLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        bankLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(bankLbl);
        side.add(Box.createVerticalStrut(8));

        // Scrollable game log
        JTextArea logArea = new JTextArea(12, 20);
        logArea.setName("logArea");
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(0x1a, 0x2a, 0x1f));
        logArea.setForeground(new Color(0xd8, 0xe2, 0xdc));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x3a, 0x5a, 0x40)));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(scroll);

        // Initial fill
        refreshSide(state, side);
        return side;
    }

    /**
     * Called every time GameState fires a change event.
     * Walks the side panel's component tree by name and updates only the
     * dynamic parts: player cards, the prompt label, the bank label, and the log.
     * Also enables or disables the action buttons to match the current game phase.
     *
     * @param state the shared game state to read from
     * @param side  the side panel whose named children are refreshed
     */
    private static void refreshSide(GameState state, JPanel side) {
        boolean isBuy = state.getPhase() == GameState.Phase.BUY;
        boolean isRoll = state.getPhase() == GameState.Phase.ROLL;
        boolean over = state.isGameOver();

        for (Component c : allComponents(side)) {
            switch (c.getName() == null ? "" : c.getName()) {

                case "playersPanel" -> {
                    JPanel pp = (JPanel) c;
                    pp.removeAll();
                    for (int i = 0; i < state.getPlayers().size(); i++) {
                        Player p = state.getPlayers().get(i);
                        boolean current = (i == state.players.indexOf(state.currentPlayer()));
                        pp.add(makePlayerCard(p, i, current));
                        pp.add(Box.createVerticalStrut(4));
                    }
                    pp.revalidate();
                    pp.repaint();
                }

                case "promptLbl" -> {
                    JLabel lbl = (JLabel) c;
                    if (over) {
                        lbl.setText(state.getBankFunds() <= 0
                                ? "🏦 Bank is empty — game over!"
                                : "🏆 " + state.getWinnerName() + " wins!");
                    } else if (isBuy && state.getPendingBuy() != null) {
                        Tile t = state.getPendingBuy();
                        int price = t instanceof Property ? ((Property) t).getPrice() : 200;
                        lbl.setText("Buy " + t.getName() + " for $" + price + "?");
                    } else {
                        lbl.setText(" ");
                    }
                }

                case "bankLbl" -> {
                    JLabel lbl = (JLabel) c;
                    int funds = state.getBankFunds();
                    lbl.setText("Bank: $" + funds);
                    // Colour the bank label: green → yellow (low funds) → red (empty)
                    lbl.setForeground(funds <= 0
                            ? new Color(0xe0, 0x5f, 0x5f)
                            : funds < 500
                            ? new Color(0xff, 0xd0, 0x60)
                            : new Color(0xb5, 0xd5, 0xb5));
                }

                case "logArea" -> {
                    JTextArea ta = (JTextArea) c;
                    StringBuilder sb = new StringBuilder();
                    for (String line : state.getLog()) sb.append(line).append("\n");
                    ta.setText(sb.toString());
                    ta.setCaretPosition(0);
                }
            }

            // Enable/disable action buttons based on the current game phase
            if (c instanceof JButton btn) {
                switch (btn.getText()) {
                    case "🎲  Roll Dice" -> btn.setEnabled(isRoll && !over);
                    case "✅  Buy" -> btn.setEnabled(isBuy  && !over);
                    case "❌  Skip" -> btn.setEnabled(isBuy  && !over);
                }
            }
        }
    }

    /**
     * Builds a player card for the side panel's player list.
     * The card shows a coloured dot, the player's name, any status indicator
     * (bankrupt or in jail), and their current cash balance.
     *
     * @param p  the player to represent
     * @param idx the player's index, used to pick the colour dot
     * @param current true if this is the active player whose turn it is
     * @return a configured JPanel card
     */
    private static JPanel makePlayerCard(Player p, int idx, boolean current) {
        JPanel card = new JPanel(new BorderLayout(4, 0));
        card.setBackground(current
                ? new Color(0x3d, 0x5a, 0x45)
                : new Color(0x22, 0x33, 0x27));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(current ? new Color(0xe8, 0xac, 0x65) : new Color(0x3a, 0x5a, 0x40), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        card.setMaximumSize(new Dimension(220, 48));

        // Coloured dot matching the player's board token
        JLabel dot = new JLabel("●");
        dot.setForeground(BoardPanel.PLAYER_COLORS[idx % BoardPanel.PLAYER_COLORS.length]);
        dot.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(dot, BorderLayout.WEST);

        // Name + status + balance
        String status = p.isBankrupt() ? " [BANKRUPT]" : (p.isInJail() ? " 🔒" : "");
        JLabel info = new JLabel("<html><b>" + p.getName() + "</b>" + status
                + "<br><font color='#aaccaa'>$" + p.getMoney() + "</font></html>");
        info.setForeground(Color.WHITE);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    /**
     * Applies a consistent visual style to an action button.
     *
     * @param btn the button to style
     * @param bg the background colour to apply
     */
    private static void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(new Color(0x1a, 0x2a, 0x1f));
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 36));
    }

    /**
     * Recursively collects all components inside a container, depth-first.
     * Used to find named child components anywhere in the side panel tree.
     *
     * @param root the container to traverse
     * @return a flat list of every component inside root
     */
    private static java.util.List<Component> allComponents(Container root) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        for (Component c : root.getComponents()) {
            list.add(c);
            if (c instanceof Container cont) list.addAll(allComponents(cont));
        }
        return list;
    }
}
