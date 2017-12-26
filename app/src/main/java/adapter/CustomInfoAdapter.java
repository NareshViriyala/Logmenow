package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.home.logmenow.R;

import java.util.List;

import database.DBHelper;
import shared.Models.*;

/**
 * Created by Home on 8/22/2016.
 */
public class CustomInfoAdapter extends BaseAdapter {

    private Context context;
    private List<CustomInfoItem> infoItems;
    private DBHelper mydb;
    String PageName = "CustomInfoAdapter";

    public CustomInfoAdapter(Context context, List<CustomInfoItem> infoItems){
        this.context = context;
        mydb = new DBHelper(context);
        this.infoItems = infoItems;
    }

    public void refreshList(List<CustomInfoItem> infoItems){
        //infoItems.clear();
        this.infoItems = infoItems;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return infoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return infoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View clickedItem, final ViewGroup parent){
        View v = null;
        try{
            CustomInfoItem item = infoItems.get(position);
            v = View.inflate(context, R.layout.item_custominfo, null);

            LinearLayout ll_item = (LinearLayout) v.findViewById(R.id.ll_item);
            TextView tv_id = (TextView) v.findViewById(R.id.tv_id);
            TextView tv_infoname = (TextView) v.findViewById(R.id.tv_infoname);
            TextView tv_infovalue = (TextView) v.findViewById(R.id.tv_infovalue);
            ImageView img_edit = (ImageView) v.findViewById(R.id.img_edit);
            ImageView img_delete = (ImageView) v.findViewById(R.id.img_delete);

            img_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });

            tv_id.setText(String.valueOf(item.getId()));
            //cb_select.setSelected(true);
            tv_infoname.setText(item.getInfoName()+":");
            if(item.getInfoName().equalsIgnoreCase("Vehicle No")) {
                String vehno = item.getInfoValue().substring(1);
                String vtype = item.getInfoValue().substring(0, 1);
                vtype = vtype.equalsIgnoreCase("0")?"Others":vtype+" Wheeler";
                Spanned textdec = Html.fromHtml(vehno+"<small> ("+vtype+")</small>");
                tv_infovalue.setText(textdec);
            }
            else
                tv_infovalue.setText(item.getInfoValue());

            if(item.getSelected()){
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                img_edit.setColorFilter(context.getResources().getColor(R.color.white));
                img_delete.setColorFilter(context.getResources().getColor(R.color.white));
                tv_infoname.setTextColor(context.getResources().getColor(R.color.white));
                tv_infovalue.setTextColor(context.getResources().getColor(R.color.white));
            }
            else {
                ll_item.setBackgroundColor(context.getResources().getColor(R.color.white));
                img_edit.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                img_delete.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                tv_infoname.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                tv_infovalue.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            }
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return v;
    }
}
