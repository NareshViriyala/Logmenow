package fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar.LayoutParams;

import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import adapter.CustomRestaurantMenuAdapter;
import database.DBHelper;
import shared.GlobalClass;
import shared.HTTPCallJSon;
import shared.Models;
import shared.NetworkDetector;

/**
 * Created by Home on 7/20/2016.
 */
public class FragmentRestaurantMenu extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener{
    private DBHelper mydb;
    private String PageName = "FragmentRestaurantMenu";
    private GlobalClass gc;
    private NetworkDetector nd;
    //private ScrollView srv_items;
    //private LinearLayout ll_resmenu;
    private ImageView img_loading;
    private ImageView img_itemsearch;
    private EditText et_itemsearch;
    //private FloatingActionButton fab;
    //private List<Models.RestaurantMenuItem> totalitemslist = new ArrayList<>();
    //private List<Models.RestaurantMenuItem> selecteditemslist = new ArrayList<>();
    private CustomRestaurantMenuAdapter customRestaurantMenuAdapter;

    private ListView lv_items;
    private TextView tv_grpname;
    private TextView tv_total;
    private ImageView img_checkout;

    private int pagenumber = 0;
    private int pagecount = 1000;
    private String guid = "";
    private View rootView = null;
    public boolean fetchingdata = false;
    public boolean allFetched = false;

