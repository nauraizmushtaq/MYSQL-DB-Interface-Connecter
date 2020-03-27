
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBController {
	
	public static ArrayList<String> getDatabases(DatabaseMetaData metaData) throws SQLException
	{
		ResultSet rsDatabases = null;
		ArrayList<String> databases= new ArrayList<String>();
		
		/* All Databases in MySQL */
		rsDatabases = metaData.getCatalogs();
		while (rsDatabases.next())
		{
			databases.add(rsDatabases.getString(1));
		}
		return databases;
	}
	
	public static ArrayList<String> getTables(DatabaseMetaData metaData) throws SQLException
	{
		ArrayList<String> tables = new ArrayList<String>();
		ResultSet rsTables = metaData.getTables(null, null, null, null);
		while(rsTables.next())
		{
			tables.add(rsTables.getString("TABLE_NAME"));
		}
		return tables;
	}
	
//	public static ArrayList<String[]> getColumns(DatabaseMetaData metaData, String tableName) throws SQLException
//	{
//		ArrayList<String[]> columns = new ArrayList<String[]>();
//		ResultSet rsColumns = metaData.getColumns(null, null, tableName, null);
//		while(rsColumns.next())
//		{
//			String[] types = new String[3];
//			types[0] =  rsColumns.getString("COLUMN_NAME");
//			types[1] =  rsColumns.getString("TYPE_NAME");
//			types[2] =  rsColumns.getString("COLUMN_SIZE");			
//			columns.add(types);
//		}
//		rsColumns.close();
//		return columns;	
//	}
	
	public static ArrayList<String> getColumns(Connection conn, String tableName) throws SQLException
	{
		ArrayList<String> columns = new ArrayList<String>();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
		ResultSetMetaData rsmd = rs.getMetaData();		
		for (int i = 1; i <= rsmd.getColumnCount(); ++i)
		{
			columns.add(rsmd.getColumnName(i));
		}
		return columns;
	}	
	
	
	public static ArrayList<ArrayList<String>> getRows(Connection connection, String tableName) throws SQLException
	{
		ArrayList<String> columns = getColumns(connection, tableName);
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		Statement stmt = connection.createStatement();
		String sqlQuery = new String();
		sqlQuery = "SELECT * FROM " + tableName;
		ResultSet rsRows = stmt.executeQuery(sqlQuery);
		
		while (rsRows.next())
		{
			ArrayList<String> rowData = new ArrayList<String>();
			for (int j = 0; j < columns.size(); ++j)
			{
				
				rowData.add(rsRows.getString((columns.get(j))));
			}
			rows.add(rowData);
		}
		rsRows.close();
		return rows;
	}
	
	public static boolean executeUpdate(Connection conn, String query)
	{
		try
		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			return true;
		}
		catch (SQLException ex)
		{
			return false;
		}
	}
}




