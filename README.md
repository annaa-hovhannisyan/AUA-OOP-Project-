# Monopoly Game
Group Project, Introduction to Object-Oriented Programming, AUA  
Authors: Anna Hovhannisyan and Seda Hovhannisyan  
Instructor: Varduhi Yeghiazaryan

## Description
This is a Java implementation of the game Monopoly.

# AUA Monopoly – Yerevan Edition
## Comprehensive Project Documentation

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Overview](#2-architecture-overview)
3. [Class Reference](#3-class-reference)
   - 3.1 [Bank](#31-bank)
   - 3.2 [Board](#32-board)
   - 3.3 [Player](#33-player)
   - 3.4 [Dice](#34-dice)
   - 3.5 [Game](#35-game)
   - 3.6 [GameState](#36-gamestate)
   - 3.7 [SwingGame](#37-swinggame)
   - 3.8 [MonopolyApp](#38-monopolyapp)
   - 3.9 [BoardPanel](#39-boardpanel)
   - 3.10 [CardDeck](#310-carddeck)
   - 3.11 [Tile Hierarchy](#311-tile-hierarchy)
4. [Game Rules & Logic](#4-game-rules--logic)
5. [Board Layout](#5-board-layout)
6. [Data Flow & Interaction Diagrams](#6-data-flow--interaction-diagrams)
7. [UI Architecture](#7-ui-architecture)
8. [Known Issues & Design Notes](#8-known-issues--design-notes)
9. [How to Build & Run](#9-how-to-build--run)

---

## 1. Project Overview

AUA Monopoly – Yerevan Edition is a Java-based Monopoly adaptation set in Yerevan, Armenia. It features a fully playable board game with a Swing graphical user interface alongside a legacy text-based (console) mode.

The board uses familiar Monopoly mechanics — property ownership, rent collection, taxes, chance and community chest cards, a metro station system (railways), and a jail — but all locations are replaced with Yerevan landmarks, shops, hotels, universities, and metro stations.

**Key characteristics:**

- 40-tile board matching classic Monopoly structure
- 2–4 players, each starting with $1,500
- A bank with a finite $20,580 pool
- Swing GUI with a live game log, player status cards, and an interactive board panel
- Fallback console (text) mode via `Main` → `Game`
- Card decks for Chance and Community Chest with 25 cards each
- Jail mechanic: miss up to 3 turns; if two or more players are jailed simultaneously, all are released

---

## 2. Architecture Overview

The project is split into two parallel execution paths that share the same core model classes:

```
┌─────────────────────────────────────────────────────────┐
│                     CORE MODEL LAYER                    │
│  Board · Tile hierarchy · Player · Dice · Bank          │
│  CardDeck                                               │
└────────────────┬────────────────────────────────────────┘
                 │ shared by both paths
       ┌─────────┴──────────┐
       │                    │
┌──────▼──────┐      ┌──────▼──────────────────────────┐
│  Console    │      │         Swing GUI Path           │
│  Path       │      │                                  │
│             │      │  GameState  ◄──── SwingGame      │
│  Main       │      │     │                 │          │
│   └─ Game   │      │  BoardPanel    MonopolyApp       │
└─────────────┘      └─────────────────────────────────-┘
```

### Layer responsibilities

| Layer | Classes | Responsibility |
|---|---|---|
| Core model | `Board`, `Tile` subtypes, `Player`, `Dice`, `Bank`, `CardDeck` | Game rules, state mutation, business logic |
| Console driver | `Main`, `Game` | Text-based I/O loop |
| GUI model bridge | `GameState` | Mutable shared state + change notification |
| GUI controller | `SwingGame` | Translates UI button presses into model calls; updates `GameState` |
| GUI views | `MonopolyApp`, `BoardPanel` | Swing rendering, layout, event wiring |

---

## 3. Class Reference

---

### 3.1 `Bank`

**File:** `Bank.java`  
**Role:** Central financial authority. Tracks a finite pool of funds and mediates all money transfers between itself and players.

#### Fields

| Field | Type | Value | Description |
|---|---|---|---|
| `funds` | `int` | (runtime) | Current bank balance |
| `INITIAL_FUNDS` | `static final int` | `20,580` | Starting bank balance |

#### Methods

| Method | Parameters | Description |
|---|---|---|
| `Bank()` | — | Initialises `funds` to `INITIAL_FUNDS` |
| `pay(player, amount)` | `Player`, `int` | Transfers `amount` from bank to player, if funds allow |
| `collect(player, amount)` | `Player`, `int` | Subtracts `amount` from player and adds to bank; guards against `amount <= 0` |
| `buyProperty(player, property)` | `Player`, `Property` | Validates affordability, calls `collect`, sets property owner and adds to player's list |
| `payGoSalary(player)` | `Player` | Pays exactly $200 to a player passing or landing on GO |
| `collectTax(player, amount, reason)` | `Player`, `int`, `String` | Collects a tax; logs the reason |
| `collectMetroTicket(player)` | `Player` | Collects exactly $25 for a metro fare |
| `applyCard(player, amount)` | `Player`, `int` | Pays or collects depending on sign of `amount`; zero is a no-op |
| `getFunds()` | — | Returns current bank balance |

#### Design notes

- `collect` short-circuits on `amount <= 0` to prevent spurious zero-dollar transactions from logging noise.
- `buyProperty` is the canonical purchase path for `Property` tiles; `MetroStationTile` purchases are handled differently in `SwingGame` (see §3.7).
- The bank can go insolvent (`funds < amount` in `pay`), but the game does not currently handle this as a terminal condition; it simply prints a warning.

---

### 3.2 `Board`

**File:** `Board.java`  
**Role:** Constructs and owns the ordered list of all 40 tiles.

#### Fields

| Field | Type | Description |
|---|---|---|
| `tiles` | `List<Tile>` | Ordered tile list (index = board position) |
| `bank` | `Bank` | Reference passed in at construction |
| `BOARD_SIZE` | `static final int` | `40` |

#### Methods

| Method | Returns | Description |
|---|---|---|
| `Board(bank)` | — | Calls `initializeBoard()` |
| `getTile(position)` | `Tile` | Returns tile at given index |
| `getSize()` | `int` | Returns `40` |
| `getBank()` | `Bank` | Returns the bank reference |

#### `initializeBoard()`

Populates the tile list in order from position 0 to 39. Each tile is constructed inline with its name, position, price/rent/tax, and color group. See §5 for the full board layout table.

---

### 3.3 `Player`

**File:** `Player.java`  
**Role:** Represents a single player's full game state: money, position, jail status, property list, and bankruptcy flag.

#### Fields

| Field | Type | Description |
|---|---|---|
| `name` | `String` | Display name |
| `money` | `int` | Current balance |
| `position` | `int` | Current board position (0–39) |
| `inJail` | `boolean` | Jail flag |
| `jailTurns` | `int` | Consecutive turns spent in jail |
| `properties` | `List<Property>` | Owned properties |
| `bankrupt` | `boolean` | Set permanently to `true` when `money` reaches 0 |

#### Methods

| Method | Description |
|---|---|
| `move(steps, boardSize)` | Advances position by `steps` modulo `boardSize` (wraps around) |
| `addMoney(amount)` | Increases balance |
| `subtractMoney(amount)` | Decreases balance; sets `bankrupt = true` and clamps to `0` if balance goes non-positive |
| `addProperty(p)` | Appends to the property list |
| `goToJail(jailPosition)` | Sets `inJail = true`, resets `jailTurns = 0`, teleports player |
| `incrementJailTurns()` | Increments `jailTurns` counter |
| `releaseFromJail()` | Clears `inJail` and resets `jailTurns` |
| `getters / setters` | Standard accessors for all fields |

#### Bankruptcy logic

`subtractMoney` is the only place `bankrupt` is set. Once set it is never cleared. The player is removed from active play in the console path (`Game.play()` calls `players.removeIf(Player::isBankrupt)`) and skipped in the Swing path (turn loop in `SwingGame.nextTurn()` skips bankrupt players).

---

### 3.4 `Dice`

**File:** `Dice.java`  
**Role:** Simulates a pair of six-sided dice.

#### Fields

| Field | Type | Description |
|---|---|---|
| `random` | `Random` | Seeded at construction |
| `lastRoll1` | `int` | Value of die 1 after last roll |
| `lastRoll2` | `int` | Value of die 2 after last roll |

#### Methods

| Method | Returns | Description |
|---|---|---|
| `roll()` | `int` | Rolls both dice; returns their sum; prints result |
| `isDoubles()` | `boolean` | `true` if both dice show the same value |
| `getDie1()` | `int` | Last value of die 1 |
| `getDie2()` | `int` | Last value of die 2 |

---

### 3.5 `Game`

**File:** `Game.java`  
**Role:** Console game driver. Manages the text-based game loop, player setup, and turn sequence.

#### Fields

| Field | Type | Description |
|---|---|---|
| `board` | `Board` | The game board |
| `dice` | `Dice` | Dice instance |
| `players` | `List<Player>` | Active players (bankrupt players removed in-loop) |
| `currentPlayerIndex` | `int` | Points to the current player in `players` |
| `scanner` | `Scanner` | `System.in` reader |
| `bank` | `Bank` | Shared bank |

#### Methods

| Method | Description |
|---|---|
| `Game()` | Constructs all components |
| `setup()` | Prompts for player count (2–4) and names; initialises `Player` objects with $1,500 |
| `play()` | Main loop; calls `takeTurn` per player until `isGameOver()` |
| `takeTurn(player)` | Handles jail check, dice roll, position update, GO salary, and `tile.landOn()` |
| `isGameOver()` | Returns `true` when `players.size() <= 1` |
| `announceWinner()` | Prints winner name, balance, and bank funds |
| `getBank()` | Returns the bank |

#### Turn flow (console)

```
takeTurn(player)
  │
  ├─ isInJail? → handleJailTurn (JailTile)
  │     ├─ still jailed? → return (miss turn)
  │     └─ released? → continue
  │
  ├─ Press ENTER to roll
  ├─ dice.roll()
  ├─ player.move(steps, boardSize)
  ├─ passed GO? → bank.payGoSalary(player)
  └─ tile.landOn(player)
```

---

### 3.6 `GameState`

**File:** `GameState.java`  
**Role:** Observable shared model for the Swing GUI. All mutable game state lives here; `SwingGame` writes it, UI components read it.

#### Enum `Phase`

| Value | Meaning |
|---|---|
| `ROLL` | Waiting for the current player to roll |
| `BUY` | Waiting for a buy/skip decision on `pendingBuy` |
| `GAME_OVER` | The game has ended |

#### Fields (package-visible)

| Field | Type | Description |
|---|---|---|
| `players` | `List<Player>` | All players (including bankrupt) |
| `tiles` | `List<Tile>` | Mirror of `Board`'s tile list |
| `currentIdx` | `int` | Index into `players` of the active player |
| `dice` | `int[]` | Last rolled values `{die1, die2}` |
| `phase` | `Phase` | Current game phase |
| `pendingBuy` | `Tile` | Tile awaiting buy/skip (null otherwise) |
| `lastDoubles` | `boolean` | Whether the last roll was doubles |
| `bankFunds` | `int` | Snapshot of `Bank.getFunds()` |
| `log` | `List<String>` | Game event log (newest first, max 60 entries) |
| `gameOver` | `boolean` | Terminal state flag |
| `winnerName` | `String` | Name of the winner (null if no winner) |

#### Key methods

| Method | Description |
|---|---|
| `addChangeListener(Runnable)` | Registers a UI refresh callback |
| `fireChange()` | Calls all listeners via `SwingUtilities.invokeLater` |
| `currentPlayer()` | Returns `players.get(currentIdx)` |
| `addLog(msg)` | Prepends to log; trims to 60 entries |
| Public getters | Immutable views (`Collections.unmodifiableList`) of players, tiles, log |

#### Observer pattern

`GameState` implements a minimal observer/listener pattern. `MonopolyApp` registers a single `Runnable` that repaints `BoardPanel` and calls `refreshSide`. `SwingGame` calls `state.fireChange()` at the end of every action, triggering the callback on the Event Dispatch Thread.

---

### 3.7 `SwingGame`

**File:** `SwingGame.java`  
**Role:** The GUI's game controller. Receives button-press actions from `MonopolyApp`, applies game rules using the core model classes, and writes results into `GameState`.

#### Fields

| Field | Type | Description |
|---|---|---|
| `state` | `GameState` | Shared state reference |
| `board` | `Board` | Board instance |
| `dice` | `Dice` | Dice instance |
| `bank` | `Bank` | Bank instance |
| `chanceDeck` | `CardDeck` | Chance card deck |
| `communityDeck` | `CardDeck` | Community chest deck |

#### Constructor

Builds all players from the provided name list (each with $1,500), mirrors tiles from `Board` into `GameState.tiles`, and fires an initial change.

#### Public action methods

| Method | Trigger | Description |
|---|---|---|
| `doRoll()` | "Roll Dice" button | Handles jail check, rolls dice, moves player, detects GO crossing, calls `landOn` |
| `doBuy(boolean yes)` | "Buy" / "Skip" buttons | Completes or cancels a purchase on `state.pendingBuy` |

#### Internal logic methods

| Method | Description |
|---|---|
| `landOn(player, tile)` | Dispatches to the correct handler by tile type using `instanceof` pattern matching |
| `applyCard(player, card)` | Applies cash effect of a drawn card; handles "Go to Jail" keyword |
| `handlePurchasable(player, Property)` | Buy prompt, rent payment, or "you own this" for a Property tile |
| `handlePurchasable(player, MetroStationTile)` | Same for metro stations |
| `checkBankruptAndContinue(player)` | Releases bankrupt player's properties; calls `finishTurn` |
| `finishTurn(player)` | Checks game-over; handles doubles re-roll; advances turn |
| `nextTurn()` | Advances `currentIdx`, skipping bankrupt players |
| `refreshBank()` | Syncs `state.bankFunds` from `bank.getFunds()` |
| `setMetroOwner(metro, player)` | Reflectively sets the `private owner` field on `MetroStationTile` |

#### Doubles rule

When `state.lastDoubles` is `true` and the player is neither in jail nor bankrupt, `finishTurn` returns without advancing the turn, giving the same player another roll.

#### Metro purchase workaround

`MetroStationTile.owner` is `private` with no setter. `SwingGame.doBuy` uses reflection (`getDeclaredField + setAccessible`) to set it. This is flagged as a known design issue in §8.

---

### 3.8 `MonopolyApp`

**File:** `MonopolyApp.java`  
**Role:** Swing application entry point. Builds the window, side panel, and wires all UI components to `GameState` and `SwingGame`.

#### Static methods

| Method | Description |
|---|---|
| `main(String[])` | Posts `launch()` to the Event Dispatch Thread |
| `launch()` | Shows setup dialog; builds `GameState`, `SwingGame`, `BoardPanel`, side panel, `JFrame` |
| `showSetupDialog()` | Modal dialog with 4 name fields (defaults: Alice, Bob, Carol, Dave); returns `String[4]` or null on cancel |
| `buildSidePanel(state, game, board)` | Creates the right-hand panel with player cards, buttons, prompt label, bank label, log area |
| `refreshSide(state, side)` | Called on every state change; walks the component tree by `getName()` to update dynamic parts |
| `makePlayerCard(player, idx, current)` | Builds a styled `JPanel` badge for one player |
| `styleButton(btn, bg)` | Applies consistent styling to action buttons |
| `allComponents(root)` | Recursively collects all `Component` descendants |

#### Side panel components (identified by name)

| Component name | Type | Content |
|---|---|---|
| `"playersPanel"` | `JPanel` | Rebuilt on each refresh with one card per player |
| `"promptLbl"` | `JLabel` | Shows pending buy prompt or winner announcement |
| `"bankLbl"` | `JLabel` | Current bank balance |
| `"logArea"` | `JTextArea` | Full log, newest entries at top |

#### Button enable/disable logic (in `refreshSide`)

| Button | Enabled when |
|---|---|
| "🎲  Roll Dice" | `phase == ROLL && !gameOver` |
| "✅  Buy" | `phase == BUY && !gameOver` |
| "❌  Skip" | `phase == BUY && !gameOver` |

---

### 3.9 `BoardPanel`

**File:** `BoardPanel.java`  
**Role:** Custom `JPanel` that paints the entire Monopoly board using Java2D.

#### Constants

| Constant | Value | Meaning |
|---|---|---|
| `BOARD_PX` | `724` | Total board pixel size (square) |
| `CORNER_PX` | `80` | Corner cell size |
| `CELL_PX` | `56` | Regular cell width/height |
| `BAR_H` | `10` | Color group bar thickness |
| `PLAYER_COLORS` | 4-element `Color[]` | Red, Purple, Green, Blue tokens |

#### Color maps

`GROUP_COLORS` maps color group names to `Color` objects:

| Group | Color |
|---|---|
| Red | `#D08C60` |
| Light Blue | `#468FAF` |
| Gray | `#B58463` |
| Orange | `#FFCB69` |
| Pink | `#D9AE94` |
| Green | `#9B9B7A` |
| Yellow | `#E8AC65` |
| Dark Blue | `#797D62` |
| Metro | `#BAA587` |

#### Coordinate system: `cellRect(pos)`

Maps position 0–39 to a pixel `Rectangle`. The board is laid out as follows:

- **Pos 0** (GO): bottom-right corner
- **Pos 1–9**: bottom row, right to left
- **Pos 10** (Cascade): bottom-left corner
- **Pos 11–19**: left column, bottom to top
- **Pos 20** (Jail): top-left corner
- **Pos 21–29**: top row, left to right
- **Pos 30** (Go To Jail): top-right corner
- **Pos 31–39**: right column, top to bottom

#### Paint pipeline (`paintComponent`)

1. Fill center rectangle with `CENTER_BG`
2. Draw center label (title, subtitle, turn badge, dice boxes)
3. For each tile: fill background → border → color bar → text (rotated on sides) → price → owner dot
4. Draw player tokens grouped by position
5. Draw outer board border

#### Text rotation

Tiles on the left side (pos 11–19) rotate text `+π/2`; right side (pos 31–39) rotate `-π/2`. Done with `Graphics2D.rotate()` on a cloned graphics context.

#### Player tokens (`drawTokens`)

Tokens are grouped by position. Up to 4 tokens are arranged in a 2×2 grid in the bottom-right corner of the cell. Each token is a filled circle with the player's first initial in white.

---

### 3.10 `CardDeck`

**File:** `CardDeck.java`  
**Role:** A shuffled circular deck of cards, used for both Chance and Community Chest.

#### Inner class `Card`

| Field | Type | Description |
|---|---|---|
| `description` | `String` | Text displayed to the player |
| `cashEffect` | `int` | Positive = receive, negative = pay, zero = special or movement |

#### `CardDeck(String type)`

Builds either the Chance deck (25 cards) or Community Chest deck (25 cards), then shuffles with `Collections.shuffle`.

#### `draw()`

Returns `deck.get(index)` and increments `index` modulo `deck.size()`, cycling infinitely through the shuffled order.

#### Notable card types

| Category | Examples |
|---|---|
| Receive money | "Bank error in your favor — collect $200", "Holiday fund matures" |
| Pay money | "Doctor's fee — pay $50", "Pay school fees" |
| Go to Jail | "Go to Jail! Do not pass GO." (cashEffect = 0; keyword detected by `SwingGame.applyCard`) |
| Movement | "Advance to GO!", "Advance to Yeritasardakan station" (cashEffect = 0 or 200) |

---

### 3.11 Tile Hierarchy

```
Tile  (abstract)
├── GoTile
├── FreeTile
├── JailTile
├── TaxTile
├── ChanceTile
├── CommunityChestTile
├── MetroStationTile
└── Property
```

#### `Tile` (abstract base)

**File:** `Tile.java`

| Member | Description |
|---|---|
| `name` | Display name |
| `position` | Board index (0–39) |
| `getName()` | Returns name |
| `getPosition()` | Returns position |
| `landOn(Player)` | **Abstract.** Called when a player lands on this tile |
| `toString()` | `"name (position N)"` |

---

#### `GoTile`

Position 0. `landOn` adds $200 to the player. In the Swing path, the bank's `payGoSalary` is used instead, so the direct `addMoney` call in `landOn` is redundant but harmless.

---

#### `FreeTile`

Positions 10 (Cascade Complex) and 30 (Republic Square / Go To Jail). `landOn` prints a rest message. Position 30 is intercepted in `SwingGame.landOn` before `landOn` is called — the player is sent to jail there.

---

#### `JailTile`

Position 20. Has two responsibilities:

- `landOn`: "Just Visiting" message.
- `handleJailTurn(player, allPlayers)`: Called by `Game` (console) when a jailed player's turn begins. Implements the "two players in jail → both released" rule and the 3-turn miss rule.

In the Swing path, jail logic is handled directly in `SwingGame.doRoll()`.

---

#### `TaxTile`

Positions 4, 12, 28, 38. Stores `taxAmount` and `color`. `landOn` calls `player.subtractMoney(taxAmount)`. In the Swing path, `bank.collectTax` is used instead (which also subtracts from the player but properly credits the bank).

---

#### `ChanceTile`

Positions 7, 22, 36. Holds a `static final CardDeck` shared across all instances. `landOn` draws a card and applies its cash effect directly to the player (no bank mediation in console path). Swing path uses `SwingGame.applyCard` via its own deck instances.

---

#### `CommunityChestTile`

Positions 2, 17, 33. Identical structure to `ChanceTile` but uses the community chest deck.

---

#### `MetroStationTile`

Positions 5, 15, 25, 35. Represents metro stations (railways equivalent).

| Field | Value | Description |
|---|---|---|
| `BASE_RENT` | `25` | Fixed fare (no scaling by stations owned) |
| `price` | `200` | Purchase price |
| `owner` | `Player` | Private, no setter — requires reflection in Swing path |
| `color` | `"Beige"` | Visual grouping |

`landOn`: if unowned, prompts to buy (console via `Scanner`). If owned by another, charges $25.

---

#### `Property`

All purchasable street tiles.

| Field | Description |
|---|---|
| `price` | Purchase price |
| `rent` | Flat rent (no house/hotel system) |
| `owner` | Owning `Player`, or `null` |
| `colorGroup` | String key into `BoardPanel.GROUP_COLORS` |

`landOn` (console path): prompts buy/skip via `Scanner`. Swing path bypasses `landOn` entirely — `SwingGame.handlePurchasable` manages the flow.

| Method | Description |
|---|---|
| `setOwner(player)` | Sets the owner (used by `Bank.buyProperty`) |
| `isOwned()` | `owner != null` |
| `getOwner()` | Returns owner |
| `getPrice()` | Returns price |
| `getRent()` | Returns rent |
| `getColorGroup()` | Returns color group string |

---

## 4. Game Rules & Logic

### Starting conditions

- Each player begins with **$1,500** at position 0 (GO).
- The bank begins with **$20,580**.
- Turn order is determined by the order players were entered.

### Turn sequence (Swing path)

```
Phase: ROLL
  → Player clicks "Roll Dice"
  → doRoll():
      ├─ Jail check (miss turn or release)
      ├─ Roll dice → move player
      ├─ Passed GO? → bank.payGoSalary (+$200)
      └─ landOn(tile)
           ├─ GoTile → +$200, finishTurn
           ├─ FreeTile → log, finishTurn
           ├─ JailTile → log, finishTurn
           ├─ pos 30 → goToJail, finishTurn
           ├─ TaxTile → collect tax, checkBankrupt
           ├─ ChanceTile → draw+apply card, checkBankrupt
           ├─ CommunityChestTile → draw+apply card, checkBankrupt
           ├─ MetroStationTile → handlePurchasable(metro)
           └─ Property → handlePurchasable(prop)

Phase: BUY (if purchasable and player can afford it)
  → Player clicks "Buy" or "Skip"
  → doBuy(yes/no):
      ├─ yes → bank transaction, set owner
      └─ no → log skip
  → phase back to ROLL, nextTurn or doubles re-roll
```

### Jail rules

| Condition | Outcome |
|---|---|
| Land on pos 30 | Sent to jail (position = 20, `inJail = true`) |
| Draw "Go to Jail" card | Same |
| In jail, only player jailed | Miss turn; `jailTurns++`; after 3 turns, released |
| Two or more players in jail simultaneously | All jailed players immediately released |

### Doubles

Rolling doubles (both dice equal) grants the player an extra turn. The doubles flag is cleared when the turn advances or the player is jailed.

### Bankruptcy

A player is bankrupt when their balance reaches $0 or below after any payment. On bankruptcy:
- All their owned properties are unowned (owner set to `null`).
- They are permanently skipped in the turn loop.
- The game ends when only one non-bankrupt player remains.

### Rent

Rent is flat per property — there is no house/hotel building mechanic. Metro station rent is always $25, regardless of how many stations a player owns.

### GO salary

$200 is paid each time a player's position wraps from a higher number to a lower number, indicating a full lap. Landing exactly on GO also pays $200.

---

## 5. Board Layout

| Pos | Name | Type | Price | Rent / Tax | Group |
|---|---|---|---|---|---|
| 0 | GO | GoTile | — | +$200 | — |
| 1 | KFC | Property | $60 | $2 | Red |
| 2 | Community Chest | CommunityChestTile | — | — | — |
| 3 | McDonald's | Property | $60 | $4 | Red |
| 4 | Income Tax | TaxTile | — | $200 | — |
| 5 | Marshal Baghramyan | MetroStationTile | $200 | $25 | Metro |
| 6 | Coffee House | Property | $100 | $6 | Light Blue |
| 7 | Chance | ChanceTile | — | — | — |
| 8 | Cofix | Property | $100 | $6 | Light Blue |
| 9 | Gotcha | Property | $120 | $8 | Light Blue |
| 10 | Cascade | FreeTile | — | — | — |
| 11 | Zara | Property | $140 | $10 | Gray |
| 12 | Electric Company | TaxTile | — | $150 | — |
| 13 | Bershka | Property | $140 | $10 | Gray |
| 14 | Pull & Bear | Property | $160 | $12 | Gray |
| 15 | Yeritasardakan | MetroStationTile | $200 | $25 | Metro |
| 16 | Yerevan Mall | Property | $180 | $14 | Orange |
| 17 | Community Chest | CommunityChestTile | — | — | — |
| 18 | Dalma Mall | Property | $180 | $14 | Orange |
| 19 | Mega Mall | Property | $200 | $16 | Orange |
| 20 | Jail | JailTile | — | — | — |
| 21 | Chanel | Property | $220 | $18 | Pink |
| 22 | Chance | ChanceTile | — | — | — |
| 23 | Dior | Property | $220 | $18 | Pink |
| 24 | Gucci | Property | $240 | $20 | Pink |
| 25 | Republic Square (Metro) | MetroStationTile | $200 | $25 | Metro |
| 26 | Yerevan Hotel | Property | $260 | $22 | Green |
| 27 | Dilijan Hotel | Property | $260 | $22 | Green |
| 28 | Water Works | TaxTile | — | $150 | — |
| 29 | Tsaghkadzor Hotel | Property | $280 | $24 | Green |
| 30 | Go To Jail | FreeTile (pos 30) | — | — | — |
| 31 | Paris | Property | $300 | $26 | Yellow |
| 32 | New York | Property | $300 | $26 | Yellow |
| 33 | Community Chest | CommunityChestTile | — | — | — |
| 34 | Oxford University | Property | $320 | $28 | Dark Blue |
| 35 | Zoravar Andranik | MetroStationTile | $200 | $25 | Metro |
| 36 | Chance | ChanceTile | — | — | — |
| 37 | Harvard University | Property | $350 | $35 | Dark Blue |
| 38 | Luxury Tax | TaxTile | — | $100 | — |
| 39 | MIT University | Property | $400 | $40 | Dark Blue |

---

## 6. Data Flow & Interaction Diagrams

### Roll action data flow

```
[User clicks "Roll Dice"]
        │
        ▼
MonopolyApp (ActionListener)
        │  game.doRoll()
        ▼
SwingGame.doRoll()
   ├── state.currentPlayer() → Player
   ├── dice.roll() → total, doubles
   ├── player.move(total, 40)
   ├── bank.payGoSalary() if passed GO
   ├── board.getTile(newPos) → Tile
   ├── landOn(player, tile)
   │       └── per-tile handler updates Player, Bank, GameState
   ├── state.bankFunds = bank.getFunds()
   └── state.fireChange()
                │
                ▼ (invokeLater)
        MonopolyApp listener
          ├── board.repaint() → BoardPanel.paintComponent()
          └── refreshSide(state, side)
                  ├── rebuild playersPanel cards
                  ├── update promptLbl
                  ├── update bankLbl
                  └── update logArea
```

### Property purchase data flow

```
[Player lands on unowned Property]
        │
SwingGame.handlePurchasable(player, prop)
   ├── player.getMoney() >= prop.getPrice()?
   │       Yes → state.pendingBuy = prop
   │              state.phase = BUY
   │              state.fireChange()  → "Buy?" buttons enabled
   │       No  → log "can't afford", finishTurn()
        │
[User clicks "Buy"]
        │
SwingGame.doBuy(true)
   ├── bank.buyProperty(player, prop)
   │       ├── player.subtractMoney(price)
   │       ├── bank.funds += price
   │       ├── prop.setOwner(player)
   │       └── player.addProperty(prop)
   ├── state.pendingBuy = null
   ├── state.phase = ROLL
   └── state.fireChange()
```

---

## 7. UI Architecture

### Window layout

```
┌────────────────────────────────────────────────────────┐
│  JFrame "AUA Monopoly – Yerevan Edition"               │
│  BorderLayout                                          │
├─────────────────────────────────┬──────────────────────┤
│                                 │  Side Panel (EAST)   │
│       BoardPanel (CENTER)       │  ─────────────────   │
│       724 × 724 px              │  Title label         │
│                                 │  Player cards        │
│       Custom Java2D             │  Roll button         │
│       rendering                 │  Buy button          │
│                                 │  Skip button         │
│                                 │  Prompt label        │
│                                 │  Bank label          │
│                                 │  Log scroll area     │
└─────────────────────────────────┴──────────────────────┘
```

### Threading model

All UI operations run on the **Event Dispatch Thread (EDT)**. `GameState.fireChange()` wraps listener calls in `SwingUtilities.invokeLater`, ensuring that state changes triggered by button clicks (already on the EDT) are processed safely and that the paint cycle is not blocked.

### Refresh strategy

Rather than rebuilding the entire side panel on each change, `refreshSide` walks the existing component tree and updates only the named components. `BoardPanel` is fully redrawn on each `repaint()` call — no partial invalidation. This is acceptable because the board is small and rendering is fast.

---

## 8. Known Issues & Design Notes

### Reflection workaround for `MetroStationTile.owner`

`MetroStationTile` declares `owner` as `private` with no public setter. `SwingGame.setMetroOwner` uses `getDeclaredField("owner").setAccessible(true)` to set it. This is fragile (breaks under stricter JVM module access) and should be resolved by adding a `setOwner(Player)` method to `MetroStationTile`, matching the interface on `Property`.

### Metro purchase double-charging

In `SwingGame.doBuy`, when buying a metro station, the code calls `p.subtractMoney(200)` and then `bank.collect(p, 0)`. The `bank.collect(p, 0)` call is a no-op (guarded by the `amount <= 0` check in `Bank.collect`), meaning the bank's `funds` counter is never incremented for metro purchases. The player loses $200, but the bank does not gain it.

### Dual card deck instances

`ChanceTile` and `CommunityChestTile` each hold a `static final` deck used in the console path. `SwingGame` creates its own separate `chanceDeck` and `communityDeck` instances. These decks are shuffled independently, so the draw order will differ between runs and between the two paths.

### `GoTile.landOn` redundancy

`GoTile.landOn` calls `player.addMoney(200)` directly, bypassing the bank. In the Swing path, `SwingGame.landOn` calls `bank.payGoSalary(p)` before any tile `landOn` is invoked, resulting in the player receiving $400 if they land exactly on GO via the Swing path. This is a bug: the bank's `payGoSalary` fires, then `landOn` fires and adds another $200.

### `TaxTile.landOn` bank bypass

`TaxTile.landOn` calls `player.subtractMoney(taxAmount)` directly — the bank does not receive the funds. In the Swing path, `bank.collectTax` is correctly used, and `tile.landOn` is not called. The console path leaks tax money.

### No `setOwner` on `MetroStationTile` but `setOwner` on `Property`

Inconsistent API: `Property` has a public `setOwner(Player)` but `MetroStationTile` does not. Any future refactor should unify these under a common `Purchasable` interface.

### Board position 30 tile type

Position 30 is constructed as `new FreeTile("Republic Square", 30)` but represents "Go To Jail". The distinction is made in `SwingGame.landOn` by checking `pos == 30` explicitly. This is misleading — a dedicated `GoToJailTile` class or at least a clear naming convention would improve clarity.

### No `Board.java` import needed

`Board.java` contains `import java.util.*` but all list handling is done with concrete `ArrayList` — the wildcard import is unused for any type other than `ArrayList` and `List`.

---

## 9. How to Build & Run

### Requirements

- Java 17 or later (uses `instanceof` pattern matching and records syntax)
- No external libraries — standard Java SE only

### Compile

From the directory containing all `.java` files:

```bash
javac *.java
```

### Run — Swing GUI

```bash
java MonopolyApp
```

A setup dialog will appear asking for player names (2–4). After confirming, the board window opens.

### Run — Console (text) mode

```bash
java Main
```

Follow the prompts: enter the number of players, then each player's name. Press ENTER each turn to roll dice. Buy decisions are made by typing `yes` or `no`.

### File list

| File | Purpose |
|---|---|
| `MonopolyApp.java` | Swing entry point |
| `Main.java` | Console entry point |
| `SwingGame.java` | Swing game controller |
| `Game.java` | Console game controller |
| `GameState.java` | Observable state model |
| `BoardPanel.java` | Board rendering |
| `Board.java` | Tile list construction |
| `Player.java` | Player state |
| `Bank.java` | Financial logic |
| `Dice.java` | Dice rolling |
| `CardDeck.java` | Card deck + `Card` inner class |
| `Tile.java` | Abstract base tile |
| `GoTile.java` | GO corner tile |
| `FreeTile.java` | Free parking / rest tile |
| `JailTile.java` | Jail corner tile |
| `TaxTile.java` | Tax/utility tile |
| `ChanceTile.java` | Chance tile |
| `CommunityChestTile.java` | Community Chest tile |
| `MetroStationTile.java` | Metro station (railway) tile |
| `Property.java` | Purchasable street tile |

## How to Run
Run src  ---> javac *.java && java MonopolyApp to start the game.
