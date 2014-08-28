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

/**
 * Created by simone on 02/06/2014.
 */
public class ChangePswActivity extends Activity {

    private static EditText etMail;
    private static EditText etOldPsw;
    private static EditText etNewPsw0;
    private static EditText etNewPsw1;
    private static Button btConfirm;
    private static String mail = null;
    private static String oldPass = null;
    private static String newPass0 = null;
    private static String newPass1 = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);

        etMail = (EditText) findViewById(R.id.etChangeMail);
        etOldPsw = (EditText) findViewById(R.id.etOldPass);
        etNewPsw0 = (EditText) findViewById(R.id.etNewPass0);
        etNewPsw1 = (EditText) findViewById(R.id.etNewPass1);
        btConfirm = (Button) findViewById(R.id.btChange);

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mail = etMail.getText().toString();
                oldPass = etOldPsw.getText().toString();
                newPass0 = etNewPsw0.getText().toString();
                newPass1 = etNewPsw1.getText().toString();

                if (!checkData(mail, oldPass, newPass0, newPass1)) {
                    Toast.makeText(getApplicationContext(), Variables_it.FILL_FIELD, Toast.LENGTH_LONG).show();
                } else {
                    newPass0 = computeSHAHash.sha1(newPass0);
                    oldPass = computeSHAHash.sha1(oldPass);
                    try {
                        managePass(mail, oldPass, newPass0);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void managePass(String mail, String oldPass, String newPass0) throws ExecutionException, InterruptedException {
        new Connection(this, true, Variables_it.MODIFYING, Variables_it.PASS_CHANGED, Variables_it.CHANGEP).execute(Variables_it.CHANGE_PASSWORD, Variables_it.MAIL, mail, Variables_it.OLD_PASS, oldPass, Variables_it.NEW_PASS, newPass0);
    }

    private boolean checkData(String mail, String old, String new1, String new2) {
        return !(mail == null || old == null || new1 == null || new2 == null || mail.isEmpty() || old.isEmpty() || new1.isEmpty() || new2.isEmpty() || !new1.equals(new2) || old.equals(new1) || new1.length() < 8);
    }

    public static String getpass() {
        return newPass0;
    }
}
