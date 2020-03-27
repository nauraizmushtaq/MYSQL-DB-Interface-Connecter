package controllers;
	

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;


public class MainController extends Application implements Initializable{
	
	public static 	AnchorPane mp;
	public static Stage ps;
	//GUI stuff
	public Button refreshDBbtn;
	public Button addDBbtn;
	public ListView<String> DBlist;
	public AnchorPane mainPane;
	public ComboBox<String> DBMSList;
	
	//networking stuff
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	ObjectInputStream objectInFromServer = null;
	ObjectOutputStream objectOutToServer = null;
	private static final String hostName = "localhost";
	private static final int serverPort = 3738;
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/views/Main.fxml"));
			primaryStage.setTitle("DB Connect");
			primaryStage.setScene(new Scene(root,1024,720));
			primaryStage.show();
			ps=primaryStage;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void populateDBList() {
		// TODO fetch DB names info from somewhere
//		List<String> namesList= new ArrayList<String>();
//		namesList.add("MeraDB");
//		namesList.add("TeraDB");
//		namesList.add("UskaDB");
//		namesList.add("KiskaDB");
		List<String> namesList=null;
		try {
			sendFlag("2", out); //tell server to send lists of databases
			namesList = (ArrayList<String>) objectInFromServer.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		ObservableList<String> list = FXCollections.observableArrayList(namesList);
		
		if(DBlist == null) {
			System.out.println("Nope");
		}
		DBlist.setItems(list);
		DBlist.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<String>() {

			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				System.out.println("You Selected " + newValue);
				String databaseName = newValue;
				
				/* Sending database name to server */
				sendFlag("3", out); //tell server to recive DB name
				out.println(databaseName);
				out.flush();
				MetaDataController metadata = new MetaDataController(newValue,socket,in,out,objectInFromServer,objectOutToServer);
				mainPane.getChildren().clear();
				mainPane.getChildren().add(metadata);
				
			}
		});
		
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public void refreshDBList() {
		refreshDBbtn.setText("You clicked me :(");
		populateDBList();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			socket = new Socket(hostName, serverPort);
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out=new PrintWriter(socket.getOutputStream());
			objectInFromServer = new ObjectInputStream(socket.getInputStream());
			objectOutToServer = new ObjectOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp=mainPane;
		try {
			getDBMSchoice();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private void getDBMSchoice() throws Exception {
		//List <String> DBMSNames = (ArrayList<String>) objectInFromServer.readObject();
		refreshDBbtn.setDisable(true);
		addDBbtn.setDisable(true);
		
		sendFlag("0", out);//tell server to give dbms list
		List <String> DBMSNames = (ArrayList<String>) objectInFromServer.readObject();
		
		
		ArrayList<String> DBMSRequirements = new ArrayList<String>();
		
		if(DBMSList == null) System.out.println("NULL!");
		DBMSList.getItems().addAll(DBMSNames);
		DBMSList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				System.out.println("You Selected " + newValue);
				DBMSRequirements.add(newValue);
				DBMSRequirements.add("root");
				DBMSRequirements.add("");
				try {
					sendFlag("1", out); //tell server to accept dbms choice
					objectOutToServer.writeObject(DBMSRequirements);
				} catch (IOException e) {
					e.printStackTrace();
				}
				refreshDBbtn.setDisable(false);
				addDBbtn.setDisable(false);
				//TODO: send DBMS selection to sever here
				populateDBList();
			}
		});
		
	}
	
	public static void sendFlag(String flag, PrintWriter outToClient)
	{
		outToClient.flush();
		outToClient.println(flag);
		outToClient.flush();
	}
}
