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

/**
 * Created by simone on 17/07/2014.
 */
public class EditCourseActivity extends Activity {

    private EditText etChangeCfu;
    private EditText etProf2;
    private EditText etProf3;
    private TextView tvTitle;
    private Button btEdit;
    private String title;
    private String newCfu;
    private String Prof1;
    private String newProf2;
    private String newProf3;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        Bundle passed = getIntent().getExtras();
        Prof1 = passed.getString(Variables_it.NAME);
        title = passed.getString(Variables_it.COURSE);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        etChangeCfu = (EditText) findViewById(R.id.etChangeCfu);
        etProf2 = (EditText) findViewById(R.id.etProf2);
        etProf3 = (EditText) findViewById(R.id.etProf3);
        btEdit = (Button) findViewById(R.id.btEdit);

        tvTitle.setText(title);


        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                newCfu = etChangeCfu.getText().toString();
                newProf2 = etProf2.getText().toString();
                newProf3 = etProf3.getText().toString();
                if (!checkData(newCfu)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    new Change().execute(title, newCfu, Prof1, newProf2, newProf3);
                }
            }
        });
    }

    private boolean checkData(String c) {
        return !(c == null || c.isEmpty());
    }

    private class Change extends AsyncTask<String, Void, String> {

        private String result = null;
        private String line = null;
        private InputStream is = null;
        private int code;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(EditCourseActivity.this, Variables_it.WAIT, Variables_it.MODIFYING);
        }


        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.COURSE, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.CFU, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_1, params[2]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_2, params[3]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_3, params[4]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.EDIT_COURSE);
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
                    return Variables_it.EDIT_COURSE_OK;
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
            if (code == 1) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                finish();
            } else
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
