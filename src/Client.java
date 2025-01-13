import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
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
    public int balanceToWin = 60;

    public JFrame game;

    public Connection clientConnection;

    public Client(Connection clientConnection) {
        this.clientConnection = clientConnection;
    }


    public void playerLoseGame() {
        System.out.println("konec hry hráč prohrál hru");
        // TODO: poslat na server a začít novou hru
        // TODO: na serveru kontrola, jestli nevyhráli oba najednou
    }

    public void playerWinGame() {
        System.out.println("konec hry hráč vyhrál");
        // TODO: poslat na server a začít novou hru
        // TODO: na serveru kontrola, jestli nevyhráli oba najednou
    }

    public void updatePlayerInfo(int player_id) {
        if (player_id == 1) {
            playerOneCardsValue.setText("YOUR CARDS: "+player.getCardsValue());
            playerOneCards.setText(player.getCardsText());

            playerTwoCardsValue.setText("Player two cards value: "+opponent.getCardsValue());
            playerTwoCards.setText(opponent.getCardsText());
        } else {
            playerOneCardsValue.setText("Player one cards value: "+opponent.getCardsValue());
            playerOneCards.setText(opponent.getCardsText());

            playerTwoCardsValue.setText("YOUR CARDS VALUE: "+player.getCardsValue());
            playerTwoCards.setText(player.getCardsText());
        }

    }

    public void handEnded() {
        clientConnection.sendMessage("hand_end");
        refreshPlayerValues();
        opponent.clearPlayerData();
        updatePlayerInfo(player.id);
        clearTable();
        clearCroupier();
    }


    public void refreshPlayerValues() {
        player.playerLost = false;
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        updatePlayerInfo(player.id);
//        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//        playerOneCards.setText(player.getCardsText());
    }

    public void clearTable() {
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        updatePlayerInfo(player.id);
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

    public int getRandomNumber() {
        Random rand = new Random();
        int a = rand.nextInt(13) - 1;
        return a;
    }

    public void initializeGUIGame(int playerNumber) {
        // TODO: přidat přezdívku + číslo
        this.playerNumber = playerNumber;

        player = new Player("Hrac", 50);
        player.id = playerNumber;

        opponent = new Player("opponent", 50);
        if (playerNumber == 1) {
            opponent.id = 2;
        } else {
            opponent.id = 1;
        }

        game = new JFrame();
        game.setTitle("Blackjack Game Player"+playerNumber);
        game.setSize(700, 400);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // přidat boolean pro vytváření jen jednou kdyby došlo k vypadnutí sítě
        croupier = new Player("croupier", 0);
        LcroupierText = new JLabel("croupier", JLabel.CENTER);
        croupierCards = new JLabel("", JLabel.CENTER);
        croupierCardsValue = new JLabel("Cards value: "+croupier.getCardsValue(), JLabel.CENTER);

        JPanel Pcroupier = new JPanel();
        Pcroupier.setLayout(new GridLayout(3, 1));
        Pcroupier.add(LcroupierText);
        Pcroupier.add(croupierCards);
        Pcroupier.add(croupierCardsValue);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));

        playerOneCards = new JLabel("", JLabel.CENTER);
        playerOneCardsValue = new JLabel("Player one cards value: "+0, JLabel.CENTER);

        playerTwoCards = new JLabel("", JLabel.CENTER);
        playerTwoCardsValue = new JLabel("Player two cards value: "+0, JLabel.CENTER);

        JPanel playerOne = new JPanel();
        playerOne.setLayout(new GridLayout(2, 1));
        playerOne.setBackground(Color.lightGray);
        playerOne.add(playerOneCards);
        playerOne.add(playerOneCardsValue);

        JPanel playerTwo = new JPanel();
        playerTwo.setBackground(Color.gray);
        playerTwo.setLayout(new GridLayout(2, 1));
        playerTwo.add(playerTwoCards);
        playerTwo.add(playerTwoCardsValue);

        JPanel playingBoard = new JPanel();
        //playingBoard.setBackground(Color.gray);
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
        JButton bet5 = new JButton("Bet 5");
        bet5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.bet(5);
                currentBetValue.setText("Current Bet: " + player.getBetValue());
                balanceLabel.setText("Current Balance: " + player.getBalance());
            }
        });
        JButton bet10 = new JButton("Bet 10");
        bet10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.bet(10);
                currentBetValue.setText("Current Bet: " + player.getBetValue());
                balanceLabel.setText("Current Balance: " + player.getBalance());
            }
        });
        JButton bet20 = new JButton("Bet 20");
        bet20.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player.bet(20);
                currentBetValue.setText("Current Bet: " + player.getBetValue());
                balanceLabel.setText("Current Balance: " + player.getBalance());
            }
        });
        // TODO: smazat, v casinu se to taky nedělá
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
        readyToPlay = new JButton("Play");
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
                // TODO: za krupiera hraje server
                // croupierPlay();

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
