package it.connectpa.odatapushservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtil {

    private static final List<SimpleDateFormat> DATEFORMATS = new ArrayList<SimpleDateFormat>() {

        private static final long serialVersionUID = 5636572627689425575L;

        {
            add(new SimpleDateFormat("M/dd/yyyy"));
            add(new SimpleDateFormat("dd/M/yyyy"));
            add(new SimpleDateFormat("dd.M.yyyy"));
            add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("dd.M.yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("dd.MMM.yyyy"));
            add(new SimpleDateFormat("dd-MMM-yyyy"));
        }
    };

    public static Date convertToDate(String input) {
        Date date = null;
        if (null == input) {
            return null;
        }
        for (SimpleDateFormat format : DATEFORMATS) {
            try {
                format.setLenient(false);
                date = format.parse(input);
            } catch (ParseException e) {
                //try other formats
            }
            if (date != null) {
                break;
            }
        }

        return date;
    }
}
