
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;



public class IoTRequestProcessor implements Runnable {
	
	InetAddress IoTNodeAddress;
	int IoTRemotePort;
	String request;
	
	public IoTRequestProcessor(InetAddress IoTNodeAddress, int IoTRemotePort, String request) {
		
		this.IoTNodeAddress = IoTNodeAddress;
		this.IoTRemotePort = IoTRemotePort;
		this.request = request;
	}
	
	@Override
	public void run() {
		
		QueueInfo RequestQueue=new QueueInfo();
		int currentQueuingDelay = RequestQueue.getQueuingDelay(); // removing static can call ?
		
		//Split the incoming packet from IOT
		String[] requestValues = request.split(" ");
		String sequenceNumber = requestValues[0];
		String processingTime = requestValues[1];
		String forwardLimit = requestValues[2];
		int processingTimeValue = Integer.valueOf(processingTime.split(":")[1]); // ?
		int forwardLimitOfRequest = Integer.valueOf(forwardLimit.split(":")[1]); //?
		String IOTHostName = requestValues[3];
		String IOTPort = requestValues[4].trim();
		
        //Create outgoing packet from current Fog-node
		String createReqPacket = "";
		createReqPacket = createReqPacket.concat(sequenceNumber);
		createReqPacket = createReqPacket.concat(" ");
		createReqPacket = createReqPacket.concat(processingTime);
		createReqPacket = createReqPacket.concat(" ");
		createReqPacket = createReqPacket.concat("FL:"+forwardLimitOfRequest);
		createReqPacket = createReqPacket.concat(" ");
		createReqPacket = createReqPacket.concat(IOTHostName);
		createReqPacket = createReqPacket.concat(" ");
		createReqPacket = createReqPacket.concat(IOTPort);
		createReqPacket = createReqPacket.concat(";");
		String fogNodeDetails = "Visited_FogNode-"+FogMain.my_IP_addr.toString() +":UDP-port-" +String.valueOf(FogMain.my_udp_port) +":queueing-delay-"+RequestQueue.getQueuingDelay() +":Max-response-time-" +String.valueOf(FogMain.max_response_time);
		createReqPacket = createReqPacket.concat(fogNodeDetails);
		createReqPacket = createReqPacket.concat(" ");
		createReqPacket = createReqPacket.concat("TCP-port-" +String.valueOf(FogMain.my_tcp_port));
		forwardLimitOfRequest--;
		
		if(currentQueuingDelay + processingTimeValue <= FogMain.max_response_time && forwardLimitOfRequest>0)
		{
			System.out.println("Adding Request seq " +sequenceNumber +" to Request Queue. Size: "+RequestQueue.getQueueLength());
			RequestQueue.insertInQueue(createReqPacket, processingTimeValue);
		}
		
		//send to neighboring best fog node
		else if(forwardLimitOfRequest > 0)
		{
			int delay=9999;
			Socket socket = null;
			Iterator<?> iterator = FogMain.neighbor_node_info.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pair = (Map.Entry)iterator.next();
				if((int)pair.getValue() < delay)
				{
					socket = (Socket)pair.getKey();
					delay = (int)pair.getValue();
				}
			}
			if (socket !=null)
			{	
			System.out.println("Sending Request seq " +sequenceNumber +" to Neighbor-node: "+socket+" as Queue is at current-delay["+RequestQueue.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				writer.write(createReqPacket+"\n");
				writer.flush();
//				writer.close();
			} catch (IOException e){
				
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
			}
			//send to cloud if no neighboring fog-node to send the request
			else
			{
				System.out.println("\nAs no neighbouring nodes exist, packet seq " +sequenceNumber +" was forwarded to the Cloud as Queue is at Limit["+QueueInfo.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
				Thread cloud = new Thread(new CloudProcessor(createReqPacket));
				cloud.start();
			}
		}
		
		//send to cloud if hop-count is zero
		else if (forwardLimitOfRequest == 0)
		{   
			
			System.out.println("\nAs forwarding limit has been reached, packet seq " +sequenceNumber +" was forwarded to the Cloud as Queue is at Limit ["+QueueInfo.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
			Thread cloud = new Thread(new CloudProcessor(createReqPacket));
			cloud.start();
		}
	}
}
