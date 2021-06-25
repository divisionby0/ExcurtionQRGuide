package dev.div0.muribaqrguide.socket;

import org.json.JSONException;

public interface ISocketSender {
    void sendPosition(Double lat, Double lng, double distance) throws JSONException;
    void addPoint(Double lat, Double lng, String userId, String name, String description, int catId) throws JSONException;
    void getNearestPoints(Double lat, Double lng, int catId, double distance) throws JSONException;
    void getPointDescription(int pointId, String lang) throws JSONException;
    void getPointById(int pointId, String lang) throws JSONException;
}
