package de.twerner.jdbcclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static java.lang.String.format;

public class JdbcClient {
    private final Properties config;
    private Connection connection;
    private Statement statement;

    public JdbcClient() {
        try {
            config = new Properties();
            final InputStream configStream = new FileInputStream("jdbcclient.properties");
            config.load(configStream);
        } catch (IOException e) {
            throw new RuntimeException("failed to open file jdbcclient.properties", e);
        }
    }

    private void openConnection() {
        try {
            connection = DriverManager.getConnection(config.getProperty("db.url"), config.getProperty("db.username"),
                    config.getProperty("db.password"));
        } catch (SQLException e) {
            throw new RuntimeException("failed to open database connection", e);
        }
    }

    private void createStatement() {
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException("failed to create statement", e);
        }
    }

    private int executeStatement(String sql) {
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("failed to execute statement", e);
        }
    }

    public void execute() {
        try {
            Class.forName(config.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            final String message = format("failed to load class %s", config.getProperty("db.driver"));
            throw new RuntimeException(message, e);
        }
        openConnection();
        createStatement();
        final String sql = "declare\n" +
                "  cursor c is select id from temp_node order by id asc;\n" +
                "begin\n" +
                "  for row in c loop\n" +
                "    delete from ALF_NODE_PROPERTIES anp where anp.NODE_ID = row.id;\n" +
                "    delete from alf_node an where an.id = row.id;\n" +
                "    delete from temp_node tn where tn.id = row.id;\n" +
                "    commit;\n" +
                "  end loop;\n" +
                "end;";
        System.out.println("statement will be executed now");
        final int rows = executeStatement(sql);
        System.out.printf("number of rows: %d\n", rows);
    }

    public static void main(String[] args) {
        new JdbcClient().execute();
    }
}
