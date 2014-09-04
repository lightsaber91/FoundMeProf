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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.widget.ArrayAdapter.*;


public class SendMessageActivity extends Activity {

    private static EditText etMessage;
    private static EditText etTitle;
    private static Button btInvia;
    private static String messaggio;
    private static String titolo;
    private static String cid;
    private static String pid;
    private static String priority;
    private static Spinner spPriority;
    private static Context context;
    private static ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        context = this;

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        etMessage = (EditText) findViewById(R.id.etmessaggio);
        etTitle = (EditText) findViewById(R.id.etmsgTitle);
        btInvia = (Button) findViewById(R.id.btninv);
        spPriority = (Spinner) findViewById(R.id.spPriority);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        final ArrayAdapter<CharSequence> adapter = createFromResource(this,
                R.array.Priority, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            protected Adapter initializedAdapter=null;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                if(initializedAdapter !=parentView.getAdapter() ) {
                    initializedAdapter = parentView.getAdapter();
                    return;
                }

                priority = parentView.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //
            }
        });
        if (priority == null)
            priority = "Normale";

        Bundle passed = getIntent().getExtras();
        cid = passed.getString(Variables_it.COURSE);
        pid = passed.getString(Variables_it.NAME);


        btInvia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                messaggio = etMessage.getText().toString();
                titolo = etTitle.getText().toString();
                if (checkMessage(messaggio, titolo, priority)) {
                    try {
                        manageMsg(cid, pid, messaggio, titolo);
                        //finish();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(SendMessageActivity.this, Variables_it.FILL_FIELD, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etMessage.setText(result.get(0));
                }
                break;
            }

        }
    }


    private void manageMsg(String cid, String pid, String msg, String title) throws ExecutionException, InterruptedException {
        new Connection(context, false, Variables_it.SENDING, Variables_it.SEND_MSG_OK, "")
                .execute(Variables_it.NOTIFY, Variables_it.COURSE, cid, Variables_it.NAME, pid, Variables_it.MSG, msg, Variables_it.FLAG, "1");
        new Connection(context, true, Variables_it.SENDING, Variables_it.SEND_MSG_OK, "")
                .execute(Variables_it.SEND_MSG, Variables_it.COURSE, cid, Variables_it.NAME, pid, Variables_it.MSG, msg, Variables_it.FLAG, "1", Variables_it.TITLE,title, Variables_it.PRIORITY, priority);
    }

    private boolean checkMessage(String msg, String title, String priority) {
        return !(msg == null || msg.isEmpty() || title == null || title.isEmpty() || priority == null || priority.isEmpty());
    }
}
