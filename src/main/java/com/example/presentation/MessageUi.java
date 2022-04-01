package com.example.presentation;

import com.example.business.Controller;
import com.example.domain.Message;
import com.example.domain.User;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MessageUi {
    private Controller service;
    private int id;

    /**
     * Constructor
     * @param cont Controler
     * @param id id of te User
     * @throws Exception if there is  o user with this id
     */
    public MessageUi(Controller cont, int id) throws Exception {
        this.service = cont;
        boolean ok = false;
        for(User user : service.allUsers())
            if(user.getId() == id)
                ok = true;
        if(! ok)
            throw new Exception("user invalid");
        this.id = id;
    }

    /**
     * Ui for addMessage
     * @param input Scanner
     */
    private void addMessage(Scanner input){
        String to;
        System.out.println("Enter the id of the recipient!");
        to = input.nextLine();
        String[] toString = to.split(" ");
        List<Integer> list = new ArrayList<Integer>();
        Arrays.stream(toString).forEach(x->list.add(Integer.parseInt(x)));
        System.out.println("Enter the message!");
        String message = input.nextLine();
        try {
            service.addNewMessage(this.id, list, message);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        } catch (ValidatorException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * UI for removeMessage
     * @param  input Scanner
     */

    private void removeMessage(Scanner input){
        System.out.println("Enter the id of the message!");
        Integer idM = Integer.parseInt(input.nextLine());
        try {
            service.removeMessage(idM);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Ui for printReceivedMessages
     */
    private void printReceivedMessages(){
        try {
            List<Message> list = service.findMessages(this.id);
            list.forEach(System.out::println);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * UI for updateMessages
     * @param input Scanner
     */
    private void updateMessages(Scanner input){
        System.out.println("Enter the id of the message!");
        Integer idM = Integer.parseInt(input.nextLine());
        String to;
        System.out.println("Enter the id of the recipient!");
        to = input.nextLine();
        String[] toString = to.split(" ");
        List<Integer> list = new ArrayList<Integer>();
        Arrays.stream(toString).forEach(x->list.add(Integer.parseInt(x)));
        System.out.println("Enter the message!");
        String message = input.nextLine();
        System.out.println("Enther the id of the message!");
        String messId = input.nextLine();
        Integer i = null;
        try{
            i = Integer.parseInt(messId);
        } catch (NumberFormatException e) {
            i = null;
        }
        try {
            service.updateMessage(idM, this.id, list, message, i);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        } catch (ValidatorException e) {
            e.printStackTrace();
        }
    }

    /**
     * UI for replyMessage
     * @param input Scanner
     */
    private void replyMessage(Scanner input){
        String to;
        System.out.println("Enter the id of recipient!");
        to = input.nextLine();
        String[] toString = to.split(" ");
        List<Integer> list = new ArrayList<Integer>();
        Arrays.stream(toString).forEach(x->list.add(Integer.parseInt(x)));
        System.out.println("Enter the message!");
        String message = input.nextLine();
        int mess;
        System.out.println("Enter the id of message!");
        mess = Integer.parseInt(input.nextLine());
        try {
            service.replyMessage(this.id, list, message, mess);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        } catch (ValidatorException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * UI for printSentMessage
     */
    private void printSentMessage(){
        List<Message> messages = service.allMessageByUser(id);
        messages.forEach(System.out::println);
    }

    /**
     * Prints the menu
     */
    private void menu() {
        System.out.println("1.Add new message");
        System.out.println("2.Reply message");
        System.out.println("3.Update message");
        System.out.println("4.Remove message");
        System.out.println("5.Print sent messages");
        System.out.println("6.Print received messages");
        System.out.println("7.Print conversation");
        System.out.println("0.Logout");
    }

    public void run() throws ValidatorException, RepositoryException {
        Scanner input = new Scanner(System.in);
        int cmd;
        do {
            menu();
            cmd = Integer.parseInt(input.nextLine());
            try {
                switch (cmd) {
                    case 1->addMessage(input);
                    case 2->replyMessage(input);
                    case 3->updateMessages(input);
                    case 4->removeMessage(input);
                    case 5->printSentMessage();
                    case 6->printReceivedMessages();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (cmd != 0);
    }
}
