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
        new Connection(this, true, Variables_it.ADDING, Variables_it.ADD_COURSE_OK, Variables_it.FINISH)
                .execute(Variables_it.ADD_COURSE, Variables_it.NAME_1, name1, Variables_it.NAME_2, name2, Variables_it.NAME_3, name3, Variables_it.COURSE, title, Variables_it.CFU, cfu);
    }

    private boolean checkLoginData(String name1, String title, String cfu) {
        return !(name1 == null || name1.isEmpty() || name1.equalsIgnoreCase("") || title == null || title.isEmpty() || title.equalsIgnoreCase("") || cfu == null || cfu.isEmpty() || cfu.equalsIgnoreCase(""));
    }
}
