package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenow.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DBHelper;

/**
 * Created by nviriyala on 08-07-2016.
 */
public class FragmentAddVehicle extends Fragment implements View.OnClickListener {
    private DBHelper mydb;
    private String PageName = "FragmentAddVehicle";

    private Button btn_Set;
    private Button btn_Cancel;

    private ImageView img_wheeler2;
    private ImageView img_wheeler3;
    private ImageView img_wheeler4;
    private ImageView img_wheeler0;


    private EditText et_vehno;
    private int VehicleType = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_addvehicle, container, false);
        mydb = new DBHelper(getActivity());
        et_vehno = (EditText) rootView.findViewById(R.id.et_vehno);

        btn_Set = (Button) rootView.findViewById(R.id.btn_Set);
        btn_Cancel = (Button) rootView.findViewById(R.id.btn_Cancel);
        btn_Set.setOnClickListener(this);
        btn_Cancel.setOnClickListener(this);

        img_wheeler2 = (ImageView) rootView.findViewById(R.id.img_wheeler2);
        img_wheeler3 = (ImageView) rootView.findViewById(R.id.img_wheeler3);
        img_wheeler4 = (ImageView) rootView.findViewById(R.id.img_wheeler4);
        img_wheeler0 = (ImageView) rootView.findViewById(R.id.img_wheeler0);
        img_wheeler2.setOnClickListener(this);
        img_wheeler3.setOnClickListener(this);
        img_wheeler4.setOnClickListener(this);
        img_wheeler0.setOnClickListener(this);
        return rootView;
    }

    public void getBack(){
        try{
            Fragment fragment = new FragmentParking();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
            fragmentTransaction.replace(R.id.container_body, fragment, "FragmentParking");
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "getBack", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Set:
                if(!ValidateVehicleNo()) {
                    if(et_vehno.getText().toString().equalsIgnoreCase(""))
                        Toast.makeText(getActivity(), "Enter a vehicle Number", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Not a Valid vehicle No", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    if(VehicleType == -1)
                        Toast.makeText(getActivity(), "Select Vehicle Type", Toast.LENGTH_SHORT).show();
                    else {
                        mydb.addVehicle(et_vehno.getText().toString(), VehicleType);
                        getBack();
                    }
                }
                break;
            case R.id.btn_Cancel:
                getBack();
                break;
            case R.id.img_wheeler2:
                selectVehicle("2");
                break;
            case R.id.img_wheeler3:
                selectVehicle("3");
                break;
            case R.id.img_wheeler4:
                selectVehicle("4");
                break;
            case R.id.img_wheeler0:
                selectVehicle("0");
                break;
            default:
                break;
        }
    }

    public void selectVehicle(String vehicleType){
        try{
            if(vehicleType.equalsIgnoreCase("2")){
                img_wheeler2.setBackgroundResource(R.drawable.border_selected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                VehicleType = 2;
            }
            if(vehicleType.equalsIgnoreCase("3")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_selected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                VehicleType = 3;
            }
            if(vehicleType.equalsIgnoreCase("4")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_selected);
                img_wheeler0.setBackgroundResource(R.drawable.border_unselected);
                VehicleType = 4;
            }
            if(vehicleType.equalsIgnoreCase("0")){
                img_wheeler2.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler3.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler4.setBackgroundResource(R.drawable.border_unselected);
                img_wheeler0.setBackgroundResource(R.drawable.border_selected);
                VehicleType = 0;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "selectVehicle", "Exception", e.getMessage());}
    }

    public boolean ValidateVehicleNo(){
        boolean retValue = false;
        String vehno = et_vehno.getText().toString().replace(" ","");
        try{
            if(vehno == null || vehno.equalsIgnoreCase("") || vehno.length() < 4 || vehno.length() > 10)
                return retValue;

            Pattern pattern1 = Pattern.compile("^[A-Za-z]{3}[0-9]{1,4}$");
            Pattern pattern2 = Pattern.compile("^[A-Za-z]{2}[0-9]{1,2}[A-Za-z]{1,2}[0-9]{1,4}$");

            Matcher matcher1 = pattern1.matcher(vehno);
            Matcher matcher2 = pattern2.matcher(vehno);

            if(matcher1.matches()) {
                while(vehno.length() < 7) {
                    vehno = vehno.substring(0, 3)+"0"+vehno.substring(3);
                }

                vehno = vehno.substring(0, 3)+" "+vehno.substring(3);
                et_vehno.setText(vehno.toUpperCase());
                return true;
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

                et_vehno.setText(vehno.toUpperCase());
                return true;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onDrawerItemSelected", "Exception", e.getMessage());}
        return  retValue;
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Add Vehicle");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
