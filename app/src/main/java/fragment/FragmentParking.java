package fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.VehicleList;

/**
 * Created by nviriyala on 08-07-2016.
 */
public class FragmentParking extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private FloatingActionButton fab;
    private LinearLayout ll_vehlist;
    private DBHelper mydb;
    private String PageName = "FragmentParking";
    private CountDownTask countDownTask;
    private Timer countDownTimer;
    private boolean TimerFlag = true;
    private CommonClasses cc;
    private TableLayout tableLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parking, container, false);
        try {
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setOnClickListener(FragmentParking.this);
            tableLayout = new TableLayout(getActivity());
            mydb = new DBHelper(getActivity());
            mydb.setSystemParameter("FragmentParkingNotification", "False");


            //mydb.setSystemParameter("AP 28 DV 6834", "0");
           // mydb.setSystemParameter("TS 07 EN 4422", "0");
           // mydb.setSystemParameter("SD 03 SF 0004", "0");
            /*mydb.deleteVehicleEntry();
            mydb.addVehicle("TS 07 EN 4422", 2);
            mydb.addVehicle("AP 28 DV 6834", 4);
            mydb.addVehicle("AP 28 DV 6835", 4);
            mydb.addVehicle("RF 35 FH 0355", 4);
            mydb.addVehicle("AP 11 AN 1111", 3);
            mydb.addVehicle("AP 11 AN 2222", 0);*/

            cc = new CommonClasses(getActivity());
            ll_vehlist = (LinearLayout) rootView.findViewById(R.id.ll_vehlist);
            countDownTask = new CountDownTask();
            countDownTimer = new Timer();
            populateVehicleList();
            checkExits();
            countDownTimer.schedule(countDownTask, 0, 1000);
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void checkExits(){
        try{
            JSONArray jsonArray = mydb.getVehicleExits();
            for(int i = 0; i < jsonArray.length(); i++){
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                String parktime = cc.substractDates(jsonObject.getString("ServerEntryDate"), jsonObject.getString("ServerExitDate"));
                Spanned textDecoration = Html.fromHtml("<u>"+jsonObject.getString("Entity")+"</u><br/><small>Entered : </small>"+jsonObject.getString("ServerEntryDate")+"<br/><small>Exited : </small>"+jsonObject.getString("ServerExitDate")+"<br/><br/><u>Park Time</u><br/>"+parktime+"<br/><br/><u>Parking Tariff</u><br/>"+jsonObject.getString("TariffAmt")+"/-");
                new AlertDialog.Builder(getActivity())
                    .setTitle(Html.fromHtml("<small><u>Vehicle</u></small><br/>"+jsonObject.getString("VehicleNo")))
                    .setMessage(textDecoration)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mydb.setSystemParameter(jsonObject.getString("VehicleNo"), "0");
                            }catch (Exception e){}
                        }
                    })
                    //.setIcon(getActivity().getResources().getIdentifier("wheeler" + jsonObject.getInt("VehicleType"), "drawable", getActivity().getPackageName()))
                    .show();
            }
        }
        catch (NullPointerException e){mydb.logAppError(PageName, "checkExits", "NullPointerException", e.getMessage());}
        catch (JSONException e){mydb.logAppError(PageName, "checkExits", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "checkExits", "Exception", e.getMessage());}
    }

    public void populateVehicleList(){
        try {
            List<VehicleList> lstEntries = mydb.getVehicleList();
            tableLayout.removeAllViews();
            if(lstEntries.size() == 0)
                return;
            LayoutInflater inflater = getActivity().getLayoutInflater();
            for(VehicleList entry : lstEntries){
                TableRow row = (TableRow)inflater.inflate(R.layout.row_vehicle, tableLayout, false);
                row.setOnClickListener(this);
                row.setOnLongClickListener(this);

                ImageView img_vehType = (ImageView) row.findViewById(R.id.img_vehType);
                img_vehType.setImageResource(getActivity().getResources().getIdentifier("wheeler" + entry.getVehicleType(), "drawable", getActivity().getPackageName()));

                String vehdetails = entry.getVehicleNo();
                Spanned textDecoration = Html.fromHtml("<b>"+vehdetails+"</b>");
                if(entry.getEntity() != null)
                    textDecoration = Html.fromHtml("<b>"+vehdetails+"</b><br/><small>"+entry.getEntity()+"</small>");

                if(entry.getEntryDate() != null)
                    textDecoration = Html.fromHtml("<b>"+vehdetails+"</b><br/><small>"+entry.getEntity()+"</small><br/><small>"+entry.getEntryDate()+"</small>");

                TextView tv_vehNumber = (TextView)row.findViewById(R.id.tv_vehNumber);
                tv_vehNumber.setText(textDecoration);

                TextView tv_tariff = (TextView)row.findViewById(R.id.tv_tariff);
                switch (entry.getTariffAmt()){
                    case 0:
                        tv_tariff.setText("");
                        tv_tariff.setBackground(null);
                        break;
                    case -1:
                        tv_tariff.setText("");
                        break;
                    default:
                        tv_tariff.setText(entry.getTariffAmt() + "/-");
                        break;
                }

                TextView tv_timer = (TextView)row.findViewById(R.id.tv_timer);
                switch (entry.getTimer()){
                    case 0:
                        tv_timer.setText("");
                        tv_timer.setBackground(null);
                        break;
                    case -1:
                        tv_timer.setText("");
                        break;
                    default:
                        String TimerValue = cc.getTimerValue(entry.getEntryDate(), entry.getTimer());
                        tv_timer.setText(TimerValue);
                        tv_timer.setTextColor(Color.RED);
                        break;
                }
                tableLayout.addView(row);
            }
            ll_vehlist.removeView(tableLayout);
            ll_vehlist.addView(tableLayout);
        }
        catch (Exception e){mydb.logAppError(PageName, "populateVehicleList", "Exception", e.getMessage());}
    }

    @Override
    public boolean onLongClick(View v) {
        try{
            TextView tv_vehNumber = (TextView)v.findViewById(R.id.tv_vehNumber);
            final String[] txt = tv_vehNumber.getText().toString().split("\n");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Delete Vehicle?");
            alertDialogBuilder.setTitle( Html.fromHtml("<font color='"+getResources().getString(R.string.colorprimary)+"'>Delete Vehicle?<br/>"+txt[0]+"</font>"));
            alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    mydb.deleteVehicle(txt[0]);
                    populateVehicleList();
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
        catch (Exception e){mydb.logAppError(PageName, "onLongClick", "Exception", e.getMessage());}
        return true;
    }

    public class CountDownTask extends TimerTask {
        public void run(){
            try {
                if(TimerFlag)
                    new updateTimerAsync().execute();
                else{
                    if(countDownTimer != null){
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    if(countDownTask != null){
                        countDownTask.cancel();
                        countDownTask = null;
                    }
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "CountDownTask", "Exception", e.getMessage());}
        }
    }

    public class updateTimerAsync extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object obj) {
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(obj);
            try{
                TimerFlag = false;
                for(int i = 0; i < tableLayout.getChildCount(); i++){
                    TableRow row = (TableRow) tableLayout.getChildAt(i);
                    TextView tv = (TextView) row.findViewById(R.id.tv_timer);
                    if(!(tv.getText().toString().equalsIgnoreCase("Time Up") || tv.getText().toString().equalsIgnoreCase("") || tv.getText().toString().equalsIgnoreCase("*"))){
                        String timerText = tv.getText().toString();
                        long inTime = cc.StringToMilli(timerText);
                        TimerFlag = true;
                        timerText = cc.MilliToString(inTime-1000);
                        tv.setText(timerText);
                    }
                }
            }
            catch (Exception e){
                mydb.logAppError(PageName, "updateTimerAsync--onPostExecute", "Exception", e.getMessage());
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = null;
            if (v.getId() == R.id.fab) {
                fragment = new FragmentAddVehicle();
                fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
                fragmentTransaction.replace(R.id.container_body, fragment, "FragmentAddVehicle");
                fragmentTransaction.commit();
                return;
            }

            TextView tv_vehNumber = (TextView)v.findViewById(R.id.tv_vehNumber);
            String[] txt = tv_vehNumber.getText().toString().split("\n");
            //Toast.makeText(getActivity(), "onClick -- "+txt[0], Toast.LENGTH_SHORT).show();
            fragment = new FragmentParkingStub();
            Bundle bundle = new Bundle();
            bundle.putString("VehicleNo",txt[0]);
            fragment.setArguments(bundle);
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            fragmentTransaction.replace(R.id.container_body, fragment, "FragmentParkingStub");
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            if (countDownTimer == null) {
                countDownTimer = new Timer();
                countDownTask = new CountDownTask();
            }

            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Parking");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    @Override
    public void onPause(){
        super.onPause();
        if(countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if(countDownTask != null){
            countDownTask.cancel();
            countDownTask = null;
        }
    }
}

