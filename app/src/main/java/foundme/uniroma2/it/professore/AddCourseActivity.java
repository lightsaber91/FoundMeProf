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
import java.util.concurrent.ExecutionException;


public class AddCourseActivity extends Activity {

    private Button btSubmit;
    private EditText etTitle;
    private EditText etCfu;
    private EditText etName1;
    private EditText etName2;
    private EditText etName3;

    private String name = null;
    private String name1 = null;
    private String name2 = null;
    private String name3 = null;
    private String title = null;
    private String cfu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        btSubmit = (Button) findViewById(R.id.btSubmit);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etCfu = (EditText) findViewById(R.id.etCfu);
        etName1 = (EditText) findViewById(R.id.etName1);
        etName2 = (EditText) findViewById(R.id.etName2);
        etName3 = (EditText) findViewById(R.id.etName3);

        Bundle passedName = getIntent().getExtras();
        name = passedName.getString(Variables_it.NAME);
        etName1.setText(name, TextView.BufferType.EDITABLE);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                name1 = etName1.getText().toString();
                name2 = etName2.getText().toString();
                name3 = etName3.getText().toString();
                cfu = etCfu.getText().toString();
                title = etTitle.getText().toString();

                if (!checkLoginData(name1, title, cfu)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        manageCourse(name1, name2, name3, title, cfu);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void manageCourse(String name1, String name2, String name3, String title, String cfu) throws ExecutionException, InterruptedException {
        new AddCourse().execute(name1, name2, name3, title, cfu);
    }

    private boolean checkLoginData(String name1, String title, String cfu) {
        return !(name1 == null || name1.isEmpty() || name1.equalsIgnoreCase("") || title == null || title.isEmpty() || title.equalsIgnoreCase("") || cfu == null || cfu.isEmpty() || cfu.equalsIgnoreCase(""));
    }

    public class AddCourse extends AsyncTask<String, Void, String> {

        private InputStream is = null;
        private String result = null;
        private String line = null;
        private int code;
        private ProgressDialog caricamento;

        @Override
        protected void onPreExecute() {
            caricamento = ProgressDialog.show(AddCourseActivity.this, Variables_it.WAIT, Variables_it.ADDING);
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_1, params[0]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_2, params[1]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.NAME_3, params[2]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.COURSE, params[3]));
            nameValuePairs.add(new BasicNameValuePair(Variables_it.CFU, params[4]));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Variables_it.ADD_COURSE);
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
                    return Variables_it.ADD_COURSE_OK;
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
            Toast.makeText(AddCourseActivity.this, result, Toast.LENGTH_LONG).show();
            if (code == 1) {
                AddCourseActivity.this.finish();
            }
        }
    }
}
