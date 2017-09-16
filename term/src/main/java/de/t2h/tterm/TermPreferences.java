/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.t2h.tterm;

//T!{ ------------------------------------------------------------
//T! import de.t2h.tterm.compat.ActionBarCompat;
import android.app.ActionBar;
//T!} ------------------------------------------------------------

import de.t2h.tterm.compat.AndroidCompat;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class TermPreferences extends PreferenceActivity {
    private static final String ACTIONBAR_KEY = "actionbar";
    private static final String CATEGORY_SCREEN_KEY = "screen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //T- // Remove the action bar pref on older platforms without an action bar
        //T- if (AndroidCompat.SDK < 11) {
        //T-     Preference actionBarPref = findPreference(ACTIONBAR_KEY);
        //T-      PreferenceCategory screenCategory =
        //T-             (PreferenceCategory) findPreference(CATEGORY_SCREEN_KEY);
        //T-      if ((actionBarPref != null) && (screenCategory != null)) {
        //T-          screenCategory.removePreference(actionBarPref);
        //T-      }
        //T- }

        // Display up indicator on action bar home button
        if (AndroidCompat.V11ToV20) {
            //T{ ------------------------------------------------------------
            //T! ActionBarCompat bar = ActivityCompat.getActionBar(this);
            ActionBar bar = getActionBar();
            if (bar != null) {
                //T! bar.setDisplayOptions(ActionBarCompat.DISPLAY_HOME_AS_UP, ActionBarCompat.DISPLAY_HOME_AS_UP);
                bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
            }
            //T} ------------------------------------------------------------
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        //T!{ ------------------------------------------------------------
        //T! case ActionBarCompat.ID_HOME:
        case android.R.id.home:
        //T!} ------------------------------------------------------------
            // Action bar home button selected
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
