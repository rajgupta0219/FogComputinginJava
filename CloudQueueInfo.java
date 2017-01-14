import java.util.LinkedList;

public class CloudQueueInfo {

	public static LinkedList<QueueFormat> queue = new LinkedList<QueueFormat>();
	
    synchronized static void insertInQueue(String data, int delay)
	{
	queue.addLast(new QueueFormat(data, delay));
	}
	
	synchronized static QueueFormat removeFromQueue()
	{
		if(queue != null || queue.size() != 0) {
			
			return queue.removeFirst();
		}
		return null;
	}
	
	synchronized static int getQueueLength()
	{
		return queue.size();
	}
}

class CloudQueue
{
	String data;
	int delay;
	public CloudQueue(String data, int delay) 
	{
		
		this.data = data;
		this.delay = delay;
	}

}
