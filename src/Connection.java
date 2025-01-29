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
    public JTextField nickname;
    public JButton readyButton, exitButton;
    public JTextField info;

    // CONNECTION FRAME
    public JFrame connectionFrame;
    public JTextField addressField;
    public JTextField portField;

    public String serverAddress;
    public int port;

    public Client playerClient;

    /**
     * Konstruktor.
     */
    public Connection() {
        initializeGUIConnection();
    }

    /**
     * Vytvoří okno pro zadání dat o připojení do hry (adresa a port)
     */
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

        addressField = new JTextField("147.228.67.110");
        addressField.setColumns(20);
        JPanel addressPanel = new JPanel();
        addressPanel.add(addressField);

        portField = new JTextField("7000");
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

    /**
     * Vytvoří spojení.
     */
    public void createConnection() {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            playerClient = new Client(this);
            initializeGUILobby();

            // Start a thread to listen for server messages
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            // System.exit(1);
        }
    }

    /**
     * Ukonční spojení.
     */
    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nedošlo k zavření spojení");
        }
    }

    /**
     * Odesílá zprávu na server.
     * @param message Zpráva k odeslání.
     */
    public void sendMessage(String message) {
        System.out.println("odesílám: "+message);
        try {
            output.write(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message. Attempting to reconnect...");
            // attemptReconnect();
            sendMessage(message); // Zkusíme znovu odeslat
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(lobby, "Unable to send message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Krupier dohrál, má dost karet a rozhoduje se jestli porazil hráče.
     */
    public void croupierEndHand() {
        playerClient.croupier.croupierCanPlay = false;
        if (!playerClient.player.playerLost) {
            if (playerClient.croupier.getCardsValue() == playerClient.player.getCardsValue() && playerClient.croupier.getCardsValue() <= 21) {
                playerClient.handResultInfoPlayer.setText("Hand result: Draw!");

                playerClient.player.draw();
            } else if (playerClient.croupier.getCardsValue() > playerClient.player.getCardsValue() && playerClient.croupier.getCardsValue() <= 21) {
                playerClient.handResultInfoPlayer.setText("Hand result: You lost!");

                // lose - crupier has better cards
                playerClient.player.lose();

            } else {
                playerClient.handResultInfoPlayer.setText("Hand result: You win!");

                playerClient.player.win();
            }

        }
    }

    /**
     * Hráči byla přidána karta a dochází k aktualizaci jeho stavu karet a kontrole zda nemá moc.
     * @param player_id ID hráče.
     */
    public void checkAfterAddedCard(int player_id) {
        if (playerClient.player.cards.size() > 2 && playerClient.player.id == player_id && !playerClient.player.playerLost) {
            playerClient.hit.setVisible(true);
            playerClient.stand.setVisible(true);
        }

        playerClient.updatePlayerInfo(playerClient.player.id);

        if (playerClient.player.getCardsValue() > 21 && !playerClient.player.playerLost) {
            playerClient.player.playerLost = true;
            playerClient.handResultInfoPlayer.setText("Hand result: To many, you lost!");

            // odeslani zpravy player stand - lost hand
            sendMessage("player_stand:L");
        }
    }

    // TODO: zamknout úpravu nicknamu po druhé


    private volatile long lastMessageTime; // Sledování času poslední zprávy
    private final long TIMEOUT_SECONDS = 5; // Timeout v sekundách


    private boolean pinged = false;
    private boolean threadCreated = false;
    /**
     * Čeká na zprávy přicházející od serveru a podle toho rozděluje co se má stát.
     */
    public void listenForMessages() {

        // Inicializace času poslední zprávy
        lastMessageTime = System.currentTimeMillis();


        try {
            String message;
            char[] buffer = new char[1024];
            int bytesRead;

            bytesRead = input.read(buffer); // Blokující čtení
            while (bytesRead != -1) {
                message = new String(buffer, 0, bytesRead);
                System.out.println("\nmessage: " + message);

                // Zpracování zprávy
                processServerMessage(message);


                if (pinged && !threadCreated) {
                    threadCreated = true;
                    Thread timeoutChecker = new Thread(() -> {
                        try {
                            while (true) {
                                long currentTime = System.currentTimeMillis();
                                long elapsedSeconds = (currentTime - lastMessageTime) / 1000;

                                System.out.println("Počet sekund bez přijaté zprávy: "+ elapsedSeconds);
                                if (elapsedSeconds > TIMEOUT_SECONDS) {
                                    System.out.println("Timeout: Žádná zpráva během " + TIMEOUT_SECONDS + " sekund.");
                                    playerClient.infoText.setText("Připojení k serveru bylo ztraceno.");
                                    attemptReconnect();
                                    Thread.currentThread().interrupt();
                                    break; // Ukončí vlákno po timeoutu
                                } else {
                                    System.out.println("Player is alive");
                                }
                                Thread.sleep(1000); // Kontrola každých 500 ms
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Timeout vlákno bylo přerušeno.");
                        }
                    });

                    timeoutChecker.start(); // Spuštění timeout checker vlákna
                }

                System.out.println("čekám na zprávu...");
                bytesRead = input.read(buffer); // Blokující čtení
                // Aktualizace času poslední zprávy
                lastMessageTime = System.currentTimeMillis();
            }
        } catch (IOException e) {
            System.out.println("Connection lost. Attempting to reconnect...");
//            attemptReconnect();
        } finally {
//            closeConnection();
        }
    }



    private void processServerMessage(String message) {
        String[] messageParts;
        messageParts = message.split(";");
        for (String part : messageParts) {
            if (part.contains("ping")) {
                if (!pinged) {
                    pinged = true;
                }
                sendMessage("pong");
            } else if (part.contains("reconnected")) {
                String[] parts = part.split(":");
                String nickName = parts[1];
                if (playerClient.player.getName().equals(nickName)) {
                    playerClient.infoText.setText("Hráč "+ nickName +" byl připojen zpět.\n");
                }
                System.out.println("Hráč "+ nickName +" byl připojen zpět.\n");

            } else if (part.contains("disconnected")) {
                String[] parts = part.split(":");
                String playerId = parts[1];
                String name = "";
                if (playerClient.player.id - 1 == Integer.parseInt(playerId)) {
                    name = playerClient.player.getName();
                } else {
                    name = playerClient.opponent.getName();
                }
//                JOptionPane.showMessageDialog(null, "Hráč "+ name +" byl odpojen.");
                System.out.println("Hráč "+ name +" byl odpojen.\n");
                playerClient.infoText.setText("Hráč "+ name +" byl odpojen.\n");
                // TODO: zablokovat hru dokud se nepřipojí
            } else if (part.contains("start_game")) {
                /* SPUSTENI HRY */
                lobby.setVisible(false);
                String[] parts2 = part.split(":");
                String[] parts3 = parts2[1].split("_");
                playerClient.initializeGUIGame(Integer.parseInt(parts3[0]), parts3[1], parts3[2]);
            } else if (part.contains("croupier_hit")) {
                /* KRUPIEROVI BUDE PRIDANA KARTA */
                String[] s = part.split(":");
                String card = s[1];
                playerClient.croupier.addCard(card);

                playerClient.croupierCardsValue.setText("Cards value: "+playerClient.croupier.getCardsValue());
                playerClient.croupierCards.setText(playerClient.croupier.getCardsText());
                if (playerClient.croupier.getCardsValue() >= 17) {
                    croupierEndHand();
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

            } else if (part.contains("hand_ended_for_all")) {
                /* RUKA SKONCILA PRO OBA HRACE */
                playerClient.player.clearPlayerData();
                playerClient.handEnded();
            } else if (part.contains("ask_for_first_cards")) {
                /* HRAC SI RIKA O PRVNI KARTY */
                // TODO: before this, control if a balance is 0 if yes, end game, send message to get balances
                // dočištění plátna
                playerClient.clearCroupier();
                playerClient.handResultInfoPlayer.setText("");
                playerClient.updatePlayerInfo(playerClient.player.id);
                sendMessage("get_first_cards");
            } else if (part.contains("hide_play_buttons")) {
                /* ZAKRYTI HRACICH TLACITEK */
                playerClient.hit.setVisible(false);
                playerClient.stand.setVisible(false);
            } else if (part.contains("show_play_buttons")) {
                /* ZOBRAZENI HRACICH TLACITEK */
                playerClient.hit.setVisible(true);
                playerClient.stand.setVisible(true);
            } else if (part.contains("player_hit")) {
                /* HRACI JE PRIDANA KARTA */
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

                checkAfterAddedCard(player_id);
            } else if (part.contains("start_croupier_play")) {
                /* HRACI DOHRALI, MUZE ZACIT HRAT KRUPIER */
                if (!playerClient.infoText.getText().equals(" ")) {
                    playerClient.infoText.setText(" ");
                }

                playerClient.croupier.croupierCanPlay = true;
                sendMessage("croupier_get_hit");
            } else if (part.contains("lose")) {
                /* HRAC PROHRAL CELOU HRU */
                String[] s = part.split(":");
                String[] s1 = s[1].split("_");
                String playerBalance = s1[0];
                String opponentBalance = s1[1];
//                JOptionPane.showMessageDialog(null, "Game over: YOU LOST\n" +
//                        "Your balance: "+playerBalance+"\n" +
//                        playerClient.opponent.getName()+" balance: "+opponentBalance);
                info.setText("Game over: YOU LOST,\n" +
                        "Your balance: "+playerBalance+",\n" +
                        playerClient.opponent.getName()+" balance: "+opponentBalance);
                backToLobby();
            } else if (part.contains("win")) {
                /* HRAC VYHRAL CELOU HRU */
                String[] s = part.split(":");
                String[] s1 = s[1].split("_");
                String playerBalance = s1[0];
                String opponentBalance = s1[1];
//                JOptionPane.showMessageDialog(null, "Game over: YOU WIN\n" +
//                        "Your balance: "+playerBalance+"\n" +
//                        playerClient.opponent.getName()+" balance: "+opponentBalance);
                info.setText("Game over: YOU WIN,\n" +
                        "Your balance: "+playerBalance+",\n" +
                        playerClient.opponent.getName()+" balance: "+opponentBalance);
                backToLobby();
            } else if (part.contains("draw")) {
                /* REMIZA HRACU */
                String[] s = part.split(":");
//                JOptionPane.showMessageDialog(null, "Game over: DRAW\n" +
//                        "Balance of both players "+s[1]);
                info.setText("Game over: DRAW,\n" +
                        "Balance of both players "+s[1]);
                backToLobby();
            } else if (part.contains("game_over")) {
                /* HRA SKONCILA HRAC ODESILA BALANCE NA SERVER PRO VYHODNOCENI KDO VYHRAL */
                sendMessage("balance:"+playerClient.player.getBalance());
            } else {
                // TODO: test přesunout do okna s připojením
                // JOptionPane.showMessageDialog(null, "Neznámá zpráva - došlo k odpojení.");
                try {
                    System.out.println("Přepínám na obrazovku s připojením");
                    Thread.sleep(1000);
                    closeConnection();
                    backToConnectionWindow();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // System.exit(1);
            }


        }
    }

    private void attemptReconnect() {
        closeConnection(); // Zavření starého spojení
        int attempts = 0;
        boolean connected = false;

        while (attempts < 10 && !connected) {
            try {
                System.out.println("Reconnecting... Attempt " + (attempts + 1));
                socket = new Socket(serverAddress, port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                connected = true;

                threadCreated = false;
                pinged = false;
                // Restartování posluchače zpráv
                new Thread(this::listenForMessages).start();
                playerClient.infoText.setText(" ");
                System.out.println("Reconnected successfully.");
                sendMessage("reconnected:"+playerClient.player.getName());
            } catch (IOException e) {
                attempts++;
                try {
                    Thread.sleep(3000); // Pauza mezi pokusy
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!connected) {
            // TODO: test návrat na přihlašovací obrazovku
            // JOptionPane.showMessageDialog(null, "Unable to reconnect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            try {
                System.out.println("Přepínám na obrazovku s připojením");
                Thread.sleep(1000);
                closeConnection();
                backToConnectionWindow();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //System.exit(1);
        }

        Thread.currentThread().interrupt();
    }

    private void backToConnectionWindow() {
        playerClient.game.setVisible(false);
        connectionFrame.setVisible(true);
    }

    /**
     * Zavírá hru a otevírá lobby.
     */
    private void backToLobby() {
        lobby.setVisible(true);
        playerClient.game.setVisible(false);
        // info.setText(" ");
        nickname.setEditable(true);
        readyButton.setText("Ready");
    }

    /**
     * Vytváří okno s lobby.
     */
    public void initializeGUILobby() {
        lobby = new JFrame("Lobby");
        lobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lobby.setSize(600, 300);
        lobby.setMinimumSize(new Dimension(600, 300));

        JLabel nicknameLabel = new JLabel("Nickname:");
        nickname = new JTextField(20);

        info = new JTextField(" ");
        info.setEditable(false);

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
        });

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            sendMessage("exit");
            closeConnection();
        });


        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(info);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(readyButton);

        inputPanel.add(nicknameLabel);
        inputPanel.add(nickname);
        inputPanel.add(buttonPanel);

        panel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        lobby.add(panel);
        lobby.setVisible(true);
    }
}