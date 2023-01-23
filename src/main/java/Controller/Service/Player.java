package Controller.Service;

import Controller.Enums.GameStateEnum;
import Controller.Enums.PlayerStateEnum;

public class Player {
    private String username;
    private int points = 0;
    private PlayerStateEnum playerState = PlayerStateEnum.NONE;
    private GameStateEnum gameState = GameStateEnum.INITIAL;
    public Player(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public PlayerStateEnum getPlayerState() {
        return playerState;
    }
    public GameStateEnum getGameState() {
        return gameState;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPlayerState(PlayerStateEnum playerState) {
        this.playerState = playerState;
    }
    public void setGameState(GameStateEnum gameState) {
        this.gameState = gameState;
    }
    public void resetStates(){
        gameState = GameStateEnum.INITIAL;
        playerState = PlayerStateEnum.NONE;
    }
    public int getPoints() {
        return points;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public void addPoints(int points) {
        this.points += points;
    }

    @Override
    public String toString() {
        return username + ";" + points;
    }
}
