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

    public boolean connected = false;

    /**
     * Konstruktor.
     * @param name
     * @param balance
     */
    public Player(String name, int balance) {
        this.id = 0;
        this.name = name;
        this.balance = balance;
        this.betValue = 0;
        this.cards = new ArrayList<>();
        System.out.println("Hráč " + name + " byl vytvořen.");
    }

    /**
     * Přidá kartu do hráčových karet a přepočítá jejich hodnotu.
     * @param card Přidávaná karta.
     */
    public void addCard(String card) {
        cards.add(card);
        countCardsValue();
    }

    /**
     * Vsadí sázku, přičte hodnotu do proměnné pro hodnotu sázky.
     * @param bet Sázka.
     */
    public void bet(int bet) {
        if (bet <= balance) {
            balance -= bet;
            betValue += bet;
        }
    }

    /**
     * Zruší sázku, vrátí jí do zůstatku a nastaví hodnotu sázky na 0.
     */
    public void cancelBet() {
        this.balance += this.betValue;
        this.betValue = 0;
    }

    /**
     * Remíza, do zůstatku se zpět připočte sázka a vyčistí se data hráče.
     */
    public void draw() {
        this.balance += this.betValue;
        clearPlayerData();
    }

    /**
     * Hráč vyhrál, do zůstatku se uloží dvojnásobek sázky a vyčistí se data hráče.
     */
    public void win() {
        this.balance += this.betValue * 2;
        clearPlayerData();
    }

    /**
     * Hráč prohrál - vyčístí data hráče.
     */
    public void lose() {
        clearPlayerData();
    }

    /**
     * Vyčístí data hráče. Odstraní karty, přepočítá hodnotu karet a nastaví velikost sázky na 0.
     */
    public void clearPlayerData() {
        this.cards.clear();
        countCardsValue();
        this.betValue = 0;
    }

    /**
     * Vypočítá a uloží hodnotu hráčových karet.
     */
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

    /**
     * Vrací hodnotu karty.
     * @param card Karta, jejíž hodnotu chceme získat.
     * @return Hodnota karty.
     */
    private int getCardValue(String card) {
        if (card.equals("A")) {
            return 11;
        }
        if (card.equals("K") || card.equals("Q") || card.equals("J") || card.equals("T")) {
            return 10;
        }
        return Integer.parseInt(card);
    }

    /**
     * Vytvoří text z karet, které jsou uloženy v arraylistu.
     * @return Text z karet.
     */
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



    public int getCardsValue() {
        return cardsValue;
    }

    public int getBetValue() {
        return betValue;
    }

    public int getBalance() {
        return this.balance;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", balance=" + balance +
                ", cards=" + cards +
                '}';
    }
}