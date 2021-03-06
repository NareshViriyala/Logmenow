package shared;

/**
 * Created by Home on 6/1/2016.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import activity.ActivityHome;
import database.DBHelper;

public class LocationHelper extends Service implements LocationListener{
    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private String PageName = "LocationHelper";
    protected LocationManager locationManager;
    private DBHelper mydb ;

    public LocationHelper(Context context) {
        this.mContext = context;
        mydb = new DBHelper(mContext);
        getLocation();
    }

    public Location getLocation(){
        try{
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //Toast.makeText(mContext, "Please enable Location service", Toast.LENGTH_SHORT).show();
            }
            else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        }
        catch (SecurityException secEx){
            //mydb.logAppError(PageName, "getLocation", "SecurityException", secEx.getMessage());

        }
        catch (Exception e){
            mydb.logAppError(PageName, "getLocation", "Exception", e.getMessage());
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        }
        catch (Exception e){
            mydb.logAppError(PageName, "onLocationChanged", "Exception", e.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void stopUsingGPS(){
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(LocationHelper.this);
            }
        }
        catch (SecurityException secEx){
            mydb.logAppError(PageName, "stopUsingGPS", "SecurityException", secEx.getMessage());
        }
        catch (Exception e){
            mydb.logAppError(PageName, "stopUsingGPS", "Exception", e.getMessage());
        }
    }

    public void showSettingsAlert(){
        try {
            //Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            //Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
            //intent.setData(uri);
            //mContext.startActivity(intent);


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("GPS not found!");  // GPS not found
            builder.setMessage("Enable GPS"); // Want to enable?
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("No", null);
            builder.create().show();
            return;

        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);

                *//*IntentFilter intF = new IntentFilter(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Instrumentation instrumentation = new Instrumentation();
                Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(intF, null, true);
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                instrumentation.startActivitySync(i);
                int gotback = 0;*//*
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();*/
        }
        catch (Exception e){}
    }

}

