import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Client {

    public Connection clientConnection;

    public Player player;
    public Player opponent;


    public JLabel playerOneCards;
    public JLabel playerOneCardsValue;

    public JLabel playerTwoCards;
    public JLabel playerTwoCardsValue;

    public Player croupier;
    public JLabel infoText;
    public JLabel LcroupierText;
    public JLabel croupierCards;
    public JLabel croupierCardsValue;

    public JLabel currentBetValue;
    public JButton hit;
    public JButton stand;
    public JButton readyToPlay;
    public JPanel bets;
    public JLabel balanceLabel;

    public int playerNumber = 0;

    public JFrame game;

    public JLabel handResultInfoPlayer;
    public JLabel handResultInfoOpponent;

    public Color playersColor = new Color(210, 178, 168);
    public Color playingBoardColor = new Color(37, 119, 107);
    Font fontBold = new Font("Arial", Font.BOLD, 16);
    Font fontNormal = new Font("Arial", Font.TRUETYPE_FONT, 16);


    /**
     * Konsturktor.
     * @param clientConnection
     */
    public Client(Connection clientConnection) {
        this.clientConnection = clientConnection;
    }

    /**
     * Aktualizuje informace na stole o uživateli dle id.
     * @param player_id ID uživatele.
     */
    public void updatePlayerInfo(int player_id) {
        String opponentName = opponent.getName();
        if (player_id == 1) {
            playerOneCardsValue.setText("YOUR CARDS VALUE: "+player.getCardsValue());
            playerOneCards.setText(player.getCardsText());

            playerTwoCardsValue.setText(opponentName+" cards value: "+opponent.getCardsValue());
            playerTwoCards.setText(opponent.getCardsText());
        } else {
            playerOneCardsValue.setText(opponentName+" cards value: "+opponent.getCardsValue());
            playerOneCards.setText(opponent.getCardsText());

            playerTwoCardsValue.setText("YOUR CARDS VALUE: "+player.getCardsValue());
            playerTwoCards.setText(player.getCardsText());
        }

    }

    /**
     * Posílá na server informaci o ukončení odehrané ruky. Vyčistí údaje o uživateli i u opponenta.
     */
    public void handEnded() {
        clientConnection.sendMessage("hand_end");
        refreshPlayerValues();
        opponent.clearPlayerData();
    }

    /**
     * Vyčistí údaje o uživateli.
     */
    public void refreshPlayerValues() {
        player.playerLost = false;
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
    }

    /**
     * Vyčistí všechny krupierovi hodnoty.
     */
    public void clearCroupier() {
        croupier.croupierCanPlay = false;
        croupier.clearPlayerData();
        croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
        croupierCards.setText(croupier.getCardsText());
    }

    /**
     * Odesílá zprávu na server, že žádá o kartu.
     */
    public void playerGetHit() {
        String mess = "player_get_hit";
        clientConnection.sendMessage(mess);
    }

    /**
     * Vytvoří insatnce hráče a protihráče.
     * @param playerNumber Číslo (id) aktuálního hráče.
     * @param p1 Přezdívka 1. hráče
     * @param p2 Přezdívka 2. hráče
     */
    public void createPlayers(int playerNumber, String p1, String p2) {
        this.playerNumber = playerNumber;

        if (playerNumber == 1) {
            player = new Player(p1, 50);
            opponent = new Player(p2, 50);
            opponent.id = 2;
        } else {
            player = new Player(p2, 50);
            opponent = new Player(p1, 50);
            opponent.id = 1;
        }
        player.id = playerNumber;
        player.connected = true;
    }

    /**
     * Vytváří celé okno pro hru včetně všech komponent.
     * @param playerNumber Číslo (id) aktuálního hráče.
     * @param p1 Přezdívka 1. hráče
     * @param p2 Přezdívka 2. hráče
     */
    public void initializeGUIGame(int playerNumber, String p1, String p2) {

        createPlayers(playerNumber, p1, p2);

        /* ZAKLADNI UDAJE O OKNE */
        game = new JFrame();
        game.setTitle("Blackjack Game - "+player.getName());
        game.setSize(900, 400);
        game.setMinimumSize(new Dimension(750, 400));
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        /* TVORBA CASTI S KRUPIEREM */
        croupier = new Player("croupier", 0);

        infoText = new JLabel("", JLabel.CENTER);
        infoText.setFont(fontBold);
        infoText.setText(" ");

        LcroupierText = new JLabel("", JLabel.CENTER);
        LcroupierText.setFont(fontBold);
        LcroupierText.setText("CROUPIER");

        croupierCards = new JLabel("", JLabel.CENTER);
        croupierCards.setFont(fontBold);
        croupierCards.setText(" ");

        croupierCardsValue = new JLabel("", JLabel.CENTER);
        croupierCardsValue.setFont(fontBold);
        croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());

        JPanel Pcroupier = new JPanel();
        Pcroupier.setLayout(new GridLayout(4, 1));
        Pcroupier.add(infoText);
        Pcroupier.add(LcroupierText);
        Pcroupier.add(croupierCards);
        Pcroupier.add(croupierCardsValue);
        Pcroupier.setBackground(playersColor);



        /* TVORBA HRACU A JEJICH KOMPONENT */
        JPanel playerOne = new JPanel();
        playerOne.setLayout(new GridLayout(3, 1));
        playerOne.setBackground(playingBoardColor);
        JPanel playerTwo = new JPanel();
        playerTwo.setLayout(new GridLayout(3, 1));
        playerTwo.setBackground(playingBoardColor);

        playerOneCards = new JLabel("", JLabel.CENTER);
        playerTwoCards = new JLabel("", JLabel.CENTER);
        handResultInfoPlayer = new JLabel("", JLabel.CENTER);
        handResultInfoPlayer.setFont(fontBold);
        handResultInfoOpponent = new JLabel("", JLabel.CENTER);
        handResultInfoOpponent.setFont(fontBold);

        if (playerNumber == 1) {
            playerOneCards.setFont(fontBold);
            playerTwoCards.setFont(fontNormal);

            playerOneCardsValue = new JLabel("YOUR CARDS VALUE: "+0, JLabel.CENTER);
            playerTwoCardsValue = new JLabel(opponent.getName()+" cards value: "+0, JLabel.CENTER);
            playerOneCardsValue.setFont(fontBold);
            playerTwoCardsValue.setFont(fontNormal);

            playerOne.add(handResultInfoPlayer);
            playerTwo.add(handResultInfoOpponent);
        } else {
            playerOneCards.setFont(fontNormal);
            playerTwoCards.setFont(fontBold);

            playerOneCardsValue = new JLabel(opponent.getName()+" cards value: "+0, JLabel.CENTER);
            playerTwoCardsValue = new JLabel("YOUR CARDS VALUE: "+0, JLabel.CENTER);
            playerOneCardsValue.setFont(fontNormal);
            playerTwoCardsValue.setFont(fontBold);

            playerOne.add(handResultInfoOpponent);
            playerTwo.add(handResultInfoPlayer);
        }

        playerOne.add(playerOneCards);
        playerOne.add(playerOneCardsValue);
        playerTwo.add(playerTwoCards);
        playerTwo.add(playerTwoCardsValue);



        /* OBLAST PRO HRU */
        JPanel playingBoard = new JPanel();
        playingBoard.setLayout(new GridLayout(1, 2));
        playingBoard.add(playerOne);
        playingBoard.add(playerTwo);



        /* HLAVNI PANEL */
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.darkGray);



        /* PANEL HRACE PRO VSAZENI A OVLADANI HRY */
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BorderLayout());

        balanceLabel = new JLabel("Current Balance: " + player.getBalance());
        JPanel playerBalance = new JPanel();
        playerBalance.add(balanceLabel);

        currentBetValue = new JLabel("Current Bet: " + player.getBetValue(), JLabel.CENTER);

        bets = new JPanel();
        bets.setLayout(new GridLayout(1, 4));
        bets.setBackground(playersColor);
        JButton bet5 = new JButton("Bet 5");
        bet5.addActionListener(e -> {
            player.bet(5);
            currentBetValue.setText("Current Bet: " + player.getBetValue());
            balanceLabel.setText("Current Balance: " + player.getBalance());
        });
        JButton bet10 = new JButton("Bet 10");
        bet10.addActionListener(e -> {
            player.bet(10);
            currentBetValue.setText("Current Bet: " + player.getBetValue());
            balanceLabel.setText("Current Balance: " + player.getBalance());
        });
        JButton bet20 = new JButton("Bet 20");
        bet20.addActionListener(e -> {
            player.bet(20);
            currentBetValue.setText("Current Bet: " + player.getBetValue());
            balanceLabel.setText("Current Balance: " + player.getBalance());
        });
        JButton cancelBet = new JButton("Cancel bet");
        cancelBet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.cancelBet();
                currentBetValue.setText("Current Bet: " + player.getBetValue());
                balanceLabel.setText("Current Balance: " + player.getBalance());
            }
        });

        bets.add(bet5);
        bets.add(bet10);
        bets.add(bet20);
        bets.add(cancelBet);

        JPanel currentBet = new JPanel();
        currentBet.setLayout(new GridLayout(2, 1));
        currentBet.add(currentBetValue);
        currentBet.add(bets);



        /* PANEL PRO TLACITKA PRO OVLADANI HRY */
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));
        hit = new JButton("Hit");
        readyToPlay = new JButton("Ready to play");
        stand = new JButton("Stand");
        hit.setVisible(false);
        hit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hit.setVisible(false);
                stand.setVisible(false);
                playerGetHit();
            }
        });

        stand.setVisible(false);
        stand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // odesila, že hrac pokracuje (continue)
                clientConnection.sendMessage("player_stand:C");
            }
        });

        readyToPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (player.getBetValue() > 0) {
                    bets.setVisible(false);
                    readyToPlay.setVisible(false);

                    clientConnection.sendMessage("ready_to_play_hand");

                    croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
                    croupierCards.setText(croupier.getCardsText());
                }
            }
        });

        buttonPanel.add(hit);
        buttonPanel.add(stand);
        buttonPanel.add(readyToPlay);



        /* NASTAVENI BAREV POZADI A POZIC PANELU */
        buttonPanel.setBackground(playersColor);
        playerBalance.setBackground(playersColor);
        currentBet.setBackground(playersColor);

        // Přidání podpanelu do hlavního panelu
        playerPanel.add(buttonPanel, BorderLayout.EAST);
        playerPanel.add(playerBalance, BorderLayout.WEST);
        playerPanel.add(currentBet, BorderLayout.CENTER);

        mainPanel.add(playerPanel, BorderLayout.SOUTH); // Umístění na dolní část
        mainPanel.add(playingBoard, BorderLayout.CENTER);
        mainPanel.add(Pcroupier, BorderLayout.NORTH);

        game.add(mainPanel);
        game.setVisible(true);
    }

    /**
     * Hlavní metoda spouštěná na začátku programu.
     * @param args
     */
    public static void main(String[] args) {
        new Connection();
    }
}
