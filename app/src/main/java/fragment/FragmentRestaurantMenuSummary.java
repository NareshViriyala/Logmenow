package fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import activity.ActivityHome;
import database.DBHelper;
import gcm.PushNotification;
import shared.CommonClasses;
import dialog.DialogInformation;
import dialog.DialogRestOrderPlaced;
import shared.GlobalClass;
import shared.HTTPCallJSon;
import shared.LocationHelper;
import shared.Models;
import shared.NetworkDetector;



/**
 * Created by Home on 7/21/2016.
 */
public class FragmentRestaurantMenuSummary extends Fragment implements View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentRestaurantMenuSummary";
    private NetworkDetector nd;
    private LinearLayout ll_resmenu;
    private List<Models.RestaurantMenuItem> selecteditemslist = new ArrayList<Models.RestaurantMenuItem>();
    private GlobalClass gc;
    private CommonClasses cc;
    private LocationHelper lh;
    private LinearLayout ll_callwaiter;
    private TextView btn_modo;
    private TextView btn_plco;
    //private String[] taxes ={"Sub Total","Service Charge", "Service Tax", "Vat Tax", "Swachh Bharat Cess", "Krishi Kalyan Cess"};
    //private double[] taxesper = {100, 5, 4.94, 5, 0.5, 0.5};

    //private String[] taxes ={"Sub Total", "Service Tax", "Vat Tax", "Swachh Bharat Cess"};
    //private double[] taxesper = {100, 4.94, 5, 0.5};
    private View rootView = null;
    private double totalbill = 0;
    private String guid = "";
    private String DeviceID;

    private TableLayout tl_taxes = null;
    private TableLayout tl_total = null;

    private ImageView img_modo_processing;
    private ImageView img_plco_processing;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.fragment_restaurantmenusummary, container, false);
            mydb = new DBHelper(getActivity());
            gc = (GlobalClass) getActivity().getApplicationContext();
            cc = new CommonClasses(getActivity());
            DeviceID =  mydb.getDeviceID();
            guid = mydb.getSystemParameter("RestaurantGUID");
            selecteditemslist = mydb.getSavedMenuItems("SelectedItems");
            nd = new NetworkDetector(getActivity());
            ll_resmenu = (LinearLayout) rootView.findViewById(R.id.ll_resmenu);
            ll_callwaiter = (LinearLayout) rootView.findViewById(R.id.ll_callwaiter);
            ll_callwaiter.setOnClickListener(this);
            if(mydb.getSystemParameter("OrderPlacedID").equalsIgnoreCase(""))
                ll_callwaiter.setVisibility(View.INVISIBLE);

            btn_plco = (TextView) rootView.findViewById(R.id.btn_plco);
            btn_plco.setOnClickListener(this);
            btn_modo = (TextView) rootView.findViewById(R.id.btn_modo);
            btn_modo.setOnClickListener(this);

            img_modo_processing = (ImageView) rootView.findViewById(R.id.img_modo_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_modo_processing);

            img_plco_processing = (ImageView) rootView.findViewById(R.id.img_plco_processing);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_plco_processing);

            if(nd.isInternetAvailable()) {
                addItemList( selecteditemslist);
                new getTaxes().execute();
            }
            else
                Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public void resetTaxes(){
        try{
            double subtotal = 0;
            for (Models.RestaurantMenuItem sitem:selecteditemslist) {
                subtotal = subtotal+(sitem.getIP()*sitem.getQTY());
            }

            totalbill = 0;

            for(int i = 0; i<tl_taxes.getChildCount();i++){
                TableRow row = (TableRow) tl_taxes.getChildAt(i);
                TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
                TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);

                JSONArray ja = new JSONArray(mydb.getSystemParameter("RestaurantTaxes"));
                for(int j = 0; j <ja.length(); j++){
                    JSONObject jo = ja.getJSONObject(j);
                    if(jo.getString("TaxName").equalsIgnoreCase(tv_itemname.getText().toString())){
                        double amt = subtotal*(jo.getDouble("TaxPercentage")/100);
                        amt = Math.round(amt*100.0)/100.0;
                        totalbill = totalbill + amt;
                        tv_itemprice.setText(amt+"/-");
                    }
                }
            }

            TableRow row = (TableRow) tl_total.getChildAt(0);
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(Math.round(totalbill)+"/-");
        }
        catch (Exception e){mydb.logAppError(PageName, "loadSummary", "Exception", e.getMessage());}
    }

    public void calculateTotal(){
        try{
            tl_total = new TableLayout(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tl_total, false);

            LinearLayout ll_col1 = (LinearLayout) row.findViewById(R.id.ll_col1);
            ll_col1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            LinearLayout ll_col2 = (LinearLayout) row.findViewById(R.id.ll_col2);
            ll_col2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            img_itemtype.setVisibility(View.GONE);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText("Total");
            tv_itemname.setGravity(Gravity.RIGHT);
            tv_itemname.setTextColor(getResources().getColor(R.color.white));


            //Item qty +
            ImageView img_qtyp = (ImageView) row.findViewById(R.id.img_qtyp);
            img_qtyp.setVisibility(View.GONE);

            //Item qty -
            ImageView img_qtym = (ImageView) row.findViewById(R.id.img_qtym);
            img_qtym.setVisibility(View.GONE);

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setVisibility(View.GONE);


            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(Math.round(totalbill)+"/-");
            tv_itemprice.setTextColor(getResources().getColor(R.color.white));
            tl_total.addView(row);
            ll_resmenu.addView(tl_total);
        }
        catch (Exception e){mydb.logAppError(PageName, "calculateTotal", "Exception", e.getMessage());}
    }

    public TableRow rowInfrater(String tax, double taxpercentage, TableLayout tableLayout){
        TableRow returnview = null;
        try {
            double subtotal = 0;
            for (Models.RestaurantMenuItem sitem:selecteditemslist) {
                subtotal = subtotal+(sitem.getIP()*sitem.getQTY());
            }

            subtotal = subtotal*(taxpercentage/100);
            subtotal = Math.round(subtotal*100.0)/100.0;
            totalbill = totalbill + subtotal;

            LayoutInflater inflater = getActivity().getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tableLayout, false);

            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            img_itemtype.setVisibility(View.GONE);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText(tax);
            tv_itemname.setGravity(Gravity.RIGHT);


            //Item qty +
            ImageView img_qtyp = (ImageView) row.findViewById(R.id.img_qtyp);
            img_qtyp.setVisibility(View.GONE);

            //Item qty -
            ImageView img_qtym = (ImageView) row.findViewById(R.id.img_qtym);
            img_qtym.setVisibility(View.GONE);

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setVisibility(View.GONE);


            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(subtotal+"/-");

            //trow.addView(row);
            returnview = row;
        }
        catch(Exception e){mydb.logAppError(PageName, "rowInfrater", "Exception", e.getMessage());}
        return returnview;
    }

    public void addItemList(List<Models.RestaurantMenuItem> rmiList){
        try {
            TableLayout tableLayout = new TableLayout(getActivity());
            for (Models.RestaurantMenuItem item : rmiList) {
                TableRow tblrow = rowInfrater(item, tableLayout);
                tableLayout.addView(tblrow);//, new TableLayout.LayoutParams(AppBarLayout.LayoutParams.MATCH_PARENT, AppBarLayout.LayoutParams.MATCH_PARENT));
            }
            ll_resmenu.addView(tableLayout);
        }
        catch(Exception e){mydb.logAppError(PageName, "addItemList", "Exception", e.getMessage());}
    }

    public TableRow rowInfrater(Models.RestaurantMenuItem item, TableLayout tableLayout){
        TableRow returnview = null;
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            TableRow row = (TableRow)inflater.inflate(R.layout.row_restaurantmenusummary, tableLayout, false);

            //Item Name
            TextView tv_oid = (TextView) row.findViewById(R.id.tv_oid);
            tv_oid.setText(item.getOID()+"");

            //Item Type
            ImageView img_itemtype = (ImageView) row.findViewById(R.id.img_itemtype);
            if(item.getIT())
                img_itemtype.setImageResource(R.drawable.ic_veg);
            else
                img_itemtype.setImageResource(R.drawable.ic_nonveg);

            //Item Name
            TextView tv_itemname = (TextView) row.findViewById(R.id.tv_itemname);
            tv_itemname.setText(item.getIN());
            tv_itemname.setTextSize(12);

            //Qty Minus
            ImageView img_qtym = (ImageView) row.findViewById(R.id.img_qtym);
            img_qtym.setOnClickListener(this);

            //Item qty
            TextView tv_qty = (TextView) row.findViewById(R.id.tv_qty);
            tv_qty.setText(item.getQTY()+"");

            //Qty Plus
            ImageView img_qtyp = (ImageView) row.findViewById(R.id.img_qtyp);
            img_qtyp.setOnClickListener(this);

            //Item Price
            TextView tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
            tv_itemprice.setText(item.getIP()*item.getQTY()+"/-");

            //trow.addView(row);
            returnview = row;
        }
        catch(Exception e){mydb.logAppError(PageName, "rowInfrater", "Exception", e.getMessage());}
        return returnview;
    }

    public class getTaxes extends AsyncTask<Object, Integer, String>{

        @Override
        protected String doInBackground(Object... params) {
            //android.os.Debug.waitForDebugger();
            String response = mydb.getSystemParameter("RestaurantTaxes");
            try {
                if(response.equalsIgnoreCase("")) {
                    response = new HTTPCallJSon(getActivity()).Get("GetRestaurantTaxes", "?id=" + guid);
                    mydb.setSystemParameter("RestaurantTaxes", response);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getTaxes--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                mydb.setSystemParameter("RestaurantTaxes", jsonArray.toString());
                tl_taxes = new TableLayout(getActivity());

                for(int i = 0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    TableRow tblrow = rowInfrater(jsonObject.getString("TaxName"), jsonObject.getDouble("TaxPercentage"), tl_taxes);
                    tl_taxes.addView(tblrow);
                }
                ll_resmenu.addView(tl_taxes);
                calculateTotal();
            }
            catch (Exception e){mydb.logAppError(PageName, "getTaxes--onPostExecute", "Exception", e.getMessage());}
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Items Summary");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            LinearLayout row = null;
            TextView tv_qty = null;
            TextView tv_itemprice = null;
            TextView tv_oid = null;
            int qty = 0, oid = 0, price = 0;

            switch (v.getId()){
                case R.id.btn_modo:
                    btn_modo.setVisibility(View.GONE);
                    img_modo_processing.setVisibility(View.VISIBLE);
                    ((ActivityHome)getActivity()).getBack();
                    break;
                case R.id.btn_plco:
                    if(nd.isInternetAvailable())
                        checkCurrentLocationAndPlaceOrder();
                    else
                        Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.ll_callwaiter:
                    ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.colorPrimary)), new ColorDrawable(getResources().getColor(R.color.white))};
                    TransitionDrawable trans = new TransitionDrawable(color);
                    ll_callwaiter.setBackgroundDrawable(trans);
                    trans.startTransition(1000);
                    if(checkCurrentLocation()) {
                        new callWaiter().execute();
                        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                        View popupView = layoutInflater.inflate(R.layout.dialog_callingwaiter, null);
                        final PopupWindow popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
                        popupWindow.setAnimationStyle(R.style.DialogAnimation);
                        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                popupWindow.dismiss();
                            }
                        }, 2000);
                    }
                    break;
                case R.id.img_qtyp:
                    row = (TableRow) v.getParent().getParent();
                    tv_qty = (TextView) row.findViewById(R.id.tv_qty);
                    tv_oid = (TextView) row.findViewById(R.id.tv_oid);
                    tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
                    oid = Integer.parseInt(tv_oid.getText().toString());
                    qty = Integer.parseInt(tv_qty.getText().toString());
                    price = Integer.parseInt(tv_itemprice.getText().toString().replace("/-", ""));

                    mydb.updateMenuItem(oid, true, qty+1);
                    tv_qty.setText((qty+1)+"");
                    tv_itemprice.setText(((price/qty)*(qty+1))+"/-");
                    selecteditemslist = mydb.getSavedMenuItems("SelectedItems");
                    resetTaxes();
                    break;
                case R.id.img_qtym:
                    row = (TableRow) v.getParent().getParent();
                    tv_qty = (TextView) row.findViewById(R.id.tv_qty);
                    qty = Integer.parseInt(tv_qty.getText().toString());
                    tv_oid = (TextView) row.findViewById(R.id.tv_oid);
                    oid = Integer.parseInt(tv_oid.getText().toString());
                    tv_itemprice = (TextView) row.findViewById(R.id.tv_itemprice);
                    price = Integer.parseInt(tv_itemprice.getText().toString().replace("/-", ""));

                    if(qty > 1) {
                        mydb.updateMenuItem(oid, true, qty-1);
                        tv_qty.setText((qty - 1) + "");
                        tv_itemprice.setText(((price/qty)*(qty-1))+"/-");
                        selecteditemslist = mydb.getSavedMenuItems("SelectedItems");
                        resetTaxes();
                    }
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class callWaiter extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] params) {
            try{
                //JSONArray jsonArray = mydb.getCallWaiterURLs();
                String OrderPlacedID = mydb.getSystemParameter("OrderPlacedID");
                String ResGUID = mydb.getSystemParameter("RestaurantGUID");
                new HTTPCallJSon(getActivity()).Get("CallWaiter","?json={\"MasterID\":"+OrderPlacedID+",\"input\":\""+ResGUID+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "callWaiter--doInBackground", "Exception", e.getMessage());}
            return null;
        }

        @Override
        protected void onPostExecute(Object response) {
            super.onPostExecute(response);
            try {
                new PushNotification(getActivity()).execute();
            }
            catch (Exception e){mydb.logAppError(PageName, "callWaiter--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public boolean checkCurrentLocation(){
        try{
            lh = new LocationHelper(getActivity());
            if (lh.canGetLocation()) { //GPS is enabled
                double longitude = lh.getLongitude();
                double latitude = lh.getLatitude();
                if (longitude == 0.0 || latitude == 0.0) { //location services for this app is not allowed
                    new DialogInformation(getActivity(), Html.fromHtml("We could not get your current location.<br/>This feature will not work now."), "Restaurant").show();
                    return false;
                } else {//GPS is enables and location services for this app is allowed
                    //check for distance and then place order
                    JSONObject json = new JSONObject(mydb.getSystemParameter("RestaurantDetails"));
                    double entityLat = json.getDouble("Lat");
                    double entitLong = json.getDouble("Long");
                    int distanceallowed = json.getInt("PD");
                    float distance = cc.calculateDistance(latitude, longitude, entityLat, entitLong);

                    if(distance > distanceallowed){
                        new DialogInformation(getActivity(), Html.fromHtml("We don't find you inside our restaurant.<br/>This feature will not work now."), "Restaurant").show();
                        return false;
                    }
                    else{
                        return true;
                    }
                }
            } else { //GPS is not enabled
                Toast.makeText(getActivity(), "Please enable Location service", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "checkCurrentLocationAndCallWaiter", "Exception", e.getMessage());
            return false;
        }
    }

    public boolean checkCurrentLocationAndPlaceOrder(){
        try {
            lh = new LocationHelper(getActivity());
            if (lh.canGetLocation()) { //GPS is enabled
                double longitude = lh.getLongitude();
                double latitude = lh.getLatitude();
                if (longitude == 0.0 || latitude == 0.0) { //location services for this app is not allowed
                    requestLocationPermission();
                    return false;
                } else {//GPS is enables and location services for this app is allowed
                    //check for distance and then place order
                    JSONObject json = new JSONObject(mydb.getSystemParameter("RestaurantDetails"));
                    double entityLat = json.getDouble("Lat");
                    double entitLong = json.getDouble("Long");
                    int distanceallowed = json.getInt("PD");
                    float distance = cc.calculateDistance(latitude, longitude, entityLat, entitLong);

                    if(distance > distanceallowed){
                        DialogRestOrderPlaced drop = new DialogRestOrderPlaced(getActivity(), "We don't find you inside our restaurant.", "Pleas pick a table and place order");
                        drop.show();
                        return false;
                    }
                    else{
                        btn_plco.setVisibility(View.GONE);
                        img_plco_processing.setVisibility(View.VISIBLE);
                        String username = mydb.getCustomInfoItem("Name").getInfoValue();
//                        if(username.isEmpty()) {
//                            JSONObject profileinfo = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
//                            username = profileinfo.getString("FirstName")+' '+profileinfo.getString("MiddleName")+' '+profileinfo.getString("LastName");
//                        }
                        new placeOrder().execute(username);
                        return true;
                    }
                }
            } else { //GPS is not enabled
                Toast.makeText(getActivity(), "Please enable Location service", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "checkCurrentLocationAndPlaceOrder", "Exception", e.getMessage());
            return false;
        }
    }

    public class placeOrder extends AsyncTask<Object,Integer,String>{

        @Override
        protected String doInBackground(Object[] params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {

                //ArrayList<String> itemList = new ArrayList<String>();
                JSONArray itemList = new JSONArray();
                String oitems = "<Order>";
                for(Models.RestaurantMenuItem item:selecteditemslist){
                    oitems = oitems+"<TID>"+item.getTID()+"</TID>";
                    oitems = oitems+"<QTY>"+item.getQTY()+"</QTY>";
                    /*JSONObject jo = new JSONObject();
                    jo.put("TID", item.getTID());
                    jo.put("QTY", item.getQTY());*/

                    //itemList.add(jo.toString());
                    //itemList.put(jo);
                }
                oitems = oitems+"</Order>";
                //String oitems = new Gson().toJson(itemList);
                response = new HTTPCallJSon(getActivity()).Get("AddRestaurantOrder", "?json={\"DeviceID\":"+DeviceID+",\"SubjectID\":\""+guid+"\",\"XMLOrder\":\""+oitems+"\",\"DeviceType\":1,\"PersonName\":\""+params[0]+"\"}");
            }
            catch (Exception e){mydb.logAppError(PageName, "placeOrder--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                //JSONArray jsonArray = new JSONArray(response);
                //JSONObject jsonObject = jsonArray.getJSONObject(0);
                response = new JSONArray(response).getJSONObject(0).toString();
                JSONObject json = new JSONObject(response);
                if(cc.isInteger(json.getString("MasterID"))){
                    //mydb.addCallWaiterURL(jsonArray);
                    mydb.setSystemParameter("OrderPlacedID", json.getString("MasterID"));
                    ll_callwaiter.setVisibility(View.VISIBLE);
                    DialogInformation drop = new DialogInformation(getActivity(), Html.fromHtml("We have received your order.\nWe will be at your table shortly."), "Restaurant");
                    drop.show();
                    new PushNotification(getActivity()).execute();
                }
                else{
                    Toast.makeText(getActivity(), "We could not place your order. We are looking into the reasons.", Toast.LENGTH_SHORT).show();
                }
                btn_plco.setVisibility(View.VISIBLE);
                img_plco_processing.setVisibility(View.GONE);
            }
            catch (Exception e){mydb.logAppError(PageName, "placeOrder--onPostExecute", "Exception", e.getMessage());}
        }
    }

    public void requestLocationPermission(){
        try{
            int hasCoarseLocationAccess = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
            int hasFineLocationAccess = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasCoarseLocationAccess != PackageManager.PERMISSION_GRANTED || hasFineLocationAccess != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 500);
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "requestPermissions", "Exception", e.getMessage());}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case 500:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        checkCurrentLocationAndPlaceOrder();
                    else
                        Toast.makeText(getActivity(), "Sorry, We can not place order without accessing your location!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onRequestPermissionsResult", "Exception", e.getMessage());}
    }
}
