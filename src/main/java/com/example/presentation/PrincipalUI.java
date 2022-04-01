package com.example.presentation;

import com.example.business.Controller;
import com.example.domain.User;
import com.example.domain.UsersFriendsDTO;
import com.example.exception.EntityException;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;

import java.util.List;
import java.util.Scanner;

public class PrincipalUI {
    private Controller service;

    /**
     * Constructor for the UI class
     */
    public PrincipalUI(Controller cont) {
        this.service = cont;
    }

    /**
     * Reads a first and last name and calls the add function from the service with the given parameters
     *
     * @param input a Scanner for input
     * @throws ValidatorException  if the given strings are not valid
     * @throws RepositoryException if the object already exists in the repository
     */
    private void add(Scanner input) throws ValidatorException, RepositoryException {
        String firstName, lastName;
        System.out.println("Enter your desired firstName!");
        firstName = input.nextLine();
        System.out.println("Enter your desired lastName!");
        lastName = input.nextLine();
        System.out.println("Added the new user with the id: " + service.add(null, firstName, lastName,"fds").getId());
    }

    /**
     * Prints all users
     */
    private void allUsers() {
        service.allUsers().forEach((user) -> System.out.println(user.toString()));
    }


    /**
     * Prints the number of communities from the network
     */
    private void communitiesNumber() {
        int sol = service.communitiesNumber();
        System.out.println("The number of communities is " + sol);
    }

    /**
     * Prints the size of the largest community and, if the user wants, it prints the users from the community
     *
     * @param input a Scanner for input
     */
    private void largestCommunity(Scanner input) {
        List<User> largest = service.largestCommunity();
        System.out.println("Largest community has " + largest.size() + " members");
        System.out.println("Do you want to see the members?(Y/N)");
        String rasp = input.nextLine();
        if (rasp.equals("Y")) {
            largest.forEach((user) -> {
                System.out.print(user + "  ");
            });
            System.out.println();
        }
    }

    /**
     * Prints the network
     */
    private void printNetwork() {
        service.printNetwork();
    }

    private int login(Scanner input) {
        int id;
        System.out.println("Enter your user id!");
        id = Integer.parseInt(input.nextLine());
        User found = null;
        try {
            found = service.findUser(id);
        } catch (RepositoryException e) {
            System.out.println("Not a valid user id!");
            return -1;
        }
        System.out.println("Logged in as " + found.toString());
        return id;
    }

    /**
     * Reads an id and new first and last names and calls the updateuser function from the controller
     *
     * @param input a Scanner for input
     * @throws RepositoryException if the user with the given id does not exist
     * @throws ValidatorException  if the new fields are not valid
     */
    private void updateUser(int id, Scanner input) throws RepositoryException, ValidatorException {
        String firstName, lastName;
        System.out.println("Enter the new firstName!");
        firstName = input.nextLine();
        System.out.println("Enter the new lastName!");
        lastName = input.nextLine();
        service.updateUser(id, null, firstName, lastName, "iuhgf");
    }


    /**
     * Reads the ids of two users and creates a friendship between them
     *
     * @param input a Scanner for input
     * @throws EntityException if the Friendship already exists
     */
    private void addFriendship(int id, Scanner input) throws EntityException, RepositoryException, ValidatorException {
        int username2;
        System.out.println("Enter the id of the second person");
        username2 = Integer.parseInt(input.nextLine());
        service.addFriend(id, username2);
    }

    /**
     * Reads the ids of two users and deletes the frienship between them
     *
     * @param input a Scanner for input
     * @throws EntityException if the Friendship does not exist;
     */
    private void removeFriendship(int id, Scanner input) throws EntityException, RepositoryException {
        int username2;
        System.out.println("Enter the id of the second person");
        username2 = Integer.parseInt(input.nextLine());
        service.removeFriends(id, username2);
    }

    /**
     * @param input Scanner for input
     * @throws ValidatorException  if month is not valid
     * @throws RepositoryException if user does not exist
     */
    private void findFriendshipByUserAndMonth(int id, Scanner input) throws ValidatorException, RepositoryException {
        int month;
        System.out.println("Enter the month!");
        month = Integer.parseInt(input.nextLine());
        List<UsersFriendsDTO> usersFriendsDTOList = service.findFriendshipByUserAndMonth(id, month);
        if (usersFriendsDTOList.size() == 0)
            System.out.println("lista vida!\n");
        usersFriendsDTOList.forEach(System.out::println);
    }

