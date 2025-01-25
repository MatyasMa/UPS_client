import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Client {

//    public Socket socket;
//    public BufferedReader input;
//    public BufferedWriter output;
//
//    // CONNECTION FRAME
//    public JFrame connectionFrame;
//
//    // LOBBY
//    public JFrame lobby;
//    public JTextArea chatArea;
//    public JTextField messageField, nickname;
//    public JButton readyButton, exitButton, controlReadyButton;
//    public JLabel info;


    public Player player;
    public Player opponent;

    Font fontBold = new Font("Arial", Font.BOLD, 16);
    Font fontNormal = new Font("Arial", Font.TRUETYPE_FONT, 16);

    public JLabel playerOneCards;
    public JLabel playerOneCardsValue;

    public JLabel playerTwoCards;
    public JLabel playerTwoCardsValue;

    public Player croupier;
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
    // TODO: udělat na serveru
    // TODO: smazat, vyjebat se na to
    public int balanceToWin = 60;

    public JFrame game;

    public Connection clientConnection;

    public JLabel handResultInfoPlayer;
    public JLabel handResultInfoOpponent;

    public Color playersColor = new Color(210, 178, 168);
    public Color playingBoardColor = new Color(37, 119, 107);

    public Client(Connection clientConnection) {
        this.clientConnection = clientConnection;
    }

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

    public void handEnded() {
        clientConnection.sendMessage("hand_end");
        refreshPlayerValues();
        opponent.clearPlayerData();
        // updatePlayerInfo(player.id);
        clearTable();
        // clearCroupier();
    }


    public void refreshPlayerValues() {
        player.playerLost = false;
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        // updatePlayerInfo(player.id);
//        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//        playerOneCards.setText(player.getCardsText());
    }

    public void clearTable() {
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        // updatePlayerInfo(player.id);
//        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//        playerOneCards.setText(player.getCardsText());
    }

    public void clearCroupier() {
        croupier.croupierCanPlay = false;
        croupier.clearPlayerData();
        croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
        croupierCards.setText(croupier.getCardsText());
    }

    public void playerGetHit() {
        String mess = "player_get_hit";
        clientConnection.sendMessage(mess);
    }

    public void initializeGUIGame(int playerNumber, String p1, String p2) {
        this.playerNumber = playerNumber;

        if (playerNumber == 1) {
            player = new Player(p1, 50);
            opponent = new Player(p2, 50);
        } else {
            player = new Player(p2, 50);
            opponent = new Player(p1, 50);
        }

        player.id = playerNumber;


        if (playerNumber == 1) {
            opponent.id = 2;
        } else {
            opponent.id = 1;
        }

        game = new JFrame();
        game.setTitle("Blackjack Game - "+player.getName());
        game.setSize(750, 400);
        game.setMinimumSize(new Dimension(750, 400));
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // přidat boolean pro vytváření jen jednou kdyby došlo k vypadnutí sítě
        croupier = new Player("croupier", 0);

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
        Pcroupier.setLayout(new GridLayout(3, 1));
        Pcroupier.add(LcroupierText);
        Pcroupier.add(croupierCards);
        Pcroupier.add(croupierCardsValue);
        Pcroupier.setBackground(playersColor);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));


        playerOneCards = new JLabel("", JLabel.CENTER);
        playerTwoCards = new JLabel("", JLabel.CENTER);


        if (playerNumber == 1) {
            playerOneCards.setFont(fontBold);
            playerTwoCards.setFont(fontNormal);

            playerOneCardsValue = new JLabel("YOUR CARDS VALUE: "+0, JLabel.CENTER);
            playerTwoCardsValue = new JLabel(opponent.getName()+" cards value: "+0, JLabel.CENTER);
            playerOneCardsValue.setFont(fontBold);
            playerTwoCardsValue.setFont(fontNormal);
        } else {
            playerOneCards.setFont(fontNormal);
            playerTwoCards.setFont(fontBold);

            playerOneCardsValue = new JLabel(opponent.getName()+" cards value: "+0, JLabel.CENTER);
            playerTwoCardsValue = new JLabel("YOUR CARDS VALUE: "+0, JLabel.CENTER);
            playerOneCardsValue.setFont(fontNormal);
            playerTwoCardsValue.setFont(fontBold);
        }




        handResultInfoPlayer = new JLabel("", JLabel.CENTER);
        handResultInfoPlayer.setFont(fontBold);
        handResultInfoOpponent = new JLabel("", JLabel.CENTER);
        handResultInfoOpponent.setFont(fontBold);


        JPanel playerOne = new JPanel();
        playerOne.setLayout(new GridLayout(3, 1));
        playerOne.setBackground(playingBoardColor);

        JPanel playerTwo = new JPanel();
        playerTwo.setLayout(new GridLayout(3, 1));
        playerTwo.setBackground(playingBoardColor);

        if (playerNumber == 1) {
            playerOne.add(handResultInfoPlayer);
            playerTwo.add(handResultInfoOpponent);
        } else {
            playerOne.add(handResultInfoOpponent);
            playerTwo.add(handResultInfoPlayer);
        }

        playerOne.add(playerOneCards);
        playerOne.add(playerOneCardsValue);

        playerTwo.add(playerTwoCards);
        playerTwo.add(playerTwoCardsValue);

        JPanel playingBoard = new JPanel();
        playingBoard.setLayout(new GridLayout(1, 2));
        playingBoard.add(playerOne);
        playingBoard.add(playerTwo);

        // Hlavní panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.darkGray);


        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BorderLayout());

        // Panel pro zobrazení zůstatku
        balanceLabel = new JLabel("Current Balance: " + player.getBalance());
        JPanel playerBalance = new JPanel();
        playerBalance.add(balanceLabel);

        // Panel pro aktuální sázku
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


        hit = new JButton("Hit");
        readyToPlay = new JButton("Ready to play");
        stand = new JButton("Stand");
        hit.setVisible(false);
        hit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // playerHit(player, playerOneCardsValue, playerOneCards);
                hit.setVisible(false);
                stand.setVisible(false);
                playerGetHit();
