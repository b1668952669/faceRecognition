package com.example.a11630.tools;


import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**

 * 等待搜索线程

 */

public class ResponseThread extends Thread {

    private byte[] recvDate = null;

    private byte[] sendDate = null;

    private DatagramPacket recvDP;

    private DatagramSocket recvDS = null;

    private DatagramSocket sendDS = null;

    private boolean flag = true;

    private Handler mHandler;



    public ResponseThread(Handler handler) {

        recvDate = new byte[256];

        recvDP = new DatagramPacket(recvDate, 0, recvDate.length);

        mHandler = handler;

    }



    public void run() {

        try {

            sendMsg("设备已经开启，等待其他设备搜索...");

            recvDS = new DatagramSocket(53000);//用于接收搜索端的套接口

            sendDS = new DatagramSocket();//用于给搜索端发送确认信息

            while (flag) {

                recvDS.receive(recvDP);//阻塞等待搜索广播

                String content = new String(recvDP.getData());

                if (content.contains("response")) {

                    sendMsg("确认收到回应");

                } else if (content.contains("stop_receive")) {

                    sendMsg("下线：" + flag);

                } else {

                    sendMsg("收到：" + recvDP.getAddress() + ":" + recvDP.getPort() + " \n发来连接请求：" + content);

                    sendDate = "name:客户端:msg:已打卡:type:response".getBytes();

                    sendMsg("回应>>");

                    DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 54000);

                    sendDS.send(sendDP);

                }

            }



        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (recvDS != null)

                recvDS.close();

            if (sendDS != null)

                sendDS.close();

        }

    }



    private void sendMsg(String string) {

        Message msg = Message.obtain(mHandler);

        msg.obj = string;

        mHandler.sendMessage(msg);

    }



    public void startResponse() {

        flag = true;

        start();

        sendMsg("上线");

    }



    public void stopResponse() {

        flag = false;

        //为了避免用户在UI线程调用，所以新建一个线程

        new Thread() {

            @Override

            public void run() {

                if (sendDS != null) {

                    sendDate = "name:客户端:msg:stop_receive:type:stop".getBytes();

                    try {

                        DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("localhost"), 53000);

                        sendDS.send(sendDP);

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            }

        }.start();



    }

}
