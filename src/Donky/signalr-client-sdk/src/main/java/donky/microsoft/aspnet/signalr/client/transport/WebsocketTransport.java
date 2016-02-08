/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package donky.microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.net.ssl.SSLSocketFactory;

import donky.microsoft.aspnet.signalr.client.ConnectionBase;
import donky.microsoft.aspnet.signalr.client.LogLevel;
import donky.microsoft.aspnet.signalr.client.Logger;
import donky.microsoft.aspnet.signalr.client.SignalRFuture;
import donky.microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import donky.microsoft.aspnet.signalr.client.http.HttpConnection;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class WebsocketTransport extends HttpClientTransport {

    private String mPrefix;
    private static final Gson gson = new Gson();
    WebSocketClient mWebSocketClient;
    private UpdateableCancellableFuture<Void> mConnectionFuture;

    public WebsocketTransport(Logger logger) {
        super(logger);
    }

    public WebsocketTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    /**
     * This method has been modified by Donky to include following fix https://github.com/safetree/java-client/commit/364110f6a0198d996c9d4bee9a1b6ba7680cc994
     * This is also an official pull request https://github.com/SignalR/java-client/pull/88.
     */
    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = getName();
        final String connectionToken = connection.getConnectionToken();
        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";
        
        boolean isSsl = false;
        String url = null;
        try {
            url = connection.getUrl() + connectionString + '?'
                    + "connectionData=" + URLEncoder.encode(URLEncoder.encode(connectionData, "UTF-8"), "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(URLEncoder.encode(connectionToken, "UTF-8"), "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
            if(connection.getQueryString() != null) {
            	url += "&" + connection.getQueryString();
            }
            if(url.startsWith("https://")){
            	isSsl = true;
            	url = url.replace("https://", "wss://");
            } else if(url.startsWith("http://")){
            	url = url.replace("http://", "ws://");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            mConnectionFuture.triggerError(e);
            return mConnectionFuture;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), connection.getHeaders(), 0) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                mConnectionFuture.setResult(null);
            }

            @Override
            public void onMessage(String s) {
                callback.onData(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                mWebSocketClient.close();
            }

            @Override
            public void onError(Exception e) {
                mWebSocketClient.close();
            }

            @Override
            public void onFragment(Framedata frame) {
                try {
                    String decodedString = Charsetfunctions.stringUtf8(frame.getPayloadData());

                    if(decodedString.equals("]}")){
                        return;
                    }

                    if(decodedString.endsWith(":[") || null == mPrefix){
                        mPrefix = decodedString;
                        return;
                    }

                    String simpleConcatenate = mPrefix + decodedString;

                    if(isJSONValid(simpleConcatenate)){
                        onMessage(simpleConcatenate);
                    }else{
                        String extendedConcatenate = simpleConcatenate + "]}";
                        if (isJSONValid(extendedConcatenate)) {
                            onMessage(extendedConcatenate);
                        } else {
                            log("invalid json received:" + decodedString, LogLevel.Critical);
                        }
                    }
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        };
        
        if(isSsl){
        	SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try {
                mWebSocketClient.setSocket(factory.createSocket());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        mWebSocketClient.connect();

        connection.closed(new Runnable() {
            @Override
            public void run() {
                mWebSocketClient.close();
            }
        });

        return mConnectionFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        mWebSocketClient.send(data);
        return new UpdateableCancellableFuture<Void>(null);
    }

    private boolean isJSONValid(String test){
        try {
            gson.fromJson(test, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}