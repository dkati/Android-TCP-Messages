package com.serial.tcplistener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.serial.tcplistener.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

@SuppressWarnings({"SetTextI18n","Convert2Lambda"})
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding __binder;
    private Thread mThreadSocket = null;
    private Thread mCommunicationThread = null;

    //private String SERVER_IP;
    private int SERVER_PORT;
    private ServerSocket mServerSocket;
    private Socket mSocket;

    private boolean mIsServerActive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        __binder = ActivityMainBinding.inflate(getLayoutInflater());
        View view = __binder.getRoot();
        setContentView(view);

        __binder.tvMessages.setMovementMethod(new ScrollingMovementMethod());

        __binder.etIP.setText(getLocalIpAddress());

        __binder.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                __binder.btnConnect.setText("Stop");
                mIsServerActive = !mIsServerActive;

                if (!mIsServerActive) {
                    __binder.btnConnect.setText("Start");
                    if (mThreadSocket != null) mThreadSocket.interrupt();
                    if (mCommunicationThread != null) mCommunicationThread.interrupt();

                    mThreadSocket = null;
                    mCommunicationThread = null;

                    try {
                        mSocket.close();
                        mServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    appendToLog("Server stopped...");
                    return;
                }

                appendToLog("Starting server...");
                //SERVER_IP = __binder.etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(__binder.etPort.getText().toString().trim());

                mThreadSocket = new Thread(new SocketRunner());
                mThreadSocket.start();
                appendToLog("Server started...");
            }
        });

    }

    private void appendToLog(String s){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String currentDateTime = dateFormat.format(new Date());
        __binder.tvMessages.setText(currentDateTime+": "+s + "\n"+__binder.tvMessages.getText().toString());
    }
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    class SocketRunner implements Runnable {
        
        public void run() {
            try {
                mServerSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    mSocket = mServerSocket.accept();
                    SocketReaderRunner commThread = new SocketReaderRunner(mSocket);

                    if (mCommunicationThread != null)
                        mCommunicationThread.interrupt();

                    mCommunicationThread = new Thread(commThread);
                    mCommunicationThread.start();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SocketReaderRunner implements Runnable {
        private BufferedReader input;

        public SocketReaderRunner(Socket clientSocket) {
            try {
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (read != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appendToLog(read);
                            }
                        });

                    } else {
                        if (mThreadSocket != null)
                            mThreadSocket.interrupt();

                        mThreadSocket = new Thread(new SocketRunner());
                        mThreadSocket.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}