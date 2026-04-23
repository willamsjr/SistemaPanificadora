package util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DateUtils {

    public static Timestamp toTimestamp(LocalDateTime data) {
        return (data != null) ? Timestamp.valueOf(data) : null;
    }
}
