package shared;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nviriyala on 04-07-2016.
 */
public class GlobalClass extends Application{

    private boolean ProceedOffline;
    private String ToActivity;
    private String FromActivity;
    private String guid;
    //private List<Models.RestaurantMenuItem> selecteditems =  new ArrayList<>();
    //private List<Models.RestaurantMenuItem> totalitems =  new ArrayList<>();

    //private Bitmap RestaurantImage;
    //private String RestaurantDetails;
    //private String RestaurantBusinessHours;
    //private String RestaurantTaxes;
    //private int RestaurantPageNumber;

    //private Bitmap HospitalImage;
    //private String HospitalDetails;
    //private String HospitalBusinessHours;


    public boolean getProceedOffline() {
        return this.ProceedOffline;
    }
    public void setProceedOffline(boolean ProceedOffline) {
        this.ProceedOffline = ProceedOffline;
    }

    public String getToActivity(){return this.ToActivity;}
    public void setToActivity(String ToActivity){this.ToActivity = ToActivity;}

    public String getFromActivity(){return this.FromActivity;}
    public void setFromActivity(String FromActivity){this.FromActivity = FromActivity;}

    public String getguid(){return this.guid;}
    public void setguid(String guid){this.guid = guid;}

    /*public List<Models.RestaurantMenuItem> getselecteditems(){return this.selecteditems;}
    public void setselecteditems(List<Models.RestaurantMenuItem> selecteditems){
        if(selecteditems == null)
            this.selecteditems.clear();
        else
            this.selecteditems = selecteditems;
    }

    public List<Models.RestaurantMenuItem> getTotalitems(){return this.totalitems;}
    public void setTotalitems(List<Models.RestaurantMenuItem> totalitems){
        if(totalitems == null)
            this.totalitems.clear();
        else
            this.totalitems = totalitems;
    }

    public Bitmap getSubjectImage(String type){
        switch (type) {
            case "Restaurant":
                return this.RestaurantImage;
            case "Hospital":
                return this.HospitalImage;
            default:
                return null;
        }
    }
    public void setSubjectImage(Bitmap SubjectImage, String type){
        switch (type){
            case "Restaurant":
                this.RestaurantImage = SubjectImage;
                break;
            case "Hospital":
                this.HospitalImage = SubjectImage;
                break;
            default:
                break;
        }
    }

    public String getEntityDetails(String type){
        switch (type) {
            case "Restaurant":
                return this.RestaurantDetails;
            case "Hospital":
                return this.HospitalDetails;
            default:
                return null;
        }
    }
    public void setEntityDetails(String EntityDetails, String type){
        switch (type){
            case "Restaurant":
                this.RestaurantDetails = EntityDetails;
                break;
            case "Hospital":
                this.HospitalDetails = EntityDetails;
                break;
            default:
                break;
        }
    }

    public String getEntityBusinessHours(String type){
        switch (type) {
            case "Restaurant":
                return this.RestaurantBusinessHours;
            case "Hospital":
                return this.HospitalBusinessHours;
            default:
                return null;
        }
    }
    public void setEntityBusinessHours(String EntityBusinessHours, String type){
        switch (type){
            case "Restaurant":
                this.RestaurantBusinessHours = EntityBusinessHours;
                break;
            case "Hospital":
                this.HospitalBusinessHours = EntityBusinessHours;
                break;
            default:
                break;
        }
    }

    public String getRestaurantTaxes(){return this.RestaurantTaxes;}
    public void setRestaurantTaxes(String RestaurantTaxes){this.RestaurantTaxes = RestaurantTaxes;}

    public int getRestaurantPageNumber(){return this.RestaurantPageNumber;}
    public void setRestaurantPageNumber(int cnt){this.RestaurantPageNumber = cnt;}*/
}
