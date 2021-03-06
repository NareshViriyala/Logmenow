package activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONObject;

import database.DBHelper;
import gcm.GCMRegistrationIntentService;
import shared.BackgroundTasks;
import shared.GlobalClass;
import shared.NetworkDetector;

public class ActivityLaunch extends AppCompatActivity {
    private DBHelper mydb;
    private String PageName = "LaunchActivity";
    //private ImageView img_logo;
    private ImageView img_processing_bar;
    private NetworkDetector nd;
    private GlobalClass gc;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_launch);
            gc = (GlobalClass) this.getApplicationContext();
            mydb = new DBHelper(this);
            //img_logo = (ImageView) findViewById(R.id.img_logo);
            img_processing_bar = (ImageView) findViewById(R.id.img_processing_bar);
            //Glide.with(this).load(R.drawable.logo).into(img_logo);
            Glide.with(this).load(R.drawable.loading).into(img_processing_bar);
            nd = new NetworkDetector(this);
            if(!nd.isInternetAvailable()){
                //mydb.setSystemParameter("ProceedOffline", "False");
                if (mydb.getSystemParameter("ProceedOffline").equalsIgnoreCase("True")) {
                    //TODO
                    //in settings give option to make it false again
                    //so that the user be prompted with offline pop up
                    //Toast.makeText(LaunchActivity.this, "Proceeding with offline mode", Toast.LENGTH_SHORT).show();
                    gotoHome();
                }
                else {
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_internet, null);
                    dialogBuilder.setView(dialogView);
                    final Dialog dialog = dialogBuilder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    Button btn_proceed = (Button) dialogView.findViewById(R.id.btn_proceed);
                    Button btn_exit = (Button) dialogView.findViewById(R.id.btn_exit);
                    CheckBox cb_proceedoffline = (CheckBox) dialogView.findViewById(R.id.cb_proceedoffline);
                    cb_proceedoffline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked)
                                mydb.setSystemParameter("ProceedOffline", "True");
                        }
                    });
                    btn_proceed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            gotoHome();
                        }
                    });
                    btn_exit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            System.exit(0);
                        }
                    });
                    dialog.show();
                }
            }
            else {
                //gotoHome();
                //Register Notification Services
                mRegistrationBroadcastReceiver = new BroadcastReceiver() {

                    //When the broadcast received
                    //We are sending the broadcast from GCMRegistrationIntentService

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        //If the broadcast has received with success
                        //that means device is registered successfully
                        if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                            //Getting the registration token from the intent
                            String token = intent.getStringExtra("token");
                            //Displaying the token as toast
                            //Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
                            try {
                                JSONObject jsonObject = mydb.getDeviceInfo();
                                String currentToken = jsonObject.getString("DeviceToken");
                                if(!currentToken.equalsIgnoreCase(token)) {
                                    mydb.setDeviceInfo("DeviceToken", token);
                                    mydb.setDeviceInfo("DeviceVersion", Build.VERSION.RELEASE);
                                }
                                new syncLocalDB().execute();
                            }
                            catch (Exception e){mydb.logAppError(PageName, "onReceive", "Exception", e.getMessage());}

                            //if the intent is not with success then displaying error messages
                        } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                            Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                            new syncLocalDB().execute();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                //Checking play service is available or not
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

                //if play service is not available
                if(ConnectionResult.SUCCESS != resultCode) {
                    //If play service is supported but not installed
                    if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                        //Displaying message that play service is not installed
                        Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!\nNotifications might not work.", Toast.LENGTH_LONG).show();
                        GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                        //If play service is not supported
                        //Displaying an error message
                    } else {
                        Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!\nNotifications might not work", Toast.LENGTH_LONG).show();
                    }
                    new syncLocalDB().execute();

                    //If play service is available
                } else {
                    //Starting intent to register device
                    Intent itent = new Intent(this, GCMRegistrationIntentService.class);
                    startService(itent);
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void gotoHome(){
        try{
            gc.setToActivity("ActivityHome");
            gc.setFromActivity("ActivityLaunch");
            Intent intent = new Intent(ActivityLaunch.this, ActivityHome.class);
            intent.putExtra("Fragment", "FragmentHome");
            startActivity(intent);
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            finish();
        }
        catch (Exception e){mydb.logAppError(PageName, "gotoHome", "Exception", e.getMessage());}
    }

    public class syncLocalDB extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {
            try {
                new BackgroundTasks(ActivityLaunch.this, null);
            }
            catch(Exception e){
                mydb.logAppError(PageName, "asyncEnterCall--doInBackground", "Exception", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object json) {
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(json);
            gotoHome();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
