package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.home.logmenow.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import database.DBHelper;
import shared.CommonClasses;
import shared.Models.HistoryItem;

/**
 * Created by nviriyala on 29-08-2016.
 */
public class CustomHistoryAdapter extends BaseAdapter {
    private Context context;
    private List<HistoryItem> infoItems = new ArrayList<>();
    private DBHelper mydb;
    String PageName = "CustomHistoryAdapter";
    private CommonClasses cc;

    public CustomHistoryAdapter(Context context){
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
    }

    public void appentItems(List<HistoryItem> itemList){
        this.infoItems.addAll(itemList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return infoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return infoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {}
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public View getView(final int position, View clickedItem, final ViewGroup parent){
        View view = null;
        try{
            HistoryItem item = infoItems.get(position);
            view = View.inflate(context, R.layout.item_clienthistory, null);
            TextView tv_type = (TextView) view.findViewById(R.id.tv_type);
            Spanned textDec = Html.fromHtml("<b>"+item.getOID()+".<u>"+item.gettype()+"</u></b>");
            tv_type.setText(textDec);
            LinearLayout ll_hospital = (LinearLayout) view.findViewById(R.id.ll_hospital);
            LinearLayout ll_restaurant = (LinearLayout) view.findViewById(R.id.ll_restaurant);
            LinearLayout ll_securitycheck = (LinearLayout) view.findViewById(R.id.ll_securitycheck);
            LinearLayout ll_parking = (LinearLayout) view.findViewById(R.id.ll_parking);
            TextView tv_text;
            ImageView img_vw;
            String text="";
            JSONObject jsonObject = new JSONObject(item.getinfo());
            String imageurl = "http://www.logmenow.com/images/"+item.getImagename();
            switch (item.gettype()){
                case "Parking":
                    ll_parking.setVisibility(View.VISIBLE);
                    tv_text = (TextView) ll_parking.findViewById(R.id.tv_parkinginfo);
                    text = "<b><u>" + jsonObject.getString("EntityName") + "</u></b>";
                    text = text + "<br/><u>Date</u>: <b>" + item.getlogdate().substring(0,10)+ "</b>";
                    text = text + "<br/><u>Time</u>: <b>" + cc.get12HourFormat(item.getlogdate())+ "</b>";
                    text = text + "<br/><u>Vehicle number</u>: <b>"+ jsonObject.getString("VehicleNo")+ "</b>";
                    text = text + "<br/><u>In Time</u>: <b>" + cc.get12HourFormat(jsonObject.getString("InDate"))+ "</b>";
                    if(!jsonObject.getString("OutDate").contains("1900-01-01") && !jsonObject.getString("OutDate").equalsIgnoreCase(""))
                        text = text + "<br/><u>Out Time</u>: <b>" + cc.get12HourFormat(jsonObject.getString("OutDate"))+ "</b>";
                    textDec = Html.fromHtml(text);
                    tv_text.setText(textDec);

                    img_vw = (ImageView) ll_parking.findViewById(R.id.img_parking);
                    new DownloadImageTask(img_vw).execute(imageurl);
                    break;
                case "General Visit":
                    ll_securitycheck.setVisibility(View.VISIBLE);
                    tv_text = (TextView) ll_securitycheck.findViewById(R.id.tv_securitycheckinfo);
                    text = "<b><u>" + jsonObject.getString("EntityName") + "</u></b>";
                    text = text + "<br/><u>Date</u>: <b>" + item.getlogdate().substring(0,10)+ "</b>";
                    text = text + "<br/><u>Time</u>: <b>" + cc.get12HourFormat(item.getlogdate())+ "</b>";
                    text = text + "<br/><br/><b><u>Information Shared by you</u></b>";
                    jsonObject = jsonObject.getJSONObject("Information Shared");
                    if(jsonObject.has("Name"))
                        text = text + "<br/><u>Name</u>: <b>"+ jsonObject.getString("Name")+ "</b>";
                    if(jsonObject.has("Phone"))
                        text = text + "<br/><u>Phone</u>: <b>"+ jsonObject.getString("Phone")+ "</b>";
                    if(jsonObject.has("DOB"))
                        text = text + "<br/><u>DOB</u>: <b>"+ jsonObject.getString("DOB")+ "</b>";
                    if(jsonObject.has("Age"))
                        text = text + "<br/><u>Age</u>: <b>"+ jsonObject.getString("Age")+ "</b>";
                    if(jsonObject.has("Sex"))
                        text = text + "<br/><u>Sex</u>: <b>"+ jsonObject.getString("Sex")+ "</b>";
                    if(jsonObject.has("Vehicle"))
                        text = text + "<br/><u>Vehicle No</u>: <b>"+ jsonObject.getString("Vehicle")+ "</b>";
                    if(jsonObject.has("ComingFrom"))
                        text = text + "<br/><u>ComingFrom</u>: <b>"+ jsonObject.getString("ComingFrom")+ "</b>";
                    if(jsonObject.has("Purpose"))
                        text = text + "<br/><u>Purpose</u>: <b>"+ jsonObject.getString("Purpose")+ "</b>";
                    if(jsonObject.has("VisitingCompany"))
                        text = text + "<br/><u>VisitingCompany</u>: <b>"+ jsonObject.getString("VisitingCompany")+ "</b>";
                    if(jsonObject.has("ContactPerson"))
                        text = text + "<br/><u>ContactPerson</u>: <b>"+ jsonObject.getString("ContactPerson")+ "</b>";
                    if(jsonObject.has("Block"))
                        text = text + "<br/><u>Block</u>: <b>"+ jsonObject.getString("Block")+ "</b>";
                    if(jsonObject.has("Flat"))
                        text = text + "<br/><u>Flat</u>: <b>"+ jsonObject.getString("Flat")+ "</b>";
                    textDec = Html.fromHtml(text);
                    tv_text.setText(textDec);

                    img_vw = (ImageView) ll_securitycheck.findViewById(R.id.img_security);
                    new DownloadImageTask(img_vw).execute(imageurl);
                    break;
                case "Hospital":
                    ll_hospital.setVisibility(View.VISIBLE);
                    tv_text = (TextView) ll_hospital.findViewById(R.id.tv_hospitalinfo);
                    text = "<b><u>" + jsonObject.getString("EntityName") + "</u></b>";
                    text = text + "<br/><u>Date</u>: <b>" + item.getlogdate().substring(0,10)+ "</b>";
                    text = text + "<br/><u>Time</u>: <b>" + cc.get12HourFormat(item.getlogdate())+ "</b>";
                    text = text + "<br/><u>Doctor</u>: <b>" + jsonObject.getString("DoctorName")+ "</b>";

                    if(!jsonObject.getString("InDate").contains("1900-01-01") && !jsonObject.getString("InDate").equalsIgnoreCase(""))
                        text = text + "<br/><u>In Time</u>: <b>" + cc.get12HourFormat(jsonObject.getString("InDate"))+ "</b>";
                    if(!jsonObject.getString("OutDate").contains("1900-01-01") && !jsonObject.getString("OutDate").equalsIgnoreCase(""))
                        text = text + "<br/><u>Out Time</u>: <b>" + cc.get12HourFormat(jsonObject.getString("OutDate"))+ "</b>";

                    //text = text + "<br/><u>Out Time</u>: <b>" + cc.get12HourFormat(jsonObject.getString("OutDate"))+ "</b>";
                    if(!jsonObject.getString("Remark").equalsIgnoreCase("") && jsonObject.getString("Remark") != null && !jsonObject.getString("Remark").equalsIgnoreCase("null"))
                        text = text + "<br/><u>Remarks</u>: <b>" + jsonObject.getString("Remark")+ "</b>";
                    textDec = Html.fromHtml(text);
                    tv_text.setText(textDec);

                    img_vw = (ImageView) ll_hospital.findViewById(R.id.img_hospital);
                    new DownloadImageTask(img_vw).execute(imageurl);
                    break;
                case "Restaurant":
                    ll_restaurant.setVisibility(View.VISIBLE);
                    tv_text = (TextView) ll_restaurant.findViewById(R.id.tv_restaurantinfo);
                    text = "<b><u>" + jsonObject.getString("EntityName") + "</u></b>";
                    text = text + "<br/><u>Date</u>: <b>" + item.getlogdate().substring(0,10)+ "</b>";
                    text = text + "<br/><u>Time</u>: <b>" + cc.get12HourFormat(item.getlogdate())+ "</b>";
                    text = text + "<br/><u>Ordered Items</u>: <b>" + jsonObject.getString("Total items")+"</b>";
                    text = text + "<br/><u>Total bill</u>: <b>" + jsonObject.getString("Total bill")+"/-</b>";
                    textDec = Html.fromHtml(text);
                    tv_text.setText(textDec);

                    img_vw = (ImageView) ll_restaurant.findViewById(R.id.img_restaurant);
                    new DownloadImageTask(img_vw).execute(imageurl);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return view;
    }
}
