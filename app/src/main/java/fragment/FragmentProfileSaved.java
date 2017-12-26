package fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.home.logmenow.R;
import database.DBHelper;

import org.json.JSONObject;

/**
 * Created by Home on 7/13/2016.
 */
public class FragmentProfileSaved extends Fragment implements View.OnClickListener, View.OnLongClickListener{

    private DBHelper mydb;
    private String PageName = "FragmentProfileSaved";

    private TextView tv_personalinfo;
    private TextView tv_homeaddress;
    private TextView tv_officeaddress;

    private RelativeLayout rl_personalinfo;
    private RelativeLayout rl_homeaddress;
    private RelativeLayout rl_officeaddress;

    private ImageView img_personalinfo;
    private ImageView img_homeaddress;
    private ImageView img_officeaddress;

    private FloatingActionButton fab_add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.fragment_profilesaved, container, false);
            mydb = new DBHelper(getActivity());

            fab_add = (FloatingActionButton) rootView.findViewById(R.id.fab_add);
            fab_add.setOnClickListener(this);

            tv_personalinfo = (TextView) rootView.findViewById(R.id.tv_personalinfo);
            tv_homeaddress = (TextView) rootView.findViewById(R.id.tv_homeaddress);
            tv_officeaddress = (TextView) rootView.findViewById(R.id.tv_officeaddress);

            img_personalinfo = (ImageView) rootView.findViewById(R.id.img_personalinfo);
            img_personalinfo.setOnClickListener(this);
            img_homeaddress = (ImageView) rootView.findViewById(R.id.img_homeaddress);
            img_homeaddress.setOnClickListener(this);
            img_officeaddress = (ImageView) rootView.findViewById(R.id.img_officeaddress);
            img_officeaddress.setOnClickListener(this);

            rl_personalinfo = (RelativeLayout) rootView.findViewById(R.id.rl_personalinfo);
            rl_personalinfo.setOnLongClickListener(this);
            rl_personalinfo.setOnClickListener(this);
            rl_homeaddress = (RelativeLayout) rootView.findViewById(R.id.rl_homeaddress);
            rl_homeaddress.setOnLongClickListener(this);
            rl_homeaddress.setOnClickListener(this);
            rl_officeaddress = (RelativeLayout) rootView.findViewById(R.id.rl_officeaddress);
            rl_officeaddress.setOnLongClickListener(this);
            rl_officeaddress.setOnClickListener(this);

            /*JSONObject jo = new JSONObject();
            jo.put("FirstName", "Naresh");
            jo.put("MiddleName", "Kumar");
            jo.put("LastName", "Viriyala");
            jo.put("Email", "naresh1253@gmail.com");
            jo.put("Phone", "9985265352");
            mydb.addPersonalInfo(jo.toString());

            JSONObject joha = new JSONObject();
            joha.put("HAddress1","Block-34, Flat-1004");
            joha.put("HAddress2","Malaysian Township");
            joha.put("HLandMark","KPHB Phase-5");
            joha.put("HCity","Hyderabad");
            joha.put("HState","Telangana");
            joha.put("HCountry","India");
            joha.put("HZip","500072");
            mydb.addHomeAddress(joha.toString());

            JSONObject jooa = new JSONObject();
            jooa.put("OAddress1","Cotiviti Ltd");
            jooa.put("OAddress2","My Home Hub, 5th,6th & 7th Floors");
            jooa.put("OCity","Hyderabad");
            jooa.put("OState","Telangana");
            jooa.put("OCountry","India");
            jooa.put("OZip","500081");
            mydb.addOfficeAddress(jooa.toString());*/

            loadData();
            checkView();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public void loadData(){
        try{
            String text = "";
            Spanned textDecoration = Html.fromHtml("");
            JSONObject personalInfo = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
            if(!personalInfo.toString().equalsIgnoreCase("{}")) {
                text = personalInfo.getString("FirstName");
                text = text + " " + personalInfo.getString("MiddleName");
                text = text + " " + personalInfo.getString("LastName");
                if (personalInfo.getString("Email").equalsIgnoreCase(""))
                    text = text + "<br/>Email not provided";
                else
                    text = text + "<br/>" + personalInfo.getString("Email");
                text = text + "<br/>" + personalInfo.getString("Phone");
                textDecoration = Html.fromHtml(text);
                tv_personalinfo.setText(textDecoration);
            }
            else{rl_personalinfo.setVisibility(View.GONE);}

            JSONObject homeAddress = new JSONObject(mydb.getProfileInfo("HomeAddress"));
            if(!homeAddress.toString().equalsIgnoreCase("{}")) {
                text = homeAddress.getString("HAddress1");
                if (!homeAddress.getString("HAddress2").equalsIgnoreCase(""))
                    text = text + ",<br/>" + homeAddress.getString("HAddress2");
                if (!homeAddress.getString("HLandMark").equalsIgnoreCase(""))
                    text = text + ",<br/>" + homeAddress.getString("HLandMark");
                text = text + ",<br/>" + homeAddress.getString("HCity");
                text = text + ",<br/>" + homeAddress.getString("HState");
                text = text + ",<br/>" + homeAddress.getString("HCountry");
                text = text + ",<br/>Zip : " + homeAddress.getString("HZip");
                textDecoration = Html.fromHtml(text);
                tv_homeaddress.setText(textDecoration);
            }else{rl_homeaddress.setVisibility(View.GONE);}

            JSONObject officeAddress = new JSONObject(mydb.getProfileInfo("OfficeAddress"));
            if(!officeAddress.toString().equalsIgnoreCase("{}")) {
                text = officeAddress.getString("OAddress1");
                if (!officeAddress.getString("OAddress2").equalsIgnoreCase(""))
                    text = text + ",<br/>" + officeAddress.getString("OAddress2");
                text = text + ",<br/>" + officeAddress.getString("OCity");
                text = text + ",<br/>" + officeAddress.getString("OState");
                text = text + ",<br/>" + officeAddress.getString("OCountry");
                text = text + ",<br/>Zip : " + officeAddress.getString("OZip");
                textDecoration = Html.fromHtml(text);
                tv_officeaddress.setText(textDecoration);
            }else {rl_officeaddress.setVisibility(View.GONE);}
        }
        catch (Exception e){mydb.logAppError(PageName, "loadData", "Exception", e.getMessage());}
    }

    public void loadFragment(String fragmentName, Bundle bundle){
        try{
            Class<?> c = Class.forName("fragment."+fragmentName);
            Fragment fragment = (Fragment) c.newInstance();
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            fragmentTransaction.replace(R.id.container_body, fragment, fragmentName);
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "loadFragment", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try {
            Bundle bundle = new Bundle();
            switch (v.getId()){
                case R.id.fab_add:
                    bundle.putString("select", "FromProfileSaved");
                    loadFragment("FragmentProfile", bundle);
                    break;
                case R.id.img_personalinfo:
                    bundle.putString("select", "PersonalInfo");
                    loadFragment("FragmentProfile", bundle);
                    break;
                case R.id.img_homeaddress:
                    bundle.putString("select", "HomeAddress");
                    loadFragment("FragmentProfile", bundle);
                    break;
                case R.id.img_officeaddress:
                    bundle.putString("select", "OfficeAddress");
                    loadFragment("FragmentProfile", bundle);
                    break;
                case R.id.rl_personalinfo:
                    bundle.putString("texttoqr", tv_personalinfo.getText().toString());
                    bundle.putString("title", "Basic Information");
                    loadFragment("FragmentProfileQR", bundle);
                    break;
                case R.id.rl_homeaddress:
                    bundle.putString("texttoqr", tv_homeaddress.getText().toString());
                    bundle.putString("title", "Home Address");
                    loadFragment("FragmentProfileQR", bundle);
                    break;
                case R.id.rl_officeaddress:
                    bundle.putString("texttoqr", tv_officeaddress.getText().toString());
                    bundle.putString("title", "Office Address");
                    loadFragment("FragmentProfileQR", bundle);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());
        }
    }

    public void checkView(){
        try {
            boolean exists = false;
            boolean missing = false;
            if(rl_personalinfo.getVisibility() == View.VISIBLE)
                exists = true;
            else
                missing = true;
            if(rl_homeaddress.getVisibility() == View.VISIBLE)
                exists = true;
            else
                missing = true;
            if(rl_officeaddress.getVisibility() == View.VISIBLE)
                exists = true;
            else
                missing = true;
            if(!exists){
                Bundle bundle = new Bundle();
                bundle.putString("select", "");
                loadFragment("FragmentProfile", bundle);
            }
            if(missing)
                fab_add.setVisibility(View.VISIBLE);
            else
                fab_add.setVisibility(View.GONE);
        }
        catch (Exception e){mydb.logAppError(PageName, "checkView", "Exception", e.getMessage());}
    }

    @Override
    public boolean onLongClick(View v) {
        try {
            switch (v.getId()){
                case R.id.rl_personalinfo:
                    showDeleteDialog("Basic Information");
                    break;
                case R.id.rl_homeaddress:
                    showDeleteDialog("Home Address");
                    break;
                case R.id.rl_officeaddress:
                    showDeleteDialog("Office Address");
                    break;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "onLongClick", "Exception", e.getMessage());
        }
        return false;
    }

    public void showDeleteDialog(final String gridName){
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Delete?");
            alertDialogBuilder.setTitle( Html.fromHtml("<font color='"+getResources().getString(R.string.colorprimary)+"'>Delete "+gridName+"?</font>"));
            alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    switch (gridName){
                        case "Basic Information":
                            mydb.deleteProfileInfo("PersonalInfo");
                            rl_personalinfo.setVisibility(View.GONE);
                            checkView();
                            break;
                        case "Home Address":
                            mydb.deleteProfileInfo("HomeAddress");
                            rl_homeaddress.setVisibility(View.GONE);
                            checkView();
                            break;
                        case "Office Address":
                            mydb.deleteProfileInfo("OfficeAddress");
                            rl_officeaddress.setVisibility(View.GONE);
                            checkView();
                            break;
                        default:
                            break;
                    }
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        catch (Exception e){
            mydb.logAppError(PageName, "showDeleteDialog", "Exception", e.getMessage());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Saved Profiles");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
