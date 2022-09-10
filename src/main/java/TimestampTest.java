import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.function.Consumer;
import java.util.logging.LogManager;

@Slf4j
public class TimestampTest {
    private Connection connection;

    static {
        try {
            LogManager.getLogManager().readConfiguration(
                    TimestampTest.class.getResourceAsStream("/logging.properties"));
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        var t = new TimestampTest();
        t.doConnection(t::doTest);
    }

    private void doTest(Connection connection) {
        fillDb();
        log.info("Check start");
        checkDb();
    }

    @SneakyThrows
    private void fillDb() {
        TimeHolder[] examples = new TimeHolder[]{
                TimeHolder.of(0),
                TimeHolder.of(954027000000L),
                TimeHolder.of("2000-03-26T02:30:00"),
                TimeHolder.of(954027000000L + 3600000),
                TimeHolder.of("2000-03-26T03:30:00"),
                TimeHolder.of(972772200000L),
                TimeHolder.of("2000-10-29T02:30:00"),
                TimeHolder.of(972772200000L + 3600000),
                TimeHolder.of("2000-10-29T03:30:00"),
        };

        connection.prepareStatement("delete from ts").executeUpdate();
        log.info("DB is cleaned up");

        PreparedStatement insert = connection.prepareStatement("insert into ts (s, t, tz, l, lz) values (?, ?, ?, ?, ?)");
        for (var ts : examples) {
            insert.setString(1, ts.toString());
            insert.setTimestamp(2, ts.t);
            insert.setTimestamp(3, ts.t);
            insert.setObject(4, ts.l);
            insert.setObject(5, ts.l);
            insert.executeUpdate();
            log.info("Inserted {}", ts);
        }
    }

    @SneakyThrows
    private void checkDb() {
        PreparedStatement select = connection.prepareStatement("select id, s, t, tz, l, lz from ts order by id");
        ResultSet resultSet = select.executeQuery();
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            Timestamp t = resultSet.getTimestamp("t");
            Timestamp tz = resultSet.getTimestamp("tz");

            Timestamp l = resultSet.getTimestamp("l");
            Timestamp lz = resultSet.getTimestamp("lz");
            String s = resultSet.getString("s");

            log.info("s = {}: t == tz: {}; l == lz: {}; id = {}, t = {}, tz = {}, l = {}, lz = {}",
                    s, t.equals(tz), l.equals(lz), id, t, tz, l, lz);
        }
        resultSet.close();
    }

    private void doConnection(Consumer<Connection> func) throws SQLException {
//        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test", "postgres", "postgres");
            connection.setAutoCommit(true);
            log.info("Connected to the PostgreSQL server successfully.");

            func.accept(connection);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