    private void allFriendships(int id) {
        try {
            List<UsersFriendsDTO> usersFriendsDTOList = service.getFriends(id);
            usersFriendsDTOList.forEach(System.out::println);
        } catch (RepositoryException e) {
            System.out.println(e.getMessage());
        }

    }


    private void sendFriendInvite(int id, Scanner input) throws RepositoryException, ValidatorException {
        int id1;
        System.out.println("Sent friend request to id: ");
        id1 = Integer.parseInt(input.nextLine());
        service.findUser(id1);
        service.addFriendRequest(id, id1);
        System.out.println("Friend request sent!");
    }

    private void seeFriendInvites(int id, Scanner input) throws RepositoryException {
        var requests = service.getFriendRequests(id);
        if (requests.size() == 1) {
            System.out.println("You have " + requests.size() + " friend request!");
        } else {
            System.out.println("You have " + requests.size() + " friend requests!");
        }
        if (requests.size() > 0) {
            System.out.print("From: ");
            requests.forEach(dto -> System.out.println(dto.getFrom()));
        }
    }

    private void respondInvite(int id, Scanner input) throws RepositoryException, ValidatorException, EntityException {
        int id1;
        String st;

        System.out.println("Respond to user with the id: ");
        id1 = Integer.parseInt(input.nextLine());

        System.out.println("APPROVE / REJECT ?");
        st = input.nextLine();
        service.respondFriendRequest(id1, id, st);
    }

    private void respondToAllInvites(int id, Scanner input) throws RepositoryException {
        String st;
        if (service.getFriendRequests(id).size() == 0) {
            System.out.println("You have no friend requests!");
            return;
        }
        System.out.println("APPROVE / REJECT all requests?");
        st = input.nextLine();
        service.respondToAllRequests(id, st);
    }

    private void chatRoom(int id, Scanner input) throws Exception {
        MessageUi messageUi = new MessageUi(service, id);
        messageUi.run();

    }



    private int preLogin(int login_id, Scanner input) {
        int cmd;
        do {
            menuPreLogin();
            cmd = Integer.parseInt(input.nextLine());
            try {
                switch (cmd) {
                    case 1 -> {
                        login_id = login(input);
                        return login_id;
                    }
                    case 2 -> add(input);
                    case 3 -> allUsers();
                    case 4 -> communitiesNumber();
                    case 5 -> largestCommunity(input);
                    case 6 -> printNetwork();
                    case 0 -> {
                        System.out.println("Ending...");
                        return 0;
                    }
                    default -> System.out.println("Not a valid command!");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (login_id == -1 && cmd != 0);
        return 0;
    }


    private void afterLogin(int login_id, Scanner input) {
        int cmd = -1;
        while (cmd != 0) {
            menuAfterLogin();
            cmd = Integer.parseInt(input.nextLine());
            try {
                switch (cmd) {
                    case 1 -> updateUser(login_id, input);
                    case 2 -> removeFriendship(login_id, input);
                    case 3 -> allFriendships(login_id);
                    case 4 -> sendFriendInvite(login_id, input);
                    case 5 -> seeFriendInvites(login_id, input);
                    case 6 -> respondInvite(login_id, input);
                    case 7 -> respondToAllInvites(login_id, input);
                    case 8 -> findFriendshipByUserAndMonth(login_id, input);
                    case 9 -> chatRoom(login_id, input);
                    case 0 -> System.out.println("Logged out");
                    default -> System.out.println("Not a valid command!");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public void menuPreLogin() {
        System.out.println("1.Login");
        System.out.println("2.Add new user");
        System.out.println("3.Show all users");
        System.out.println("4.Number of communities");
        System.out.println("5.Largest community");
        System.out.println("6.Print network");
        System.out.println("0.Exit");
    }


    public void menuAfterLogin() {
        System.out.println("1.Update user");
        System.out.println("2.Delete friendship");
        System.out.println("3.Show all friendships");
        System.out.println("4.Send friend request");
        System.out.println("5.See friend requests");
        System.out.println("6.Respond to friend request");
        System.out.println("7.Respond to all friend requests");
        System.out.println("8.Print the friendship relationships made in a certain month");
        System.out.println("9.ChatRoom");
        System.out.println("0.Logout");
    }


    public void run() throws ValidatorException, RepositoryException {
        Scanner input = new Scanner(System.in);
        int login_id = -1;
        int cmd = -1;

        login_id = preLogin(login_id, input);
        while (login_id != 0) {
            if(login_id>0){
                afterLogin(login_id, input);
            }
            login_id = preLogin(login_id, input);
        }
    }


}

