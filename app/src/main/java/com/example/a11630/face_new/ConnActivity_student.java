package com.example.a11630.face_new;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11630.R;
import com.example.a11630.tools.ResponseThread;
import com.example.a11630.tools.SearchThread;

public class ConnActivity_student extends AppCompatActivity implements View.OnClickListener {


    private LinearLayout logContainer;

    private SearchThread searchThread;

    private ResponseThread responseThread;

    private boolean in_searching, in_response;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.now_student);

        findViewById(R.id.online).setOnClickListener(this);

        findViewById(R.id.offline).setOnClickListener(this);

        logContainer = (LinearLayout) findViewById(R.id.log);


    }


    private Handler mHandler = new Handler() {

        @Override

        public void handleMessage(Message msg) {

            super.handleMessage(msg);

            showLog((String) msg.obj);

        }

    };


    @Override

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.online:

                if (!in_response) {

                    responseThread = new ResponseThread(mHandler);

                    responseThread.startResponse();

                    in_response = true;

                } else {

                    Toast.makeText(this, "线程已经启动", Toast.LENGTH_SHORT).show();

                }

                break;

            case R.id.offline:

                if (in_response) {

                    responseThread.stopResponse();

                    in_response = false;

                } else {

                    Toast.makeText(this, "线程未启动", Toast.LENGTH_SHORT).show();

                }

                break;

        }

    }


    private void showLog(String msg) {

        TextView tv = new TextView(ConnActivity_student.this);
        /////////////////////////////
        Intent now=getIntent();
        msg=now.getStringExtra("data")+"|"+msg;
        //////////////////////////

        tv.setText(msg);

        logContainer.addView(tv);

    }
}