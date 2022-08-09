package nz.ac.canterbury.seng302.portfolio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Used by Project and Sprint to convert dates to yyyy-MM-dd format and vice versa
 * that is required by the date type input field.
 */
@Component
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    // Using a singleton pattern, because SonarLint doesn't like it when a static
    // utility class get initialized, but `DatesExpressionDialect` NEEDS an instance.
    private static DateUtils singleton = null;

    public static DateUtils getInstance() {
        if (singleton == null)
            singleton = new DateUtils();
        return singleton;
    }

    private DateUtils() {}

    /**
     * Converts a Date object to a String with dd/MMM/yyyy format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public static String toDisplayString(Date date) {
        return new SimpleDateFormat(DISPLAY_DATE_FORMAT).format(date);
    }

    /**
     * Converts a Date object to a String with dd/MMM/yyyy HH:mm:ss format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public static String toDisplayDateTimeString(Date date) {
        return new SimpleDateFormat(DISPLAY_DATETIME_FORMAT).format(date);
    }

    /**
     * Converts a String to a Date in yyyy-MM-dd format.
     * @param date String to be converted to Date
     * @return Date object
     */
    public static Date toDate(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing date: %s", e.getMessage()));
        }
        return null;
    }

    /**
     * Converts a Date object to a String with yyyy-MM-dd format.
     * @param date String to be converted to Date
     * @return Date object
     */
    public static String toString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    /**
     * Checks whether the first date given is the day after the second date given
     */
    public static boolean isDayAfter(Date dayAfter, Date dayBefore) {
        Calendar c = Calendar.getInstance();
        c.setTime(dayBefore);
        c.add(Calendar.DATE, 1);    // Add n days to the date
        return c.getTime().equals(dayAfter);
    }

    // https://stackoverflow.com/a/23885950
    /**
     * <p>Combines a LocalDate and a LocalTime into a Date object, normalized by the given's timezone</p>
     * Note: To get a user's timezone, add a <code>TimeZone</code> argument in the Controller method
     * @param date The date specified
     * @param time The time of day specified
     * @param usersTimezone The timezone this occurs in
     * @return A Date object of the date + time, according to the timezone
     */
    public static Date localDateAndTimeToDate(LocalDate date, LocalTime time, TimeZone usersTimezone) {
        LocalDateTime datetime = LocalDateTime.of(date, time);
        return Date.from(datetime.atZone(usersTimezone.toZoneId()).toInstant());
    }
}
