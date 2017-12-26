package shared;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.widget.RemoteViews;

import com.example.home.logmenow.R;

import org.json.JSONObject;

import activity.ActivityHome;
import database.DBHelper;

/**
 * Created by Home on 8/21/2016.
 */
public class PushNotificationHandler {

    private DBHelper mydb;
    private String PageName = "PushNotificationHandler";
    private JSONObject jsonObject;
    private Context context;
    //private String NotificationMessage;

    public PushNotificationHandler(Context context, JSONObject jsonObject){
        this.context = context;
        this.jsonObject = jsonObject;
        mydb = new DBHelper(context);
    }

    public String getNotificationMessage(){
        String NotificationMessage = "";
        try{
            String Status = jsonObject.getString("Status");
            switch (jsonObject.getString("ScreenName")) {
                case "FragmentParking":
                    NotificationMessage = ParkingNotification();
                    break;
                case "FragmentScanQR":
                    if (Status.equalsIgnoreCase("Status Update") || Status.equalsIgnoreCase("Visit Complete") || Status.equalsIgnoreCase("Appointment Cancelled"))
                        NotificationMessage = HospitalNotification();
                    if (Status.equalsIgnoreCase("OrderCompleted") || Status.equalsIgnoreCase("OrderDeleted"))
                        NotificationMessage = RestaurantNotification();
                        break;
                case "FragmentProfile":
                    NotificationMessage = "Welcome to "+jsonObject.getString("Entity");
                    break;
                default:
                    break;
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "HandleNotification", "Exception", e.getMessage());}
        return NotificationMessage;
    }

    public String ParkingNotification(){
        String NotificationMessage = "";
        try{
            mydb.setSystemParameter(jsonObject.getString("ScreenName")+"Notification", "True");
            if(jsonObject.getString("ExitDate").equalsIgnoreCase("") || jsonObject.getString("ExitDate").equalsIgnoreCase("null"))
                NotificationMessage = "Welcome to "+jsonObject.getString("Entity");
            else
                NotificationMessage = "Visit again "+jsonObject.getString("Entity");
            mydb.addVehicleEntry(jsonObject);
        }
        catch(Exception e){mydb.logAppError(PageName, "ParkingNotification", "Exception", e.getMessage());}
        return NotificationMessage;
    }

    public String RestaurantNotification(){
        String NotificationMessage = "";
        try{
            mydb.setSystemParameter("FragmentRestaurantNotification", "True");
            if(jsonObject.getString("Status").equalsIgnoreCase("OrderDeleted")) {
                NotificationMessage = "Your order has been deleted.";
                mydb.setSystemParameter("OrderPlacedID", "");
            }
            if(jsonObject.getString("Status").equalsIgnoreCase("OrderCompleted")) {
                NotificationMessage = "Hope your had a good time.<br/>Please visit again.<br/>Thanks.";
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "RestaurantNotification", "Exception", e.getMessage());}
        return NotificationMessage;
    }

    public String HospitalNotification(){
        String NotificationMessage = "";
        try{
            jsonObject.put("ApptTime", new  Models.TimeStamp().getCurrentTimeStamp());
            mydb.setSystemParameter("FragmentHospitalNotification", "True");
            mydb.setSystemParameter("DocApptID", jsonObject.toString());
            String Qcnt = jsonObject.getString("Qcnt");
            if(Qcnt.equalsIgnoreCase("0")) {
                if(jsonObject.getString("Status").equalsIgnoreCase("Status Update"))
                    NotificationMessage = "Please step in, Its your turn now.";
                if(jsonObject.getString("Status").equalsIgnoreCase("Visit Complete"))
                    NotificationMessage = "Hope your visit was fruitful.<br/>Your appointment is now closed.<br/>Thanks.";
                if(jsonObject.getString("Status").equalsIgnoreCase("Appointment Cancelled"))
                    NotificationMessage = "Your appointment has been cancelled.<br/>Please contact your doctor for further details.<br/>Thanks.";
            }
            else
                NotificationMessage = "Now your are number \""+Qcnt+"\" in Q";
        }
        catch(Exception e){mydb.logAppError(PageName, "HospitalNotification", "Exception", e.getMessage());}
        return NotificationMessage;
    }

    public void CustomNotification(String NotificationMessage) {
        try {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_general);
            Intent intent = new Intent(context, ActivityHome.class);
//            intent.putExtra("Fragment", jsonObject.getString("ScreenName"));
            intent.putExtra("Fragment", "FragmentHome");
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setContent(remoteViews);
            remoteViews.setImageViewResource(R.id.img_icon,R.drawable.ic_logo);
            remoteViews.setTextViewText(R.id.tv_notificationtext, Html.fromHtml(NotificationMessage));
            remoteViews.setTextColor(R.id.tv_notificationtext, context.getResources().getColor(R.color.colorPrimary));
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationmanager.notify(0, builder.build());
        }
        catch(Exception e){mydb.logAppError(PageName, "CustomNotification", "Exception", e.getMessage());}
    }
}
