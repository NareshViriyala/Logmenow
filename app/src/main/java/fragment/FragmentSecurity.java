package fragment;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DBHelper;
import dialog.DialogQRImage;
import shared.CommonClasses;
import shared.HTTPCallJSon;
import shared.Models;
import shared.NetworkDetector;


/**
 * Created by nviriyala on 18-01-2017.
 */
public class FragmentSecurity extends Fragment implements View.OnClickListener{
    private String PageName = "FragmentSecurity";
    private DBHelper mydb;
    private String guid = "";
    private NetworkDetector nd;
    private LinearLayout ll_socdetails, ll_qrinfo;
    private ImageView img_soc;

    private EditText et_name, et_email, et_phone, et_dateofbirth, et_age, et_gender, et_vehicle, et_comingfrom, et_purposeofvisit, et_visitingcompany, et_meetingperson, et_block, et_flat;
    private ImageView img_cal, img_male, img_female, img_wheeler2, img_wheeler3, img_wheeler4, img_wheeler0, img_povo, img_fooddelivery, img_postaldelivery, img_interview, img_pvisit, img_ovisit, img_other;
    private GridLayout gl_visitoptions;
    private Button btn_savegenqr;

    private CommonClasses cc;
    private JSONArray jsonarray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_security, container, false);
        try {
            mydb = new DBHelper(getActivity());
            nd = new NetworkDetector(getActivity());
            cc = new CommonClasses(getActivity());
            guid = mydb.getSystemParameter("SecurityGUID");
//            if(guid.equalsIgnoreCase("D0162440-7129-11E6-9022-00155DA81418"))
//                guid = "8";
//            else
//                guid = "9";
            String[] push = {guid, mydb.getDeviceID()};
            ll_socdetails = (LinearLayout)rootView.findViewById(R.id.ll_socdetails);
            ll_qrinfo = (LinearLayout)rootView.findViewById(R.id.ll_qrinfo);
            img_soc = (ImageView) rootView.findViewById(R.id.img_soc);

            if(nd.isInternetAvailable()){
                new getEntityImage().execute(guid);
                new getEntityDetails().execute(guid);
                new getSecurityParameters().execute(push);
            }
            else {
                Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public class getEntityImage extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            Bitmap entityimagebitmap = null;
            try {
                response = new HTTPCallJSon(getActivity()).Get("GetEntityImage", "?id="+params[0]);
                InputStream in = new java.net.URL(response).openStream();
                entityimagebitmap = BitmapFactory.decodeStream(in);
            }
            catch (Exception e){mydb.logAppError(PageName, "getEntityImage--doInBackground", "Exception", e.getMessage());}
            return entityimagebitmap;
        }

        @Override
        protected void onPostExecute(Bitmap entityimagebitmap) {
            super.onPostExecute(entityimagebitmap);
            try{
                ll_socdetails.setVisibility(View.VISIBLE);
                if(entityimagebitmap == null)
                    img_soc.setImageResource(R.drawable.ic_noimage);
                else
                    img_soc.setImageBitmap(entityimagebitmap);
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityParameters--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getEntityDetails extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                response = new HTTPCallJSon(getActivity()).Get("GetEntityDetails", "?id="+params[0]);
            }
            catch (Exception e){mydb.logAppError(PageName, "getEntityDetails--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                JSONObject jobj = new JSONArray(response).getJSONObject(0);
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
                TextView tv_hospdetails = (TextView) ll_socdetails.findViewById(R.id.tv_socdetails);
                tv_hospdetails.setLinksClickable(true);
                tv_hospdetails.setMovementMethod(LinkMovementMethod.getInstance());
                tv_hospdetails.setText(textDecoration);
                //img_dtlprocessing.setVisibility(View.GONE);
                ll_socdetails.setVisibility(View.VISIBLE);
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityParameters--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getSecurityParameters extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                JSONObject jparams = new JSONObject();
                jparams.put("EntityID", params[0]);
                jparams.put("DeviceID", params[1]);
                response = new HTTPCallJSon(getActivity()).Get("GetEntitySCEParam", "?json="+jparams.toString());
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityParameters--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                jsonarray = new JSONArray(response);
                renderScreenWithParameters();
            }
            catch (Exception e){mydb.logAppError(PageName, "getSecurityParameters--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public void renderScreenWithParameters(){
        try{
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.component_qrparameters,null);
            ll_qrinfo.addView(view);

            btn_savegenqr = (Button)view.findViewById(R.id.btn_savegenqr);
            btn_savegenqr.setOnClickListener(FragmentSecurity.this);

            /*for(int i = 0; i<jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int id = getResources().getIdentifier("vwgrp_"+item.getString("ParamType").replaceAll(" ","").toLowerCase(), "id", getActivity().getPackageName());
                if(id != 0) {
                    View par = view.findViewById(id);
                    par.setVisibility(View.VISIBLE);
                }
            }*/

            for(int i = 0; i<jsonarray.length(); i++) {
                JSONObject item = jsonarray.getJSONObject(i);
                int id = getResources().getIdentifier("vwgrp_"+item.getString("ParamType").replaceAll(" ","").toLowerCase(), "id", getActivity().getPackageName());
                if(id != 0) {
                    View par = view.findViewById(id);
                    par.setVisibility(View.VISIBLE);
                }
                switch (item.getString("ParamType")){
                    case "Name":
                        et_name = (EditText)view.findViewById(R.id.et_name);
                        Models.CustomInfoItem svdname = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdname.getInfoValue().equalsIgnoreCase("")) {
                            et_name.setText(svdname.getInfoValue());
                        }
                        break;
                    case "Email":
                        et_email = (EditText)view.findViewById(R.id.et_email);
                        Models.CustomInfoItem svdmail = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdmail.getInfoValue().equalsIgnoreCase("")) {
                            et_email.setText(svdmail.getInfoValue());
                        }
                        break;
                    case "Phone":
                        et_phone = (EditText)view.findViewById(R.id.et_phone);
                        Models.CustomInfoItem svdphone = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdphone.getInfoValue().equalsIgnoreCase("")) {
                            et_phone.setText(svdphone.getInfoValue());
                        }
                        break;
                    case "Date of Birth":
                        et_dateofbirth = (EditText)view.findViewById(R.id.et_dateofbirth);
                        img_cal = (ImageView)view.findViewById(R.id.img_cal);
                        img_cal.setOnClickListener(FragmentSecurity.this);
                        Models.CustomInfoItem svddob = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svddob.getInfoValue().equalsIgnoreCase("")) {
                            et_dateofbirth.setText(svddob.getInfoValue());
                        }
                        break;
                    case "Age":
                        et_age = (EditText)view.findViewById(R.id.et_age);
                        Models.CustomInfoItem svdage = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdage.getInfoValue().equalsIgnoreCase("")) {
                            et_age.setText(svdage.getInfoValue());
                        }
                        break;
                    case "Gender":
                        et_gender = (EditText)view.findViewById(R.id.et_gender);
                        img_male = (ImageView)view.findViewById(R.id.img_male);
                        img_female = (ImageView)view.findViewById(R.id.img_female);
                        img_male.setOnClickListener(FragmentSecurity.this);
                        img_female.setOnClickListener(FragmentSecurity.this);
                        Models.CustomInfoItem svdgender = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdgender.getInfoValue().equalsIgnoreCase("")) {
                            et_gender.setText(svdgender.getInfoValue());
                        }
                        break;
                    case "Vehicle No":
                        et_vehicle = (EditText)view.findViewById(R.id.et_vehicle);
                        img_wheeler2 = (ImageView)view.findViewById(R.id.img_wheeler2);
                        img_wheeler3 = (ImageView)view.findViewById(R.id.img_wheeler3);
                        img_wheeler4 = (ImageView)view.findViewById(R.id.img_wheeler4);
                        img_wheeler0 = (ImageView)view.findViewById(R.id.img_wheeler0);

                        img_wheeler2.setOnClickListener(FragmentSecurity.this);
                        img_wheeler3.setOnClickListener(FragmentSecurity.this);
                        img_wheeler4.setOnClickListener(FragmentSecurity.this);
                        img_wheeler0.setOnClickListener(FragmentSecurity.this);

                        Models.CustomInfoItem svdveh = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdveh.getInfoValue().equalsIgnoreCase("")) {
                            String vh = svdveh.getInfoValue().substring(0,1);
                            vh = "("+vh+"W)-"+svdveh.getInfoValue().substring(1);
                            et_vehicle.setText(vh);
                        }
                        break;
                    case "Coming from":
                        et_comingfrom = (EditText)view.findViewById(R.id.et_comingfrom);
                        Models.CustomInfoItem svdcmg = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdcmg.getInfoValue().equalsIgnoreCase("")) {
                            et_comingfrom.setText(svdcmg.getInfoValue());
                        }
                        break;
                    case "Purpose of visit":
                        et_purposeofvisit = (EditText)view.findViewById(R.id.et_purposeofvisit);
                        gl_visitoptions = (GridLayout)view.findViewById(R.id.gl_visitoptions);

                        img_povo = (ImageView)view.findViewById(R.id.img_povo);
                        img_fooddelivery = (ImageView)view.findViewById(R.id.img_fooddelivery);
                        img_postaldelivery = (ImageView)view.findViewById(R.id.img_postaldelivery);
                        img_interview = (ImageView)view.findViewById(R.id.img_interview);
                        img_pvisit = (ImageView)view.findViewById(R.id.img_pvisit);
                        img_ovisit = (ImageView)view.findViewById(R.id.img_ovisit);
                        img_other = (ImageView)view.findViewById(R.id.img_other);

                        img_povo.setOnClickListener(FragmentSecurity.this);
                        img_fooddelivery.setOnClickListener(FragmentSecurity.this);
                        img_postaldelivery.setOnClickListener(FragmentSecurity.this);
                        img_interview.setOnClickListener(FragmentSecurity.this);
                        img_pvisit.setOnClickListener(FragmentSecurity.this);
                        img_ovisit.setOnClickListener(FragmentSecurity.this);
                        img_other.setOnClickListener(FragmentSecurity.this);

                        /*Models.CustomInfoItem svdpov = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdpov.getInfoValue().equalsIgnoreCase("")) {
                            et_purposeofvisit.setText(svdpov.getInfoValue());
                        }*/
                        break;
                    case "Visiting Company":
                        et_visitingcompany = (EditText)view.findViewById(R.id.et_visitingcompany);
                        Models.CustomInfoItem svdvc = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdvc.getInfoValue().equalsIgnoreCase("")) {
                            et_visitingcompany.setText(svdvc.getInfoValue());
                        }
                        break;
                    case "Meeting person":
                        et_meetingperson = (EditText)view.findViewById(R.id.et_meetingperson);
                        Models.CustomInfoItem svdmp = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdmp.getInfoValue().equalsIgnoreCase("")) {
                            et_meetingperson.setText(svdmp.getInfoValue());
                        }
                        break;
                    case "Block":
                        et_block = (EditText)view.findViewById(R.id.et_block);
                        //List<Models.CustomInfoItem> alld = mydb.getCustomInfoItems(false);
                        Models.CustomInfoItem svdblk = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdblk.getInfoValue().equalsIgnoreCase("")) {
                            et_block.setText(svdblk.getInfoValue());
                        }
                        break;
                    case "Flat":
                        et_flat = (EditText)view.findViewById(R.id.et_flat);
                        Models.CustomInfoItem svdflt = mydb.getCustomInfoItem(item.getString("ParamType"));
                        if(!svdflt.getInfoValue().equalsIgnoreCase("")) {
                            et_flat.setText(svdflt.getInfoValue());
                        }
                        break;
                    default:
                        break;
                };
            }


        }
        catch (Exception e){mydb.logAppError(PageName, "renderScreenWithParameters", "Exception", e.getMessage());}
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            selectedMonth++;
            String month = selectedMonth<10?"0"+selectedMonth:String.valueOf(selectedMonth);
            String day = selectedDay<10?"0"+selectedDay:String.valueOf(selectedDay);
            et_dateofbirth.setText(day+"/"+month+"/"+selectedYear);
        }
    };


    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()) {
                case R.id.img_cal:
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    //new DatePickerDialog(getActivity(),datePickerListener,mYear,mMonth,mDay).show();
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(), datePickerListener,mYear,mMonth,mDay);
                    dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    dialog.show();
                    break;
                case R.id.img_male:
                    et_gender.setText("Male");
                    break;
                case R.id.img_female:
                    et_gender.setText("Female");
                    break;
                case R.id.img_wheeler2:
                    if(et_vehicle.getText().toString().equalsIgnoreCase(""))
                        et_vehicle.setText("(2W)-");
                    else if(et_vehicle.getText().toString().substring(0,1).equalsIgnoreCase("(")){
                        et_vehicle.setText("(2W)-" + et_vehicle.getText().toString().substring(5));
                    }else {
                        et_vehicle.setText("(2W)-" + et_vehicle.getText());
                    }
                    break;
                case R.id.img_wheeler3:
                    if(et_vehicle.getText().toString().equalsIgnoreCase(""))
                        et_vehicle.setText("(3W)-");
                    else if(et_vehicle.getText().toString().substring(0,1).equalsIgnoreCase("(")){
                        et_vehicle.setText("(3W)-" + et_vehicle.getText().toString().substring(5));
                    }else {
                        et_vehicle.setText("(3W)-" + et_vehicle.getText());
                    }
                    break;
                case R.id.img_wheeler4:
                    if(et_vehicle.getText().toString().equalsIgnoreCase(""))
                        et_vehicle.setText("(4W)-");
                    else if(et_vehicle.getText().toString().substring(0,1).equalsIgnoreCase("(")){
                        et_vehicle.setText("(4W)-" + et_vehicle.getText().toString().substring(5));
                    }else {
                        et_vehicle.setText("(4W)-" + et_vehicle.getText());
                    }
                    break;
                case R.id.img_wheeler0:
                    if(et_vehicle.getText().toString().equalsIgnoreCase(""))
                        et_vehicle.setText("(0W)-");
                    else if(et_vehicle.getText().toString().substring(0,1).equalsIgnoreCase("(")){
                        et_vehicle.setText("(0W)-" + et_vehicle.getText().toString().substring(5));
                    }else {
                        et_vehicle.setText("(0W)-" + et_vehicle.getText());
                    }
                    break;
                case R.id.img_povo:
                    if(gl_visitoptions.getVisibility() == View.VISIBLE){
                        img_povo.setImageResource(R.drawable.ic_qtym);
                        gl_visitoptions.setVisibility(View.GONE);
                    }else {
                        img_povo.setImageResource(R.drawable.ic_qtyp);
                        gl_visitoptions.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.img_fooddelivery:
                    et_purposeofvisit.setText("Food delivery");
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.img_postaldelivery:
                    et_purposeofvisit.setText("Postal\\Courier delivery");
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.img_interview:
                    et_purposeofvisit.setText("Interview");
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.img_pvisit:
                    et_purposeofvisit.setText("Personal visit");
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.img_ovisit:
                    et_purposeofvisit.setText("Officical visit");
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.img_other:
                    et_purposeofvisit.setText("");
                    et_purposeofvisit.requestFocus();
                    gl_visitoptions.setVisibility(View.GONE);
                    img_povo.setImageResource(R.drawable.ic_qtym);
                    break;
                case R.id.btn_savegenqr:
                    saveAndGenerateQR();
                    break;
                default:
                    break;
            };
        }catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public void saveAndGenerateQR(){
        try{
            boolean founderror = false;
            String error = null;
            String QRText = "0."+mydb.getDeviceID();
            for(int i = 0; i<jsonarray.length(); i++) {
                JSONObject item = jsonarray.getJSONObject(i);
                error = null;
                switch (item.getString("ParamType")){
                    case "Name":
                        error = validateText(et_name.getText().toString(), "Name");
                        if(error != null) {
                            et_name.setError(error);
                            founderror = true;
                        }else {
                            et_name.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_name.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_name.getText().toString();
                        }
                        break;
                    case "Email":
                        error = validateText(et_email.getText().toString(), "Email");
                        if(error != null) {
                            et_email.setError(error);
                            founderror = true;
                        }else {
                            et_email.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_email.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_email.getText().toString();
                        }
                        break;
                    case "Phone":
                        error = validateText(et_phone.getText().toString(), "Phone");
                        if(error != null) {
                            et_phone.setError(error);
                            founderror = true;
                        }else {
                            et_phone.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_phone.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_phone.getText().toString();
                        }
                        break;
                    case "Date of Birth":
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String inputdate = et_dateofbirth.getText().toString();
                        try {
                            Date date = sdf.parse(inputdate);
                            if (!inputdate.equals(sdf.format(date))) {
                                et_dateofbirth.setError("Invalid date");
                                founderror = true;
                            } else {
                                et_dateofbirth.setError(null);
                                mydb.setCustomInfoItem(item.getString("ParamType"), et_dateofbirth.getText().toString());
                                QRText = QRText + "\n" + item.getString("ParamID") + "." + et_dateofbirth.getText().toString();
                            }
                        }catch (Exception e){
                            et_dateofbirth.setError("Invalid date");
                            founderror = true;
                        }
                        break;
                    case "Age":
                        error = validateText(et_age.getText().toString(), "Age");
                        if(error != null) {
                            et_age.setError(error);
                            founderror = true;
                        }else {
                            et_age.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_age.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_age.getText().toString();
                        }
                        break;
                    case "Gender":
                        String genval = et_gender.getText().toString();
                        if(!(genval.equalsIgnoreCase("Male") || genval.equalsIgnoreCase("Female"))) {
                            et_gender.setError("Invalid");
                            founderror = true;
                        }else {
                            et_gender.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_gender.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_gender.getText().toString();
                        }
                        break;
                    case "Vehicle No":
                        String vehdel = et_vehicle.getText().toString();
                        if(vehdel.length() > 5) {
                            String vehtype =   vehdel.substring(0,5);
                            String vehno = vehdel.substring(5);
                            if(vehtype.equalsIgnoreCase("(2W)-") || vehtype.equalsIgnoreCase("(3W)-") || vehtype.equalsIgnoreCase("(4W)-") || vehtype.equalsIgnoreCase("(0W)-")){
                                vehno = ValidateVehicleNo(vehno);
                                if(vehno == null){
                                    et_vehicle.setError("Invalid vehicle number");
                                    founderror = true;
                                }else {
                                    et_vehicle.setError(null);
                                    mydb.setCustomInfoItem(item.getString("ParamType"), vehtype.substring(1, 2) + vehno);
                                    QRText = QRText + "\n" + item.getString("ParamID") + "." + vehno;
                                    //here is the special case for vehicle type
                                    QRText = QRText + "\n" + "8." + vehtype.substring(1, 2);
                                }
                            }
                        }else{
                            et_vehicle.setError("Vehicle type?");
                            founderror = true;
                        }
                        break;
                    case "Coming from":
                        if(et_comingfrom.getText().toString().isEmpty()) {
                            et_comingfrom.setError("Invalid");
                            founderror = true;
                        }else {
                            et_comingfrom.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_comingfrom.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_comingfrom.getText().toString();
                        }
                        break;
                    case "Purpose of visit":
                        if(et_purposeofvisit.getText().toString().isEmpty()) {
                            et_purposeofvisit.setError("Invalid");
                            founderror = true;
                        }else {
                            et_purposeofvisit.setError(null);
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_purposeofvisit.getText().toString();
                        }
                        break;
                    case "Visiting Company":
                        if(et_visitingcompany.getText().toString().isEmpty()) {
                            et_visitingcompany.setError("Invalid");
                            founderror = true;
                        }else {
                            et_visitingcompany.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_visitingcompany.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_visitingcompany.getText().toString();
                        }
                        break;
                    case "Meeting person":
                        error = validateText(et_meetingperson.getText().toString(), "Name");
                        if(error != null) {
                            et_meetingperson.setError(error);
                            founderror = true;
                        }else {
                            et_meetingperson.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_meetingperson.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_meetingperson.getText().toString();
                        }
                        break;
                    case "Block":
                        if(et_block.getText().toString().isEmpty()) {
                            et_block.setError("Invalid");
                            founderror = true;
                        }else {
                            et_block.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_block.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_block.getText().toString();
                        }
                        break;
                    case "Flat":
                        if(et_flat.getText().toString().isEmpty()) {
                            et_flat.setError("Invalid");
                            founderror = true;
                        }else {
                            et_flat.setError(null);
                            mydb.setCustomInfoItem(item.getString("ParamType"), et_flat.getText().toString());
                            QRText = QRText + "\n" + item.getString("ParamID") + "." + et_flat.getText().toString();
                        }
                        break;
                    default:
                        break;
                };

                if(founderror)
                    break;
            }

            if(!founderror)
                new DialogQRImage(getActivity(), QRText).show();
        }catch (Exception e){mydb.logAppError(PageName, "saveAndGenerateQR", "Exception", e.getMessage());}
    }

    public String validateText(String text, String type){
        String retValue = null;
        try{
            switch (type){
                case "Name":
                    if(text.equalsIgnoreCase("") || text == null)
                        return "Empty";
                    for (char c: text.toCharArray()) {
                        if(Character.isDigit(c))
                            return "Numeric Values";
                        else{
                            Pattern pattern = Pattern.compile("^[A-Za-z\\s]$");
                            Matcher matcher = pattern.matcher(c+"");
                            if(!matcher.matches())
                                return "Special Characters";
                        }
                    }
                    break;
                case "Email":
                    if(text.equalsIgnoreCase("") || text == null)
                        return "Empty";
                    Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
                    Matcher matcher = pattern.matcher(text);
                    if(!matcher.matches())
                        return "not valid";
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
                case "Address":
                    if(text.equalsIgnoreCase("") || text == null)
                        return "Empty";
                    break;
                case "Zip":
                    Pattern patternzip = Pattern.compile("^\\d{6}$");
                    Matcher matcherzip = patternzip.matcher(text);
                    if(!matcherzip.matches())
                        return "not valid";
                    break;
                case "Age":
                    if(cc.isInteger(text)){
                        int age = Integer.parseInt(text);
                        if(age >0 && age < 127)
                            return null;
                        else
                            retValue = "Age not valid";
                    }
                    else
                        retValue = "Age not valid";
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validateText", "Exception", e.getMessage());}
        return retValue;
    }

    public String ValidateVehicleNo(String vehno){
        try{
            vehno = vehno.replace(" ","");
            if(vehno == null || vehno.equalsIgnoreCase("") || vehno.length() < 4 || vehno.length() > 10)
                return null;

            Pattern pattern1 = Pattern.compile("^[A-Za-z]{3}[0-9]{1,4}$");
            Pattern pattern2 = Pattern.compile("^[A-Za-z]{2}[0-9]{1,2}[A-Za-z]{1,2}[0-9]{1,4}$");

            Matcher matcher1 = pattern1.matcher(vehno);
            Matcher matcher2 = pattern2.matcher(vehno);

            if(matcher1.matches()) {
                while(vehno.length() < 7) {
                    vehno = vehno.substring(0, 3)+"0"+vehno.substring(3);
                }

                vehno = vehno.substring(0, 3)+" "+vehno.substring(3);
                return vehno.toUpperCase();
            }

            if(matcher2.matches()){
                if(!Character.isDigit(vehno.charAt(3)))
                    vehno = vehno.substring(0, 2)+"0"+vehno.substring(2);

                while(vehno.length() < 10) {
                    if(Character.isDigit(vehno.charAt(5)))
                        vehno = vehno.substring(0, 5)+"0"+vehno.substring(5);
                    else
                        vehno = vehno.substring(0, 6)+"0"+vehno.substring(6);
                }

                if(Character.isDigit(vehno.charAt(5)))
                    vehno = vehno.substring(0, 2)+" "+vehno.substring(2, 4)+" "+vehno.charAt(4)+" "+vehno.substring(6);
                else
                    vehno = vehno.substring(0, 2) + " " + vehno.substring(2, 4) + " " + vehno.substring(4, 6) +" "+ vehno.substring(6);

                return vehno.toUpperCase();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "ValidateVehicleNo", "Exception", e.getMessage());}
        return  null;
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Security Info");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
