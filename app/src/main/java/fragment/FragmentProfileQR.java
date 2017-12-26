package fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.home.logmenow.R;
import database.DBHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.widget.PopupWindow;

import org.json.JSONObject;

/**
 * Created by Home on 7/13/2016.
 */
public class FragmentProfileQR extends Fragment implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentProfileQR";

    //private ImageView img_adddetails;

    private TextView tv_profileinfo;
    private ImageView img_QRCode;
    private TextView tv_title;
    private LinearLayout ll_info;


    private String texttoqr;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.fragment_profileqr, container, false);
            mydb = new DBHelper(getActivity());

            //img_adddetails = (ImageView) rootView.findViewById(R.id.img_adddetails);
            //img_adddetails.setOnClickListener(this);

            ll_info = (LinearLayout) rootView.findViewById(R.id.ll_info);
            ll_info.setOnClickListener(this);

            tv_profileinfo = (TextView) rootView.findViewById(R.id.tv_profileinfo);
            tv_title = (TextView) rootView.findViewById(R.id.tv_title);
            img_QRCode = (ImageView) rootView.findViewById(R.id.img_QRCode);

            texttoqr = getArguments().getString("texttoqr");
            tv_profileinfo.setText(texttoqr);

            title = getArguments().getString("title");
            tv_title.setText(title);
            String actualtexttoqr = textToQR(title);
            new GenerateQRCode().execute(actualtexttoqr);
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public String textToQR(String tag){
        String texttoqr = "1."+mydb.getDeviceID()+"\n";
        try{
            JSONObject jobj;
            switch (tag){
                case "Basic Information":
                    jobj = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
                    texttoqr = texttoqr+"2."+jobj.getString("FirstName")+"\n";
                    if(!jobj.getString("MiddleName").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"3."+jobj.getString("MiddleName")+"\n";
                    if(!jobj.getString("LastName").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"4."+jobj.getString("LastName")+"\n";
                    if(!jobj.getString("Email").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"5."+jobj.getString("Email")+"\n";
                    if(!jobj.getString("Phone").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"6."+jobj.getString("Phone")+"\n";
                    break;
                case "Home Address":
                    jobj = new JSONObject(mydb.getProfileInfo("HomeAddress"));
                    texttoqr = texttoqr+"7."+jobj.getString("HAddress1")+"\n";
                    if(!jobj.getString("HAddress2").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"8."+jobj.getString("HAddress2")+"\n";
                    if(!jobj.getString("HLandMark").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"9."+jobj.getString("HLandMark")+"\n";
                    texttoqr = texttoqr+"10."+jobj.getString("HCity")+"\n";
                    texttoqr = texttoqr+"11."+jobj.getString("HState")+"\n";
                    texttoqr = texttoqr+"12."+jobj.getString("HCountry")+"\n";
                    texttoqr = texttoqr+"13."+jobj.getString("HZip")+"\n";
                    break;
                case "Office Address":
                    jobj = new JSONObject(mydb.getProfileInfo("OfficeAddress"));
                    texttoqr = texttoqr+"14."+jobj.getString("OAddress1")+"\n";
                    if(!jobj.getString("OAddress2").equalsIgnoreCase(""))
                        texttoqr = texttoqr+"15."+jobj.getString("OAddress2")+"\n";
                    texttoqr = texttoqr+"16."+jobj.getString("OCity")+"\n";
                    texttoqr = texttoqr+"17."+jobj.getString("OState")+"\n";
                    texttoqr = texttoqr+"18."+jobj.getString("OCountry")+"\n";
                    texttoqr = texttoqr+"19."+jobj.getString("OZip")+"\n";
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "textToQR", "Exception", e.getMessage());}
        return texttoqr;
    }

    @Override
    public void onClick(View v) {
        try{
            Fragment fragment = new FragmentProfileSaved();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            fragmentTransaction.replace(R.id.container_body, fragment, "FragmentProfileSaved");
            fragmentTransaction.commit();
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class GenerateQRCode extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute(){
            //dps.show();
        }

        @Override
        protected Bitmap doInBackground(String[] params) {
            //android.os.Debug.waitForDebugger();
            Bitmap QRCode = null;
            try {
                String TexttoQR = params[0];
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
            catch (WriterException e){mydb.logAppError(PageName, "doInBackground", "Exception", e.getMessage());}
            return QRCode;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try{
                //Toast.makeText(getActivity(), img_QRCode.getWidth()+"", Toast.LENGTH_SHORT).show();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(700, 700);
                img_QRCode.setLayoutParams(layoutParams);
                img_QRCode.setImageBitmap(bitmap);
                //dps.dismiss();
            }
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute", "Exception", e.getMessage());}
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Profile Code");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

}
