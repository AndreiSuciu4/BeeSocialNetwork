package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.FriendRequest;
import com.example.domain.Status;
import com.example.domain.User;
import com.example.domain.UsersRequestsDTO;
import com.example.exception.EntityException;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.repository.database.DataBaseMessageRepository;
import com.example.repository.database.DataBaseRequestsRepository;
import com.example.repository.database.DataBaseUserRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.build.Build.*;

public class FriendRequestsController implements Initializable, Observer {

    public TableView<RequestModel> requestsTable;
    public TableColumn<RequestModel, String> firstName;
    public TableColumn<RequestModel, String> lastName;
    public TableColumn<RequestModel, String> id;
    public TableColumn data;
    public TableColumn status;
    public ImageView backImage;
    public Button homeButton;
    public ImageView homeImage;
    private Controller service;
    private int userId;
    private int pageNumberRec = 0;
    private int offsetRec = 0;
    private int pageNumberSend = 0;
    private int offsetSend = 0;
    private int pageSize = 3;
    List<RequestModel> requests = new ArrayList<>();

    @FXML
    public Button closeButton, acceptButton, declineButton, acceptAllButton, myFriendRequests;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstName.setCellValueFactory(new PropertyValueFactory<>("FirstName"));
        lastName.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        id.setVisible(false);
        Image image1 = new Image("file:images/back.jpg");
        backImage.setImage(image1);
        Image image = new Image("file:images/homeButtonImage.jpg");
        homeImage.setImage(image);

        requestsTable.setPlaceholder(new Label("No friend requests yet"));


        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) requestsTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 1.0) {
                    if (myFriendRequests.isDisabled()) {
                        requestsTable.setItems(loadSentRequests());
                    } else {
                        requestsTable.setItems(loadTable());
                    }
                }
            });
        });
    }

    public void setService(Controller service, int id) {
        this.service = service;
        this.userId = id;
        service.addObserver(this);
        ObservableList<RequestModel> requestModels1 = loadTable();
        requestsTable.setItems(requestModels1);
        requestsTable.setPlaceholder(new Label("No friend requests yet"));
    }


    private ObservableList<RequestModel> loadTable() {
        service.getFriendRequestsPag(requests, userId, pageSize, offsetRec);
        offsetRec = pageSize * pageNumberRec + pageSize;
        pageNumberRec++;
        return FXCollections.observableArrayList(requests);
    }

    public void acceptRequest(ActionEvent actionEvent) throws ValidatorException, EntityException, RepositoryException {
        ObservableList<RequestModel> requestModels = requestsTable.getSelectionModel().getSelectedItems();
        if (requestModels.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Select a request!");
            alert.setTitle("Warning");
            alert.show();
        } else {
            int from = Integer.parseInt(requestModels.get(0).getId());
            service.respondFriendRequest(from, userId, "APPROVE");
            requests.clear();
            offsetRec=0;
            pageNumberRec=0;
        }
    }

    public void declineRequest(ActionEvent actionEvent) throws ValidatorException, EntityException, RepositoryException {
        ObservableList<RequestModel> requestModels = requestsTable.getSelectionModel().getSelectedItems();
        if (requestModels.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Select a request!");
            alert.setTitle("Warning");
            alert.show();
        } else {
            int from = Integer.parseInt(requestModels.get(0).getId());
            service.respondFriendRequest(from, userId, "DECLINE");
            requests.clear();
            offsetRec=0;
            pageNumberRec=0;
        }
    }

    public void returnB(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void acceptAll(ActionEvent actionEvent) {
        service.respondToAllRequests(userId, "APPROVE");
        requests.clear();
        offsetRec=0;
        pageNumberRec=0;
    }

    public ObservableList<RequestModel> loadSentRequests() {
        service.sentFriendRequestsPag(requests, userId, pageSize, offsetSend);
        offsetSend = pageSize * pageNumberSend + pageSize;
        pageNumberSend++;
        return FXCollections.observableArrayList(requests);
    }


    public void myRequests(ActionEvent actionEvent) {
        requests.clear();
        pageNumberSend = 0;
        offsetSend = 0;

        ObservableList<RequestModel> requestModels1 = loadSentRequests();
        requestsTable.setItems(loadTable());
        acceptButton.setDisable(true);
        acceptAllButton.setDisable(true);
        myFriendRequests.setDisable(true);
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                ObservableList<RequestModel> requestModels = requestsTable.getSelectionModel().getSelectedItems();
                if (requestModels.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Select a request!");
                    alert.setTitle("Warning");
                    alert.show();
                } else {
                    int to = Integer.parseInt(requestModels.get(0).getId());
                    try {
                        requestsTable.setVisible(true);
                        service.deleteFriendRequest(userId, to);
                        requests.clear();
                        offsetSend=0;
                        pageNumberSend=0;
                        requestsTable.setItems(loadSentRequests());
                    } catch (RepositoryException repositoryException) {
                        repositoryException.printStackTrace();
                    }

                }
            }
        };
        declineButton.setOnAction(event);
        EventHandler<ActionEvent> exit = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (myFriendRequests.isDisabled()) {
                    requests.clear();
                    pageNumberRec = 0;
                    offsetRec = 0;
                    ObservableList<RequestModel> requestModels1 = loadTable();
                    requestsTable.setItems(loadTable());
                    acceptButton.setDisable(false);
                    acceptAllButton.setDisable(false);
                    myFriendRequests.setDisable(false);
                } else {
                    Stage stage = (Stage) closeButton.getScene().getWindow();
                    stage.close();
                }

            }
        };
        closeButton.setOnAction(exit);
    }

    @Override
    public void update(Observable o, Object arg) {
        requests.clear();
        offsetSend=0;
        pageNumberSend=0;
        ObservableList<RequestModel> requestModels1 = loadTable();
        requestsTable.setItems(loadTable());
    }

    public void homeClicked() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
    }
}
