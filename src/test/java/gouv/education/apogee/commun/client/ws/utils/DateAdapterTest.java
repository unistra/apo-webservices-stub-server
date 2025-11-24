package gouv.education.apogee.commun.client.ws.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateAdapterTest {

    public static final TimeZone restoreTimeZone = TimeZone.getDefault();

    public static final String ZONE_OFFSET =
            ZoneId.systemDefault()
                    .getRules()
                    .getOffset(Instant.EPOCH)
                    .toString();

    @BeforeAll static void setTimeZoneToParis() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
    }
    @AfterAll static void restoreTimeZone() {
        TimeZone.setDefault(restoreTimeZone);
    }

    @Test
    void printDate() {
        assertEquals(
                "2021-01-02T00:00:00"+ZONE_OFFSET,
                DateAdapter.printDate(LocalDate.of(2021, 1, 2))
        );
    }

    @Test
    void printDateTime() {
        assertEquals(
                "2021-01-02T03:04:05"+ZONE_OFFSET,
                DateAdapter.printDateTime(LocalDateTime.of(2021, 1, 2, 3, 4, 5))
        );
    }

    @Test
    void parseDate() {
        assertEquals(
                LocalDate.of(2021, 1, 2),
                DateAdapter.parseDate("2021-01-02T03:04:05"+ZONE_OFFSET)
        );
    }

    @Test
    void parseDateTime() {
        assertEquals(
                LocalDateTime.of(2021, 1, 2, 3, 4, 5),
                DateAdapter.parseDateTime("2021-01-02T03:04:05"+ZONE_OFFSET));
    }

}