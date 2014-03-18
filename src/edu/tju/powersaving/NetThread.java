package edu.tju.powersaving;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import edu.tju.powersaving.utils.Appliance;

/**
 * This thread runs in the background while the user has our
 * program in the foreground, and handles sending mDNS queries
 * and processing incoming mDNS packets.
 * @author simmons
 */
public class NetThread extends Thread{

    public static final String TAG = "NetThread";
    StringBuilder sb;
    WifiManager wifiManager;
    MulticastLock multicastLock = null;
    
    // the standard mDNS multicast address and port number
    private static final byte[] MDNS_ADDR =
        new byte[] {(byte) 224,(byte) 0,(byte) 0,(byte) 251};
    private static final int MDNS_PORT = 5353;

    private static final int BUFFER_SIZE = 4096;

    private NetworkInterface networkInterface;
    private InetAddress groupAddress;
    private MulticastSocket multicastSocket;
	
	String receivedStringData = "";
	private int offset = 12;
	private byte []recvData;
	private ArrayList<Appliance> newFoundDeviceList;	
	ControlDeviceActivity aControlDeviceActivity;
	Object object;
	private String HostName, HostIP;
	 AsyncTask<Object, Object, Object> RXHandler;
    /**
     * Construct the network thread.
     * @param activity
     */
    public NetThread(WifiManager wifiManager,  MulticastLock multicastLock, ControlDeviceActivity obj) {
    	this.wifiManager =  wifiManager;
    	this.multicastLock = multicastLock;
    	Log.d(TAG,"inside NetThread class");
    	newFoundDeviceList = new ArrayList<Appliance>();
    	aControlDeviceActivity = new ControlDeviceActivity();
    	aControlDeviceActivity = obj;
    }
    

    private InetAddress getLocalAddress()throws IOException {
    	try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        //return inetAddress.getHostAddress().toString();
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }


    /**
     * Open a multicast socket on the mDNS address and port.
     * @throws IOException
     */
    private void openSocket() throws IOException {
        multicastSocket = new MulticastSocket(MDNS_PORT);
        multicastSocket.setTimeToLive(0);
        multicastSocket.setReuseAddress(true);
        multicastSocket.setNetworkInterface(networkInterface);
        multicastSocket.joinGroup(groupAddress);
    }
    
    private NetworkInterface getFirstWifiOrEthernetInterface()
    {
    	
    	Enumeration<NetworkInterface> networkInterfaces;
    	 NetworkInterface networkIntrfc = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
	    	while (networkInterfaces.hasMoreElements())
	    	{
	    		networkIntrfc = (NetworkInterface) networkInterfaces.nextElement();
	    	    if(networkIntrfc.getName().startsWith("wlan")) {
	    	    	Log.d(TAG,networkIntrfc.getDisplayName());
	    	    	break;
	    	    }
	    	}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return networkIntrfc;

    }

