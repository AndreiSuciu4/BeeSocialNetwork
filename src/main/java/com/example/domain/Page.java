package com.example.domain;
import java.util.List;

public class Page {
    String firstName;
    String lastName;
    List<UsersFriendsDTO> listOfFriends;
    List<MessageDTO> receivedMessages;
    List<UsersRequestsDTO> receivedRequest;

    public Page(String firstName, String lastName, List<UsersFriendsDTO> listOfFriends, List<MessageDTO> receivedMessages, List<UsersRequestsDTO> receivedRequest) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.listOfFriends = listOfFriends;
        this.receivedMessages = receivedMessages;
        this.receivedRequest = receivedRequest;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<UsersFriendsDTO> getListOfFriends() {
        return listOfFriends;
    }

    public void setListOfFriends(List<UsersFriendsDTO> listOfFriends) {
        this.listOfFriends = listOfFriends;
    }

    public List<MessageDTO> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<MessageDTO> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public List<UsersRequestsDTO> getReceivedRequest() {
        return receivedRequest;
    }

    public void setReceivedRequest(List<UsersRequestsDTO> receivedRequest) {
        this.receivedRequest = receivedRequest;
    }
}
