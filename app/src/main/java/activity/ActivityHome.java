package activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.home.logmenow.R;

import org.json.JSONObject;

import database.DBHelper;
import fragment.FragmentHome;
import fragment.FragmentHospital;
import fragment.FragmentScanQR;
import shared.BackgroundTasks;
import shared.CommonClasses;
import shared.DeleteSavedClientData;
import dialog.DialogInformation;
import dialog.DialogNoInternet;
import dialog.DialogWaitTime;
import shared.GlobalClass;
import shared.NetworkDetector;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityHome extends AppCompatActivity implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "ActivityHome";

    private int syncCount = 1;
    private int maxSyncCount = 9;
    private NetworkDetector nd;

    private BlinkTask blinktask;
    private Timer blinktimer;
    private Animation btnclick_amin;

    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_sync;
    private ImageView img_home;
    private ImageView img_search;

    private LinearLayout ll_menuhome;
    public static boolean isapprunning;

    public GlobalClass gc;
    private CommonClasses cc;
    //private LocationHelper lh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);
            mydb = new DBHelper(this);
            nd = new NetworkDetector(this);
            //lh = new LocationHelper(this);

            gc = (GlobalClass) this.getApplicationContext();
            cc = new CommonClasses(this);
            btnclick_amin = AnimationUtils.loadAnimation(this, R.anim.btn_click);
            String fragmentName = getIntent().getStringExtra("Fragment");
            isapprunning = true;
            /*mydb.deleteVehicleEntry();
            mydb.addVehicle("TS 07 EN 4422", 2);*/

            ll_menuhome = (LinearLayout) findViewById(R.id.ll_menuhome);
            ll_menuhome.setOnClickListener(this);

            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_sync = (ImageView) findViewById(R.id.img_sync);
            img_sync.setOnClickListener(this);
            img_home = (ImageView) findViewById(R.id.img_home);
            img_home.setOnClickListener(this);
            img_search = (ImageView) findViewById(R.id.img_search);
            img_search.setOnClickListener(this);
            tv_title.setText(gc.getToActivity().replace("Activity", ""));

            if(gc.getFromActivity().equalsIgnoreCase("ActivityLaunch") && !nd.isInternetAvailable()){
                blinktask = new BlinkTask();
                blinktimer = new Timer();
                blinktimer.schedule(blinktask,100,250);
            }
            //mydb.getImages();
            //String response = mydb.getSystemParameter("RestaurantBusinessHours");
            //Toast.makeText(ActivityHome.this, response, Toast.LENGTH_SHORT).show();
            loadFragment(fragmentName, true);
            //loadFragment("FragmentScanQR");

        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    public void loadFragment(String fragmentName, boolean animatescreen){
        try{
            Class<?> c = Class.forName("fragment."+fragmentName);
            Fragment fragment = (Fragment) c.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if(animatescreen)
                fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            fragmentTransaction.replace(R.id.container_body, fragment, fragmentName);
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "loadFragment", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            v.startAnimation(btnclick_amin);
            switch (v.getId()){
                case R.id.img_back:
                    getBack();
                    break;
                case R.id.tv_title:
                    getBack();
                    break;
                case R.id.ll_menuhome:
                    getBack();
                    break;
                case R.id.img_sync:
                    if(nd.isInternetAvailable()){
                        //String token = mydb.getSystemParameter("DeviceToken");
                        //Toast.makeText(ActivityHome.this, mydb.getSystemParameter("DeviceToken"), Toast.LENGTH_SHORT).show();
                        //new SyncCentralDB().execute();

                        /*double longitude = lh.getLongitude();
                        double latitude = lh.getLatitude();
                        Toast.makeText(this, "longitude = "+longitude+", latitude="+latitude, Toast.LENGTH_SHORT).show();
                        if(longitude == 0.0 || latitude == 0.0)
                            requestLocationPermission();*/
                    }
                    else{
                        DialogNoInternet dni = new DialogNoInternet(this);
                        dni.show();


                    }
                    break;
                case R.id.img_home:
                    loadFragment("FragmentHome", true);
                    break;
                case R.id.img_search:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class SyncCentralDB extends AsyncTask{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            img_sync.setImageResource(R.drawable.ic_syncing);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            new BackgroundTasks(ActivityHome.this, null);
            return null;
        }

        @Override
        protected void onPostExecute(Object obj){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(obj);
            try{
                img_sync.setImageResource(R.drawable.ic_sync);

                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment loadfragment = fragmentManager.findFragmentById(R.id.container_body);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.remove(loadfragment);
                //fragmentTransaction.replace(R.id.container_body, loadfragment, loadfragment.getTag());
                fragmentTransaction.detach(loadfragment);
                fragmentTransaction.attach(loadfragment);
                fragmentTransaction.commit();
            }
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute--Test", "Exception", e.getMessage());}
        }
    }

    public class BlinkTask extends TimerTask {
        public void run(){
            try {
                if(syncCount <= maxSyncCount)
                    new blickSyncIcon().execute();
                else{
                    if(blinktimer != null){
                        blinktimer.cancel();
                        blinktimer = null;
                    }
                    if(blinktask != null){
                        blinktask.cancel();
                        blinktask = null;
                    }
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "BlinkTask", "Exception", e.getMessage());}
        }
    }

    public class blickSyncIcon extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object obj){
            super.onPostExecute(obj);
            try{
                if(syncCount%2 == 0)
                    img_sync.setImageResource(R.drawable.ic_sync);
                else
                    img_sync.setImageResource(R.drawable.ic_sync_offline);
                syncCount = syncCount + 1;
            }
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute", "Exception", e.getMessage());}
        }
    }

    public void getBack(){
        try{
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                //Toast.makeText(ActivityHome.this, "Testing", Toast.LENGTH_SHORT).show();
                fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return;
            }

            switch (tv_title.getText().toString()){
                case "Parking":
                    loadFragment("FragmentHome", true);
                    break;
                case "Vehicle":
                    loadFragment("FragmentParking", true);
                    break;
                case "Add Vehicle":
                    loadFragment("FragmentParking", true);
                    break;
                case "Saved Profiles":
                    loadFragment("FragmentHome", true);
                    break;
                case "Edit Profile":
                    if(!(mydb.ifProfileExists("PersonalInfo") || mydb.ifProfileExists("HomeAddress") || mydb.ifProfileExists("OfficeAddress"))) {
                        loadFragment("FragmentHome", true);
                    }
                    else {
                        loadFragment("FragmentProfileSaved", true);
                    }
                    break;
                case "Profiles":
                    loadFragment("FragmentHome", true);
                    break;
                case "Profile Code":
                    loadFragment("FragmentProfileSaved", true);
                    break;
                case "Scan QR":
                    loadFragment("FragmentHome", true);
                    break;
                case "Restaurant":
                    loadFragment("FragmentScanQR", true);
                    break;
                case "Restaurant Menu":
                    loadFragment("FragmentRestaurant", true);
                    break;
                case "Items Summary":
                    loadFragment("FragmentRestaurantMenu", true);
                    break;
                case "Hospital":
                    loadFragment("FragmentScanQR", true);
                    break;
                case "History":
                    loadFragment("FragmentHome", true);
                    break;
                case "Home":
                    break;
                default:
                    loadFragment("FragmentHome", true);
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "getBack", "Exception", e.getMessage());}
    }

    @Override
    public void onBackPressed() {
        try {
            if (tv_title.getText().toString().equalsIgnoreCase("Home")) {
                isapprunning = false;
                super.onBackPressed();
            }
            else
                getBack();
        }
        catch (Exception e){mydb.logAppError(PageName, "onBackPressed", "Exception", e.getMessage());}
    }

    @Override
    public void onResume() {
        super.onResume();
        isapprunning = true;
        this.registerReceiver(mMessageReceiver, new IntentFilter(getResources().getString(R.string.package_name)));
    }

    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);
        isapprunning = false;
    }


    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                JSONObject jsonObject = new JSONObject(intent.getStringExtra("Json"));
                String FragmentName = jsonObject.getString("ScreenName");
                FragmentManager fragmentManager = getSupportFragmentManager();
                int count = getSupportFragmentManager().getBackStackEntryCount();
                Fragment fragment = fragmentManager.getFragments().get(0);//(count > 0 ? count - 1 : count);
                String currentFragName = (fragment == null)?"NoFragment":fragment.getTag();
                switch (FragmentName) {
                    case "FragmentParking":
                        ParkingNotificationReceived(currentFragName);
                        break;
                    case "FragmentScanQR":
                        String Status = jsonObject.getString("Status");
                        if (Status.equalsIgnoreCase("Status Update") || Status.equalsIgnoreCase("Visit Complete") || Status.equalsIgnoreCase("Appointment Cancelled"))
                            HospitalNotificationReceived(currentFragName);
                        if (Status.equalsIgnoreCase("OrderCompleted") || Status.equalsIgnoreCase("OrderDeleted"))
                            RestaurantNotificationReceived(currentFragName, Status);
                        break;
                    case "FragmentProfile":
                        ProfileNotificationReceived(jsonObject);
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "BroadcastReceiver", "Exception", e.getMessage());}
        }
    };

    public void ParkingNotificationReceived(String currentFragName){
        try{
            if (currentFragName.equalsIgnoreCase("FragmentParking") || currentFragName.equalsIgnoreCase("FragmentParkingStub"))
                loadFragment("FragmentParking", false);
            if (currentFragName.equalsIgnoreCase("FragmentHome")){
                FragmentHome frag = (FragmentHome) getSupportFragmentManager().findFragmentByTag("FragmentHome");
                if (frag != null) {
                    frag.showNotifications();
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "ParkingNotificationReceived", "Exception", e.getMessage());}
    }

    public void RestaurantNotificationReceived(String currentFragName, String Status){
        try{
            mydb.setSystemParameter("FragmentRestaurantNotification", "False");
            if(Status.equalsIgnoreCase("OrderDeleted")) {
                new DialogInformation(ActivityHome.this, Html.fromHtml("Your order has been deleted.<br/>" +
                        "Please contact waiter for further details.<br/>" +
                        "Thanks."), "Restaurant").show();
                mydb.setSystemParameter("OrderPlacedID", "");
            }
            if(Status.equalsIgnoreCase("OrderCompleted")) {
                new DialogInformation(ActivityHome.this, Html.fromHtml("Hope your had a good time.<br/>" +
                        "Please visit again.<br/>" +
                        "Thanks."), "Restaurant").show();
                new DeleteSavedClientData(ActivityHome.this).Delete("Restaurant");
            }

            if(currentFragName.contains("Restaurant")){
                loadFragment("FragmentScanQR", true);
            }
            if(currentFragName.equalsIgnoreCase("FragmentScanQR")){
                FragmentScanQR fragt = (FragmentScanQR) getSupportFragmentManager().findFragmentByTag("FragmentScanQR");
                if(fragt != null){
                    fragt.isCamOn = true;
                    fragt.displaySavedTiles();
                    fragt.camOperation(fragt.isCamOn);
                }
            }

            if(currentFragName.equalsIgnoreCase("NoFragment")){
                loadFragment("FragmentHome", true);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "RestaurantNotificationReceived", "Exception", e.getMessage());}
    }

    public void HospitalNotificationReceived(String currentFragName){
        try{
            mydb.setSystemParameter("FragmentHospitalNotification", "False");
            JSONObject json = new JSONObject(mydb.getSystemParameter("DocApptID"));
            if(json.getString("Qcnt").equalsIgnoreCase("0")) {
                if(json.getString("Status").equalsIgnoreCase("Status Update"))
                    new DialogInformation(ActivityHome.this, Html.fromHtml("Please step in<br/>It's your turn now"), "Doctor").show();
                if(json.getString("Status").equalsIgnoreCase("Visit Complete")) {
                    new DialogInformation(ActivityHome.this, Html.fromHtml("Hope your visit was fruitful.<br/>" +
                            "Your appointment is now closed.<br/>" +
                            "Thanks."), "Doctor").show();
                    mydb.setSystemParameter("DocApptID","");
                    new DeleteSavedClientData(ActivityHome.this).Delete("Hospital");
                    FragmentScanQR fragt = (FragmentScanQR) getSupportFragmentManager().findFragmentByTag("FragmentScanQR");
                    if(fragt != null){
                        fragt.isCamOn = true;
                        fragt.displaySavedTiles();
                        fragt.camOperation(fragt.isCamOn);
                    }
                }
                if(json.getString("Status").equalsIgnoreCase("Appointment Cancelled")) {
                    new DialogInformation(ActivityHome.this, Html.fromHtml("Your appointment has been cancelled.<br/>" +
                            "Please contact your doctor for further details.<br/>" +
                            "Thanks."), "Doctor").show();
                    mydb.setSystemParameter("DocApptID","");
                    new DeleteSavedClientData(ActivityHome.this).Delete("Hospital");
                    FragmentScanQR fragt = (FragmentScanQR) getSupportFragmentManager().findFragmentByTag("FragmentScanQR");
                    if(fragt != null){
                        fragt.isCamOn = true;
                        fragt.displaySavedTiles();
                        fragt.camOperation(fragt.isCamOn);
                    }
                }
            }
            else
                new DialogWaitTime(ActivityHome.this).show();

            //if the app is open and is in FragmentHospital screen, then we need to reset WaitTimer
            if(currentFragName.equalsIgnoreCase("FragmentHospital")){
                FragmentHospital frag = (FragmentHospital) getSupportFragmentManager().findFragmentByTag("FragmentHospital");
                if (frag != null) {
                    String timertext = json.getString("ApptTime").replace("T", " ").substring(0,19);
                    String timer = cc.getTimerValue(timertext, Integer.parseInt(json.getString("AWT")));
                    frag.tv_mainappstatus.setText(json.getString("Qcnt"));
                    frag.tv_maintimer.setText(timer);
                    frag.tv_mainapptid.setText(json.getString("ApptID"));
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "HospitalNotificationReceived", "Exception", e.getMessage());}
    }

    public void ProfileNotificationReceived(JSONObject jsonObject){
        try{
            String text = "Welcome to <u><b>"+jsonObject.getString("Entity")+"</b></u>";
            text = text + "<br/>you have shared following information:";
            JSONObject jsonInfo = jsonObject.getJSONObject("InformationShared");
            if(jsonInfo.has("Name"))
                text = text + "<br/><b>Name:</b>"+ jsonInfo.getString("Name");
            if(jsonInfo.has("Phone"))
                text = text + "<br/><b>Phone:</b>"+ jsonInfo.getString("Phone");
            if(jsonInfo.has("Email"))
                text = text + "<br/><b>Email:</b>"+ jsonInfo.getString("Email");
            if(jsonInfo.has("DOB"))
                text = text + "<br/><b>DOB:</b>"+ jsonInfo.getString("DOB");
            if(jsonInfo.has("Age"))
                text = text + "<br/><b>Age:</b>"+ jsonInfo.getString("Age");
            if(jsonInfo.has("Sex"))
                text = text + "<br/><b>Sex:</b>"+ jsonInfo.getString("Sex");
            if(jsonInfo.has("Vehicle"))
                text = text + "<br/><b>Vehicle No:</b>"+ jsonInfo.getString("Vehicle");
            if(jsonInfo.has("ComingFrom"))
                text = text + "<br/><b>ComingFrom:</b>"+ jsonInfo.getString("ComingFrom");
            if(jsonInfo.has("Purpose"))
                text = text + "<br/><b>Purpose:</b>"+ jsonInfo.getString("Purpose");
            if(jsonInfo.has("VisitingCompany"))
                text = text + "<br/><b>VisitingCompany:</b>"+ jsonInfo.getString("VisitingCompany");
            if(jsonInfo.has("ContactPerson"))
                text = text + "<br/><b>ContactPerson:</b>"+ jsonInfo.getString("ContactPerson");
            if(jsonInfo.has("Block"))
                text = text + "<br/><b>Block:</b>"+ jsonInfo.getString("Block");
            if(jsonInfo.has("Flat"))
                text = text + "<br/><b>Flat:</b>"+ jsonInfo.getString("Flat");
            new DialogInformation(ActivityHome.this, Html.fromHtml(text), "").show();
        }
        catch (Exception e){mydb.logAppError(PageName, "HospitalNotificationReceived", "Exception", e.getMessage());}
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isapprunning = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        //Added this and the problem was solved
        super.onSaveInstanceState(outState);
    }

}
