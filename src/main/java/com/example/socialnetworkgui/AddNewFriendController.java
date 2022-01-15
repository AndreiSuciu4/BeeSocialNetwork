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
    public Button addButton;
    public ImageView searchImage;
    public ImageView addImage;
    public ImageView homeImage;
    public Label invalidUser;
    public TableColumn<UserModel, String> firstname;
    public TableColumn<UserModel, String> lastname;
    public Button homeButton;
    public ImageView background;
    public AnchorPane anchorPane;
    private int userId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PrincipalSceneController.setCell(id, username, firstname, lastname);
        userTable.setVisible(false);
        addButton.setVisible(false);
        Image image = new Image("file:images/searchButton.png");
        searchImage.setImage(image);
        Image image1 = new Image("file:images/addNewFriendImage.jpg");
        addImage.setImage(image1);
        Image image3 = new Image("file:images/homeButtonImage.jpg");
        homeImage.setImage(image3);
        Image image4 = new Image("file:images/back.jpg");
        background.setImage(image4);
        invalidUser.setVisible(false);
        addImage.setVisible(false);

    }

    public void setService(Controller service, int id) {
        AddNewFriendController.service = service;
        this.userId = id;
        service.addObserver(this);
    }

    private ObservableList<UserModel> loadTable(String name) {
        List<UserModel> friends = new ArrayList<>();
        List<User> users = service.getNoFriend(this.userId, name);
<<<<<<< HEAD
        int pozx =689;
        int pozy = 56;
        for(User user:users){
                    String id1 = user.getId().toString();
                    String userName = user.getUsername();
                    String firstname = user.getFirstName();
                    String lastname = user.getLastName();
=======
        users.stream().
                forEach(x -> {
                    String id1 = x.getId().toString();
                    String userName = x.getUsername();
                    String firstname = x.getFirstName();
                    String lastname = x.getLastName();
>>>>>>> 50163296a5c24b78bd3d8f54d0fdb9e4e77e7e3d
                    UserModel userModel = new UserModel(id1, userName, firstname, lastname);
                    friends.add(userModel);
                    Button button = new Button();
                    button.setLayoutX(pozx);
                    button.setLayoutY(pozy);
                    if(service.existsFriendRequest(userId, Integer.parseInt(id1)))
                    {
                        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                    if (mouseEvent.getClickCount() == 1) {
                                        try {
                                            service.deleteFriendRequest(userId, Integer.parseInt(id1));
                                        } catch (RepositoryException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }
                    else{
                        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                    if (mouseEvent.getClickCount() == 1) {
                                        try {
                                            service.addFriendRequest(userId, Integer.parseInt(id1));
                                        } catch (RepositoryException | ValidatorException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }
                    pozy += 26;
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
            ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "This friendship already exists, do you want to delete it?" ,
                    yes,
                    no);


            Optional<ButtonType> result = alert.showAndWait();

            alert.setHeaderText("Warning");
            if (result.orElse(no) == yes) {
                try {
                    service.deleteFriendRequest(userId, id1);
                } catch (RepositoryException ignored) {
                }
            }
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
            addButton.setVisible(true);
            addImage.setVisible(true);
            invalidUser.setVisible(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        searchClicked();
    }
}
