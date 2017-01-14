
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
public class ProcessingFogNodeRequest extends Thread { //
	

	String request;
	Socket neighbor;
	
	public ProcessingFogNodeRequest(String request,Socket neighbor) { //
		// TODO Auto-generated constructor stub
		this.request = request;
		this.neighbor = neighbor;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		

		//neighbor MRT periodic Update
		try
		{
			int neighbor_response_time = Integer.valueOf(request.split(" ")[0]); //
			FogMain.neighbor_node_info.put(neighbor, neighbor_response_time);
			/*
			 * Code to display Periodic Updates
			 * */
			System.out.println("\nRT Update Received from: " + neighbor.getInetAddress().toString() +" with my_TCP_port," +request.split(" ")[1]);
			Iterator<?> iterator = FogMain.neighbor_node_info.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pair = (Map.Entry)iterator.next();
				System.out.println("Current Queueing delay received at localport "+ neighbor.getLocalSocketAddress().toString().split(":")[1] +" from neighboring port" +neighbor.getRemoteSocketAddress().toString().split(":")[1] +" is: " +pair.getValue());
			}
		}

		//IOTRequest-packet forwarded from Neighbor
		catch(NumberFormatException exception)
		{
			try
			{
				System.out.println("\nIOT Request " + request.split(" ")[0] +" Received from Neighbor FogNode: "+neighbor.getInetAddress().toString() +" ," +request.split(" ")[5]);
				String[] requestValues = request.split(" ");
				String sequenceNumber = requestValues[0];
				String processingTime = requestValues[1];
				String forwardLimit = requestValues[2];
				String IOTHostName = requestValues[3];
				String IOTRequestPath = requestValues[4].trim();
				String IOTPort = IOTRequestPath.split(";")[0].split(":")[1];

				int processingTimeValue = Integer.valueOf(processingTime.split(":")[1]); //
				int forwardLimitOfRequest = Integer.valueOf(forwardLimit.split(":")[1]);//
				int currentQueuingDelay = QueueInfo.getQueuingDelay();

				//Create outgoing packet from neighboring Fog-node
				String createReqPacket = ""; //
				forwardLimitOfRequest--;
				createReqPacket = createReqPacket.concat(sequenceNumber);//
				createReqPacket = createReqPacket.concat(" ");//
				createReqPacket = createReqPacket.concat(processingTime);//
				createReqPacket = createReqPacket.concat(" ");//
				createReqPacket = createReqPacket.concat("FL:"+forwardLimitOfRequest);//
				createReqPacket = createReqPacket.concat(" ");//
				createReqPacket = createReqPacket.concat(IOTHostName);//
				createReqPacket = createReqPacket.concat(" ");//
				createReqPacket = createReqPacket.concat(IOTRequestPath);//
				createReqPacket = createReqPacket.concat(";");//
				String fogNodeDetails = "Visited_FogNode-"+FogMain.my_IP_addr.toString() +":UDP-port-" +String.valueOf(FogMain.my_udp_port) +":queueing-delay-"+QueueInfo.getQueuingDelay() +":Max-response-time-" +String.valueOf(FogMain.max_response_time);
				createReqPacket = createReqPacket.concat(fogNodeDetails);
				createReqPacket = createReqPacket.concat(" ");
				createReqPacket = createReqPacket.concat("TCP-port-" +String.valueOf(FogMain.my_tcp_port));
				
				//if neighbour Node Can process request
				//format the packet before adding to request queue
				if(currentQueuingDelay + processingTimeValue <= FogMain.max_response_time && forwardLimitOfRequest>0)
				{
					System.out.println("Adding Request " +sequenceNumber +" to Queue. Queue Size : "+QueueInfo.queue.size());
					QueueInfo.insertInQueue(createReqPacket, processingTimeValue);
				}

				//if node cannot process request and forwardLimit > 0
				else if(forwardLimitOfRequest>0)
				{
					int delay=9999;
					Socket socket = null;
					Iterator<?> iterator = FogMain.neighbor_node_info.entrySet().iterator();
					while(iterator.hasNext())
					{
						Map.Entry pair = (Map.Entry)iterator.next();
						if((int)pair.getValue() < delay)
						{
						   String text = pair.getKey().toString();
						   String neighbor_check = neighbor.toString();
						   int compare = text.split(",")[1].split("=")[1].compareTo(neighbor_check.split(",")[1].split("=")[1]);
						   if(compare !=0 )
						   {
						    socket = (Socket)pair.getKey();
							delay = (int)pair.getValue();
						   }
						}
					}
					if (socket !=null)
					{
					System.out.println("Sending Request " +sequenceNumber +" to Neighbor: "+socket+" as Queue is at Limit ["+QueueInfo.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
					try {
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						writer.write(createReqPacket+"\n");
						writer.flush();
//						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
					else
					{
						System.out.println("\nAs no further neighbouring nodes exist, packet seq " +sequenceNumber +" was forwarded to the Cloud as Queue is at Limit ["+QueueInfo.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
						Thread cloud = new Thread(new CloudProcessor(createReqPacket));
						cloud.start();
					}
				}
				
				//sent to cloud
				else if (forwardLimitOfRequest == 0)
				{
					System.out.println("As forwarding limit has been reached, packet seq " +sequenceNumber +" was forwarded to the Cloud as Queue is at Limit ["+QueueInfo.getQueuingDelay()+"]" +"and processing-time of request-packet:" +processingTimeValue);
					Thread cloud = new Thread(new CloudProcessor(createReqPacket));
					cloud.start();
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
