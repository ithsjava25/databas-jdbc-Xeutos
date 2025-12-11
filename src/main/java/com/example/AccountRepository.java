package com.example;

import java.sql.*;
import java.util.Objects;

public class AccountRepository {
    private Connection connection;

    public AccountRepository(Connection connection){
        this.connection = connection;
    }

    public boolean validateUser(String username, String password) throws SQLException {

        String query = "select name, password from account where name = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
        preparedStatement.setString(1, username);
        try(ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                String name = result.getString(1);
                String pass = result.getString(2);

                if (Objects.equals(name, username) && Objects.equals(pass, password)) {
                    return true;
                }
            }
        }
    }
        System.out.println("Invalid username or password");
        return false;
    }

    public void createAccount(String first_name, String last_name, String ssn, String password) throws SQLException {

        String insert = "insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?);";

        try(PreparedStatement preparedStatement = connection.prepareStatement(insert)) {
            preparedStatement.setString(1, first_name);
            preparedStatement.setString(2, last_name);
            preparedStatement.setString(3, ssn);
            preparedStatement.setString(4, password);

            preparedStatement.executeUpdate();
            System.out.println("Account created.");
        }
    }

    public void updatePassword(String newPassword, String id) throws SQLException {
        String update = "update account set password = ? where user_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(update)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, id);

            preparedStatement.executeUpdate();
            System.out.println("Password updated for id " + id);
        }
    }

    public void deleteAccount(String id) throws SQLException {
        String delete = "delete from account where user_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(delete)) {
            preparedStatement.setString(1, id);

            preparedStatement.executeUpdate();
            System.out.println("Deleted user with id: " + id);
        }
    }
}
