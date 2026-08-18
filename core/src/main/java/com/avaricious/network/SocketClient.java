package com.avaricious.network;

import com.badlogic.gdx.Gdx;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

public class SocketClient {

    private Socket socket;
    private final String socketUrl = "https://excusably-stoppage-astonish.ngrok-free.dev";

    public void connect() {
        try {
            IO.Options opts = new IO.Options();
            OkHttpClient client = createTrustedClient();
            opts.callFactory = client;
            opts.webSocketFactory = client;

            socket = IO.socket(socketUrl, opts);

            socket.on(Socket.EVENT_CONNECT, args -> {
                Gdx.app.log("SOCKET", "Connected: " + socket.id());
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                for (Object arg : args) {
                    Gdx.app.error("SOCKET", "arg class: " + arg.getClass().getName());
                    Gdx.app.error("SOCKET", "arg value: " + arg);

                    if (arg instanceof Throwable) {
                        Throwable t = (Throwable) arg;
                        Gdx.app.error("SOCKET", "throwable", t);

                        Throwable cause = t.getCause();
                        while (cause != null) {
                            Gdx.app.error("SOCKET", "cause: " + cause.getClass().getName() + ": " + cause.getMessage());
                            cause = cause.getCause();
                        }
                    }
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void emit(String eventName) {
        if (!isConnected()) {
            Gdx.app.error("SOCKET", "Cannot emit, socket not connected: " + eventName);
            return;
        }

        socket.emit(eventName);
    }

    public void emitJson(String eventName, JsonPayloadBuilder builder) {
        if (!isConnected()) {
            Gdx.app.error("SOCKET", "Cannot emit, socket not connected: " + eventName);
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            builder.build(payload);

            socket.emit(eventName, payload);

        } catch (JSONException e) {
            Gdx.app.error("SOCKET", "JSON error while creating payload: " + eventName, e);
        } catch (Exception e) {
            Gdx.app.error("SOCKET", "Unexpected error in event: " + eventName, e);
        }
    }

    public void on(String eventName, Emitter.Listener listener) {
        socket.on(eventName, listener);
    }

    public void onJson(String eventName, JsonHandler handler) {
        socket.on(eventName, socketListener(eventName, handler));
    }

    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    public String getSocketId() {
        return socket != null ? socket.id() : null;
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
        }
    }

    private Emitter.Listener socketListener(String eventName, JsonHandler handler) {
        return args -> {
            try {
                if (args.length == 0 || !(args[0] instanceof JSONObject)) {
                    Gdx.app.error("SOCKET", "Invalid payload for event: " + eventName);
                    return;
                }

                handler.handle((JSONObject) args[0]);

            } catch (JSONException e) {
                Gdx.app.error("SOCKET", "JSON error in event: " + eventName, e);
            } catch (Exception e) {
                Gdx.app.error("SOCKET", "Unexpected error in event: " + eventName, e);
            }
        };
    }

    @FunctionalInterface
    public interface JsonHandler {
        void handle(JSONObject data) throws JSONException;
    }

    @FunctionalInterface
    public interface JsonPayloadBuilder {
        void build(JSONObject payload) throws JSONException;
    }

    private static OkHttpClient createTrustedClient() {
        try {
            // Trust all certificates - dev only!
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new SecureRandom());

            return new OkHttpClient.Builder()
                .hostnameVerifier((hostname, session) -> true)
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAll[0])
                .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
