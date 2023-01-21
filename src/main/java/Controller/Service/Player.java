package Controller.Service;

import Controller.Enums.PlayerStateEnum;

public class Player {
    private String username;
    private PlayerStateEnum playerState = PlayerStateEnum.NONE;

    public Player(String username) {
        this.username = username;
    }
}
