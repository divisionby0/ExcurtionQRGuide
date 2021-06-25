package dev.div0.muribaqrguide.socket;

public interface ISocketListenerCallback {
    void onSocketConnected();
    void onSocketDisconnected();
    void onSocketConnectError(String error);
    void onYandexAPIKey(String key);
    //void onNearestPoints(String key);
}
