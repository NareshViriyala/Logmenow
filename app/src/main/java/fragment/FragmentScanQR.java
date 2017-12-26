package fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DBHelper;
import gcm.PushNotification;
import shared.BackgroundTasks;
import shared.CommonClasses;
import shared.DeleteSavedClientData;
import dialog.DialogInformation;
import dialog.DialogWaitTime;
import shared.HTTPCallJSon;
import shared.LocationHelper;
import shared.NetworkDetector;
import zxing.BinaryBitmap;
import zxing.ChecksumException;
import zxing.DecodeHintType;
import zxing.FormatException;
import zxing.NotFoundException;
import zxing.PlanarYUVLuminanceSource;
import zxing.Reader;
import zxing.Result;
import zxing.common.HybridBinarizer;
import zxing.qrcode.QRCodeReader;

/**
 * Created by Home on 7/18/2016.
 */
public class FragmentScanQR extends Fragment implements SurfaceHolder.Callback, View.OnClickListener{

    private DBHelper mydb;
    private String PageName = "FragmentScanQR";
    private NetworkDetector nd;

    public TakeShots takeshots;
    public Timer timer;
    private TextView tv_scanqr;
    private ImageView img_processing;
    private TextView tv_scanStatus;

    public int shotInterval = 100;
    public boolean decodingQR = false;
    private boolean screenChanging = false;
    public SurfaceView sfv_camview;
    public Camera mCamera;
    public SurfaceHolder surfaceHolder;
    public Camera.PreviewCallback previewCallback;
    public int vibrateMilli = 400;
    public Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();

    public String lastQRContent = "";
    public Vibrator v;
    private boolean progress = true;
    public boolean isCamOn = true;

    //private GlobalClass gc;
    private CommonClasses cc;
    private LocationHelper lh;
    private int REQUEST_CAMERA_CODE = 100;

    private LinearLayout ll_delete_restaurant;
    private LinearLayout ll_delete_hospital;
    private LinearLayout ll_callwaiter;
    private LinearLayout ll_waitingtime;

