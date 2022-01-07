package com.example.socialnetworkgui;

import com.example.business.Controller;
import com.example.domain.Friendship;
import com.example.domain.Message;
import com.example.domain.MessageDTO;
import com.example.domain.User;
import com.example.exception.RepositoryException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;


public class RapoarteController implements Initializable {
    public ComboBox firstDay;
    public ComboBox firstMonth;
    public ComboBox firstYear;
    public TextField path;
    public TextField filename;
    public ComboBox lastDay;
    public ComboBox lastMonth;
    public ComboBox lastYear;
    public Controller service;
    public int userId;
    List<Friendship> friendships;
    List<MessageDTO> messages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        friendships = new ArrayList<>();
        messages = new ArrayList<>();

        LocalDateTime date = LocalDateTime.now();
        int year = date.getYear();
        ArrayList<Integer> years = new ArrayList<>();
        for(int i = 2021; i <= year; i++)
            years.add(i);
        ObservableList<Integer> years1 = FXCollections.observableArrayList(years);
        firstYear.setItems(years1);
        lastYear.setItems(years1);

        ArrayList<Integer> months = new ArrayList<>();
        for(int i = 1; i <= 12; i++)
            months.add(i);
        ObservableList<Integer> months1 = FXCollections.observableArrayList(months);
        firstMonth.setItems(months1);
        lastMonth.setItems(months1);

        ArrayList<Integer> days = new ArrayList<>();
        for(int i = 1; i <= 31; i++)
            days.add(i);
        ObservableList<Integer> days1 = FXCollections.observableArrayList(days);
        firstDay.setItems(days1);
        lastDay.setItems(days1);
    }

    public void setService(Controller service, int id){
        this.service = service;
        this.userId = id;
    }
    public void raport1Clicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        int day1 = Integer.parseInt(firstDay.getValue().toString());
        int month1 = Integer.parseInt(firstMonth.getValue().toString());
        int year1 = Integer.parseInt(firstYear.getValue().toString());
        int day2 = Integer.parseInt(lastDay.getValue().toString());
        int month2 = Integer.parseInt(lastMonth.getValue().toString());
        int year2 = Integer.parseInt(lastYear.getValue().toString());
        try {
            this.friendships = service.friendshipsBetween2Dates(userId,day1,month1, year1, day2, month2, year2);
            this.messages = service.messagesBetween2Dates(userId,day1,month1, year1, day2, month2, year2);
            showRaport1();
        } catch (Exception e) {
            alert.setTitle("Message Here...");
            alert.setHeaderText("Incorrect date");
            alert.setContentText(e.getMessage());
            alert.setTitle("Warning");
            alert.show();
            e.printStackTrace();
        }
    }

    private void showRaport1() throws IOException, RepositoryException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("raport1.fxml"));
        AnchorPane root = loader.load();
        Raport1Controller raport1Controller = loader.getController();
        raport1Controller.setLists(service, messages, friendships, userId);
        Scene scene = new Scene(root, 750, 400);
        Stage stage = new Stage();
        stage.setTitle("Raport1");
        stage.setScene(scene);
        stage.show();
    }

    public void raport2Clicked(ActionEvent actionEvent) {
    }

    public void generatePDF1Clicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        try (PDDocument doc = new PDDocument()) {

            PDPage myPage = new PDPage();
            doc.addPage(myPage);

            try (PDPageContentStream cont = new PDPageContentStream(doc, myPage)) {

                cont.beginText();

                cont.setFont(PDType1Font.TIMES_ROMAN, 12);
                cont.setLeading(14.5f);

                cont.newLineAtOffset(25, 700);
                String line1 = "First report:";
                cont.showText(line1);

                cont.newLine();

                String line2 = "New friends:";
                cont.showText(line2);
                cont.newLine();

                System.out.println(friendships);
                for(Friendship friendship : friendships){
                    if(friendship.getUserA() != userId){
                        User user1 = service.findUser(friendship.getUserA());
                        String lineFr = user1.getFirstName() + " " + user1.getLastName();
                        cont.showText(lineFr);
                        cont.newLine();
                    }
                    else{
                        User user1 = service.findUser(friendship.getUserB());
                        String lineFr = user1.getFirstName() + " " + user1.getLastName();
                        cont.showText(lineFr);
                        cont.newLine();
                    }
                }
                cont.newLine();

                String line3 = "All messages received:";
                cont.showText(line3);
                cont.newLine();

                System.out.println(messages);
                for(MessageDTO message : messages) {
                    User user = service.findUser(message.getFrom());
                    String lineMess = user.getFirstName() + " " + user.getLastName()  + ":" + message.getMessage();
                    cont.showText(lineMess);
                    cont.newLine();
                }
                cont.endText();
            }
            String filename = this.filename.getText();
            String path = this.path.getText();
            doc.save(path + "\\" + filename + ".pdf");
        } catch (IOException | RepositoryException e) {
            alert.setTitle("Message Here...");
            alert.setHeaderText("Incorrect date");
            alert.setContentText(e.getMessage());
            alert.setTitle("Warning");
            alert.show();
            e.printStackTrace();        }

    }

    public void generatePDF2Clicked(ActionEvent actionEvent) {
    }
}
