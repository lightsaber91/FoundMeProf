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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CoursesActivity extends Activity {

    private Button btSendNotify;
    private TextView tvCourseName;
    private EditText etRoom;
    private EditText etTime;
    private Button btSendMsg;
    private String cid;
    private String pid;
    private String hours;
    private String room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_courses);

        Bundle passed = getIntent().getExtras();
        cid = passed.getString(Variables_it.COURSE);
        pid = passed.getString(Variables_it.NAME);
        room = passed.getString(Variables_it.ROOM);

        btSendNotify = (Button) findViewById(R.id.btnSendNotify);
        btSendMsg = (Button) findViewById(R.id.btnSendMsg);
        tvCourseName = (TextView) findViewById(R.id.tvCourseName);
        etRoom = (EditText) findViewById(R.id.etRoom);
        etTime = (EditText) findViewById(R.id.etTime);

        tvCourseName.setText(cid);
        etRoom.setText(room, TextView.BufferType.EDITABLE);

        btSendNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                room = etRoom.getText().toString();
                hours = etTime.getText().toString();
                if(checkData(room, hours))
                new sendNotification().execute(cid, pid, room, hours);
            }
        });
        btSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(CoursesActivity.this, SendMessageActivity.class);
                i.putExtra(Variables_it.COURSE,cid);
                i.putExtra(Variables_it.NAME, pid);
                startActivity(i);
            }
        });
    }

    boolean checkData(String room, String hours) {
        return !(room == null || hours == null || hours.isEmpty() || room.isEmpty());
    }

    public class sendNotification extends AsyncTask<String, Void, String> {
        private String result = null;
        private String line = null;
        private InputStream is = null;
        private ProgressDialog caricamento;
        private int code;


        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(CoursesActivity.this, Variables_it.WAIT, Variables_it.LOGGING_IN);
        }

        @Override
        protected String doInBackground(String... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.COURSE, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.ROOM, params[2]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.TIME, params[3]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.FLAG, "0"));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.NOTIFY);
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
              code = (json_data.getInt(Variables_it.CODE));
              if (code == 1) {
                  return Variables_it.SEND_NOTIF_OK;
              } else {
                  return Variables_it.ERROR;
              }
            } catch (JSONException e) {
                return Variables_it.JSON_FAILURE;
            }
        }

        protected void onPostExecute(String result) {
            caricamento.dismiss();
            Toast.makeText(CoursesActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

}