import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class TimeHolder {
    public static final ZoneId zoneId = ZoneId.of("Europe/Moscow");

    public final Timestamp t;
    public final LocalDateTime l;

    public final char source;

    public static TimeHolder of(long ms) {
        var t = new Timestamp(ms);
        LocalDateTime l = t.toInstant().atZone(zoneId).toLocalDateTime();
        return new TimeHolder(t, l, 't');
    }

    public static TimeHolder of(LocalDateTime l) {
        var t = Timestamp.from(l.atZone(zoneId).toInstant());
        log.debug("{} -> {} ({})", l, t, t.getTime());
        return new TimeHolder(t, l, 'l');
    }

    public static TimeHolder of(String s) {
        var l = LocalDateTime.parse(s);
        return TimeHolder.of(l);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", l, source);
    }
}
