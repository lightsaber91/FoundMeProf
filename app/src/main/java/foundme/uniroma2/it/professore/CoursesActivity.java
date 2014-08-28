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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

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
                if (checkData(room, hours)) {
                    try {
                        manageCourse(cid, pid, room, hours);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        btSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(CoursesActivity.this, SendMessageActivity.class);
                i.putExtra(Variables_it.COURSE, cid);
                i.putExtra(Variables_it.NAME, pid);
                startActivity(i);
            }
        });
    }

    void manageCourse(String cid, String pid, String room, String hours) throws ExecutionException, InterruptedException {
        new Connection(this, true, Variables_it.LOGGING_IN, Variables_it.SEND_NOTIF_OK, "")
                .execute(Variables_it.NOTIFY, Variables_it.COURSE, cid, Variables_it.NAME, pid, Variables_it.ROOM, room, Variables_it.TIME, hours, Variables_it.FLAG, "0");
    }

    boolean checkData(String room, String hours) {
        return !(room == null || hours == null || hours.isEmpty() || room.isEmpty());
    }

}