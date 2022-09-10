import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;

@Slf4j
public class TimestampTest {
    private Connection connection;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
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

    private static Timestamp parse(String time) {
        return Timestamp.from(LocalDateTime.parse(time).atZone(ZoneId.of("Europe/Moscow")).toInstant());
    }

    @SneakyThrows
    private void fillDb() {
        Timestamp[] examples = new Timestamp[]{
                new Timestamp(0),
                parse("2017-03-25T02:30:00")
        };

        connection.prepareStatement("delete from ts").executeUpdate();
        log.info("DB is cleaned up");

        PreparedStatement insert = connection.prepareStatement("insert into ts (t, tz, s) values (?, ?, ?)");
        for (var ts : examples) {
            insert.setTimestamp(1, ts);
            insert.setTimestamp(2, ts);
            insert.setString(3, ts.toString());
            insert.executeUpdate();
            log.info("Inserted {}", ts);
        }
    }

    @SneakyThrows
    private void checkDb() {
        PreparedStatement select = connection.prepareStatement("select id, t, tz, s from ts order by id");
        ResultSet resultSet = select.executeQuery();
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            Timestamp t = resultSet.getTimestamp("t");
            Timestamp tz = resultSet.getTimestamp("tz");
            String s = resultSet.getString("s");

            log.info("t == tz: {}; id = {}, t = {}, tz = {}, s = {}", t.equals(tz), id, t, tz, s);
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
