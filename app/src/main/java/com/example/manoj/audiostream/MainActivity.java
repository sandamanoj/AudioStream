package com.example.manoj.audiostream;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.widget.Button;

//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//import org.java_websocket.drafts.Draft_17;

import java.io.*;
import com.neovisionaries.ws.client.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "AudioClient";

    // the server information
    private static final String SERVER = "xx.xx.xx.xx";
    private static final int PORT = 50005;

    // the audio recording options
    private static final int RECORDING_RATE = 24000;//44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // the button the user presses to send the audio stream to the server
    private Button sendAudioButton;

    // the audio recorder
    private AudioRecord recorder;

    // the minimum buffer size needed for audio recording
//    private static int BUFFER_SIZE = AudioRecord.getMinBufferSize(
//            RECORDING_RATE, CHANNEL, FORMAT);
    private static int BUFFER_SIZE = 960;//2048;
    // are we currently sending audio data
    private boolean currentlySendingAudio = false;

    private static final int TIMEOUT = 5000;
//    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Creating the Audio Client with minimum buffer of "
                + BUFFER_SIZE + " bytes");

        // set up the button
        sendAudioButton = (Button) findViewById(R.id.btnStart);
        sendAudioButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startStreamingAudio();
                        break;

                    case MotionEvent.ACTION_UP:
                        stopStreamingAudio();
                        break;
                }

                return false;
            }
        });
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void startStreamingAudio() {

        Log.i(TAG, "Starting the audio stream");
        currentlySendingAudio = true;
        startStreaming();
    }

    private void stopStreamingAudio() {

        Log.i(TAG, "Stopping the audio stream");
        currentlySendingAudio = false;
        recorder.release();
    }

    private void startStreaming() {

        Log.i(TAG, "Starting the background thread to stream the audio data");

        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

//                    Log.d(TAG, "Creating the datagram socket");
//                    DatagramSocket socket = new DatagramSocket();
//
                    Log.d(TAG, "Creating the buffer of size " + BUFFER_SIZE);
                    byte[] buffer = new byte[BUFFER_SIZE];
//
//                    Log.d(TAG, "Connecting to " + SERVER + ":" + PORT);
//                    final InetAddress serverAddress = InetAddress
//                            .getByName(SERVER);
//                    Log.d(TAG, "Connected to " + SERVER + ":" + PORT);
//
//                    Log.d(TAG, "Creating the reuseable DatagramPacket");
//                    DatagramPacket packet;

                    Log.d(TAG, "Creating the AudioRecord");
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE);

                    Log.d(TAG, "AudioRecord recording...");
                    recorder.startRecording();

                    // open web socket
                    WebSocket ws = connect();

                    while (currentlySendingAudio == true) {



                        // read the data into the buffer
                        int read = recorder.read(buffer, 0, buffer.length);
                        Log.d(TAG, String.valueOf(read));
                        ws.sendBinary(buffer);
//                        Log.d(TAG, String.valueOf("qwer"));
//                        // place contents of buffer into the packet
//                        packet = new DatagramPacket(buffer, read,
//                                serverAddress, PORT);
//
//                        // send the packet
//                        socket.send(packet);
                    }

                    Log.d(TAG, "AudioRecord finished recording");

                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e);
                }
            }
        });

        // start the thread
        streamThread.start();
    }

    /**
     * Connect to the server.
     */
    private static WebSocket connect() throws IOException, WebSocketException, URISyntaxException
    {
        URI uri;
        uri = new URI("ws://192.168.0.42:8000/stream/ws/publish/2tT9bp1--");
        Log.e(TAG, "Exception: " + uri);
        return new WebSocketFactory()
                .setConnectionTimeout(TIMEOUT)
                .createSocket(uri)
                .addListener(new WebSocketAdapter() {
                    // A text message arrived from the server.
                    public void onTextMessage(WebSocket websocket, String message) {
                        System.out.println(message);
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    private void connectWebSocket() {
//        URI uri;
//        try {
//            uri = new URI("ws://s1.zyzz.co:8000/stream/publish/i1rZPKCCC/");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            return;
//        }
//
//
//
//        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
//            @Override
//            public void onOpen(ServerHandshake serverHandshake) {
//                Log.i("Websocket", "Opened");
//                mWebSocketClient.send("hello");
//            }
//
//            @Override
//            public void onMessage(String s) {
////                final String message = s;
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        TextView textView = (TextView)findViewById(R.id.messages);
////                        textView.setText(textView.getText() + "\n" + message);
////                    }
////                });
//            }
//
//            @Override
//            public void onClose(int i, String s, boolean b) {
//                Log.i("Websocket", "Closed " + s);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i("Websocket", "Error " + e.getMessage());
//            }
//        };
//        mWebSocketClient.connect();
//    }
}
