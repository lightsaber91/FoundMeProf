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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;


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
                if (checkMessage(messaggio)) {
                    try {
                        manageMsg(cid, pid, messaggio);
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

    void manageMsg(String cid, String pid, String msg) throws ExecutionException, InterruptedException {
        new Connection(this, false, Variables_it.SENDING, Variables_it.SEND_MSG_OK, "")
                .execute(Variables_it.NOTIFY, Variables_it.COURSE, cid, Variables_it.NAME, pid, Variables_it.MSG, msg, Variables_it.FLAG, "1");
        new Connection(this, true, Variables_it.SENDING, Variables_it.SEND_MSG_OK, "")
                .execute(Variables_it.SEND_MSG, Variables_it.COURSE, cid, Variables_it.NAME, pid, Variables_it.MSG, msg, Variables_it.FLAG, "1");
    }

    private boolean checkMessage(String msg) {
        return !(msg == null || msg.isEmpty());
    }
}
