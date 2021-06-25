package dev.div0.muribaqrguide.socket;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import dev.div0.muribaqrguide.ICallbacks;
import dev.div0.muribaqrguide.Settings;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

public class AppSocket implements ISocketSender{
    private Socket socket;
    private ISocketListenerCallback socketListenerCallback;
    private ICallbacks callbacks;

    private String tag = "AppSocket";
    private String userId;
    private String deviceInfo;

    private static AppSocket instance = null;

    private AppSocket() {
    }

    public static AppSocket getInstance() {
        if (instance == null)
            instance = new AppSocket();

        return instance;
    }

    public void setUserId(String _userId){
        userId = _userId;
    }
    public void setDeviceInfo(String _deviceInfo){
        deviceInfo = _deviceInfo;
    }

    public void setSocketListenerCallback(ISocketListenerCallback _socketListenerCallback){
        socketListenerCallback = _socketListenerCallback;
    }

    public void setCallbacks(ICallbacks _callbacks){
        callbacks = _callbacks;
    }

    public boolean isConnected(){
        if(socket == null){
            return false;
        }
        else{
            return socket.connected();
        }
    }

    public void init(){
        AppSocket that = this;

        try {
            Log.d(tag, "connecting to "+ Settings.socketServerUrl);

            IO.Options options = IO.Options.builder()
                    // IO factory options
                    .setForceNew(false)
                    .setMultiplex(true)

                    // low-level engine options
                    .setTransports(new String[] { WebSocket.NAME, Polling.NAME})
                    .setUpgrade(true)
                    .setRememberUpgrade(false)
                    .setPath("/socket.io/")
                    .setQuery("userId="+userId+"&deviceInfo="+deviceInfo)
                    .setExtraHeaders(null)

                    // Manager options
                    .setReconnection(true)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setReconnectionDelay(1_000)
                    .setReconnectionDelayMax(5_000)
                    .setRandomizationFactor(0.5)
                    .setTimeout(20_000)

                    // Socket options
                    .setAuth(null)
                    .build();

            socket = IO.socket(Settings.socketServerUrl, options);

            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("onPointByIdData", onPointByIdData);
            socket.connect();

            Log.d(tag, "socket="+socket);
        }
        catch (URISyntaxException e) {
            socketListenerCallback.onSocketConnectError(e.getMessage());
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketListenerCallback.onSocketConnected();
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketListenerCallback.onSocketDisconnected();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Object exception = args[0];

            if((EngineIOException)exception!=null){
                String error = ((EngineIOException) exception).getMessage();
                socketListenerCallback.onSocketConnectError(error);
            }
            else{
                socketListenerCallback.onSocketConnectError("");
            }
        }
    };

    private Emitter.Listener onHello = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(tag, "server said hello");
        }
    };

    private Emitter.Listener onPointByIdData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(tag, "onPointByIdData");
            String data = args[0].toString();
            Log.d(tag, "data:"+data);
            callbacks.onPoint(data);

            //Log.d(tag, "server send point data: "+points);
            //Log.d(tag, "total points: "+points.length());
            //socketListenerCallback.onNearestPoints(points);
        }
    };

    @Override
    public void sendPosition(Double lat, Double lng, double distance){
    }

    @Override
    public void addPoint(Double lat, Double lng, String userId, String name, String description, int catId) throws JSONException {
    }

    @Override
    public void getNearestPoints(Double lat, Double lng, int catId, double distance) throws JSONException {
    }

    @Override
    public void getPointDescription(int pointId, String lang) throws JSONException {
        JSONObject json = new JSONObject();
        Log.d(tag,"getPointDescription");

        try {
            json.put("pointId", pointId);
            json.put("lang", lang);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("getPointDescription", json.toString());
    }

    @Override
    public void getPointById(int pointId, String lang) throws JSONException {
        JSONObject json = new JSONObject();
        Log.d(tag,"getPointById");
        try {
            json.put("pointId", pointId);
            json.put("lang", lang);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("getPointById", json.toString());
    }

    public void sendLog(String message){
        JSONObject json = new JSONObject();

        try {
            json.put("id", userId);
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("clientLog", json.toString());
    }
}
