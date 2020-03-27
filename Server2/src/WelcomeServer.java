import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class WelcomeServer {
	private static int port = 3738;
		
	public static void main(String[] args) 
	{	
		ServerSocket welcomeSocket = null;
		try
		{
			welcomeSocket = new ServerSocket(port);
			while(true)
			{
				Socket client = welcomeSocket.accept();
				System.out.println("Connected!");
				DBServer clientHandler = new DBServer(client);
				clientHandler.start();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally 
		{
			try 
			{
				if(welcomeSocket != null)
					welcomeSocket.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
