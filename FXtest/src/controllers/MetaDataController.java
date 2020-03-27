package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.sun.javafx.event.EventHandlerManager;

import javafx.event.ActionEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MetaDataController extends VBox implements Initializable {

	public ListView<String> tablesList;
	public String tableName;
	public Text textView;
	public ContextMenu contextMenu;
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	ObjectInputStream objectInFromServer = null;
	ObjectOutputStream objectOutToServer = null;
	
	private String currentSelection;
	private double cursorX;
	private double cursorY;
	
	public MetaDataController(String tableName,Socket socket, BufferedReader in, PrintWriter out, ObjectInputStream i, ObjectOutputStream o) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/MetaData.fxml"));
		System.out.println("View loaded");
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//tablesList.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
		textView.setText(tableName);
		
		//TODO: get tables list form server for tableName
//		List<String> namesList= new ArrayList<String>();
//		namesList.add("MeraTable");
//		namesList.add("TeraTable");
//		namesList.add("UskaTable");
//		namesList.add("KiskaTable");
		
		List<String> namesList = null;
		try {
			System.out.println("Here!");
			MainController.sendFlag("4", out); //tell server to send list of tables
			namesList = (ArrayList<String>) objectInFromServer.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ObservableList<String> list = FXCollections.observableArrayList(namesList);
		tablesList.setItems(list);
		
		MenuItem item1 = new MenuItem("Show Meta Data");
        item1.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
            	System.out.println(currentSelection);
            	Alert alert = new Alert(AlertType.INFORMATION);
            	alert.setTitle("MetaData");
            	alert.setHeaderText("MetaData for table: "+currentSelection);
            	String tableName = currentSelection;
    			
    			/* Sending table Name to server */
            	MainController.sendFlag("7", out); //tell server to receive name of table
    			out.println(tableName);
    			out.flush();
    			ArrayList<String> columns = null;
				try {
					columns = (ArrayList<String>) objectInFromServer.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
    			String metdata = "";
    			for (int i = 0; i< columns.size(); ++i)
    			{
    				metdata += columns.get(i)+"\n";
    			}
            	alert.setContentText(metdata);

            	alert.showAndWait();
            }
        });
        
        MenuItem item2 = new MenuItem("Show table");
        item2.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
             TableController tc =new TableController(currentSelection,socket, in, out, objectInFromServer, objectOutToServer);
             MainController.mp.getChildren().clear();
             MainController.mp.getChildren().add(tc);
            }
        });
        
        contextMenu.getItems().addAll(item1, item2);
        
		//tablesList.getSelectionModel().sele
		tablesList.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<String>() {

			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				System.out.println("You Selected " + newValue);
				currentSelection=newValue;
				contextMenu.show(tablesList, 700, 200);
			}
		});
		
		tablesList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				cursorX=event.getX();
				cursorY=event.getY();
			}
			
		});
	}

}
