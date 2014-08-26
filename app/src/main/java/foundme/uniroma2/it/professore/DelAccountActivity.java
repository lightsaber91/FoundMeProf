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
public class DelAccountActivity extends Activity {

    private EditText etMail;
    private EditText etPsw;
    private Button btConfirm;
    private String mail = null;
    private String pass = null;
    private SharedPreferences pref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_del_account);

        etMail = (EditText) findViewById(R.id.etMail);
        etPsw = (EditText) findViewById(R.id.etPsw);
        btConfirm = (Button) findViewById(R.id.btConfirm);

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mail = etMail.getText().toString();
                pass = etPsw.getText().toString();

                if (!checkData(mail, pass)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    pass = computeSHAHash.sha1(pass);
                    new Delete().execute(mail, pass);
                }
            }
        });

    }

    private boolean checkData(String mail, String pass) {
        return !(mail == null || pass == null || mail.isEmpty() || pass.isEmpty() || pass.length() < 8);
    }

    private class Delete extends AsyncTask<String, Void, String> {

        private String result = null;
        private String line = null;
        private InputStream is = null;
        private int code;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(DelAccountActivity.this, Variables_it.WAIT, Variables_it.DELETION);
        }


        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.MAIL, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.PASS, params[1]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.DEL_ACCOUNT);
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
                    return Variables_it.DEL_ACCOUNT_OK;
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
                pref = SPEditor.init(DelAccountActivity.this.getApplicationContext());
                SPEditor.delete(pref);
                Intent intent = new Intent(DelAccountActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

}
