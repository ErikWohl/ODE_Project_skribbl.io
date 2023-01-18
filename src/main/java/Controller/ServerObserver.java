package Controller;

public interface ServerObserver {
    public void onCrash(String UUID);
    public void echo(String UUID, String msg);
}
