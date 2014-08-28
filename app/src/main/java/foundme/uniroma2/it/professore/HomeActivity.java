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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends Activity {

    private SharedPreferences pref;

    private static NfcAdapter mNfcAdapter;
    private static TextView nfctest;

    private static TextView profName;
    private static String name = null;
    private static ListView lvCourses;
    private static ImageButton imgUniroma2;
    private static String TAGRead = null;
    private static String[] courses = null;
    private static String toEdit = null;
    private static ActionMode.Callback modeCallBack;
    private static SwipeRefreshLayout swipeView;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeView.setEnabled(false);

        modeCallBack = new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        Intent i = new Intent(HomeActivity.this, EditCourseActivity.class);
                        i.putExtra(Variables_it.COURSE, toEdit);
                        i.putExtra(Variables_it.NAME, name);
                        startActivity(i);
                        toEdit = null;
                        mode.finish();    // Automatically exists the action mode, when the user selects this action
                        break;
                    case R.id.delete:
                        try {
                            manageCourse(name, toEdit);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        toEdit = null;
                        mode.finish();
                        break;
                }
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                mode = null;
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(Variables_it.OPTION);
                mode.getMenuInflater().inflate(R.menu.context_menu, menu);
                return true;
            }
        };


        Bundle passed = getIntent().getExtras();
        name = passed.getString(Variables_it.NAME);
        TAGRead = passed.getString(Variables_it.TAG);

        imgUniroma2 = (ImageButton) findViewById(R.id.ivLogo2);
        imgUniroma2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Variables_it.SITE_TV));
                startActivity(browserIntent);
            }
        });

        nfctest = (TextView) findViewById(R.id.tvNFC);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, Variables_it.NFC_UNSUPPORTED, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, Variables_it.NFC_DISABLED, Toast.LENGTH_LONG).show();
        }


        lvCourses = (ListView) findViewById(R.id.lvCourses);

        if (name != null /*&& courses == null*/) {
            try {
                getCourse(true);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        profName = (TextView) findViewById(R.id.tvUserName);
        profName.setText(name);
        nfctest.setText(TAGRead);
        handleIntent(getIntent());
    }

    private void manageCourse(String name, String title) throws ExecutionException, InterruptedException {
        new Connection(this, true, Variables_it.DELETION, Variables_it.DEL_COURSE_OK, Variables_it.HOME)
                .execute(Variables_it.DEL_COURSE, Variables_it.NAME, name, Variables_it.COURSE, title);
    }

    public static void getCourse(boolean en) throws ExecutionException, InterruptedException {
        new Connection(context, en, Variables_it.LOADING, "", Variables_it.GET)
                .execute(Variables_it.GET_COURSES, Variables_it.NAME, name);
    }

    public static void populateView(String[] result) {

        courses = new String[result.length];
        System.arraycopy(result, 0, courses, 0, result.length);
        ArrayList<String> listp = new ArrayList<String>();

        Collections.addAll(listp, courses);
        //creo l'adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listp);
        //inserisco i dati
        lvCourses.setAdapter(adapter);
        swipeView.setColorSchemeColors(0xff429874,0xffffffff,0xff429874,0xffffffff);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getCourse(false);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        swipeView.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        lvCourses.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });

        lvCourses.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                toEdit = courses[position];
                for (int j = 0; j < parent.getChildCount(); j++)
                    parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                view.setBackgroundColor(Color.LTGRAY);
                ((Activity)context).startActionMode(modeCallBack);
                return true;
            }
        });

        lvCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(context, CoursesActivity.class);
                i.putExtra(Variables_it.COURSE, courses[position]);
                i.putExtra(Variables_it.NAME, name);
                i.putExtra(Variables_it.ROOM, nfctest.getText().toString());
                context.startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuInflater Inflater = getMenuInflater();
        Inflater.inflate(R.menu.add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.logout:
                pref = SPEditor.init(HomeActivity.this.getApplicationContext());
                SPEditor.delete(pref);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intent2 = new Intent(this, SettingsActivity.class);
                startActivity(intent2);
                return true;
            case R.id.add:
                Intent intent3 = new Intent(this, AddCourseActivity.class);
                intent3.putExtra(Variables_it.NAME, name);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Questa funzione  mantiene l'activity in primo piano .
         * Scartando eventuali IllegalStateException
         *
         */

        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Chiama prima l'onPause
         * altrimenti viene generata correttamente un IllegalStateException.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * Questo metodo viene chiamato , quando associamo una Intent ad un' activity.
         * Prima di creare una nuova activity viene  richiamata la funzione onNewIntent per associarla all activity usata correntemente
         * Usiamo la funzione quando un Tag  viene allegato al dispositivo
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (Variables_it.MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In questo caso usermo la Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        //Stesso filtro del Manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(Variables_it.MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException(Variables_it.MIME_ERROR);
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF non è supportato per questo Tag.
                return null;
            }
            //qui verifico se il tag è identificato nelle vicinanze dell Nfc
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            //getRecord prende il primo posto disponibile
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(HomeActivity.this, Variables_it.MIME_ERROR, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();

            // Ottiene il testo da codificare
            String textEncoding = ((payload[0] & 128) == 0) ? Variables_it.UTF_8 : Variables_it.UTF_16;

            // Ottiene il linguaggio del codice
            int languageCodeLength = payload[0] & 0063;

            // Ottieni la stringa di testo
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (name == null || name.isEmpty() || name.equalsIgnoreCase("")) {
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                i.putExtra(Variables_it.TAG, result);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else nfctest.setText(result);
        }

    }
}