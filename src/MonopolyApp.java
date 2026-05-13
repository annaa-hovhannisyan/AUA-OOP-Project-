import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
public class BoardPanel extends JPanel {
    static final Map<String, Color> GROUP_COLORS = new LinkedHashMap<>();
    static {
        GROUP_COLORS.put("Red",       new Color(0xd0, 0x8c, 0x60));
        GROUP_COLORS.put("Light Blue",new Color(0x46, 0x8f, 0xaf));
        GROUP_COLORS.put("Gray",      new Color(0xb5, 0x84, 0x63));
        GROUP_COLORS.put("Orange",    new Color(0xff, 0xcb, 0x69));
        GROUP_COLORS.put("Pink",      new Color(0xd9, 0xae, 0x94));
        GROUP_COLORS.put("Green",     new Color(0x9b, 0x9b, 0x7a));
        GROUP_COLORS.put("Yellow",    new Color(0xe8, 0xac, 0x65));
        GROUP_COLORS.put("Dark Blue", new Color(0x79, 0x7d, 0x62));
        GROUP_COLORS.put("Metro",     new Color(0xba, 0xa5, 0x87));
    }
    private static final Color BOARD_BG   = new Color(0xd8, 0xe2, 0xdc);
    private static final Color CENTER_BG  = new Color(0xf1, 0xdc, 0xa7);
    private static final Color BORDER_COL = new Color(0x3a, 0x5a, 0x40);
    private static final Color JAIL_BG    = new Color(0xf2, 0xe8, 0xc9);
    private static final Color CHANCE_BG  = new Color(0xfd, 0xf1, 0xd8);
    private static final Color COMM_BG    = new Color(0xdd, 0xea, 0xf5);
    private static final Color TAX_BG     = new Color(0xf5, 0xdd, 0xe6);
    private static final Color METRO_BG   = new Color(0xed, 0xe7, 0xf6);

