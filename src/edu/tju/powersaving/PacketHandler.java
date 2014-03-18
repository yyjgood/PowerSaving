package edu.tju.powersaving;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;




public class PacketHandler{

	public static final int FRAM_FRAME_LENGTH = 8;
	
	Socket Soc=new Socket();
	
	OutputStream outstream;
	//PrintWriter outPrint;
    InputStream instream;
    public String s_IP_Addr="192.168.1.15";
    public String HostName="000000000000.cc3000.local";
    InetSocketAddress SocAddr;
    Inet4Address Inet4_Addr;
    public int port=3333;
    public boolean ConnectionRequested=false, ReadyToSend = false, stopRXThread = true;
    private boolean SocConnected=false,pSocConnected=false;
    private boolean SocOpen=false,pSocOpen=false;
    private boolean Isconnected = false;
    
    byte[] outpktbuffer;
    
    AsyncTask<Object, Object, Object> RXHandler;
    
    ControlDeviceActivity aControlDeviceActivity;
    
    public PacketHandler(ControlDeviceActivity obj)
    {
    	aControlDeviceActivity = obj;
    }
    
	public static interface Socket_RX_Handler{
		public void handle(byte[] data);
	}
	
	
	
	Socket_RX_Handler RX_hdnlr;
	

	public void StopRXHendler(){
		stopRXThread = false;
	}
	//@Override
	public void StartRXHandler() {
		RXHandler.execute();
	}
			

	

	public void StartSocketController() {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Inet4_Addr=null;
				ConnectionRequested=true;
				
				try {
					Inet4_Addr= (Inet4Address) Inet4Address.getByName(s_IP_Addr);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				
				
				SocAddr = new InetSocketAddress(Inet4_Addr, port);
				
				Soc= new Socket();
				
				Log.d("CC3000HomeAutomationSocket", "Attempting to connect socket..."+Inet4_Addr.getHostAddress());

				while(ConnectionRequested){
					
					pSocOpen=SocOpen;
					SocOpen=!Soc.isClosed();
					
					if(pSocOpen && !SocOpen)Log.d("CC3000HomeAutomationSocket", "Socket was closed.");
					if(!pSocOpen && SocOpen)Log.d("CC3000HomeAutomationSocket", "Socket open.");


					pSocConnected=SocConnected;
					SocConnected=Soc.isConnected();
					
					if(pSocConnected && !SocConnected) {
						Log.d("CC3000HomeAutomationSocket", "Socket was disconnected.");
					}
					
					if(!pSocConnected && SocConnected) {
						Log.d("CC3000HomeAutomationSocket", "Socket connected.");

					}
					
					
					if(!SocOpen || !SocConnected){
							try{
								Soc=new Socket(s_IP_Addr,port);
								Soc.setKeepAlive(true);
								outstream = Soc.getOutputStream();
								instream = Soc.getInputStream();
								Soc.setSoTimeout(30000);
								Isconnected = true;
							} catch (UnknownHostException e) {
						        e.printStackTrace();
							} catch (IOException e) {
						        Log.d("CC3000HomeAutomationSocket", "IOexception  "+e.getMessage());
							}
					}
										
				}
				
				if(!Soc.isClosed()){
					try {
						Log.d("CC3000HomeAutomationSocket", "Socket disconnect requested. Attempting to close socket.");
						Soc.close();
						instream.close();
						outstream.close();
						Isconnected = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
					
			}
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				byte[] inpktbuffer = new byte[FRAM_FRAME_LENGTH];

				while(ConnectionRequested){
					if(Soc!=null && RX_hdnlr!=null && instream!=null) if(Soc.isConnected() && !Soc.isClosed()){
						if(Isconnected)
						{
						try {
							if(instream.read(inpktbuffer)>0){
								RX_hdnlr.handle(inpktbuffer);
								Log.d("PacketHandler","data received");
							}
						} catch (IOException e) {
							Message msg = new Message();
			                msg = aControlDeviceActivity.handler.obtainMessage();
			                msg.what =1;
			                aControlDeviceActivity.handler.sendMessage(msg);
						}
						}
					}
				}
			
		}
		}).start();
		
		
	}
		

	public void StopSocketController() {
		ConnectionRequested=false;
	}
	
	
	public void Send(final byte[] outpktbuffer){
		if(Soc.isConnected() && ConnectionRequested && !Soc.isClosed())
		{
			try {
				outstream.write(outpktbuffer);
			} catch (IOException e) {
				//e.printStackTrace();
				Message msg = new Message();
                msg = aControlDeviceActivity.handler.obtainMessage();
                msg.what =1;
                aControlDeviceActivity.handler.sendMessage(msg);
			}
			Log.d("CC3000HomeAutomationSocket", "inside send backgroung - Data sent");
		}
	}
}
