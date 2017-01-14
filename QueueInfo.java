import java.util.LinkedList;


public class QueueInfo {
	
	public static LinkedList<QueueFormat> queue = new LinkedList<QueueFormat>();
	
	synchronized static int getQueuingDelay()
	{
		int totalQueuingDelay=0;
		for (QueueFormat entry : queue) 
		{
			totalQueuingDelay+=entry.delay;
		}
		return totalQueuingDelay;
	}
	
	synchronized static void removeFromQueue()
	{
		queue.removeFirst();
	}
	
	synchronized static QueueFormat getNextDelay()
	{
		return queue.getFirst();
	}
	
	synchronized static void insertInQueue(String data, int delay)
	{
		queue.addLast(new QueueFormat(data, delay));
	}
	
	synchronized static int getQueueLength()
	{
		return queue.size();
	}
}

class QueueFormat
{
	String data;
	int delay;
	public QueueFormat(String data, int delay) 
	{
		
		this.data = data;
		this.delay = delay;
	}
}
