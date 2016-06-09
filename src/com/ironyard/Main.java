package com.ironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Sneaker> sneakers = new ArrayList<>();

    public static void main(String[] args) {
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
                        m.put("sneaker", sneakers);
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

                    User user = users.get(username);
                    if (user == null) {
                        user = new User(username, password);
                        users.put(username, user);
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
                    Sneaker sneaker = new Sneaker(sneakers.size(), brand, name, year, price, size, username);
                    sneakers.add(sneaker);
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
                    Sneaker s = sneakers.get(id);
                    if (!s.username.equals(username)) {
                        throw new Exception("You can't delete this!");
                    }

                    String brand = request.queryParams("brand");
                    String name = request.queryParams("name");
                    int year = Integer.valueOf(request.queryParams("year"));
                    float price = Float.valueOf(request.queryParams("price"));
                    int size = Integer.valueOf(request.queryParams("size"));

                    Sneaker editMessage = sneakers.get(id);
                    editMessage.brand = brand;
                    editMessage.name = name;
                    editMessage.year = year;
                    editMessage.price = price;
                    editMessage.size = size;


                    //RESET IDS
                    int index = 0;
                    for (Sneaker msg : sneakers) {
                        msg.id = index;
                        index++;
                    }
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
                    Sneaker m = sneakers.get(id);
                    if (!m.username.equals(username)) {
                        throw new Exception("You can't delete this!");
                    }
                    sneakers.remove(id);

                    //RESET IDS
                    int index = 0;
                    for (Sneaker msg : sneakers) {
                        msg.id = index;
                        index++;
                    }
                    response.redirect("/");
                    return "";
                }
        );
    }
}
