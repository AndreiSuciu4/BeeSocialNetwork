package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.Friendship;
import com.example.domain.MessageDTO;
import com.example.domain.User;
import com.example.exception.RepositoryException;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Raport2Controller implements Initializable {
    public ScrollPane scrollPane;
    public ImageView background;
    private List<MessageDTO> messageList;
    private Controller service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image1 = new Image("file:images/back.jpg");
        background.setImage(image1);
    }

    public void setLists(Controller service, List<MessageDTO> messageList, int user, String from, String start, String end) throws RepositoryException {
        this.service = service;
        this.messageList = messageList;

        AnchorPane anchorPaneRaport = new AnchorPane();
        scrollPane.setContent(anchorPaneRaport);
        int x = 14;
        int y = 40;
        Label label = new Label();
        Label label1 = new Label();
        label1.setText("All messages received from " + from + " between " + start + " and " + end + ":");
        label1.setStyle("-fx-background-radius: 5");
        label1.setTextAlignment(TextAlignment.JUSTIFY);
        label1.setMaxWidth(600);
        label1.setFont(new Font("Arial", 18));
        label1.setWrapText(true);
        label1.setLayoutX(x);
        label1.setLayoutY(y);
        y += 60;
        anchorPaneRaport.getChildren().add(label1);
        for (MessageDTO message : messageList) {
            Label labelMess = new Label();
            User user1 = service.findUser(message.getFrom());
            String lineMess = user1.getFirstName() + " " + user1.getLastName() + ":" + message.getMessage();
            labelMess.setText(lineMess);
            labelMess.setStyle("-fx-background-radius: 5");
            labelMess.setTextAlignment(TextAlignment.JUSTIFY);
            labelMess.setMaxWidth(300);
            labelMess.setFont(new Font("Arial", 18));
            labelMess.setWrapText(true);
            labelMess.setLayoutX(x);
            labelMess.setLayoutY(y);
            y += 20;
            anchorPaneRaport.getChildren().add(labelMess);
        }
    }
}
