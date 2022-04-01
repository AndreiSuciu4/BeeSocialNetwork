package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.Event;
import com.example.domain.Page;
import com.example.domain.User;
import com.example.exception.EntityException;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.repository.database.DataBaseMessageRepository;
import com.example.repository.database.DataBaseUserRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class PrincipalSceneController implements Initializable, Observer {

    public TableView<UserModel> friendshipTable;
    public TableColumn<UserModel, String> id;
    public TableColumn<UserModel, String> username;
    public ImageView addFriendImage;
    public ImageView deleteImage;
    public Button deleteButton;
    public Button logOutButton;
    public ImageView logOutImage;
    public Label userAccount;
    public AnchorPane anchorPane;
    public Button messageButton;
    public Button eventsButton;
    public ImageView messageImage;
    public TableColumn<UserModel, String> firstname;
    public TableColumn<UserModel, String> lastname;
    public ImageView background;
    public ImageView friendRequestImage;
    public ImageView raportImage;
    public ImageView userImage;
    public Circle circle;
    public ImageView eventIcon;
    private Controller service;
    private int userId;
    private int pageNumber = 0;
    private int offset = 0;
    private int pageSize = 10;
    List<UserModel> friends = new ArrayList<>();
    public Label nextEvent;
    public Label noOfDays;
    private Page page;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setCell(id, username, firstname, lastname);
        id.setVisible(false);

        Image image1 = new Image("file:images/back.jpg");
        background.setImage(image1);
        Image image3 = new Image("file:images/addNewFriendImage.jpg");
        addFriendImage.setImage(image3);
        Image image4 = new Image("file:images/deleteButton.png");
        deleteImage.setImage(image4);
        Image image5 = new Image("file:images/logoutButton.png");
        logOutImage.setImage(image5);
        Image image6 = new Image("file:images/messageButton.png");
        messageImage.setImage(image6);
        Image image2 = new Image("file:images/friendRequest.png");
        friendRequestImage.setImage(image2);
        Image image7 = new Image("file:images/reports.jpg");
        raportImage.setImage(image7);
        Image image8 = new Image("file:images/userIcon.png");
        userImage.setImage(image8);
        Image image9 = new Image("file:images/eventIcon.jpg");
        eventIcon.setImage(image9);

        friendshipTable.setPlaceholder(new Label("No friends yet"));

        Platform.runLater(() -> {
            ScrollBar tvScrollBar = (ScrollBar) friendshipTable.lookup(".scroll-bar:vertical");
            tvScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((Double) newValue == 1.0) {
                    try {
                        friendshipTable.setItems(loadTable());
                    } catch (ValidatorException | RepositoryException e) {
                        e.printStackTrace();
                    }
                }
            });

        });

    }

    static void setCell(TableColumn<UserModel, String> id, TableColumn<UserModel, String> username, TableColumn<UserModel, String> firstname, TableColumn<UserModel, String> lastname) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        lastname.setCellValueFactory(new PropertyValueFactory<>("lastname"));
    }

    public void setService(Controller service, int id, Page page) {
        this.userId = id;
        this.service = service;
        service.addObserver(this);
        try {
            User user = service.findUser(userId);
            String user1 = "Welcome " + user.getUsername();
            userAccount.setText(user1);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        try {
            friendshipTable.setItems(loadTable());
            Event event = service.nextEventForUser(userId);
            if(event != null) {
                Notifications.create()
                        .darkStyle()
                        .title("Next event")
                        .text(event.getName() + " in " + String.valueOf(service.daysUntilNextEvent(userId)) + " days")
                        .graphic(new Rectangle(20, 20, Color.RED)) // sets node to display
                        .show();
            }

        } catch (ValidatorException | RepositoryException e) {
            e.printStackTrace();
        }
        this.page = page;
    }


    private ObservableList<UserModel> loadTable() throws ValidatorException, RepositoryException {
        service.getFriendsForAUserPag(friends, userId, pageSize, offset);
        offset = pageSize * pageNumber + pageSize;
        pageNumber++;
        return FXCollections.observableArrayList(friends);
    }

    public void addFriendClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("addNewFriend.fxml"));
        AnchorPane root = loader.load();
        AddNewFriendController addNewFriendController = loader.getController();
        addNewFriendController.setService(service, userId);
        Scene scene = new Scene(root, 800, 400);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setTitle("Add new friend");
        stage.setScene(scene);
        stage.show();
    }

    public void deleteClicked() throws EntityException, RepositoryException, ValidatorException {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ObservableList<UserModel> users = friendshipTable.getSelectionModel().getSelectedItems();
        if (users.isEmpty() || users == null) {
            alert.setTitle("Delete error");
            alert.setContentText("Please select a column from table and press the delete button");
            alert.show();
            return;
        }
        if(users.get(0).getId() == null)
            return;
        int id = Integer.parseInt(users.get(0).getId());
        service.removeFriends(this.userId, id);
        page.setListOfFriends(service.getFriends(userId));
    }

    public void logOutClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("login.fxml"));
        AnchorPane root = loader.load();
        LoginController loginController = loader.getController();
        loginController.setService(service);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Log in");
        stage.setScene(scene);
        stage.show();
    }

    public void friendRequestsClicked() throws IOException {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("friendRequests.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load(), 800, 400);
            FriendRequestsController friendRequestsController = fxmlLoader.getController();
            friendRequestsController.setService(service, userId);
            stage.setTitle("Friend requests");
            stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
            stage.setScene(scene);
            stage.show();
            friendshipTable.setItems(loadTable());
        } catch (IOException | ValidatorException | RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void messagesClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("messages.fxml"));
        AnchorPane root = loader.load();
        MessageController messageController = loader.getController();
        messageController.setService(service, userId);
        Scene scene = new Scene(root, 800, 400);
        Stage stage = new Stage();
        stage.setTitle("Messages");
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            friends.clear();
            offset = 0;
            pageNumber = 0;
            friendshipTable.setItems(loadTable());
            Event event = service.nextEventForUser(userId);
        } catch (ValidatorException | RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void rapoarteClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("rapoarte.fxml"));
        AnchorPane root = loader.load();
        RapoarteController messageController = loader.getController();
        messageController.setService(service, userId);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = new Stage();
        stage.setTitle("Reports");
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setScene(scene);
        stage.show();
    }

    public void eventsClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("myEvents.fxml"));
        AnchorPane root = loader.load();
        EventsController eventsController = loader.getController();
        eventsController.setService(service, userId);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = new Stage();
        stage.setTitle("Events");
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setScene(scene);
        stage.show();

    }
}
