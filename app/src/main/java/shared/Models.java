package shared;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by nviriyala on 05-07-2016.
 */
public class Models {
    public static class TimeStamp{
        private String ts;

        public TimeStamp(){
            //this.ts = new java.sql.Timestamp(new java.util.Date().getTime());
            setTimeStamp();
        }

        public String getTimeStamp(){return this.ts;}

        public void setTimeStamp(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.ts = df.format(c.getTime());
        }

        public String getCurrentTimeStamp(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(c.getTime());
        }
    }

    public static class VehicleList{
        private String VehicleNo;
        private int VehicleType;
        private String Entity;
        private int TariffAmt;
        private String EntryDate;
        private String ExitDate;
        private int Timer;
        private String ServerEntryDate;
        private String ServerExitDate;

        public VehicleList(String VehicleNo,int VehicleType,String Entity,int TariffAmt,String EntryDate,String ExitDate,int Timer,String ServerEntryDate,String ServerExitDate){
            this.VehicleNo = VehicleNo;
            this.VehicleType = VehicleType;
            this.Entity = Entity;
            this.TariffAmt = TariffAmt;
            this.EntryDate = EntryDate;
            this.ExitDate = ExitDate;
            this.Timer = Timer;
            this.ServerEntryDate = ServerEntryDate;
            this.ServerExitDate = ServerExitDate;
        }

        public String getVehicleNo(){return this.VehicleNo;}
        public int getVehicleType(){return this.VehicleType;}
        public String getEntity(){return this.Entity;}
        public int getTariffAmt(){return this.TariffAmt;}
        public String getEntryDate(){return this.EntryDate;}
        public String getExitDate(){return this.ExitDate;}
        public int getTimer(){return this.Timer;}
        public String getServerEntryDate(){return this.ServerEntryDate;}
        public String getServerExitDate(){return this.ServerExitDate;}
    }

    public static class ErrorLog{
        private String PageName;
        private String MethodName;
        private String ExceptionType;
        private String ExceptionText;
        private String OcrTime;

        public ErrorLog(String PageName, String MethodName, String ExceptionType, String ExceptionText, String OcrTime){
            this.PageName = PageName;
            this.MethodName = MethodName;
            this.ExceptionType = ExceptionType;
            this.ExceptionText = ExceptionText;
            this.OcrTime = OcrTime;
        }

        public String getPageName(){return this.PageName;}
        public String getMethodName(){return this.MethodName;}
        public String getExceptionType(){return this.ExceptionType;}
        public String getExceptionText(){
            if(this.ExceptionText.length() > 500)
                return this.ExceptionText.substring(1,499);
            else
                return this.ExceptionText;
        }
        public String getOcrTime(){return this.OcrTime;}
    }

    public static class BoxErrorLog{
        private String DeviceID;
        private List<ErrorLog> rows;
        public BoxErrorLog(String DeviceID, List<ErrorLog> rows){
            this.DeviceID = DeviceID;
            this.rows = rows;
        }
    }

    public static class TableID{
        private int ServerID;

        public TableID(int ID){
            this.ServerID = ID;
        }
    }

    public static class BoxParkingLog{
        private String DeviceID;
        private List<TableID> rows;
        private boolean getLog;
        private int pageNo;
        private int pageCount;
        public BoxParkingLog(String DeviceID, List<TableID> rows, boolean getLog, int pageNo, int pageCount){
            this.DeviceID = DeviceID;
            this.rows = rows;
            this.getLog = getLog;
            this.pageNo = pageNo;
            this.pageCount = pageCount;
        }
    }

    public static class ParkingLogResponse{
        private int ServerID;
        private String VehicleNo;
        private int VehicleType;
        private String Entity;
        private int TariffAmt;
        private String EntryDate;
        private String ExitDate;
        private int Timer;
        private String ServerEntryDate;
        private String ServerExitDate;
        private long Oid;

