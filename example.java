package org.demo;

import java.sql.Statement;

public class Vulnerable {
    public void test(String userInput, Statement stmt) throws Exception {
        stmt.executeQuery(
            "SELECT * FROM users WHERE username='" + userInput + "'"
        );
    }
}
