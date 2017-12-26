package shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import database.DBHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Home on 6/25/2016.
 */
public class HTTPCallJSon {

    private DBHelper mydb;
    private String PageName = "HTTPCallJSon";
    //private String apiURL = "http://caremetricsdemo.ihealthtechnologies.com/api/";
    private String apiURL = "http://www.logmenow.com/db_calls/";
    public String feedbackapiURL = "http://www.logmenow.com/";

    public HTTPCallJSon(Context context){mydb = new DBHelper(context);}

    public String Post(String rawControllerName, String jsonPostString){
        String returnStr = "";
        String controllerName = rawControllerName+".php";
        try{
            URL url = new URL(apiURL+controllerName);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("POST");
            httpurlconnection.setReadTimeout(10000);
            //httpurlconnection.setConnectTimeout(10000);
            httpurlconnection.setDoInput(true);


            httpurlconnection.setDoOutput(true);
            OutputStream ops = httpurlconnection.getOutputStream();
            ops.write(("="+jsonPostString).getBytes());
            ops.flush();
            ops.close();

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }
            httpurlconnection.disconnect();
            returnStr = returnStr.replace("\\\"", "\"");
            returnStr = returnStr.substring(1, returnStr.length()-1);
        }
        catch(Exception e){mydb.logAppError(PageName, "Post", "Exception", e.getMessage());}
        return returnStr;
    }

    public String Get(String rawControllerName, String querystring){
        String returnStr = "";
        String controllerName = rawControllerName+".php";
        querystring = querystring.replace(" ","%20");
        try{
            URL url = new URL(apiURL+controllerName+querystring);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            httpurlconnection.setReadTimeout(10000);
            httpurlconnection.setDoInput(true);

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }
            /*else{
                int i = httpurlconnection.getResponseCode();
                String MethodName = httpurlconnection.getResponseMessage();
                InputStream inStream = httpurlconnection.getErrorStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }*/
            httpurlconnection.disconnect();
        }
        catch(Exception e){mydb.logAppError(PageName, "Get", "Exception", e.getMessage());}
        return returnStr;
    }



    public String GetString(String cname){
        String returnStr = "";
        try{
            URL url = new URL(feedbackapiURL+cname);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            httpurlconnection.setReadTimeout(10000);
            httpurlconnection.setDoInput(true);

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
                String temp = "";
                while ((temp = bReader.readLine()) != null) {
                    returnStr += temp;
                }
                bReader.close();
            }
            httpurlconnection.disconnect();
        }
        catch(Exception e){mydb.logAppError(PageName, "GetString", "Exception", e.getMessage());}
        return returnStr;
    }

    /*
    public Bitmap GetImage(String rawControllerName, String querystring){
        Bitmap bitmap = null;
        String controllerName = rawControllerName+".php";
        try{
            URL url = new URL(apiURL+controllerName+querystring);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }
        catch(Exception e){mydb.logAppError(PageName, "GetImage", "Exception", e.getMessage());}
        return bitmap;
    }
    */

    public Bitmap GetImage(String rawControllerName, String querystring){
        Bitmap bitmap = null;
        String controllerName = rawControllerName+".php";
        try{
            URL url = new URL(apiURL+controllerName+querystring);
            HttpURLConnection  httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            httpurlconnection.setReadTimeout(10000);
            httpurlconnection.setDoInput(true);

            if(httpurlconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream inStream = httpurlconnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
            }
            httpurlconnection.disconnect();
        }
        catch(Exception e){mydb.logAppError(PageName, "GetImage", "Exception", e.getMessage());}
        return bitmap;
    }

}
