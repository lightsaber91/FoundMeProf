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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by simone on 27/08/2014.
 */
public class Connection extends AsyncTask<String, Void, String[]> {

    private Context context;
    private boolean enProgressDialog;
    private String ProgressDialogMessage;
    private String returnMessage;
    private String toDo;

    private InputStream is = null;
    private String result = null;
    private String line = null;
    private int code = 0;
    private ProgressDialog caricamento;

    public Connection(Context c, boolean progDialog, String s, String m, String job) {
        context = c;
        enProgressDialog = progDialog;
        ProgressDialogMessage = s;
        returnMessage = m;
        toDo = job;
    }

    @Override
    protected void onPreExecute() {
        if (enProgressDialog)
            caricamento = ProgressDialog.show(context, Variables_it.WAIT, ProgressDialogMessage);
    }

    @Override
    protected String[] doInBackground(String... params) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (int i = 1; i < params.length; i = i + 2) {
            nameValuePairs.add(new BasicNameValuePair(params[i], params[i + 1]));
        }
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            return new String[]{Variables_it.INVALID_IP};
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
            return new String[]{Variables_it.FAIL_CONNECTION};
        }
        try {
            if(toDo.equalsIgnoreCase(Variables_it.GET)){
                JSONObject json_data = new JSONObject(result);
                List<String> list = new ArrayList<String>();
                for (int i = 0; ; i++) {
                    try {
                        String n = json_data.getString(Integer.toString(i + 1));
                        list.add(n);
                    } catch (JSONException e) {
                        break;
                    }
                }
                String[] received = new String[list.size()];
                list.toArray(received);
                return received;
            }
            else if (returnMessage.equalsIgnoreCase(Variables_it.NAME)) {
                JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                return new String[]{(json_data.getString(Variables_it.NAME))};
            }
            else {
                JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                code = (json_data.getInt(Variables_it.CODE));
            }
            if (code == 1) {
                return new String[]{returnMessage, params[params.length - 1]};
            } else {

                return new String[]{Variables_it.ERROR};
            }
        } catch (Exception e) {
            if (returnMessage.equalsIgnoreCase(Variables_it.NAME)){
                return new String[]{Variables_it.ERROR};
            }
            else if(returnMessage.equalsIgnoreCase("")) {
                return new String[]{Variables_it.NO_COURSE};
            }
            return new String[]{Variables_it.JSON_FAILURE};
        }
    }

    @Override
    protected void onPostExecute(String result[]) {
        if (enProgressDialog) {
            caricamento.dismiss();
            if (!returnMessage.equalsIgnoreCase(Variables_it.NAME) || result[0].equalsIgnoreCase(Variables_it.ERROR)) {
                if(!returnMessage.equalsIgnoreCase("")) {
                    Toast.makeText(context, result[0], Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(toDo.equalsIgnoreCase(Variables_it.GET)){
            HomeActivity.populateView(result);
        }
        else if(returnMessage.equalsIgnoreCase(Variables_it.SEND_MSG_OK) && toDo.equalsIgnoreCase(Variables_it.MSGS)) {
            ((Activity) context).finish();
        }
        else if (returnMessage.equalsIgnoreCase(Variables_it.NAME) && toDo.equalsIgnoreCase(Variables_it.LOG) && !result[0].equalsIgnoreCase(Variables_it.ERROR)) {
            SharedPreferences pref = SPEditor.init(context);
            SPEditor.setUser(pref, LoginActivity.getuser());
            SPEditor.setPass(pref, LoginActivity.getpass());
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra(Variables_it.TAG, LoginActivity.gettag());
            intent.putExtra(Variables_it.NAME, result[0]);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        else if (code == 1 && toDo.equalsIgnoreCase(Variables_it.FINISH)) {
            ((Activity) context).finish();
            try {
                HomeActivity.getCourse(true);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (code == 1 && toDo.equalsIgnoreCase(Variables_it.CHANGEP)) {
            SharedPreferences pref = SPEditor.init(context);
            SPEditor.setPass(pref, ChangePswActivity.getpass());
            ((Activity) context).finish();
        }
        else if (code == 1 && toDo.equalsIgnoreCase(Variables_it.REGIS)) {
            SharedPreferences pref = SPEditor.init(context);
            SPEditor.setUser(pref, RegistrationActivity.getmail());
            SPEditor.setPass(pref, RegistrationActivity.getpass());
            ((Activity) context).finish();
        }
        else if (code == 1 && toDo.equalsIgnoreCase(Variables_it.DELACC)) {
            SharedPreferences pref = SPEditor.init(context);
            SPEditor.delete(pref);
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        else if (code == 1 && toDo.equalsIgnoreCase(Variables_it.HOME)) {
            try {
                HomeActivity.getCourse(false);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
