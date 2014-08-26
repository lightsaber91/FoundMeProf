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
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by simone on 25/08/2014.
 */
public class SPEditor extends Activity {

    private static final String PREF = "ProfessorPref";
    private static final String USER = "UserMail";
    private static final String PASS = "CryptedPassword";

    public static SharedPreferences init(Context ctx) {
        return ctx.getSharedPreferences(PREF, MODE_PRIVATE);
    }

    public static void delete(SharedPreferences p) {
        p.edit().clear().commit();
    }

    public static String getUser(SharedPreferences p) {
        return p.getString(USER, null);
    }

    public static String getPass(SharedPreferences p) {
        return p.getString(PASS, null);
    }

    public static void setUser(SharedPreferences p, String s) {
        p.edit().putString(USER, s).commit();
    }

    public static void setPass(SharedPreferences p, String s) {
        p.edit().putString(PASS, s).commit();
    }

}
