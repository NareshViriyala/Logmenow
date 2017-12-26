package fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.Image;
import android.media.TimedText;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Timer;

import database.DBHelper;
import shared.CommonClasses;
import shared.GlobalClass;
import shared.HTTPCallJSon;
import shared.NetworkDetector;

/**
 * Created by nviriyala on 19-07-2016.
 */
public class FragmentRestaurant extends Fragment implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentRestaurant";
    private NetworkDetector nd;

    private ImageView img_restaurant;
    private ImageView img_logoprocessing;

    private LinearLayout ll_address;
    private ImageView img_dtlprocessing;

    private LinearLayout ll_businesshours;
    private ImageView img_bhprocessing;
    private TextView tv_busstitle;

    private TextView btn_scanagain;
    private TextView btn_getmenu;

    private ImageView img_getmenu_processing;
    private ImageView img_scanagain_processing;

    private GlobalClass gc;
    private CommonClasses cc;

    private String guid = "";
    private String restBussHours = "";
    private String restDetails = "";
    private byte[] restImage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restaurant, container, false);
        try {
            mydb = new DBHelper(getActivity());
            nd = new NetworkDetector(getActivity());
            gc = (GlobalClass) getActivity().getApplicationContext();
            cc = new CommonClasses(getActivity());

            guid = mydb.getSystemParameter("RestaurantGUID");
            restBussHours = mydb.getSystemParameter("RestaurantBusinessHours");
            restDetails = mydb.getSystemParameter("RestaurantDetails");
            restImage = mydb.getEntityImage("Restaurant");

            img_restaurant = (ImageView) rootView.findViewById(R.id.img_restaurant);
            img_logoprocessing = (ImageView) rootView.findViewById(R.id.img_logoprocessing);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_logoprocessing);

            ll_address = (LinearLayout) rootView.findViewById(R.id.ll_address);
            img_dtlprocessing = (ImageView) rootView.findViewById(R.id.img_dtlprocessing);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_dtlprocessing);

            ll_businesshours = (LinearLayout) rootView.findViewById(R.id.ll_businesshours);
            tv_busstitle = (TextView) rootView.findViewById(R.id.tv_busstitle);
            img_bhprocessing = (ImageView) rootView.findViewById(R.id.img_bhprocessing);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_bhprocessing);

            btn_scanagain = (TextView) rootView.findViewById(R.id.btn_scanagain);
            btn_scanagain.setOnClickListener(this);

            btn_getmenu = (TextView) rootView.findViewById(R.id.btn_getmenu);
            btn_getmenu.setOnClickListener(this);

            img_scanagain_processing = (ImageView) rootView.findViewById(R.id.img_scanagain_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_scanagain_processing);
            img_getmenu_processing = (ImageView) rootView.findViewById(R.id.img_getmenu_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_getmenu_processing);

            if(nd.isInternetAvailable()){
                new getImage().execute(guid);
                new getDetails().execute(guid);
                new getBusinessHours().execute(guid);
            }
            else {
                Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                img_logoprocessing.setVisibility(View.GONE);
                img_dtlprocessing.setVisibility(View.GONE);
                img_bhprocessing.setVisibility(View.GONE);

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
                case R.id.btn_getmenu:
                    btn_getmenu.setVisibility(View.GONE);
                    img_getmenu_processing.setVisibility(View.VISIBLE);
                    fragment = new FragmentRestaurantMenu();
                    tag = "FragmentRestaurantMenu";
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                    break;
                case R.id.btn_scanagain:
                    btn_scanagain.setVisibility(View.GONE);
                    img_scanagain_processing.setVisibility(View.VISIBLE);
                    fragment = new FragmentScanQR();
                    tag = "FragmentScanQR";
                    fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                    break;
                default:
                    break;
            }
            fragmentTransaction.replace(R.id.container_body, fragment, tag);
            fragmentTransaction.commit();

        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class getImage extends AsyncTask<String, Integer, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //byte[] bytes = mydb.getEntityImage("Restaurant");
            Bitmap bitmap = null;
            try {
                if(restImage != null)
                    bitmap = cc.getBitmap(restImage);
                else{
                    //bitmap = new HTTPCallJSon(getActivity()).GetImage("GetEntityImage", "?id=" + params[0]);
                    //mydb.setEntityImage("Restaurant", bitmap);

                    String response = new HTTPCallJSon(getActivity()).Get("GetEntityImage", "?id="+params[0]);
                    InputStream in = new java.net.URL(response).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    mydb.setEntityImage("Restaurant", bitmap);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getImage--doInBackground", "Exception", e.getMessage());}
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try {
                if(bitmap == null) {
                    //Toast.makeText(getActivity(), "Image is null", Toast.LENGTH_SHORT).show();
                    img_logoprocessing.setVisibility(View.GONE);
                    img_restaurant.setVisibility(View.VISIBLE);
                    img_restaurant.setImageResource(R.drawable.ic_noimage);
                }
                else {
                    img_logoprocessing.setVisibility(View.GONE);
                    img_restaurant.setVisibility(View.VISIBLE);
                    img_restaurant.setImageBitmap(bitmap);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getImage--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getDetails extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //String response = mydb.getSystemParameter("RestaurantDetails");
            try {
                if(restDetails.equalsIgnoreCase("")) {
                    restDetails = new HTTPCallJSon(getActivity()).Get("GetEntityDetails", "?id=" + params[0]);
                    restDetails = new JSONArray(restDetails).getJSONObject(0).toString();
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getDetails--doInBackground", "Exception", e.getMessage());}
            return restDetails;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {

                JSONObject jobj = new JSONObject(response);
                TextView tv_name = (TextView) ll_address.findViewById(R.id.tv_name);
                tv_name.setText(jobj.getString("Ename"));

                TextView tv_address1 = (TextView) ll_address.findViewById(R.id.tv_address1);
                tv_address1.setText(jobj.getString("Add1"));

                TextView tv_address2 = (TextView) ll_address.findViewById(R.id.tv_address2);

                if(jobj.getString("Add2").equalsIgnoreCase(""))
                    tv_address2.setVisibility(View.GONE);
                else
                    tv_address2.setText(jobj.getString("Add2"));

                TextView tv_cscz = (TextView) ll_address.findViewById(R.id.tv_cscz);
                tv_cscz.setText(jobj.getString("City")+", "+jobj.getString("State")+", "+jobj.getString("Country")+", "+jobj.getString("Zip"));

                TextView tv_cnt = (TextView) ll_address.findViewById(R.id.tv_cnt);
                String contactno = "";
                if(!jobj.getString("ContactNo1").equalsIgnoreCase(""))
                    contactno = jobj.getString("ContactNo1");

                if(!jobj.getString("ContactNo2").equalsIgnoreCase("")){
                    if(contactno.equalsIgnoreCase(""))
                        contactno = jobj.getString("ContactNo2");
                    else
                        contactno = contactno+", "+jobj.getString("ContactNo2");
                }

                if(contactno.equalsIgnoreCase(""))
                    tv_cnt.setVisibility(View.GONE);
                else
                    tv_cnt.setText(contactno);

                TextView tv_email = (TextView) ll_address.findViewById(R.id.tv_email);
                if(jobj.getString("EmailID").equalsIgnoreCase(""))
                    tv_email.setVisibility(View.GONE);
                else
                    tv_email.setText(jobj.getString("EmailID"));

                TextView tv_web = (TextView) ll_address.findViewById(R.id.tv_web);
                if(jobj.getString("WebSite").equalsIgnoreCase(""))
                    tv_web.setVisibility(View.GONE);
                else
                    tv_web.setText(jobj.getString("WebSite"));


                img_dtlprocessing.setVisibility(View.GONE);
                ll_address.setVisibility(View.VISIBLE);
                mydb.setSystemParameter("RestaurantDetails", response);
            }
            catch (Exception e){mydb.logAppError(PageName, "getDetails--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public class getBusinessHours extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            //android.os.Debug.waitForDebugger();
            //String response = mydb.getSystemParameter("RestaurantBusinessHours");
            //String response = "[{\"Day\":\"Mon\",\"BH\":\"10AM - 10PM\"},{\"Day\":\"Tue\",\"BH\":\"10AM - 10PM\"},{\"Day\":\"Wed\",\"BH\":\"10AM - 10PM\"},{\"Day\":\"Thu\",\"BH\":\"10AM - 10PM\"},{\"Day\":\"Fri\",\"BH\":\"10AM - 10PM\"},{\"Day\":\"Sat\",\"BH\":\"10AM - 10PM\"}]";

            try {
                if(restBussHours.equalsIgnoreCase(""))
                    restBussHours = new HTTPCallJSon(getActivity()).Get("GetBusinessHours", "?id=" + params[0]);
            }
            catch (Exception e){mydb.logAppError(PageName, "getDetails--doInBackground", "Exception", e.getMessage());}
            return restBussHours;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {

                JSONArray jsonArray = new JSONArray(response);
                TableLayout tableLayout = new TableLayout(getActivity());
                //tableLayout.setGravity(Gravity.CENTER_VERTICAL);
                LayoutInflater inflater = getActivity().getLayoutInflater();
                for(int i = 0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    TableRow row = (TableRow)inflater.inflate(R.layout.row_business_hours, tableLayout, false);

                    TextView tv_day = (TextView) row.findViewById(R.id.tv_day);
                    tv_day.setText(jsonObject.getString("Day"));

                    TextView tv_bh = (TextView) row.findViewById(R.id.tv_bh);
                    tv_bh.setText(jsonObject.getString("BH"));

                    tableLayout.addView(row);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                ll_businesshours.addView(tableLayout, params);
                tv_busstitle.setVisibility(View.VISIBLE);
                img_bhprocessing.setVisibility(View.GONE);
                mydb.setSystemParameter("RestaurantBusinessHours", response);
            }
            catch (Exception e){mydb.logAppError(PageName, "getDetails--onPostExecute", "Exception", e.getMessage());}
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Restaurant");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