    // Player token colours
    static final Color[] PLAYER_COLORS = {
        new Color(0xc1, 0x44, 0x0e),
        new Color(0x5a, 0x3e, 0x8c),
        new Color(0x0a, 0x77, 0x35),
        new Color(0x0d, 0x5c, 0xa6)
    }
    private static final int BOARD_PX   = 724;
    private static final int CORNER_PX  = 80;
    private static final int CELL_PX    = 56;   // (724 - 2*80) / 9 ≈ 56
    private static final int BAR_H      = 10;
    private final List<Tile> tiles;
    private final List<Player> players;
    private final GameState state;
    public BoardPanel(List<Tile> tiles, List<Player> players, GameState state) {
        this.tiles   = tiles;
        this.players = players;
        this.state   = state;
        setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        setBackground(BOARD_BG);
    }
    /** Returns pixel rectangle [x, y, w, h] for board position 0..39 */
    private Rectangle cellRect(int pos) {
        if (pos == 0)  return new Rectangle(BOARD_PX - CORNER_PX, BOARD_PX - CORNER_PX, CORNER_PX, CORNER_PX);
        if (pos <= 9)  { int x = BOARD_PX - CORNER_PX - pos * CELL_PX; return new Rectangle(x, BOARD_PX - CORNER_PX, CELL_PX, CORNER_PX); }
        if (pos == 10) return new Rectangle(0, BOARD_PX - CORNER_PX, CORNER_PX, CORNER_PX);
        if (pos <= 19) { int y = BOARD_PX - CORNER_PX - (pos - 10) * CELL_PX; return new Rectangle(0, y, CORNER_PX, CELL_PX); }
        if (pos == 20) return new Rectangle(0, 0, CORNER_PX, CORNER_PX);
        if (pos <= 29) { int x = CORNER_PX + (pos - 21) * CELL_PX; return new Rectangle(x, 0, CELL_PX, CORNER_PX); }
        if (pos == 30) return new Rectangle(BOARD_PX - CORNER_PX, 0, CORNER_PX, CORNER_PX);
        /* 31..39 */ int y = CORNER_PX + (pos - 31) * CELL_PX; return new Rectangle(BOARD_PX - CORNER_PX, y, CORNER_PX, CELL_PX);
    }
    /** Which edge the colour bar is on */
    private String barSide(int pos) {
        if (pos >= 1  && pos <= 9)  return "top";
        if (pos >= 11 && pos <= 19) return "right";
        if (pos >= 21 && pos <= 29) return "bottom";
        if (pos >= 31 && pos <= 39) return "left";
        return null;
    }
    private Color tileBg(Tile t) {
        if (t instanceof GoTile || t instanceof FreeTile) return BOARD_BG;
        if (t instanceof JailTile) return JAIL_BG;
        if (t instanceof ChanceTile) return CHANCE_BG;
        if (t instanceof CommunityChestTile) return COMM_BG;
        if (t instanceof TaxTile)   return TAX_BG;
        if (t instanceof MetroStationTile) return METRO_BG;
        return BOARD_BG;
    }
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle centre = new Rectangle(CORNER_PX, CORNER_PX,
                BOARD_PX - 2 * CORNER_PX, BOARD_PX - 2 * CORNER_PX);
        g.setColor(CENTER_BG);
        g.fill(centre);
        drawCentreLabel(g, centre);
        for (Tile tile : tiles) {
            drawTile(g, tile);
        }
        drawTokens(g);
        g.setColor(BORDER_COL);
        g.setStroke(new BasicStroke(3f));
        g.drawRect(1, 1, BOARD_PX - 3, BOARD_PX - 3);
    }
    private void drawCentreLabel(Graphics2D g, Rectangle r) {
        g.setColor(BORDER_COL);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        String title = "AUA MONOPOLY";
        g.drawString(title, r.x + (r.width - fm.stringWidth(title)) / 2,
                r.y + r.height / 2 - 30);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        fm = g.getFontMetrics();
        String sub = "Yerevan Edition";
        g.drawString(sub, r.x + (r.width - fm.stringWidth(sub)) / 2,
                r.y + r.height / 2 - 12);
        String turnText = state.isGameOver() ? "Game Over!" :
                (state.getCurrentPlayerName() + "'s turn");
        int bw = Math.max(160, fm.stringWidth(turnText) + 36);
        int bx = r.x + (r.width - bw) / 2;
        int by = r.y + r.height / 2 + 5;
        g.setColor(CENTER_BG);
        g.fillRoundRect(bx, by, bw, 28, 20, 20);
        g.setColor(BORDER_COL);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bx, by, bw, 28, 20, 20);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        fm = g.getFontMetrics();
        g.drawString(turnText, bx + (bw - fm.stringWidth(turnText)) / 2, by + 19);
        int[] dice = state.getDice();
        int dx = r.x + r.width / 2 - 56;
        int dy = by + 42;
        drawDieBox(g, dice[0], dx, dy);
        drawDieBox(g, dice[1], dx + 62, dy);
    }
    private void drawDieBox(Graphics2D g, int val, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, 52, 52, 10, 10);
        g.setColor(BORDER_COL);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, 52, 52, 10, 10);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g.getFontMetrics();
        String s = val <= 0 ? "?" : String.valueOf(val);
        g.drawString(s, x + (52 - fm.stringWidth(s)) / 2, y + 36);
    }
    private void drawTile(Graphics2D g, Tile tile) {
        int pos = tile.getPosition();
        Rectangle r = cellRect(pos);
        g.setColor(tileBg(tile));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(BORDER_COL);
        g.setStroke(new BasicStroke(1f));
        g.drawRect(r.x, r.y, r.width, r.height);
        Color barColor = null;
        if (tile instanceof Property) {
            barColor = GROUP_COLORS.getOrDefault(((Property) tile).getColorGroup(), Color.GRAY);
        } else if (tile instanceof MetroStationTile) {
            barColor = GROUP_COLORS.get("Metro");
        }
        String side = barSide(pos);
        if (barColor != null && side != null) {
            g.setColor(barColor);
            switch (side) {
                case "top"    -> g.fillRect(r.x, r.y, r.width, BAR_H);
                case "bottom" -> g.fillRect(r.x, r.y + r.height - BAR_H, r.width, BAR_H);
                case "left"   -> g.fillRect(r.x, r.y, BAR_H, r.height);
                case "right"  -> g.fillRect(r.x + r.width - BAR_H, r.y, BAR_H, r.height);
            }
        }
        boolean isCorner = (pos == 0 || pos == 10 || pos == 20 || pos == 30);
        if (isCorner) {
            drawCorner(g, tile, r);
            return;
        }
        boolean onLeft  = (pos >= 11 && pos <= 19);
        boolean onRight = (pos >= 31 && pos <= 39);
        Graphics2D gc = (Graphics2D) g.create();
        if (onLeft) {
            gc.rotate(Math.PI / 2, r.x + r.width / 2.0, r.y + r.height / 2.0);
        } else if (onRight) {
            gc.rotate(-Math.PI / 2, r.x + r.width / 2.0, r.y + r.height / 2.0);
        }
        String name = tile.getName();
        Color textCol = new Color(0x2c, 0x3e, 0x30);
        if (tile instanceof ChanceTile)         textCol = new Color(0x7a, 0x4a, 0x00);
        else if (tile instanceof CommunityChestTile) textCol = new Color(0x0d, 0x3b, 0x6b);
        else if (tile instanceof TaxTile)       textCol = new Color(0x6b, 0x00, 0x30);
        gc.setColor(textCol);
        gc.setFont(new Font("Arial", Font.BOLD, 7));
        int textW = onLeft || onRight ? r.height - 4 : r.width - 4;
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        drawWrappedCentered(gc, name, cx, cy - 6, textW);
        String priceStr = null;
        if (tile instanceof Property) priceStr = "$" + ((Property) tile).getPrice();
        else if (tile instanceof MetroStationTile) priceStr = "$200";
        else if (tile instanceof TaxTile) priceStr = "$" + ((TaxTile) tile).getTaxAmount();
        if (priceStr != null) {
            gc.setColor(new Color(0x55, 0x55, 0x55));
            gc.setFont(new Font("Arial", Font.PLAIN, 7));
            FontMetrics fm = gc.getFontMetrics();
            gc.drawString(priceStr, cx - fm.stringWidth(priceStr) / 2, cy + 10);
        }
        if (tile instanceof Property) {
            Player owner = ((Property) tile).getOwner();
            if (owner != null) {
                int pi = players.indexOf(owner);
                if (pi >= 0) {
                    gc.setColor(PLAYER_COLORS[pi % PLAYER_COLORS.length]);
                    gc.fillOval(r.x + r.width - 10, r.y + 2, 8, 8);
                    gc.setColor(Color.WHITE);
                    gc.setStroke(new BasicStroke(1f));
                    gc.drawOval(r.x + r.width - 10, r.y + 2, 8, 8);
                }
            }
        }
        gc.dispose();
    }
    private void drawCorner(Graphics2D g, Tile tile, Rectangle r) {
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        g.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        if (tile instanceof GoTile) {
            g.setColor(BORDER_COL);
            String go = "GO";
            g.drawString(go, cx - fm.stringWidth(go) / 2, cy - 4);
            g.setColor(GROUP_COLORS.get("Red"));
            g.fillOval(cx - 14, cy + 6, 10, 10);
            g.setColor(GROUP_COLORS.get("Dark Blue"));
            g.fillOval(cx + 4, cy + 6, 10, 10);
        } else if (tile instanceof JailTile) {
            g.setColor(new Color(0x6b, 0x59, 0x00));
            g.setFont(new Font("Arial", Font.BOLD, 10));
            fm = g.getFontMetrics();
            g.drawString("JAIL", cx - fm.stringWidth("JAIL") / 2, cy - 4);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            fm = g.getFontMetrics();
            String sub = "Just Visiting";
            g.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 8);
        } else if (tile instanceof FreeTile) {
            g.setColor(BORDER_COL);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            fm = g.getFontMetrics();
            g.drawString("Cascade", cx - fm.stringWidth("Cascade") / 2, cy - 3);
            g.drawString("Complex", cx - fm.stringWidth("Complex") / 2, cy + 9);
        } else { // Go To Jail (pos 30)
            g.setColor(new Color(0x6b, 0x59, 0x00));
            g.setFont(new Font("Arial", Font.BOLD, 9));
            fm = g.getFontMetrics();
            g.drawString("Go To", cx - fm.stringWidth("Go To") / 2, cy - 3);
            g.drawString("Jail", cx - fm.stringWidth("Jail") / 2, cy + 9);
        }
    }
    private void drawTokens(Graphics2D g) {
        Map<Integer, List<Integer>> byPos = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.isBankrupt()) continue;
            byPos.computeIfAbsent(p.getPosition(), k -> new ArrayList<>()).add(i);
        }
        for (var entry : byPos.entrySet()) {
            Rectangle r = cellRect(entry.getKey());
            List<Integer> pis = entry.getValue();
            int n = pis.size();
            for (int k = 0; k < n; k++) {
                int pi = pis.get(k);
                int tx = r.x + r.width  - 20 - (k % 2) * 18;
                int ty = r.y + r.height - 20 - (k / 2) * 18;
                drawToken(g, pi, tx, ty, 16);
            }
        }
    }
    private void drawToken(Graphics2D g, int playerIndex, int x, int y, int size) {
        Color c = PLAYER_COLORS[playerIndex % PLAYER_COLORS.length];
        g.setColor(c);
        g.fillOval(x, y, size, size);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, size, size);
        g.setFont(new Font("Arial", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        String init = players.get(playerIndex).getName().substring(0, 1);
        g.drawString(init, x + (size - fm.stringWidth(init)) / 2, y + size - 4);
    }
    /** Draw multi-line centred text within given pixel width */
    private void drawWrappedCentered(Graphics2D g, String text, int cx, int cy, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String test = cur.isEmpty() ? w : cur + " " + w;
            if (fm.stringWidth(test) > maxW && !cur.isEmpty()) {
                lines.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        int lineH = fm.getHeight();
        int totalH = lines.size() * lineH;
        int startY = cy - totalH / 2 + fm.getAscent();
        for (String line : lines) {
            g.drawString(line, cx - fm.stringWidth(line) / 2, startY);
            startY += lineH;
        }
    }
}
