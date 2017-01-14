import java.util.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Comparator;

public class FogMain {
	
	public static int max_response_time;
	public static int periodic_interval;
	public static InetAddress my_IP_addr;
	public static int my_udp_port;
	public static int my_tcp_port;
	public static ServerSocket my_tcp_socket;
	public static HashMap<Socket, Integer> neighbor_node_info = new HashMap<Socket, Integer>();
	public static QueueRequest delay_queue = new QueueRequest();
	
	public static void main(String[] args) 
	{
		if(args.length < 5)
		{
			System.out.println("You have not declared less than 5 mandatory arguments in Fog-node");
			System.exit(0);
		}
		else if((args.length - 5)% 2 != 0)
		{
			System.out.println("IP/Port entry count unmatched. Enter an IP/port pair for each Fog Neighbor");
			System.exit(0);
		}
		try {
			max_response_time = Integer.valueOf(args[0]);//
			periodic_interval = Integer.valueOf(args[1]);//
			my_IP_addr = InetAddress.getByName(args[2]);//
			my_udp_port = Integer.valueOf(args[3]);//
			my_tcp_port = Integer.valueOf(args[4]);//
						
			//Thread for receiving IOT request 
			Thread datagramReceiver = new Thread(new RequestReceiver(my_udp_port, my_IP_addr));
			datagramReceiver.start();
			
			//start the IOT-request worker thread
			Thread queueProcessor = new Thread(new QueueRequest());
			queueProcessor.start();
			
			for(int i=5;i<args.length;i+=2)
			{
				InetAddress neighbor_fogNode = InetAddress.getByName(args[i]);
				int neighbor_fogNodeTcpPort = Integer.valueOf(args[i+1]);//
				
				//ip-address/port comparison for TCP socket connection among fog-nodes
				int compare = my_IP_addr.toString().compareTo(neighbor_fogNode.toString());
				if((compare < 0 && my_tcp_port < neighbor_fogNodeTcpPort)|| (compare ==0 && my_tcp_port < neighbor_fogNodeTcpPort))
				{
					System.out.println("TCP request initiated with: "+ args[i] +" at port: "+args[i+1]);
					Socket neighbor_socket = new Socket(neighbor_fogNode, neighbor_fogNodeTcpPort);
					neighbor_node_info.put(neighbor_socket, 9999);
					Thread neighbor = new Thread(new FogNode(neighbor_socket));//
					neighbor.start();
				}
			}
			//start the server socket at my_TCP_port
			my_tcp_socket = new ServerSocket(my_tcp_port);
			while(true)
			{
				Socket neighbor_socket = my_tcp_socket.accept();
				neighbor_node_info.put(neighbor_socket, 9999);
				System.out.println("TCP request received from: "+ neighbor_socket.getInetAddress() + " : " + neighbor_socket.getPort());
				Thread neighbor = new Thread(new FogNode(neighbor_socket));//
				neighbor.start();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