//                if (player.getCardsValue() > 21) {
//                    JOptionPane.showMessageDialog(null, "You lose!");
//
//                    player.lose();
//                    // clear table
//                    hit.setVisible(false);
//                    stand.setVisible(false);
//                    readyToPlay.setVisible(true);
//                    bets.setVisible(true);
//                    currentBetValue.setText("Current Bet: " + player.getBetValue());
//                    balanceLabel.setText("Current Balance: " + player.getBalance());
//                    playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//                    playerOneCards.setText(player.getCardsText());
//                    // clear croupier
//                    croupier.clearPlayerData();
//                    croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
//                    croupierCards.setText(croupier.getCardsText());
//                }
            }
        });

        stand.setVisible(false);
        stand.addActionListener(new ActionListener() {
            // hraje krupier / další hráč
            public void actionPerformed(ActionEvent e) {
                clientConnection.sendMessage("player_stand:C");
//                if (croupier.getCardsValue() == player.getCardsValue()) {
//                    JOptionPane.showMessageDialog(null, "Draw!");
//
//                    player.draw();
//                } else if (croupier.getCardsValue() > player.getCardsValue() && croupier.getCardsValue() <= 21) {
//                    JOptionPane.showMessageDialog(null, "You lose!");
//
//                    player.lose();
//                } else {
//                    JOptionPane.showMessageDialog(null, "You win!");
//
//                    player.win();
//                }
//                // clear table
//                hit.setVisible(false);
//                stand.setVisible(false);
//                readyToPlay.setVisible(true);
//                bets.setVisible(true);
//                currentBetValue.setText("Current Bet: " + player.getBetValue());
//                balanceLabel.setText("Current Balance: " + player.getBalance());
//                playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//                playerOneCards.setText(player.getCardsText());
//                // clear croupier
//                croupier.clearPlayerData();
//                croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
//                croupierCards.setText(croupier.getCardsText());
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

        // Přidání tlačítek do podpanelu
        buttonPanel.add(hit);
        buttonPanel.add(stand);
        buttonPanel.add(readyToPlay);


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


        // TODO přida klienta co mám vytvořeno
        // bud použít hru a předělat jí na 2 hráče, nebu udělat nový klient
        // nový klient karty pro jednoho a balance druhýho




        game.add(mainPanel);
        game.setVisible(true);
    }

    public static void main(String[] args) {
        new Connection();
//        Connection conn = new Connection();

        // SwingUtilities.invokeLater(() -> new Client(address, port));

        // SwingUtilities.invokeLater(() -> new Client("localhost", 8080));
    }
}
