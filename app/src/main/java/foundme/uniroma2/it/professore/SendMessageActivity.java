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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class SendMessageActivity extends Activity {

    private EditText etMessage;
    private Button btInvia;
    private String messaggio;
    private String cid;
    private String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        etMessage = (EditText) findViewById(R.id.etmessaggio);
        btInvia = (Button) findViewById(R.id.btninv);

        Bundle passed = getIntent().getExtras();
        cid = passed.getString(Variables_it.COURSE);
        pid = passed.getString(Variables_it.NAME);


        btInvia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                messaggio = etMessage.getText().toString();
                if(checkMessage(messaggio))
                    new sendmsg().execute(cid, pid, messaggio);
                else
                    Toast.makeText(SendMessageActivity.this, Variables_it.FILL_FIELD, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private boolean checkMessage(String msg) {
        return !(msg == null || msg.isEmpty());
    }

    public class sendmsg extends AsyncTask<String, Void, String> {
        private String result0 = null;
        private String result1 = null;
        private String line0 = null;
        private String line1 = null;
        private InputStream is0 = null;
        private InputStream is1 = null;
        private ProgressDialog caricamento;
        private int code0;
        private int code1;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(SendMessageActivity.this, Variables_it.WAIT, Variables_it.SENDING);
        }

        @Override
        protected String doInBackground(String... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.COURSE, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.FLAG, "1"));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.MSG, params[2]));

            try {
                HttpClient httpclient0 = new DefaultHttpClient();
                HttpPost httppost0 = new HttpPost(Variables_it.NOTIFY);
                httppost0.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response0 = httpclient0.execute(httppost0);
                HttpEntity entity0 = response0.getEntity();
                is0 = entity0.getContent();

                HttpClient httpclient1 = new DefaultHttpClient();
                HttpPost httppost1 = new HttpPost(Variables_it.SEND_MSG);
                httppost1.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response1 = httpclient1.execute(httppost1);
                HttpEntity entity1 = response1.getEntity();
                is1 = entity1.getContent();

            } catch (Exception e) {
                return Variables_it.INVALID_IP;
            }

            try {
                BufferedReader reader0 = new BufferedReader(new InputStreamReader(is0, Variables_it.ISO), 8);
                StringBuilder sb0 = new StringBuilder();
                while ((line0 = reader0.readLine()) != null) {
                    sb0.append(line0 + "\n");
                }
                is0.close();
                result0 = sb0.toString();
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(is1, Variables_it.ISO), 8);
                StringBuilder sb1 = new StringBuilder();
                while ((line1 = reader1.readLine()) != null) {
                    sb1.append(line1 + "\n");
                }
                is1.close();
                result1 = sb1.toString();
            } catch (Exception e) {
                return Variables_it.FAIL_CONNECTION;
            }

            try {
                JSONObject json_data0 = new JSONObject(result0.substring(result0.indexOf("{"), result0.lastIndexOf("}") + 1));
                JSONObject json_data1 = new JSONObject(result1.substring(result1.indexOf("{"), result1.lastIndexOf("}") + 1));
                code0 = (json_data0.getInt(Variables_it.CODE));
                code1 = (json_data1.getInt(Variables_it.CODE));

                if (code0 == 1 && code1 == 0) {
                    return Variables_it.SEND_MSG_OK;
                } else {
                    return Variables_it.ERROR;
                }
            } catch (JSONException e) {
                return Variables_it.JSON_FAILURE;
            }
        }

        protected void onPostExecute(String result) {
            caricamento.dismiss();
            Toast.makeText(SendMessageActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }
}
