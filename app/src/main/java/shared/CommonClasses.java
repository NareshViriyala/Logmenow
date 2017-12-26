package shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import database.DBHelper;

/**
 * Created by Home on 7/5/2016.
 */
public class CommonClasses {
    private Context context;
    private DBHelper mydb;
    private String PageName = "CommonClasses";
    public CommonClasses(Context context){
        this.context = context;
        mydb = new DBHelper(context);
    }

    public long StringToMilli(String inTime){
        long outTime = 0;
        try {
            if(inTime.equalsIgnoreCase("Time Up"))
                return 0;
            int hr = inTime.indexOf(":");
            String hour = inTime.substring(0, hr);
            String min = inTime.substring(hr+1, hr+3);
            String sec = inTime.substring(hr+4, hr+6);

            int h = Integer.parseInt(hour);
            int m = Integer.parseInt(min);
            int s = Integer.parseInt(sec);

            long lH = h * 60 * 60 * 1000;
            long lM = m * 60 * 1000;
            long lS = s * 1000;

            outTime = lH + lM + lS;

            outTime = outTime<=0?0:outTime;
        }
        catch (Exception e){mydb.logAppError(PageName, "StringToMilli", "Exception", e.getMessage());}
        return outTime;
    }

    public String MilliToString(long inTime){
        //inTime = 3023000;
        String outTime = "";
        try{
            long hrs = TimeUnit.MILLISECONDS.toHours(inTime);
            long min = TimeUnit.MILLISECONDS.toMinutes(inTime)%60;
            long sec = TimeUnit.MILLISECONDS.toSeconds(inTime)%60;
            //long sec = (inTime/1000)%(60);
            if(hrs+min+sec == 0)
                outTime = "Time Up";
            else
                outTime = String.format("%02d:%02d:%02d", hrs, min, sec);
        }
        catch (Exception e){mydb.logAppError(PageName, "MilliToString", "Exception", e.getMessage());}
        return outTime;
    }

    public String getTimerValue(String StartDate, int Timer){
        String TimerValue = null;
        try{
            SimpleDateFormat simpleDateFormatL = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat simpleDateFormatC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String ts = new Models.TimeStamp().getCurrentTimeStamp();
            Date entertime = simpleDateFormatL.parse(StartDate);
            Date currenttime = simpleDateFormatC.parse(ts);
            long millisElapsed =  currenttime.getTime()-entertime.getTime();
            Timer = Timer * 60 * 1000;
            if(Timer > millisElapsed){
                /*SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("IST"));
                TimerValue = df.format(new Date(millisElapsed));*/
                long millisLeft = Timer-millisElapsed;
                long hoursLeft = millisLeft/(1000 * 60 * 60);
                if(hoursLeft < 10)
                    TimerValue = "0"+hoursLeft+":";
                else
                    TimerValue = hoursLeft+":";

                long minsLeft = (millisLeft%(1000 * 60 * 60))/(60*1000);
                if(minsLeft < 10)
                    TimerValue = TimerValue + "0"+minsLeft+":";
                else
                    TimerValue = TimerValue + minsLeft+":";

                long secsLeft = (millisLeft%(1000 * 60))/1000;
                if(secsLeft < 10)
                    TimerValue = TimerValue + "0"+secsLeft;
                else
                    TimerValue = TimerValue + secsLeft;
            }
            else
                TimerValue = "Time Up";
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getTimerValue", "Exception", e.getMessage());
        }
        return TimerValue;
    }

    public String convertSpecialChar(String inputString){
        String retString = "";
        try{
            String[][] specialChars = {{"$","%24"}, {"&","%26"},{"`","%60"}, {":","%3A"},{"<","%3C"}, {">","%3E"},{"[","%5B"}, {"]","%5D"},{"{","%7B"}, {"}","%7D"},{"“","%22"}, {"+","%2B"},{"#","%23"}, {"%","%25"},
                    {"@","%40"}, {"/","%2F"},{";","%3B"}, {"=","%3D"},{"?","%3F"}, {"\\","%5C"},{"^","%5E"}, {"|","%7C"},{"~","%7E"}, {"‘","%27"},{",","%2C"}};

            String convertedchar;
            for (char ch:inputString.toCharArray()) {
                convertedchar = ch+"";
                for (String[] cc:specialChars) {
                    if(cc[0].equalsIgnoreCase(ch+""))
                        convertedchar = cc[1];
                }
                retString = retString+convertedchar;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "getSpecialChar", "Exception", e.getMessage());}
        return retString;
    }

    public boolean isInteger( String input ) {
        try {Integer.parseInt( input );return true;}
        catch( Exception e ) {return false;}
    }

    public float calculateDistance(double userLat, double userLong, double entityLat, double entityLong){
        float distance = 0;
        try{
            Location user_location=new Location("UserLocation");
            user_location.setLatitude(userLat);
            user_location.setLongitude(userLong);

            Location entity_locations=new Location("EntityLocation");
            entity_locations.setLatitude(entityLat);
            entity_locations.setLongitude(entityLong);

            distance = user_location.distanceTo(entity_locations);
        }
        catch (Exception e){mydb.logAppError(PageName, "calculateDistance", "Exception", e.getMessage());}
        return distance;
    }

    public String substractDates(String strtdate, String enddate){
        String returnVal = "";
        try {
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat returnformater = new SimpleDateFormat("HH:mm");
            Date dateInit = formater.parse(strtdate);
            Date dateEnd = formater.parse(enddate);


            long restDatesinMillis = dateEnd.getTime() - dateInit.getTime();

            int day = (int) TimeUnit.MILLISECONDS.toDays(restDatesinMillis);
            int hh = (int) (TimeUnit.MILLISECONDS.toHours(restDatesinMillis) - TimeUnit.DAYS.toHours(day));
            int mm = (int) (TimeUnit.MILLISECONDS.toMinutes(restDatesinMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(restDatesinMillis)));


            if(day==0)
                returnVal =  hh + " hour " + mm + " min";
            else if(hh==0)
                returnVal =  mm + " min";
            else
                returnVal =  day + " days " + hh + " hour " + mm + " min";

            //returnVal = returnformater.format(restdate);
        }
        catch (Exception e){mydb.logAppError(PageName, "substractDates", "Exception", e.getMessage());}
        return returnVal;

    }

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public Bitmap getBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public String get12HourFormat(String date24HourFormat){
        String format12Hour = "";
        try{
            //below condition is needed is case date24HourFormat is like 2016-08-23T16:58:16.840
            //the it will be converted to 2016-08-23 16:58:16
            date24HourFormat = date24HourFormat.replace('T',' ').substring(0, 19);
            SimpleDateFormat sdf24h = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat sdf12h = new SimpleDateFormat("hh:mm a");
            Date d24H = sdf24h.parse(date24HourFormat);
            format12Hour = sdf12h.format(d24H);
        }
        catch (Exception e){mydb.logAppError(PageName, "get12HourFormat", "Exception", e.getMessage());}
        return format12Hour;
    }
}
