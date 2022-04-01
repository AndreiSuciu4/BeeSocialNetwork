package com.example.business;

import com.example.domain.*;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.repository.Repository;
import com.example.repository.database.DataBaseMessageRepository;
import com.example.repository.database.DataBaseUserRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MessageService {
    private Connection connection;
    private Statement statement;
    private Repository<Integer, MessageDTO> repository;
    private Repository<Integer, User> userRepository;
    private ValidatorMessage validatorMessage;
    private int id = 1;

    /**
     * The constructor
     * @param url String representing the URL of database
     * @param user String representing the user of the database
     * @param password String representing the password of the user's account
     */
    public MessageService(String url, String user, String password) {
        try {
            this.repository = new DataBaseMessageRepository(url, user, password);
            this.userRepository = new DataBaseUserRepository(url, user, password);
            this.validatorMessage = new ValidatorMessage();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        generateId();
    }

    public MessageService(Connection connection, Statement statement) {
        this.connection = connection;
        this.statement = statement;
        repository = new DataBaseMessageRepository(connection, statement);
        userRepository = new DataBaseUserRepository(connection, statement);
        validatorMessage = new ValidatorMessage();
        generateId();
    }

    /**
     * generate the id of message
     */
    private void generateId(){

        String sql = "SELECT * FROM messages ORDER BY ms_id DESC";
        ArrayList<MessageDTO> messages = new ArrayList<>();
        try{
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                this.id = resultSet.getInt("ms_id")+1;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    /**
     * Add a message in database
     * @param from User representing the sender of the message
     * @param to List of User representing the receiver of the message
     * @param message String representing the message of the Message
     * @throws RepositoryException if there is another message with the same id in the database or if i want so send myself a message
     * @throws ValidatorException if the new Message is not valid
     */
    public void addNewMessage(int from, List<Integer> to, String message) throws RepositoryException, ValidatorException {
        MessageDTO messageDTO= new MessageDTO(from, to, message);
        messageDTO.setId(id);
        repository.add(id,messageDTO);
        id++;

    }

    /**
     * Return all message from database
     * @return List of Message representing all message from database
     */
    public List<MessageDTO> all(){
        return  repository.all();
    }

    /**
     * Remove a message from database
     * @param id Integer representing the id of message which we want to remove
     * @throws RepositoryException if there is no message with the id given in the database
     */
    public void removeMessage(int id) throws RepositoryException {
        repository.remove(id);
    }

    /**
     * Find a message in database
     * @param id Integer representing the id of the message which we are looking for
     * @return Message representing the message which we are looking for
     * @throws RepositoryException if there is no message with the id given in the database
     */
    public MessageDTO findMessage(int id) throws RepositoryException {
        return repository.find(id);
    }

    /**
     * Find all messages for a user
     * @param idUser Integer representing the id of the user
     * @return List of Message representing all messages for a user
     * @throws RepositoryException if there are no messages with for this user in database
     */
    public List<MessageDTO> findMessages(int idUser) throws RepositoryException {
        User user = userRepository.find(idUser);
        List<MessageDTO> list = repository.all();
        List<MessageDTO> listMessages = new ArrayList<>();
        list.forEach(x->{
            if(x.getTo().contains(user))
                listMessages.add(x);
        });
        return listMessages;
    }

    /**
     * Update a message
     * @param id1 Integer representing the id of message which we want to update
     * @param from User representing the sender of the message
     * @param to List of User representing the receiver of the message
     * @param message String representing the message of the Message
     * @throws RepositoryException if there is another message with the same id in the database or if i want so send myself a message
     * @throws ValidatorException if the new Message is not valid
     */
    public void updateMessage(int id1, int from, List<Integer> to, String message, Integer reply_to) throws RepositoryException, ValidatorException {
        MessageDTO messageDTO = new MessageDTO(from, to, message);
        messageDTO.setId(id1);
        messageDTO.setReply(reply_to);
        repository.update(id1, messageDTO);
    }

    /**
     * Reply message
     * @param from User representing the sender of the message
     * @param to List of User representing the receiver of the message
     * @param mess String representing the message of the Message
     * @param message Integer representing the id of the message which we want to reply
     * @throws RepositoryException if there is another message with the same id in the database or if i want so send myself a message or
     * if the sender is invalid or if the receptor is iinvalid
     * @throws ValidatorException if the new Message is not valid
     */
    public void replyMessage(int from, List<Integer> to, String mess, int message) throws RepositoryException, ValidatorException {
        MessageDTO messageDTO = new MessageDTO(from,to,mess);
        messageDTO.setId(id);
        messageDTO.setReply(message);
        repository.add(id,messageDTO);
        id++;
    }

    /**
     * Find all messages sent by a user
     * @param user Integer representing the id of the user
     * @return List of Message representing all messages sent by a user
     */
    public List<MessageDTO> allMessageByUser(int user){
        List<MessageDTO> messages = repository.all();
        return messages.stream().filter(x->x.getFrom() == user)
                .collect(Collectors.toList());
    }


}
