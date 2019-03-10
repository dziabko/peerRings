/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.nisrulz.projectqreader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class MainActivity extends AppCompatActivity {

  //Public/private keys
  EditText publicKey;
  EditText privateKey;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.greet_screen);

    Button btnQR = findViewById(R.id.submitKeys);

    publicKey = (EditText) findViewById(R.id.publicKey);
    privateKey = (EditText) findViewById(R.id.privateKey);


    btnQR.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //Store the public & private keys
        Log.d("Test","PubKey:" + publicKey.getText().toString());
        Log.d("Test","PrivKey:" + privateKey.getText().toString());




        //Move to next screen
        Intent intentMain = new Intent(MainActivity.this ,
                qrScan.class);
        intentMain.putExtra("privateKey", privateKey.getText().toString());
        intentMain.putExtra("publicKey", publicKey.getText().toString());
        MainActivity.this.startActivity(intentMain);
//        Log.i("Content "," Main layout ");

      }
    });
  }

  }
