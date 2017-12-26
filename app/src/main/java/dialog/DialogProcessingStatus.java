package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.example.home.logmenow.R;


/**
 * Created by Home on 6/29/2016.
 */
public class DialogProcessingStatus extends Dialog {
    public ImageView img_processing;
    public Context context;
    public String Style;
    public DialogProcessingStatus(Context context, String Style) {
        super(context);
        this.context = context;
        this.Style = Style;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_processing);
        this.setCanceledOnTouchOutside(false);
        //this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        switch (Style) {
            case "Bottom":
                this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                break;
            default:
                break;
        }
        img_processing = (ImageView) findViewById(R.id.img_processing);
        Glide.with(context).load(R.drawable.processing).into(img_processing);
    }
}
