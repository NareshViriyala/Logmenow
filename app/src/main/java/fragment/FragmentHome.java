package fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.home.logmenow.R;


import java.util.Timer;

import database.DBHelper;

/**
 * Created by Home on 7/9/2016.
 */
public class FragmentHome extends Fragment implements View.OnClickListener{
    private LinearLayout ll_parking;
    private LinearLayout ll_membership;
    private LinearLayout ll_scanqr;
    private LinearLayout ll_profile;
    private LinearLayout ll_history;
    private LinearLayout ll_offers;
    private RelativeLayout rl_fraghome;
    //private Fragment fragment = null;

    public ImageView img_parking_notify;
    public ImageView img_qr_notify;

    private View rootView;
    private DBHelper mydb;
    private String PageName = "FragmentHome";
    private String prevGrid = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mydb = new DBHelper(getActivity());
        rl_fraghome = (RelativeLayout) rootView.findViewById(R.id.rl_fraghome);
        ll_parking = (LinearLayout) rootView.findViewById(R.id.ll_parking);
        ll_parking.setOnClickListener(this);
        ll_membership = (LinearLayout) rootView.findViewById(R.id.ll_membership);
        ll_membership.setOnClickListener(this);
        ll_scanqr = (LinearLayout) rootView.findViewById(R.id.ll_scanqr);
        ll_scanqr.setOnClickListener(this);
        ll_profile = (LinearLayout) rootView.findViewById(R.id.ll_profile);
        ll_profile.setOnClickListener(this);
        ll_history = (LinearLayout) rootView.findViewById(R.id.ll_history);
        ll_history.setOnClickListener(this);
        ll_offers = (LinearLayout) rootView.findViewById(R.id.ll_offers);
        ll_offers.setOnClickListener(this);
        showNotifications();


        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadView(rootView);
            }
        }, 100);*/
        return rootView;
    }

    /*public void loadView(View rootView){
        try{
            rl_fraghome = (RelativeLayout) rootView.findViewById(R.id.rl_fraghome);
            ll_parking = (LinearLayout) rootView.findViewById(R.id.ll_parking);
            ll_parking.setOnClickListener(this);
            ll_membership = (LinearLayout) rootView.findViewById(R.id.ll_membership);
            ll_membership.setOnClickListener(this);
            ll_scanqr = (LinearLayout) rootView.findViewById(R.id.ll_scanqr);
            ll_scanqr.setOnClickListener(this);
            ll_profile = (LinearLayout) rootView.findViewById(R.id.ll_profile);
            ll_profile.setOnClickListener(this);
            ll_history = (LinearLayout) rootView.findViewById(R.id.ll_history);
            ll_history.setOnClickListener(this);
            ll_offers = (LinearLayout) rootView.findViewById(R.id.ll_offers);
            ll_offers.setOnClickListener(this);
            showNotifications();
        }
        catch (Exception e){mydb.logAppError(PageName, "loadView", "Exception", e.getMessage());}
    }*/

    public void showNotifications(){
        try{

            if(mydb.getSystemParameter("FragmentParkingNotification").equalsIgnoreCase("True")) {
                img_parking_notify = (ImageView) rootView.findViewById(R.id.img_parking_notify);
                img_parking_notify.setVisibility(View.VISIBLE);
            }

            if(mydb.getSystemParameter("FragmentHospitalNotification").equalsIgnoreCase("True")) {
                img_qr_notify = (ImageView) rootView.findViewById(R.id.img_qr_notify);
                img_qr_notify.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "showNotifications", "Exception", e.getMessage());}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        try {
            String gridname = v.getResources().getResourceName(v.getId());
            int spechar = gridname.indexOf('_');
            gridname = gridname.substring(spechar + 1);

            if(prevGrid != null){
                View previousView = rl_fraghome.findViewById(this.getResources().getIdentifier("ll_" + prevGrid, "id", getActivity().getPackageName()));
                ImageView img_grid = (ImageView) previousView.findViewById(this.getResources().getIdentifier("img_" + prevGrid, "id", getActivity().getPackageName()));
                img_grid.setColorFilter(getResources().getColor(R.color.colorPrimary));
                TextView tv_grid = (TextView) previousView.findViewById(this.getResources().getIdentifier("tv_" + prevGrid, "id", getActivity().getPackageName()));
                tv_grid.setTextColor(getResources().getColor(R.color.colorPrimary));
                previousView.setBackgroundColor(getResources().getColor(R.color.white));
            }
            prevGrid = gridname;
            ImageView img_grid = (ImageView) v.findViewById(this.getResources().getIdentifier("img_" + gridname, "id", getActivity().getPackageName()));
            img_grid.setColorFilter(getResources().getColor(R.color.white));
            TextView tv_grid = (TextView) v.findViewById(this.getResources().getIdentifier("tv_" + gridname, "id", getActivity().getPackageName()));
            tv_grid.setTextColor(getResources().getColor(R.color.white));
            v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            loadFragment(gridname);
            /*Activity activity = getActivity();
            if(activity instanceof HomeActivity){
                HomeActivity myactivity = (HomeActivity) activity;
                myactivity.selectDrawerItem(null);
            }*/
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public void loadFragment(String fragmentName){
        try{
            //fragment = null;
            String title = "";
            switch(fragmentName) {
                case "parking":
                    //fragment = new FragmentParking();
                    title = "Parking";
                    fragmentName = "FragmentParking";
                    break;
                case "profile":
                    //fragment = new FragmentProfileSaved();
                    title = "Profile";
                    fragmentName = "FragmentProfile";
                    break;
                case "scanqr":
                    //fragment = new FragmentScanQR();
                    title = "Scan QR";
                    fragmentName = "FragmentScanQR";
                    break;
                case "history":
                    fragmentName = "FragmentHistory";
                    break;
                default:
                    fragmentName = null;
                    Toast.makeText(getActivity(), "Feature not available yet", Toast.LENGTH_SHORT).show();
                    break;
            }

            if (fragmentName != null) {
                Class<?> c = Class.forName("fragment."+fragmentName);
                Fragment fragment = (Fragment) c.newInstance();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                fragmentTransaction.replace(R.id.container_body, fragment, fragmentName);
                fragmentTransaction.commit();
            }
            //getActivity().setTitle(title);
        }
        catch (Exception e){mydb.logAppError(PageName, "loadFragment", "Exception", e.getMessage());}
    }


    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Home");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.GONE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
