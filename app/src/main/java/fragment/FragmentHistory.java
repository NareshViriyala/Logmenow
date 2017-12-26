package fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.CustomHistoryAdapter;
import database.DBHelper;
import shared.HTTPCallJSon;
import shared.Models.HistoryItem;
import shared.NetworkDetector;

/**
 * Created by nviriyala on 29-08-2016.
 */
public class FragmentHistory extends Fragment {
    private DBHelper mydb;
    private String PageName = "FragmentHistory";

    private ImageView img_loading;
    private ListView lv_items;

    private NetworkDetector nd;

    private int pagenumber = 0;
    private int pagecount = 10;
    private boolean fetchingdata = false;
    private boolean allFetched = false;
    private String DeviceID;
    private CustomHistoryAdapter cha;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        try {
            rootView = inflater.inflate(R.layout.fragment_history, container, false);
            mydb = new DBHelper(getActivity());
            nd = new NetworkDetector(getActivity());
            lv_items = (ListView) rootView.findViewById(R.id.lv_items);
            DeviceID = mydb.getDeviceID();
            img_loading = (ImageView) rootView.findViewById(R.id.img_loading);
            Glide.with(getActivity()).load(R.drawable.loading).into(img_loading);
            cha = new CustomHistoryAdapter(getActivity());
            lv_items.setAdapter(cha);
            lv_items.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(view.getLastVisiblePosition() == totalItemCount-1 && !fetchingdata && !allFetched){
                        if(nd.isInternetAvailable()) {
                            fetchingdata = true;
                            new getClientLog().execute();
                        }
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    public class getClientLog extends AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pagenumber++;
            img_loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String[] params) {
            String response = "";
            try{
                response = new HTTPCallJSon(getActivity()).Get("GetClientHistory","?DeviceID="+DeviceID+"&PageNumber="+pagenumber+"&PageCount="+pagecount);
            }
            catch (Exception e){mydb.logAppError(PageName, "getClientLog--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try{
                response = response.replace("|", "\"").replace("\"{", "{").replace("}\"", "}");
                //response = "[{\"Type\":\"Restaurant\",\"Info\":{\"EntityName\":\"Cream Stone\",\"Total items\":\"4\",\"Total bill\":\"481\"},\"LogTime\":\"2016-08-29T18:26:44.843\"}]";
                JSONArray jsonArray = new JSONArray(response);
                if(jsonArray.length() == 0){
                    allFetched = true;
                }
                else{
                    List<HistoryItem> fetchedList = new ArrayList<>();
                    if(jsonArray.length() < pagecount)
                        allFetched = true;
                    for(int i = 0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if(!jsonObject.getString("Info").equalsIgnoreCase("null")) {
                            HistoryItem item = new HistoryItem(jsonObject.getString("Type"), jsonObject.getString("Info"), jsonObject.getString("Image"), jsonObject.getString("LogTime").replace('T', ' ').substring(0, 19), jsonObject.getInt("OID"));
                            fetchedList.add(item);
                        }
                    }
                    cha.appentItems(fetchedList);
                }
                img_loading.setVisibility(View.GONE);
                fetchingdata = false;
            }
            catch (Exception e){
                mydb.logAppError(PageName, "getClientLog--onPostExecute", "Exception", e.getMessage());
                img_loading.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("History");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }
}
