package com.ironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by illladell on 6/14/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testEntry() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertEntry(conn, "Nike SB", "De La High", 2005, 150, 9, 1);
        Sneaker sneaker = Main.selectEntry(conn, 1);
        conn.close();
        assertTrue(sneaker != null);
        assertTrue(sneaker.username.equals("Alice"));
    }

    @Test
    public void testReplies() throws SQLException {
        Connection conn = startConnection();

        Main.insertUser(conn, "Alice", "");
        Main.insertUser(conn, "Bob", "");

        User alice = Main.selectUser(conn, "Alice");

        Main.insertEntry(conn, "Nike", "CB34", 2016, 150, 9, alice.id);
        Main.insertEntry(conn, "Saucony", "Burger", 2013, 160, 13, alice.id);
        Main.insertEntry(conn, "Reebok", "Purple Haze", 2016, 130, 11, alice.id);

        ArrayList<Sneaker> sneakers = Main.selectEntries(conn);
        conn.close();
        assertTrue(sneakers.size() == 3);

    }

    @Test
    public void testUpdateEntry() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertEntry(conn, "Nike SB", "De La High", 2005, 150, 9, 1);
        Main.updateEntry(conn, "Asics", "Neptunes", 2012, 225, 9, 1);
        Sneaker sneaker = Main.selectEntry(conn, 1);
        conn.close();
        assertTrue(sneaker.brand.equals("Asics"));
        assertTrue(sneaker.year == 2012);
    }

    @Test
    public void testDeleteSneaker() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertEntry(conn, "Nike", "CB34", 2016, 150, 9, 1);
        Main.deleteEntry(conn, 1);
        Sneaker sneaker = Main.selectEntry(conn, 1);
        conn.close();
        assertTrue(sneaker == null);
    }


}