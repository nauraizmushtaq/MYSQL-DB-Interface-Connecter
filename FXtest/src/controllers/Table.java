package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
 
public class Table extends TableView<ArrayList<String>> implements Initializable {
 
    private final ObservableList<Student> data =
            FXCollections.observableArrayList(
            new Student("haris", "i160125"),
            new Student("nauraiz", "i160106"),
            new Student("Abid", "i160229"));
    final HBox hb = new HBox();
    
    private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	ObjectInputStream objectInFromServer = null;
	ObjectOutputStream objectOutToServer = null;
    
    
 
    public Table(String tableName,Socket socket, BufferedReader in, PrintWriter out, ObjectInputStream i, ObjectOutputStream o) {
    	super();
        this.in=in;
        this.out=out;
        this.objectInFromServer=i;
        this.objectOutToServer=o;
        this.socket=socket;
	}
 
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	final Label label = new Label("Student Record");
        label.setFont(new Font("Arial", 20));
 
        this.setEditable(true);
        Callback<TableColumn, TableCell> cellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new EditingCell();
                 }
             };
 
        TableColumn NameCol = new TableColumn("First Name");
        NameCol.setMinWidth(100);
        NameCol.setCellValueFactory(
            new PropertyValueFactory<Student, String>("Name"));
        NameCol.setCellFactory(cellFactory);
        NameCol.setOnEditCommit(
            new EventHandler<CellEditEvent<Student, String>>() {
                @Override
                public void handle(CellEditEvent<Student, String> t) {
                    ((Student) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setName(t.getNewValue());
                }
             }
        ); 
 
 
        TableColumn RollnoCol = new TableColumn("Last Name");
        RollnoCol.setMinWidth(100);
        RollnoCol.setCellValueFactory(
            new PropertyValueFactory<Student, String>("Rollno"));
        RollnoCol.setCellFactory(cellFactory);
        RollnoCol.setOnEditCommit(
            new EventHandler<CellEditEvent<Student, String>>() {
                @Override
                public void handle(CellEditEvent<Student, String> t) {
                    ((Student) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setRollno(t.getNewValue());
                }
            }
        );
 
     
       // this.setItems(data);
        this.getColumns().addAll(NameCol, RollnoCol);
 
        final TextField addName = new TextField();
        addName.setPromptText("First Name");
        addName.setMaxWidth(NameCol.getPrefWidth());
        final TextField addRollno = new TextField();
        addRollno.setMaxWidth(RollnoCol.getPrefWidth());
        addRollno.setPromptText("Last Name");
       
        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                data.add(new Student(
                        addName.getText(),
                        addRollno.getText()));
                addName.clear();
                addRollno.clear();
             
            }
        });
 
        hb.getChildren().addAll(addName, addRollno,addButton);
        hb.setSpacing(3);
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, this, hb);
    }
    
//    @Override
//    public void start(Stage stage) {
//        Scene scene = new Scene(new Group());
//        stage.setTitle("Ye Lo Haris");
//        stage.setWidth(450);
//        stage.setHeight(550);
// 
//        final Label label = new Label("Student Record");
//        label.setFont(new Font("Arial", 20));
// 
//        this.setEditable(true);
//        Callback<TableColumn, TableCell> cellFactory =
//             new Callback<TableColumn, TableCell>() {
//                 public TableCell call(TableColumn p) {
//                    return new EditingCell();
//                 }
//             };
// 
//        TableColumn NameCol = new TableColumn("First Name");
//        NameCol.setMinWidth(100);
//        NameCol.setCellValueFactory(
//            new PropertyValueFactory<Student, String>("Name"));
//        NameCol.setCellFactory(cellFactory);
//        NameCol.setOnEditCommit(
//            new EventHandler<CellEditEvent<Student, String>>() {
//                @Override
//                public void handle(CellEditEvent<Student, String> t) {
//                    ((Student) t.getTableView().getItems().get(
//                        t.getTablePosition().getRow())
//                        ).setName(t.getNewValue());
//                }
//             }
//        ); 
// 
// 
//        TableColumn RollnoCol = new TableColumn("Last Name");
//        RollnoCol.setMinWidth(100);
//        RollnoCol.setCellValueFactory(
//            new PropertyValueFactory<Student, String>("Rollno"));
//        RollnoCol.setCellFactory(cellFactory);
//        RollnoCol.setOnEditCommit(
//            new EventHandler<CellEditEvent<Student, String>>() {
//                @Override
//                public void handle(CellEditEvent<Student, String> t) {
//                    ((Student) t.getTableView().getItems().get(
//                        t.getTablePosition().getRow())
//                        ).setRollno(t.getNewValue());
//                }
//            }
//        );
// 
//     
//        this.setItems(data);
//        this.getColumns().addAll(NameCol, RollnoCol);
// 
//        final TextField addName = new TextField();
//        addName.setPromptText("First Name");
//        addName.setMaxWidth(NameCol.getPrefWidth());
//        final TextField addRollno = new TextField();
//        addRollno.setMaxWidth(RollnoCol.getPrefWidth());
//        addRollno.setPromptText("Last Name");
//       
//        final Button addButton = new Button("Add");
//        addButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//                data.add(new Student(
//                        addName.getText(),
//                        addRollno.getText()));
//                addName.clear();
//                addRollno.clear();
//             
//            }
//        });
// 
//        hb.getChildren().addAll(addName, addRollno,addButton);
//        hb.setSpacing(3);
// 
//        final VBox vbox = new VBox();
//        vbox.setSpacing(5);
//        vbox.setPadding(new Insets(10, 0, 0, 10));
//        vbox.getChildren().addAll(label, this, hb);
// 
//        ((Group) scene.getRoot()).getChildren().addAll(vbox);
// 
//        stage.setScene(scene);
//        stage.show();
//    }
 
    public static class Student {
 
        private final SimpleStringProperty Name;
        private final SimpleStringProperty Rollno;
 
        private Student(String fName, String lName) {
            this.Name = new SimpleStringProperty(fName);
            this.Rollno = new SimpleStringProperty(lName);
        }
 
        public String getName() {
            return Name.get();
        }
 
        public void setName(String fName) {
            Name.set(fName);
        }
 
        public String getRollno() {
            return Rollno.get();
        }
 
        public void setRollno(String fName) {
            Rollno.set(fName);
        }
    }
 
    class EditingCell extends TableCell<Student,String> {
 
        private TextField textField;
 
        public EditingCell() {
        }
 
        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }
 
        @Override
        public void cancelEdit() {
            super.cancelEdit();
 
            setText((String) getItem());
            setGraphic(null);
        }
 
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }
 
        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, 
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            commitEdit(textField.getText());
                        }
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

	
}
