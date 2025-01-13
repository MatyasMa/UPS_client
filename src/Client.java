import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    private JFrame lobby;
    private JTextArea chatArea;
    private JTextField messageField, nickname;
    private JButton readyButton, exitButton, controlReadyButton;


    private Player player;
    private Player opponent;

    private JLabel playerOneCards;
    private JLabel playerOneCardsValue;

    private JLabel playerTwoCards;
    private JLabel playerTwoCardsValue;

    private Player croupier;
    private JLabel LcroupierText;
    private JLabel croupierCards;
    private JLabel croupierCardsValue;

    private JLabel currentBetValue;
    private JButton hit;
    private JButton stand;
    private JButton readyToPlay;
    private JPanel bets;
    private JLabel balanceLabel;

    private int playerNumber = 0;
    private int balanceToWin = 60;

    private JFrame game;

    public Client(String serverAddress, int port) {
        // initializeGUIGame();
        try {
            // Connect to the server
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Initialize the GUI
            initializeGUILobby();

            // Start a thread to listen for server messages
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeGUILobby() {
        lobby = new JFrame("Lobby");
        lobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lobby.setSize(400, 300);

        // Chat area
//        chatArea = new JTextArea();
//        chatArea.setEditable(false);
//        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Message input field
        nickname = new JTextField(20);
//        messageField = new JTextField();
//        messageField.addActionListener(e -> sendMessage(messageField.getText()));

        // Buttons
        boolean clicked = false;
        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            if (!clicked && nickname.getText() != null && nickname.getText().length() > 0) {
                sendMessage("ready:"+nickname.getText());
            }

        });

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            sendMessage("exit");
            closeConnection();
        });

        // Layout
        JPanel panel = new JPanel(new BorderLayout());
        // panel.add(chatScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(nickname, BorderLayout.CENTER);
        // inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(readyButton);
        // buttonPanel.add(controlReadyButton);
        buttonPanel.add(exitButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        lobby.add(panel);
        lobby.setVisible(true);
    }

    private void listenForMessages() {
        try {
            String message;
            char[] buffer = new char[1024]; // Same as buffer in C
            int bytesRead;


            // Wait for data to be received (blocks until data is received)
            bytesRead = input.read(buffer);
            String[] messageParts;
            while (bytesRead != -1) {
                message = new String(buffer, 0, bytesRead);
                System.out.println("message: " + message);

                messageParts = message.split(";");
                for (String part : messageParts) {
                    System.out.println("part:"+part);
                    if (part.contains("start_game")) {
                        lobby.setVisible(false);
                        String[] parts2 = part.split(":");
                        initializeGUIGame(Integer.parseInt(parts2[1]));
                    }
                    if (part.contains("croupier_hit")) {
                        String[] s = part.split(":");
                        String card = s[1];
                        croupier.addCard(card);

                        croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
                        croupierCards.setText(croupier.getCardsText());
                        if (croupier.getCardsValue() > 17 && !player.playerLost) {
                            if (croupier.getCardsValue() == player.getCardsValue()) {
                                JOptionPane.showMessageDialog(null, "Hand result: Draw!");

                                player.draw();
                            } else if (croupier.getCardsValue() > player.getCardsValue() && croupier.getCardsValue() <= 21) {
                                JOptionPane.showMessageDialog(null, "Hand result: You lose!");

                                // lose - crupier has better cards
                                player.lose();


                                // TODO: dodělat
//                                if (player.getBalance() == 0) {
//                                    playerLoseGame();
//                                    sendMessage("game_over");
//                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Hand result: You win!");

                                player.win();

                                if (player.getBalance() >= balanceToWin) {
                                    playerWinGame();
                                    sendMessage("game_win");
                                }
                            }

                            handEnded();
                            // sendMessage("start_new_hand");
                        } else if (croupier.croupierCanPlay){
                            sendMessage("croupier_get_hit");
                        }
                        if (player.playerLost) {
                            // lose - to many
                            player.lose();
                            handEnded();

                            // TODO: dodělat
//                                if (player.getBalance() == 0) {
//                                    playerLoseGame();
//                                    sendMessage("game_over");
//                                }

                            // sendMessage("start_new_hand");
                        }
                    }
                    if (part.contains("hand_ended")) {
                        player.lose();

                        handEnded();
                    }
                    if (part.contains("ask_for_first_cards")) {
                        sendMessage("get_first_cards");
                    }
                    if (part.contains("hide_play_buttons")) {
                        hit.setVisible(false);
                        stand.setVisible(false);
                    }
                    if (part.contains("show_play_buttons")) {
                        hit.setVisible(true);
                        stand.setVisible(true);
                    }
                    if (part.contains("player_hit")) {
                        String[] s = part.split(":");
                        String player_card = s[1];

                        String[] s1 = player_card.split("_");
                        String card = s1[1];
                        int player_id = Integer.parseInt(s1[0]);

                        if (player_id == player.id) {
                            player.addCard(card);
                        } else {
                            opponent.addCard(card);
                        }


                        if (player.cards.size() > 2 && player.id == player_id) {
                            hit.setVisible(true);
                            stand.setVisible(true);
                        }

                        updatePlayerInfo(player.id);
//                        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//                        playerOneCards.setText(player.getCardsText());

                        if (player.getCardsValue() > 21) {
                            player.playerLost = true;
                            // TODO: možná JOptionPane vypisovat jako print a hned pokračovat
                            JOptionPane.showMessageDialog(null, "Hand result: To many, you lose!");
                            sendMessage("player_stand:L");
                        }
                    }
                    if (part.contains("start_croupier_play")) {
                        croupier.croupierCanPlay = true;
                        sendMessage("croupier_get_hit");
                    }
                    if (part.contains("lose")) {
                        System.out.println("you lose !!!");
                    }
                    if (part.contains("win")) {
                        System.out.println("you win !!!");
                    }
                    if (part.contains("draw")) {
                        System.out.println("remíza vole ");
                    }


//                    if (part.contains("get_card")) {
//                        playerGetHit();
//                    }

                }


                System.out.println("čekám na zprávu...");
                bytesRead = input.read(buffer);

            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void playerLoseGame() {
        System.out.println("konec hry hráč prohrál hru");
        // TODO: poslat na server a začít novou hru
        // TODO: na serveru kontrola, jestli nevyhráli oba najednou
    }

    private void playerWinGame() {
        System.out.println("konec hry hráč vyhrál");
        // TODO: poslat na server a začít novou hru
        // TODO: na serveru kontrola, jestli nevyhráli oba najednou
    }

    private void updatePlayerInfo(int player_id) {
        if (player_id == 1) {
            playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
            playerOneCards.setText(player.getCardsText());

            playerTwoCardsValue.setText("Player two cards value: "+opponent.getCardsValue());
            playerTwoCards.setText(opponent.getCardsText());
        } else {
            playerOneCardsValue.setText("Player one cards value: "+opponent.getCardsValue());
            playerOneCards.setText(opponent.getCardsText());

            playerTwoCardsValue.setText("Player two cards value: "+player.getCardsValue());
            playerTwoCards.setText(player.getCardsText());
        }

    }

    private void handEnded() {
        sendMessage("hand_end");
        refreshPlayerValues();
        opponent.clearPlayerData();
        updatePlayerInfo(player.id);
        clearTable();
        clearCroupier();
    }


    private void refreshPlayerValues() {
        player.playerLost = false;
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        updatePlayerInfo(player.id);
//        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//        playerOneCards.setText(player.getCardsText());
    }

    private void clearTable() {
        readyToPlay.setVisible(true);
        bets.setVisible(true);
        currentBetValue.setText("Current Bet: " + player.getBetValue());
        balanceLabel.setText("Current Balance: " + player.getBalance());
        updatePlayerInfo(player.id);
//        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//        playerOneCards.setText(player.getCardsText());
    }

    private void clearCroupier() {
        croupier.croupierCanPlay = false;
        croupier.clearPlayerData();
        croupierCardsValue.setText("Cards value: "+croupier.getCardsValue());
        croupierCards.setText(croupier.getCardsText());
    }

    private void playerGetHit() {
        String mess = "player_get_hit";
        sendMessage(mess);
    }

    private int getRandomNumber() {
        Random rand = new Random();
        int a = rand.nextInt(13) - 1;
        return a;
    }

    private void initializeGUIGame(int playerNumber) {
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

                sendMessage("player_stand:C");
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

                    sendMessage("ready_to_play_hand");

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


    private void sendMessage(String message) {
        if (message.equals("ready")) {
//            readyButton.setEnabled(false);
//            exitButton.setEnabled(false);
        }
        try {
            output.write(message);
            output.flush();
            // messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(lobby, "Unable to send message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client("localhost", 8080));
    }
}
