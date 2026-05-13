/**
 * Represents a deck of Chance or Community Chest cards.
 * Handles storing and drawing cards.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    public static class Card {
        private final String description;
        private final int cashEffect;
        public Card(String description, int cashEffect) {
            this.description = description;
            this.cashEffect = cashEffect;
        }
        public String getDescription() { 
          return description; 
        }
        public int getCashEffect() { 
          return cashEffect; 
        }
    }
    private final List<Card> deck = new ArrayList<>();
    private int index = 0;
    public CardDeck(String type) {
        if (type.equalsIgnoreCase("chance")) {
            buildChanceDeck();
        } else {
            buildCommunityChestDeck();
        }
        Collections.shuffle(deck);
    }
    private void buildChanceDeck() {
        deck.add(new Card("Advance to GO! Collect $200.", 200));
        deck.add(new Card("Bank pays you dividend of $50.", 50));
        deck.add(new Card("Your building loan matures. Collect $150.", 150));
        deck.add(new Card("You won a crossword competition! Collect $100.", 100));
        deck.add(new Card("Speeding fine — pay $15.", -15));
        deck.add(new Card("Pay school fees of $150.", -150));
        deck.add(new Card("Doctor's fee — pay $50.", -50));
        deck.add(new Card("You have won second prize in a beauty contest. Collect $10.", 10));
        deck.add(new Card("Holiday fund matures — receive $100.", 100));
        deck.add(new Card("Income tax refund — collect $20.", 20));
        deck.add(new Card("Life insurance matures — collect $100.", 100));
        deck.add(new Card("Receive interest on 7% preference shares — collect $25.", 25));
        deck.add(new Card("Pay hospital fees of $100.", -100));
        deck.add(new Card("Street repairs: pay $40 per house you own.", 0));  // special — handled as $40 flat for simplicity
        deck.add(new Card("Pay $50 for repairs.", -50));
        deck.add(new Card("Advance to Yeritasardakan station.", 0));   // movement cards kept at 0 cash
        deck.add(new Card("Take a trip to Marshal Baghramyan.", 0));
        deck.add(new Card("Go to Jail! Do not pass GO, do not collect $200.", 0));
        deck.add(new Card("Make general repairs — pay $25 per house.", -25));
        deck.add(new Card("You are assessed for street repairs — pay $100.", -100));
        deck.add(new Card("Congratulations! Collect $200 bonus.", 200));
        deck.add(new Card("Found $50 on the street. Lucky you!", 50));
        deck.add(new Card("Your investment pays off — collect $75.", 75));
        deck.add(new Card("Pay luxury tax of $75.", -75));
        deck.add(new Card("Advance to GO and collect $200.", 200));
    }
 
    private void buildCommunityChestDeck() {
        deck.add(new Card("Bank error in your favor — collect $200.", 200));
        deck.add(new Card("Doctor's fee — pay $50.", -50));
        deck.add(new Card("From sale of stock you get $50.", 50));
        deck.add(new Card("Holiday fund matures — receive $100.", 100));
        deck.add(new Card("Income tax refund — collect $20.", 20));
        deck.add(new Card("It's your birthday! Collect $10 from every player.", 10));
        deck.add(new Card("Life insurance matures — collect $100.", 100));
        deck.add(new Card("Pay hospital fees of $100.", -100));
        deck.add(new Card("Pay school fees of $50.", -50));
        deck.add(new Card("Receive $25 consultancy fee.", 25));
        deck.add(new Card("You inherit $100.", 100));
        deck.add(new Card("You have won second prize in a beauty contest — collect $10.", 10));
        deck.add(new Card("Advance to GO! Collect $200.", 200));
        deck.add(new Card("Go to Jail! Do not pass GO.", 0));
        deck.add(new Card("Street repairs — pay $40.", -40));
        deck.add(new Card("Collect $50 from every player for your birthday.", 50));
        deck.add(new Card("Grand Opera Night — collect $50 from every player.", 50));
        deck.add(new Card("Receive $100 from the bank.", 100));
        deck.add(new Card("Pay $150 in fines.", -150));
        deck.add(new Card("Collect $10 from every player.", 10));
        deck.add(new Card("You donated to charity — pay $20.", -20));
        deck.add(new Card("Lottery winnings — collect $75.", 75));
        deck.add(new Card("Unexpected bonus — collect $60.", 60));
        deck.add(new Card("Overdue rent — pay $30.", -30));
        deck.add(new Card("Community fund payout — collect $45.", 45));
    }
    /** Draw the next card (cycles through the shuffled deck). */
    public Card draw() {
        Card card = deck.get(index);
        index = (index + 1) % deck.size();
        return card;
    }
}
