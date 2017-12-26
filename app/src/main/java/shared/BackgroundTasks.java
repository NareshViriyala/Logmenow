package shared;

import android.content.Context;

import database.DBHelper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by nviriyala on 11-07-2016.
 */
public class BackgroundTasks {

    private DBHelper mydb;
    private String PageName = "BackgroundTasks";
    private Context context;
    private NetworkDetector nd;
    private CommonClasses cc;

    public BackgroundTasks(Context context, String MethodName){
        try {
            this.context = context;
            mydb = new DBHelper(context);
            cc = new CommonClasses(context);
            nd = new NetworkDetector(context);
            if (!nd.isInternetAvailable())
                return;

            //syncParkingTable();
            if(MethodName == null)
                MethodName = "0";
            if(getServerMapID()){
                switch (MethodName){
                    case "0":
                        syncPersonalInfoTable();
                        syncHomeAddress();
                        syncOfficeAddress();
                        //syncParkingTable();
                        break;
                    case "PersonalInfo":
                        syncPersonalInfoTable();
                        break;
                    case "HomeAddress":
                        syncHomeAddress();
                        break;
                    case "OfficeAddress":
                        syncOfficeAddress();
                        break;
                }
                pushErrorLog();
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "BackgroundTasks", "Exception", e.getMessage());}
    }

    public void syncOfficeAddress(){
        try{
            JSONObject jo = new JSONObject(mydb.getProfileInfo("OfficeAddress"));
            if(jo.toString().equalsIgnoreCase("{}"))
                return;

            String lastServerUpdateStr = mydb.getSystemParameter("OfficeAddress");
            String lastLocalUpdateStr = jo.getString("AddedUpdatedDate");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date lastServerUpdate = simpleDateFormat.parse(lastServerUpdateStr);
            Date lastLocalUpdate = simpleDateFormat.parse(lastLocalUpdateStr);
            if(lastLocalUpdate.compareTo(lastServerUpdate) <= 0)
                return;

            JSONObject sendJO = new JSONObject();
            sendJO.put("DeviceID", mydb.getDeviceID());
            sendJO.put("Add1",cc.convertSpecialChar(jo.getString("OAddress1")));
            sendJO.put("Add2",cc.convertSpecialChar(jo.getString("OAddress2")));
            sendJO.put("City",jo.getString("OCity"));
            sendJO.put("State",jo.getString("OState"));
            sendJO.put("Country",jo.getString("OCountry"));
            sendJO.put("Zip",jo.getString("OZip"));

            String response = new HTTPCallJSon(context).Get("AddUserOfficeAddress", "?json="+sendJO.toString());
            if(response.equalsIgnoreCase("Success")) {
                mydb.setSystemParameter("OfficeAddress", new Models.TimeStamp().getCurrentTimeStamp());
            }
        }
        catch (JSONException e){mydb.logAppError(PageName, "syncOfficeAddress", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "syncOfficeAddress", "Exception", e.getMessage());}
    }

    public void syncHomeAddress(){
        try{
            JSONObject jo = new JSONObject(mydb.getProfileInfo("HomeAddress"));
            if(jo.toString().equalsIgnoreCase("{}"))
                return;

            String lastServerUpdateStr = mydb.getSystemParameter("HomeAddress");
            String lastLocalUpdateStr = jo.getString("AddedUpdatedDate");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date lastServerUpdate = simpleDateFormat.parse(lastServerUpdateStr);
            Date lastLocalUpdate = simpleDateFormat.parse(lastLocalUpdateStr);
            if(lastLocalUpdate.compareTo(lastServerUpdate) <= 0)
                return;

            JSONObject sendJO = new JSONObject();
            sendJO.put("DeviceID", mydb.getDeviceID());
            sendJO.put("Add1",cc.convertSpecialChar(jo.getString("HAddress1")));
            sendJO.put("Add2",cc.convertSpecialChar(jo.getString("HAddress2")));
            sendJO.put("LandMark",cc.convertSpecialChar(jo.getString("HLandMark")));
            sendJO.put("City",jo.getString("HCity"));
            sendJO.put("State",jo.getString("HState"));
            sendJO.put("Country",jo.getString("HCountry"));
            sendJO.put("Zip",jo.getString("HZip"));

            String response = new HTTPCallJSon(context).Get("AddUserHomeAddress", "?json="+sendJO.toString());
            if(response.equalsIgnoreCase("Success")) {
                mydb.setSystemParameter("HomeAddress", new Models.TimeStamp().getCurrentTimeStamp());
            }
        }
        catch (JSONException e){mydb.logAppError(PageName, "syncHomeAddress", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "syncHomeAddress", "Exception", e.getMessage());}
    }

    public void syncPersonalInfoTable(){
        try{
            JSONObject jo = new JSONObject(mydb.getProfileInfo("PersonalInfo"));
            if(jo.toString().equalsIgnoreCase("{}"))
                return;

            String lastServerUpdateStr = mydb.getSystemParameter("PersonalInfo");
            String lastLocalUpdateStr = jo.getString("AddedUpdatedDate");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date lastServerUpdate = simpleDateFormat.parse(lastServerUpdateStr);
            Date lastLocalUpdate = simpleDateFormat.parse(lastLocalUpdateStr);
            if(lastLocalUpdate.compareTo(lastServerUpdate) <= 0)
                return;

            JSONObject sendJO = new JSONObject();
            sendJO.put("DeviceID", mydb.getDeviceID());
            sendJO.put("FName",jo.getString("FirstName"));
            sendJO.put("MName",jo.getString("MiddleName"));
            sendJO.put("LName",jo.getString("LastName"));
            sendJO.put("Email",jo.getString("Email"));
            sendJO.put("Phone",jo.getString("Phone"));

            String response = new HTTPCallJSon(context).Get("AddUserPersonalInfo", "?json="+sendJO.toString());
            if(response.equalsIgnoreCase("Success")) {
                mydb.setSystemParameter("PersonalInfo", new Models.TimeStamp().getCurrentTimeStamp());
            }
        }
        catch (JSONException e){mydb.logAppError(PageName, "syncPersonalInfoTable", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "syncPersonalInfoTable", "Exception", e.getMessage());}
    }

