package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MoonMissionRepository {

    private Connection connection;

    public MoonMissionRepository(Connection connection) {
        this.connection = connection;
    }

    public void getMissions() throws SQLException {
        String query = "select spacecraft from moon_mission";

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()){
                System.out.println(result.getString(1));
            }
        }
    }

    public void getMissionById(String id) throws SQLException {
        String query = "select * from moon_mission where mission_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,id);

            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    System.out.println("Details for mission id: " + result.getString(1)
                            + ", Spacecraft: " + result.getString(2)
                            + ", Launch date:" + result.getString(3)
                            + ", Carrier rocket: " + result.getString(4)
                            + ", Operator: " + result.getString(5)
                            + ", Mission type:" + result.getString(6)
                            + ", Outcome: " + result.getString(7));
                }
            }
        }
    }

    public void getMissionCountByYear(int launch_date) throws SQLException {
        String query = "select count(*) from moon_mission where Year(launch_date) = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, launch_date);
            try(ResultSet result = preparedStatement.executeQuery()) {
                if(result.next()) {
                    System.out.println("there were " + result.getString(1) + " missions year " + launch_date);
                }
            }
        }
    }
}
