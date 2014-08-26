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

/**
 * Created by simone on 02/06/2014.
 */
public class ChangePswActivity extends Activity {

    private SharedPreferences pref;

    private EditText etMail;
    private EditText etOldPsw;
    private EditText etNewPsw0;
    private EditText etNewPsw1;
    private Button btConfirm;
    private String mail = null;
    private String oldPass = null;
    private String newPass0 = null;
    private String newPass1 = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);

        etMail = (EditText) findViewById(R.id.etChangeMail);
        etOldPsw = (EditText) findViewById(R.id.etOldPass);
        etNewPsw0 = (EditText) findViewById(R.id.etNewPass0);
        etNewPsw1 = (EditText) findViewById(R.id.etNewPass1);
        btConfirm = (Button) findViewById(R.id.btChange);

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mail = etMail.getText().toString();
                oldPass = etOldPsw.getText().toString();
                newPass0 = etNewPsw0.getText().toString();
                newPass1 = etNewPsw1.getText().toString();

                if (!checkData(mail, oldPass, newPass0, newPass1)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    newPass0 = computeSHAHash.sha1(newPass0);
                    oldPass = computeSHAHash.sha1(oldPass);
                    new Change().execute(mail, oldPass, newPass0);
                }
            }
        });
    }

    private boolean checkData(String mail, String old, String new1, String new2) {
        return !(mail == null || old == null || new1 == null || new2 == null || mail.isEmpty() || old.isEmpty() || new1.isEmpty() || new2.isEmpty() || !new1.equals(new2) || old.equals(new1) || new1.length() < 8);
    }

    private class Change extends AsyncTask<String, Void, String> {

        private String result = null;
        private String line = null;
        private InputStream is = null;
        private int code;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(ChangePswActivity.this, Variables_it.WAIT, Variables_it.MODIFYING);
        }


        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.MAIL, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.OLD_PASS, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NEW_PASS, params[2]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.CHANGE_PASSWORD);
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
                    return Variables_it.PASS_CHANGED;
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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            if (code == 1) {
                pref = SPEditor.init(ChangePswActivity.this.getApplicationContext());
                SPEditor.setPass(pref, newPass0);
                finish();
            }
        }
    }
}