    public boolean getServerMapID(){
        boolean retVal = false;
        try{
            JSONObject jobj = mydb.getDeviceInfo();

            String lastServerUpdateStr = mydb.getSystemParameter("DeviceInfo");
            String lastLocalUpdateStr = jobj.getString("ModTime");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date lastServerUpdate = simpleDateFormat.parse(lastServerUpdateStr);
            Date lastLocalUpdate = simpleDateFormat.parse(lastLocalUpdateStr);
            //boolean id = isInteger(mydb.getDeviceID());
            //int com = lastLocalUpdate.compareTo(lastServerUpdate);
            //int com1 = lastServerUpdate.compareTo(lastLocalUpdate);
            if(lastLocalUpdate.compareTo(lastServerUpdate) <= 0 && cc.isInteger(mydb.getDeviceID()))
                return true;

            JSONObject jo = new JSONObject();
            jo.put("DeviceID",jobj.getString("DeviceID"));
            jo.put("Uri",jobj.getString("DeviceToken"));
            jo.put("DeviceType",jobj.getString("DeviceType"));
            jo.put("DeviceVersion",jobj.getString("DeviceVersion"));
            jo.put("AppVersion",jobj.getString("AppVersion"));
            String response = new HTTPCallJSon(context).Get("AddUserDevice", "?json="+jo.toString());
            JSONObject jsonObject = new JSONArray(response).getJSONObject(0);
            int ServerMAPID = jsonObject.getInt("ServerMAPID");
            mydb.setDeviceInfo("ServerMapID",ServerMAPID+"");
            mydb.setSystemParameter("DeviceInfo", new Models.TimeStamp().getCurrentTimeStamp());
            retVal = true;
        }
        catch (JSONException e){mydb.logAppError(PageName, "getServerMapID", "JSONException", e.getMessage());}
        catch (Exception e){mydb.logAppError(PageName, "getServerMapID", "Exception", e.getMessage());}
        return retVal;
    }

    public void pushErrorLog(){
        try{
            List<Models.ErrorLog> rows = mydb.getErrorLog(10);
            String DeviceID = mydb.getDeviceID();

            if(rows.size() > 0) {
                String input = "<row>";
                for(Models.ErrorLog row : rows){
                    input = input+"<DeviceID>"+DeviceID+"</DeviceID>";
                    input = input+"<PageName>"+row.getPageName()+"</PageName>";
                    input = input+"<MethodName>"+row.getMethodName()+"</MethodName>";
                    input = input+"<ExceptionType>"+row.getExceptionType()+"</ExceptionType>";
                    input = input+"<ExceptionText>"+row.getExceptionText()+"</ExceptionText>";
                    input = input+"<OcrTime>"+row.getOcrTime()+"</OcrTime>";
                }
                input = input+"</row>";
                new HTTPCallJSon(context).Get("AddUserDeviceErrorLog", "?json="+input);
                mydb.deleteErrorLog(rows.size());
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "pushErrorLog", "Exception", e.getMessage());}
    }

    public void syncParkingTable(){
        try{
            //mydb.getVehicleEntry();
            List<Models.TableID> rows = mydb.getTableID("Parking");
            String DeviceID = mydb.getDeviceID();
            if(rows.size() > 0) {
                Models.BoxParkingLog bel = new Models.BoxParkingLog(DeviceID, rows, false, 0, 0);
                String strObj = new Gson().toJson(bel);
                String response = new HTTPCallJSon(context).Get("SyncUserParkingInfo", "?json="+strObj);
                JSONArray jArray = new JSONArray(response);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobj = jArray.getJSONObject(i);
                    int ServerID = jobj.getInt("ServerID");
                    String VehicleNo = jobj.getString("VehicleNo");
                    int VehicleType = jobj.getInt("VehicleType");
                    String Entity = jobj.getString("Entity");
                    int TariffAmt = jobj.getInt("TariffAmt");
                    String EntryDate = jobj.getString("EntryDate").replace('T',' ').substring(0, 19);
                    String ExitDate = jobj.getString("ExitDate").replace('T',' ');
                    if(!ExitDate.equalsIgnoreCase("null"))
                        ExitDate = ExitDate.substring(0, 19);
                    int Timer = jobj.getInt("Timer");
                    String ServerEntryDate = jobj.getString("ServerEntryDate").replace('T',' ').substring(0, 19);
                    String ServerExitDate = jobj.getString("ServerExitDate").replace('T',' ');
                    if(!ServerExitDate.equalsIgnoreCase("null"))
                        ServerExitDate = ServerExitDate.substring(0, 19);
                    int Oid = jobj.getInt("Oid");
                    Models.ParkingLogResponse plr = new Models.ParkingLogResponse(ServerID,VehicleNo,VehicleType,Entity,TariffAmt,EntryDate,ExitDate,Timer,ServerEntryDate,ServerExitDate,Oid);
                    mydb.addVehicleEntry(plr);
                }
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "pushErrorLog", "Exception", e.getMessage());}
    }
}