    /**
     * The main network loop.  Multicast DNS packets are received,
     * processed, and sent to the UI.
     * 
     * This loop may be interrupted by closing the multicastSocket,
     * at which time any commands in the commandQueue will be
     * processed.
     */
    @Override
    public void run() {
        Log.d(TAG, "starting network thread");

      		 try {
			    		InetAddress localAddresses = getLocalAddress();//NetUtil.getLocalAddresses();
			    		networkInterface = getFirstWifiOrEthernetInterface();//netUtil.getFirstWifiOrEthernetInterface();
						if (networkInterface == null) {
			                throw new IOException("Your WiFi is not enabled.");
			            }
			            groupAddress = InetAddress.getByAddress(MDNS_ADDR); 

			            multicastLock = wifiManager.createMulticastLock("unmote");
			            multicastLock.acquire();
			            //Log.v(TAG, "acquired multicast lock: "+multicastLock);

			            openSocket();
			        } catch (IOException e1) {
			           Log.d(TAG, "exception 1"+e1.getMessage());
			        }

			        // set up the buffer for incoming packets
			        byte[] responseBuffer = new byte[BUFFER_SIZE];
			        DatagramPacket response = new DatagramPacket(responseBuffer, BUFFER_SIZE);
			 
			        int i = 0;
			        // loop!
			        while (i++<500) {
			            // zero the incoming buffer for good measure.
			            java.util.Arrays.fill(responseBuffer, (byte) 0); // clear buffer
			            
			            // receive a packet (or process an incoming command)
			            try {
			            	//multicastSocket.setTimeToLive(10);
			            	Log.d(TAG," get TTL"+multicastSocket.getTimeToLive());
			                multicastSocket.receive(response);
			            	Log.v(TAG,"received: hostname "+response.getAddress().getHostName()+" address:"+response.getSocketAddress()+"multi ");
			            	HostIP = response.getAddress().getHostName();
			            	receivedStringData  = new String(response.getData(),0 , response.getLength(), "UTF-8");
			                recvData = new byte[response.getData().length];
			                recvData = response.getData().clone();
			                decodePacketToReadable();
			                
			                Message msg = new Message();
			                msg = aControlDeviceActivity.handler.obtainMessage();
			                msg.what =0;
			                Bundle newDevice = new Bundle();
			                newDevice.putString("HOST_NAME", HostName);
			                newDevice.putString("HOST_IP", HostIP);
			                msg.setData(newDevice);
			                aControlDeviceActivity.handler.sendMessage(msg);
			                
			                Thread.sleep(500, 0);
			                response = null;
			                responseBuffer = null;
			                
			                System.gc();
			                responseBuffer = new byte[BUFFER_SIZE];
			                response = new DatagramPacket(responseBuffer, BUFFER_SIZE);
			            } catch (IOException e) {
			            	  Log.d(TAG, "exception 2");
			            } catch (InterruptedException e) {
							e.printStackTrace();
						}
			        }
			        
			        multicastSocket.close();
			        multicastLock.release();
			        multicastLock = null;

			        Log.v(TAG, "stopping network thread");
    }
    
    
 
    
    private int BytesIndexOf(byte[] Source, byte[] Search, int fromIndex) {
    	  boolean Find = false;
    	  int i;
    	  for (i = fromIndex;i<Source.length-Search.length;i++){
    	    if(Source[i]==Search[0]){
    	      Find = true;
    	      for (int j = 0;j<Search.length;j++){
    	        if (Source[i+j]!=Search[j]){
    	          Find = false;
    	        }
    	      }
    	    }
    	    if(Find){
    	      break;
    	    }
    	  }
    	  Log.d(TAG," ByteIndexOf-- "+i);
    	  if(!Find){
    	    return -1;
    	  }
    	  return i;
    	}



    private void decodePacketToReadable()
    {
    	byte[] pattern = new byte[]{0x11,(byte) 0x94};//{(byte) 0xC0, 0x0C, (byte) 0xC0, 0x2F};
    	offset = 47;//BytesIndexOf(recvData, pattern, 0) + 4;//14;
    	HostName = readName();
    	Log.d(TAG," Info-- "+HostName);//bytesToString(recvData, 13, (int)readByte()));
    		//offset =12;
    	
    }
    
    public String readName() {
        sb = new StringBuilder();
        boolean needDot = false;
        String label;
        while ((label = readLabel()) != null) {
            if (needDot) {
                sb.append('.');
            } else {
                needDot = true;
            }
            sb.append(label);
        }
        return sb.toString();
    }
    
    public byte readByte() {
        return recvData[offset++];
    }
    
    public String readString(int numBytes) {
        String string = bytesToString(recvData, offset, numBytes);
        offset += numBytes;
        return string;
    }
    
    public static String bytesToString(byte[] bytes, int offset, int length) {
        String string = null;
            try {
				string = new String(bytes, offset, length, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      
        return string;
    }
    public int toInt(byte hb, byte lb)
    {
        return ( lb<<8)>>8;
    }


    private String readLabel()
    {
    	//offset = 12;
    
    	int length = (int)  readByte();
    	Log.d(TAG," readLabel length___--"+length);
    	    	
        if (length > 63) {
            Log.d(TAG,"label length > 63");
        } else if (length <= 0 ) {
            return null;
        }
        return readString(length);
    }
    /**
     * Transmit an mDNS query on the local network.
     * @param host
     * @throws IOException
     */
    // inter-process communication
    // poor man's message queue

    private Queue<Command> commandQueue = new ConcurrentLinkedQueue<Command>();
    private static abstract class Command {
    }
    private static class QuitCommand extends Command {}
    private static class QueryCommand extends Command {
        public QueryCommand(String host) { this.host = host; }
        public String host;
    }
    public void submitQuery(String host) {
        commandQueue.offer(new QueryCommand(host));
        multicastSocket.close();
    }
    public void submitQuit() {
        commandQueue.offer(new QuitCommand());
        if (multicastSocket != null) {
            multicastSocket.close();
        }
    }


}

