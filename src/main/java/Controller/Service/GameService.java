package Controller.Service;


import Controller.Enums.CommandEnum;
import Controller.Enums.GameStateEnum;
import Controller.Enums.LanguageEnum;
import Controller.Enums.PlayerStateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GameService implements ClientObserver {
    private Logger logger = LogManager.getLogger(GameService.class);

    private GameObserver gameObserver;
    private HashMap<String, Player> playerHashMap;
    private Queue<Map.Entry<String, Player>> roundRanking;
    private final List<String> wordList;
    private int wordCount = 3;
    private int round_max = 3;
    private int current_round = 1;
    private List<String> choosableWords;
    private String chosenWord;
    private volatile boolean commence_guessing = false;
    private volatile boolean roles_set = false;
    private volatile boolean sent_chosen_word = false;
    private volatile boolean round_restart = false;
    private volatile boolean game_end = false;


    private Random rnd = new Random();
    public GameService(LanguageEnum languageEnum) {
        playerHashMap = new HashMap<>();
        //todo: Einbauen mehrerer Sprachen
        //wordList = Arrays.asList("Test", "foo", "bar", "lorem", "ipsum");
        wordList = loadWordList(languageEnum);
    }
    public void setGameObserver(GameObserver gameObserver) {
        this.gameObserver = gameObserver;
    }

    public List<String> loadWordList(LanguageEnum language) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("wordlist_" + language.getLocale() +".csv"));) {
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            logger.error("Wordlist file not found!");
            throw new RuntimeException(e);
        }
        return list;
    }

    private void hardReset() {
        for(var player : playerHashMap.entrySet()) {
            player.getValue().resetStates();
        }
        softReset();
        game_end = false;
    }
    private void softReset() {
        choosableWords = null;
        chosenWord = null;
        roundRanking = null;
        commence_guessing = false;
        roles_set = false;
        sent_chosen_word = false;
        round_restart = false;
    }

    public boolean isInitial(String UUID) {
        return playerHashMap.get(UUID).getGameState() == GameStateEnum.INITIAL;
    }
    public boolean isStarting(String UUID) {
        return playerHashMap.get(UUID).getGameState() == GameStateEnum.STARTING;
    }
    public boolean isDrawer(String UUID) {
        return playerHashMap.get(UUID).getPlayerState() == PlayerStateEnum.DRAWER;
    }
    public boolean isGuesser(String UUID) {
        return playerHashMap.get(UUID).getPlayerState() == PlayerStateEnum.GUESSER;
    }

    public void syncPlayerList(String UUID) {
        for(var player : playerHashMap.entrySet()) {
            if(!player.getKey().equals(UUID)) {
                gameObserver.unicast(UUID, CommandEnum.USER_UPDATED.getCommand() + player.getKey() + ";" + player.getValue());
            }
        }
    }

    public Player getWinner() {
        logger.debug("Searching for highest score.");
        String uuid = "";
        int points = 0;
        for(var player : playerHashMap.entrySet()) {
            if(player.getValue().getPoints() > points) {
                uuid = player.getKey();
                points = player.getValue().getPoints();
            }
        }
        gameObserver.broadcast(CommandEnum.MESSAGE.getCommand() + "Gewinner ist: " + playerHashMap.get(uuid).getUsername() + " mit " + points + " Punkten!");
        return playerHashMap.get(uuid);
    }

    public void resetPLayerList() {
        logger.debug("Reset player points to 0.");
        for(var player : playerHashMap.entrySet()) {
            player.getValue().setPoints(0);
            gameObserver.broadcast(CommandEnum.USER_UPDATED.getCommand() + player.getKey() + ";" + player.getValue());
        }
    }
    private void choosingPlayerStates() {
        int drawer = rnd.nextInt(playerHashMap.size());
        List<Map.Entry<String, Player>> list = new ArrayList<>(playerHashMap.entrySet());
        Player player = list.get(drawer).getValue();

        for(int i = 0; i < playerHashMap.size(); i++) {
            if(i == drawer) {
                playerHashMap.get(list.get(i).getKey()).setPlayerState(PlayerStateEnum.DRAWER);
                logger.info("Player (" + list.get(i).getKey() + ") with username: " + list.get(i).getValue().getUsername() + " was chosen a the drawer.");
            } else {
                playerHashMap.get(list.get(i).getKey()).setPlayerState(PlayerStateEnum.GUESSER);
                logger.info("Player (" + list.get(i).getKey() + ") with username: " + list.get(i).getValue().getUsername() + " was chosen as a guesser.");
            }
        }

        logger.info("Sending guesser requests.");
        gameObserver.multicast(list.get(drawer).getKey(), CommandEnum.GUESSER_REQUEST.getCommand());
        logger.info("Sending special drawer request.");
        gameObserver.unicast(list.get(drawer).getKey(), CommandEnum.DRAWER_REQUEST.getCommand() + chooseRandomWords());
        //todo: Auch hier überprüfung auf timeout einbauen
    }
    private String chooseRandomWords() {
        choosableWords = new ArrayList<>();

        for(int i = 0; i < wordCount; i++) {
            int r = rnd.nextInt(wordList.size());
            if(!choosableWords.contains(wordList.get(r))) {
                choosableWords.add(wordList.get(r));
            } else {
                i--;
            }
        }
        return String.join(";", choosableWords);
    }
    private void sendChosenWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chosenWord.length(); i++) {
            sb.append('_');
        }
        String blank = sb.toString();
        for(var player : playerHashMap.entrySet()) {
            if(player.getValue().getPlayerState() == PlayerStateEnum.GUESSER) {
                logger.info("Sending guesser the blank word.");
                gameObserver.unicast(player.getKey(), CommandEnum.ROUND_STARTED.getCommand()+blank);
            } else if(player.getValue().getPlayerState() == PlayerStateEnum.DRAWER) {
                logger.info("Sending the drawer the actual word.");
                gameObserver.unicast(player.getKey(), CommandEnum.ROUND_STARTED.getCommand()+chosenWord);
            } else {
                logger.error("Player (" + player.getKey() + ") is in an illegal state.");
                throw new RuntimeException();
            }
        }
    }
    public void guessingWords(String UUID, String message) {
        String chosen = chosenWord.toLowerCase();
        String word = message.substring(3).toLowerCase();
        logger.debug("Player (" + UUID + ") guess: " + word);
        if(!isGuesser(UUID)) {
            return;
        }
        if(word.length() != chosen.length()) {
            gameObserver.multicast(UUID, message);
            return;
        }

        if(word.equals(chosen)) {
            logger.info("Player (" + UUID + ") guessed the word.");

            roundRanking.add(new AbstractMap.SimpleEntry<String, Player>(UUID, playerHashMap.get(UUID)));

            //todo: besseres Punktesystem überlegen
            int points = playerHashMap.size()- roundRanking.size();
            logger.info("Player (" + UUID + ") gets:" + points + " Points.");
            playerHashMap.get(UUID).addPoints(points);
            gameObserver.broadcast(CommandEnum.MESSAGE.getCommand() + playerHashMap.get(UUID).getUsername() + " guessed the word!");
            gameObserver.broadcast(CommandEnum.USER_UPDATED.getCommand() + UUID + ";" + playerHashMap.get(UUID));
            gameObserver.unicast(UUID, CommandEnum.CORRECT_GUESS.getCommand());
            //todo: Überprüfung, ob die im Ranking vorhandenen UUIDs auch wirklich alle guesser sind
            // Könnte jemand disconnected im ranking sein?
            if (roundRanking.size() == playerHashMap.size() - 1) {
                logger.info("Alle guessers guessed the word. Initiating round end.");
                gameObserver.broadcast(CommandEnum.ROUND_END_SUCCESS.getCommand());
            }
            return;
        }

        int count = 0;
        for(int i = 0; i < chosen.length(); i++) {
            if(word.charAt(i) == chosen.charAt(i)) {
                count++;
            }
        }

        if(count + 1 == chosen.length()) {
            logger.info("Player (" + UUID + ") close guess. Word: " + chosenWord + " his: " + word);
            gameObserver.unicast(UUID, CommandEnum.CLOSE_GUESS.getCommand() + word);
            return;
        }
        gameObserver.multicast(UUID, message);
    }

    @Override
    public void onStart(String UUID) {
        logger.info("New player created: " + UUID);
        playerHashMap.put(UUID, new Player("placeholderUsername"));
    }
    @Override
    public void processMessage(String UUID, String message) {
        String command = message.substring(0, 3);

        try {
            CommandEnum commandEnum = CommandEnum.fromString(command);

            switch (commandEnum) {
                case MESSAGE: {
                    if(commence_guessing) {
                        guessingWords(UUID, message);
                    } else {
                        gameObserver.multicast(UUID, message);
                    }
                    break;
                }
                case CLEAR:
                case DRAWING: {
                    gameObserver.multicast(UUID, message);
                    break;
                }
                // Wird gesendet, wenn client sich verbindet
                case ADD_USER_REQUEST: {
                    logger.debug("Player (" + UUID + ") sent his username: " + message.substring(3));
                    playerHashMap.get(UUID).setUsername(message.substring(3));
                    gameObserver.broadcast(CommandEnum.USER_ADDED.getCommand() + UUID + ";" + message.substring(3));
                    gameObserver.broadcast(CommandEnum.USER_UPDATED.getCommand() + UUID + ";" + playerHashMap.get(UUID));
                    syncPlayerList(UUID);
                    break;
                }

                case INITIAL_GAME_REQUEST: {
                    logger.info("Trying to start a game ...");
                    //todo: Wenn es nur einen Spieler gibt soll kein spiel gestartet werden können
                    if(playerHashMap.size() < 2) {
                        gameObserver.unicast(UUID, CommandEnum.MESSAGE.getCommand() + "Not enough players for game start!");
                        return;
                    }
                    for(var player : playerHashMap.entrySet()) {
                        if(player.getValue().getGameState() != GameStateEnum.INITIAL && player.getValue().getPlayerState() != PlayerStateEnum.NONE) {
                            logger.error("Player (" + player.getKey() + ") was not in the correct state.");
                            logger.error("Sending error message.");
                            gameObserver.broadcast(CommandEnum.ERROR.getCommand());
                            hardReset();
                            return;
                        }
                    }

                    game_end = false;
                    logger.debug("Game end set: " + game_end);

                    logger.info("Sending game start request.");
                    gameObserver.broadcast(CommandEnum.START_GAME_REQUEST.getCommand() + round_max + ";" + current_round);
                    break;
                }
                case START_GAME_ACKNOWLEDGEMENT: {
                    logger.info("Player (" + UUID + ") has acknowledged the game start.");
                    playerHashMap.get(UUID).setGameState(GameStateEnum.STARTING);

                    int count = 0;
                    for(var player : playerHashMap.entrySet()) {
                        if(player.getValue().getGameState() == GameStateEnum.STARTING && player.getValue().getPlayerState() == PlayerStateEnum.NONE) {
                            count++;
                        }
                    }
                    if(count == playerHashMap.size()) {
                        logger.debug("Roles set: " + roles_set);
                        if(!roles_set) {
                            // Wird benötigt, da es trotz locking irgendwie zu mehrfachsetzung kommt
                            roles_set = true;
                            logger.debug("Roles set: " + roles_set);
                            logger.info("All players acknowledged. Choosing drawer and guessers.");
                            choosingPlayerStates();
                        }
                    }

                    //todo: Abbruch, wenn zu viel Zeit vergangen ist.
                    // Timer der nach 5 sek einen error schickt? Request timed out.
                    break;
                }
                case START_GAME_NOTACKNOWLEDGEMENT: {
                    logger.error("Player (" + UUID + ") has not acknowledged the game start.");
                    gameObserver.broadcast(CommandEnum.ERROR.getCommand());
                    hardReset();
                    break;
                }
                case DRAWER_ACKNOWLEDGEMENT:  {
                    var player = playerHashMap.get(UUID);

                    if(player.getPlayerState() == PlayerStateEnum.DRAWER) {
                        String word = message.substring(3);
                        if(!choosableWords.contains(word)) {
                            logger.error("Drawer (" + UUID + ") did not send a correct word.");
                            logger.error("Sending error message.");
                            gameObserver.broadcast(CommandEnum.ERROR.getCommand());
                            hardReset();
                            break;
                        }
                        chosenWord = word;
                        logger.info("The drawer has chosen the word: " + word);
                        logger.info("Sending round start request.");
                        gameObserver.broadcast(CommandEnum.ROUND_START_REQUEST.getCommand());
                    }
                    break;
                }
                case ROUND_START_ACKNOWLEDGEMENT: {
                    logger.info("Player (" + UUID + ") has acknowledged the round start.");
                    playerHashMap.get(UUID).setGameState(GameStateEnum.STARTED);

                    int count = 0;
                    for(var player : playerHashMap.entrySet()) {
                        if(player.getValue().getGameState() == GameStateEnum.STARTED && player.getValue().getPlayerState() != PlayerStateEnum.NONE) {
                            count++;
                        }
                    }
                    if(count == playerHashMap.size()) {
                        logger.debug("sent_chosen_word set: " + sent_chosen_word);
                        if(!sent_chosen_word) {
                            // Wird benötigt, da es trotz locking irgendwie zu mehrfachsetzung kommt
                            sent_chosen_word = true;
                            logger.debug("sent_chosen_word set: " + sent_chosen_word);
                            logger.info("All players acknowledged. Sending round started.");
                            sendChosenWord();
                            commence_guessing = true;
                            roundRanking = new LinkedList<>();
                        }
                    }

                    //todo: Abbruch, wenn zu viel Zeit vergangen ist.
                    // Timer der nach 5 sek einen error schickt? Request timed out.
                    break;
                }
                case ROUND_START_NOTACKNOWLEDGEMENT: {
                    logger.error("Player (" + UUID + ") has not acknowledged the round start.");
                    gameObserver.broadcast(CommandEnum.ERROR.getCommand());
                    hardReset();
                    break;
                }
                case ROUND_END_ACKNOWLEDGEMENT: {
                    logger.info("Player (" + UUID + ") has acknowledged the round end.");

                        playerHashMap.get(UUID).setGameState(GameStateEnum.INITIAL);
                        playerHashMap.get(UUID).setPlayerState(PlayerStateEnum.NONE);

                        int count = 0;
                        for(var player : playerHashMap.entrySet()) {
                            if(player.getValue().getGameState() == GameStateEnum.INITIAL && player.getValue().getPlayerState() == PlayerStateEnum.NONE) {
                                count++;
                            }
                        }
                        if(count == playerHashMap.size()) {
                            if(!round_restart && !game_end) {
                                // Wird benötigt, da es trotz locking irgendwie zu mehrfachsetzung kommt
                                round_restart = true;
                                if(current_round >= round_max) {
                                    game_end = true;
                                } else {
                                    current_round += 1;
                                }

                                logger.debug("round_restart set: " + round_restart);
                                logger.debug("Current round set: " + current_round);
                                logger.debug("Game end set: " + game_end);

                                logger.info("All players acknowledged.");
                                if(!game_end){
                                    logger.info("Sending a start game.");
                                    gameObserver.broadcast(CommandEnum.START_GAME_REQUEST.getCommand() + round_max + ";" + current_round);
                                    softReset();
                                } else {
                                    logger.info("Sending end game.");
                                    getWinner();
                                    gameObserver.broadcast(CommandEnum.GAME_ENDED.getCommand());
                                    current_round = 1;
                                    softReset();
                                    resetPLayerList();
                                }
                            }
                        }


                    //todo: Abbruch, wenn zu viel Zeit vergangen ist.
                    // Timer der nach 5 sek einen error schickt? Request timed out.
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("Unrecognizable command received: " + message);
        }
    }

    @Override
    public void onCrash(String UUID) {
        logger.error("Player removed: " + UUID);
        Player player = playerHashMap.remove(UUID);
        gameObserver.broadcast(CommandEnum.ERROR.getCommand());
        gameObserver.broadcast(CommandEnum.MESSAGE.getCommand() + player.getUsername() + " has disconnected!");
        gameObserver.broadcast(CommandEnum.USER_REMOVED.getCommand() + UUID + ";" + player);

        hardReset();
        gameObserver.onCrash(UUID);
    }
}
