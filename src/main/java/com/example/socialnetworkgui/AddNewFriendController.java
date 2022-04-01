package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.User;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;


public class AddNewFriendController implements Initializable, Observer {
    public Button find;
    public TableView<UserModel> userTable;
    public TableColumn<UserModel, String> username;
    public TableColumn<UserModel, String> id;
    private static Controller service;
    public TextField searchField;
    public ImageView searchImage;
    public ImageView homeImage;
    public Label invalidUser;
    public TableColumn<UserModel, String> firstname;
    public TableColumn<UserModel, String> lastname;
    public Button homeButton;
    public ImageView background;
    public AnchorPane anchorPane;
    private int userId;
    private List<Button> buttons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PrincipalSceneController.setCell(id, username, firstname, lastname);
        userTable.setVisible(false);
        Image image = new Image("file:images/searchButton.png");
        searchImage.setImage(image);
        Image image3 = new Image("file:images/homeButtonImage.jpg");
        homeImage.setImage(image3);
        Image image4 = new Image("file:images/back.jpg");
        background.setImage(image4);
        invalidUser.setVisible(false);
        buttons = new ArrayList<>();
        userTable.setPlaceholder(new Label(""));

    }

    public void setService(Controller service, int id) {
        AddNewFriendController.service = service;
        this.userId = id;
        service.addObserver(this);
    }

    private ObservableList<UserModel> loadTable(String name) {
        for (Button button : buttons) {
            anchorPane.getChildren().remove(button);
        }
        buttons = new ArrayList<>();
        List<UserModel> friends = new ArrayList<>();
        List<User> users = service.getNoFriend(this.userId, name);
        int pozx = 689;
        int pozy = 50;
        for (User user : users) {
            String id1 = user.getId().toString();
            String userName = user.getUsername();
            String firstname = user.getFirstName();
            String lastname = user.getLastName();
            UserModel userModel = new UserModel(id1, userName, firstname, lastname);
            friends.add(userModel);
            Button button = new Button();
            button.setLayoutX(pozx);
            button.setLayoutY(pozy);
            button.setPrefHeight(15);
            button.setPrefWidth(15);
            if (service.existsFriendRequest(userId, Integer.parseInt(id1))) {
                ImageView iv1 = new ImageView("file:images/deleteFriend.jpg");
                iv1.setFitHeight(19);
                iv1.setFitWidth(19);
                button.setGraphic(iv1);
                button.setStyle("-fx-background-radius: 10; -fx-background-color:  white");
                button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                            if (mouseEvent.getClickCount() == 1) {
                                try {
                                    service.deleteFriendRequest(userId, Integer.parseInt(id1));
                                    ;
                                } catch (RepositoryException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            } else {
                ImageView iv1 = new ImageView("file:images/addNewFriendImage.jpg");
                iv1.setFitHeight(19);
                iv1.setFitWidth(19);
                button.setGraphic(iv1);
                button.setStyle("-fx-background-radius: 10; -fx-background-color:  white");
                button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                            if (mouseEvent.getClickCount() == 1) {
                                try {
                                    service.addFriendRequest(userId, Integer.parseInt(id1));
                                } catch (ValidatorException | RepositoryException ignored) {
                                }
                            }
                        }
                    }
                });
            }
            pozy += 21;
            buttons.add(button);
            anchorPane.getChildren().add(button);

        }
        return FXCollections.observableArrayList(friends);
    }
    public void addClicked() {
        ObservableList<UserModel> users = userTable.getSelectionModel().getSelectedItems();
        if (users.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add new friend error");
            alert.setContentText("Please select a column from table and press the Add new friend button");
            alert.show();
            return;
        }
        String str = users.get(0).getId();
        int id1 = Integer.parseInt(str);
        try {
            service.addFriendRequest(this.userId, id1);
            searchClicked();
        } catch (ValidatorException | RepositoryException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.setHeaderText("Try again");
            alert.show();
        }
    }

    public void homeClicked() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
    }

    public void searchClicked() {
        if (searchField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Search error");
            alert.setContentText("Please give a name");
            alert.show();
        }
        String userName = searchField.getText();
        ObservableList<UserModel> userModels = loadTable(userName);
        if (userModels.size() == 0)
            invalidUser.setVisible(true);
        else {
            userTable.setItems(userModels);
            userTable.setVisible(true);
            invalidUser.setVisible(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        searchClicked();
    }
}