        public ParkingLogResponse(int ServerID,String VehicleNo,int VehicleType,String Entity,int TariffAmt,String EntryDate,String ExitDate,int Timer,String ServerEntryDate,String ServerExitDate,long Oid){
            this.ServerID = ServerID;
            this.VehicleNo = VehicleNo;
            this.VehicleType = VehicleType;
            this.Entity = Entity;
            this.TariffAmt = TariffAmt;
            this.EntryDate = EntryDate;
            this.ExitDate = ExitDate;
            this.Timer = Timer;
            this.ServerEntryDate = ServerEntryDate;
            this.ServerExitDate = ServerExitDate;
            this.Oid = Oid;
        }
        public int getServerID(){return this.ServerID;}
        public String getVehicleNo(){return this.VehicleNo;}
        public int getVehicleType(){return this.VehicleType;}
        public String getEntity(){return this.Entity;}
        public int getTariffAmt(){return this.TariffAmt;}
        public String getEntryDate(){return this.EntryDate;}
        public String getExitDate(){return this.ExitDate;}
        public int getTimer(){return this.Timer;}
        public String getServerEntryDate(){return this.ServerEntryDate;}
        public String getServerExitDate(){return this.ServerExitDate;}
        public long getOid(){return this.Oid;}
    }

    public static class RestaurantMenuItem{
        private int TID; //Table ID
        private int OID; //Order
        private boolean IT; //Item Type - Veg/NonVeg
        private String IG; //Item Group
        private String IN; //Item Name
        private int IP; //Item Price
        private String ID; //Item Description
        private int SI; //Spice Index
        private boolean CR; //Chef Recommended
        private int Usr; //Users
        private int RT; //Rating
        private int QTY; //Quantity
        private boolean Selected;


        public RestaurantMenuItem(int TID, int OID, boolean IT, String IG, String IN, int IP, String ID, int SI, boolean CR, int Usr, int RT, int QTY, boolean Selected){
            this.TID = TID;
            this.OID = OID;
            this.IT = IT;
            this.IG = IG;
            this.IN = IN;
            this.IP = IP;
            this.ID = ID;
            this.SI = SI;
            this.CR = CR;
            this.Usr = Usr;
            this.RT = RT;
            this.QTY = QTY;
            this.Selected = Selected;
        }

        public int getTID(){return this.TID;}
        public int getOID(){return this.OID;}
        public boolean getIT(){return this.IT;}
        public String getIG(){return this.IG;}
        public String getIN(){return this.IN;}
        public int getIP(){return this.IP;}
        public String getID(){return this.ID;}
        public int getSI(){return this.SI;}
        public boolean getCR(){return this.CR;}
        public int getUsr(){return this.Usr;}
        public int getRT(){return this.RT;}
        public int getQTY(){return this.QTY;}
        public boolean getSelected(){return  this.Selected;}

        public void setSelected(boolean Selected){this.Selected = Selected;}
        public void setQTY(int cnt){this.QTY = cnt;}
    }

    public static class CustomInfoItem{
        private int id;
        private String infoName;
        private String infoValue;
        private boolean Selected;

        public CustomInfoItem(int id,String infoName,String infoValue){
            this.id = id;
            this.infoName = infoName;
            this.infoValue = infoValue;
            Selected = false;
        }

        public int getId(){return this.id;}
        public String getInfoName(){return this.infoName;}
        public String getInfoValue(){return this.infoValue;}
        public boolean getSelected(){return this.Selected;}
        public void setSelected(boolean value){this.Selected = value;}
    }

    public static class HistoryItem{
        private int OID;
        private String type;
        private String info;
        private String imagename;
        private String logdate;

        public HistoryItem(String type,String info,String imagename,String logdate, int OID){
            this.type = type;
            this.info = info;
            this.imagename = imagename;
            this.logdate = logdate;
            this.OID = OID;
        }

        public String gettype(){return this.type;}
        public String getinfo(){return this.info;}
        public String getImagename(){return this.imagename;}
        public String getlogdate(){return this.logdate;}
        public int getOID(){return this.OID;}
    }
}
