package shared;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import database.DBHelper;
import fragment.FragmentScanQR;
import gcm.PushNotification;

/**
 * Created by nviriyala on 11-08-2016.
 */
public class DeleteSavedClientData {
    private Context context;
    private DBHelper mydb;
    private String PageName = "DeleteSavedClientData";

    public DeleteSavedClientData(Context context){
        this.context = context;
        mydb = new DBHelper(context);
    }

    public void Delete(String type){
        try{
            switch (type) {
                case "Restaurant":
                    String OrderPlacedID = mydb.getSystemParameter("OrderPlacedID");
                    if(!OrderPlacedID.equalsIgnoreCase("")){
                        new deleteOrder().execute(OrderPlacedID, mydb.getSystemParameter("RestaurantGUID"),mydb.getDeviceID(),"DeleteOrder");
                        new PushNotification(context).execute();
                    }
                    mydb.deleteMenuItems();
                    mydb.setSystemParameter("RestaurantGUID", "");
                    mydb.setSystemParameter("RestaurantPageNumber", "0");
                    mydb.setSystemParameter("RestaurantBusinessHours", "");
                    mydb.setSystemParameter("RestaurantDetails", "");
                    mydb.setSystemParameter("RestaurantTaxes", "");
                    mydb.setEntityImage("Restaurant", null);
                    mydb.setSystemParameter("OrderPlacedID", "");
                    break;
                case "Hospital":
                    String DocApp = mydb.getSystemParameter("DocApptID");
                    if (!DocApp.equalsIgnoreCase("")) {
                        String ApptID = new JSONObject(DocApp).getString("ApptID");
                        new cancelAppointment().execute(new String[]{ApptID, "UserCancelled", "0", mydb.getDeviceID()});
                        new PushNotification(context).execute();
                    }
                    mydb.setSystemParameter("DocApptID", "");
                    mydb.setSystemParameter("HospitalGUID", "");
                    mydb.setSystemParameter("HospitalBusinessHours", "");
                    mydb.setSystemParameter("HospitalDetails", "");
                    mydb.setEntityImage("Hospital", null);
                    mydb.setEntityImage("Doctor", null);
                    mydb.setSystemParameter("DoctorDetails", "");
                    mydb.setSystemParameter("PatientDetails", "");
                    mydb.setSystemParameter("FragmentHospitalNotification","False");
                    break;
                case "Bank":
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "Delete", "Exception", e.getMessage());}
    }

    public class cancelAppointment extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            //android.os.Debug.waitForDebugger();
            String respose = "";
            try{
                JSONObject jparam = new JSONObject();
                jparam.put("ApptID",params[0]);
                jparam.put("CallType",params[1]);
                jparam.put("CallFrom",params[2]);
                jparam.put("deviceid",params[3]);
                //String QueryStr = "?ApptID="+params[0]+"&ct="+params[1]+"&cf="+params[2];
                respose = new HTTPCallJSon(context).Get("SetDoctorAppointment","?json="+jparam.toString());
                if(!respose.equalsIgnoreCase("Success"))
                    mydb.logAppError(PageName, "cancelAppointment--doInBackground", "Exception", respose);
            }
            catch (Exception e){mydb.logAppError(PageName, "cancelAppointment--doInBackground", "Exception", e.getMessage());}
            return null;
        }

        @Override
        protected void onPostExecute(Object s) {
            super.onPostExecute(s);
            new PushNotification(context).execute();
        }
    }

    public class deleteOrder extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try{response = new HTTPCallJSon(context).Get("DeleteRestaurantOrder","?json={\"MasterID\":"+params[0]+",\"GUID\":\"\",\"DeviceID\":"+params[2]+",\"Status\":\""+params[3]+"\"}");}
            catch (Exception e){mydb.logAppError(PageName, "deleteOrder--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new PushNotification(context).execute();
        }
    }
}
