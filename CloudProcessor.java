import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class CloudProcessor implements Runnable {
	String packet;
	
	public CloudProcessor(String packet){
		this.packet=packet;
	}
	
	@Override
	public void run() {

		try {
			ProcessingCloudRequest(packet);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Cloud adding Request seq " +packet.split(" ")[0] +"to Queue. Queue Size : "+CloudQueueInfo.getQueueLength());
		new IOT_send().start();
		
	}
	
    //Thread to process packet and send back to IOT request-generator
	class IOT_send extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//	super.run();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			while(true)
			{
				if(CloudQueueInfo.getQueueLength() > 0)
				{
					QueueFormat entry = CloudQueueInfo.removeFromQueue();
					if(entry == null) {
						continue;
					}
					String request = entry.data;
					int processingTime = entry.delay;
					String[] requestValues = request.split(" ");
					String IOTHostName = requestValues[3].split(":")[1];
					try {
						InetAddress IOTAddress = InetAddress.getByName(IOTHostName);
						int IOTPort = Integer.valueOf(requestValues[4].trim().split(";")[0].split(":")[1]);
						byte[] byteRequest = request.getBytes();
						Thread.sleep(processingTime*10);
						System.out.println("\nIOT Request seq" +request.split(" ")[0] +" Processed : "+request);
						DatagramPacket packet = new DatagramPacket(byteRequest, byteRequest.length, IOTAddress, IOTPort);
						
						RequestReceiver.dgSocket.send(packet);
						
					//	CloudQueueInfo.removeFromQueue();
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			try {
				Thread.sleep(200);;
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			}
		}

	}
	
	//Parse the received IOT packet from fog-node
	public void ProcessingCloudRequest(String request) throws InterruptedException
	{ 

		System.out.println("\nIOT Request " + request.split(" ")[0] +" Received from FogNode: "+FogMain.my_IP_addr.toString() +" ," +request.split(" ")[5]);
		String[] requestValues = request.split(" ");
		String sequenceNumber = requestValues[0];
		String processingTime = requestValues[1];
		String forwardLimit = requestValues[2];
		String IOTHostName = requestValues[3];
		String IOTRequestPath = requestValues[4].trim();
		String IOTPort = IOTRequestPath.split(";")[0].split(":")[1];
		int processingTimeValue = Integer.valueOf(processingTime.split(":")[1]); //
	
		//Create outgoing packet from Cloud-node
		String createReqPacket = ""; //
		createReqPacket = createReqPacket.concat(sequenceNumber);//
		createReqPacket = createReqPacket.concat(" ");//
		createReqPacket = createReqPacket.concat(processingTime);//
		createReqPacket = createReqPacket.concat(" ");//
		createReqPacket = createReqPacket.concat(forwardLimit);//
		createReqPacket = createReqPacket.concat(" ");//
		createReqPacket = createReqPacket.concat(IOTHostName);//
		createReqPacket = createReqPacket.concat(" ");//
		createReqPacket = createReqPacket.concat(IOTRequestPath);//
		createReqPacket = createReqPacket.concat(";");//
		String cloudNodeDetails = "Processed-by-Cloud as unable to be processed by fog" +":Cloud-queue length-"+CloudQueueInfo.getQueueLength();
		createReqPacket = createReqPacket.concat(cloudNodeDetails);

		//format the packet before adding to request queue
		
			CloudQueueInfo.insertInQueue(createReqPacket, processingTimeValue);
			
		}
	
	}





