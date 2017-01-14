import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;


public class FogNode implements Runnable { //

	Socket neighbor;
	public FogNode(Socket neighbor) {
		// TODO Auto-generated constructor stub
		this.neighbor = neighbor;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try 
		{
			new update_receive(neighbor.getInputStream()).start();
			new update_send(neighbor.getOutputStream()).start();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class update_receive extends Thread
	{
		InputStreamReader client;
		update_receive(InputStream client)
		{
			this.client = new InputStreamReader(client);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader reader = null;
			try 
			{
				reader = new BufferedReader(client);
				while(true)
				{
					String in_message = reader.readLine();
					new ProcessingFogNodeRequest(in_message,neighbor).start();	
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally 
			{
				try 
				{
					client.close();
					reader.close();
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
	class update_send extends Thread
	{
		OutputStreamWriter client;
		update_send(OutputStream client)
		{
			this.client = new OutputStreamWriter(client);
		}
		@Override
		public void run() {
			BufferedWriter writer = null;
			try 
			{
				writer = new BufferedWriter(client);
				while(true)
				{
					writer.write(QueueInfo.getQueuingDelay() +" " +FogMain.my_tcp_port +"\n");
					writer.flush();
					Thread.sleep(FogMain.periodic_interval*1000);
				}
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception e)
			{
				e.printStackTrace();
			} finally
			{
				try 
				{
					client.close();
					writer.close();
				} catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
