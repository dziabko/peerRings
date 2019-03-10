package github.nisrulz.projectqreader;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class qrGeneration extends AppCompatActivity {

    String TAG = "GenerateQRCode";
    ImageView qrImage;
    Button generate, save;
    String inputValue;
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";

    //Sell/Buy Amount
    double sellAmount;
    double buyAmount;

    //Sell/Buy Token
    String sellToken;
    String buyToken;

    //Private Key
    String publicKey;
    String privateKey;

    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_generation);

        qrImage = (ImageView) findViewById(R.id.QR_Image);
        generate = (Button) findViewById(R.id.generate);
        save = (Button) findViewById(R.id.save);


        Bundle bundle = getIntent().getExtras();
        privateKey = bundle.getString("privateKey");
        publicKey = bundle.getString("publicKey");


        //Set the textviews for balance
        final TextView textView = (TextView) findViewById(R.id.text);




//        SharedPreferences sharedPref = qrGeneration.this.getPreferences(Context.MODE_PRIVATE);
//        privateKey = getResources().getString(R.string.private_key);
//        publicKey = getResources().getString(R.string.public_key);



        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Buy/Sell amounts/tokens
                sellAmount = Double.valueOf(((EditText) (findViewById(R.id.sell_value))).getText().toString());
                buyAmount = Double.valueOf(((EditText) (findViewById(R.id.buy_value))).getText().toString());

                Spinner mySpinner1 = (Spinner) findViewById(R.id.buyToken);
                Spinner mySpinner2 = (Spinner) findViewById(R.id.sellToken);
                buyToken = mySpinner1.getSelectedItem().toString();
                sellToken = mySpinner2.getSelectedItem().toString();

                final JSONObject obj = new JSONObject();
                try {
                    obj.put("sellAmount", sellAmount);
                    obj.put("buyAmount", buyAmount);
                    obj.put("publicKey", publicKey);
                    obj.put("privateKey", privateKey);
                    obj.put("buyTokenType", buyToken);
                    obj.put("sellTokenType", sellToken);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.d("JSON", obj.toString());
                inputValue = privateKey;
                if (inputValue.length() > 0) {
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int smallerDimension = width < height ? width : height;
                    smallerDimension = smallerDimension * 3 / 4;

                    qrgEncoder = new QRGEncoder(
                            inputValue, null,
                            QRGContents.Type.TEXT,
                            smallerDimension);
                    try {
                        bitmap = qrgEncoder.encodeAsBitmap();
                        qrImage.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        Log.v(TAG, e.toString());
                    }
                }

                //Post to the blockchain
                String response = "";
                try{
                     response = post("http://localhost:3000/add-order", obj.toString());}
                catch (Exception e){
                    System.out.println(e.getStackTrace().toString());
                }
                System.out.println(response);

            }
        });

//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean save;
//                String result;
//                try {
//                    save = QRGSaver.save(savePath, privateKey.getText().toString().trim(), bitmap, QRGContents.ImageType.IMAGE_JPEG);
//                    result = save ? "Image Saved" : "Image Not Saved";
//                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

}