    public LinearLayout ll_selections;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.fragment_restaurantmenu, container, false);
            mydb = new DBHelper(getActivity());
            nd = new NetworkDetector(getActivity());
            gc = (GlobalClass) getActivity().getApplicationContext();
            //gc.setguid("5b0a3d65-ba4c-4312-9910-0f13d994513d");
            guid = mydb.getSystemParameter("RestaurantGUID");
            //totalitemslist = mydb.getSavedMenuItems("TotalItems");
            //selecteditemslist = mydb.getSavedMenuItems("SelectedItems");
            if(!mydb.getSystemParameter("RestaurantPageNumber").equalsIgnoreCase(""))
                pagenumber = Integer.parseInt(mydb.getSystemParameter("RestaurantPageNumber"));
            //srv_items = (ScrollView) rootView.findViewById(R.id.srv_items);
            lv_items = (ListView) rootView.findViewById(R.id.lv_items);
            lv_items.setOnItemClickListener(this);

            ll_selections = (LinearLayout) rootView.findViewById(R.id.ll_selections);

            tv_grpname = (TextView) rootView.findViewById(R.id.tv_grpname);
            tv_total = (TextView) rootView.findViewById(R.id.tv_total);

            et_itemsearch = (EditText) rootView.findViewById(R.id.et_itemsearch);
            et_itemsearch.addTextChangedListener(new searchMenuItems());

            img_checkout = (ImageView) rootView.findViewById(R.id.img_checkout);
            img_checkout.setOnClickListener(this);

            img_itemsearch = (ImageView) rootView.findViewById(R.id.img_itemsearch);
            img_itemsearch.setOnClickListener(this);

            img_loading = (ImageView) rootView.findViewById(R.id.img_loading);
            //fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            //fab.setOnClickListener(this);
            Glide.with(getActivity()).load(R.drawable.processing).into(img_loading);
            if(mydb.getMenuItemCount("TotalItems") <= 0) {
                if(nd.isInternetAvailable())
                    new getMenuListItems().execute();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();
            }
            else {
                customRestaurantMenuAdapter = new CustomRestaurantMenuAdapter(getActivity(), mydb.getSavedMenuItems("TotalItems"));
                lv_items.setAdapter(customRestaurantMenuAdapter);
                showhideLoading(false);
                if(mydb.getMenuItemCount("SelectedItems") > 0)
                    updateSelection();
            }


            lv_items.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(mydb.getMenuItemCount("TotalItems") > firstVisibleItem)
                        tv_grpname.setText(mydb.getSavedMenuItems("TotalItems").get(firstVisibleItem).getIG());
                    else
                        tv_grpname.setText("");

                    if(view.getLastVisiblePosition() == totalItemCount-1 && mydb.getMenuItemCount("TotalItems") >= pagecount && !fetchingdata && !allFetched){
                        //Toast.makeText(getActivity(), "Reached last", Toast.LENGTH_SHORT).show();
                        fetchingdata = true;
                        if(nd.isInternetAvailable())
                            new getMenuListItems().execute();
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.nointernet), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            TextView tv_oid = (TextView) view.findViewById(R.id.tv_oid);
            int OID = Integer.parseInt(tv_oid.getText().toString());
            Models.RestaurantMenuItem item = mydb.getRestaurantMenuItem(OID);
            if(item.getSelected()){
                mydb.updateMenuItem(OID, false, 1);
                customRestaurantMenuAdapter.refreshList();
            }
            else{
                mydb.updateMenuItem(OID, true, 1);
                customRestaurantMenuAdapter.refreshList();
            }
            updateSelection();
        }
        catch(Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public void updateSelection(){
        try{
            ll_selections.removeAllViews();
            tv_total.setText(String.valueOf(mydb.getMenuItemCount("SelectedItems")));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            params.setMargins(10,10,0,10);
            for(Models.RestaurantMenuItem item : mydb.getSavedMenuItems("SelectedItems")){
                TextView tv = new TextView(getActivity());
                tv.setBackgroundResource(R.drawable.view_click);
                tv.setText(item.getOID()+"");
                tv.setOnClickListener(this);
                if(item.getOID() < 10)
                    tv.setPadding(32,15,32,15);
                else if (item.getOID() < 100)
                    tv.setPadding(25,15,25,15);
                else
                    tv.setPadding(15, 15, 15, 15);
                tv.setLayoutParams(params);
                ll_selections.addView(tv);
            }
        }
        catch(Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    public void loadMenuList(List<Models.RestaurantMenuItem> fetchedList){
        try{
            mydb.setSystemParameter("RestaurantPageNumber", String.valueOf(pagenumber));
            if(fetchedList.size() == 0) {
                allFetched = true;
                showhideLoading(false);
                return;
            }
            //totalitemslist.addAll(fetchedList);
            if(pagenumber == 1) {
                customRestaurantMenuAdapter = new CustomRestaurantMenuAdapter(getActivity(), fetchedList);
                lv_items.setAdapter(customRestaurantMenuAdapter);
            }
            else{
                customRestaurantMenuAdapter.refreshList();
                //customRestaurantMenuAdapter.addItemstoListAdapter(fetchedList);
            }
            showhideLoading(false);
        }
        catch (Exception e){mydb.logAppError(PageName, "loadMenuList", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try {
            if(v.getId() == R.id.img_checkout){
                Fragment fragment = new FragmentRestaurantMenuSummary();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                fragmentTransaction.replace(R.id.container_body, fragment, "FragmentRestaurantMenuSummary");
                fragmentTransaction.commit();
                return;
            }

            if(v.getId() == R.id.img_itemsearch){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(tv_grpname.getVisibility() == View.VISIBLE){
                    tv_grpname.setVisibility(View.GONE);
                    et_itemsearch.setVisibility(View.VISIBLE);
                    et_itemsearch.requestFocus();
                    et_itemsearch.setText("");
                    imm.showSoftInput(et_itemsearch, InputMethodManager.SHOW_IMPLICIT);
                    img_itemsearch.setImageResource(R.drawable.ic_cross);
                }else{
                    customRestaurantMenuAdapter.filter("");
                    tv_grpname.setVisibility(View.VISIBLE);
                    et_itemsearch.setVisibility(View.GONE);
                    img_itemsearch.setImageResource(R.drawable.ic_search);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return;
            }
            int position = Integer.parseInt(((TextView)v).getText().toString());
            /*ColorDrawable[] color = {new ColorDrawable(getResources().getColor(R.color.colorPrimary)), new ColorDrawable(getResources().getColor(R.color.white))};
            TransitionDrawable trans = new TransitionDrawable(color);
            v.setBackgroundDrawable(trans);
            trans.startTransition(200);*/
            //lv_items.smoothScrollToPosition(position-1);
            //lv_items.setSelection(position-1);
            lv_items.smoothScrollToPositionFromTop(position-1, 0, 0);
            //Toast.makeText(getActivity(), ((TextView) v).getText(), Toast.LENGTH_SHORT).show();

        }
        catch(Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    public class searchMenuItems implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            customRestaurantMenuAdapter.filter(s.toString());
        }
    }

    public class getMenuListItems extends AsyncTask<Object, Integer, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pagenumber = pagenumber+1;
            showhideLoading(true);
        }

        @Override
        protected String doInBackground(Object... params) {
            //android.os.Debug.waitForDebugger();
            String response = "";
            try {
                response = new HTTPCallJSon(getActivity()).Get("GetRestaurantMenu", "?input="+guid+"&PageNumber="+pagenumber+"&PageCount="+pagecount);
                //response = "[{\"IT\":true,\"IG\":\"Rice Non-Veg\",\"IN\":\"Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":2,\"CR\":true,\"Usr\":109,\"RT\":2,\"OID\":1},{\"IT\":true,\"IG\":\"Rice Non-Veg\",\"IN\":\"Egg Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":235,\"RT\":4,\"OID\":2},{\"IT\":true,\"IG\":\"Rice Non-Veg\",\"IN\":\"Schezwan Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":168,\"RT\":2,\"OID\":3},{\"IT\":true,\"IG\":\"Rice Non-Veg1\",\"IN\":\"Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":2,\"CR\":true,\"Usr\":167,\"RT\":4,\"OID\":4},{\"IT\":true,\"IG\":\"Rice Non-Veg1\",\"IN\":\"Egg Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":267,\"RT\":5,\"OID\":5},{\"IT\":true,\"IG\":\"Rice Non-Veg1\",\"IN\":\"Schezwan Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":135,\"RT\":5,\"OID\":6},{\"IT\":true,\"IG\":\"Rice Non-Veg2\",\"IN\":\"Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":2,\"CR\":true,\"Usr\":428,\"RT\":3,\"OID\":7},{\"IT\":true,\"IG\":\"Rice Non-Veg2\",\"IN\":\"Egg Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":317,\"RT\":1,\"OID\":8},{\"IT\":true,\"IG\":\"Rice Non-Veg2\",\"IN\":\"Schezwan Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":176,\"RT\":4,\"OID\":9},{\"IT\":true,\"IG\":\"Rice Non-Veg3\",\"IN\":\"Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":2,\"CR\":true,\"Usr\":19,\"RT\":3,\"OID\":10},{\"IT\":true,\"IG\":\"Rice Non-Veg3\",\"IN\":\"Egg Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":175,\"RT\":2,\"OID\":11},{\"IT\":true,\"IG\":\"Rice Non-Veg3\",\"IN\":\"Schezwan Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":55,\"RT\":4,\"OID\":12},{\"IT\":true,\"IG\":\"Rice Non-Veg4\",\"IN\":\"Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":2,\"CR\":true,\"Usr\":216,\"RT\":1,\"OID\":13},{\"IT\":true,\"IG\":\"Rice Non-Veg4\",\"IN\":\"Egg Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":482,\"RT\":1,\"OID\":14},{\"IT\":true,\"IG\":\"Rice Non-Veg4\",\"IN\":\"Schezwan Chicken Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":false,\"Usr\":174,\"RT\":1,\"OID\":15},{\"IT\":false,\"IG\":\"Rice Veg\",\"IN\":\"Schezwan Vegetable Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":0,\"CR\":false,\"Usr\":68,\"RT\":2,\"OID\":16},{\"IT\":false,\"IG\":\"Rice Veg\",\"IN\":\"Vegetable Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":true,\"Usr\":116,\"RT\":3,\"OID\":17},{\"IT\":false,\"IG\":\"Rice Veg1\",\"IN\":\"Schezwan Vegetable Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":0,\"CR\":false,\"Usr\":222,\"RT\":2,\"OID\":18},{\"IT\":false,\"IG\":\"Rice Veg1\",\"IN\":\"Vegetable Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":1,\"CR\":true,\"Usr\":445,\"RT\":1,\"OID\":19},{\"IT\":false,\"IG\":\"Rice Veg2\",\"IN\":\"Schezwan Vegetable Fried Rice\",\"IP\":184,\"ID\":\"\",\"SI\":0,\"CR\":false,\"Usr\":227,\"RT\":4,\"OID\":20}]";
                //response = response.replace("\\\"", "\"");
            }
            catch(Exception e){mydb.logAppError(PageName, "getMenuListItems--doInBackground", "Exception", e.getMessage());}
            return response;
        }

        @Override
        protected void onPostExecute(String item) {
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(item);
            try {
                JSONArray jsonArray = new JSONArray(item);
                List<Models.RestaurantMenuItem> fetchedList = new ArrayList<>();
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jo = jsonArray.getJSONObject(i);
                    jo.put("Selected", false);
                    jo.put("QTY", 1);
                    Gson gson = new Gson();
                    Type type = new TypeToken<Models.RestaurantMenuItem>(){}.getType();
                    Models.RestaurantMenuItem rmi = gson.fromJson(jo.toString(), type);
                    fetchedList.add(rmi);
                }
                mydb.addMenuItems(fetchedList);
                loadMenuList(fetchedList);
            }
            catch (JSONException e){mydb.logAppError(PageName, "getMenuListItems--onPostExecute", "Exception", e.getMessage());}
            catch(Exception e){mydb.logAppError(PageName, "getMenuListItems--onPostExecute", "Exception", e.getMessage());}
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            TextView tv_title = (TextView)getActivity().findViewById(R.id.tv_title);
            tv_title.setText("Restaurant Menu");

            ImageView img_back = (ImageView)getActivity().findViewById(R.id.img_back);
            img_back.setVisibility(View.VISIBLE);
        }
        catch (Exception e){mydb.logAppError(PageName, "onResume", "Exception", e.getMessage());}
    }

    public void showhideLoading(boolean show){
        try{
            if(show)
                img_loading.setVisibility(View.VISIBLE);
            else {
                img_loading.setVisibility(View.GONE);
                fetchingdata = false;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "showhideLoading", "Exception", e.getMessage());}
    }
}