    private LinearLayout ll_restaurant;
    private LinearLayout ll_hospital;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scanqr, container, false);
        try {
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadViewContent();
                }
            }, 100);*/
            mydb = new DBHelper(getActivity());

            nd = new NetworkDetector(getActivity());
            cc = new CommonClasses(getActivity());
            lh = new LocationHelper(getActivity());
            //gc = (GlobalClass) getActivity().getApplicationContext();

            ll_restaurant = (LinearLayout) rootView.findViewById(R.id.ll_restaurant);
            ll_restaurant.setOnClickListener(this);
            ll_hospital = (LinearLayout) rootView.findViewById(R.id.ll_hospital);
            ll_hospital.setOnClickListener(this);

            //takeshots = new TakeShots();
            //timer = new Timer();
            img_processing = (ImageView) rootView.findViewById(R.id.img_processing);
            Glide.with(getActivity()).load(R.drawable.preloader).into(img_processing);
            tv_scanqr = (TextView) rootView.findViewById(R.id.tv_scanqr);
            tv_scanqr.setOnClickListener(this);
            tv_scanStatus = (TextView) rootView.findViewById(R.id.tv_scanStatus);

            displaySavedTiles();

            ll_delete_restaurant = (LinearLayout) rootView.findViewById(R.id.ll_delete_restaurant);
            ll_delete_hospital = (LinearLayout) rootView.findViewById(R.id.ll_delete_hospital);
            ll_waitingtime = (LinearLayout) rootView.findViewById(R.id.ll_waitingtime);
            ll_callwaiter = (LinearLayout) rootView.findViewById(R.id.ll_callwaiter);
            ll_delete_restaurant.setOnClickListener(this);
            ll_delete_hospital.setOnClickListener(this);
            ll_waitingtime.setOnClickListener(this);
            ll_callwaiter.setOnClickListener(this);

            if(mydb.getSystemParameter("OrderPlacedID").equalsIgnoreCase(""))
                ll_callwaiter.setVisibility(View.GONE);
            else
                ll_callwaiter.setVisibility(View.VISIBLE);

            if(mydb.getSystemParameter("DocApptID").equalsIgnoreCase(""))
                ll_waitingtime.setVisibility(View.GONE);
            else {
                JSONObject jobj = new JSONObject(mydb.getSystemParameter("DocApptID"));
                if(jobj.has("ApptID") && jobj.getString("ApptID").equalsIgnoreCase("0"))
                    ll_waitingtime.setVisibility(View.GONE);
                else
                    ll_waitingtime.setVisibility(View.VISIBLE);
            }


            sfv_camview = (SurfaceView) rootView.findViewById(R.id.sfv_camview);
            sfv_camview.setOnClickListener(this);
            surfaceHolder = sfv_camview.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            previewCallback = new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    try {
                        if(!decodingQR && !screenChanging) {
                            decodingQR = true;
                            Object boxdata = data;
                            new decodeQR().execute(boxdata);
                        }
                    } catch (Exception e) {
                        mydb.logAppError(PageName, "onPreviewFrame", "Exception", e.getMessage());
                    }
                }
            };

        }
        catch (Exception e){mydb.logAppError(PageName, "onCreateView", "Exception", e.getMessage());}
        return rootView;
    }

    public void displaySavedTiles(){
        try{
            //check for saved restaurant menu
            if(!mydb.getSystemParameter("RestaurantDetails").equalsIgnoreCase("")){
                isCamOn = false;
                ImageView img_restaurant = (ImageView) rootView.findViewById(R.id.img_restaurant);
                byte[] bytes = mydb.getEntityImage("Restaurant");
                Bitmap bitmap = null;

                if(bytes != null) {
                    bitmap = cc.getBitmap(bytes);
                    img_restaurant.setImageBitmap(bitmap);
                }
                else
                    img_restaurant.setImageResource(R.drawable.ic_noimage);

                TextView tv_restaurant = (TextView) rootView.findViewById(R.id.tv_restaurant);
                JSONObject jobj = new JSONObject(mydb.getSystemParameter("RestaurantDetails"));
                tv_restaurant.setText(jobj.getString("Ename")+"\nSaved order");
                ll_restaurant.setVisibility(View.VISIBLE);
            }
            else
                ll_restaurant.setVisibility(View.GONE);

            //check for saved hospital details
            if(!mydb.getSystemParameter("HospitalDetails").equalsIgnoreCase("")){
                isCamOn = false;
                ImageView img_hospital = (ImageView) rootView.findViewById(R.id.img_hospital);
                //img_hospital.setImageBitmap(mydb.getEntityImage("Hospital"));

                byte[] bytes = mydb.getEntityImage("Hospital");
                Bitmap bitmap = null;

                if(bytes != null) {
                    bitmap = cc.getBitmap(bytes);
                    img_hospital.setImageBitmap(bitmap);
                }
                else
                    img_hospital.setImageResource(R.drawable.ic_noimage);

                TextView tv_hospital = (TextView) rootView.findViewById(R.id.tv_hospital);
                JSONObject jobj = new JSONObject(mydb.getSystemParameter("HospitalDetails"));
                tv_hospital.setText(jobj.getString("Ename")+"\nPending appointment");

                ll_hospital.setVisibility(View.VISIBLE);
            }
            else
                ll_hospital.setVisibility(View.GONE);
        }
        catch (Exception e){mydb.logAppError(PageName, "displaySavedTiles", "Exception", e.getMessage());}
    }

    private void overwriteSavedDetails(final String fragmentName){
        try{
            switch (fragmentName){
                case "FragmentRestaurant":
                    if(ll_restaurant.getVisibility() == View.VISIBLE)
                        showOverWritePopup(fragmentName.replace("Fragment", ""), "Your current saved menu and its selections (if any) would be erased.\n\nWould you like to proceed ?\n", true);
                    else
                        loadFragment(fragmentName);
                    break;
                case "FragmentHospital":
                    if(ll_hospital.getVisibility() == View.VISIBLE) {
                        JSONObject jsonObject = new JSONObject(mydb.getSystemParameter("DoctorDetails"));
                        String DocName = jsonObject.getString("Dname");
                        showOverWritePopup(fragmentName.replace("Fragment", ""), "Your current appointment with "+DocName+" would be cancelled.\n\nWould you like to proceed ?\n", true);
                    }
                    else
                        loadFragment(fragmentName);
                    break;
                case "FragmentSecurity":
                    loadFragment(fragmentName);
                    break;
                case "FragmentCustomerFeedback":
                    loadFragment(fragmentName);
                    break;
                /*case "FragmentBank":
                    break;*/
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "overwriteSavedDetails", "Exception", e.getMessage());}
    }

    private void callWaiter(){
        try{
            new callWaiter().execute();
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.dialog_callingwaiter, null);
            final PopupWindow popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setAnimationStyle(R.style.DialogAnimation);
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                }
            }, 2000);
        }
        catch (Exception e){mydb.logAppError(PageName, "showTakeApptPopup", "Exception", e.getMessage());}
    }

    public class decodeQR extends AsyncTask<Object, Integer, String> {

        @Override
        protected String doInBackground(Object[] params) {
            //android.os.Debug.waitForDebugger();
            String decodedText = null;
            try {
                byte[] data = (byte[])params[0];
                Camera.Parameters parameters = mCamera.getParameters();
                int previewHeight = parameters.getPreviewSize().width;
                int previewWidth = parameters.getPreviewSize().height;
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, previewHeight, previewWidth, 0, 0, previewHeight, previewWidth, false);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new QRCodeReader();
                Result result = reader.decode(bitmap, decodeHints);
                decodedText = result.getText();
            }
            catch (NotFoundException e) {
                decodingQR = false;
            }
            catch (ChecksumException e) {
                decodingQR = false;
            }
            catch (FormatException e) {
                decodingQR = false;
            }
            return decodedText;
        }

        @Override
        protected void onPostExecute(String decodedText){
            super.onPostExecute(decodedText);
            if(decodedText != null) {
                doOperation(decodedText);
            }
            else
                new scanAnimation().execute();
        }
    }

    public class scanAnimation extends AsyncTask<Object,Integer,Object>{

        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object input){
            super.onPostExecute(input);
            try{
                if(progress){
                    if(tv_scanStatus.getText().toString().length() > 40) {
                        progress = false;
                    }
                    else
                        tv_scanStatus.setText("*"+tv_scanStatus.getText().toString()+"*");
                }
                else{
                    if(tv_scanStatus.getText().toString().length() <= 12){
                        progress = true;
                        tv_scanStatus.setText("Searching QR");
                    }
                    else{
                        tv_scanStatus.setText(tv_scanStatus.getText().toString().substring(1, tv_scanStatus.getText().toString().length()-1));
                    }
                }
            }
            catch (Exception e){}
        }
    }

    public void doOperation(String QRContent){
        try{
            if(lastQRContent.equalsIgnoreCase(QRContent))
                return;
            lastQRContent = QRContent;
            v.vibrate(vibrateMilli/4);
            if(nd.isInternetAvailable() && validGUID(QRContent)) {
                mCamera.stopPreview();
                if(!cc.isInteger(mydb.getDeviceID()))
                    new BackgroundTasks(getActivity(), null);
                new getGUIDType().execute(QRContent);
            }
            else {
                decodingQR = false;
                lastQRContent = "";
                if(!nd.isInternetAvailable())
                    tv_scanqr.setText("Internet Unavailable");
                else {
                    tv_scanqr.setText("Not a " + getResources().getString(R.string.app_name) + " QR code");
                    //tv_scanqr.setText(QRContent);
                }

            }
        }
        catch (Exception e){mydb.logAppError(PageName, "doOperation", "Exception", e.getMessage());}
    }

    public boolean validGUID(String guid){
        boolean ret = false;
        decodingQR = false;
        try{
            Pattern pattern = Pattern.compile("^[0-9A-Fa-f]{8}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{4}[-][0-9A-Fa-f]{12}$");
            Matcher matcher = pattern.matcher(guid);
            if(matcher.matches())
                ret = true;
        }
        catch (Exception e){mydb.logAppError(PageName, "validGUID", "Exception", e.getMessage());}
        return ret;
    }

    public class getGUIDType extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            try {
                img_processing.setVisibility(View.VISIBLE);
                tv_scanqr.setVisibility(View.GONE);
            }
            catch (Exception e){mydb.logAppError(PageName, "getGUIDType--onPreExecute", "Exception", e.getMessage());}
        }

        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = null;
            try {
                response = new HTTPCallJSon(getActivity()).Get("GetEntityType", "?id="+params[0]);
            }
            catch (Exception e){
                decodingQR = false;
                mCamera.startPreview();
                mydb.logAppError(PageName, "getGUIDType--doInBackground", "Exception", e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {
                response = new JSONArray(response).getJSONObject(0).toString();
                JSONObject jobj = new JSONObject(response);
                img_processing.setVisibility(View.GONE);
                tv_scanqr.setVisibility(View.VISIBLE);
                if(jobj.getString("EntityType").equalsIgnoreCase("null") || jobj.getString("SubjectType").equalsIgnoreCase("null")){
                    tv_scanqr.setText("Apologies!! "+getResources().getString(R.string.app_name)+" do not recognise this QR code");
                    decodingQR = false;
                    lastQRContent = "";
                    mCamera.startPreview();
                    return;
                }
                String fragmentName = "";
                switch (jobj.getString("EntityType")){
                    case "Restaurant":
                        mydb.setSystemParameter("RestaurantGUID", lastQRContent);
                        fragmentName = "FragmentRestaurant";
                        break;
                    case "Hospital":
                        mydb.setSystemParameter("HospitalGUID", lastQRContent);
                        fragmentName = "FragmentHospital";
                        break;
                    case "SecurityCheck":
                        mydb.setSystemParameter("SecurityGUID", lastQRContent);
                        fragmentName = "FragmentSecurity";
                        break;
                    case "CustomerFeedback":
                        mydb.setSystemParameter("FeedbackGUID", lastQRContent);
                        mydb.setSystemParameter("FeedbackSubject", jobj.getString("SubjectType").toLowerCase());
                        fragmentName = "FragmentCustomerFeedback";
                        break;
                    /*case "Bank":
                        mydb.setSystemParameter("BankGUID", lastQRContent);
                        fragmentName = "FragmentBank";
                        break;*/
                    default:
                        tv_scanqr.setText("Apologies!! "+getResources().getString(R.string.app_name)+" do not recognise this QR code");
                        decodingQR = false;
                        lastQRContent = "";
                        mCamera.startPreview();
                        break;
                }
                overwriteSavedDetails(fragmentName);
            }
            catch (Exception e){
                decodingQR = false;
                mCamera.startPreview();
                mydb.logAppError(PageName, "getGUIDType--onPostExecute", "Exception", e.getMessage());
            }
        }
    }

    public class callWaiter extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] params) {
            try{
                //JSONArray jsonArray = mydb.getCallWaiterURLs();
                String OrderPlacedID = mydb.getSystemParameter("OrderPlacedID");
                String ResGUID = mydb.getSystemParameter("RestaurantGUID");
                new HTTPCallJSon(getActivity()).Get("CallWaiter","?json={\"MasterID\":"+OrderPlacedID+",\"input\":\""+ResGUID+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "callWaiter--doInBackground", "Exception", e.getMessage());}
            return null;
        }

        @Override
        protected void onPostExecute(Object response) {
            super.onPostExecute(response);
            try {
                new PushNotification(getActivity()).execute();
            }
            catch (Exception e){mydb.logAppError(PageName, "callWaiter--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public void loadFragment(String fragmentName){
        try{
            //Bundle bundle = new Bundle();
            //bundle.putString("guid",lastQRContent);
            screenChanging = true;
            decodingQR = false;
            Class<?> c = Class.forName("fragment."+fragmentName);
            Fragment fragment = (Fragment) c.newInstance();
            //fragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            fragmentTransaction.replace(R.id.container_body, fragment, fragmentName);
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "loadFragment", "Exception", e.getMessage());}
    }

    public void camOperation(boolean start){
        try{
            if(start){
                if (timer == null) {
                    mCamera.startPreview();
                    timer = new Timer();
                    takeshots = new TakeShots();
                    timer.schedule(takeshots, 100, shotInterval);
                }
            }
            else{
                if(mCamera != null)
                    mCamera.stopPreview();
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                if(takeshots != null){
                    takeshots.cancel();
                    takeshots = null;
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "camOperation", "Exception", e.getMessage());}
    }

    public class TakeShots extends TimerTask {
        public void run(){
            try {
                mCamera.setOneShotPreviewCallback(previewCallback);
            }
            catch (Exception e){
                mydb.logAppError(PageName, "TakeShots", "Exception", e.getMessage());
            }
        }
    }

    public void requestCameraPermissions(){
        try{
            int hasCameraAccess = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if (hasCameraAccess != PackageManager.PERMISSION_GRANTED) {

                showMessageOKCancel(getResources().getString(R.string.app_name)+ " " +getResources().getString(R.string.campermission),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
                        }
                    });
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "requestPermissions", "Exception", e.getMessage());}
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showOverWritePopup(final String type, final String text, final boolean gotonext) {
        try{
            mCamera.stopPreview();
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.dialog_overscanqr, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(popupView);
            final AlertDialog alertDialog = builder.show();
            //builder.show();

            ImageView img_entitylogo = (ImageView) popupView.findViewById(R.id.img_entitylogo);
            TextView tv_line = (TextView) popupView.findViewById(R.id.tv_line);
            tv_line.setText(text);
            byte[] bytes = mydb.getEntityImage(type);
            if(bytes != null) {
                Bitmap bitmap = cc.getBitmap(bytes);
                img_entitylogo.setImageBitmap(bitmap);
            }
            else
                img_entitylogo.setImageResource(R.drawable.ic_noimage);

            Button btn_ok = (Button) popupView.findViewById(R.id.btn_ok);
            Button btn_cancel = (Button) popupView.findViewById(R.id.btn_cancel);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeleteSavedClientData(getActivity()).Delete(type);
                    if(!gotonext) {
                        isCamOn = true;
                        displaySavedTiles();
                        camOperation(isCamOn);
                    }
                    else
                        loadFragment("Fragment"+type);
                    alertDialog.dismiss();
                }
            });
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decodingQR = false;
                    lastQRContent = "";
                    mCamera.startPreview();
                    alertDialog.dismiss();
                }
            });
        }
        catch (Exception e){mydb.logAppError(PageName, "showOverWriteDialog", "Exception", e.getMessage());}
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        int wid = sfv_camview.getWidth();
        int hig = sfv_camview.getHeight();

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        int diff = Math.abs((wid * hig)-(sizeList.get(0).width * sizeList.get(0).height));
        for(int i = 0; i < sizeList.size(); i++){
            int slw = sizeList.get(i).width;
            int slh = sizeList.get(i).height;
            if(diff > Math.abs((wid * hig)-(sizeList.get(i).width * sizeList.get(i).height))){
                bestSize = sizeList.get(i);
                diff = Math.abs((wid * hig)-(sizeList.get(i).width * sizeList.get(i).height));
            }
        }
        return bestSize;
    }

    public void checkCurrentLocationAndCallWaiter(){
        try{
            lh = new LocationHelper(getActivity());
            if (lh.canGetLocation()) { //GPS is enabled
                double longitude = lh.getLongitude();
                double latitude = lh.getLatitude();
                if (longitude == 0.0 || latitude == 0.0) { //location services for this app is not allowed
                    requestLocationPermission();
                    return;
                } else {//GPS is enables and location services for this app is allowed
                    //check for distance and then place order
                    JSONObject json = new JSONObject(mydb.getSystemParameter("RestaurantDetails"));
                    double entityLat = json.getDouble("Lat");
                    double entitLong = json.getDouble("Long");
                    int distanceallowed = json.getInt("PD");
                    float distance = cc.calculateDistance(latitude, longitude, entityLat, entitLong);

                    if(distance > distanceallowed){
                        new DialogInformation(getActivity(), Html.fromHtml("We don't find you inside our restaurant.<br/>This feature will not work now."), "Restaurant").show();
                        return;
                    }
                    else{
                        callWaiter();
                        return;
                    }
                }
            } else { //GPS is not enabled
                Toast.makeText(getActivity(), "Please enable Location service", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "checkCurrentLocationAndCallWaiter", "Exception", e.getMessage());}
    }

    public void requestLocationPermission(){
        try{
            int hasCoarseLocationAccess = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
            int hasFineLocationAccess = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasCoarseLocationAccess != PackageManager.PERMISSION_GRANTED || hasFineLocationAccess != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 500);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "requestPermissions", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try {
            ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.colorPrimary)), new ColorDrawable(getResources().getColor(R.color.white))};
            TransitionDrawable trans = new TransitionDrawable(color);
            switch (v.getId()){
                case R.id.ll_delete_restaurant:
                    ll_delete_restaurant.setBackgroundDrawable(trans);
                    trans.startTransition(1000);
                    showOverWritePopup("Restaurant", "Deleting saved menu list! \nAre you sure?\n\n", false);
                    break;
                case R.id.ll_delete_hospital:
                    ll_delete_hospital.setBackgroundDrawable(trans);
                    trans.startTransition(1000);
                    String msg = "";
                    if(mydb.getSystemParameter("DocApptID").equalsIgnoreCase(""))
                        msg = "Deleting saved information!\nAre you sure?\n\n";
                    else if(mydb.getSystemParameter("DoctorDetails").equalsIgnoreCase("")){
                        msg = "Saved information will be deleted.\n\nWould you like to proceed ?\n";
                    }
                    else{
                        JSONObject jsonObject = new JSONObject(mydb.getSystemParameter("DoctorDetails"));
                        String DocName = jsonObject.getString("Dname");
                        msg = "Your current appointment with " + DocName + " would be cancelled.\n\nWould you like to proceed ?\n";
                    }
                    showOverWritePopup("Hospital", msg, false);
                    break;
                case R.id.ll_waitingtime:
                    ll_waitingtime.setBackgroundDrawable(trans);
                    trans.startTransition(1000);
                    mydb.setSystemParameter("FragmentHospitalNotification", "False");
                    new DialogWaitTime(getActivity()).show();
                    break;
                case R.id.ll_callwaiter:
                    //check the current location and then make the WS call
                    ll_callwaiter.setBackgroundDrawable(trans);
                    trans.startTransition(1000);
                    checkCurrentLocationAndCallWaiter();
                    //callWaiter();
                    break;
                case R.id.ll_restaurant:
                    if(mydb.getMenuItemCount("SelectedItems")>0)
                        loadFragment("FragmentRestaurantMenuSummary");
                    else
                        loadFragment("FragmentRestaurantMenu");
                    break;
                case R.id.ll_hospital:
                    mydb.setSystemParameter("FragmentHospitalNotification", "False");
                    loadFragment("FragmentHospital");
                    break;
                case R.id.tv_scanqr:
                    if(isCamOn) {
                        camOperation(false);
                        isCamOn = false;
                    }
                    else {
                        isCamOn = true;
                        camOperation(true);
                    }
                    break;
                case R.id.sfv_camview:
                    if(isCamOn) {
                        camOperation(false);
                        isCamOn = false;
                    }
                    else {
                        isCamOn = true;
                        camOperation(true);
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try{
            if(mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "surfaceDestroyed", "Exception", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case 100: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mCamera = Camera.open();
                        Camera.Parameters params = mCamera.getParameters();
                        params.setRotation(90);
                        Camera.Size size = getBestPreviewSize(params);
                        params.setPreviewSize(size.width, size.height);
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        mCamera.setParameters(params);
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                        if(takeshots == null)
                            takeshots = new TakeShots();
                        if(timer == null)
                            timer = new Timer();
                        timer.schedule(takeshots, 100, shotInterval);

                    } else {
                        Toast.makeText(getActivity(), "We can not scan QR without CAMERA ACCESS \n Go to Settings and allow access.", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onRequestPermissionsResult", "Exception", e.getMessage());}
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            mCamera = Camera.open();
            Camera.Parameters params = mCamera.getParameters();
            //params.setPreviewSize(sfv_camview.getWidth(), sfv_camview.getHeight());
            params.setRotation(90);
            Camera.Size size = getBestPreviewSize(params);
            params.setPreviewSize(size.width, size.height);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(surfaceHolder);
            if(isCamOn)
                camOperation(true);
            //timer.schedule(takeshots, 100, shotInterval);
        }
        catch (RuntimeException ex){
            requestCameraPermissions();
        }
        catch (Exception e){
            mydb.logAppError(PageName, "surfaceCreated", "Exception", e.getMessage());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            if(mCamera != null)
                camOperation(isCamOn);
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Scan QR");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
            screenChanging = false;
            //mCamera.startPreview();
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            screenChanging = true;
            while (decodingQR) {
                Thread.sleep(100);
            }
            camOperation(false);
        }
        catch (Exception e){mydb.logAppError(PageName, "onPause", "Exception", e.getMessage());}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
