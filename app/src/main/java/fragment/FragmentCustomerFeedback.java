package fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.home.logmenow.R;

import org.json.JSONArray;

import activity.ActivityHome;
import database.DBHelper;
import shared.CommonClasses;
import shared.GlobalClass;
import shared.HTTPCallJSon;
import shared.NetworkDetector;
import android.widget.Toast;

/**
 * Created by Home on 2/19/2017.
 */
public class FragmentCustomerFeedback extends Fragment {
    private DBHelper mydb;
    private String PageName = "FragmentCustomerFeedback";

    private String guid = "", feedbacksubject = "";
    private GlobalClass gc;
    private CommonClasses cc;
    private NetworkDetector nd;

    private WebView webView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_customerfeedback, container, false);
        try {
            mydb = new DBHelper(getActivity());
            gc = (GlobalClass) getActivity().getApplicationContext();
            nd = new NetworkDetector(getActivity());
            cc = new CommonClasses(getActivity());
            guid = mydb.getSystemParameter("FeedbackGUID");
            feedbacksubject = mydb.getSystemParameter("FeedbackSubject");
            webView = (WebView) rootView.findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.getSettings().setAppCacheEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            webView.getSettings().setUseWideViewPort(true);
            webView.setWebViewClient(new WebViewClient());
            webView.addJavascriptInterface(new Object()
                 {
                     @JavascriptInterface
                     public void performClick()
                     {
                         //Toast.makeText(getActivity(), "This device does not support for Google Play Service!\nNotifications might not work", Toast.LENGTH_LONG).show();
                         ((ActivityHome)getActivity()).loadFragment("FragmentHome",true);
                     }
                 }, "btn_back_mobile");
            webView.addJavascriptInterface(new Object()
                {
                    @JavascriptInterface
                    public void performClick()
                    {
                        Toast.makeText(getActivity(), "Thank you for your feedback", Toast.LENGTH_LONG).show();
                        ((ActivityHome)getActivity()).loadFragment("FragmentHome",true);
                    }
                }, "btn_submit_mobile");
            //webView.setHorizontalScrollBarEnabled(false);
            new getFeedbackpage().execute(guid);
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreateView", "Exception", e.getMessage());}
        return rootView;
    }

    public class getFeedbackpage extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            String returnid = "0";

            try {
                String resultid = new HTTPCallJSon(getActivity()).Get("GetIDFromGUID", "?guid=" + params[0]);
                if(!resultid.equalsIgnoreCase("[]")){
                    JSONArray jarray = new JSONArray(resultid);
                    returnid = jarray.getJSONObject(0).getString("SubjectID");
                    //returnid = new HTTPCallJSon(getActivity()).GetString("feedback_"+returnid);
                }
            }
            catch (Exception e){mydb.logAppError(PageName, "getFeedbackpage--doInBackground", "Exception", e.getMessage());}
            return returnid;
        }

        @Override
        protected void onPostExecute(String returnid) {
            super.onPostExecute(returnid);
            try {

                if(!returnid.equalsIgnoreCase("0")){
                    String feedbackurl = new HTTPCallJSon(getActivity()).feedbackapiURL;
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(feedbackurl+"customerfeedback.php?route="+mydb.getDeviceID()+"&reroute="+returnid+"&type="+feedbacksubject);
                }

            }catch (Exception e){mydb.logAppError(PageName, "getFeedbackpage--onPostExecute", "Exception", e.getMessage());}
        }
    }
}
