import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Connection {

    public Socket socket;
    public BufferedReader input;
    public BufferedWriter output;

    // LOBBY
    public JFrame lobby;
    public JTextArea chatArea;
    public JTextField messageField, nickname;
    public JButton readyButton, exitButton, controlReadyButton;
    public JLabel info;

    // CONNECTION FRAME
    public JFrame connectionFrame;
    public JTextField addressField;
    public JTextField portField;

    public String serverAddress;
    public int port;

    public Client playerClient;

    public Connection() {
        initializeGUIConnection();
    }

    public void initializeGUIConnection() {
        connectionFrame = new JFrame("Set connection");
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setSize(400, 300);
        connectionFrame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 1));

        JLabel headline = new JLabel("Set connection");
        JPanel headlinePanel = new JPanel();
        headlinePanel.add(headline);

        JLabel info = new JLabel(" ");
        JPanel infoPanel = new JPanel();
        infoPanel.add(info);

        addressField = new JTextField("127.0.0.1");
        addressField.setColumns(20);
        JPanel addressPanel = new JPanel();
        addressPanel.add(addressField);

        portField = new JTextField("8080");
        portField.setColumns(20);
        JPanel portPanel = new JPanel();
        portPanel.add(portField);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            if (addressField.getText().length() > 0 && portField.getText().length() > 0) {
                String connectMessage = addressField.getText() + ":" + portField.getText();
                System.out.println(connectMessage);

                this.serverAddress = addressField.getText();
                this.port = Integer.parseInt(portField.getText());
                createConnection();

                connectionFrame.setVisible(false);
            } else {
                info.setText("Vyplňte všechny údaje.");
            }

        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(connectButton);

        panel.add(headlinePanel);
        panel.add(infoPanel);
        panel.add(addressPanel);
        panel.add(portPanel);
        panel.add(buttonPanel);

        connectionFrame.add(panel);
        connectionFrame.setVisible(true);
    }

    public void createConnection() {
        // initializeGUIGame();
        try {
            // Connect to the server
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Initialize the GUI
            // initializeGUIConnection();
            playerClient = new Client(this);
            initializeGUILobby();

            // Start a thread to listen for server messages
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }


    public void closeConnection() {
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

    public void sendMessage(String message) {
        System.out.println("odesílám: "+message);
        try {
            output.write(message);
            output.flush();
            // messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(lobby, "Unable to send message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void listenForMessages() {
        try {
            String message;
            char[] buffer = new char[1024]; // Same as buffer in C
            int bytesRead;


            // Wait for data to be received (blocks until data is received)
            bytesRead = input.read(buffer);
            String[] messageParts;
            while (bytesRead != -1) {
                message = new String(buffer, 0, bytesRead);
                System.out.println("\nmessage: " + message);

                messageParts = message.split(";");
                for (String part : messageParts) {
                    System.out.println("part:"+part);
                    if (part.contains("start_game")) {
                        lobby.setVisible(false);
                        String[] parts2 = part.split(":");
                        String[] parts3 = parts2[1].split("_");
                        playerClient.initializeGUIGame(Integer.parseInt(parts3[0]), parts3[1], parts3[2]);
                    }
                    if (part.contains("croupier_hit")) {
                        String[] s = part.split(":");
                        String card = s[1];
                        playerClient.croupier.addCard(card);

                        playerClient.croupierCardsValue.setText("Cards value: "+playerClient.croupier.getCardsValue());
                        playerClient.croupierCards.setText(playerClient.croupier.getCardsText());
                        if (playerClient.croupier.getCardsValue() >= 17) {
                            playerClient.croupier.croupierCanPlay = false;
                            if (!playerClient.player.playerLost) {
                                if (playerClient.croupier.getCardsValue() == playerClient.player.getCardsValue()) {
                                    // JOptionPane.showMessageDialog(null, "Hand result: Draw!");
                                    playerClient.handResultInfoPlayer.setText("Hand result: Draw!");

                                    playerClient.player.draw();
                                } else if (playerClient.croupier.getCardsValue() > playerClient.player.getCardsValue() && playerClient.croupier.getCardsValue() <= 21) {
                                    // JOptionPane.showMessageDialog(null, "Hand result: You lost!");
                                    playerClient.handResultInfoPlayer.setText("Hand result: You lost!");

                                    // lose - crupier has better cards
                                    playerClient.player.lose();


                                    // TODO: dodělat
    //                                if (player.getBalance() == 0) {
    //                                    playerLoseGame();
    //                                    sendMessage("game_over");
    //                                }
                                } else {
                                    // JOptionPane.showMessageDialog(null, "Hand result: You win!");
                                    playerClient.handResultInfoPlayer.setText("Hand result: You win!");

                                    playerClient.player.win();
                                }

                                // playerClient.croupier.croupierCanPlay = false;
    //                            playerClient.handEnded();
                                // sendMessage("start_new_hand");
                            }


                        } else if (playerClient.croupier.croupierCanPlay) {
                            sendMessage("croupier_get_hit");
                        }

                        if (!playerClient.croupier.croupierCanPlay && playerClient.croupier.cards.size() > 1) {

                            if (playerClient.player.playerLost) {
                                playerClient.player.lose();
                            }

                            if (playerClient.croupier.getCardsValue() >= 17) {
                                playerClient.handEnded();
                            }

                        }


                    }
                    if (part.contains("hand_ended_for_all")) {
                        // playerClient.player.lose();
                        playerClient.player.clearPlayerData();
                        playerClient.handEnded();
                    }
                    if (part.contains("ask_for_first_cards")) {
                        // TODO: before this, controll if a balance is 0 if yes, end game, send message to get balances
                        playerClient.clearCroupier();
                        playerClient.handResultInfoPlayer.setText("");
                        playerClient.updatePlayerInfo(playerClient.player.id);
                        sendMessage("get_first_cards");
                    }
                    if (part.contains("hide_play_buttons")) {
                        playerClient.hit.setVisible(false);
                        playerClient.stand.setVisible(false);
                    }
                    if (part.contains("show_play_buttons")) {
                        playerClient.hit.setVisible(true);
                        playerClient.stand.setVisible(true);
                    }
                    if (part.contains("player_hit")) {
                        String[] s = part.split(":");
                        String player_card = s[1];

                        String[] s1 = player_card.split("_");
                        String card = s1[1];
                        int player_id = Integer.parseInt(s1[0]);

                        if (player_id == playerClient.player.id) {
                            playerClient.player.addCard(card);
                        } else {
                            playerClient.opponent.addCard(card);
                        }


                        if (playerClient.player.cards.size() > 2 && playerClient.player.id == player_id && !playerClient.player.playerLost) {
                            playerClient.hit.setVisible(true);
                            playerClient.stand.setVisible(true);
                        }

                        playerClient.updatePlayerInfo(playerClient.player.id);
//                        playerOneCardsValue.setText("Player one cards value: "+player.getCardsValue());
//                        playerOneCards.setText(player.getCardsText());

                        if (playerClient.player.getCardsValue() > 21 && !playerClient.player.playerLost) {
                            playerClient.player.playerLost = true;
                            // TODO: možná JOptionPane vypisovat jako print a hned pokračovat
                            // JOptionPane.showMessageDialog(null, "Hand result: To many, you lost!");
                            playerClient.handResultInfoPlayer.setText("Hand result: To many, you lost!");

                            sendMessage("player_stand:L");
                        }
                    }
                    if (part.contains("start_croupier_play")) {
                        playerClient.croupier.croupierCanPlay = true;
                        sendMessage("croupier_get_hit");
                    }
                    if (part.contains("lose")) {
                        String[] s = part.split(":");
                        String[] s1 = s[1].split("_");
                        String playerBalance = s1[0];
                        String opponentBalance = s1[1];
                        JOptionPane.showMessageDialog(null, "Game over: YOU LOST\n" +
                                "Your balance: "+playerBalance+"\n" +
                                playerClient.opponent.getName()+" balance: "+opponentBalance);
                        backToLobby();
                    }
                    if (part.contains("win")) {
                        String[] s = part.split(":");
                        String[] s1 = s[1].split("_");
                        String playerBalance = s1[0];
                        String opponentBalance = s1[1];
                        JOptionPane.showMessageDialog(null, "Game over: YOU WIN\n" +
                                "Your balance: "+playerBalance+"\n" +
                                playerClient.opponent.getName()+" balance: "+opponentBalance);
                        backToLobby();
                    }
                    if (part.contains("draw")) {
                        String[] s = part.split(":");
                        JOptionPane.showMessageDialog(null, "Game over: DRAW\n" +
                                "Balance of both players "+s[1]);
                        backToLobby();
                    }
                    if (part.contains("game_over")) {
                        sendMessage("balance:"+playerClient.player.getBalance());
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

    private void backToLobby() {
        lobby.setVisible(true);
        playerClient.game.setVisible(false);
        info.setText(" ");
        nickname.setEditable(true);
        readyButton.setText("Ready");
    }

    public void initializeGUILobby() {
        lobby = new JFrame("Lobby");
        lobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lobby.setSize(600, 300);
        lobby.setMinimumSize(new Dimension(600, 300));

        // Chat area
//        chatArea = new JTextArea();
//        chatArea.setEditable(false);
//        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Message input field
        JLabel nicknameLabel = new JLabel("Nickname:");
        nickname = new JTextField(20);

        info = new JLabel(" ");

        // Buttons
        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            if (nickname.getText().length() <= 0) {
                info.setText("Enter your nickname please.");
            }
            if (nickname.getText() != null && nickname.getText().length() > 0) {
                if (readyButton.getText() == "Ready") {
                    sendMessage("ready:"+nickname.getText());
                    info.setText("Waiting for second player...");
                    readyButton.setText("Unready");
                    nickname.setEditable(false);
                } else {
                    sendMessage("unready:"+nickname.getText());
                    info.setText(" ");
                    readyButton.setText("Ready");
                    nickname.setEditable(true);
                }
            }

//            if (!clicked && nickname.getText() != null && nickname.getText().length() > 0) {
//                sendMessage("ready:"+nickname.getText());
//            }

        });

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            sendMessage("exit");
            closeConnection();
        });

        // Layout
        JPanel panel = new JPanel(new BorderLayout());
        // panel.add(chatScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout());

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(info);
        // inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(readyButton);
        // buttonPanel.add(exitButton);

        inputPanel.add(nicknameLabel);
        inputPanel.add(nickname);
        inputPanel.add(buttonPanel);

        panel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        lobby.add(panel);
        lobby.setVisible(true);
    }

}