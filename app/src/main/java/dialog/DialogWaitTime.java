package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.logmenow.R;

import org.json.JSONObject;

import database.DBHelper;
import shared.CommonClasses;

/**
 * Created by Home on 8/10/2016.
 */
public class DialogWaitTime extends Dialog {

    private Button btn_ok;
    private Context context;
    private DBHelper mydb;
    private CommonClasses cc;

    private TextView tv_appstatus;
    private TextView tv_timer;
    private TextView tv_appid;
    private String Qcnt;
    private String waittime;

    public DialogWaitTime(Context context) {
        super(context);
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_hospital_waittime);
            JSONObject json = new JSONObject(mydb.getSystemParameter("DocApptID"));
            tv_timer = (TextView) findViewById(R.id.tv_timer);
            tv_appstatus = (TextView) findViewById(R.id.tv_appstatus);
            tv_appid = (TextView) findViewById(R.id.tv_appid);

            tv_appstatus.setText(json.getString("Qcnt"));
            tv_appid.setText(json.getString("ApptID"));
            String timertext = json.getString("ApptTime").replace("T", " ").substring(0,19);
            //String timertext = new Models.TimeStamp().getCurrentTimeStamp();
            String timer = cc.getTimerValue(timertext, Integer.parseInt(json.getString("AWT")));
            tv_timer.setText(timer);
            long fullTime = cc.StringToMilli(timer);
            new CountDownTimer(fullTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String timerText = tv_timer.getText().toString();
                    long inTime = cc.StringToMilli(timerText);
                    if(inTime <= 0)
                        this.cancel();
                    timerText = cc.MilliToString(inTime-1000);
                    tv_timer.setText(timerText);
                }

                @Override
                public void onFinish() {

                }
            }.start();
            btn_ok = (Button) findViewById(R.id.btn_ok);
            this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        catch (Exception e){mydb.logAppError("DialogWaitTime", "onCreate", "Exception", e.getMessage());}
    }
}
