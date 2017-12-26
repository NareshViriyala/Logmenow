package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.logmenow.R;

import database.DBHelper;
import shared.CommonClasses;
import shared.GlobalClass;

/**
 * Created by nviriyala on 27-07-2016.
 */
public class DialogRestOrderPlaced extends Dialog {
    private Button btn_ok;
    private ImageView img_entitylogo;
    private GlobalClass gc;
    private Context context;
    private DBHelper mydb;
    private CommonClasses cc;

    private TextView tv_line1;
    private TextView tv_line2;

    private String line1_txt;
    private String line2_txt;

    public DialogRestOrderPlaced(Context context, String line1_txt, String line2_txt) {
        super(context);
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        this.line1_txt = line1_txt;
        this.line2_txt = line2_txt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rest_orderplaced);
        gc = (GlobalClass) context.getApplicationContext();

        img_entitylogo = (ImageView) findViewById(R.id.img_entitylogo);

        byte[] bytes = mydb.getEntityImage("Restaurant");
        Bitmap bitmap = null;

        if(bytes != null) {
            bitmap = cc.getBitmap(bytes);
            img_entitylogo.setImageBitmap(bitmap);
        }
        else
            img_entitylogo.setImageResource(R.drawable.ic_noimage);

        tv_line1 = (TextView) findViewById(R.id.tv_line1);
        tv_line1.setText(line1_txt);
        tv_line2 = (TextView) findViewById(R.id.tv_line2);
        tv_line2.setText(line2_txt);

        btn_ok = (Button) findViewById(R.id.btn_ok);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}