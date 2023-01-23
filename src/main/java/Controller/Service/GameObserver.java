package Controller.Service;

public interface GameObserver {
    public void onCrash(String UUID);
    public void unicast(String UUID, String msg);
    public void multicast(String UUID, String msg);
    public void broadcast(String msg);
}
