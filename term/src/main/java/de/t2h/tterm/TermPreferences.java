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

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import de.t2h.tterm.compat.AndroidCompat;

public class TermPreferences extends PreferenceActivity {
    // ************************************************************
    // Constants
    // ************************************************************

    private static final String ACTIONBAR_KEY = "actionbar";
    private static final String CATEGORY_SCREEN_KEY = "screen";

    // ************************************************************
    // Methods
    // ************************************************************

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);

        // Display up indicator on action bar home button.
        if(AndroidCompat.V11ToV20) {
            ActionBar bar = getActionBar();
            if(bar != null) {
                bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch(item.getItemId()) {
        case android.R.id.home:
            // Action bar home button selected
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
