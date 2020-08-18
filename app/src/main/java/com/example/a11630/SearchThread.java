package com.example.a11630;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.os.Handler;


class SearchThread extends Thread {

    private boolean flag = true;

    private byte[] recvDate = null;

    private byte[] sendDate = null;

    private DatagramPacket recvDP = null;

    private DatagramSocket recvDS = null;

    private DatagramSocket sendDS = null;

    private Handler mHandler;

    private StateChangeListener onStateChangeListener;

    private int state;

    private int maxDevices;//防止广播攻击，设置最大搜素数量

    public static final int STATE_INIT_FINISH = 0;

    public static final int STATE_SEND_BROADCAST = 1;

    public static final int STATE_WAITE_RESPONSE = 2;

    public static final int STATE_HANDLE_RESPONSE = 3;



    public SearchThread(Handler handler, int max) {

        recvDate = new byte[256];

        recvDP = new DatagramPacket(recvDate, 0, recvDate.length);

        mHandler = handler;

        maxDevices = max;



    }



    public void setOnStateChangeListener(StateChangeListener onStateChangeListener) {

        this.onStateChangeListener = onStateChangeListener;

    }



    public void run() {

        try {

            recvDS = new DatagramSocket(54000);//接收响应套接口

            sendDS = new DatagramSocket();//广播发送套接口



            changeState(STATE_INIT_FINISH);//更新线程状态

            //发送一次广播:广播地址255.255.255.255和组播地址224.0.1.140 --  为了防止丢包，理应多次发送

            sendDate = "name:服务器:msg:你好啊:type:search".getBytes();//设置发送数据

            DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("255.255.255.255"), 53000);//广播UDP数据包

            sendDS.send(sendDP);//发送数据包

            changeState(STATE_SEND_BROADCAST);//更新线程状态

            sendMsg("等待接收-----");//日志打印

            int curDevices = 0;//当前搜索到的设备数量

            while (flag) {

                changeState(STATE_WAITE_RESPONSE);

                recvDS.receive(recvDP);//阻塞等待接收响应

                changeState(STATE_HANDLE_RESPONSE);

                String recvContent = new String(recvDP.getData());

                //判断是不是本机发起的结束搜索请求--处理响应内容

                if (recvContent.contains("stop_search")) {

                    sendMsg("停止搜索：" + flag);

                } else {

                    if (curDevices >= maxDevices) {

                        break;

                    }

                    sendMsg("收到：" + recvDP.getAddress() + ":" + recvDP.getPort() + " 发来：" + recvContent);

                    //回应

                    sendDate = "name:服务器:msg:你好啊:type:response".getBytes();//回应内容

                    DatagramPacket responseDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 53000);//回应数据包

                    sendDS.send(responseDP);//发送回应

                    curDevices++;

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



    public void stopSearch() {

        flag = false;

        //由于在等待接收数据包时阻塞，无法达到关闭线程效果，因此给本机发送一个消息取消阻塞状态

        //为了避免用户在UI线程调用，所以新建一个线程

        new Thread() {

            @Override

            public void run() {

                if (sendDS != null) {

                    sendDate = "name:服务器:msg:stop_search:type:stop".getBytes();

                    try {

                        DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("localhost"), 54000);

                        sendDS.send(sendDP);

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            }

        }.start();

    }



    public void startSearch() {

        flag = true;

        start();

        sendMsg("开始搜索");

    }



    private void changeState(int state) {

        this.state = state;

        if (onStateChangeListener != null) {

            onStateChangeListener.onStateChanged(this.state);

        }

    }

//搜索状态更新回调

    public interface StateChangeListener {

        void onStateChanged(int state);

    }

}
