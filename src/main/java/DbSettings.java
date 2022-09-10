import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class DbSettings {
    public static final String url;
    public static final String user;
    public static final String password;

    static {
        var defaultProperties = new Properties();

        try {
            defaultProperties.load(DbSettings.class.getResourceAsStream("/db.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var systemProperties = System.getProperties();

        url = getProperty(defaultProperties, systemProperties, "timestamp-test.url");
        user = getProperty(defaultProperties, systemProperties, "timestamp-test.user");
        password = getProperty(defaultProperties, systemProperties, "timestamp-test.password");

        log.debug("Url = {}; User = {}", url, user);
    }

    private static String getProperty(Properties defaultProperties, Properties systemProperties, String name) {
        return systemProperties.getProperty(name, defaultProperties.getProperty(name));
    }
}
