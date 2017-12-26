package gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.home.logmenow.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import database.DBHelper;

/**
 * Created by Home on 7/24/2016.
 */
public class GCMRegistrationIntentService extends IntentService {

    //Constants for success and errors
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";
    private DBHelper mydb;
    private String PageName = "GCMRegistrationIntentService";

    //Class constructor
    public GCMRegistrationIntentService() {
        super("");
        mydb = new DBHelper(this);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            //Registration complete intent initially null
            Intent registrationComplete = null;

            //Register token is also null
            //we will get the token on successfull registration
            String token = null;
            try {
                //Creating an instanceid
                InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

                //Getting the token from the instance id
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                //Displaying the token in the log so that we can copy it to send push notification
                //You can also extend the app by storing the token in to your server
                //Log.w("GCMRegIntentService", "token:" + token);

                //on registration complete creating intent with success
                registrationComplete = new Intent(REGISTRATION_SUCCESS);

                //Putting the token to the intent
                registrationComplete.putExtra("token", token);
            } catch (Exception e) {
                //If any error occurred
                registrationComplete = new Intent(REGISTRATION_ERROR);
            }

            //Sending the broadcast that registration is completed
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
        catch (Exception e){mydb.logAppError(PageName, "onHandleIntent", "Exception", e.getMessage());}
    }
}
