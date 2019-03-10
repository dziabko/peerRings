package github.nisrulz.projectqreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static github.nisrulz.projectqreader.qrGeneration.JSON;

public class qrAccept extends AppCompatActivity {


    TextView buyOrderTextView;
    TextView sellOrderTextView;

    Double buyAmount;
    Double sellAmount;

    String buyToken;
    String sellToken;

    String privateKey;
    String publicKey;


    Button acceptBtn;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_accept);

        //Buy & Sell Orders
        buyOrderTextView = (TextView) findViewById(R.id.buyOrder2);
        sellOrderTextView = (TextView) findViewById(R.id.sellOrder2);

        Bundle bundle = getIntent().getExtras();

        //Public & Private Keys
        publicKey = bundle.getString("publicKey");
        privateKey = bundle.getString("privateKey");

        Log.d("Test", "pb:" + publicKey);
        Log.d("Test", "pk:" + privateKey);
        Log.d("Test", "pk:" + bundle.get("data").toString());


        //Build the JSON object from bundle
        JSONParser parser = new JSONParser();
        JSONObject json;
        final JSONObject obj;
        try {
            json = (JSONObject) parser.parse(bundle.get("data").toString());


            //Setting inverse of counterpartie's order
            buyOrderTextView.setText("BUY: " + json.get("sellAmount").toString() + json.get("sellToken").toString());
            sellOrderTextView.setText("SELL: " +json.get("buyAmount").toString() + json.get("buyToken").toString());

            //Buy/Sell amounts/tokens
            sellAmount = Double.valueOf(json.get("buyAmount").toString());
            buyAmount = Double.valueOf(json.get("sellAmount").toString());

            buyToken = json.get("sellToken").toString();
            sellToken = json.get("buyToken").toString();
        }catch(Exception e){
            System.out.println(e.toString());
        }


        obj = new JSONObject();
        try {
            obj.put("sellAmount", sellAmount);
            obj.put("sellToken", sellToken);
            obj.put("buyAmount", buyAmount);
            obj.put("buyToken", buyToken);
            obj.put("privateKey", privateKey);
            obj.put("publicKey", publicKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        acceptBtn = (Button) findViewById(R.id.accept);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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
