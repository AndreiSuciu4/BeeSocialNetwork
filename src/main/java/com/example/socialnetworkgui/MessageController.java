package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.MessageDTO;
import com.example.domain.UsersFriendsDTO;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class MessageController implements Initializable, Observer {
    public ScrollPane scrollPaneMessage;
    public TextField messageField;
    public Button deleteButton;
    public Button replyButton;
    public ImageView homeImage;
    public Button homeButton;
    public AnchorPane anchorPaneFriends;
    public AnchorPane anchorPaneMessage;
    public ImageView leftImage;
    private Controller service;
    private int userId;
    private int toId;
    private List<Button> buttons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttons = new ArrayList<>();
        Image image = new Image("file:images/homeButtonImage.jpg");
        homeImage.setImage(image);
        Image image2 = new Image("file:images/2colors.jpg");
        leftImage.setImage(image2);
    }
    public void setService(Controller service, int id){
        this.service = service;
        service.addObserver(this);
        this.userId = id;
        try {
            showFriend();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void showMessage() throws RepositoryException {
        AnchorPane anchorPane = new AnchorPane();
        scrollPaneMessage.setContent(anchorPane);
        List<MessageDTO> messageList = service.getConversation(toId, userId);
        if(messageList.size() == 0)
        {
            Label label = new Label();
            label.setText("no message found ");
            label.setStyle("-fx-background-radius: 5; -fx-background-color:  white");
            label.setLayoutX(197);
            label.setLayoutY(160);
            label.setTextAlignment(TextAlignment.JUSTIFY);
            anchorPane.getChildren().add(label);
        }
        int y = 21;
        List<Label> messageLabel = new ArrayList<>();
        for(MessageDTO message : messageList){
            Label labelMess = new Label();
            labelMess.setText(message.getMessage());
            labelMess.setStyle("-fx-background-radius: 5; -fx-background-color:  #fad907");
            labelMess.setTextAlignment(TextAlignment.JUSTIFY);
            labelMess.setMaxWidth(146);
            labelMess.setFont(new Font("Arial", 18));
            labelMess.setWrapText(true);

            if(message.getReply() != 0)
            {
                Label replyLabel = new Label();
                MessageDTO reply = service.findMessageDTO(message.getReply());
                replyLabel.setText(reply.getMessage());
                replyLabel.setStyle("-fx-background-radius: 5; -fx-background-color:  #b9b1b1");
                replyLabel.setTextAlignment(TextAlignment.JUSTIFY);
                if(message.getFrom() == userId)
                    replyLabel.setLayoutX(290);
                else
                    replyLabel.setLayoutX(25);
                replyLabel.setLayoutY(y);
                anchorPane.getChildren().add(replyLabel);
                y += 20;
            }

            if(message.getFrom() == userId)
                labelMess.setLayoutX(290);
            else
                labelMess.setLayoutX(25);
            labelMess.setLayoutY(y);
            y += (40 + (message.getMessage().length() / 4) * 6);
            anchorPane.getChildren().add(labelMess);
            messageLabel.add(labelMess);
            labelMess.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 1) {
                            deleteClicked(message.getId());
                            replyClicked(message.getId());
                    }
                }
            });
        }
    }
    public void showFriend() throws RepositoryException {

        for (Button button: buttons){
            button.setVisible(false);
        }

        int y = 44;
        List<UsersFriendsDTO> friends = service.getFriends(userId);
        buttons = new ArrayList<>();
        for(UsersFriendsDTO friend : friends){
            Button friendButton = new Button();
            if(friend.getUsera().getId() != userId) {
                friendButton.setText(friend.getUsera().getFirstName() + " " + friend.getUsera().getLastName());
                friendButton.setOnAction(event -> {
                    try {
                        toId = friend.getUsera().getId();
                        showMessage();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                });
            }
            else
            {
                friendButton.setText(friend.getUserb().getFirstName() + " " + friend.getUserb().getLastName());
                friendButton.setOnAction(event -> {
                    try {
                        toId = friend.getUserb().getId();
                        showMessage();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                });
            }
            buttons.add(friendButton);

        }
        for (Button button: buttons){
            button.setLayoutX(25);
            button.setLayoutY(y);
            y += 40;
            anchorPaneFriends.getChildren().add(button);
        }
    }

    public void sendClicked(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String mess = messageField.getText();
        try {
            service.addNewMessage(userId, List.of(toId), mess);
        } catch (RepositoryException | ValidatorException e) {
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Empty message");
            alert.show();
        }
        messageField.deleteText(0, mess.length());
    }

    public void deleteClicked(int id){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        deleteButton.setOnAction(event -> {
            try {
                service.removeMessage(id);
            } catch (RepositoryException e) {
                alert.setHeaderText("Select a message, please!");
                alert.setContentText(e.getMessage());
                alert.setTitle("Warning");
                alert.show();
            }
        });
    }

    public void replyClicked(int id){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        replyButton.setOnAction(event -> {
            try {
                String mess = messageField.getText();
                service.replyMessage(userId, List.of(toId), mess, id);
                messageField.deleteText(0, mess.length());
            } catch (RepositoryException | ValidatorException e) {
                alert.setHeaderText("Error");
                alert.setContentText("Empty message");
                alert.show();
            }
        });
    }

    public void replyAllClicked(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String mess = messageField.getText();
        try {
            service.replyAll(userId, mess);
        } catch (ValidatorException | RepositoryException e) {
            alert.setHeaderText("Error");
            alert.setContentText("Empty message");
            alert.show();
        }
        messageField.deleteText(0, mess.length());
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            showFriend();
            showMessage();

        } catch (RepositoryException ignored) {
        }
    }

    public void homeClicked() {
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
    }
}
