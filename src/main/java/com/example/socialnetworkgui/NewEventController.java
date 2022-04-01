package com.example.socialnetworkgui;

import com.example.business.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class NewEventController implements Initializable {
    public TextField nameField;
    public TextArea descriptionField;
    public ComboBox<Integer> day;
    public ComboBox<Integer> month;
    public ComboBox<Integer> year;
    public Controller service;
    public Button addButton;
    public ImageView background;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalDateTime date = LocalDateTime.now();
        ArrayList<Integer> years = new ArrayList<>();
        for (int i = date.getYear(); i < date.getYear() + 10; i++) {
            years.add(i);
        }
        ObservableList<Integer> years1 = FXCollections.observableArrayList(years);
        year.setItems(years1);

        ArrayList<Integer> months = new ArrayList<>();
        for (int i = date.getMonthValue(); i <= 12; i++)
            months.add(i);
        ObservableList<Integer> months1 = FXCollections.observableArrayList(months);
        month.setItems(months1);

        ArrayList<Integer> days = new ArrayList<>();
        for (int i = date.getDayOfMonth(); i <= 31; i++)
            days.add(i);
        ObservableList<Integer> days1 = FXCollections.observableArrayList(days);
        day.setItems(days1);
        Image image1 = new Image("file:images/back.jpg");
        background.setImage(image1);
    }

    public void setService(Controller service) {
        this.service = service;
    }

    public void addClicked() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String name = nameField.getText();
        String desription = descriptionField.getText();
        if(name == null || desription == null){
            alert.setHeaderText("Incorrect data");
            alert.show();
            return;
        }
        if(day.getValue() == null || month.getValue() == null || year.getValue() == null){
            alert.setHeaderText("Incorrect data");
            alert.show();
            return;
        }
        int d = Integer.parseInt(day.getValue().toString());
        int m = Integer.parseInt(month.getValue().toString());
        int y = Integer.parseInt(year.getValue().toString());
        LocalDate date;
        try {
            date = LocalDate.of(y, m, d);
            service.addNewEvent(name, desription, date);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            alert.setTitle("Incorrect data");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }


}
