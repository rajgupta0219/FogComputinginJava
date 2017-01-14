import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RequestReceiver implements Runnable { //
	
	int myUdpPort;
	InetAddress myAddress;
	public static DatagramSocket dgSocket;
	
	public RequestReceiver(int myUdpPort, InetAddress myAddress) {
		
		this.myUdpPort = myUdpPort;
		this.myAddress=myAddress;
		try 
		{
			dgSocket = new DatagramSocket(myUdpPort, myAddress);
			System.out.println("IOT request receiver thread Started at port "+myUdpPort);
		} 
		catch (SocketException e) 
		{
			
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		while(true)
		{
			try 
			{
				byte[] requestReceived = new byte[1024]; // making byte array of size 1024
				DatagramPacket packetFromIot = new DatagramPacket(requestReceived, requestReceived.length);
				dgSocket.receive(packetFromIot);
				InetAddress IoTSenderAddr = packetFromIot.getAddress();
				int remoteIoTPort = packetFromIot.getPort();
				String request = new String(packetFromIot.getData());
				System.out.println("\nIOT Request" + request.split(" ")[0] +" received from IOT node: "+packetFromIot.getAddress().toString() +":local UDP-port " +String.valueOf(packetFromIot.getPort()));
				
				//Thread for Request Processing
				Thread processor = new Thread(new IoTRequestProcessor(IoTSenderAddr,remoteIoTPort,request));
				processor.start();
				
			} 
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
		}
	}


}
