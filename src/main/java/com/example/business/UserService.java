package com.example.business;

import com.example.domain.User;
import com.example.domain.ValidatorUser;
import com.example.exception.RepositoryException;
import com.example.exception.ValidatorException;
import com.example.repository.Repository;
import com.example.repository.database.DataBaseUserRepository;
import com.example.repository.file.FileUserRepository;
import com.example.socialnetworkgui.UserModel;

import java.sql.*;
import java.util.ArrayList;


public class UserService {
    private Connection connection;
    private Statement statement;
    Repository<Integer, User> repository;
    ValidatorUser validator;

    /**
     * constructor
     *
     * @param url      the url of database
     * @param user     the user of database
     * @param password the password of database
     */
    public UserService(String url, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
        statement = connection.createStatement();
        this.repository = new DataBaseUserRepository(connection, statement);
        this.validator = new ValidatorUser();
    }

    public UserService(Connection connection, Statement statement) {
        this.connection = connection;
        this.statement = statement;
        this.repository = new DataBaseUserRepository(connection, statement);
        this.validator = new ValidatorUser();
    }

    public UserService(String fileName) {
        this.repository = new FileUserRepository(fileName);
        this.validator = new ValidatorUser();
    }

    /**
     * @return an integer representing the size of the repository
     */
    public int size() {
        int si=0;
        String sql = "select count(*) from users";
        try{
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()){
                si = rs.getInt("count");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//        return repository.size();
        return si;
    }

    /**
     * @return true if the repository is empty, false otherwise
     */
    public boolean isEmpty() {
        return repository.isEmpty();
    }

    /**
     * Generates a new id based on how many records are already in the repository
     *
     * @return an integer representing the generated id
     * @throws RepositoryException
     */
    private int generateId() throws RepositoryException {
        int id = repository.size();
        try {
            while (true) {
                User user = repository.find(id);
                if (user == null) {
                    break;
                }
                id++;
            }
        } catch (Exception ignored) {
        }
        return id;
    }

    /**
     * @param firstName string representing the first name of the user we want to add
     * @param lastName  string representing the last name of the user we want to add
     * @throws ValidatorException  if the desired user is not valid
     * @throws RepositoryException if the user already exists in the repository
     */
    public User add(String username, String firstName, String lastName, String password) throws RepositoryException, ValidatorException {
        System.out.println("d");
        User user = new User(username, firstName, lastName, password);
        System.out.println("e");
        int id = generateId();
        System.out.println(id + "a");
        user.setId(id);
        validator.valideaza(user);
        repository.add(id, user);
        return user;
    }

    /**
     * Updates the user with the given id
     *
     * @param id        integer representing the id of the user we want to modify
     * @param firstName new first name
     * @param lastName  new last name
     * @throws RepositoryException if a user with the given id does not exist in the repository
     * @throws ValidatorException  if the new names are not valid
     */
    public void update(int id, String username, String firstName, String lastName, String password) throws RepositoryException, ValidatorException {
        User user = new User(username, firstName, lastName, password);
        user.setId(id);
        validator.valideaza(user);
        repository.update(id, user);
    }

    /**
     * @return an ArrayList representing all the users in the repository
     */
    public ArrayList<User> all() {
        return repository.all();
    }

    /**
     * @param id integer representing the id of the user we want to find
     * @return a user that has the given id
     * @throws RepositoryException if a user with the given username does not exists in the repository
     */
    public User find(int id) throws RepositoryException {
        return repository.find(id);
    }


    /**
     * @param id integer representing the id of the user we want to erase
     * @throws RepositoryException if a user with the given id does not exist in the repository
     */
    public void remove(int id) throws RepositoryException {
        repository.remove(id);
    }


    /**
     * @param username a string representing the username of the user we want to find
     * @return a UserModel containing the id, firstname and lastname of the user with the given username
     * or null if such a user does not exist
     */
    public UserModel findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username='" + username + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                String id = String.valueOf(resultSet.getInt("id"));
                String firstname = resultSet.getString("firstname");
                String lastname = resultSet.getString("lastname");
                UserModel user = new UserModel(id, username, firstname, lastname);
                return user;
            }
            return null;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
