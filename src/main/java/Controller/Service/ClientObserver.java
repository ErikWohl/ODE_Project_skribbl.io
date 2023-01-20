package Controller.Service;

public interface ClientObserver {
    public void processMessage(String UUID, String message);
    public void onCrash(String UUID);
}
