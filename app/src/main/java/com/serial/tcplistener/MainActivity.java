package com.serial.tcplistener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    String SERVER_IP;
    int SERVER_PORT;
    private ServerSocket serverSocket;
    private Handler UIHandler;

    public static String getLocalIpAddress() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        Button btnConnect = findViewById(R.id.btnConnect);

        UIHandler = new Handler();
        etIP.setText(getLocalIpAddress());

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Thread1 != null && Thread1.isAlive()) {
                    Thread1.interrupt();
                    return;
                }

                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Thread1.interrupt();
    }

    class Thread1 implements Runnable {
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
                    Thread2 commThread = new Thread2(socket);
                    new Thread(commThread).start();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Thread2 implements Runnable {
        private BufferedReader input;

        public Thread2(Socket clientSocket) {
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
                        UIHandler.post(new updateUIThread(read));
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private final String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            tvMessages.setText(tvMessages.getText().toString() + "Client says: " + msg + "\n");
        }
    }
}