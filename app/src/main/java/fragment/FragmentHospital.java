package fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import shared.DeleteSavedClientData;
import dialog.DialogInformation;
import dialog.DialogProcessingStatus;
import dialog.DialogWaitTime;
import shared.GlobalClass;
import shared.HTTPCallJSon;
import shared.LocationHelper;
import shared.Models;
import shared.NetworkDetector;
import shared.PushNotificationHandler;

/**
 * Created by nviriyala on 19-07-2016.
 */
public class FragmentHospital extends Fragment implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentHospital";
    //private NetworkDetector nd;

    //private ImageView img_doc;
    //private ImageView img_logoprocessing;

    //private RelativeLayout rl_address;
    //private ImageView img_dtlprocessing;

    private LinearLayout ll_businesshours;
    private LinearLayout ll_hospdetails;
    private LinearLayout ll_docdetails;
    private LinearLayout ll_bhwt;
    private LinearLayout ll_status;

    private ImageView img_hosp;
    private ImageView img_doc;
    //private TextView tv_busstitle;
    //private TextView tv_drdetails;

    private TextView btn_scanagain;
    private TextView btn_takeappt;

    public TextView tv_mainappstatus;
    public TextView tv_maintimer;
    public TextView tv_mainapptid;

    //private ImageView img_getmenu_processing;
    private ImageView img_scanagain_processing;

    private String guid = "";
    private GlobalClass gc;
    private CommonClasses cc;

    private String hospitaldetails = "";
    private String hospitalbusinesshours = "";
    private String doctordetails = "";
    private byte[] hospitalimage = null;
    private byte[] doctorimage = null;
    //private String doctorQload = "";
    private String apptID = "";
    private DialogProcessingStatus dps;
    private NetworkDetector nd;
    private LocationHelper lh;
    //boolean himg,hd,dimg,dd,bh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hospital, container, false);
        try {
            mydb = new DBHelper(getActivity());
            gc = (GlobalClass) getActivity().getApplicationContext();
            nd = new NetworkDetector(getActivity());
            cc = new CommonClasses(getActivity());
            guid = mydb.getSystemParameter("HospitalGUID");

            dps = new DialogProcessingStatus(getActivity(), "Bottom");
            dps.show();
            mydb.setSystemParameter("FragmentHospitalNotification", "False");
            hospitaldetails = mydb.getSystemParameter("HospitalDetails");
            hospitalbusinesshours = mydb.getSystemParameter("HospitalBusinessHours");
            hospitalimage = mydb.getEntityImage("Hospital");
            doctorimage = mydb.getEntityImage("Doctor");
            doctordetails = mydb.getSystemParameter("DoctorDetails");
            //doctorQload = mydb.getSystemParameter("DoctorQLoad");
            apptID = mydb.getSystemParameter("DocApptID");

            //himg = hd = dimg = dd = bh = false;


            //Bundle bundle = this.getArguments();
            //guid = bundle.getString("guid");

            img_hosp = (ImageView) rootView.findViewById(R.id.img_hosp);
            img_doc = (ImageView) rootView.findViewById(R.id.img_doc);
            /*img_logoprocessing = (ImageView) rootView.findViewById(R.id.img_logoprocessing);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_logoprocessing);

            rl_address = (RelativeLayout) rootView.findViewById(R.id.rl_address);
            img_dtlprocessing = (ImageView) rootView.findViewById(R.id.img_dtlprocessing);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_dtlprocessing);*/

            ll_businesshours = (LinearLayout) rootView.findViewById(R.id.ll_businesshours);
            ll_hospdetails = (LinearLayout)rootView.findViewById(R.id.ll_hospdetails);
            ll_docdetails = (LinearLayout)rootView.findViewById(R.id.ll_docdetails);
            ll_bhwt = (LinearLayout)rootView.findViewById(R.id.ll_bhwt);
            ll_status = (LinearLayout)rootView.findViewById(R.id.ll_status);


            //tv_busstitle = (TextView) rootView.findViewById(R.id.tv_busstitle);
            //img_bhprocessing = (ImageView) rootView.findViewById(R.id.img_bhprocessing);
            //Glide.with(getActivity()).load(R.drawable.processing).into(img_bhprocessing);

            //tv_drdetails = (TextView) rootView.findViewById(R.id.tv_drdetails);

            btn_scanagain = (TextView) rootView.findViewById(R.id.btn_scanagain);
            btn_takeappt = (TextView) rootView.findViewById(R.id.btn_takeappt);
            btn_takeappt.setOnClickListener(this);
            btn_scanagain.setOnClickListener(this);



            if(!apptID.equalsIgnoreCase("") && !(new JSONObject(apptID).getString("ApptID").equalsIgnoreCase("0")))
                btn_takeappt.setText("Edit Appointment");

            tv_mainappstatus = (TextView) rootView.findViewById(R.id.tv_mainappstatus);
            tv_maintimer = (TextView) rootView.findViewById(R.id.tv_maintimer);
            tv_mainapptid = (TextView) rootView.findViewById(R.id.tv_mainapptid);

            /*if(!mydb.getSystemParameter("DocApptID").equalsIgnoreCase("")){
                tv_mainappstatus.setText("");
                tv_maintimer.setText("");
            }*/

            img_scanagain_processing = (ImageView) rootView.findViewById(R.id.img_scanagain_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_scanagain_processing);
            /*img_getmenu_processing = (ImageView) rootView.findViewById(R.id.img_getmenu_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_getmenu_processing);*/

            if(nd.isInternetAvailable()){
                new getHospitalImage().execute(guid);
                new getHospitalDetails().execute(guid);
                new getBusinessHours().execute(guid);
                new getDoctorImage().execute(guid);
                new getDoctorDetials().execute(guid);
                new getDoctorQueueLoad().execute(guid);
            }
            else {
                dps.dismiss();
                Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreateView", "Exception", e.getMessage());}
        return rootView;
    }

    @Override
    public void onClick(View v) {
        try{
            String tag = "";
            Fragment fragment = null;
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (v.getId()){
                case R.id.btn_takeappt:
                    if(checkCurrentLocation())
                        showTakeApptDialog();
                    break;
                case R.id.btn_scanagain:
                    btn_scanagain.setVisibility(View.GONE);
                    img_scanagain_processing.setVisibility(View.VISIBLE);
                    fragment = new FragmentScanQR();
                    tag = "FragmentScanQR";
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                    fragmentTransaction.replace(R.id.container_body, fragment, tag);
                    fragmentTransaction.commit();
                    break;
                default:
                    break;
            }

        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    private void showTakeApptDialog(){
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        View dialoglayout = layoutInflater.inflate(R.layout.dialog_patientdetails, null);

        Button btn_ok = (Button) dialoglayout.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) dialoglayout.findViewById(R.id.btn_cancel);

        final EditText et_patientname = (EditText) dialoglayout.findViewById(R.id.et_patientname);
        final EditText et_ageyears = (EditText) dialoglayout.findViewById(R.id.et_ageyears);
        final EditText et_agemonths = (EditText) dialoglayout.findViewById(R.id.et_agemonths);
        final EditText et_patientphone = (EditText) dialoglayout.findViewById(R.id.et_patientphone);
        final RadioGroup rg_gender = (RadioGroup) dialoglayout.findViewById(R.id.rg_gender);

        String savedPatientDetails = mydb.getSystemParameter("PatientDetails");
        String savedPatientPhone = mydb.getSystemParameter("PatientPhoneNumber");
        if(!savedPatientDetails.equalsIgnoreCase("")){
            String[] data = savedPatientDetails.split("\\|");
            et_patientname.setText(data[0]);
            et_ageyears.setText(data[1]);
            et_agemonths.setText(data[2]);
            if(data[3].equalsIgnoreCase("1"))
                ((RadioButton)rg_gender.getChildAt(0)).setChecked(true);
            else
                ((RadioButton)rg_gender.getChildAt(1)).setChecked(true);
        }

        if(!savedPatientPhone.equalsIgnoreCase(""))
            et_patientphone.setText(savedPatientPhone);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.show();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = validatePatientDetails(et_patientname.getText().toString(), "Name");
                if(status != null) {
                    //Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                    et_patientname.setError(status);
                    return;
                }

                status = validatePatientDetails(et_ageyears.getText().toString(), "AgeYear");
                if(status != null) {
                    //Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                    et_ageyears.setError(status);
                    return;
                }

                status = validatePatientDetails(et_agemonths.getText().toString(), "AgeMonth");
                if(status != null) {
                    //Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                    et_agemonths.setError(status);
                    return;
                }

                if(et_ageyears.getText().toString().equalsIgnoreCase("") && et_agemonths.getText().toString().equalsIgnoreCase("")){
                    //Toast.makeText(getActivity(), "Please enter age", Toast.LENGTH_SHORT).show();
                    et_agemonths.setError("Fill Age");
                    et_ageyears.setError("Fill Age");
                    return;
                }

                int gender = rg_gender.getCheckedRadioButtonId();
                if(gender == -1){
                    Toast.makeText(getActivity(), "Select Gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                status = validatePatientDetails(et_patientphone.getText().toString(), "Phone");
                if(status != null) {
                    //Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                    et_patientphone.setError(status);
                    return;
                }

                RadioButton rb = (RadioButton) rg_gender.findViewById(gender);
                String gen = rb.getText().toString();
                gender = gen.equalsIgnoreCase("Male")?1:0;
                String[] push = {guid, et_patientname.getText().toString(), et_ageyears.getText().toString(), et_agemonths.getText().toString(), String.valueOf(gender), et_patientphone.getText().toString()};
                mydb.setSystemParameter("PatientDetails", push[1]+"|"+push[2]+"|"+push[3]+"|"+push[4]);
                mydb.setSystemParameter("PatientPhoneNumber", push[5]);
                if(nd.isInternetAvailable())
                    new takeAppointment().execute(push);
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showTakeApptPopup(){
        try{
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.dialog_patientdetails, null);
            final PopupWindow popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
            Button btn_ok = (Button) popupView.findViewById(R.id.btn_ok);
            Button btn_cancel = (Button) popupView.findViewById(R.id.btn_cancel);

            final EditText et_patientname = (EditText) popupView.findViewById(R.id.et_patientname);
            final EditText et_ageyears = (EditText) popupView.findViewById(R.id.et_ageyears);
            final EditText et_agemonths = (EditText) popupView.findViewById(R.id.et_agemonths);

            final RadioGroup rg_gender = (RadioGroup) popupView.findViewById(R.id.rg_gender);

            String savedPatientDetails = mydb.getSystemParameter("PatientDetails");
            if(!savedPatientDetails.equalsIgnoreCase("")){
                String[] data = savedPatientDetails.split("\\|");
                et_patientname.setText(data[0]);
                et_ageyears.setText(data[1]);
                et_agemonths.setText(data[2]);
                if(data[3].equalsIgnoreCase("1"))
                    ((RadioButton)rg_gender.getChildAt(0)).setChecked(true);
                else
                    ((RadioButton)rg_gender.getChildAt(1)).setChecked(true);
            }




            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check the location and then take appointment
                    /*ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.colorPrimary)), new ColorDrawable(getResources().getColor(R.color.white))};
                    TransitionDrawable trans = new TransitionDrawable(color);
                    tv_appstatus.setBackgroundDrawable(trans);
                    trans.startTransition(5000);*/
                    String status = validatePatientDetails(et_patientname.getText().toString(), "Name");
                    if(status != null) {
                        Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                        //et_patientname.setError(status);
                        return;
                    }

                    status = validatePatientDetails(et_ageyears.getText().toString(), "AgeYear");
                    if(status != null) {
                        Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                        //et_ageyears.setError(status);
                        return;
                    }

                    status = validatePatientDetails(et_agemonths.getText().toString(), "AgeMonth");
                    if(status != null) {
                        Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                        //et_agemonths.setError(status);
                        return;
                    }

                    if(et_ageyears.getText().toString().equalsIgnoreCase("") && et_agemonths.getText().toString().equalsIgnoreCase("")){
                        Toast.makeText(getActivity(), "Please enter age", Toast.LENGTH_SHORT).show();
                        //et_agemonths.setError(status);
                        return;
                    }

                    int gender = rg_gender.getCheckedRadioButtonId();
                    if(gender == -1){
                        Toast.makeText(getActivity(), "Select Gender", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RadioButton rb = (RadioButton) rg_gender.findViewById(gender);
                    String gen = rb.getText().toString();
                    gender = gen.equalsIgnoreCase("Male")?1:0;
                    String[] push = {guid, et_patientname.getText().toString(), et_ageyears.getText().toString(), et_agemonths.getText().toString(), String.valueOf(gender)};
                    mydb.setSystemParameter("PatientDetails", push[1]+"|"+push[2]+"|"+push[3]+"|"+push[4]);
                    if(nd.isInternetAvailable())
                        new takeAppointment().execute(push);
                    else
                        Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            });
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.colorPrimary)), new ColorDrawable(getResources().getColor(R.color.white))};
                    TransitionDrawable trans = new TransitionDrawable(color);
                    tv_appstatus.setBackgroundDrawable(trans);
                    trans.startTransition(5000);*/
                    popupWindow.dismiss();
                }
            });
            popupWindow.setAnimationStyle(R.style.DialogAnimation);
            popupWindow.showAtLocation(popupView, Gravity.TOP, 0, 100);
        }
        catch (Exception e){mydb.logAppError(PageName, "showTakeApptPopup", "Exception", e.getMessage());}
    }

    public String validatePatientDetails(String text, String type){
        String retValue = null;
        try{
            switch (type){
                case "Name":
                    if(text.equalsIgnoreCase("") || text == null)
                        return "Empty Name";
                    for (char c: text.toCharArray()) {
                        if(Character.isDigit(c))
                            return "Name has numeric values";
                        else{
                            Pattern pattern = Pattern.compile("^[A-Za-z\\s]$");
                            Matcher matcher = pattern.matcher(c+"");
                            if(!matcher.matches())
                                return "Name has special characters";
                        }
                    }
                    break;
                case "AgeYear":
                    if(text.equalsIgnoreCase(""))
                        return null;

                    if(!cc.isInteger(text))
                        return "Age invalid value";
                    int ageyear = Integer.parseInt(text);
                    if(ageyear < 0 || ageyear > 125)
                        return "years should be between 0 and 125";
                    break;
                case "AgeMonth":
                    if(text.equalsIgnoreCase(""))
                        return null;
                    if(!cc.isInteger(text))
                        return "Age invalid value";
                    int agemonth = Integer.parseInt(text);
                    if(agemonth < 0 || agemonth > 12)
                        return "months should be between 0 and 12";
                    break;
                case "Phone":
                    for (char c: text.toCharArray()) {
                        if(!Character.isDigit(c))
                            return "Phone number contains non numeric value(s)";
                    }
                    if(text.length() < 10 || text.length() > 12)
                        return "Not a valid phone number";
                    if(text.length() == 10 && text.charAt(0) == '0')
                        return "Not a valid phone number";
                    if(text.length() == 11 && text.charAt(0) != '0')
                        return "Not a valid phone number";
                    if(text.length() == 12 && !(text.substring(0, 2).equalsIgnoreCase("91")))
                        return "Not a valid phone number";
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validatePatientDetails", "Exception", e.getMessage());}
        return retValue;
    }

    public class getHospitalImage extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //byte[] bytes = mydb.getEntityImage("Hospital");
            Bitmap bitmap = null;
            try {
                if(hospitalimage != null)
                    bitmap = cc.getBitmap(hospitalimage);
                else {
                    //bitmap = new HTTPCallJSon(getActivity()).GetImage("GetEntityImage", "?guid=" + params[0]+"&type=entity");
                    //mydb.setEntityImage("Hospital", bitmap);

                    String response = new HTTPCallJSon(getActivity()).Get("GetEntityImage", "?id="+params[0]);
                    InputStream in = new java.net.URL(response).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    mydb.setEntityImage("Hospital", bitmap);
                }
            }
            catch (Exception e){
                mydb.logAppError(PageName, "getHospitalImage--doInBackground", "Exception", e.getMessage());
                mydb.setEntityImage("Hospital", null);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try {
                if(bitmap == null) {
                    //Toast.makeText(getActivity(), "Image is null", Toast.LENGTH_SHORT).show();
                    //img_logoprocessing.setVisibility(View.GONE);
                    ll_hospdetails.setVisibility(View.VISIBLE);
                    img_hosp.setImageResource(R.drawable.ic_noimage);
                }
                else {
                    //img_logoprocessing.setVisibility(View.GONE);
                    ll_hospdetails.setVisibility(View.VISIBLE);
                    img_hosp.setImageBitmap(bitmap);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getHospitalImage--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getHospitalDetails extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //String response = mydb.getSystemParameter("HospitalDetails");
            //response = "";
            try {
                if(hospitaldetails.equalsIgnoreCase("")) {
                    hospitaldetails = new HTTPCallJSon(getActivity()).Get("GetEntityDetails", "?id=" + params[0]);
                    hospitaldetails = new JSONArray(hospitaldetails).getJSONObject(0).toString();
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getHospitalDetails--doInBackground", "Exception", e.getMessage());}
            return hospitaldetails;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {
                JSONObject jobj = new JSONObject(response);
                String entityDetails = "<b>"+jobj.getString("Ename")+"</b><br/>";
                entityDetails = entityDetails + "<small>"+jobj.getString("Add1")+"</small>";
                if(!jobj.getString("Add2").equalsIgnoreCase(""))
                    entityDetails = entityDetails + "<br/><small>"+jobj.getString("Add2")+"</small>";

                entityDetails = entityDetails + "<br/><small>"+jobj.getString("City")+", "+jobj.getString("State")+", "+jobj.getString("Country")+",<br/> Pin:"+jobj.getString("Zip")+"</small>";
                if(!jobj.getString("ContactNo1").equalsIgnoreCase(""))
                    entityDetails = entityDetails + "<br/><small>"+jobj.getString("ContactNo1")+"</small>";

                if(!jobj.getString("ContactNo2").equalsIgnoreCase(""))
                    entityDetails = entityDetails + "<small>, "+jobj.getString("ContactNo2")+"</small>";

                if(!jobj.getString("EmailID").equalsIgnoreCase(""))
                    entityDetails = entityDetails + "<br/><small><a href = '"+jobj.getString("EmailID")+"'>"+jobj.getString("EmailID")+"</a></small>";

                if(!jobj.getString("WebSite").equalsIgnoreCase(""))
                    entityDetails = entityDetails + "<br/><small><a href = "+jobj.getString("WebSite")+">"+jobj.getString("WebSite")+"</a></small>";

                Spanned textDecoration = Html.fromHtml(entityDetails);
                TextView tv_hospdetails = (TextView) ll_hospdetails.findViewById(R.id.tv_hospdetails);
                tv_hospdetails.setLinksClickable(true);
                tv_hospdetails.setMovementMethod(LinkMovementMethod.getInstance());
                tv_hospdetails.setText(textDecoration);
                //img_dtlprocessing.setVisibility(View.GONE);
                ll_hospdetails.setVisibility(View.VISIBLE);
                mydb.setSystemParameter("HospitalDetails", response);

            }
            catch (Exception e){mydb.logAppError(PageName, "getHospitalDetails--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getBusinessHours extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //String response = mydb.getSystemParameter("HospitalBusinessHours");
            //response = "";
            try {
                if(hospitalbusinesshours.equalsIgnoreCase(""))
                    hospitalbusinesshours = new HTTPCallJSon(getActivity()).Get("GetBusinessHours", "?id=" + params[0]);
            }
            catch (Exception e){mydb.logAppError(PageName, "getBusinessHours--doInBackground", "Exception", e.getMessage());}
            return hospitalbusinesshours;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                TableLayout tableLayout = new TableLayout(getActivity());
                TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                LayoutInflater inflater = getActivity().getLayoutInflater();
                for(int i = 0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    TableRow row = (TableRow)inflater.inflate(R.layout.row_business_hours, tableLayout, false);

                    TextView tv_day = (TextView) row.findViewById(R.id.tv_day);
                    tv_day.setText(jsonObject.getString("Day")+"   ");

                    TextView tv_bh = (TextView) row.findViewById(R.id.tv_bh);
                    tv_bh.setText(jsonObject.getString("BH"));


                    if(i != jsonArray.length()-1)
                        layoutParams.setMargins(0,0,0,1);
                    tableLayout.addView(row, layoutParams);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;

                if(jsonArray.length() < 4) {
                    LinearLayout.LayoutParams llayoutParams = new LinearLayout.LayoutParams(180, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llayoutParams.setMargins(0,0,1,0);
                    ll_status.setLayoutParams(llayoutParams);
                }


                //ll_businesshours.addView(tableLayout, params);
                ll_businesshours.addView(tableLayout);
                ll_bhwt.setVisibility(View.VISIBLE);
                mydb.setSystemParameter("HospitalBusinessHours", response);

            }
            catch (Exception e){mydb.logAppError(PageName, "getBusinessHours--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getDoctorDetials extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            try {
                //doctordetails = "";
                if(doctordetails.equalsIgnoreCase("")) {
                    doctordetails = new HTTPCallJSon(getActivity()).Get("GetDoctorDetails", "?id=" + params[0]);
                    doctordetails = new JSONArray(doctordetails).getJSONObject(0).toString();
                    //doctordetails = "{\"Dname\":\"Dr. Rama Mani\",\"Deg\":\"MBBS FRCS\",\"Spec\":\"Orthopedic\",\"Fee\":\"200\",\"Act\":\"14\",\"mobile\":\"9986521458\",\"land\":\"040 44589632\",\"email\":\"ramamani@gmail.com\",\"remarks\":\"\"}";
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorDetials--doInBackground", "Exception", e.getMessage());}
            return doctordetails;
        }

        @Override
        protected void onPostExecute(String response) {
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {
                JSONObject jobj = new JSONObject(response);
                String doctor = "<b>"+jobj.getString("Dname")+"</b>";
                doctor = doctor + "<small> "+jobj.getString("Deg")+"</small><br/>";
                doctor = doctor + "<small><u>Specialty</u>: "+jobj.getString("Spec")+"</small><br/>";
                doctor = doctor + "<small><u>Consulation Fee</u>: Rs."+jobj.getString("Fee")+"</small><br/>";
                doctor = doctor + "<small><u>Avg consultation time</u>: "+jobj.getString("Act")+" minutes</small>";
                if(!jobj.getString("Mobile").equalsIgnoreCase("null"))
                    doctor = doctor + "<br/><small><u>Mobile</u>: "+jobj.getString("Mobile")+"</small>";
                if(!jobj.getString("Land").equalsIgnoreCase("null"))
                    doctor = doctor + "<br/><small><u>Work</u>: "+jobj.getString("Land")+"</small>";
                if(!jobj.getString("email").equalsIgnoreCase("null"))
                    doctor = doctor + "<br/><small><a href = '"+jobj.getString("email")+"'>"+jobj.getString("email")+"</a></small>";
                if(!jobj.getString("remarks").equalsIgnoreCase("null"))
                    doctor = doctor + "<br/><small>"+jobj.getString("remarks")+"</small>";


                Spanned textDecoration = Html.fromHtml(doctor);
                TextView tv_docdetails = (TextView) ll_docdetails.findViewById(R.id.tv_docdetails);
                tv_docdetails.setLinksClickable(true);
                tv_docdetails.setMovementMethod(LinkMovementMethod.getInstance());
                tv_docdetails.setText(textDecoration);

                /*tv_mainappstatus.setText(jobj.getString("Qid"));
                tv_maintimer.setText(jobj.getString("Awt"));*/


                ll_docdetails.setVisibility(View.VISIBLE);
                mydb.setSystemParameter("DoctorDetails", response);
                dps.dismiss();
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorDetials--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getDoctorImage extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //byte[] bytes = mydb.getEntityImage("Hospital");
            Bitmap bitmap = null;
            try {
                if(doctorimage != null)
                    bitmap = cc.getBitmap(doctorimage);
                else {
                    //bitmap = new HTTPCallJSon(getActivity()).GetImage("GetEntityImage", "?guid=" + params[0]+"&type=Doctor");
                    //bitmap = null;
                    //mydb.setEntityImage("Doctor", bitmap);

                    String response = new HTTPCallJSon(getActivity()).Get("GetDoctorImage", "?id="+params[0]);
                    InputStream in = new java.net.URL(response).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    mydb.setEntityImage("Doctor", bitmap);
                }
            }
            catch (Exception e){
                mydb.logAppError(PageName, "getDoctorImage--doInBackground", "Exception", e.getMessage());
                mydb.setEntityImage("Doctor", null);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try {
                ll_docdetails.setVisibility(View.VISIBLE);
                if(bitmap == null)
                    img_doc.setImageResource(R.drawable.ic_noimage);
                else
                    img_doc.setImageBitmap(bitmap);

            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorImage--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getDoctorQueueLoad extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            try {
                if(apptID.equalsIgnoreCase("")) {
                    apptID = new HTTPCallJSon(getActivity()).Get("GetDoctorQLoad", "?id=" + params[0]);
                    apptID = new JSONArray(apptID).getJSONObject(0).toString();
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getDoctorQueueLoad--doInBackground", "Exception", e.getMessage());}
            return apptID;
        }

        @Override
        protected void onPostExecute(String response) {
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {

                JSONObject jobj = new JSONObject(response);
                if(jobj.getString("ApptTime").equalsIgnoreCase("null"))
                    jobj.put("ApptTime", new Models.TimeStamp().getCurrentTimeStamp());
                tv_mainappstatus.setText(jobj.getString("Qcnt"));
                tv_maintimer.setText(jobj.getString("AWT"));
                tv_mainapptid.setText(jobj.getString("ApptID"));
                mydb.setSystemParameter("DocApptID", jobj.toString());
                ll_businesshours.setVisibility(View.VISIBLE);
                if(!jobj.getString("AWT").equalsIgnoreCase("0"))
                    setWaitTimer();
            }
            catch (Exception e){
                mydb.setSystemParameter("DocApptID", "");
                mydb.logAppError(PageName, "getDoctorDetials--onPostExecute", "Exception", e.getMessage());
            }
        }
    }

    public void setWaitTimer(){
        try{
            JSONObject jobj = new JSONObject(mydb.getSystemParameter("DocApptID"));
            String timertext = jobj.getString("ApptTime").replace("T", " ").substring(0,19);
            String timer = cc.getTimerValue(timertext, Integer.parseInt(jobj.getString("AWT")));
            tv_maintimer.setText(timer);
            tv_mainappstatus.setText(jobj.getString("Qcnt"));
            tv_mainapptid.setText(jobj.getString("ApptID"));
            long fullTime = cc.StringToMilli(timer);
            CountDownTimer cdt = new CountDownTimer(fullTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String timerText = tv_maintimer.getText().toString();
                    long inTime = cc.StringToMilli(timerText);
                    if(inTime <= 0)
                        this.cancel();
                    timerText = cc.MilliToString(inTime-1000);
                    tv_maintimer.setText(timerText);
                }

                @Override
                public void onFinish() {

                }
            };
            cdt.start();
        }
        catch (Exception e){mydb.logAppError(PageName, "setWaitTimer", "Exception", e.getMessage());}
    }

    public class takeAppointment extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                JSONObject jparams = new JSONObject();
                jparams.put("DocGuid", params[0]);
                jparams.put("PatientName", params[1]);
                jparams.put("AgeYear", params[2].equalsIgnoreCase("")?0:params[2]);
                jparams.put("AgeMonth", params[3].equalsIgnoreCase("")?0:params[3]);
                jparams.put("Gender", params[4]);
                jparams.put("PatientPhone", params[5]);
                jparams.put("PatientDeviceID", mydb.getDeviceID());
                String ApptID = mydb.getSystemParameter("DocApptID");
                ApptID = ApptID.equalsIgnoreCase("")?"0":new JSONObject(ApptID).getString("ApptID");
                jparams.put("CurrApptID", ApptID);
                response = new HTTPCallJSon(getActivity()).Get("AddDoctorAppointment", "?json="+jparams.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "takeAppointment--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                response = new JSONArray(response).getJSONObject(0).toString();
                JSONObject json = new JSONObject(response);
                json.put("ApptTime", new  Models.TimeStamp().getCurrentTimeStamp());
                String timertext = json.getString("ApptTime").replace("T", " ").substring(0,19);
                String timer = cc.getTimerValue(timertext, Integer.parseInt(json.getString("AWT")));
                tv_mainappstatus.setText(json.getString("Qcnt"));
                tv_mainapptid.setText(json.getString("ApptID"));
                tv_maintimer.setText(timer);
                mydb.setSystemParameter("DocApptID", json.toString());
                if(json.getString("Qcnt").equalsIgnoreCase("0"))
                    new DialogInformation(getActivity(), Html.fromHtml("Please step in<br/>It's your turn now"), "Doctor").show();
                else if (json.getString("Qcnt").equalsIgnoreCase("-1")){
                    new DialogInformation(getActivity(), Html.fromHtml("This appointment is closed.<br/>Please create a new one."), "Doctor").show();
                    mydb.setSystemParameter("DocApptID", "");
                    new DeleteSavedClientData(getActivity()).Delete("Hospital");
                    btn_scanagain.performClick();
                }
                else {
                    new DialogWaitTime(getActivity()).show();
                }
                new PushNotification(getActivity()).execute();
                btn_takeappt.setText("Edit Appointment");
                //mydb.setSystemParameter("DocApptID", json.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "takeAppointment--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public boolean checkCurrentLocation(){
        try {
            lh = new LocationHelper(getActivity());
            if (lh.canGetLocation()) { //GPS is enabled
                double longitude = lh.getLongitude();
                double latitude = lh.getLatitude();
                if (longitude == 0.0 || latitude == 0.0) { //location services for this app is not allowed
                    requestLocationPermission();
                    return false;
                } else {//GPS is enables and location services for this app is allowed
                    //check for distance and then place order
                    JSONObject json = new JSONObject(mydb.getSystemParameter("HospitalDetails"));
                    double entityLat = json.getDouble("Lat");
                    double entitLong = json.getDouble("Long");
                    int distanceallowed = json.getInt("PD");
                    float distance = cc.calculateDistance(latitude, longitude, entityLat, entitLong);

                    if(distance > distanceallowed){
                        //DialogRestOrderPlaced drop = new DialogRestOrderPlaced(getActivity(), "We don't find you inside our hospital.", "You can not take appointment now");
                        DialogInformation di = new DialogInformation(getActivity(),Html.fromHtml("We don't find you inside our hospital.<br/>You can not take appointment now"),"Doctor");
                        di.show();
                        return false;
                    }
                    else{
                        return true;
                    }
                }
            } else { //GPS is not enabled
                Toast.makeText(getActivity(), "Please enable Location service", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "checkCurrentLocation", "Exception", e.getMessage());
            return false;
        }
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case 500:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        checkCurrentLocation();
                    else
                        Toast.makeText(getActivity(), "Sorry, We can not make appointment without accessing your location!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onRequestPermissionsResult", "Exception", e.getMessage());}
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            //Situation where the user has taken the appointment, while in FragmentHospital
            //user clicked the home button, A notification is received that the appointment is closed\cancelled
            //all the data is deleted(like hospitalID, DocGuid, Appt details e.t.c) behind the scenes
            //If the user now opens the app (the app will open directly FragmentHospital) screen and hit the edit appointment screen
            //since there are not detials an error will occur, hence we are redirecting the user to ScanQR screen
            guid = mydb.getSystemParameter("HospitalGUID");
            if(guid.equalsIgnoreCase(""))
                btn_scanagain.performClick();

            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Hospital");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}