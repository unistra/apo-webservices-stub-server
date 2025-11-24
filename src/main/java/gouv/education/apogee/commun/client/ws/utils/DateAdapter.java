package gouv.education.apogee.commun.client.ws.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import jakarta.xml.bind.DatatypeConverter;

public class DateAdapter {
    private DateAdapter() { /*_*/ }

    public static String printDate(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
        return DatatypeConverter.printDateTime(calendar);
    }

    public static String printDateTime(LocalDateTime localDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(localDateTime.getYear(), localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth(), localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        return DatatypeConverter.printDateTime(calendar);
    }

    public static LocalDate parseDate(String xmlDate) {
        return DatatypeConverter.parseDate(xmlDate).getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime parseDateTime(String xmlDateTime) {
        return DatatypeConverter.parseDate(xmlDateTime).getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
