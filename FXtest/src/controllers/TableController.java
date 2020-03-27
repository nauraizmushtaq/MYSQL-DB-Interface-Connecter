package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;




public class TableController extends VBox implements Initializable {

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	ObjectInputStream objectInFromServer = null;
	ObjectOutputStream objectOutToServer = null;
	private TableView table;
	private ObservableList<ObservableList> data;
	public Pane tableLocation;
	public Button runSqlBtn,addRowbtn;
	public TextArea sqlTV;
	
	private String tableName;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// table=new Table("Student", socket, in, out, objectInFromServer, objectOutToServer);
		table=new TableView<>();
		try {
			buildTable();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		tableLocation.getChildren().add(table);
	}
	
	
	public TableController(String tableName, Socket socket, BufferedReader in, PrintWriter out, ObjectInputStream i, ObjectOutputStream o) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/TableView.fxml"));
		fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.tableName=tableName;
        this.in=in;
        this.out=out;
        this.objectInFromServer=i;
        this.objectOutToServer=o;
        this.socket=socket;
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

	}
	
	public void addRow() {
		ArrayList<TextField> textFields = new ArrayList<TextField>();
		GridPane root = new GridPane();
		root.layout();
		 MainController.sendFlag("5", out); //tell server to send table
			out.println(tableName);
			out.flush();
      ArrayList<String> columnNames=null;;
      ArrayList<ArrayList<String>> rows =null;
		try {
			columnNames = (ArrayList<String>)objectInFromServer.readObject();
			rows = (ArrayList<ArrayList<String>>) objectInFromServer.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		  

          
		for (int i = 0; i < columnNames.size(); ++i)
		{
			root.add(new Text(columnNames.get(i)), 0, i);
			TextField textField = new TextField();
			textFields.add(textField);
			root.add(textField, 1, i);
		}
		Button btn = new Button("Submit");
		root.add(btn, 1, columnNames.size());
		
		btn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent ae) 
			{
//				INSERT INTO `customer`(`fname`, `lname`, `phoneNo`, `address`) VALUES ([value-1],[value-2],[value-3],[value-4])
				
				ArrayList<String> data = new ArrayList<String>(); 
				for (int i = 0; i<textFields.size(); ++i)
				{
					data.add(textFields.get(i).getText());
				}
				try {
					String sqlQuery = insertDataInTable(tableName, data);
					System.out.println(sqlQuery);
					out.flush();
					MainController.sendFlag("6", out);
					MainController.sendFlag(sqlQuery, out);
					updateRows();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		Label secondLabel = new Label("I'm a Label on new Window");
		  
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().add(secondLabel);
        
        Scene secondScene = new Scene(secondaryLayout, 230, 100);

        // New window (Stage)
        Stage newWindow = new Stage();
        newWindow.setTitle("Second Stage");
        

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(MainController.ps);

        // Set position of second window, related to primary window.
        newWindow.setX(MainController.ps.getX() + 200);
        newWindow.setY(MainController.ps.getY() + 100);
		secondaryLayout.getChildren().add(root);
		newWindow.setScene(secondScene);
		newWindow.show();
	}
	
	public String insertDataInTable(String tableName, ArrayList<String> data) throws SQLException
	{
		
			String sqlQuery = new String();
			sqlQuery = "INSERT INTO " + tableName + " VALUES (";
			for (int i = 0; i < data.size(); i++)
			{
				sqlQuery = sqlQuery.concat("'");
				sqlQuery = sqlQuery.concat(data.get(i));
				sqlQuery = sqlQuery.concat("'");
				if (data.size() != i+1)
				{
					sqlQuery = sqlQuery.concat(",");
				}
			}
			sqlQuery = sqlQuery.concat(");");
			return sqlQuery;
	}
	
	public void runSQL(){
		out.flush();
		MainController.sendFlag("6", out);
		MainController.sendFlag(sqlTV.getText(), out);
		updateRows();
	}
	
	@SuppressWarnings("unchecked")
	public void buildTable() throws ClassNotFoundException{
		table.setEditable(true);  
         data = FXCollections.observableArrayList();
         MainController.sendFlag("5", out); //tell server to send table
			out.println(tableName);
			out.flush();
         ArrayList<String> columnNames=null;;
         ArrayList<ArrayList<String>> rows =null;
		try {
			columnNames = (ArrayList<String>)objectInFromServer.readObject();
			rows = (ArrayList<ArrayList<String>>) objectInFromServer.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
         
           /**********************************
            * TABLE COLUMN ADDED DYNAMICALLY *
            **********************************/
           for(int i=0 ; i<columnNames.size(); i++){
               //We are using non property style for making dynamic table
               final int j = i;                
               TableColumn col = new TableColumn(columnNames.get(i));
               col.setCellFactory(TextFieldTableCell.forTableColumn());
               col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){                    
                   public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {                                                                                              
                       return new SimpleStringProperty(param.getValue().get(j).toString());                        
                   }                    
               });

               final List<String> colNames=columnNames;
               col.setOnEditCommit(
            		   new EventHandler<CellEditEvent<ObservableList, String>>() {
            			   @Override
            			   public void handle(CellEditEvent<ObservableList, String> t) {
            				   int index = t.getTablePosition().getRow();
            				   System.out.println("Row:"+index);
            				   int col = t.getTablePosition().getColumn();
            				   System.out.println("Col:"+col);
            				   //show new value
            				   String nv=t.getNewValue();
            				   System.out.println(nv);
            				   
            				   //make arrays
            				   List<String> oldRow = t.getTableView().getItems().get(t.getTablePosition().getRow());
            				   List<String> newRow = new ArrayList<>(oldRow);
            				   newRow.set(col, nv);
            				   for(int i = 0;i<oldRow.size();i++) {
            					   System.out.println(oldRow.get(i));
            					   System.out.println(newRow.get(i));
            				   }
            				  
            				   String updateQuery = "UPDATE "+tableName+" SET ";
            				   for(int i=0;i<colNames.size();i++) {
            					   if(i==colNames.size()-1) {
            						   updateQuery+=""+colNames.get(i)+"='"+newRow.get(i)+"'";
            					   }
            					   else {
            						   updateQuery+=""+colNames.get(i)+"='"+newRow.get(i)+"',";
            					   }
           
            				   }
            				   updateQuery+=" WHERE ";
            				   for(int i=0;i<colNames.size();i++) {
            					   if(i==colNames.size()-1) {
            						   updateQuery+=""+colNames.get(i)+"='"+oldRow.get(i)+"';";
            					   }
            					   else {
            						   updateQuery+=""+colNames.get(i)+"='"+oldRow.get(i)+"' AND ";
            					   }
            				   }
            				  System.out.println(updateQuery);
            				  MainController.sendFlag("6", out);
            				  sqlTV.setText(updateQuery);
            				  MainController.sendFlag(updateQuery, out);
            			   }
            		   }
            		   );
               table.getColumns().addAll(col); 
               System.out.println("Column ["+i+"] ");
           }

           /********************************
            * Data added to ObservableList *
            ********************************/
           for (int i=0;i<rows.size();i++) {
        	   ObservableList<String> row = FXCollections.observableArrayList();
        	   for (int j=0;j<rows.get(i).size();j++) {
        		   row.add(rows.get(i).get(j));
        	   }
        	   data.add(row);
           }
           //FINALLY ADDED TO TableView
           table.setItems(data);
	}

	public void updateRows() {
		table.getItems().clear();
		data.clear();
		MainController.sendFlag("5", out); //tell server to send table
		out.println(tableName);
		out.flush();
		ArrayList<String> columnNames=null;;
		ArrayList<ArrayList<String>> rows =null;
		try {
			columnNames = (ArrayList<String>)objectInFromServer.readObject();
			rows = (ArrayList<ArrayList<String>>) objectInFromServer.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		/********************************
		 * Data added to ObservableList *
		 ********************************/
		for (int i=0;i<rows.size();i++) {
			ObservableList<String> row = FXCollections.observableArrayList();
			for (int j=0;j<rows.get(i).size();j++) {
				row.add(rows.get(i).get(j));
			}
			data.add(row);
		}
		//FINALLY ADDED TO TableView
		table.setItems(data);
	}
}
