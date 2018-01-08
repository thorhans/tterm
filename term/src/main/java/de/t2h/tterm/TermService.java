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

import android.app.Service;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.app.Notification;
import android.app.PendingIntent;

import de.t2h.tterm.emulatorview.TermSession;

//T-{ ------------------------------------------------------------
//T- import de.t2h.tterm.compat.ServiceForegroundCompat;
//T-} ------------------------------------------------------------

import de.t2h.tterm.libtermexec.v1.*;
import de.t2h.tterm.util.SessionList;
import de.t2h.tterm.util.TermSettings;

import java.util.UUID;

public class TermService extends Service
    implements TermSession.FinishCallback
{
    // ************************************************************
    // Constants
    // ************************************************************

    private static final int RUNNING_NOTIFICATION = 1;

    // ************************************************************
    // Attributes
    // ************************************************************

    private SessionList mTermSessions;
    public SessionList getSessions () {
        return mTermSessions;
    }

    public class TermServiceBinder extends Binder {
        TermService getService () {
            Log.i("TTerm.TermService", "Activity binding to service");
            return TermService.this;
        }
    }
    private final IBinder mTSBinder = new TermServiceBinder();

    // ************************************************************
    // Methods
    // ************************************************************

    @Override
    public void onStart (Intent intent, int flags) {}

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public IBinder onBind (Intent intent) {
        if(TermExec.SERVICE_ACTION_V1.equals(intent.getAction())) {
            Log.i("TTerm.TermService", "Outside process called onBind()");

            return new RBinder();
        } else {
            Log.i("TTerm.TermService", "Activity called onBind()");

            return mTSBinder;
        }
    }

    @Override
    public void onCreate () {
        // Should really belong to the Application class, but we don't use one...
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        // The default is `app_HOME´, I don't yet know where `app_´ comes from.
        String defaultValue = getDir("HOME", MODE_PRIVATE).getAbsolutePath();
        String homePath = prefs.getString("home_path", defaultValue);
        editor.putString("home_path", homePath);
        editor.commit();

        mTermSessions = new SessionList();

        // Put the service in the foreground.

        Intent notifyIntent = new Intent(this, Term.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        Notification notification = builder
          .setSmallIcon(R.drawable.ic_stat_service_notification_icon)
          .setTicker(getText(R.string.service_notify_text))
          .setWhen(System.currentTimeMillis())
          .setOngoing(true)
          .setContentIntent(pendingIntent)
          .setContentTitle(getText(R.string.application_terminal))
          .setContentText(getText(R.string.service_notify_text))
          .build();

        startForeground(RUNNING_NOTIFICATION, notification);

        Log.d(TermDebug.LOG_TAG, "TermService started");
        return;
    }

    @Override
    public void onDestroy () {
        stopForeground(true);

        for(TermSession session : mTermSessions) {
            //Don't automatically remove from list of sessions -- we clear the list below anyway and we could
            // trigger ConcurrentModificationException if we do.
            session.setFinishCallback(null);
            session.finish();
        }
        mTermSessions.clear();
        return;
    }

    public void onSessionFinish (TermSession session) {
        mTermSessions.remove(session);
    }

    // ************************************************************
    // Inner class
    // ************************************************************

    private final class RBinder extends ITerminal.Stub {
        @Override
        public IntentSender startSession (
            final ParcelFileDescriptor pseudoTerminalMultiplexerFd, final ResultReceiver callback
        ) {
            final String sessionHandle = UUID.randomUUID().toString();

            // Distinct Intent Uri and PendingIntent requestCode must be sufficient to avoid collisions.
            final Intent switchIntent = new Intent(RemoteInterface.PRIVACT_OPEN_NEW_WINDOW)
                .setData(Uri.parse(sessionHandle))
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(RemoteInterface.PRIVEXTRA_TARGET_WINDOW, sessionHandle);

            final PendingIntent result = PendingIntent.getActivity(getApplicationContext(),
                sessionHandle.hashCode(), switchIntent, 0);

            final PackageManager pm = getPackageManager();
            final String[] pkgs = pm.getPackagesForUid(getCallingUid());
            if(pkgs == null || pkgs.length == 0)
                return null;

            for(String packageName : pkgs) {
                try {
                    final PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);

                    final ApplicationInfo appInfo = pkgInfo.applicationInfo;

                    if(appInfo == null) continue;

                    final CharSequence label = pm.getApplicationLabel(appInfo);

                    if(! TextUtils.isEmpty(label)) {
                        final String niceName = label.toString();

                        new Handler(Looper.getMainLooper()).post(() -> {
                            GenericTermSession session = null;
                            try {
                                final TermSettings settings = new TermSettings(getResources(),
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

                                session = new BoundSession(pseudoTerminalMultiplexerFd, settings, niceName);

                                mTermSessions.add(session);

                                session.setHandle(sessionHandle);
                                session.setFinishCallback(new RBinderCleanupCallback(result, callback));
                                session.setTitle("");

                                session.initializeEmulator(80, 24);
                            } catch(Exception whatWentWrong) {
                                Log.e("TTerm.TermService", "Failed to bootstrap AIDL session: "
                                        + whatWentWrong.getMessage());

                                if(session != null) session.finish();
                            }
                        });

                        return result.getIntentSender();
                    }
                } catch(PackageManager.NameNotFoundException ignore) {}
            }

            return null;
        }
    }

    // ************************************************************
    // Inner class
    // ************************************************************

    private final class RBinderCleanupCallback implements TermSession.FinishCallback {
        private final PendingIntent result;
        private final ResultReceiver callback;

        public RBinderCleanupCallback (PendingIntent result, ResultReceiver callback) {
            this.result = result;
            this.callback = callback;
        }

        @Override
        public void onSessionFinish (TermSession session) {
            result.cancel();

            callback.send(0, new Bundle());

            mTermSessions.remove(session);
        }
    }
}
