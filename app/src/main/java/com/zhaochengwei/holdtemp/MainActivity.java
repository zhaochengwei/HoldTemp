package com.zhaochengwei.holdtemp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private TextView IP;
    private Button getip;
    private EditText port;
    private Button listening;
    private TextView getmessage;
    private ToggleButton switchListening;

    ServerSocket serverSocket;
    String client_IP;

    Handler handler;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IP = (TextView) findViewById(R.id.IP);
        IP.setText("IP:" + getWIFILocalIpAdress(MainActivity.this));
        listening = (Button) findViewById(R.id.listening);
        getmessage = (TextView) findViewById(R.id.getmessage);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d("show result", msg.obj.toString());
                getmessage.setText(msg.obj.toString());
                Toast.makeText(MainActivity.this,client_IP.substring(1)+"已连接到服务器",Toast.LENGTH_SHORT).show();

            }
        };
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /** 创建ServerSocket*/
                    // 创建一个ServerSocket在端口2013监听客户请求
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(8080));
                    while (true) {
                        // 侦听并接受到此Socket的连接,请求到来则产生一个Socket对象，并继续执行
                        Socket socket = serverSocket.accept();
                        Message msg = new Message();
                        //Toast.makeText(MainActivity.this,socket.getInetAddress()+"已经连接",Toast.LENGTH_SHORT).show();
                        /** 获取客户端传来的信息 */
                        client_IP=socket.getInetAddress().toString();
                        // 由Socket对象得到输入流，并构造相应的BufferedReader对象
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        // 获取从客户端读入的字符串
                        String result = bufferedReader.readLine();
                        Log.d("get result", result);
                        msg.obj = result;
                        handler.sendMessage(msg);
                        //System.out.println("Client say : " + result);

                        /** 发送服务端准备传输的 */
                        // 由Socket对象得到输出流，并构造PrintWriter对象
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.print("Server received!");
                        printWriter.flush();

                        /** 关闭Socket*/
                        printWriter.close();
                        //bufferedReader.close();
                        socket.close();

                    }
                } catch (Exception e) {
                    System.out.println("Exception:" + e);
                }
                //线程执行内容
            }
        });

        listening.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {

                //开启线程
                if (flag) {
                    Toast.makeText(MainActivity.this,"Server已打开",Toast.LENGTH_LONG).show();

                    thread.start();
                    flag = false;
                }
            }
        });


    }

    public static String getWIFILocalIpAdress(Context mContext) {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }

    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }
}

