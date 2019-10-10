package com.example.weatherapp.Common;

import android.location.Location;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    public static final String APP_ID = "c49eb71ced2c65ac8bc1d6349198ac39";
    public static Location current_location = null;

    public static String convertUnixToDate(long dt) {

        Date date = new Date(dt*1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd EEE MM yyyy");
        String formmated = simpleDateFormat.format(date);
        return formmated;
    }

    public static String convertUnixToHour(long dt) {

        Date date = new Date(dt*1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        String formmated = simpleDateFormat.format(date);
        return formmated;
    }

    public static double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}
