/*
 * Copyright (C) 2014 - Simone Martucci <martucci.simone.91@gmail.com>
 * Copyright (C) 2014 - Mattia Mancini <mattia.mancini.1991@gmail.com>
 *
 * This file is part of Foundme Professore.
 *
 * Foundme Professore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foundme Professore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foundme Professore.  If not, see <http://www.gnu.org/licenses/>.
 */

package foundme.uniroma2.it.professore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoginActivity extends Activity {

    public static SharedPreferences pref;

    public static String user = null;
    public static String pass = null;
    private TextView tvRegistration;
    private EditText etUser;
    private EditText etPpass;
    private Button btAccess;
    private String TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GetSharedPref();

        setContentView(R.layout.activity_login);

        btAccess = (Button) findViewById(R.id.btAccess);
        etUser = (EditText) findViewById(R.id.etUserName);
        etPpass = (EditText) findViewById(R.id.etPassword);
        tvRegistration = (TextView) findViewById(R.id.tvRegistration);

        tvRegistration.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent goToRegistration = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(goToRegistration);
            }
        });

        btAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                user = etUser.getText().toString();
                pass = etPpass.getText().toString();
                pass = computeSHAHash.sha1(pass);

                new Login().execute(user,pass);
            }
        });

    }

    private void GetSharedPref() {
        pref = SPEditor.init(LoginActivity.this.getApplicationContext());
        user = SPEditor.getUser(pref);
        pass = SPEditor.getPass(pref);
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            return;
        }
        new Login().execute(user,pass);
    }

    public class Login extends AsyncTask<String, Void, String> {

        private String result = null;
        private String line = null;
        private InputStream is = null;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(LoginActivity.this, Variables_it.WAIT, Variables_it.LOGGING_IN);
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.MAIL, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.PASS, params[1]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://54.187.29.169/android/prof/login.php");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                return Variables_it.INVALID_IP;
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, Variables_it.ISO), 8);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            } catch (Exception e) {
                return Variables_it.FAIL_CONNECTION;
            }

            try {
                JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                return (json_data.getString(Variables_it.NAME));
            } catch (Exception e) {
                return Variables_it.JSON_FAILURE;
            }
        }

        protected void onPostExecute(String result) {
            caricamento.dismiss();
            if (result.equalsIgnoreCase(Variables_it.INVALID_IP) || result.equalsIgnoreCase(Variables_it.FAIL_CONNECTION) || result.equalsIgnoreCase(Variables_it.JSON_FAILURE))
                Toast.makeText(LoginActivity.this, Variables_it.ERROR, Toast.LENGTH_LONG).show();
            else {
                Bundle passedTAG = getIntent().getExtras();
                if (passedTAG != null) {
                    TAG = passedTAG.getString(Variables_it.TAG);
                }
                SPEditor.setUser(pref,user);
                SPEditor.setPass(pref,pass);
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra(Variables_it.NAME, result);
                intent.putExtra(Variables_it.TAG, TAG);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

}

