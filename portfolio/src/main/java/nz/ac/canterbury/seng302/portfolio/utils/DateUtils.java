package nz.ac.canterbury.seng302.portfolio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    // Private constructor to hide the implicit public one
    private DateUtils() {}

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    // Using a singleton pattern, because SonarLint doesn't like it when a static
    // utility class get initialized, but `DatesExpressionDialect` NEEDS an instance.
    private static DateUtils singleton = null;

    public static DateUtils getInstance() {
        if (singleton == null)
            singleton = new DateUtils();
        return singleton;
    }

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
     * @param date Date object to be converted to String
     * @return String object
     */
    public static String toString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    /**
     * Converts a Date Time object to a String with yyyy-MM-dd HH:mm format.
     * @param date Date Time object to be converted to String
     * @return String object
     */
    public static String toDateTimeString(Date date) { return new SimpleDateFormat(DATETIME_FORMAT).format(date); }

    /**
     * Converts a String to a Date in yyyy-MM-dd HH:mm format.
     * @param dateTime String to be converted to Date
     * @return Date object
     */
    public static Date toDateTime(String dateTime) {
        try {
            return new SimpleDateFormat(DATETIME_FORMAT).parse(dateTime);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing date: %s", e.getMessage()));
        }
        return null;
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

    // https://stackoverflow.com/questions/22929237
    /**
     * Converts a LocalDate object to a Date object.
     * @param localDate The LocalDate to convert
     * @return The converted Date object
     */
    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
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

    /**
     * Takes the start and ends of two time periods and checks whether they overlap
     * Returns true if the times overlap, including if the start of one period equals the end of the other
     * @param startA the start time of the first time period
     * @param endA the end time of the first time period
     * @param startB the start time of the second time period
     * @param endB the end time of the second time period
     * @return true if the time periods overlap
     */
    public static boolean timesOverlapIncl(Date startA, Date endA, Date startB, Date endB){
        if (startB.before(startA)){
            return !startA.after(endB);
        }
        return !startB.after(endA);
    }

    /**
     * Takes the start and ends of two time periods and checks whether they overlap
     * Returns true if the times overlap, excluding if the start of one period equals the end of the other
     * @param startA the start time of the first time period
     * @param endA the end time of the first time period
     * @param startB the start time of the second time period
     * @param endB the end time of the second time period
     * @return true if the time periods overlap
     */
    public static boolean timesOverlapExcl(Date startA, Date endA, Date startB, Date endB){
        if (startB.before(startA)){
            return endB.after(startA);
        }
        return startB.before(endA);
    }
}
