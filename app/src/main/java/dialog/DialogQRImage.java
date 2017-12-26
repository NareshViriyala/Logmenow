package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.home.logmenow.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import database.DBHelper;

/**
 * Created by nviriyala on 23-08-2016.
 */
public class DialogQRImage extends Dialog {

    private Button btn_ok;
    private ImageView img_qr;
    private DBHelper mydb;
    private String QRContent;
    private Context context;
    private String PageName = "DialogQRImage";
    private int QRsize = 500;

    public DialogQRImage(Context context, String QRContent) {
        super(context);
        this.context = context;
        mydb = new DBHelper(context);
        this.QRContent = QRContent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_qrimage);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        img_qr = (ImageView) findViewById(R.id.img_qr);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        new GenerateQRCode().execute(QRContent);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public class GenerateQRCode extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute(){
            //dps.show();
        }

        @Override
        protected Bitmap doInBackground(String[] params) {
            //android.os.Debug.waitForDebugger();
            Bitmap QRCode = null;
            try {
                String TexttoQR = params[0];
                BitMatrix result = new MultiFormatWriter().encode(TexttoQR, BarcodeFormat.QR_CODE, QRsize, QRsize, null);
                int w = result.getWidth();
                int h = result.getHeight();
                int[] pixels = new int[w*h];
                for (int y = 0; y < h; y++) {
                    int offset = y * w;
                    for (int x = 0; x < w; x++) {
                        pixels[offset + x] = result.get(x, y) ? context.getResources().getColor(R.color.black) : context.getResources().getColor(R.color.white);
                    }
                }
                QRCode = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                QRCode.setPixels(pixels, 0, QRsize, 0, 0, w, h);
            }
            catch (WriterException e){

            }
            return QRCode;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            //android.os.Debug.waitForDebugger();
            super.onPostExecute(bitmap);
            try{
                //Toast.makeText(getActivity(), img_QRCode.getWidth()+"", Toast.LENGTH_SHORT).show();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(QRsize, QRsize);
                //layoutParams.setMargins(10,10,10,10);
                img_qr.setLayoutParams(layoutParams);
                img_qr.setImageBitmap(bitmap);
                //img_QRCode.setPadding(10,10,10,10);

                //dps.dismiss();
            }
            catch (Exception e){mydb.logAppError(PageName, "onPostExecute", "Exception", e.getMessage());}
        }
    }
}
