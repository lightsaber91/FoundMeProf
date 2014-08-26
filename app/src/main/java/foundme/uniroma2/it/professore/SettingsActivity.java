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
import android.widget.TextView;


public class SettingsActivity extends Activity {

    private TextView tvChangePass;
    private TextView tvDelAccount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        tvChangePass = (TextView) findViewById(R.id.tvChangePsw);
        tvDelAccount = (TextView) findViewById(R.id.tvDelAccount);

        tvChangePass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent goToChangePws = new Intent(SettingsActivity.this, ChangePswActivity.class);
                startActivity(goToChangePws);
            }
        });

        tvDelAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent goToDelAccount = new Intent(SettingsActivity.this, DelAccountActivity.class);
                startActivity(goToDelAccount);
            }
        });
    }
}