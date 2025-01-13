import java.util.ArrayList;

public class Player {
    private String name;
    public int id;
    private int balance;
    private int betValue;
    public ArrayList<String> cards;
    private int cardsValue;

    public boolean croupierCanPlay = false;
    public boolean playerLost = false;

    public Player(String name, int balance) {
        this.id = 0;
        this.name = name;
        this.balance = balance;
        this.betValue = 0;
        this.cards = new ArrayList<>();
        System.out.println("Hráč " + name + " byl vytvořen.");
    }

    public void addCard(String card) {
        cards.add(card);
        countCardsValue();
    }

    public int getBalance() {
        return this.balance;
    }

    public String getName() {
        return name;
    }

    public void bet(int bet) {
        if (bet <= balance) {
            balance -= bet;
            betValue += bet;
        } else {
            // TODO: informovat uživatele, že nemá peníze na větší sázku
        }
    }

    public void cancelBet() {
        this.balance += this.betValue;
        this.betValue = 0;
    }

    public void draw() {
        this.balance += this.betValue;
        clearPlayerData();
    }

    public void win() {
        this.balance += this.betValue * 2;
        clearPlayerData();
    }

    public void lose() {
        clearPlayerData();
    }

    public void clearPlayerData() {
        this.cards.clear();
        countCardsValue();
        this.betValue = 0;


    }



    public int getBetValue() {
        return betValue;
    }

    public void countCardsValue() {
        int countedCardsValue = 0;
        int aces = 0;
        for (String card : cards) {
            countedCardsValue += getCardValue(card);
            if (card.equals("A")) {
                aces++;
            }
        }
        while (countedCardsValue > 21 && aces > 0) {
            countedCardsValue -= 10;
            aces--;
        }
        this.cardsValue = countedCardsValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setBetValue(int betValue) {
        this.betValue = betValue;
    }

    public ArrayList<String> getCards() {
        return cards;
    }

    public void setCards(ArrayList<String> cards) {
        this.cards = cards;
    }

    public void setCardsValue(int cardsValue) {
        this.cardsValue = cardsValue;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", balance=" + balance +
                ", cards=" + cards +
                '}';
    }


    private int getCardValue(String card) {
        if (card.equals("A")) {
            return 11;
        }
        if (card.equals("K") || card.equals("Q") || card.equals("J") || card.equals("T")) {
            return 10;
        }
        return Integer.parseInt(card);
    }

    public int getCardsValue() {
        return cardsValue;
    }

    public String getCardsText() {
        String cardsText = "";

        for (String card : cards) {
            if (card.equals("T")) {
                cardsText += "10 ";
            } else {
                cardsText += card + " ";
            }
        }
        return cardsText;
    }
}