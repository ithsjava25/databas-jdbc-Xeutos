package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

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

//        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        //Todo: Starting point for your code

        Scanner scanner = new Scanner(System.in);
        boolean verifiedUser = validateUser(jdbcUrl, dbUser, dbPass, scanner);

        System.out.println("""
                test
                """);


        while (verifiedUser) {
            String input = scanner.nextLine();

            switch (input) {
                    case "1" -> missions(jdbcUrl, dbUser, dbPass);
                    case "2" -> missionById(jdbcUrl, dbUser, dbPass, scanner);
                    case "3" -> missionCount(jdbcUrl, dbUser, dbPass, scanner);
                    case "4" -> createAccount(jdbcUrl, dbUser, dbPass, scanner);
                    case "5" -> updateAccountPassword(jdbcUrl, dbUser, dbPass, scanner);
                    case "6" -> deleteAccount(jdbcUrl, dbUser, dbPass, scanner);
                    case "0" -> verifiedUser =  false;
                    default -> System.out.println("Invalid output");
                }
        }
    }



    private static boolean validateUser(String jdbcUrl, String dbUser, String dbPass, Scanner scanner) {



        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            String query = "select name, password from account where name = ?";

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                System.out.println("Enter username: ");
                String username = scanner.nextLine();
                System.out.println("Enter password: ");
                String password = scanner.nextLine();

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Invalid username or password");
        return false;
    }

    private void missions(String url, String user, String pass) {
        String query = "select spacecraft from moon_mission";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet result = preparedStatement.executeQuery();
                while (result.next()){
                    System.out.println(result.getString(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void missionById(String url, String user, String pass, Scanner scanner) {
        System.out.println("Input id: ");
        String id = scanner.nextLine().trim();
        String query = "select * from moon_mission where mission_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1,id);

                ResultSet result = preparedStatement.executeQuery();
                while (result.next()){
                    System.out.println("Details for mission id: " + result.getString(1)
                            + ", Spacecraft: " + result.getString(2)
                            + ", Launch date:" + result.getString(3)
                            + ", Carrier rocket: " + result.getString(4)
                            + ", Operator: " + result.getString(5)
                            + ", Mission type:" + result.getString(6)
                            + ", Outcome: " + result.getString(7));
                }
                result.close();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void missionCount(String url, String user, String pass, Scanner scanner) {
        System.out.println("Enter a year");
        String launch_date = scanner.nextLine().trim();
        String query = "select count(*) from moon_mission where Year(launch_date) = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, launch_date);
                ResultSet result = preparedStatement.executeQuery();

                System.out.println(result.getString(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAccount(String url, String user, String pass, Scanner scanner) {
        System.out.println("Enter first name: ");
        String first_name = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter last name: ");
        String last_name = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter ssn: ");
        String ssn = scanner.nextLine().trim();
        System.out.println("Enter password: ");
        String password = scanner.nextLine().trim().toLowerCase();

        String insert = "insert into account (first_name, last_name, ssn, password) values (?, ?, ?, ?);";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try(PreparedStatement preparedStatement = connection.prepareStatement(insert)) {
                preparedStatement.setString(1, first_name);
                preparedStatement.setString(2, last_name);
                preparedStatement.setString(3, ssn);
                preparedStatement.setString(4, password);

                preparedStatement.executeUpdate();
                System.out.println("Account created.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateAccountPassword(String url, String user, String pass, Scanner scanner) {
        System.out.println("Enter id: ");
        String id = scanner.nextLine().trim();
        System.out.println("Enter new password: ");
        String newPassword = scanner.nextLine().trim().toLowerCase();

        String update = "update account set password = ? where user_id = ?";

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

    private void deleteAccount(String url, String user, String pass, Scanner scanner) {
        System.out.println("Enter id: ");
        String id = scanner.nextLine().trim();

        String delete = "delete from account where user_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, pass)) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(delete)) {
                preparedStatement.setString(1, id);

                preparedStatement.executeUpdate();
                System.out.println("Deleted user with id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


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