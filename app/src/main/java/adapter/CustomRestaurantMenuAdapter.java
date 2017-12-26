package adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.home.logmenow.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import database.DBHelper;
import shared.Models.*;

/**
 * Created by Home on 7/24/2016.
 */
public class CustomRestaurantMenuAdapter extends BaseAdapter {

    private Context context;
    private List<RestaurantMenuItem> rmiList;
    private List<RestaurantMenuItem> filteredData = new ArrayList<>();
    private DBHelper mydb;
    String PageName = "CustomRestaurantMenuAdapter";
    String prevGrpname = "";
    List<Boolean> headerlist = new ArrayList<>();


    public CustomRestaurantMenuAdapter(Context context, List<RestaurantMenuItem> rmiList){
        this.context = context;
        mydb = new DBHelper(context);
        this.rmiList = rmiList;
    }

   public void addItemstoListAdapter(List<RestaurantMenuItem> rmiadditionalList){
       rmiList.addAll(rmiadditionalList);
       this.notifyDataSetChanged();
   }

    public void refreshList(){
        rmiList.clear();
        rmiList.addAll(mydb.getSavedMenuItems("TotalItems"));
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return rmiList.size();
    }

    @Override
    public Object getItem(int position) {
        return rmiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View clickedItem, ViewGroup parent){
        View v = null;
        try{
            RestaurantMenuItem item = rmiList.get(position);
            v = View.inflate(context, R.layout.item_restaurantmenu, null);

            if(headerlist.size() > position){
                if(headerlist.get(position)){
                    LinearLayout ll_itemgroup = (LinearLayout) v.findViewById(R.id.ll_itemgroup);
                    TextView tv_subgrpname = (TextView) v.findViewById(R.id.tv_subgrpname);
                    tv_subgrpname.setText(item.getIG());
                    ll_itemgroup.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (!item.getIG().equalsIgnoreCase(prevGrpname) && position != 0) {
                    prevGrpname = item.getIG();
                    LinearLayout ll_itemgroup = (LinearLayout) v.findViewById(R.id.ll_itemgroup);
                    TextView tv_subgrpname = (TextView) v.findViewById(R.id.tv_subgrpname);
                    tv_subgrpname.setText(item.getIG());
                    ll_itemgroup.setVisibility(View.VISIBLE);
                    headerlist.add(true);
                } else {
                    headerlist.add(false);
                    prevGrpname = item.getIG();
                }
            }

            LinearLayout ll_line1 = (LinearLayout) v.findViewById(R.id.ll_line1);
            LinearLayout ll_line2 = (LinearLayout) v.findViewById(R.id.ll_line2);
            TextView tv_oid = (TextView) v.findViewById(R.id.tv_oid);
            ImageView img_itemtype = (ImageView) v.findViewById(R.id.img_itemtype);
            ImageView img_spicecounter = (ImageView) v.findViewById(R.id.img_spicecounter);
            ImageView img_chefchoice = (ImageView) v.findViewById(R.id.img_chefchoice);
            TextView tv_rating = (TextView) v.findViewById(R.id.tv_rating);
            /*ImageView img_star1 = (ImageView) v.findViewById(R.id.img_star1);
            ImageView img_star2 = (ImageView) v.findViewById(R.id.img_star2);
            ImageView img_star3 = (ImageView) v.findViewById(R.id.img_star3);
            ImageView img_star4 = (ImageView) v.findViewById(R.id.img_star4);
            ImageView img_star5 = (ImageView) v.findViewById(R.id.img_star5);*/
            TextView tv_itemname = (TextView) v.findViewById(R.id.tv_itemname);
            TextView tv_itemprice = (TextView) v.findViewById(R.id.tv_itemprice);
            ImageView img_selected = (ImageView) v.findViewById(R.id.img_selected);

            //Item Order ID
            tv_oid.setText(item.getOID()+"");

            //Item Type
            if(item.getIT())
                img_itemtype.setImageResource(R.drawable.ic_veg);
            else
                img_itemtype.setImageResource(R.drawable.ic_nonveg);

            //Spice Counter
            if(item.getSI() > 0)
                img_spicecounter.setImageResource(R.drawable.ic_spicy);

            //Chef Choice
            if(item.getCR())
                img_chefchoice.setImageResource(R.drawable.ic_chefchoice);

            //Item Name
            String itemnamedesc = "<b>"+item.getIN()+"</b>";
            if(!item.getID().equalsIgnoreCase(""))
                itemnamedesc = itemnamedesc+"<br/><small>"+item.getID()+"</small>";
            Spanned textDecoration = Html.fromHtml(itemnamedesc);
            tv_itemname.setText(textDecoration);


            //Item Price
            tv_itemprice.setText(item.getIP()+"/-");


            //Rating Text
            if(item.getUsr() != 0) {
                tv_rating.setText(item.getUsr() + " users rated: ");
            }

            //Selected Item
            if(item.getSelected()) {
                img_selected.setVisibility(View.VISIBLE);
            }

            /*switch (item.getRT()){
                case 1:
                    img_star1.setImageResource(R.drawable.ic_star);
                    img_star1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    break;
                case 2:
                    img_star1.setImageResource(R.drawable.ic_star);
                    img_star1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star2.setImageResource(R.drawable.ic_star);
                    img_star2.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    break;
                case 3:
                    img_star1.setImageResource(R.drawable.ic_star);
                    img_star1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star2.setImageResource(R.drawable.ic_star);
                    img_star2.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star3.setImageResource(R.drawable.ic_star);
                    img_star3.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    break;
                case 4:
                    img_star1.setImageResource(R.drawable.ic_star);
                    img_star1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star2.setImageResource(R.drawable.ic_star);
                    img_star2.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star3.setImageResource(R.drawable.ic_star);
                    img_star3.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star4.setImageResource(R.drawable.ic_star);
                    img_star4.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    break;
                case 5:
                    img_star1.setImageResource(R.drawable.ic_star);
                    img_star1.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star2.setImageResource(R.drawable.ic_star);
                    img_star2.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star3.setImageResource(R.drawable.ic_star);
                    img_star3.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star4.setImageResource(R.drawable.ic_star);
                    img_star4.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    img_star5.setImageResource(R.drawable.ic_star);
                    img_star5.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    break;
                default:
                    break;
            }*/

            if(item.getSelected()){
                /*img_star1.setColorFilter(context.getResources().getColor(R.color.white));
                img_star2.setColorFilter(context.getResources().getColor(R.color.white));
                img_star3.setColorFilter(context.getResources().getColor(R.color.white));
                img_star4.setColorFilter(context.getResources().getColor(R.color.white));
                img_star5.setColorFilter(context.getResources().getColor(R.color.white));*/
                tv_rating.setTextColor(context.getResources().getColor(R.color.white));
                tv_itemname.setTextColor(context.getResources().getColor(R.color.white));
                tv_itemprice.setTextColor(context.getResources().getColor(R.color.white));
                ll_line1.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                ll_line2.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            }
            v.setTag(rmiList.get(position).getOID());
        }
        catch (Exception e){
            mydb.logAppError(PageName, "getView", "Exception", e.getMessage());
        }
        return v;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        rmiList.clear();
        filteredData.clear();
        filteredData.addAll(mydb.getSavedMenuItems("TotalItems"));

        if (charText.length() == 0)
            rmiList.addAll(filteredData);
        else
        {
            for (RestaurantMenuItem item : filteredData)
            {
                if (item.getIN().toLowerCase().contains(charText) || item.getID().toLowerCase().contains(charText) || item.getIG().toLowerCase().contains(charText))
                    rmiList.add(item);
            }
        }
        notifyDataSetChanged();
    }
}
