package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtils {

    public String toString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public Date toDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
}
