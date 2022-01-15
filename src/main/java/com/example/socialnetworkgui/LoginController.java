package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.User;
import com.example.exception.RepositoryException;
import com.example.utils.Encryption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class LoginController {
    public ImageView logoImage;
    public ImageView leftImage;
    private Controller service;
    public ImageView beeImage;
    public int id;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    public void initialize() {
        Image image = new Image("file:images/beeLogInImage3.jpg");
        beeImage.setImage(image);
        Image image1 = new Image("file:images/beeAppLogo.png");
        logoImage.setImage(image1);
        Image image2 = new Image("file:images/2colors.jpg");
        leftImage.setImage(image2);
    }

    public void setService(Controller service) {
        this.service = service;
    }

    @FXML
    public void signInClicked(ActionEvent event) throws IOException {
        Encryption encryption = new Encryption();
        ArrayList<User> users = service.allUsers();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        boolean find = false;
        for (User user : users) {
            if (usernameField.getText().equals(user.getUsername()) && passwordField.getText().equals(encryption.decrypt(user.getPassword()))) {
                this.id = user.getId();
                find = true;
                break;
            }
        }


        try {
            String username = usernameField.getText();
            String password = encryption.encrypt(passwordField.getText());
            int id = service.getUserByUsernameAndPassword(username, password);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("principalScene.fxml"));
            AnchorPane root = loader.load();
            PrincipalSceneController principalSceneController = loader.getController();
            principalSceneController.setService(service, id);
            Scene scene = new Scene(root, 800, 400);
            Stage stage;
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
            stage.setTitle("Main scene");
            stage.setScene(scene);
            stage.show();

        } catch (RepositoryException e) {
            alert.setHeaderText("Incorrect username or password");
            alert.setTitle("Error");
            alert.show();
        }
    }

    @FXML
    public void signUpClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("signUp.fxml"));
        AnchorPane root = loader.load();
        SignUpController signUpController = loader.getController();
        signUpController.setService(service);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getIcons().add(new Image("file:images/beeLogInImage3.jpg"));
        stage.setTitle("Sign up");
        stage.setScene(scene);
        stage.show();

    }
}
