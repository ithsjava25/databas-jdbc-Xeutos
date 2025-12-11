package com.example;

import java.sql.*;
import java.util.Arrays;
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

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            AccountRepository account = new AccountRepository(connection);
            MoonMissionRepository moonMission = new MoonMissionRepository(connection);
            Scanner scanner = new Scanner(System.in);

            boolean verifiedUser = validateUser(account, scanner);

            System.out.println("""
                1) List moon missions
                2) Mission by mission id
                3) Mission count for a given year
                4) Create account
                5) Update account password
                6) Delete account
                0) Exit
                """);


            while (verifiedUser) {
                String input = scanner.nextLine();

                switch (input) {
                    case "1" -> moonMission.getMissions();
                    case "2" -> missionById(moonMission, scanner);
                    case "3" -> missionCount(moonMission, scanner);
                    case "4" -> createAccount(account, scanner);
                    case "5" -> updateAccountPassword(account, scanner);
                    case "6" -> deleteAccount(account, scanner);
                    case "0" -> verifiedUser =  false;
                    default -> System.out.println("Invalid output");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateUser(AccountRepository account, Scanner scanner) throws SQLException {

         System.out.println("Enter username: ");
         String username = scanner.nextLine().trim();
         System.out.println("Enter password: ");
         String password = scanner.nextLine().trim();

         return account.validateUser(username, password);
     }

    private void missionById(MoonMissionRepository missionRepository, Scanner scanner) throws SQLException {
        System.out.println("Input id: ");
        String id = scanner.nextLine().trim();

        missionRepository.getMissionById(id);

    }

    private void missionCount(MoonMissionRepository missionRepository, Scanner scanner) throws SQLException {
        System.out.println("Enter a year");
        int launch_date;
        try {
            launch_date = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid year format");
            return;
        }

        missionRepository.getMissionCountByYear(launch_date);
    }

    private void createAccount(AccountRepository account, Scanner scanner) throws SQLException {
        System.out.println("Enter first name: ");
        String first_name = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter last name: ");
        String last_name = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter ssn: ");
        String ssn = scanner.nextLine().trim();
        System.out.println("Enter password: ");
        String password = scanner.nextLine().trim();

        account.createAccount(first_name, last_name, ssn, password);
    }

    private void updateAccountPassword(AccountRepository account, Scanner scanner) throws SQLException {
        System.out.println("Enter id: ");
        String id = scanner.nextLine().trim();
        System.out.println("Enter new password: ");
        String newPassword = scanner.nextLine().trim();

        account.updatePassword(newPassword, id);
    }

    private void deleteAccount(AccountRepository account, Scanner scanner) throws SQLException {
        System.out.println("Enter id: ");
        String id = scanner.nextLine().trim();

        account.deleteAccount(id);
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