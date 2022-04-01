package com.example.business;

import com.example.build.Build;
import com.example.domain.*;
import com.example.exception.EntityException;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.socialnetworkgui.RequestModel;
import com.example.socialnetworkgui.UserModel;
import com.example.utils.Graph;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Observable;
import java.util.*;
import java.util.stream.Collectors;


import java.time.LocalDateTime;

public class Controller extends Observable {
    private Connection connection;
    private Statement statement;
    private UserService serviceUsers;
    private FriendshipService serviceFriendships;
    private RequestService serviceRequests;
    private MessageService messageService;
    private EventService eventService;
    private Graph<Integer> network;


    /**
     * constructor
     *
     * @param url      the url of database
     * @param dbuser   the user of database
     * @param password the password of database
     */
    public Controller(String url, String dbuser, String password) {
        try {
            connection = DriverManager.getConnection(url, dbuser, password);
            statement = connection.createStatement();
            serviceUsers = new UserService(connection, statement);
            serviceFriendships = new FriendshipService(connection, statement);
            serviceRequests = new RequestService(connection, statement);
            messageService = new MessageService(connection, statement);
            eventService = new EventService(connection, statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        network = new Graph<>();
        serviceUsers.all().forEach(user -> {
            network.addVertex(user.getId());
        });
        serviceFriendships.all().forEach(fr -> {
            network.addEdge(fr.getUserA(), fr.getUserB());
        });
    }

    /**
     * Deletes the current friendship network and updates it with new data from the friendship service
     */
    public void refreshNetwork() {
        network.delete();
        serviceFriendships.all().forEach(fr -> {
            network.addEdge(fr.getUserA(), fr.getUserB());
        });
    }

    /**
     * @param name    string representing the name of the new user we want to add
     * @param surname string representing the surname of the new user we want to add
     * @throws RepositoryException if the given user already exists
     * @throws ValidatorException  if the given strings are not valid
     *                             The function adds a new user to the User repository
     */
    public User add(String username, String name, String surname, String password) throws RepositoryException, ValidatorException {
        User user = serviceUsers.add(username, name, surname, password);
        network.addVertex(user.getId());
        return user;
    }

    /**
     * Updates the user with the given id
     *
     * @param id        integer representing the id of the user we want to modify
     * @param firstName new first name
     * @param lastName  new last name
     * @throws RepositoryException if the user with the given id does not exist
     * @throws ValidatorException  if the new fields are not valid
     */
    public void updateUser(int id, String username, String firstName, String lastName, String password) throws RepositoryException, ValidatorException {
        serviceUsers.update(id, username, firstName, lastName, password);
    }

    /**
     * @return an ArrayList containing all the users
     */
    public ArrayList<User> allUsers() {
        return serviceUsers.all();
    }


    /**
     * @param id integer representing the id of the user we want to find
     * @return the user with the given username
     * @throws RepositoryException if the searched user does not exist
     */
    public User findUser(int id) throws RepositoryException {
        return serviceUsers.find(id);
    }

    /**
     * Gets all the friends of the user with the given id
     *
     * @param id the user's id
     * @return a List of users
     * @throws RepositoryException if the user with the given id does not exist
     */
    public List<UsersFriendsDTO> getFriends(int id) throws RepositoryException {
        User user = serviceUsers.find(id);
        List<UsersFriendsDTO> rez = new ArrayList<>();
        network.getEdges(id).forEach(nod -> {
            try {
                User other = findUser(nod);
                Friendship fr = serviceFriendships.findByUsers(id, other.getId());
                rez.add(new UsersFriendsDTO(user, other, fr.getDate()));
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        });
        return rez;
    }

    /**
     * @param id integer representing the id of the user we want to remove
     * @throws RepositoryException if the user does not exist
     *                             The function erases from the repository the user with the given username
     */
    public void removeUser(int id) throws RepositoryException {
        serviceFriendships.all().forEach((fr) -> {
            if (fr.isPart(id)) {
                try {
                    serviceFriendships.remove(fr.getId());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        });
        serviceUsers.remove(id);
        network.removeVertex(id);


    }


    /**
     * @param username1 integer representing the id of the first user
     * @param username2 integer representing the id of the second user
     * @throws RepositoryException if any of the users does not exist in the repository
     * @throws EntityException     if the friendship between the users already exists
     *                             The function creates a new Friendship between the two given users
     */
    public void addFriend(int username1, int username2) throws RepositoryException, EntityException, ValidatorException {
        serviceFriendships.add(username1, username2, LocalDateTime.now());
        network.addEdge(username1, username2);
        setChanged();
        notifyObservers();
    }

    /**
     * Updates a friendship with the given id
     *
     * @param id       integer representing the id of the friendship
     * @param newUserA the new first user of a frienship
     * @param newUserB the new second user of a friendship
     * @throws RepositoryException if a friendship with the given id does not exist
     * @throws EntityException     if a friendship with the given id does not exist
     */
    public void updateFriend(int id, int newUserA, int newUserB) throws RepositoryException, EntityException, ValidatorException {
        Friendship old = serviceFriendships.find(id);
        User userA = serviceUsers.find(old.getUserA()), userB = serviceUsers.find(old.getUserB());
        network.removeEdge(userA.getId(), userB.getId());
        serviceFriendships.update(id, newUserA, newUserB);

        //checks if the new users are in the repository
        User newUser1 = serviceUsers.find(newUserA), newUser2 = serviceUsers.find(newUserB);

        network.addEdge(newUserA, newUserB);
    }

    /**
     * @param username1 int representing the id of the first user
     * @param username2 int representing the id of the second user
     * @throws RepositoryException if any of the users does not exist in the repository
     * @throws EntityException     if the friendship between the users does not exist
     *                             The function removes the frienship between the two given users
     */
    public void removeFriends(int username1, int username2) throws RepositoryException, EntityException {
        Friendship friendship = serviceFriendships.findByUsers(username1, username2);
        serviceFriendships.remove(friendship.getId());
        network.removeEdge(username1, username2);
        setChanged();
        notifyObservers();
    }

    /**
     * @return an ArrayList of users representing the largest community
     */
    public List<User> largestCommunity() {
        List<List<Integer>> communities = network.components();
        List<User> rez = new ArrayList<>();
        for (List<Integer> list : communities) {
            if (list.size() > rez.size()) {
                rez.clear();
                List<User> temp = new ArrayList<>();
                for (int id : list) {
                    try {
                        temp.add(serviceUsers.find(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rez.addAll(temp);
            }
        }
        return rez;
    }


    /**
     * Get all the friendships from the friendship service
     *
     * @return a List of friendships with all the friendships
     */
    public List<Friendship> allFriendships() {
        return serviceFriendships.all();
    }

    /**
     * @return an integer representing the number of communities
     */
    public int communitiesNumber() {
        return network.components().size();
    }

    /**
     * Prints the users network
     */
    public void printNetwork() {
        System.out.println(network.toString());
    }

    /**
     * Gets all the friends of the user with the given id, created in a given month
     *
     * @param user  the user's id
     * @param month the friendship's month
     * @return a list of UsersFriendsDTO
     * @throws RepositoryException if the user with the given id does not exist
     * @throws ValidatorException  if month is not valid
     */
    public List<UsersFriendsDTO> findFriendshipByUserAndMonth(int user, int month) throws RepositoryException, ValidatorException {
        //validate the params
        if (month < 0 || month > 12)
            throw new ValidatorException("invalid month!\n");
        User user1 = serviceUsers.find(user);

        List<Friendship> friendshipList = serviceFriendships.all();
        List<UsersFriendsDTO> usersFriendsDTOList = new ArrayList<>();
        friendshipList.stream()
                .filter(x -> x.getDate().getMonth().getValue() == month)
                .forEach(x -> {
                    if (x.getUserA() == user || x.getUserB() == user) {
                        try {
                            User user2 = serviceUsers.find(x.getUserB());
                            usersFriendsDTOList.add(new UsersFriendsDTO(user1, user2, x.getDate()));
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return usersFriendsDTOList;
    }


    /**
     * @param from integer representing the id of the user who is sending a friend request
     * @param to   integer representing the id of the user who is receiving the friend request
     * @throws ValidatorException  if the new request is not valid
     * @throws RepositoryException if between the two users already exists a friendship or a request
     */
    public void addFriendRequest(int from, int to) throws ValidatorException, RepositoryException {
        serviceRequests.add(from, to, Status.PENDING);
        setChanged();
        notifyObservers();
    }

    /**
     * @param from integer representing the id of the user who sent the request
     * @param to   integer representing the id of the user who received the friend request
     * @param st   the response to the request
     * @throws RepositoryException if there is not a request between the two users
     * @throws ValidatorException  if the new status is not valid
     * @throws EntityException
     */
    public void respondFriendRequest(int from, int to, String st) throws RepositoryException, ValidatorException, EntityException {
        Status status = st.equals("APPROVE") ? Status.APPROVED :
                Status.REJECTED;
        FriendRequest fr = serviceRequests.findByUsers(from, to);
        serviceRequests.update(fr.getId(), from, to, status);
        if (status == Status.APPROVED) {
            addFriend(from, to);
            serviceRequests.remove(fr.getId());
            setChanged();
            notifyObservers();
        } else {
            serviceRequests.remove(fr.getId());
        }
    }

    /**
     * @param from integer representing the id of the user who sent the request
     * @param to   integer representing the id of the user who received the request
     * @throws RepositoryException if a request between the two users does not exists
     *                             the function deletes the request betweent the user with the id from and the
     *                             user with the id to
     */
    public void deleteFriendRequest(int from, int to) throws RepositoryException {
        FriendRequest fr = serviceRequests.findByUsers(from, to);
        serviceRequests.remove(fr.getId());
        setChanged();
        notifyObservers();
    }

    /**
     * @param id the id of the user of which we want to see the friend requests
     * @return a list of userRequestsDto which represents the users which have sent the user with the given id
     * a friend request
     */
    public List<UsersRequestsDTO> getFriendRequests(int id) {
        return serviceRequests.all().stream().filter(fr -> fr.isTo(id)).map(fr -> {
            UsersRequestsDTO dto = null;
            try {
                dto = new UsersRequestsDTO(serviceUsers.find(fr.getFrom()), serviceUsers.find(fr.getTo()), fr.getStatus());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            return dto;
        }).collect(Collectors.toList());
    }


    /**
     * @param requestModels the list of requests
     * @param id            the id of the user which we want to see the friend requests
     * @param pageSize      the amount of new records to the list
     * @param offset        the index from which we want the requests in the db
     *                      the function adds pageSize new values to requestModels from the given offset
     */
    public void getFriendRequestsPag(List<RequestModel> requestModels, int id, int pageSize, int offset) {
        String sql = "SELECT u.id, u.firstname, u.lastname, fr.status, fr.datesend FROM friendship_invites fr INNER JOIN users u ON u.id = fr.usera\n" +
                "WHERE fr.userb=" + id + " ORDER BY id LIMIT " + pageSize + " OFFSET " + offset;
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String uid = String.valueOf(resultSet.getInt("id"));
                String firstname = resultSet.getString("firstname");
                String lastname = resultSet.getString("lastname");
                String status = resultSet.getString("status");
                var datesend = resultSet.getTimestamp("datesend");
                String date;
                if (datesend == null) {
                    date = LocalDateTime.now().format(Build.formatter);
                } else {
                    date = resultSet.getTimestamp("datesend").toLocalDateTime().format(Build.formatter);
                }
                RequestModel requestModel = new RequestModel(uid, firstname, lastname, date, status);
                requestModels.add(requestModel);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * @param id the id of the user who sent the friend requests
     * @return a list of userRequestsDto which represents the users to which the users with the given id
     * has sent a friend request
     */
    public List<UsersRequestsDTO> sentFriendRequests(int id) {
        return serviceRequests.all().stream().
                filter(fr -> fr.isFrom(id)).
                map(fr -> {
                    UsersRequestsDTO dto = null;
                    try {
                        dto = new UsersRequestsDTO(serviceUsers.find(fr.getFrom()), serviceUsers.find(fr.getTo()), fr.getStatus());
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    return dto;
                }).collect(Collectors.toList());
    }


    /**
     * @param requestModels the list of requests
     * @param id            the id of the user who sent the friend requests
     * @param pageSize      the amount of new records we want added to the list
     * @param offset        the index from which we want the requests in the db
     *                      the function adds pageSize new values to requestModels from the given offset
     */
    public void sentFriendRequestsPag(List<RequestModel> requestModels, int id, int pageSize, int offset) {
        String sql = "SELECT u.id, u.firstname, u.lastname, fr.status, fr.datesend FROM friendship_invites fr INNER JOIN users u ON u.id = fr.userb WHERE fr.usera="
                + id + "ORDER BY id LIMIT " + pageSize + " OFFSET " + offset + ";";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String uid = String.valueOf(resultSet.getInt("id"));
                String firstname = resultSet.getString("firstname");
                String lastname = resultSet.getString("lastname");
                String status = resultSet.getString("status");
                var datesend = resultSet.getTimestamp("datesend");
                String date;
                if (datesend == null) {
                    date = LocalDateTime.now().format(Build.formatter);
                } else {
                    date = resultSet.getTimestamp("datesend").toLocalDateTime().format(Build.formatter);
                }
                RequestModel requestModel = new RequestModel(uid, firstname, lastname, date, status);
                requestModels.add(requestModel);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * @param id integer representing the id of the user who is responding to all his requests
     * @param st string representing the response
     */
    public void respondToAllRequests(int id, String st) {
        getFriendRequests(id).forEach(fr -> {
            try {
                respondFriendRequest(fr.getFrom().getId(), fr.getTo().getId(), st);
                setChanged();
                notifyObservers();
            } catch (RepositoryException | EntityException | ValidatorException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean existsFriendRequest(int id1, int id2){
        List<FriendRequest> friendRequests = serviceRequests.all();
        for(FriendRequest friendRequest : friendRequests)
            if((friendRequest.getFrom() == id1 && friendRequest.getTo() == id2) || (friendRequest.getFrom() == id2 && friendRequest.getTo() == id1)) {
                return true;
            }
        return false;
    }

    /**
     * Add a message in database
     *
     * @param from    User representing the sender of the message
     * @param to      List of User representing the receiver of the message
     * @param message String representing the message of the Message
     * @throws RepositoryException if there is another message with the same id in the database or if i want so send myself a message
     * @throws ValidatorException  if the new Message is not valid
     */
    public void addNewMessage(int from, List<Integer> to, String message) throws RepositoryException, ValidatorException {
        messageService.addNewMessage(from, to, message);
        setChanged();
        notifyObservers();
    }

    /**
     * @return a list of messages representing all the sent messages
     */
    public List<Message> allMessage() {
        List<MessageDTO> messageDTOS = messageService.all();
        List<Message> messages = new ArrayList<>();
        for (MessageDTO messageDTO : messageDTOS) {
            List<User> to = new ArrayList<>();
            for (Integer user : messageDTO.getTo()) {
                try {
                    User user1 = serviceUsers.find(user);
                    to.add(user1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            User from = null;
            try {
                from = serviceUsers.find(messageDTO.getFrom());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            Message message = new Message(from, to, messageDTO.getMessage());
            message.setData(messageDTO.getData());
            message.setId(messageDTO.getId());
            if (messageDTO.getReply() != 0) {
                try {
                    Message message1 = findMessage(messageDTO.getReply());
                    message.setReply(message1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            messages.add(message);
        }
        return messages;
    }


    /**
     * Remove a message from database
     *
     * @param id Integer representing the id of message which we want to remove
     * @throws RepositoryException if there is no message with the id given in the database
     */
    public void removeMessage(int id) throws RepositoryException {
        messageService.removeMessage(id);
        setChanged();
        notifyObservers();
    }

    /**
     * Find a message in database
     *
     * @param id Integer representing the id of the message which we are looking for
     * @return Message representing the message which we are looking for
     * @throws RepositoryException if there is no message with the id given in the database
     */
    public Message findMessage(int id) throws RepositoryException {
        MessageDTO messageDTO = messageService.findMessage(id);
        Message message;
        List<User> toUser = new ArrayList<>();
        for (Integer user : messageDTO.getTo()) {
            try {
                User user1 = serviceUsers.find(user);
                toUser.add(user1);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        User from = null;
        try {
            from = serviceUsers.find(messageDTO.getFrom());
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        message = new Message(from, toUser, messageDTO.getMessage());
        message.setData(messageDTO.getData());
        message.setId(messageDTO.getId());
        if (messageDTO.getReply() != 0) {
            try {
                Message message1 = findMessage(messageDTO.getReply());
                message.setReply(message1);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
        message.setReply(null);
        return message;

    }

    /**
     * Find all messages for a user
     *
     * @param idUser Integer representing the id of the user
     * @return List of Message representing all messages for a user
     * @throws RepositoryException if there are no messages with for this user in database
     */
    public List<Message> findMessages(int idUser) throws RepositoryException {
        List<MessageDTO> messageDTOS = messageService.findMessages(idUser);
        List<Message> messages = new ArrayList<>();
        for (MessageDTO messageDTO : messageDTOS) {
            List<User> to = new ArrayList<>();
            for (Integer user : messageDTO.getTo()) {
                try {
                    User user1 = serviceUsers.find(user);
                    to.add(user1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            User from = null;
            try {
                from = serviceUsers.find(messageDTO.getFrom());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            Message message = new Message(from, to, messageDTO.getMessage());
            message.setData(messageDTO.getData());
            message.setId(messageDTO.getId());
            if (messageDTO.getReply() != 0) {
                try {
                    Message message1 = findMessage(messageDTO.getReply());
                    message.setReply(message1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            messages.add(message);
        }
        return messages;
    }

    public List<MessageDTO> allMessages(int id) throws RepositoryException {
        return  messageService.findMessages(id);

    }

    /**
     * @param id1     integer representing the id of the message
     * @param from    integer representing the user who sent the message
     * @param to      list of integers representing the users who received the message
     * @param message string representing the message
     * @throws RepositoryException if any of the given ids does not exist
     */
    public void updateMessage(int id1, int from, List<Integer> to, String message, Integer reply) throws RepositoryException, ValidatorException {
        messageService.updateMessage(id1, from, to, message, reply);
    }

    /**
     * Reply message
     *
     * @param from    User representing the sender of the message
     * @param to      List of User representing the receiver of the message
     * @param mess    String representing the message of the Message
     * @param message Integer representing the id of the message which we want to reply
     * @throws RepositoryException if there is another message with the same id in the database or if i want so send myself a message or
     *                             if the sender is invalid or if the receptor is iinvalid
     * @throws ValidatorException  if the new Message is not valid
     */
    public void replyMessage(int from, List<Integer> to, String mess, int message) throws RepositoryException, ValidatorException {
        messageService.replyMessage(from, to, mess, message);
        setChanged();
        notifyObservers();
    }


    /**
     * @param from integer representing the id of the user which is sending the message
     * @param mess string representing the content of the message
     * @throws ValidatorException  if the message is not valid
     * @throws RepositoryException if the message is sent to himself
     */
    public void replyAll(int from, String mess) throws ValidatorException, RepositoryException {
        List<User> users = getFriendsForAUser(from);
        List<Integer> to = new ArrayList<>();
        users.forEach(x -> to.add(x.getId()));
        messageService.addNewMessage(from, to, mess);
        setChanged();
        notifyObservers();

    }

    /**
     * return a list of Message sent between tho users sorted by data
     *
     * @param id1 Integer representing the id of the first User
     * @param id2 Integer representing the id of the second User
     * @return List of Messages
     * @throws RepositoryException if id1 or id 2 are not valid
     */
    public List<MessageDTO> getConversation(int id1, int id2) throws RepositoryException {
        List<MessageDTO> messagesD = messageService.all();
        return messagesD.stream().
                filter(x -> ((x.getFrom() == id1 && x.getTo().contains(id2)) || (x.getFrom() == id2 && x.getTo().contains(id1)))
                )
                .sorted(Comparator.comparing(MessageDTO::getData))
                .collect(Collectors.toList());
    }


    /**
     * @param conversation a List<MessageDTO> representing the conversation between two users
     * @param id1          integer representing the id of the first user
     * @param id2          integer representing the id of the second user
     * @param pageSize     integer representing the number of new messages we want to add to the conversation
     * @param offset       integer representing the offset from which we want to add new messages from the conversation
     *                     The function adds a number equal to pageSize of messages between the two users to the conversation,
     *                     starting from the given offset in the db
     */
    public void getConversationPag(List<MessageDTO> conversation, int id1, int id2, int pageSize, int offset) {
        String sql = "SELECT * FROM messages INNER JOIN users_messages um ON messages.ms_id = um.mess_id " +
                "WHERE um.from_user=" + id2 + " AND um.to_user=" + id1 + " OR um.from_user=" + id1 + " AND um.to_user=" + id2 +
                " ORDER BY data DESC LIMIT " + pageSize + " OFFSET " + offset + ";";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int id = resultSet.getInt("mess_id");
                int from = resultSet.getInt("from_user");
                int to = resultSet.getInt("to_user");
                String message = resultSet.getString("mess");
                LocalDateTime date = resultSet.getTimestamp("data").toLocalDateTime();
                int reply = resultSet.getInt("reply_to");
                MessageDTO newMessage = new MessageDTO(from, new ArrayList<>(to), message);
                newMessage.setId(id);
                newMessage.setData(date);
                newMessage.setReply(reply);
                conversation.add(0, newMessage);

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Find all messages sent by a user
     *
     * @param id Integer representing the id of the user
     * @return List of Message representing all messages sent by a user
     */
    public List<Message> allMessageByUser(int id) {
        List<MessageDTO> messageDTOS = messageService.allMessageByUser(id);
        List<Message> messages = new ArrayList<>();
        for (MessageDTO messageDTO : messageDTOS) {
            List<User> to = new ArrayList<>();
            for (Integer user : messageDTO.getTo()) {
                try {
                    User user1 = serviceUsers.find(user);
                    to.add(user1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            User from = null;
            try {
                from = serviceUsers.find(messageDTO.getFrom());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            Message message = new Message(from, to, messageDTO.getMessage());
            message.setData(messageDTO.getData());
            message.setId(messageDTO.getId());
            if (messageDTO.getReply() != 0) {
                try {
                    Message message1 = findMessage(messageDTO.getId());
                    message.setReply(message1);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
            messages.add(message);
        }
        return messages;
    }


    /**
     * @param id integer representing the id of the user
     * @param n  a string
     * @return a list of users whose username, firstname or lastname contains n and are not friends with the given user
     */
    public List<User> getNoFriend(int id, String n) {
        String name = n.toUpperCase();
        List<User> users = new ArrayList<>();
        List<User> userList = serviceUsers.all();
        List<Integer> users1 = network.getEdges(id);
        userList.
                forEach(x -> {
                    if (!users1.contains(x.getId()))
                        users.add(x);
                });
        List<User> sentRequests = this.sentFriendRequests(id).stream().map(UsersRequestsDTO::getTo).collect(Collectors.toList());
        List<User> receivedRequests = this.getFriendRequests(id).stream().map(UsersRequestsDTO::getFrom).collect(Collectors.toList());
        return users.stream().filter(x -> (x.getUsername().toUpperCase().equals(name)  && x.getId() != id)).collect(Collectors.toList());
    }

    /**
     * @param id1 integer representing the id of a user
     * @param id2 integer representing the id of another user
     * @return a Friendship if the two are friends, null otherwise
     */
    public Friendship getFriendship(int id1, int id2) {
        List<Friendship> friendships = serviceFriendships.all();
        for (Friendship friendship : friendships)
            if ((friendship.getUserB() == id1 && friendship.getUserA() == id2) || (friendship.getUserB() == id2 && friendship.getUserA() == id1))
                return friendship;

        return null;
    }


    /**
     * @param friends  a List<UserModel> representing the list of friends for a given user
     * @param user     id of a given user
     * @param pageSize how many new values we want to add in the friends list
     * @param offset   the offset from which we want to begin adding the new friends
     *                 The function adds a number equal to pageSize of the user's friends, starting from the given offset in the db
     */
    public void getFriendsForAUserPag(List<UserModel> friends, int user, int pageSize, int offset) {
        if(serviceUsers.size()<pageSize){
            friends = serviceUsers.all().stream().map(x->{
                UserModel um = new UserModel(x.getId().toString(), x.getUsername(), x.getFirstName(), x.getLastName());
                return um;
            }).collect(Collectors.toList());
            return;
        }
        String sql = "select u1.id, u1.username, u1.firstname, u1.lastname, f1.fr_data from users inner join friendships f1 on users.id = f1.usera inner join users u1 on u1.id = f1.userb WHERE users.id=" +
                user + " UNION select u.id, u.username, u.firstname, u.lastname, f.fr_data from users inner join friendships f on users.id = f.userb inner join users u on u.id = f.usera\n" +
                "        where users.id= " + user + "order by id LIMIT " + pageSize + " OFFSET " + offset;
        try {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                int user1 = set.getInt("id");
                String username = set.getString("username");
                String firstname = set.getString("firstname");
                String lastname = set.getString("lastname");
                LocalDateTime date = set.getTimestamp("fr_data").toLocalDateTime();
                UserModel newUser = new UserModel(String.valueOf(user1), username, firstname, lastname);
                friends.add(newUser);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /**
     * @param user integer representing the id of a given user
     * @return a list of all the user's friends
     * @throws RepositoryException
     * @throws ValidatorException
     */
    public List<User> getFriendsForAUser(int user) throws RepositoryException, ValidatorException {

        List<Friendship> friendshipList = serviceFriendships.all();
        List<User> users = new ArrayList<>();
        friendshipList.stream().filter(x -> x.getUserA() == user || x.getUserB() == user).forEach(x -> {
            try {
                if (x.getUserA() == user) {
                    User user1 = serviceUsers.find(x.getUserB());
                    users.add(user1);
                } else {
                    User user1 = serviceUsers.find(x.getUserA());
                    users.add(user1);
                }

            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        });
        return users;
    }

    /**
     * @param id integer representing the id of the message
     * @return the message with the given id from the repository
     * @throws RepositoryException
     */
    public MessageDTO findMessageDTO(int id) throws RepositoryException {
        return messageService.findMessage(id);
    }

    /**
     * @param username a string representing a user's username
     * @param password a string representing a user's password
     * @return an integer representing the user's id
     * @throws RepositoryException if such a user does no exists
     */
    public int getUserByUsernameAndPassword(String username, String password) throws RepositoryException {
        List<User> users = serviceUsers.all();
        for (User user : users)
            if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                return user.getId();
            }
        throw new RepositoryException("Incorect username or password");
    }

    /**
     * @param user   id of the user
     * @param day1   integer
     * @param month1 integer
     * @param year1  integer representing the start date
     * @param day2   integer
     * @param month2 integer
     * @param year2  integer representing the end date
     * @return a list of Friendships which have been made by the user between the given dates
     * @throws Exception
     */
    public List<Friendship> friendshipsBetween2Dates(int user, int day1, int month1, int year1, int day2, int month2, int year2) throws Exception {
        LocalDateTime date1 = LocalDateTime.of(year1, month1, day1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(year2, month2, day2, 23, 59);
        ArrayList<Friendship> friendships = serviceFriendships.all();
        return friendships.stream().filter(x -> ((x.getUserA() == user || x.getUserB() == user) && x.getDate().compareTo(date1) >= 0 && x.getDate().compareTo(date2) <= 0)
        ).collect(Collectors.toList());
    }

    /**
     * @param user   id of the user
     * @param day1   integer
     * @param month1 integer
     * @param year1  integer representing the start date
     * @param day2   integer
     * @param month2 integer
     * @param year2  integer representing the end date
     * @return a list of messages which have been received by the user between the given dates
     * @throws Exception
     */
    public List<MessageDTO> messagesBetween2Dates(int user, int day1, int month1, int year1, int day2, int month2, int year2) throws Exception {
        LocalDateTime date1 = LocalDateTime.of(year1, month1, day1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(year2, month2, day2, 23, 59);
        List<MessageDTO> messages = messageService.all();
        return messages.stream().filter(x -> (x.getTo().contains(user) && x.getData().compareTo(date1) >= 0 && x.getData().compareTo(date2) <= 0)
        ).collect(Collectors.toList());
    }


    /**
     * @param user   id of the user that has received the messages
     * @param from   username the user which has sent the messages
     * @param day1   the day of the start date
     * @param month1 the month of the start date
     * @param year1  the year of the start date
     * @param day2   the day of the end date
     * @param month2 the month of the end date
     * @param year2  the year of the end date
     * @return a List<MessageDTO> representing the messages which have been sent by the from user to user
     * between the dates specified
     */
    public List<MessageDTO> getMessagesFromBetween(int user, String from, int day1, int month1, int year1, int day2, int month2, int year2) {
        List<MessageDTO> messages = new ArrayList<>();
        String startDate = year1 + "-" + month1 + "-" + day1;
        String endDate = year2 + "-" + month2 + "-" + day2;
        int fromId = Integer.parseInt(serviceUsers.findByUsername(from).getId());
        String sql = "SELECT * " +
                "from messages " +
                "inner join users_messages um on messages.ms_id = um.mess_id " +
                "where um.from_user=" + fromId + " and um.to_user=" + user + " and data between '" + startDate + "' AND '" + endDate +
                "' order by data;";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int fromUser = resultSet.getInt("from_user");
                int toUser = resultSet.getInt("to_user");
                String message = resultSet.getString("mess");
                LocalDateTime date = resultSet.getTimestamp("data").toLocalDateTime();
                int reply = resultSet.getInt("reply_to");
                MessageDTO messageDTO = new MessageDTO(fromUser, new ArrayList<>(toUser), message);
                messageDTO.setData(date);
                messageDTO.setId(resultSet.getInt("ms_id"));
                messageDTO.setReply(reply);
                messages.add(messageDTO);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return messages;
    }

    /**
     * @param name        string representing the event's name
     * @param description string representing the event's description
     * @param date        LocalDate representing the date of the event
     * @throws ValidatorException  if the event is not valid
     * @throws RepositoryException if an equal event already exists
     */
    public void addNewEvent(String name, String description, LocalDate date) throws ValidatorException, RepositoryException {
        eventService.addNewEvent(name, description, date);
    }


    /**
     * @param userId  integer representing a user's id
     * @param eventId integer representing an event's id
     *                removes the subscription of the given user to the given event
     */
    public void removeSubscription(Integer userId, Integer eventId) {
        String sql = "DELETE FROM users_events WHERE user_id=" + userId.toString() + " AND ev_id=" + eventId.toString() + ";";
        try {
            statement.executeUpdate(sql);
            setChanged();
            notifyObservers();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * @param userId  integer representing a user's id
     * @param eventId integer representing an event's id
     *                adds attendance of the given user to the given event
     */
    public void addAttendance(Integer userId, Integer eventId) {
        String sql = "INSERT INTO users_events(user_id, ev_id) VALUES (" +
                userId.toString() + "," + eventId.toString() + ");";
        try {
            statement.executeUpdate(sql);
            setChanged();
            notifyObservers();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * @param id integer representing the id of a user
     * @return a list of events representing the events which the user is subscribed to
     */
    public List<Event> eventsForAUser(Integer id) {
        List<Event> eventList = new ArrayList<>();
        String sql = "SELECT e.ev_id, e.name, e.description, e.date FROM users_events " +
                "INNER JOIN events e ON e.ev_id = users_events.ev_id WHERE user_id=" + id.toString() + ";";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int evId = resultSet.getInt("ev_id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                Event event = new Event(name, description, date);
                event.setId(evId);
                eventList.add(event);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return eventList;
    }


    /**
     * @param id integer representing the id of the user
     * @return a list of events to which the given user is not subscribed
     */
    public List<Event> eventsNotAttendedByUser(Integer id) {
        List<Event> eventList = new ArrayList<>();
        List<Event> all = eventService.all();
        List<Event> subscribed = eventsForAUser(id);
        String sql;
        for (Event event : all) {
            if (!subscribed.contains(event)) {
                eventList.add(event);
            }
        }
        return eventList;
    }


    /**
     * @param id integer representing the id of a user
     * @return an event representing the next event which will happen for the given user
     */
    public Event nextEventForUser(Integer id) {
        String sql = "SELECT e.ev_id, e.name, e.description, e.date FROM users_events " +
                "INNER JOIN events e ON e.ev_id = users_events.ev_id WHERE user_id=" + id.toString() + " ORDER BY e.date ASC LIMIT 1;";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                int evId = resultSet.getInt("ev_id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                Event event = new Event(name, description, date);
                event.setId(evId);
                return event;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    /**
     * @param id integer representing the id of a user
     * @return the number of days untill the next event
     */
    public long daysUntilNextEvent(Integer id) {
        Event ev = nextEventForUser(id);
        return ChronoUnit.DAYS.between(LocalDate.now(), ev.getDate());
    }


}