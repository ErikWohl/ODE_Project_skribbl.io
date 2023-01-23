package Controller.Service;

public interface ClientObserver {
    public void onStart(String UUID);
    public void processMessage(String UUID, String message);
    public void onCrash(String UUID);
}
