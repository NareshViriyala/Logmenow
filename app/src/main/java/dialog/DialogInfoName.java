package dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenow.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DBHelper;
import fragment.FragmentProfile;

/**
 * Created by nviriyala on 22-08-2016.
 */
public class DialogInfoName extends Dialog {
    private Button btn_ok;
    private DBHelper mydb;
    private FragmentProfile fragmentProfile;
    private String infoName;
    private String infoValue;
    private String PageName = "DialogInfoName";

    public DialogInfoName(FragmentProfile fragmentProfile, String infoName, String infoValue) {
        super(fragmentProfile.getActivity());
        this.fragmentProfile = fragmentProfile;
        mydb = new DBHelper(fragmentProfile.getActivity());
        this.infoName = infoName;
        this.infoValue = infoValue;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_infoname);
        TextView tv_infoname = (TextView) findViewById(R.id.tv_infoname);
        tv_infoname.setText(infoName);

        final EditText et_itemvalue = (EditText) findViewById(R.id.et_itemvalue);
        et_itemvalue.setText(infoValue);

        final Spinner spnr_pov = (Spinner) findViewById(R.id.spnr_pov);
        spnr_pov.setVisibility(View.GONE);
        final List<String> povList = new ArrayList<>();
        povList.add("Personal visit");
        povList.add("Official visit");
        povList.add("Interview");
        povList.add("Courier");
        povList.add("Food delivery");
        povList.add("Postal delivery");
        Collections.sort(povList);
        povList.add(0, "Choose from below");

        final List<String> vehTypeList = new ArrayList<>();
        vehTypeList.add("Vehicle type");
        vehTypeList.add("2 wheeler");
        vehTypeList.add("3 wheeler");
        vehTypeList.add("4 wheeler");
        vehTypeList.add("Others");

        spnr_pov.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                    return;
                if(infoName.equalsIgnoreCase("Purpose of visit"))
                    et_itemvalue.setText(povList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final RadioGroup rg_gender = (RadioGroup) findViewById(R.id.rg_gender);
        rg_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) rg_gender.findViewById(checkedId);
                String gen = rb.getText().toString();
                et_itemvalue.setText(gen);
                //et_itemvalue.setVisibility(View.VISIBLE);
            }
        });
        rg_gender.setVisibility(View.GONE);
        ArrayAdapter<String> dataAdapter;
        switch (infoName){
            case "Age":
                et_itemvalue.setHint("in years");
                break;
            case "Gender":
                et_itemvalue.setVisibility(View.GONE);
                rg_gender.setVisibility(View.VISIBLE);
                break;
            case "Date of Birth":
                //new DatePickerDialog(fragmentProfile.getActivity(), datePickerListener,year,month,day).show();
                break;
            case "Purpose of visit":
                spnr_pov.setVisibility(View.VISIBLE);
                et_itemvalue.setText(infoValue);
                dataAdapter = new ArrayAdapter<String>(fragmentProfile.getActivity(), R.layout.item_spinner,povList);
                dataAdapter.setDropDownViewResource(R.layout.item_spinner);
                spnr_pov.setAdapter(dataAdapter);
                break;
            case "Vehicle No":
                spnr_pov.setVisibility(View.VISIBLE);
                dataAdapter = new ArrayAdapter<String>(fragmentProfile.getActivity(), R.layout.item_spinner,vehTypeList);
                dataAdapter.setDropDownViewResource(R.layout.item_spinner);
                spnr_pov.setAdapter(dataAdapter);
                if(infoValue.equalsIgnoreCase(""))
                    et_itemvalue.setHint("Vehicle number");
                else {
                    et_itemvalue.setText(infoValue.substring(1));
                    int defaultposition = 0;
                    int wheelertype = Integer.parseInt(infoValue.substring(0,1));
                    switch (wheelertype){
                        case 2:
                            defaultposition=1;
                            break;
                        case 3:
                            defaultposition=2;
                            break;
                        case 4:
                            defaultposition=3;
                            break;
                        case 0:
                            defaultposition=4;
                            break;
                        default:
                            break;
                    }
                    spnr_pov.setSelection(defaultposition);
                }
                break;
            case "Visiting Company":
                et_itemvalue.setHint("Company name");
                break;
            case "Meeting person":
                et_itemvalue.setHint("Contact person name");
                break;
            default:
                break;
        }
        btn_ok = (Button) findViewById(R.id.btn_ok);
        //this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String validationText = fragmentProfile.validateText(et_itemvalue.getText().toString(), infoName);
                if(infoName.equalsIgnoreCase("Vehicle No")){
                    int selectedposition = spnr_pov.getSelectedItemPosition();
                    int vtype = -1;
                    if(selectedposition == 0){
                        Toast.makeText(fragmentProfile.getActivity(), "Select vehicle type", Toast.LENGTH_SHORT).show();
                        return;
                    }else if(selectedposition == 1)
                        vtype = 2;
                    else if(selectedposition == 2)
                        vtype = 3;
                    else if(selectedposition == 3)
                        vtype = 4;
                    else if(selectedposition == 4)
                        vtype = 0;
                    validationText = ValidateVehicleNo(et_itemvalue.getText().toString());
                    if(validationText != null){
                        et_itemvalue.setText(vtype+validationText);
                        validationText = null;
                    }else
                        validationText = "Invalid vehicle no";
                }
                if(validationText == null) {
                    mydb.setCustomInfoItem(infoName, et_itemvalue.getText().toString());
                    fragmentProfile.handleCustomInfoScreen();
                    dismiss();
                }
                else
                    et_itemvalue.setError(validationText);
            }
        });
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
}
