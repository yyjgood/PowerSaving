package edu.tju.powersaving;

import java.util.ArrayList;

import android.graphics.RectF;
import android.view.Window;
import android.widget.*;
import edu.tju.powersaving.utils.Appliance;
import edu.tju.powersaving.R;


import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;


public class ControlDeviceActivity extends Activity implements View.OnClickListener {


    Context context;

    //ArrayList<Appliance> DeviceList;
    Appliance appliance;
    PacketHandler comm;
    DataSender Dsend;
    boolean IsConnectedToDevice = false;
    boolean IsReceived = false;
    private byte[] outbuffer = {0, 0, 0, 0, 0, 0, 0, 0};
    private byte[] rcvbuffer = {0, 0, 0, 0, 0, 0, 0, 0};
    private Spinner DeviceSpinner;

    private ImageButton LightButton;
    private LightDialog lightdialog;


    PowerManager pm;
    PowerManager.WakeLock wl;


    ArrayAdapter<Appliance> adapter;


    SpinnerMonitor spinner_monitor = new SpinnerMonitor();

    WifiManager Wifi;
    WifiLock wifi_lock;
    MulticastLock mcast_lock;

    ArrayList<Appliance> UnconfiguredDeviceList;
    AsyncTask<Object, Object, Object> AllUIHandler;
    IntraDeviceCommunicator IDComm;


    protected static final int Update_Spinner = 0;

    private NetThread netThread = null;

    protected static final int ADD_DEVICE = 0;
    protected static final int DISCONNECT_DEVICE = 1;
    private View MainView;
    private boolean AsyncRunning = true;
    public static int HouseWidth,HouseHeight,ScreenWidth,ScreenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplication();
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// TODO

        setContentView(R.layout.activity_control_device);

        Wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mcast_lock = Wifi.createMulticastLock("CC3xxxHomeAutomation");
        wifi_lock = Wifi.createWifiLock(WifiManager.WIFI_MODE_FULL, "CC3xxxHomeAutomation");
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "My Tag");

        wl.acquire();

        MainView=(View)findViewById(R.id.LayoutMain);

        LightButton = (ImageButton) findViewById(R.id.lightButton);

        wifi_lock.acquire();

        ScreenWidth=MainView.getWidth();
        ScreenWidth=MainView.getHeight();

        lightdialog = new LightDialog(this, UnconfiguredDeviceList, new RectF(333,425,390,580));

        UnconfiguredDeviceList = new ArrayList<Appliance>();

        UnconfiguredDeviceList.add(0,new Appliance("Disconnected", "0.0.0.0", "none"));
        adapter	= new ArrayAdapter<Appliance>(this, R.layout.spinner_element, R.id.spinnertext1, UnconfiguredDeviceList);
        DeviceSpinner.setAdapter(adapter);
        DeviceSpinner.setOnItemSelectedListener(spinner_monitor);
        appliance = UnconfiguredDeviceList.get(0);
        //dryerdialog = new DryerDialog(this, UnconfiguredDeviceList);

        IDComm = new IntraDeviceCommunicator(){
            public void SendMessage(int dest,int cmd){

                lightdialog.HandleIDMessage(dest,cmd);
                //dryerdialog.HandleIDMessage(dest,cmd);
            }
        };

        lightdialog.IDComm = IDComm;

        comm = new PacketHandler(this);
        Dsend = new DataSender();

        comm.RX_hdnlr=new PacketHandler.Socket_RX_Handler() {
            @Override
            public void handle(byte[] data) {
                for(int i=0; i<8; i++)
                    rcvbuffer[i] = data [i];
                IsReceived = true;
            }
        };

        //TODO handleAllUI_update();
        StartmDNSListener();

    }

    @Override
    public  void  onClick(View v){
        lightdialog.show();
    }


    public void StartmDNSListener(){
        netThread = new NetThread(Wifi, mcast_lock, this);
        // netThread.stop();
        netThread.start();
    }


    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADD_DEVICE) {
                Bundle Device = new Bundle();
                Device = msg.getData();
                AddNewDeviceToList(Device.getString("HOST_NAME"), Device.getString("HOST_IP"));
            }
            if (msg.what == DISCONNECT_DEVICE) {
                DisconnectDevice();
            }
            super.handleMessage(msg);
        }
    };

    private void DisconnectDevice() {
        int i;

        if (comm.ConnectionRequested) {
            Toast.makeText(context, "Device Disconnected.", Toast.LENGTH_SHORT).show();
            comm.StopSocketController();
            Dsend.kill();
            IsConnectedToDevice = false;
            IsReceived = false;

        }

        for (i = 0; i < this.UnconfiguredDeviceList.size(); i++)
            if (UnconfiguredDeviceList.get(i).Hostname.equals("Disconnected"))
                break;
        DeviceSpinner.setSelection(i);
    }

    public static interface IntraDeviceCommunicator {
        public void SendMessage(int dest, int command);
    }


    /**
     * added for auto config for different devices *
     */
    public boolean AddNewDeviceToList(String HostName, String HostIP) {

        int i;
        boolean RetVal = false;
        if (HostName.contains("CC3")) {
            Log.d("Mainactivity", " UnconfiguredDeviceList.size(): " + this.UnconfiguredDeviceList.size());
            for (i = 0; i < this.UnconfiguredDeviceList.size(); i++) {
                Log.d("Mainactivity", UnconfiguredDeviceList.get(i).Hostname + " " + UnconfiguredDeviceList.get(i).IP_Addr);

                if (UnconfiguredDeviceList.get(i).IP_Addr.equals(HostIP))
                    return false;
            }
            UnconfiguredDeviceList.add(0, new Appliance(HostName + ":" + HostIP, HostIP, "none"));
            adapter = new ArrayAdapter<Appliance>(this, R.layout.spinner_element, R.id.spinnertext1, UnconfiguredDeviceList);
            for (i = 0; i < adapter.getCount(); i++)
                if (adapter.getItem(i) == appliance)
                    break;
            DeviceSpinner.setAdapter(adapter);
            DeviceSpinner.setSelection(i);
            DeviceSpinner.setOnItemSelectedListener(spinner_monitor);
            Toast.makeText(context, "New Device Detected.", Toast.LENGTH_SHORT).show();
        }
        return RetVal;
    }

    private class DataSender extends Thread {
        boolean running = false;

        public void run() {
            running = true;
            try {
                sleep(100);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (running) {
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (comm != null) {
                    if (IsConnectedToDevice) {
                        //TODO
                        //rgblightdialog.Updatebuffer(outbuffer);

                        //windowdialog.Updatebuffer(outbuffer);

                        SendData(outbuffer);
                        Log.d("MainActivity", "Sending Data");
                    }
                }
            }
        }

        public void kill() {
            running = false;
        }
    }


    void SendData(byte[] data) {
        comm.Send(data.clone());
    }


    private class SpinnerMonitor implements OnItemSelectedListener {
        //private class SpinnerMonitor implements OnItemClickListener{
        @Override
        public void onItemSelected(AdapterView<?> Parent, View V, int Position, long id) {

            //If previously connected to another device set color of prev device to black
            //if(!appliance.Hostname.equals("Disconnected")) {
            //	DeviceSpinner.getChildAt(array_adapter.getPosition(appliance)).setBackgroundColor(0xFFFFFFFF);
            //}

            Appliance oldappliance = appliance;
            appliance = UnconfiguredDeviceList.get(Position);
            if (oldappliance.equals(appliance))
                return;
            if (appliance.Hostname.equals("Disconnected")) {

                if (comm.ConnectionRequested) {
                    Toast.makeText(context, "Disconnecting.", Toast.LENGTH_SHORT).show();
                    comm.StopSocketController();
                    Dsend.kill();
                    IsConnectedToDevice = false;
                    IsReceived = false;

                }
                //Reprogram device for generic CC3xxx service

            } else {
                comm.StopSocketController();
                Dsend.kill();
                IsConnectedToDevice = false;
                IsReceived = false;
                comm.HostName = appliance.Hostname;
                comm.s_IP_Addr = appliance.IP_Addr;
                comm.port = 3333;
                Toast.makeText(context, "Connecting to " + comm.HostName, Toast.LENGTH_SHORT).show();
                comm.StartSocketController();
                IsConnectedToDevice = true;
                Dsend = new DataSender();
                Dsend.start();
            }

        }


        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }




}
