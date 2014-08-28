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

public class RegistrationActivity extends Activity {

    private Button btSignUp;
    private EditText etNewUser;
    private EditText etNewPass1;
    private EditText etNewPass2;
    private EditText etNewMail;
    private EditText etNewMail2;
    private EditText etNewDept;

    private static String NewUser = null;
    private static String NewPass1 = null;
    private static String NewPass2 = null;
    private static String NewMail = null;
    private static String NewMail2 = null;
    private static String NewDept = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        btSignUp = (Button) findViewById(R.id.btSignUp);
        etNewUser = (EditText) findViewById(R.id.etNewUser);
        etNewPass1 = (EditText) findViewById(R.id.etNewPass);
        etNewPass2 = (EditText) findViewById(R.id.etNewPass2);
        etNewMail = (EditText) findViewById(R.id.etNewMail);
        etNewMail2 = (EditText) findViewById(R.id.etNewMail2);
        etNewDept = (EditText) findViewById(R.id.etNewDept);

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NewUser = etNewUser.getText().toString();
                NewPass1 = etNewPass1.getText().toString();
                NewPass2 = etNewPass2.getText().toString();
                NewMail = etNewMail.getText().toString();
                NewMail2 = etNewMail2.getText().toString();
                NewDept = etNewDept.getText().toString();

                if (!checkLoginData(NewPass1, NewPass2, NewMail, NewMail2)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        manageRegistration(NewUser, NewPass1, NewMail, NewDept);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void manageRegistration(String name, String pass, String mail, String dept) throws ExecutionException, InterruptedException {
        pass = computeSHAHash.sha1(pass);
        new Connection(this, true, Variables_it.SING_UP, Variables_it.SIGN_UP_OK, Variables_it.REGIS)
                .execute(Variables_it.REGISTRATION, Variables_it.NAME, name, Variables_it.PASS, pass, Variables_it.MAIL, mail, Variables_it.DEPT, dept);
    }

    private boolean checkLoginData(String pass1, String pass2, String mail1, String mail2) {
        if (pass1.equals(pass2) && mail1.equals(mail2)) {
            int l = pass1.length();
            return l >= 8;
        }
        return false;
    }

    public static String getmail() {
        return NewMail;
    }

    public static String getpass() {
        return NewPass1;
    }
}
