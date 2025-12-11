package com.example;

import java.sql.*;
import java.util.Arrays;

public class Main {

     static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code

        boolean verifiedUser = validateUser(jdbcUrl, dbUser, dbPass);

        String input = IO.readln();

        switch (input) {
            case "1" -> missions(jdbcUrl, dbUser, dbPass);
            default -> System.out.println("Invalid input");
        }


//               while (running) {
//            String input = IO.readln("""
//                    Pick an option from bellow:
//                    1) List moon missions
//                    2) Moon Mission by id
//                    3) Mission count by select year
//                    4) Create an account
//                    5) Update an account
//                    6) Delete an account
//                    0) Exit
//                    """).trim();
//
//            switch (input) {
//                case "1" -> missions(jdbcUrl, dbUser, dbPass);
//                case "2" -> missionById(jdbcUrl, dbUser, dbPass);
//                case "3" -> missionCount(jdbcUrl, dbUser, dbPass);
//                case "4" -> createAccount(jdbcUrl, dbUser, dbPass);
//                case "5" -> updateAccountPassword(jdbcUrl, dbUser, dbPass);
//                case "6" -> deleteAccount(jdbcUrl, dbUser, dbPass);
//                case "0" -> running =  false;
//                default -> System.out.println("Invalid output");
//            }
        }
    }

    private static boolean validateUser(String url, String user, String pass) {
        String username = IO.readln();
        String password = IO.readln();

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {
            String query = "select name, password from account where name = ?";

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                ResultSet result = preparedStatement.executeQuery();
                result.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void missions(String url, String user, String pass) {
        String query = "select names from moon_mission";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet result = preparedStatement.executeQuery();
                System.out.println(result);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void missionById(String url, String user, String pass) {
        String id = IO.readln("Input id: ").trim();
        String query = "select * from moon_mission where id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1,id);

                ResultSet result = preparedStatement.executeQuery();
                while (result.next()){
                    System.out.println(result.getString(2) + " " + result.getString(3));
                }
                result.close();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void missionCount(String url, String user, String pass) {
        String year = IO.readln("Enter a year").trim();
        String query = "select count(*) from moon_mission where year = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, year);

                System.out.println(preparedStatement.executeQuery());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAccount(String url, String user, String pass) {
        String first_name = IO.readln("Enter a first name: ").trim().toLowerCase();
        String last_name = IO.readln("Enter a last name: ").trim().toLowerCase();
        String ssn = IO.readln("Enter a ssn: ").trim();
        String password = IO.readln("Enter a password: ").trim().toLowerCase();

        String insert = "insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?);";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try(PreparedStatement preparedStatement = connection.prepareStatement(insert)) {
                preparedStatement.setString(1, first_name);
                preparedStatement.setString(2, last_name);
                preparedStatement.setString(3, ssn);
                preparedStatement.setString(4, password);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Account created.");
    }

    private void updateAccountPassword(String url, String user, String pass) {
        String id = IO.readln("Enter id: ").trim();
        String newPassword = IO.readln("Enter new password: ").trim().toLowerCase();

        String update = "update account set password = ? where id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try(PreparedStatement preparedStatement = connection.prepareStatement(update)) {
                preparedStatement.setString(1, newPassword);
                preparedStatement.setString(2, id);

                preparedStatement.executeUpdate();
                System.out.println("Password updated for id " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



    }

    private void deleteAccount(String url, String user, String pass) {
        String id = IO.readln("Enter id: ").trim();

        String delete = "delete from account where id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(delete)) {
                preparedStatement.setString(1, id);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Deleted user with id: " + id);
    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}