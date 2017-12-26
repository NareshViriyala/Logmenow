package fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.CustomInfoAdapter;
import dialog.DialogQRImage;
import shared.BackgroundTasks;
import com.example.home.logmenow.R;
import database.DBHelper;
import shared.CommonClasses;
import dialog.DialogInfoName;
import shared.Models;
import shared.NetworkDetector;

import org.json.JSONObject;


/**
 * Created by nviriyala on 08-07-2016.
 */
public class FragmentProfile extends Fragment implements View.OnClickListener, OnItemSelectedListener, OnItemClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentProfile";
    private LinearLayout ll_fragprofile;
    private LinearLayout ll_personalinfo;
    private LinearLayout ll_homeaddress;
    private LinearLayout ll_officeaddress;
    private LinearLayout ll_custominfo;
    private Spinner spnr_items;

    private Button btn_save_personalinfo;
    private Button btn_save_homeaddress;
    private Button btn_save_officeaddress;
    private Button btn_save_custominfo;
    private Button btn_generateqr;
    private ImageView img_editinfo;
    private String selectedInfo = "";
    private NetworkDetector nd;
    private String fromProfileSaved;
    private CommonClasses cc;

    private List<Models.CustomInfoItem> unsavedItems;
    private List<Models.CustomInfoItem> savedItems;
    List<String> infoName = new ArrayList<>();
    private CustomInfoAdapter customInfoAdapter;
    private ListView lv_items;
    //private RelativeLayout rl_info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            mydb = new DBHelper(getActivity());
            nd = new NetworkDetector(getActivity());
            cc = new CommonClasses(getActivity());
            fromProfileSaved = "PersonalInfo";//getArguments().getString("select");
            setHasOptionsMenu(true);
            ll_fragprofile = (LinearLayout) rootView.findViewById(R.id.ll_fragprofile);
            ll_personalinfo = (LinearLayout) rootView.findViewById(R.id.ll_personalinfo);
            ll_personalinfo.setOnClickListener(FragmentProfile.this);
            ll_homeaddress = (LinearLayout) rootView.findViewById(R.id.ll_homeaddress);
            ll_homeaddress.setOnClickListener(FragmentProfile.this);
            ll_officeaddress = (LinearLayout) rootView.findViewById(R.id.ll_officeaddress);
            ll_officeaddress.setOnClickListener(FragmentProfile.this);
            ll_custominfo = (LinearLayout) rootView.findViewById(R.id.ll_custominfo);
            ll_custominfo.setOnClickListener(FragmentProfile.this);

            lv_items = (ListView)rootView.findViewById(R.id.lv_items);
            lv_items.setOnItemClickListener(this);

            spnr_items = (Spinner) rootView.findViewById(R.id.spnr_items);
            spnr_items.setOnItemSelectedListener(this);

            //rl_info = (RelativeLayout) rootView.findViewById(R.id.rl_info);
            //rl_info.setOnClickListener(FragmentProfile.this);
            btn_save_personalinfo = (Button) rootView.findViewById(R.id.btn_save_personalinfo);
            btn_save_personalinfo.setOnClickListener(this);

            btn_save_homeaddress = (Button) rootView.findViewById(R.id.btn_save_homeaddress);
            btn_save_homeaddress.setOnClickListener(this);

            btn_save_officeaddress = (Button) rootView.findViewById(R.id.btn_save_officeaddress);
            btn_save_officeaddress.setOnClickListener(this);

            btn_save_custominfo = (Button) rootView.findViewById(R.id.btn_save_custominfo);
            btn_save_custominfo.setOnClickListener(this);

            btn_generateqr = (Button) rootView.findViewById(R.id.btn_generateqr);
            btn_generateqr.setOnClickListener(this);

            img_editinfo = (ImageView) rootView.findViewById(R.id.img_editinfo);
            img_editinfo.setOnClickListener(this);
            switch (fromProfileSaved) {
                case "PersonalInfo":
                    ll_personalinfo.performClick();
                    //showHideInfo(fromProfileSaved);
                    break;
                case "OfficeAddress":
                    ll_officeaddress.performClick();
                    showHideInfo(fromProfileSaved);
                    break;
                case "HomeAddress":
                    ll_homeaddress.performClick();
                    showHideInfo(fromProfileSaved);
                    break;
                case "CustomInfo":
                    ll_custominfo.performClick();
                    showHideInfo(fromProfileSaved);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public void redesignLayout(String selectedGrid){
        try{
            String[] grids = {"PersonalInfo", "HomeAddress", "OfficeAddress", "CustomInfo"};
            for (String grid:grids) {
                grid = grid.toLowerCase();
                ImageView img_grid = (ImageView) ll_fragprofile.findViewById(this.getResources().getIdentifier("img_" + grid, "id", getActivity().getPackageName()));
                TextView tv_grid = (TextView) ll_fragprofile.findViewById(this.getResources().getIdentifier("tv_" + grid, "id", getActivity().getPackageName()));
                LinearLayout ll_grid = (LinearLayout) ll_fragprofile.findViewById(this.getResources().getIdentifier("ll_" + grid, "id", getActivity().getPackageName()));
                GridLayout gl_grid = (GridLayout)ll_fragprofile.findViewById(this.getResources().getIdentifier("gv_" + grid, "id", getActivity().getPackageName()));
                RelativeLayout rl_savedinfo = (RelativeLayout)ll_fragprofile.findViewById(this.getResources().getIdentifier("rl_savedinfo", "id", getActivity().getPackageName()));
                if(grid.equalsIgnoreCase(selectedGrid)){
                    img_grid.setColorFilter(getResources().getColor(R.color.white));
                    tv_grid.setTextColor(getResources().getColor(R.color.white));
                    ll_grid.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    if(selectedGrid.equalsIgnoreCase("CustomInfo")){
                        rl_savedinfo.setVisibility(View.GONE);
                        handleCustomInfoScreen();
                        return;
                    }
                    JSONObject jo = new JSONObject(mydb.getProfileInfo(selectedGrid));
                    if(jo.toString().equalsIgnoreCase("{}")) {
                        gl_grid.setVisibility(View.VISIBLE);
                        rl_savedinfo.setVisibility(View.GONE);
                    }
                    else {
                        Spanned textDecoration = Html.fromHtml("");
                        switch (selectedGrid) {
                            case "PersonalInfo":
                                String email = jo.getString("Email");
                                if(email.equalsIgnoreCase(""))
                                    email = "Not provided";
                                textDecoration = Html.fromHtml("<b><u>Name</u></b><br/>"
                                        + jo.getString("FirstName")
                                        +" "
                                        + jo.getString("MiddleName")
                                        +" "
                                        + jo.getString("LastName")
                                        + "<br/><b><u>Email</u></b><br/>"
                                        + email
                                        + "<br/><b><u>Phone</u></b><br/>"
                                        +jo.getString("Phone"));

                                break;
                            case "HomeAddress":
                                String value = jo.getString("HAddress1");
                                if(!jo.getString("HAddress2").equalsIgnoreCase(""))
                                    value = value+",<br/>"+jo.getString("HAddress2");
                                if(!jo.getString("HLandMark").equalsIgnoreCase(""))
                                    value = value+",<br/>"+jo.getString("HLandMark");
                                value = value+",<br/>"+jo.getString("HCity");
                                value = value+",<br/>"+jo.getString("HState");
                                value = value+",<br/>"+jo.getString("HCountry");
                                value = value+",<br/>Zip : "+jo.getString("HZip");

                                textDecoration = Html.fromHtml("<b><u>Address</u></b><br/>"+value);
                                break;
                            case "OfficeAddress":
                                String ofcvalue = jo.getString("OAddress1");
                                if(!jo.getString("OAddress2").equalsIgnoreCase(""))
                                    ofcvalue = ofcvalue+",<br/>"+jo.getString("OAddress2");
                                ofcvalue = ofcvalue+",<br/>"+jo.getString("OCity");
                                ofcvalue = ofcvalue+",<br/>"+jo.getString("OState");
                                ofcvalue = ofcvalue+",<br/>"+jo.getString("OCountry");
                                ofcvalue = ofcvalue+",<br/>Zip : "+jo.getString("OZip");

                                textDecoration = Html.fromHtml("<b><u>Address</u></b><br/>"+ofcvalue);
                                break;
                            default:
                                break;
                        }
                        TextView tv_info = (TextView) ll_fragprofile.findViewById(this.getResources().getIdentifier("tv_savedinfo" , "id", getActivity().getPackageName()));
                        tv_info.setText(textDecoration);
                        gl_grid.setVisibility(View.GONE);
                        rl_savedinfo.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    img_grid.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    tv_grid.setTextColor(getResources().getColor(R.color.colorPrimary));
                    ll_grid.setBackgroundColor(getResources().getColor(R.color.white));
                    gl_grid.setVisibility(View.GONE);
                    //rl_savedinfo.setVisibility(View.GONE);
                }
            }
            hideSoftKeyboard();
        }
        catch (Exception e){mydb.logAppError(PageName, "redesignLayout", "Exception", e.getMessage());}
    }

    public void handleCustomInfoScreen(){
        try{
            GridLayout gv_custominfo = (GridLayout)ll_fragprofile.findViewById(R.id.gv_custominfo);
            LinearLayout ll_custom = (LinearLayout)ll_fragprofile.findViewById(R.id.ll_custom);
            gv_custominfo.setVisibility(View.VISIBLE);
            populateLists();
            if(unsavedItems.size() == 1)
                ll_custom.setVisibility(View.GONE);
            else{
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner,infoName);
                dataAdapter.setDropDownViewResource(R.layout.item_spinner);
                spnr_items.setAdapter(dataAdapter);
                ll_custom.setVisibility(View.VISIBLE);
            }

            if(savedItems.size() == 0)
                lv_items.setVisibility(View.GONE);
            else{
                customInfoAdapter = new CustomInfoAdapter(getActivity(), savedItems);
                lv_items.setAdapter(customInfoAdapter);
                lv_items.setVisibility(View.VISIBLE);
            }

        }
        catch (Exception e){mydb.logAppError(PageName, "handleCustomInfoScreen", "Exception", e.getMessage());}
    }

    public void populateLists(){
        try{
            unsavedItems = mydb.getCustomInfoItems(false);
            savedItems = mydb.getCustomInfoItems(true);
            infoName.clear();
            for(int i = 0; i<unsavedItems.size();i++){
                infoName.add(unsavedItems.get(i).getInfoName());
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "populateLists", "Exception", e.getMessage());}
    }

    public void showHideInfo(String gridname){
        try{
            RelativeLayout rl_savedinfo = (RelativeLayout)ll_fragprofile.findViewById(this.getResources().getIdentifier("rl_savedinfo", "id", getActivity().getPackageName()));
            rl_savedinfo.setVisibility(View.GONE);

            JSONObject jobj = new JSONObject(mydb.getProfileInfo(gridname));
            switch (gridname){
                case "PersonalInfo":
                    EditText et_fname = (EditText) ll_fragprofile.findViewById(R.id.et_fname);
                    et_fname.setText(jobj.getString("FirstName"));

                    EditText et_mname = (EditText) ll_fragprofile.findViewById(R.id.et_mname);
                    et_mname.setText(jobj.getString("MiddleName"));

                    EditText et_lname = (EditText) ll_fragprofile.findViewById(R.id.et_lname);
                    et_lname.setText(jobj.getString("LastName"));

                    EditText et_email = (EditText) ll_fragprofile.findViewById(R.id.et_email);
                    et_email.setText(jobj.getString("Email"));

                    EditText et_phone = (EditText) ll_fragprofile.findViewById(R.id.et_phone);
                    et_phone.setText(jobj.getString("Phone"));
                    break;
                case "HomeAddress":
                    EditText et_add1 = (EditText) ll_fragprofile.findViewById(R.id.et_add1);
                    et_add1.setText(jobj.getString("HAddress1"));

                    EditText et_add2 = (EditText) ll_fragprofile.findViewById(R.id.et_add2);
                    et_add2.setText(jobj.getString("HAddress2"));

                    EditText et_landmark = (EditText) ll_fragprofile.findViewById(R.id.et_landmark);
                    et_landmark.setText(jobj.getString("HLandMark"));

                    EditText et_city = (EditText) ll_fragprofile.findViewById(R.id.et_city);
                    et_city.setText(jobj.getString("HCity"));

                    EditText et_state = (EditText) ll_fragprofile.findViewById(R.id.et_state);
                    et_state.setText(jobj.getString("HState"));

                    EditText et_country = (EditText) ll_fragprofile.findViewById(R.id.et_country);
                    et_country.setText(jobj.getString("HCountry"));

                    EditText et_zip = (EditText) ll_fragprofile.findViewById(R.id.et_zip);
                    et_zip.setText(jobj.getString("HZip"));
                    break;
                case "OfficeAddress":
                    EditText et_ofcadd1 = (EditText) ll_fragprofile.findViewById(R.id.et_ofcadd1);
                    et_ofcadd1.setText(jobj.getString("OAddress1"));

                    EditText et_ofcadd2 = (EditText) ll_fragprofile.findViewById(R.id.et_ofcadd2);
                    et_ofcadd2.setText(jobj.getString("OAddress2"));

                    EditText et_ofccity = (EditText) ll_fragprofile.findViewById(R.id.et_ofccity);
                    et_ofccity.setText(jobj.getString("OCity"));

                    EditText et_ofcstate = (EditText) ll_fragprofile.findViewById(R.id.et_ofcstate);
                    et_ofcstate.setText(jobj.getString("OState"));

                    EditText et_ofccountry = (EditText) ll_fragprofile.findViewById(R.id.et_ofccountry);
                    et_ofccountry.setText(jobj.getString("OCountry"));

                    EditText et_ofczip = (EditText) ll_fragprofile.findViewById(R.id.et_ofczip);
                    et_ofczip.setText(jobj.getString("OZip"));
                    break;
            }
            GridLayout gl_grid = (GridLayout)ll_fragprofile.findViewById(this.getResources().getIdentifier("gv_" + gridname.toLowerCase(), "id", getActivity().getPackageName()));
            gl_grid.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "showHideInfo", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()){
                case R.id.ll_personalinfo:
                    if(selectedInfo.equalsIgnoreCase("PersonalInfo"))
                        return;
                    else
                        selectedInfo = "PersonalInfo";
                    redesignLayout(selectedInfo);
                    break;
                case R.id.ll_homeaddress:
                    if(selectedInfo.equalsIgnoreCase("HomeAddress"))
                        return;
                    else
                        selectedInfo = "HomeAddress";
                    redesignLayout(selectedInfo);
                    break;
                case R.id.ll_officeaddress:
                    if(selectedInfo.equalsIgnoreCase("OfficeAddress"))
                        return;
                    else
                        selectedInfo = "OfficeAddress";
                    redesignLayout(selectedInfo);
                    break;
                case R.id.ll_custominfo:
                    if(selectedInfo.equalsIgnoreCase("CustomInfo"))
                        return;
                    else
                        selectedInfo = "CustomInfo";
                    redesignLayout(selectedInfo);
                    break;
                case R.id.btn_save_personalinfo:
                    if(validatePersonalInfo()) {
                        JSONObject personalinfo = new JSONObject();
                        EditText et_fname = (EditText) ll_fragprofile.findViewById(R.id.et_fname);
                        String str_fname = et_fname.getText().toString();
                        EditText et_mname = (EditText) ll_fragprofile.findViewById(R.id.et_mname);
                        String str_mname = et_mname.getText().toString();
                        EditText et_lname = (EditText) ll_fragprofile.findViewById(R.id.et_lname);
                        String str_lname = et_lname.getText().toString();
                        EditText et_email = (EditText) ll_fragprofile.findViewById(R.id.et_email);
                        String str_email = et_email.getText().toString();
                        EditText et_phone = (EditText) ll_fragprofile.findViewById(R.id.et_phone);
                        String str_phone = et_phone.getText().toString();
                        personalinfo.put("FirstName",str_fname);
                        personalinfo.put("MiddleName",str_mname);
                        personalinfo.put("LastName",str_lname);
                        personalinfo.put("Email",str_email);
                        personalinfo.put("Phone",str_phone);
                        mydb.addPersonalInfo(personalinfo.toString());
                        new syncLocalDB().execute("PersonalInfo");
                        redesignLayout(selectedInfo);
                    }
                    break;
                case R.id.btn_save_homeaddress:
                    if(validateHomeAddress()){
                        JSONObject homeaddress = new JSONObject();
                        EditText et_add1 = (EditText) ll_fragprofile.findViewById(R.id.et_add1);
                        String str_add1 = et_add1.getText().toString();

                        EditText et_add2 = (EditText) ll_fragprofile.findViewById(R.id.et_add2);
                        String str_add2 = et_add2.getText().toString();

                        EditText et_landmark = (EditText) ll_fragprofile.findViewById(R.id.et_landmark);
                        String str_landmark = et_landmark.getText().toString();

                        EditText et_city = (EditText) ll_fragprofile.findViewById(R.id.et_city);
                        String str_city = et_city.getText().toString();

                        EditText et_state = (EditText) ll_fragprofile.findViewById(R.id.et_state);
                        String str_state = et_state.getText().toString();

                        EditText et_country = (EditText) ll_fragprofile.findViewById(R.id.et_country);
                        String str_country = et_country.getText().toString();

                        EditText et_zip = (EditText) ll_fragprofile.findViewById(R.id.et_zip);
                        String str_zip = et_zip.getText().toString();

                        homeaddress.put("HAddress1",str_add1);
                        homeaddress.put("HAddress2",str_add2);
                        homeaddress.put("HLandMark",str_landmark);
                        homeaddress.put("HCity",str_city);
                        homeaddress.put("HState",str_state);
                        homeaddress.put("HCountry",str_country);
                        homeaddress.put("HZip",str_zip);
                        mydb.addHomeAddress(homeaddress.toString());
                        new syncLocalDB().execute("HomeAddress");
                        redesignLayout(selectedInfo);

                    }
                    break;
                case R.id.btn_save_officeaddress:
                    if(validateOfficeAddress()){
                        JSONObject officeaddress = new JSONObject();
                        EditText et_add1 = (EditText) ll_fragprofile.findViewById(R.id.et_ofcadd1);
                        String str_add1 = et_add1.getText().toString();

                        EditText et_add2 = (EditText) ll_fragprofile.findViewById(R.id.et_ofcadd2);
                        String str_add2 = et_add2.getText().toString();

                        EditText et_city = (EditText) ll_fragprofile.findViewById(R.id.et_ofccity);
                        String str_city = et_city.getText().toString();

                        EditText et_state = (EditText) ll_fragprofile.findViewById(R.id.et_ofcstate);
                        String str_state = et_state.getText().toString();

                        EditText et_country = (EditText) ll_fragprofile.findViewById(R.id.et_ofccountry);
                        String str_country = et_country.getText().toString();

                        EditText et_zip = (EditText) ll_fragprofile.findViewById(R.id.et_ofczip);
                        String str_zip = et_zip.getText().toString();

                        officeaddress.put("OAddress1",str_add1);
                        officeaddress.put("OAddress2",str_add2);
                        officeaddress.put("OCity",str_city);
                        officeaddress.put("OState",str_state);
                        officeaddress.put("OCountry",str_country);
                        officeaddress.put("OZip",str_zip);
                        mydb.addOfficeAddress(officeaddress.toString());
                        new syncLocalDB().execute("OfficeAddress");
                        redesignLayout(selectedInfo);
                    }
                    break;
                case R.id.btn_save_custominfo:
                    String qrContent = "0."+mydb.getDeviceID();
                    boolean contentSelected = false;
                    for (Models.CustomInfoItem item:savedItems) {
                        if(item.getSelected()){

                            if(item.getId() == 7) { //special case for vehicle number and vehicle type
                                qrContent = qrContent+"\n"+item.getId()+"."+item.getInfoValue().substring(1);
                                qrContent = qrContent+"\n8."+item.getInfoValue().substring(0,1);
                            }else
                                qrContent = qrContent+"\n"+item.getId()+"."+item.getInfoValue();
                            contentSelected=true;
                        }
                    }
                    if(contentSelected){
                        new DialogQRImage(getActivity(), qrContent).show();
                    }
                    else if(savedItems.size() == 0){
                        Toast.makeText(getActivity(), "Select items from drop down", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //new DialogInformation(getActivity(), Html.fromHtml("Select items to generate QR"), null).show();
                        Toast.makeText(getActivity(), "Select items from list to generate QR", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.img_editinfo:
                    showHideInfo(selectedInfo);
                    break;
                case R.id.btn_generateqr:
                    String textToQR = textToQR(selectedInfo);
                    new DialogQRImage(getActivity(), textToQR).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());
        }
    }

    public boolean validateOfficeAddress(){
        boolean retValue = true;
        try{
            EditText et_add1 = (EditText) ll_fragprofile.findViewById(R.id.et_ofcadd1);
            EditText et_city = (EditText) ll_fragprofile.findViewById(R.id.et_ofccity);
            EditText et_state = (EditText) ll_fragprofile.findViewById(R.id.et_ofcstate);
            EditText et_country = (EditText) ll_fragprofile.findViewById(R.id.et_ofccountry);
            EditText et_zip = (EditText) ll_fragprofile.findViewById(R.id.et_ofczip);

            String error = validateText(et_add1.getText().toString(), "Address");
            if(error != null) { //Address Line 1
                et_add1.setError(error);
                retValue = false;
            }
            error = validateText(et_city.getText().toString(), "Address");
            if(error != null) { //City
                et_city.setError(error);
                retValue = false;
            }
            error = validateText(et_state.getText().toString(), "Address");
            if(error != null) { //State
                et_state.setError(error);
                retValue = false;
            }
            error = validateText(et_country.getText().toString(), "Address");
            if(error != null) { //Country
                et_country.setError(error);
                retValue = false;
            }
            error = validateText(et_zip.getText().toString(), "Zip");
            if(error != null) { //Zip
                et_zip.setError(error);
                retValue = false;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validateOfficeAddress", "Exception", e.getMessage());}
        return retValue;
    }

    public boolean validateHomeAddress(){
        boolean retValue = true;
        try{
            EditText et_add1 = (EditText) ll_fragprofile.findViewById(R.id.et_add1);
            //EditText et_add2 = (EditText) ll_fragprofile.findViewById(R.id.et_add2);
            //EditText et_landmark = (EditText) ll_fragprofile.findViewById(R.id.et_landmark);
            EditText et_city = (EditText) ll_fragprofile.findViewById(R.id.et_city);
            EditText et_state = (EditText) ll_fragprofile.findViewById(R.id.et_state);
            EditText et_country = (EditText) ll_fragprofile.findViewById(R.id.et_country);
            EditText et_zip = (EditText) ll_fragprofile.findViewById(R.id.et_zip);

            String error = validateText(et_add1.getText().toString(), "Address");
            if(error != null) { //Address Line 1
                et_add1.setError(error);
                retValue = false;
            }
            error = validateText(et_city.getText().toString(), "Address");
            if(error != null) { //City
                et_city.setError(error);
                retValue = false;
            }
            error = validateText(et_state.getText().toString(), "Address");
            if(error != null) { //State
                et_state.setError(error);
                retValue = false;
            }
            error = validateText(et_country.getText().toString(), "Address");
            if(error != null) { //Country
                et_country.setError(error);
                retValue = false;
            }
            error = validateText(et_zip.getText().toString(), "Zip");
            if(error != null) { //Zip
                et_zip.setError(error);
                retValue = false;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validateHomeAddress", "Exception", e.getMessage());}
        return retValue;
    }

    public boolean validatePersonalInfo(){
        boolean retValue = true;
        try{
            EditText et_fname = (EditText) ll_fragprofile.findViewById(R.id.et_fname);
            EditText et_mname = (EditText) ll_fragprofile.findViewById(R.id.et_mname);
            EditText et_lname = (EditText) ll_fragprofile.findViewById(R.id.et_lname);
            EditText et_email = (EditText) ll_fragprofile.findViewById(R.id.et_email);
            EditText et_phone = (EditText) ll_fragprofile.findViewById(R.id.et_phone);

            String error = validateText(et_fname.getText().toString(), "Name");
            if(error != null) { //First Name
                et_fname.setError(error);
                retValue = false;
            }
            error = validateText(et_mname.getText().toString(), "Name");
            if(error != null && !error.equalsIgnoreCase("Empty")) { //Middle Name
                et_mname.setError(error);
                retValue = false;
            }
            error = validateText(et_lname.getText().toString(), "Name");
            if(error != null && !error.equalsIgnoreCase("Empty")){ //Last Name
                et_lname.setError(error);
                retValue = false;
            }
            error = validateText(et_email.getText().toString(), "Email");
            if(error != null && !error.equalsIgnoreCase("Empty")){ //Email
                et_email.setError(error);
                retValue = false;
            }
            error = validateText(et_phone.getText().toString(), "Phone");
            if(error != null){ //Phone
                et_phone.setError(error);
                retValue = false;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "validatePersonalInfo", "Exception", e.getMessage());}
        return retValue;
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

    public void hideSoftKeyboard(){
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(getView() != null)
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        catch (Exception e){mydb.logAppError(PageName, "hideSoftKeyboard", "Exception", e.getMessage());}
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Profiles");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try{
            String infoName = parent.getItemAtPosition(position).toString();
            JSONObject jsonObject = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
            String infoValue = "";
            switch (infoName){
                case "Name":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    if(!jsonObject.toString().equalsIgnoreCase("{}") && infoValue.equalsIgnoreCase("")) {
                        if (!jsonObject.getString("FirstName").equalsIgnoreCase(""))
                            infoValue = jsonObject.getString("FirstName");
                        if (!jsonObject.getString("MiddleName").equalsIgnoreCase(""))
                            infoValue = infoValue + " " + jsonObject.getString("MiddleName");
                        if (!jsonObject.getString("LastName").equalsIgnoreCase(""))
                            infoValue = infoValue + " " + jsonObject.getString("LastName");
                    }
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Date of Birth":
                    Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(getActivity(),datePickerListener,mYear,mMonth,mDay).show();
                    break;
                case "Age":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Gender":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Email":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    if(!jsonObject.toString().equalsIgnoreCase("{}") && infoValue.equalsIgnoreCase("") && !jsonObject.getString(infoName).equalsIgnoreCase(""))
                            infoValue = jsonObject.getString(infoName);
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Phone":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    if(!jsonObject.toString().equalsIgnoreCase("{}") && infoValue.equalsIgnoreCase("") && !jsonObject.getString(infoName).equalsIgnoreCase(""))
                        infoValue = jsonObject.getString(infoName);
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Vehicle No":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Coming from":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Purpose of visit":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Visiting Company":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Meeting person":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Block":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                case "Flat":
                    infoValue = mydb.getCustomInfoItem(infoName).getInfoValue();
                    new DialogInfoName(this, infoName, infoValue).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemSelected", "Exception", e.getMessage());}
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        try{
            switch (view.getId()){
                case R.id.ll_item:
                    if(savedItems.get(position).getSelected()){
                        savedItems.get(position).setSelected(false);
                        customInfoAdapter.refreshList(savedItems);
                    }
                    else{
                        savedItems.get(position).setSelected(true);
                        customInfoAdapter.refreshList(savedItems);
                    }
                    break;
                case R.id.img_edit:
                    if(savedItems.get(position).getInfoName().equalsIgnoreCase("Date of Birth")) {
                        String dob = savedItems.get(position).getInfoValue();
                        int mDay = Integer.parseInt(dob.substring(0,2));
                        int mMonth = Integer.parseInt(dob.substring(3,5))-1;
                        int mYear = Integer.parseInt(dob.substring(6,10));
                        new DatePickerDialog(getActivity(),datePickerListener,mYear,mMonth,mDay).show();
                    }
                    else
                        new DialogInfoName(this, savedItems.get(position).getInfoName(), savedItems.get(position).getInfoValue()).show();
                    break;
                case R.id.img_delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Delete "+savedItems.get(position).getInfoName())
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mydb.setCustomInfoItem(savedItems.get(position).getInfoName(), "");
                                    handleCustomInfoScreen();
                                    //populateLists();
                                    //customInfoAdapter.refreshList(savedItems);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public class syncLocalDB extends AsyncTask<String, Integer, Object> {

        @Override
        protected Object doInBackground(String... params) {
            try {new BackgroundTasks(getActivity(), params[0]);}
            catch(Exception e){mydb.logAppError(PageName, "asyncEnterCall--doInBackground", "Exception", e.getMessage());}
            return null;
        }
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            selectedMonth++;
            String month = selectedMonth<10?"0"+selectedMonth:String.valueOf(selectedMonth);
            String day = selectedDay<10?"0"+selectedDay:String.valueOf(selectedDay);
            mydb.setCustomInfoItem("Date of Birth", day+"/"+month+"/"+selectedYear);
            handleCustomInfoScreen();
        }
    };

    public String textToQR(String tag){
        String texttoqr = "0."+mydb.getDeviceID();
        try{
            JSONObject jobj;
            switch (tag){
                case "PersonalInfo":
                    jobj = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
                    texttoqr = texttoqr+"\n2."+jobj.getString("FirstName");
                    if(!jobj.getString("MiddleName").equalsIgnoreCase(""))
                        texttoqr = texttoqr+" "+jobj.getString("MiddleName");
                    if(!jobj.getString("LastName").equalsIgnoreCase(""))
                        texttoqr = texttoqr+" "+jobj.getString("LastName");
                    if(!jobj.getString("Email").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"\n3."+jobj.getString("Email");
                    if(!jobj.getString("Phone").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"\n4."+jobj.getString("Phone");
                    break;
                case "HomeAddress":
                    jobj = new JSONObject(mydb.getProfileInfo("HomeAddress"));
                    texttoqr = texttoqr+"\n13."+jobj.getString("HAddress1");
                    if(!jobj.getString("HAddress2").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"||"+jobj.getString("HAddress2");
                    if(!jobj.getString("HLandMark").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"||"+jobj.getString("HLandMark");
                    texttoqr = texttoqr+"||"+jobj.getString("HCity");
                    texttoqr = texttoqr+", "+jobj.getString("HState");
                    texttoqr = texttoqr+", "+jobj.getString("HCountry");
                    texttoqr = texttoqr+"||"+jobj.getString("HZip");
                    break;
                case "OfficeAddress":
                    jobj = new JSONObject(mydb.getProfileInfo("OfficeAddress"));
                    texttoqr = texttoqr+"\n14."+jobj.getString("OAddress1");
                    if(!jobj.getString("OAddress2").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"||"+jobj.getString("OAddress2");
                    texttoqr = texttoqr+"||"+jobj.getString("OCity");
                    texttoqr = texttoqr+", "+jobj.getString("OState");
                    texttoqr = texttoqr+", "+jobj.getString("OCountry");
                    texttoqr = texttoqr+"||"+jobj.getString("OZip");
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "textToQR", "Exception", e.getMessage());}
        return texttoqr;
    }
}



