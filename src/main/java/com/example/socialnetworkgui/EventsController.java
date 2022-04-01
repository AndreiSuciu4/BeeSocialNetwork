package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EventsController implements Initializable, Observer {
    public AnchorPane anchorPane;
    public TableView<EventModel> eventTable;
    public TableColumn<EventModel, String> id;
    public TableColumn<EventModel, String> name;
    public TableColumn<EventModel, String> description;
    public TableColumn<EventModel, String> data;
    public Controller service;
    public Button unsubscribeButton;
    public Button homeButton;
    public Button findEventsButton;
    public Button newEventButton;
    public Button attendButton;
    public int userId;
    public ImageView homeImage;
    public Label labelTable;
    public ImageView background;
    public ImageView event1;
    public ImageView event2;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        id.setVisible(false);
        Image image = new Image("file:images/homeButtonImage.jpg");
        homeImage.setImage(image);
        Image image1 = new Image("file:images/back.jpg");
        Image image2 = new Image("file:images/eventIcon.jpg");
        event1.setImage(image2);
        event2.setImage(image2);

        background.setImage(image1);
        attendButton.setDisable(true);
    }


    public void setService(Controller service, int id) {
        this.userId = id;
        this.service = service;
        service.addObserver(this);
        eventTable.setItems(loadTable());
    }

    private ObservableList<EventModel> loadTable() {
        List<Event> eventList = service.eventsForAUser(userId);
        List<EventModel> eventModels = new LinkedList<>();
        eventList.forEach(x -> {
            EventModel eventModel = new EventModel(x.getId().toString(), x.getName(), x.getDescription(), x.getDate().toString());
            eventModels.add(eventModel);
        });
        return FXCollections.observableArrayList(eventModels);
    }

    public ObservableList<EventModel> loadTableOtherEvents() {
        List<Event> eventList = service.eventsNotAttendedByUser(userId);
        List<EventModel> eventModels = new LinkedList<>();
        eventList.forEach(x -> {
            EventModel eventModel = new EventModel(x.getId().toString(), x.getName(), x.getDescription(), x.getDate().toString());
            eventModels.add(eventModel);
        });
        return FXCollections.observableArrayList(eventModels);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(!unsubscribeButton.isDisabled()) {
            eventTable.setItems(loadTable());
        }else{
            eventTable.setItems(loadTableOtherEvents());
        }
    }

    public void unsubscribeClicked() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ObservableList<EventModel> events = eventTable.getSelectionModel().getSelectedItems();
        if (events.isEmpty()) {
            alert.setTitle("Delete error");
            alert.setContentText("Please select a column from table and press the delete button");
            alert.show();
            return;
        }
        int eventId = Integer.parseInt(events.get(0).getId());
        service.removeSubscription(userId, eventId);
    }

    public void homeClicked() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
    }

    public void findEventsClicked() {
        if (!unsubscribeButton.isDisabled()) {
            findEventsButton.setText("    My events");
            unsubscribeButton.setDisable(true);
            attendButton.setDisable(false);
            eventTable.setItems(loadTableOtherEvents());
        } else {
            findEventsButton.setText("    Find events");
            unsubscribeButton.setDisable(false);
            attendButton.setDisable(true);
            eventTable.setItems(loadTable());

        }
    }

    public void attendClicked() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ObservableList<EventModel> events = eventTable.getSelectionModel().getSelectedItems();
        if (events.isEmpty()) {
            alert.setTitle("Select error");
            alert.setContentText("Please select a column from table and press the attendance button");
            alert.show();
            return;
        }
        int eventId = Integer.parseInt(events.get(0).getId());
        service.addAttendance(userId, eventId);
    }

    public void newEventClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("newEvent.fxml"));
        AnchorPane root = loader.load();
        NewEventController eventsController = loader.getController();
        eventsController.setService(service);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = new Stage();
        stage.setTitle("Add New Event");
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setScene(scene);
        stage.show();
    }
}
