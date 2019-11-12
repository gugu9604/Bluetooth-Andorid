package com.example.a212pro.newbluetoothcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;


public class ControllerActivity extends Activity implements SensorEventListener {
    private static final String NAME = "BluetoothChat";
    //Bluetooth Serial Service 중 SerialPortServiceClass UUID 값
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter mAdapter1;
    BluetoothAdapter mAdapter2;

    BluetoothDevice device1;
    BluetoothDevice device2;

    String address;
    String chatName;

    BluetoothSocket mmSocket1, mmSocket2;
    BufferedReader in1;
    InputStream in2;
    OutputStream out1;
    OutputStream out2;
    //PrintWriter out;
    //BufferedReader in;
    Button upBtn, downBtn, leftBtn, rightBtn, stopBtn, closeBtn;
    Button openBtn, gripBtn, liftBtn;
    Switch moveSwitch;
    TextView deviceStatus;

    TextView value_water;
    TextView value_moisture;

    TextView blt_log;
    boolean sensorOn;

    private SensorManager mSensorManager;
    private Sensor mOrientation;

    BackgroundTask bt;
    boolean isThread = true;

    Thread bltTwoIn, bltTwoOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        mAdapter1 = BluetoothAdapter.getDefaultAdapter();

        Intent it = getIntent();
        String addr1 = it.getStringExtra("DeviceAddress1");
        String addr2 = it.getStringExtra("DeviceAddress2");
        for(BluetoothDevice device : mAdapter1.getBondedDevices()){

            String temp = device.getAddress().toString().substring(device.getAddress().toString().length() - 17);
            try {

                if(temp.equals(addr1)) {
                    Toast.makeText(this, device.getAddress().toString(), Toast.LENGTH_SHORT).show();
                    device1 = device;
                    if(mmSocket1 == null) {
                        mmSocket1 = device.createRfcommSocketToServiceRecord(MY_UUID);
                        mmSocket1.connect();
                        Toast.makeText(this, "dwqdqwd", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(temp.equals(addr2)) {
                    Toast.makeText(this, device.getAddress().toString(), Toast.LENGTH_SHORT).show();
                    device2 = device;
                    if(mmSocket2 == null) {
                        mmSocket2 = device.createRfcommSocketToServiceRecord(MY_UUID);
                        mmSocket2.connect();
                        Toast.makeText(this, "dwqdqwd", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch(Exception e) {e.printStackTrace();}
        }
        address = it.getStringExtra("DeviceAddress1");

        // 내 기기명을 대화명으로 사용
        chatName = mAdapter1.getName();
        leftBtn = findViewById(R.id.leftButton);
        rightBtn = findViewById(R.id.rightButton);
        deviceStatus = findViewById(R.id.deviceStatus);
        blt_log = findViewById(R.id.blt_log);


        bt = new BackgroundTask();
        bt.execute("클라이언트 시작");


        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(chatName, "l", 2, out1);
                sendMsg(chatName, "l", 2, out2);
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(chatName, "r", 2, out1);
                sendMsg(chatName, "r", 2, out2);
            }
        });

    }

    public void sendMsg(String chatName, String msg, int msgType, OutputStream out) {
        // 형식에 맞춰 서버에 메시지를 전송
        //out.println("[" + chatName + "]" + ":" + msg);
        try {
            //out.write(stringToBytesASCII(msg));
            //out.write(stringToBytes(msg));
            out.write(msg.getBytes());
            out.flush();
        } catch(Exception e) { }

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(sensorOn==false) return;
        /*
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

            if(event.values[2]<-20) {
                sendMsg(chatName, "u", 2);
                deviceStatus.setText("앞으로 가기"+ Math.round(event.values[2]));
            }
            else if(event.values[2]>20)  {
                sendMsg(chatName, "d", 2);
                deviceStatus.setText("뒤로 가기"+ Math.round(event.values[2]));
            }

            if(event.values[1]>20) {
                sendMsg(chatName, "l", 2);

                deviceStatus.setText("왼쪽으로 가기"+ Math.round(event.values[1]));
            }
            else if(event.values[1]<-20) {
                sendMsg(chatName, "r", 2);
                deviceStatus.setText("오른쪽으로 가기"+ Math.round(event.values[1]));
            }

            if(((-20<event.values[2])&&(event.values[2]<20)) &&
                ((-20<event.values[1])&&(event.values[1]<20))) {
                sendMsg(chatName, "s", 2);
                deviceStatus.setText("정지"+ Math.round(event.values[1])+","+ Math.round(event.values[2]));
            }
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class BackgroundTask extends AsyncTask<String, String, Integer> {

        protected void onPreExecute() {
            deviceStatus.setTextColor(Color.RED);
            deviceStatus.setText("연결전...");
        }

        protected Integer doInBackground(String... value) {

            publishProgress("연결중...");

            try {
                //바이트 단위의 입출력을 위한 스트림
                //in1 = mmSocket1.getInputStream();
                in1 = new BufferedReader(new InputStreamReader(mmSocket1.getInputStream()));
                out1 = mmSocket1.getOutputStream();


                in2 = mmSocket2.getInputStream();
                out2 = mmSocket2.getOutputStream();
                //스트링 단위로 한줄씩 입출력을 위한 스트림
                //in = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
                //out = new PrintWriter(new OutputStreamWriter(mmSocket.getOutputStream()));



            } catch (IOException e) {
                Log.i("connect", "Connection Error:"+e.getMessage());
                try {
                    mmSocket1.close();
                    mmSocket2.close();
                } catch (IOException e2) { e2.printStackTrace();}
                return 0;
            }

            //아두이노로부터 보내는 데이터를 읽어들이는 부분
            //현재는 아두이노에서 받는 데이터가없음

            while (isThread) {
                try {
                    // InputStream 을 통해 바이트단위로 읽어드림
                    //byte[] buffer = new byte[1024];
                    //in1.read(buffer);
//                  publishProgress(new String(buffer));

                    // BufferedReader를 통한 한줄단위로 읽어드림
                    final String msg = in1.readLine();

                    runOnUiThread(new Runnable() { @Override public void run() {
                        for(char c : msg.toCharArray()) {
                            if(c == '|') blt_log.setText(blt_log.getText() + "\r\n");
                        }
                        blt_log.append(msg);
                    } });

//                  publishProgress(msg);
                } catch (IOException e) { e.printStackTrace();}
            }
            return 0;
        }

        protected void onProgressUpdate(String... msg) {
            deviceStatus.setTextColor(Color.BLUE);
            deviceStatus.setText(msg[0]);
        }

        protected void onPostExecute(Integer result) {
            deviceStatus.setTextColor(Color.RED);
            deviceStatus.setText("연결을 종료합니다...");
        }
    }

}
