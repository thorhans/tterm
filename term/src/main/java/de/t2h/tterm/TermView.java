/*
 * Copyright (C) 2012 Steven Luo
 * 
 * T+ With modifications (C) 2014 Thorbjørn Hansen.
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

import android.content.Context;
import android.util.DisplayMetrics;

import de.t2h.tterm.emulatorview.ColorScheme;
import de.t2h.tterm.emulatorview.EmulatorView;
import de.t2h.tterm.emulatorview.TermSession;

import de.t2h.tterm.util.TermSettings;

public class TermView extends EmulatorView {
    public TermView(Context context, TermSession session, DisplayMetrics metrics) {
        super(context, session, metrics);
    }

    public void updatePrefs(TermSettings settings, ColorScheme scheme) {
        if (scheme == null) {
            scheme = new ColorScheme(settings.getColorScheme());
        }

        // Section "Text"

        setTextSize(settings.getFontSize());
        setColorScheme(scheme);

        // Section "Extra keys"

        ((Term) getContext()).setExtraKeys(settings.getExtraKeys());
        ((Term) getContext()).setExtraKeySize(settings.getExtraKeySize());
        // On my Nexus 7, the soft keyboard is always turned on after showing the preferences.
        // So make the extra keys visible, too.
        ((Term) getContext()).setExtraKeysShown(settings.getExtraKeysShown(), true);

        // Section "Keyboard"

        setBackKeyCharacter(settings.getBackKeyCharacter());
        setControlKeyCode(settings.getControlKeyCode());
        setFnKeyCode(settings.getFnKeyCode());
        setUseCookedIME(settings.useCookedIME());
        setAltSendsEsc(settings.getAltSendsEscFlag());

        // Section "Shell"

        setTermType(settings.getTermType());
        setMouseTracking(settings.getMouseTrackingFlag());
    }

    public void updatePrefs(TermSettings settings) {
        updatePrefs(settings, null);
    }

    @Override
    public String toString() {
        return getClass().toString() + '(' + getTermSession() + ')';
    }
}
