package fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.home.logmenow.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nviriyala on 08-07-2016.
 */
public class FragmentParkingStub extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private DBHelper mydb;
    private String PageName = "FragmentParkingStub";
    private ImageView img_QRCode;
    private boolean TimerFlag = true;
    private CommonClasses cc;
    private CountDownTask countDownTask;
    private Timer countDownTimer;
    private String VehicleNo;
    //private int VehicleType;
    //private RelativeLayout rl_fragpark;
    //private DialogProcessingStatus dps;

    private TableLayout tableLayout;
    private LinearLayout ll_vehlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parking_stub, container, false);
        mydb = new DBHelper(getActivity());
        img_QRCode = (ImageView) rootView.findViewById(R.id.img_QRCode);

        ll_vehlist = (LinearLayout) rootView.findViewById(R.id.ll_vehlist);
        tableLayout = new TableLayout(getActivity());
        //Toast.makeText(getActivity(), img_QRCode.getWidth()+"", Toast.LENGTH_SHORT).show();
        //Glide.with(getActivity()).load(R.drawable.processing).into(img_QRCode);
        Bundle bundle = this.getArguments();
        VehicleNo = bundle.getString("VehicleNo");
        cc = new CommonClasses(getActivity());
        countDownTask = new CountDownTask();
        countDownTimer = new Timer();
        populateVehicleList();
        String[] TexttoQR = new String[4];
        TexttoQR[0] = mydb.getDeviceID();
        TexttoQR[1] = VehicleNo;
        TexttoQR[2] = mydb.getVehicleType(VehicleNo)+"";
        //TexttoQR[3] = "0";
        new GenerateQRCode().execute(TexttoQR);
        countDownTimer.schedule(countDownTask, 0, 1000);
        return rootView;
    }

    public void populateVehicleList(){
        try {
            List<Models.VehicleList> lstEntries = mydb.getVehicleList(VehicleNo);
            tableLayout.removeAllViews();
            if(lstEntries.size() == 0)
                return;
            LayoutInflater inflater = getActivity().getLayoutInflater();
            for(Models.VehicleList entry : lstEntries){
                TableRow row = (TableRow)inflater.inflate(R.layout.row_vehicle, tableLayout, false);
                row.setOnClickListener(this);
                row.setOnLongClickListener(this);

                ImageView img_direction = (ImageView) row.findViewById(R.id.img_direction);
                img_direction.setImageResource(R.drawable.ic_left);

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
        try {

            TextView tv_vehNumber = (TextView)v.findViewById(R.id.tv_vehNumber);
            final String[] txt = tv_vehNumber.getText().toString().split("\n");
            if(txt.length == 1)
                return true;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Delete Entry?");
            alertDialogBuilder.setTitle( Html.fromHtml("<font color='"+getResources().getString(R.string.colorprimary)+"'>Delete Entry?</font>"));
            alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    mydb.deleteVehicleEntry(txt[0], txt[1]);
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
            // set the toolbar title
            //((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle("Parking--Stub");
        }
        catch (Exception e){mydb.logAppError(PageName, "onLongClick", "Exception", e.getMessage());}
        return true;
    }

    @Override
    public void onClick(View v) {
        try {
            Fragment fragment = new FragmentParking();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            fragmentTransaction.replace(R.id.container_body, fragment, "FragmentParking");
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
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
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class GenerateQRCode extends AsyncTask<String, Integer, Bitmap>{

        @Override
        protected void onPreExecute(){
            //dps.show();
        }

        @Override
        protected Bitmap doInBackground(String[] params) {
            //android.os.Debug.waitForDebugger();
            Bitmap QRCode = null;
            try {
                String TexttoQR = params[0]+"\n"+params[1]+"\n"+params[2];
                BitMatrix result = new MultiFormatWriter().encode(TexttoQR, BarcodeFormat.QR_CODE, 300, 300, null);
                int w = result.getWidth();
                int h = result.getHeight();
                int[] pixels = new int[w*h];
                for (int y = 0; y < h; y++) {
                    int offset = y * w;
                    for (int x = 0; x < w; x++) {
                        pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white);
                    }
                }
                QRCode = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                QRCode.setPixels(pixels, 0, 300, 0, 0, w, h);
            }
            catch (WriterException e){

            }
            return QRCode;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try{
                //Toast.makeText(getActivity(), img_QRCode.getWidth()+"", Toast.LENGTH_SHORT).show();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(560, 560);
                //layoutParams.setMargins(10,10,10,10);
                img_QRCode.setLayoutParams(layoutParams);
                img_QRCode.setImageBitmap(bitmap);
                //img_QRCode.setPadding(10,10,10,10);

                //dps.dismiss();
            }
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute", "Exception", e.getMessage());}
        }
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
            tv_title.setText("Vehicle");

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
