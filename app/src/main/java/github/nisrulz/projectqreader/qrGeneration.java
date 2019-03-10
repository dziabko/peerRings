package github.nisrulz.projectqreader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

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

    //Sell/Buy Spinners
    Spinner mySpinner1;
    Spinner mySpinner2;

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


        final Activity activity = this;

        Thread getFirstBalanceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Timer t = new Timer();

                t.scheduleAtFixedRate(
                        new TimerTask()
                        {
                            public void run()
                            {
                                mySpinner1 = (Spinner) findViewById(R.id.buyToken);
                                mySpinner2 = (Spinner) findViewById(R.id.sellToken);
                                buyToken = mySpinner1.getSelectedItem().toString();
                                sellToken = mySpinner2.getSelectedItem().toString();

                                final TextView buyText = (TextView) findViewById(R.id.currentTokenAmount2);
                                final TextView sellText = (TextView) findViewById(R.id.currentTokenAmount1);
                                //Set the textviews for balance
                                final TextView textView = (TextView) findViewById(R.id.text);

                                String response = "";

                                Request request = new Request.Builder()
                                        .url("http://localhost:3000/balance/" + buyToken + "/" + publicKey)
                                        .build();

                                try (Response currentBuyCurrency = client.newCall(request).execute()) {
                                    final String currentBuyCurrencyStr = currentBuyCurrency.body().string();
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            buyText.setText(currentBuyCurrencyStr + " " + buyToken);
                                        }
                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0,      // run first occurrence immediately
                        500);  // run every three seconds


            }
        });
        getFirstBalanceThread.start();
        Thread getSecondBalanceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Timer t = new Timer();

                t.scheduleAtFixedRate(
                        new TimerTask()
                        {
                            public void run()
                            {
                                mySpinner1 = (Spinner) findViewById(R.id.buyToken);
                                mySpinner2 = (Spinner) findViewById(R.id.sellToken);
                                buyToken = mySpinner1.getSelectedItem().toString();
                                sellToken = mySpinner2.getSelectedItem().toString();

                                final TextView buyText = (TextView) findViewById(R.id.currentTokenAmount2);
                                final TextView sellText = (TextView) findViewById(R.id.currentTokenAmount1                                                                                                                                                                                           );
                                //Set the textviews for balance
                                final TextView textView = (TextView) findViewById(R.id.text);

                                String response = "";

                                Request request = new Request.Builder()
                                        .url("http://localhost:3000/balance/" + sellToken + "/" + publicKey)
                                        .build();

                                try (Response currencySellCurrency = client.newCall(request).execute()) {
                                    final String currentSellCurrencyStr = currencySellCurrency.body().string();
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            sellText.setText(currentSellCurrencyStr + " " + sellToken);
                                        }
                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0,      // run first occurrence immediately
                        500);  // run every three seconds


            }
        });
        getSecondBalanceThread.start();


//        SharedPreferences sharedPref = qrGeneration.this.getPreferences(Context.MODE_PRIVATE);
//        privateKey = getResources().getString(R.string.private_key);
//        publicKey = getResources().getString(R.string.public_key);




        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Buy/Sell amounts/tokens
                sellAmount = Double.valueOf(((EditText) (findViewById(R.id.sell_value))).getText().toString());
                buyAmount = Double.valueOf(((EditText) (findViewById(R.id.buy_value))).getText().toString());

                mySpinner1 = (Spinner) findViewById(R.id.buyToken);
                mySpinner2 = (Spinner) findViewById(R.id.sellToken);
                buyToken = mySpinner1.getSelectedItem().toString();
                sellToken = mySpinner2.getSelectedItem().toString();

                final JSONObject obj = new JSONObject();
                try {
                    obj.put("sellAmount", sellAmount);
                    obj.put("sellToken", sellToken);
                    obj.put("buyAmount", buyAmount);
                    obj.put("buyToken", buyToken);
                    obj.put("privateKey", privateKey);
                    obj.put("publicKey", publicKey);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.d("JSON", obj.toString());
                inputValue = obj.toString();
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
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String response = "";

                            try {
                                response = post("http://localhost:3000/add-order", obj.toString());
                                Log.d("Test", "POSTED TO BLOCKCHAIN");
                                Log.d("Test", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    System.out.println(e.getStackTrace().toString());
                }


            }
        });
//        } else {
//
//            //If came in from scanning a QR code, display data
//            EditText editBuy = (EditText) findViewById(R.id.buy_value);
//            EditText editSell = (EditText) findViewById(R.id.sell_value);
//
//            //Get the data sent from prev screen
//            String data = bundle.get("data").toString();
//            JSONParser parser = new JSONParser();
//            JSONObject json = null;
//            try {
//                json = (JSONObject) parser.parse(data);
//
//                editBuy.setText(json.get("buyAmount").toString() + " " +  json.get("buyToken"));
//                editBuy.setEnabled(false);
//
//                editSell.setText(json.get("sellAmount").toString() + " " +  json.get("sellToken"));
//                editSell.setEnabled(false);
//
//
//                //Set the spinners invisible
//                mySpinner1 = (Spinner) findViewById(R.id.buyToken);
//                mySpinner1.setVisibility(View.INVISIBLE);
//                mySpinner2 = (Spinner) findViewById(R.id.sellToken);
//                mySpinner2.setVisibility(View.INVISIBLE);
//
//
//            }catch (Exception e){
//                System.out.println(e.toString());
//            }
//
//
////            editBuy.setText();


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
//        }
    }

    OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    String post(String url, String json) throws IOException {
//        java.net.URL url2 = new URL(url);
//        HttpURLConnection client = (HttpURLConnection) url2.openConnection();
//        client.setRequestMethod("POST");



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
