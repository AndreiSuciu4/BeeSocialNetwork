package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.utils.Encryption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {
    public TextField usernameField;
    public ImageView logoImage;
    public ImageView leftImage;
    private Controller service;
    public ImageView beeImage;

    public void initialize(){
        Image image = new Image("file:images/beeLogInImage3.jpg");
        beeImage.setImage(image);
        Image image1 = new Image("file:images/beeAppLogo.png");
        logoImage.setImage(image1);
        Image image2 = new Image("file:images/2colors.jpg");
        leftImage.setImage(image2);
    }

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    public void setService(Controller service){
        this.service = service;
    }

    public void loginButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("login.fxml"));
        AnchorPane root = loader.load();
        LoginController loginController = loader.getController();
        loginController.setService(service);
        Scene scene = new Scene(root, 800, 400);
        Stage stage;
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Log in");
        stage.setScene(scene);
        stage.show();
    }

    public void newAccountClicked(ActionEvent actionEvent) {
        Encryption encryption = new Encryption();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String pass = encryption.encrypt(password);
        try {
            service.add(username,firstName, lastName,pass);
            loginButtonClicked(actionEvent);
        } catch (RepositoryException | ValidatorException | IOException e) {
            alert.setTitle("Incorrect data");
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }
}
