package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.home.logmenow.R;

/**
 * Created by Home on 7/9/2016.
 */
public class DialogNoInternet extends Dialog {
    private Button btn_ok;

    public DialogNoInternet(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_enable_internet);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        //this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}