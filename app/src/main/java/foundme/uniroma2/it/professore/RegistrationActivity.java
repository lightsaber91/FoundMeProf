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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.concurrent.ExecutionException;

public class RegistrationActivity extends Activity {

    private SharedPreferences pref;

    private Button btSignUp;
    private EditText etNewUser;
    private EditText etNewPass1;
    private EditText etNewPass2;
    private EditText etNewMail;
    private EditText etNewMail2;
    private EditText etNewDept;

    private String NewUser = null;
    private String NewPass1 = null;
    private String NewPass2 = null;
    private String NewMail = null;
    private String NewMail2 = null;
    private String NewDept = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        btSignUp = (Button) findViewById(R.id.btSignUp);
        etNewUser = (EditText) findViewById(R.id.etNewUser);
        etNewPass1 = (EditText) findViewById(R.id.etNewPass);
        etNewPass2 = (EditText) findViewById(R.id.etNewPass2);
        etNewMail = (EditText) findViewById(R.id.etNewMail);
        etNewMail2 = (EditText) findViewById(R.id.etNewMail2);
        etNewDept = (EditText) findViewById(R.id.etNewDept);

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NewUser = etNewUser.getText().toString();
                NewPass1 = etNewPass1.getText().toString();
                NewPass2 = etNewPass2.getText().toString();
                NewMail = etNewMail.getText().toString();
                NewMail2 = etNewMail2.getText().toString();
                NewDept = etNewDept.getText().toString();

                if (!checkLoginData(NewPass1, NewPass2, NewMail, NewMail2)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        manageRegistration(NewUser, NewPass1, NewMail, NewDept);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    void manageRegistration(String name, String pass, String mail, String dept) throws ExecutionException, InterruptedException {
        pass = computeSHAHash.sha1(pass);
        new Signup().execute(name, pass, mail, dept);
    }

    boolean checkLoginData(String pass1, String pass2, String mail1, String mail2) {
        if (pass1.equals(pass2) && mail1.equals(mail2)) {
            int l = pass1.length();
            return l >= 8;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public class Signup extends AsyncTask<String, Void, String> {

        private InputStream is = null;
        private String result = null;
        private String line = null;
        private int code;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(RegistrationActivity.this, Variables_it.WAIT, Variables_it.SING_UP);
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.PASS, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.MAIL, params[2]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.DEPT, params[3]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.REGISTRATION);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                return Variables_it.INVALID_IP;
            }

            try {
                BufferedReader reader = new BufferedReader
                        (new InputStreamReader(is, Variables_it.ISO), 8);
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
                code = (json_data.getInt(Variables_it.CODE));

                if (code == 1) {
                    return Variables_it.SIGN_UP_OK;
                } else {
                    return Variables_it.ERROR;
                }
            } catch (Exception e) {
                return Variables_it.JSON_FAILURE;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            caricamento.dismiss();
            Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
            if (code == 1) {
                pref = SPEditor.init(RegistrationActivity.this.getApplicationContext());
                SPEditor.setUser(pref, NewMail);
                SPEditor.setPass(pref, NewPass1);
                RegistrationActivity.this.finish();
            }
        }
    }

}
