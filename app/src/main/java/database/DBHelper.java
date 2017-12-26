package database;

/**
 * Created by Home on 6/1/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Toast;

import shared.CommonClasses;
import shared.Models;
import shared.Models.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ClientApp.db";
    public Context context;

    @Override
    public void onCreate(SQLiteDatabase db){
        //create base info table
        /*db.execSQL("DROP TABLE IF EXISTS tbl_errorLog");
        db.execSQL("DROP TABLE IF EXISTS tbl_serverDeviceInfo");
        db.execSQL("DROP TABLE IF EXISTS tbl_DeviceUsage");*/
        String ts = new TimeStamp().getCurrentTimeStamp();

        db.execSQL("create table tbl_deviceinfo (id integer" +
                ", DeviceID text" +
                ", DeviceType text" +
                ", DeviceVersion text" +
                ", AppVersion text" +
                ", DeviceToken text" +
                ", ServerMapID int" +
                ", ModTime text)");
        String DeviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String DeviceType = "Android";
        String DeviceVersion = Build.VERSION.RELEASE;
        String AppVersion = "1.0.0.0";
        String DeviceToken = "";
        int ServerMapID = 0;
        String ModTime = ts;
        db.execSQL("Insert into tbl_deviceinfo values(1, '"+DeviceID+"','"+DeviceType+"','"+DeviceVersion+"','"+AppVersion+"','"+DeviceToken+"', "+ServerMapID+", '"+ModTime+"')");

        db.execSQL("create table tbl_errorLog (id integer primary key AUTOINCREMENT" +
                ", PageName text" +
                ", MethodName text" +
                ", ExceptionType text" +
                ", ExceptionText text" +
                ", OcrTime DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("create table tbl_VehicleList (VehicleNo text primary key" +
                ", VehicleType int" +
                ", AddedUpdatedDate DATETIME DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("create table tbl_SystemParameters(ParamName text primary key, ParamValue text)");
        db.execSQL("Insert into tbl_SystemParameters values('ProceedOffline', 'False')");
        db.execSQL("Insert into tbl_SystemParameters values('PersonalInfo', '"+ts+"')");
        db.execSQL("Insert into tbl_SystemParameters values('HomeAddress', '"+ts+"')");
        db.execSQL("Insert into tbl_SystemParameters values('OfficeAddress', '"+ts+"')");
        db.execSQL("Insert into tbl_SystemParameters values('DeviceInfo', '"+ts+"')");

        db.execSQL("create table tbl_VehicleEntry(ServerID int primary key" +
                ", VehicleNo text" +
                ", VehicleType int" +
                ", Entity text" +
                ", Usage text" +
                ", TariffType text" +
                ", TariffAmt int" +
                ", EntryDate DATETIME" +
                ", ExitDate DATETIME" +
                ", Timer int" +
                ", ServerEntryDate DATETIME" +
                ", ServerExitDate DATETIME" +
                ", SoftDelete int DEFAULT 0)");

        db.execSQL("create table tbl_PersonalInfo(id integer primary key" +
                ", FirstName text" +
                ", MiddleName text" +
                ", LastName text" +
                ", Email text" +
                ", Phone text" +
                ", AddedUpdatedDate DATETIME)");

        db.execSQL("create table tbl_HomeAddress(id integer primary key" +
                ", HAddress1 text" +
                ", HAddress2 text" +
                ", HLandMark text" +
                ", HCity text" +
                ", HState text" +
                ", HCountry text" +
                ", HZip text" +
                ", AddedUpdatedDate DATETIME)");

        db.execSQL("create table tbl_OfficeAddress(id integer primary key" +
                ", OAddress1 text" +
                ", OAddress2 text" +
                ", OCity text" +
                ", OState text" +
                ", OCountry text" +
                ", OZip text" +
                ", AddedUpdatedDate DATETIME)");

        db.execSQL("create table tbl_custominfo(id int, infoName text, infoValue text)");
        //db.execSQL("create table tbl_custominfo(id integer primary key AUTOINCREMENT, infoName text, infoValue text)");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(0, '', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(1, 'Name', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(2, 'Email', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(3, 'Phone', '')");
        //db.execSQL("Insert into tbl_custominfo(infoName, infoValue) values('Home Address', '')");
        //db.execSQL("Insert into tbl_custominfo(infoName, infoValue) values('Office Address', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(4, 'Date of Birth', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(5, 'Age', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(6, 'Gender', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(7, 'Vehicle No', '')");
        //id 8 is intentionaly left for vehicle type
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(9, 'Coming from', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(10, 'Purpose of visit', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(11, 'Visiting Company', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(12, 'Meeting person', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(13, 'Block', '')");
        db.execSQL("Insert into tbl_custominfo(id, infoName, infoValue) values(14, 'Flat', '')");

        db.execSQL("create table tbl_RestaurantMenu(TID int primary key" +
                ", OID int not null unique" +
                ", IT int" +
                ", IG text" +
                ", IName text" +
                ", IDesc text" +
                ", IP int" +
                ", SI int" +
                ", CR int" +
                ", Usr int" +
                ", RT int" +
                ", QTY int" +
                ", Selected int)");

        db.execSQL("create table tbl_EntityImage(Img_type text primary key, Img BLOB)");

        //db.execSQL("create table tbl_callwaiterURLs(Uri text, DeviceType text)");
        //LoadMasterDate();
    }

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public boolean logAppError(String PageName, String MethodName, String ExceptionType, String ExceptionText)  {
        if(ExceptionText == null)
            return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PageName", PageName);
        contentValues.put("MethodName", MethodName);
        contentValues.put("ExceptionType", ExceptionType);
        contentValues.put("ExceptionText", ExceptionText);
        contentValues.put("OcrTime", new TimeStamp().getCurrentTimeStamp());
        db.insert("tbl_errorLog", null, contentValues);
        try {
            Toast.makeText(context, "logAppError :: " + ExceptionText, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){}
        finally {
            db.close();
        }
        return true;
    }

    public List<CustomInfoItem> getCustomInfoItems(boolean hasvalue){
        List<CustomInfoItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from tbl_custominfo";
        if(hasvalue)
            Query = Query + " where infoValue != ''";
        else
            Query = Query + " where infoValue == ''";
        Cursor res =  db.rawQuery(Query, null );
        try {
            if (res.moveToFirst()) {
                do {

                    int id = res.getInt(res.getColumnIndex("id"));
                    String infoName = res.getString(res.getColumnIndex("infoName"));
                    String infoValue = res.getString(res.getColumnIndex("infoValue"));
                    CustomInfoItem item = new CustomInfoItem(id, infoName, infoValue);
                    items.add(item);

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getCustomInfoItems", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return items;
    }

    public CustomInfoItem getCustomInfoItem(String infoName){
        CustomInfoItem item = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_custominfo where infoName = '"+infoName+"'", null );
        try {
            if (res.moveToFirst()) {
                do {

                    int id = res.getInt(res.getColumnIndex("id"));
                    String infoValue = res.getString(res.getColumnIndex("infoValue"));
                    item = new CustomInfoItem(id, infoName, infoValue);
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getCustomInfoItem", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return item;
    }

    public void setCustomInfoItem(String infoName, String infoValue){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update tbl_custominfo set infoValue = '"+infoValue+"' where infoName = '"+infoName+"'");
        db.close();
    }

    public void addMenuItems(List<Models.RestaurantMenuItem> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete("tbl_RestaurantMenu", null, null);
        for (Models.RestaurantMenuItem item: items) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("TID", item.getTID());
            contentValues.put("OID", item.getOID());
            contentValues.put("IT", item.getIT()?1:0);
            contentValues.put("IG", item.getIG());
            contentValues.put("IName", item.getIN());
            contentValues.put("IDesc", item.getID());
            contentValues.put("IP", item.getIP());
            contentValues.put("SI", item.getSI());
            contentValues.put("CR", item.getCR()?1:0);
            contentValues.put("Usr", item.getUsr());
            contentValues.put("RT", item.getRT());
            contentValues.put("QTY", item.getQTY());
            contentValues.put("Selected", item.getSelected()?1:0);
            //db.insert("tbl_RestaurantMenu", null, contentValues);
            db.insertWithOnConflict("tbl_RestaurantMenu","TID",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.close();
    }

    public void deleteMenuItems(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tbl_RestaurantMenu", null, null);
        db.close();
    }

    public void updateMenuItem(int OID, boolean selected, int QTY){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update tbl_RestaurantMenu set  Selected = "+(selected?1:0)+", QTY = "+QTY+" where OID = "+OID);
        db.close();
    }

    public long getMenuItemCount(String itemtype){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "";
        if(itemtype.equalsIgnoreCase("SelectedItems"))
            Query = "select count(*) from tbl_RestaurantMenu where Selected = 1";
        else
            Query = "select count(*) from tbl_RestaurantMenu";

        SQLiteStatement s = db.compileStatement(Query);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        return cnt;
    }

    public void getImages(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_EntityImage", null );
        try {
            if (res.moveToFirst()) {
                do {
                    String ename = res.getString(res.getColumnIndex("Img_type"));
                    ename = ename+"";
                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getDeviceInfo", "Exception", e.getMessage());}
        finally {res.close();db.close();}
    }

    public List<Models.RestaurantMenuItem> getSavedMenuItems(String itemtype){
        List<Models.RestaurantMenuItem> items = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_RestaurantMenu", null );
        try {
            if (res.moveToFirst()) {
                do {
                    //Models.RestaurantMenuItem item = new Models.RestaurantMenuItem();
                    if(itemtype.equalsIgnoreCase("SelectedItems") && res.getInt(res.getColumnIndex("Selected")) == 0)
                        continue;

                    int TID = res.getInt(res.getColumnIndex("TID"));
                    int OID = res.getInt(res.getColumnIndex("OID"));
                    int ITint = res.getInt(res.getColumnIndex("IT"));
                    boolean IT = (ITint == 1); //this is same as (ITint == 1) ? true : false

                    String IG = res.getString(res.getColumnIndex("IG"));
                    String IN = res.getString(res.getColumnIndex("IName"));
                    String ID = res.getString(res.getColumnIndex("IDesc"));

                    int IP = res.getInt(res.getColumnIndex("IP"));
                    int SI = res.getInt(res.getColumnIndex("SI"));
                    int CRint = res.getInt(res.getColumnIndex("CR"));
                    boolean CR = (CRint == 1); //this is same as (CRint == 1) ? true : false

                    int Usr = res.getInt(res.getColumnIndex("Usr"));
                    int RT = res.getInt(res.getColumnIndex("RT"));
                    int QTY = res.getInt(res.getColumnIndex("QTY"));
                    int Sint = res.getInt(res.getColumnIndex("Selected"));
                    boolean Selected = (Sint == 1); //this is same as (Sint == 1) ? true : false

                    Models.RestaurantMenuItem item = new Models.RestaurantMenuItem(TID, OID, IT, IG, IN, IP, ID, SI, CR, Usr, RT, QTY, Selected);
                    items.add(item);

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getSavedMenuItems", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return items;
    }

    public Models.RestaurantMenuItem getRestaurantMenuItem(int OID){

        Models.RestaurantMenuItem item = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_RestaurantMenu where OID = "+OID, null );
        try {
            if (res.moveToFirst()) {
                do {

                    int TID = res.getInt(res.getColumnIndex("TID"));
                    //int OIDr = res.getInt(res.getColumnIndex("OID"));
                    int ITint = res.getInt(res.getColumnIndex("IT"));
                    boolean IT = (ITint == 1); //this is same as (ITint == 1) ? true : false

                    String IG = res.getString(res.getColumnIndex("IG"));
                    String IN = res.getString(res.getColumnIndex("IName"));
                    String ID = res.getString(res.getColumnIndex("IDesc"));

                    int IP = res.getInt(res.getColumnIndex("IP"));
                    int SI = res.getInt(res.getColumnIndex("SI"));
                    int CRint = res.getInt(res.getColumnIndex("CR"));
                    boolean CR = (CRint == 1); //this is same as (CRint == 1) ? true : false

                    int Usr = res.getInt(res.getColumnIndex("Usr"));
                    int RT = res.getInt(res.getColumnIndex("RT"));
                    int QTY = res.getInt(res.getColumnIndex("QTY"));
                    int Sint = res.getInt(res.getColumnIndex("Selected"));
                    boolean Selected = (Sint == 1); //this is same as (Sint == 1) ? true : false

                    item = new Models.RestaurantMenuItem(TID, OID, IT, IG, IN, IP, ID, SI, CR, Usr, RT, QTY, Selected);

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getMenuItem", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return item;

    }

    public void setEntityImage(String Img_type, Bitmap Imgbitmap){
        SQLiteDatabase db = this.getWritableDatabase();
        CommonClasses cc = new CommonClasses(context);
        byte[] Img;
        if(Imgbitmap == null)
            Img = null;
        else
            Img = cc.getBytes(Imgbitmap);
        ContentValues contentValues = new ContentValues();
        contentValues.put("Img_type", Img_type);
        contentValues.put("Img", Img);
        db.insertWithOnConflict("tbl_EntityImage","Img_type",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public byte[] getEntityImage(String Img_type){
        SQLiteDatabase db = this.getReadableDatabase();
        byte[] entityImage = null;
        Cursor res =  db.rawQuery("Select Img from tbl_EntityImage where Img_type='"+Img_type+"'", null );
        if(res.moveToFirst()) {
            do{
                entityImage = res.getBlob(res.getColumnIndex("Img"));
            }while (res.moveToNext());
        }
        db.close();
        return entityImage;
    }

    public boolean addVehicle(String VehicleNo, int VehicleType){
        SQLiteDatabase db = this.getWritableDatabase();
        String ts = new TimeStamp().getCurrentTimeStamp();
        ContentValues contentValues = new ContentValues();
        contentValues.put("VehicleNo", VehicleNo);
        contentValues.put("VehicleType", VehicleType);
        contentValues.put("AddedUpdatedDate", ts);
        db.insertWithOnConflict("tbl_VehicleList","VehicleNo",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return true;
    }

    public boolean addPersonalInfo(String json){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String ts = new TimeStamp().getCurrentTimeStamp();
            JSONObject jsonObject = new JSONObject(json);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", 1);
            contentValues.put("FirstName", jsonObject.getString("FirstName"));
            contentValues.put("MiddleName", jsonObject.getString("MiddleName"));
            contentValues.put("LastName", jsonObject.getString("LastName"));
            contentValues.put("Email", jsonObject.getString("Email"));
            contentValues.put("Phone", jsonObject.getString("Phone"));
            contentValues.put("AddedUpdatedDate", ts);
            db.insertWithOnConflict("tbl_PersonalInfo","id",contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        }
        catch (JSONException jex){logAppError("DBHelper", "addPersonalInfo", "JSONException", jex.getMessage());}
        catch(Exception e){logAppError("DBHelper", "addPersonalInfo", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public boolean addHomeAddress(String json){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String ts = new TimeStamp().getCurrentTimeStamp();
            JSONObject jsonObject = new JSONObject(json);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", 1);
            contentValues.put("HAddress1", jsonObject.getString("HAddress1"));
            contentValues.put("HAddress2", jsonObject.getString("HAddress2"));
            contentValues.put("HLandMark", jsonObject.getString("HLandMark"));
            contentValues.put("HCity", jsonObject.getString("HCity"));
            contentValues.put("HState", jsonObject.getString("HState"));
            contentValues.put("HCountry", jsonObject.getString("HCountry"));
            contentValues.put("HZip", jsonObject.getString("HZip"));
            contentValues.put("AddedUpdatedDate", ts);
            db.insertWithOnConflict("tbl_HomeAddress","id",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        catch (JSONException jex){logAppError("DBHelper", "addHomeAddress", "JSONException", jex.getMessage());}
        catch(Exception e){logAppError("DBHelper", "addHomeAddress", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public boolean addOfficeAddress(String json){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String ts = new TimeStamp().getCurrentTimeStamp();
            JSONObject jsonObject = new JSONObject(json);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", 1);
            contentValues.put("OAddress1", jsonObject.getString("OAddress1"));
            contentValues.put("OAddress2", jsonObject.getString("OAddress2"));
            contentValues.put("OCity", jsonObject.getString("OCity"));
            contentValues.put("OState", jsonObject.getString("OState"));
            contentValues.put("OCountry", jsonObject.getString("OCountry"));
            contentValues.put("OZip", jsonObject.getString("OZip"));
            contentValues.put("AddedUpdatedDate", ts);
            db.insertWithOnConflict("tbl_OfficeAddress","id",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        }
        catch (JSONException jex){logAppError("DBHelper", "addOfficeAddress", "JSONException", jex.getMessage());}
        catch(Exception e){logAppError("DBHelper", "addOfficeAddress", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public String getProfileInfo(String type){
        JSONObject json = new JSONObject();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("Select * from tbl_"+type+" where id = 1", null );
        try {

            if(res.moveToFirst()) {
                switch (type) {
                    case "PersonalInfo":
                        json.put("FirstName", res.getString(res.getColumnIndex("FirstName")));
                        json.put("MiddleName", res.getString(res.getColumnIndex("MiddleName")));
                        json.put("LastName", res.getString(res.getColumnIndex("LastName")));
                        json.put("Email", res.getString(res.getColumnIndex("Email")));
                        json.put("Phone", res.getString(res.getColumnIndex("Phone")));
                        json.put("AddedUpdatedDate", res.getString(res.getColumnIndex("AddedUpdatedDate")));
                        break;
                    case "HomeAddress":
                        json.put("HAddress1", res.getString(res.getColumnIndex("HAddress1")));
                        json.put("HAddress2", res.getString(res.getColumnIndex("HAddress2")));
                        json.put("HLandMark", res.getString(res.getColumnIndex("HLandMark")));
                        json.put("HCity", res.getString(res.getColumnIndex("HCity")));
                        json.put("HState", res.getString(res.getColumnIndex("HState")));
                        json.put("HCountry", res.getString(res.getColumnIndex("HCountry")));
                        json.put("HZip", res.getString(res.getColumnIndex("HZip")));
                        json.put("AddedUpdatedDate", res.getString(res.getColumnIndex("AddedUpdatedDate")));
                        break;
                    case "OfficeAddress":
                        json.put("OAddress1", res.getString(res.getColumnIndex("OAddress1")));
                        json.put("OAddress2", res.getString(res.getColumnIndex("OAddress2")));
                        json.put("OCity", res.getString(res.getColumnIndex("OCity")));
                        json.put("OState", res.getString(res.getColumnIndex("OState")));
                        json.put("OCountry", res.getString(res.getColumnIndex("OCountry")));
                        json.put("OZip", res.getString(res.getColumnIndex("OZip")));
                        json.put("AddedUpdatedDate", res.getString(res.getColumnIndex("AddedUpdatedDate")));
                        break;
                    default:
                        break;
                }
            }
        }
        catch(Exception e){logAppError("DBHelper", "getProfileInfo", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return json.toString();
    }

    public void deleteProfileInfo(String type){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tbl_"+type, "id=1",  null);
        db.close();
    }

    public boolean ifProfileExists(String type){
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteStatement s = db.compileStatement("select count(*) from tbl_"+type);
        long cnt =  s.simpleQueryForLong();
        s.close();
        db.close();
        if(cnt > 0)
            return true;
        else
            return false;
    }

    public JSONObject getDeviceInfo(){
        SQLiteDatabase db = this.getReadableDatabase();
        JSONObject jobj = new JSONObject();
        Cursor res =  db.rawQuery("Select * from tbl_deviceinfo", null );
        try {
            if (res.moveToFirst()) {
                do {
                    jobj.put("DeviceID", res.getString(res.getColumnIndex("DeviceID")));
                    jobj.put("DeviceType", res.getString(res.getColumnIndex("DeviceType")));
                    jobj.put("DeviceVersion", res.getString(res.getColumnIndex("DeviceVersion")));
                    jobj.put("AppVersion", res.getString(res.getColumnIndex("AppVersion")));
                    jobj.put("DeviceToken", res.getString(res.getColumnIndex("DeviceToken")));
                    jobj.put("ServerMapID", res.getInt(res.getColumnIndex("ServerMapID")));
                    jobj.put("ModTime", res.getString(res.getColumnIndex("ModTime")));

                } while (res.moveToNext());
            }
        }
        catch (Exception e){logAppError("DBHelper", "getDeviceInfo", "Exception", e.getMessage());}
        finally {res.close();db.close();}
        return jobj;
    }

    public boolean setDeviceInfo(String column, String value){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String ts = new TimeStamp().getCurrentTimeStamp();

            if(column.equalsIgnoreCase("ServerMapID"))
                db.execSQL("update tbl_deviceinfo set "+column+" = "+value+", ModTime = '"+ts+"'");
            else
                db.execSQL("update tbl_deviceinfo set "+column+" = '"+value+"', ModTime = '"+ts+"'");
        }
        catch (Exception e){logAppError("DBHelper", "setDeviceInfo", "Exception", e.getMessage());}
        finally {db.close();}
        return true;
    }

    public boolean setSystemParameter(String ParamName, String ParamValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ParamName", ParamName);
        contentValues.put("ParamValue", ParamValue);
        db.insertWithOnConflict("tbl_SystemParameters","ParamName",contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return true;
    }

    public String getSystemParameter(String ParamName){
        SQLiteDatabase db = this.getReadableDatabase();
        String ParamValue = "";
        Cursor res =  db.rawQuery("Select ParamValue from tbl_SystemParameters where ParamName='"+ParamName+"'", null );
        if(res.moveToFirst()) {
            do{
                ParamValue = res.getString(res.getColumnIndex("ParamValue"));
            }while (res.moveToNext());
        }
        db.close();
        return ParamValue;
    }

    public boolean addVehicleEntry(ParkingLogResponse plr){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ServerID", plr.getServerID());
        contentValues.put("VehicleNo", plr.getVehicleNo());
        contentValues.put("VehicleType", plr.getVehicleType());
        contentValues.put("Entity", plr.getEntity());
        contentValues.put("TariffAmt", plr.getTariffAmt());
        contentValues.put("EntryDate", plr.getEntryDate());
        if(!plr.getExitDate().equalsIgnoreCase("null") && !plr.getExitDate().equalsIgnoreCase("1900-01-01 00:00:00.000") && !plr.getExitDate().equalsIgnoreCase(""))
            contentValues.put("ExitDate", plr.getExitDate());
        contentValues.put("Timer", plr.getTimer());
        contentValues.put("ServerEntryDate", plr.getServerEntryDate());
        if(!plr.getServerExitDate().equalsIgnoreCase("null") && !plr.getServerExitDate().equalsIgnoreCase("1900-01-01 00:00:00.000") && !plr.getServerExitDate().equalsIgnoreCase(""))
            contentValues.put("ServerExitDate", plr.getServerExitDate());
        int id = (int)db.insertWithOnConflict("tbl_VehicleEntry","ServerID",contentValues,SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1){
            db.update("tbl_VehicleEntry", contentValues, "ServerID=?", new String[]{Integer.toString(plr.getServerID())});
            if(!plr.getServerExitDate().equalsIgnoreCase("null") && !plr.getServerExitDate().equalsIgnoreCase("1900-01-01 00:00:00.000") && !plr.getServerExitDate().equalsIgnoreCase(""))
                setSystemParameter(plr.getVehicleNo(), String.valueOf(plr.getServerID()));
        }
        db.close();
        return true;
    }

    public void addVehicleEntry(JSONObject jobj){
        try{
            int ServerID = jobj.getInt("ServerID");
            String VehicleNo = jobj.getString("VehicleNo");
            int VehicleType = jobj.getInt("VehicleType");
            String Entity = jobj.getString("Entity");
            int TariffAmt = jobj.getInt("TariffAmt");
            String EntryDate = jobj.getString("EntryDate").replace('T',' ').substring(0, 19);
            String ExitDate = jobj.getString("ExitDate").replace('T',' ');
            if(!ExitDate.equalsIgnoreCase("1900-01-01 00:00:00.000") && !ExitDate.equalsIgnoreCase("null") && !ExitDate.equalsIgnoreCase(""))
                ExitDate = ExitDate.substring(0, 19);
            else
                ExitDate = "null";
            int Timer = jobj.getInt("Timer");
            String ServerEntryDate = jobj.getString("ServerEntryDate").replace('T',' ').substring(0, 19);
            String ServerExitDate = jobj.getString("ServerExitDate").replace('T',' ');
            if(!ServerExitDate.equalsIgnoreCase("1900-01-01 00:00:00.000") && !ServerExitDate.equalsIgnoreCase("null") && !ServerExitDate.equalsIgnoreCase(""))
                ServerExitDate = ServerExitDate.substring(0, 19);
            else
                ServerExitDate = "null";
            int Oid = jobj.getInt("OID");
            Models.ParkingLogResponse plr = new Models.ParkingLogResponse(ServerID,VehicleNo,VehicleType,Entity,TariffAmt,EntryDate,ExitDate,Timer,ServerEntryDate,ServerExitDate,Oid);
            addVehicleEntry(plr);
        }
        catch(Exception e){logAppError("DBHelper", "addVehicleEntry", "Exception", e.getMessage());}
    }

    public JSONArray getVehicleExits() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select m.* from tbl_VehicleEntry m join tbl_SystemParameters s on m.VehicleNo = s.ParamName and m.ServerID == s.ParamValue";
        Cursor res =  db.rawQuery(Query, null );
        if(res.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ServerID",res.getInt(res.getColumnIndex("ServerID")));
                jsonObject.put("VehicleNo",res.getString(res.getColumnIndex("VehicleNo")));
                jsonObject.put("VehicleType",res.getInt(res.getColumnIndex("VehicleType")));
                jsonObject.put("Entity",res.getString(res.getColumnIndex("Entity")));
                jsonObject.put("TariffAmt",res.getInt(res.getColumnIndex("TariffAmt")));
                jsonObject.put("ServerEntryDate",res.getString(res.getColumnIndex("ServerEntryDate")));
                jsonObject.put("ServerExitDate",res.getString(res.getColumnIndex("ServerExitDate")));
                jsonArray.put(jsonObject);
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return jsonArray;
    }

    public int getVehicleType(String VehicleNo){
        SQLiteDatabase db = this.getReadableDatabase();
        int VehicleType = -1;

        String Query = "select m.VehicleType ";
        Query = Query + "from tbl_VehicleList m ";
        Query = Query + "where m.VehicleNo = '"+VehicleNo+"'";

        Cursor res =  db.rawQuery(Query, null );
        if(res.moveToFirst()) {
            do {
                VehicleType = res.getInt(res.getColumnIndex("VehicleType"));
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return VehicleType;
    }

    public boolean getVehicleEntry(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from tbl_VehicleEntry", null );
        if(res.moveToFirst()) {
            do {
                String VehicleNo = res.getString(res.getColumnIndex("VehicleNo"));
                int VehicleType = res.getInt(res.getColumnIndex("VehicleType"));
                String Entity = res.getString(res.getColumnIndex("Entity"));
                int TariffAmt = res.getInt(res.getColumnIndex("TariffAmt"));
                String EntryDate = res.getString(res.getColumnIndex("EntryDate"));
                String ExitDate = res.getString(res.getColumnIndex("ExitDate"));
                int Timer = res.getInt(res.getColumnIndex("Timer"));
                String ServerEntryDate = res.getString(res.getColumnIndex("ServerEntryDate"));
                String ServerExitDate = res.getString(res.getColumnIndex("ServerExitDate"));
                int SoftDelete = res.getInt(res.getColumnIndex("SoftDelete"));
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return true;
    }

    public void deleteVehicleEntry(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tbl_VehicleEntry", null,  null);
        db.close();
    }

    public void deleteVehicle(String VehicleNo){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tbl_VehicleEntry", "VehicleNo='"+VehicleNo+"'",  null);
        db.delete("tbl_VehicleList", "VehicleNo='"+VehicleNo+"'",  null);
        db.close();
    }

    public void deleteVehicleEntry(String VehicleNo, String EntityName){
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "update tbl_VehicleEntry set SoftDelete = 1 where ";
        Query = Query + "VehicleNo='"+VehicleNo+"' and Entity='"+EntityName+"'";
        db.execSQL(Query);
        db.close();
    }

    public List<VehicleList> getVehicleList(){
        //getVehicleEntry();
        List<VehicleList> retList = new ArrayList<VehicleList>();
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select m.VehicleNo, MIN(m.VehicleType) AS VehicleType, case when count(*) > 1 then \"Multiple Entries\" else MAX(s.Entity) end AS Entity, ";
        Query = Query + "case when count(*) > 1 then \"Multiple Timings\" else MAX(s.EntryDate) end AS EntryDate, ";
        Query = Query + "case when count(*) > 1 then -1 else MAX(s.TariffAmt) end AS TariffAmt, ";
        Query = Query + "case when count(*) > 1 then -1 else MAX(s.Timer) end AS Timer ";
        Query = Query + "from tbl_VehicleList m left join tbl_VehicleEntry s on m.VehicleNo = s.VehicleNo and s.EntryDate is not null and s.ExitDate is null and s.SoftDelete = 0 ";
        Query = Query + "group by m.VehicleNo ";
        Cursor res =  db.rawQuery(Query, null );
        if(res.moveToFirst()) {
            do {
                String VehicleNo = res.getString(res.getColumnIndex("VehicleNo"));
                int VehicleType = res.getInt(res.getColumnIndex("VehicleType"));
                String Entity = res.getString(res.getColumnIndex("Entity"));
                int TariffAmt = res.getInt(res.getColumnIndex("TariffAmt"));
                String EntryDate = res.getString(res.getColumnIndex("EntryDate"));
                int Timer = res.getInt(res.getColumnIndex("Timer"));
                VehicleList row = new VehicleList(VehicleNo, VehicleType, Entity, TariffAmt, EntryDate, null, Timer, null, null);
                retList.add(row);
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return retList;
    }

    public List<VehicleList> getVehicleList(String VehicleNo){
        List<VehicleList> retList = new ArrayList<VehicleList>();
        SQLiteDatabase db = this.getReadableDatabase();
        //String Query = "Select * from tbl_VehicleEntry where VehicleNo = '"+VehicleNo+"'";

        String Query = "select m.VehicleNo, m.VehicleType, s.Entity, s.EntryDate, s.TariffAmt, s.Timer ";
        Query = Query + "from tbl_VehicleList m left join tbl_VehicleEntry s on m.VehicleNo = s.VehicleNo and s.EntryDate is not null and s.ExitDate is null and s.SoftDelete = 0 ";
        Query = Query + "where m.VehicleNo = '"+VehicleNo+"'";

        Cursor res =  db.rawQuery(Query, null );
        if(res.moveToFirst()) {
            do {
                //String VehicleNo = res.getString(res.getColumnIndex("VehicleNo"));
                int VehicleType = res.getInt(res.getColumnIndex("VehicleType"));
                String Entity = res.getString(res.getColumnIndex("Entity"));
                int TariffAmt = res.getInt(res.getColumnIndex("TariffAmt"));
                String EntryDate = res.getString(res.getColumnIndex("EntryDate"));
                int Timer = res.getInt(res.getColumnIndex("Timer"));
                VehicleList row = new VehicleList(VehicleNo, VehicleType, Entity, TariffAmt, EntryDate, null, Timer, null, null);
                retList.add(row);
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return retList;
    }

    public List<TableID> getTableID(String tableName){
        List<TableID> retList = new ArrayList<TableID>();
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "";
        boolean foundRecords = false;
        if(tableName.equalsIgnoreCase("Parking"))
            Query = "select m.VehicleNo, s.ServerID from tbl_VehicleList m left join tbl_VehicleEntry s " +
                    "on m.VehicleNo = s.VehicleNo and s.EntryDate is not null and s.ExitDate is null and s.SoftDelete = 0 ";
        Cursor res =  db.rawQuery(Query, null );
        if(res.moveToFirst()) {
            do {
                foundRecords = true;
                int ID = res.getInt(res.getColumnIndex("ServerID"));
                if(ID != 0){
                    TableID row = new TableID(ID);
                    retList.add(row);
                }
            }while (res.moveToNext());
        }
        if(retList.size() == 0 && foundRecords)
            retList.add(new TableID(-1));
        res.close();
        db.close();
        return retList;
    }

    public String getDeviceID(){

        SQLiteDatabase db = this.getReadableDatabase();
        String DeviceID = "";
        int ServerMapID = 0;
        Cursor res =  db.rawQuery("Select * from tbl_deviceinfo", null );
        if (res.moveToFirst()) {
            do {
                DeviceID =  res.getString(res.getColumnIndex("DeviceID"));
                ServerMapID = res.getInt(res.getColumnIndex("ServerMapID"));
            } while (res.moveToNext());
        }
        res.close();
        db.close();
        if(ServerMapID != 0)
            DeviceID = ServerMapID+"";
        return DeviceID;
    }

    public List<ErrorLog> getErrorLog(int limit){
        SQLiteDatabase db = this.getReadableDatabase();
        List<ErrorLog> rows = new ArrayList<ErrorLog>();
        Cursor res =  db.rawQuery( "select * from tbl_errorLog order by id limit "+limit, null );
        if(res.moveToFirst()) {
            do {
                String PageName = res.getString(res.getColumnIndex("PageName"));
                String MethodName = res.getString(res.getColumnIndex("MethodName"));
                String ExceptionType = res.getString(res.getColumnIndex("ExceptionType"));
                String ExceptionText = res.getString(res.getColumnIndex("ExceptionText"));
                String OcrDate = res.getString(res.getColumnIndex("OcrTime"));
                ErrorLog row = new ErrorLog(PageName, MethodName, ExceptionType, ExceptionText, OcrDate);
                rows.add(row);
            }while (res.moveToNext());
        }
        res.close();
        db.close();
        return rows;
    }

    public void deleteErrorLog(int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = " id in (select id from tbl_errorLog order by id limit "+limit+")";
        db.delete("tbl_errorLog", where,  null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        /*db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);*/
    }
}

