package com.geekbrains.cloud.server;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class AuthService {
    private static Connection connection;
    private static Statement statement;

    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:clientsdb.db");
            statement = connection.createStatement();
            log.error("Соединение с базой данных установлено");
        } catch (SQLException ex) {
            ex.printStackTrace();
            log.error("Ошибка соединения с БД");
        }
    }

    public String getNickByLoginAndPass(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(
                "select nickname from clients where login = ? and password = ?")) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet resSet = ps.executeQuery();
            if (resSet.next()) {
                return resSet.getString("nickname");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void addNewClient(String login, String password, String nickname) {
        try (PreparedStatement ps =
                     connection.prepareStatement("insert into clients (login, password, nickname) values (? ,? ,?)")) {
            ps.setString(1, login);
            ps.setString(2, password);
            ps.setString(3, nickname);
            ps.addBatch();
            ps.executeBatch();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isNicknameUsed(String nickname) {
        try (PreparedStatement ps = connection.prepareStatement("Select * From users Where login = ?")) {
            ps.setString(3, nickname);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Ошибка при работе с таблицей users в БД. Поиск пользователя не удался");
            return true;
        }
    }

    public void stop() {
//        log.debug("Соединение с БД разорвано");
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
