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
import java.util.ResourceBundle;

import java.util.*;

public class Raport1Controller implements Initializable {
    public ScrollPane scrollPane;
    public ImageView background;
    private List<MessageDTO> messageList;
    private List<Friendship> friendships;
    private Controller service;
    private int user;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image1 = new Image("file:images/back.jpg");
        background.setImage(image1);
    }

    public void setLists(Controller service, List<MessageDTO> messageList, List<Friendship> friendships, int user) throws RepositoryException {
        this.service = service;
        this.messageList = messageList;
        this.friendships = friendships;
        this.user = user;
        showReport1();

    }
    private void showReport1() throws RepositoryException {
        AnchorPane anchorPaneRaport = new AnchorPane();
        scrollPane.setContent(anchorPaneRaport);
        int x = 14;
        int y = 23;
        Label label = new Label();
        String text = "New friends:";
        if(friendships.size() == 0)
            text = "New friends:-";
        label = setLabel(x, y, text);
        y += 20;
        anchorPaneRaport.getChildren().add(label);

        for(Friendship friendship : friendships){
            Label labelFr = new Label();
            User user1;
            if(friendship.getUserA() != user){
                user1 = service.findUser(friendship.getUserA());
            }
            else{
                user1 = service.findUser(friendship.getUserB());
            }
            String text_fr = user1.getUsername() + ":" + user1.getFirstName() + " " + user1.getLastName();
            labelFr = setLabel(x, y, text_fr);
            y += 20;
            anchorPaneRaport.getChildren().add(labelFr);
        }

        y +=20;
        Label label1 = new Label();
        text = "All messages received:";
        if(messageList.size() == 0)
            text = "All messages received:-";
        label1 = setLabel(x, y, text);
        y += 20;
        anchorPaneRaport.getChildren().add(label1);
        for(MessageDTO message : messageList){
            Label labelMess = new Label();
            User user1 = service.findUser(message.getFrom());
            String lineMess = user1.getFirstName() + " " + user1.getLastName()  + ":" + message.getMessage();
            labelMess = setLabel(x, y, lineMess);
            y += 20;
            anchorPaneRaport.getChildren().add(labelMess);
        }
    }

    private Label setLabel(int x, int y, String text){
        Label label = new Label();
        label.setText(text);
        label.setStyle("-fx-background-radius: 5");
        label.setTextAlignment(TextAlignment.JUSTIFY);
        label.setMaxWidth(600);
        label.setFont(new Font("Arial", 18));
        label.setWrapText(true);
        label.setLayoutX(x);
        label.setLayoutY(y);
        return  label;
    }

}

