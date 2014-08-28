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
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

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
                    try {
                        manageCourse(title, newCfu, Prof1, newProf2, newProf3);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void manageCourse(String title, String newCfu, String Prof1, String newProf2, String newProf3) throws ExecutionException, InterruptedException {
        new Connection(this, true, Variables_it.MODIFYING, Variables_it.EDIT_COURSE_OK, Variables_it.FINISH)
                .execute(Variables_it.EDIT_COURSE, Variables_it.COURSE, title, Variables_it.CFU, newCfu, Variables_it.NAME_1, Prof1, Variables_it.NAME_2, newProf2, Variables_it.NAME_3, newProf3);
    }

    private boolean checkData(String c) {
        return !(c == null || c.isEmpty());
    }
}
