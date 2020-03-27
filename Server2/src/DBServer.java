import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.ArrayList;

public class DBServer extends Thread{
	
	private BufferedReader inFromClient = null;
	private PrintWriter outToClient = null;
	private ObjectOutputStream objectOutToClient = null;
	private ObjectInputStream objectInFromClient = null;
	
	public DBServer(Socket client) 
	{
		try 
		{
			inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outToClient = new PrintWriter(client.getOutputStream(), true);
			objectOutToClient = new ObjectOutputStream(client.getOutputStream());
			objectInFromClient = new ObjectInputStream(client.getInputStream());
		} 
		catch (IOException e1)
		{
			e1.printStackTrace();
		}	
	}
	
	
	@Override
	public void run()
	{
		try
		{
			/* DBMS list */
			ArrayList<String> DBMSList = new ArrayList<String>();
			/* ADD additional DBMS Here */
			DBMSList.add("MySQL");
			Connection connection = null;
			String url = new String(), user = new String(), password = new String();
			DatabaseMetaData metaData = null;
			int flag = -10;
			while (true)
			{
				//String url = new String(), user = new String(), password = new String();
				
				try {
					flag = Integer.parseInt(inFromClient.readLine());
				} catch (NumberFormatException e) {
					e.printStackTrace();
					System.out.println("invalid input");
					flag=-10;
				}
				
				
				/* Send DBMS List to client */
				if(flag ==  0)
				{
					objectOutToClient.writeObject(DBMSList);
				}
				/* Read DBMS Name From client*/
				else if(flag == 1)
				{
					/* Read selected DBMS name */
					ArrayList<String> DBMSRequirements = (ArrayList<String>) objectInFromClient.readObject();
//					System.out.println(DBMSRequirements.get(0));
					if (DBMSRequirements.get(0).equals("MySQL"))
					{
						System.out.println("MySQL Selected");
						Class.forName("com.mysql.jdbc.Driver");
						url = "jdbc:mysql://localhost";
						user = DBMSRequirements.get(1);
						password = DBMSRequirements.get(2);
					}
					connection = DriverManager.getConnection(url, user, password);
//					System.out.println("Database is connected!");
					metaData = connection.getMetaData();
				}
				/* Send Databases names to client */
				else if (flag == 2)
				{
					/* Sending List of all databases in selected DBMS */
					metaData = connection.getMetaData();
					objectOutToClient.writeObject(DBController.getDatabases(metaData));
					objectOutToClient.flush();
				}
				/* Read Database name from client*/
				else if(flag == 3)
				{
					/* Reading selected database */
					String DBName = inFromClient.readLine();
					connection.close();
					String urlplusdb=url + "/" + DBName;
					connection = DriverManager.getConnection(urlplusdb, user, password);
					metaData = connection.getMetaData();
				}
				/* Send Tables to client */
				else if (flag == 4)
				{
					/* Sending tables of selected database */
					objectOutToClient.writeObject(DBController.getTables(metaData));
					objectOutToClient.flush();
				}
				/* Select and send Table Data*/
				else if(flag == 5)
				{
					/* Reading Table selected */
					String tableName = inFromClient.readLine();
					System.out.println(tableName+" requested!!!");
					/* Sending columns of table */
					objectOutToClient.writeObject(DBController.getColumns(connection, tableName));
					
					/* Sending rows of table */
					objectOutToClient.writeObject(DBController.getRows(connection, tableName));
				}
				else if (flag == 6)
				{
					/* Read SQL query from client */
					System.out.println("In SQL mode!");
					try {
						String query = inFromClient.readLine();
						query = query.substring(0, query.indexOf(";")+1);
						System.out.println("poop");
						System.out.println(query);
						if(DBController.executeUpdate(connection,query)) {
							System.out.println("Executed "+query);
						}
						else {
							System.out.println("not Executed ");
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Incorrect sql statement!");
					}
				}
				else if (flag == 7) {
					String tableName = inFromClient.readLine();
					objectOutToClient.writeObject(DBController.getColumns(connection, tableName));
				}
				else if (flag == -1)
				{
					break;
				}
			}
			connection.close();
		}
		catch(Exception e)
		{
			System.out.println("Do not connect to DB - Error:" + e);
			e.printStackTrace();
		}
	}
}
