package gcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.widget.RemoteViews;

import com.example.home.logmenow.R;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.util.List;

import activity.ActivityHome;
import database.DBHelper;
import shared.DeleteSavedClientData;
import shared.Models;
import shared.PushNotificationHandler;

/**
 * Created by Home on 7/24/2016.
 */
public class GCMPushReceiverService extends GcmListenerService {
    private DBHelper mydb;
    private String PageName = "GCMPushReceiverService";
    public Vibrator v;
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        mydb = new DBHelper(this);
        sendNotification(message);
    }

    private void sendNotification(String message) {
        String NotificationMessage = "";
        try {
            v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
            JSONObject jsonObject = new JSONObject(message);
            PushNotificationHandler pushNotificationHandler = new PushNotificationHandler(this, jsonObject);
            NotificationMessage = pushNotificationHandler.getNotificationMessage();
            /*switch (jsonObject.getString("ScreenName")){
                case "FragmentParking":
                    mydb.setSystemParameter(jsonObject.getString("FragmentName")+"Notification", "True");
                    if(jsonObject.getString("ExitDate").equalsIgnoreCase("null"))
                        NotificationMessage = "Welcome to "+jsonObject.getString("Entity");
                    else
                        NotificationMessage = "Visit again "+jsonObject.getString("Entity");
                    mydb.addVehicleEntry(jsonObject);
                    break;
                case "FragmentScanQR":
                    jsonObject.put("ApptTime", new  Models.TimeStamp().getCurrentTimeStamp());
                    mydb.setSystemParameter("FragmentHospitalNotification", "True");
                    mydb.setSystemParameter("DocApptID", jsonObject .toString());
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
                    break;
                default:
                    break;
            }*/

            if(ActivityHome.isapprunning) {
                Intent intent = new Intent(getResources().getString(R.string.package_name));
                intent.putExtra("Json", jsonObject.toString());
                this.sendBroadcast(intent);
            }
            else {
                switch (jsonObject.getString("ScreenName")){
                    case "FragmentScanQR":
                        String Status = jsonObject.getString("Status");
                        if(Status.equalsIgnoreCase("Visit Complete") || Status.equalsIgnoreCase("Appointment Cancelled")) {
                            mydb.setSystemParameter("DocApptID","");
                            new DeleteSavedClientData(this).Delete("Hospital");
                        }
                        if(Status.equalsIgnoreCase("OrderDeleted")) {
                            mydb.setSystemParameter("OrderPlacedID", "");
                        }
                        if(Status.equalsIgnoreCase("OrderCompleted")) {
                            new DeleteSavedClientData(this).Delete("Restaurant");
                        }
                        break;
                    default:
                        break;
                }
                //CustomNotification(jsonObject, NotificationMessage);
                pushNotificationHandler.CustomNotification(NotificationMessage);
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "sendNotification", "Exception", e.getMessage());}
    }

    public void CustomNotification(JSONObject jsonObject, String NotificationMessage) {
        try {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_general);
            Intent intent = new Intent(this, ActivityHome.class);
            intent.putExtra("Fragment", jsonObject.getString("ScreenName"));
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setContent(remoteViews);
            remoteViews.setImageViewResource(R.id.img_icon,R.drawable.ic_logo);
            remoteViews.setTextViewText(R.id.tv_notificationtext, Html.fromHtml(NotificationMessage));
            remoteViews.setTextColor(R.id.tv_notificationtext, getResources().getColor(R.color.colorPrimary));
            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.notify(0, builder.build());
        }
        catch(Exception e){mydb.logAppError(PageName, "CustomNotification", "Exception", e.getMessage());}
    }
}
