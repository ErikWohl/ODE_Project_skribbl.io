package Controller;

public interface GameObserver {
    public void onCrash(String UUID);
    public void unicast(String UUID, String msg);
    public void multicast(String UUID, String msg);
    public void startGame();
    public void startRound();
    public void endRound();
}
