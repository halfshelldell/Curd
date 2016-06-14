package com.ironyard;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {


    public static void createTables(Connection conn) throws SQLException {
        Statement stmt  = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR) ");
        stmt.execute("CREATE TABLE IF NOT EXISTS sneakers (id IDENTITY, brand VARCHAR, name VARCHAR, year INT, price FLOAT, size INT, user_id INT)");
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }

    public static void insertEntry(Connection conn, String brand, String name, int year, float price, int size, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO sneakers VALUES (NULL, ? ,?, ?, ?, ?, ?)");
        stmt.setString(1, brand);
        stmt.setString(2, name);
        stmt.setInt(3, year);
        stmt.setFloat(4, price);
        stmt.setInt(5, size);
        stmt.setInt(6, userId);
        stmt.execute();
    }

    public static Sneaker selectEntry(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM sneakers INNER JOIN users ON sneakers.user_id = users.id WHERE sneakers.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String brand = results.getString("sneakers.brand");
            String name = results.getString("sneakers.name");
            int year = results.getInt("sneakers.year");
            Float price = results.getFloat("sneakers.price");
            int size = results.getInt("sneakers.size");
            String username = results.getString("users.name");
            return new Sneaker(id, brand, name, year, price, size, username);
        }
        return null;
    }

    public static ArrayList<Sneaker> selectEntries(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM sneakers INNER JOIN users ON sneakers.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        ArrayList<Sneaker> sneakers = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String brand = results.getString("sneakers.brand");
            String name = results.getString("sneakers.name");
            int year = results.getInt("sneakers.year");
            Float price = results.getFloat("sneakers.price");
            int size = results.getInt("sneakers.size");
            String username = results.getString("users.name");
            Sneaker sneaker = new Sneaker(id, brand, name, year, price, size, username);
            sneakers.add(sneaker);
        }
        return sneakers;
    }

    public static void updateEntry(Connection conn, String brand, String name, int year, float price, int size, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE sneakers SET brand = ?, name = ?, year = ?, price = ?, size = ? WHERE id = ?");
        stmt.setString(1, brand);
        stmt.setString(2, name);
        stmt.setInt(3, year);
        stmt.setFloat(4, price);
        stmt.setInt(5, size);
        stmt.setInt(6, id);
        stmt.execute();
    }

    public static void deleteEntry(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM sneakers WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }


    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);


        Spark.staticFileLocation("public");
        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();


                    if (username == null) {
                        return new ModelAndView(m, "index.html");
                    }
                    else {

                        m.put("name", username);
                        m.put("sneaker", selectEntries(conn));
                        return new ModelAndView(m, "sneaker.html");
                    }
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                (request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");

                    if (username == null || password == null) {
                        throw new Exception("Name or password not sent");
                    }

                    User user = selectUser(conn, username);
                    if (user == null) {
                        insertUser(conn, username, password);
                    }
                    else if (!password.equals(user.password)) {
                        throw new Exception("Wrong password");
                    }
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-sneaker",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }

                    String brand = request.queryParams("name");
                    String name = request.queryParams("brand");
                    int year = Integer.valueOf(request.queryParams("year"));
                    float price = Float.valueOf(request.queryParams("price"));
                    int size = Integer.valueOf(request.queryParams("size"));
                    /*Sneaker sneaker = new Sneaker(sneakers.size(), brand, name, year, price, size, username);
                    sneakers.add(sneaker);*/
                    User user = selectUser(conn, username);
                    insertEntry(conn, brand, name, year, price, size, user.id);
                    response.redirect("/");
                    return "";

                }
        );
        Spark.post(
                "/edit-message",
                (request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));

                    Session session = request.session();
                    String username = session.attribute("username");
                    User user = selectUser(conn, username);
                    Sneaker s = selectEntry(conn, user.id);;
                    if (!s.username.equals(username)) {
                        throw new Exception("You can't delete this!");
                    }

                    String brand = request.queryParams("brand");
                    String name = request.queryParams("name");
                    int year = Integer.valueOf(request.queryParams("year"));
                    float price = Float.valueOf(request.queryParams("price"));
                    int size = Integer.valueOf(request.queryParams("size"));

                    updateEntry(conn, brand, name, year, price,size, id);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.get(
                "/edit",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("You can't edit this post");
                    }
                    int id = Integer.valueOf(request.queryParams("id"));

                    HashMap m = new HashMap();
                    m.put("id", id);

                    return new ModelAndView(m, "edit.html");
                },
            new MustacheTemplateEngine()
        );
        Spark.post(
                "/delete-message",
                (request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));

                    Session session = request.session();
                    String username = session.attribute("username");
                    Sneaker m = selectEntry(conn, id);
                    if (!m.username.equals(username)) {
                        throw new Exception("You can't delete this!");
                    }
                    deleteEntry(conn, id);

                    response.redirect("/");
                    return "";
                }
        );
    }
}
