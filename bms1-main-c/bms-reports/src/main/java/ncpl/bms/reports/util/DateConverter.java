package ncpl.bms.reports.util;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateConverter {

    public String convertLongToDate(String dateLong) throws ParseException {


        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date inputDate = inputDateFormat.parse(dateLong);
        // Format the Date object into a string with the desired format
        String outputDateString = outputDateFormat.format(inputDate);
        Date timestamp = new Date(Long.parseLong(outputDateString));
        DateFormat date = new SimpleDateFormat("dd:MM:yyyy:HH:mm:ss");
        return date.format(timestamp);
    }

//    public Long stringToLong(String dateString) throws ParseException {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//        // Parse the date string into a Date object
//        Date date = dateFormat.parse(dateString);
//        // Get the time in milliseconds
//        long milliseconds = date.getTime();
//        System.out.println("Date in milliseconds: " + milliseconds);
//        return milliseconds;
//    }
public Long stringToLong(String dateString) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    // Parse the date string into a Date object
    Date date = dateFormat.parse(dateString);
    // Get the time in milliseconds
    long milliseconds = date.getTime();
    System.out.println("Date in milliseconds: " + milliseconds);
    return milliseconds;
}
}
