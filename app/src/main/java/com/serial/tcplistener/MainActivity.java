package com.serial.tcplistener;

import android.os.Bundle;
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
import java.util.Enumeration;

@SuppressWarnings({"SetTextI18n","Convert2Lambda"})
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding __binder;
    private Thread mThreadSocket = null;
    //private String SERVER_IP;
    private int SERVER_PORT;
    private ServerSocket serverSocket;
    private boolean mIsServerActive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        __binder = ActivityMainBinding.inflate(getLayoutInflater());
        View view = __binder.getRoot();
        setContentView(view);

        __binder.etIP.setText(getLocalIpAddress());

        __binder.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                __binder.btnConnect.setText("Stop server");
                mIsServerActive = !mIsServerActive;

                if (!mIsServerActive) {
                    if (mThreadSocket != null && mThreadSocket.isAlive()) {
                        mThreadSocket.interrupt();

                    }
                    __binder.btnConnect.setText("Start server");
                    __binder.tvMessages.setText("");
                    return;
                }

                __binder.tvMessages.setText("Starting server...\n");
                //SERVER_IP = __binder.etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(__binder.etPort.getText().toString().trim());

                mThreadSocket = new Thread(new SocketRunner());
                mThreadSocket.start();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mThreadSocket.interrupt();
    }

    public String getLocalIpAddress() {
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
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    SocketReaderRunner commThread = new SocketReaderRunner(socket);
                    new Thread(commThread).start();
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
                                __binder.tvMessages.setText(__binder.tvMessages.getText().toString() + "Client says: " + read + "\n");
                            }
                        });

                    } else {
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