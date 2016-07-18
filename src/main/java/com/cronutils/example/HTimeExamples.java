package com.cronutils.example;

import com.cronutils.htime.HDateTimeFormatBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HTimeExamples {
    public static void main(String[] args) {
        //You no longer need to remember "YYYY-MM-dd KK a" patterns.
        //we can create a formatter for JodaTime
        DateTimeFormatter jodatimeFormatter = HDateTimeFormatBuilder.getInstance().forJodaTime().getFormatter(Locale.US).forPattern("June 9, 2011");
        System.out.println(jodatimeFormatter.print(DateTime.now()));//formattedDateTime will be lastExecution in "dayOfWeek, Month day, Year" format

        //or we can create a formatter for JDK time
        SimpleDateFormat jdkFormatter = HDateTimeFormatBuilder.getInstance().forJDK12().getFormatter(Locale.US).forPattern("June 9, 2011");
        System.out.println(jdkFormatter.format(new Date()));//formattedDateTime will be lastExecution in "dayOfWeek, Month day, Year" format
    }
}
