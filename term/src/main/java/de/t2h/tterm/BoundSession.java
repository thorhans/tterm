package de.t2h.tterm;

import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import de.t2h.tterm.util.TermSettings;

class BoundSession extends GenericTermSession {
    // ************************************************************
    // Attributes
    // ************************************************************

    private final String issuerTitle;
    @Override
    public String getTitle () {
        final String extraTitle = super.getTitle();

        return TextUtils.isEmpty(extraTitle)
           ? issuerTitle
           : issuerTitle + " — " + extraTitle;
    }

    private boolean fullyInitialized;
    @Override
    boolean isFailFast () {
        return ! fullyInitialized;
    }

    // ************************************************************
    // Methods
    // ************************************************************

    BoundSession (ParcelFileDescriptor ptmxFd, TermSettings settings, String issuerTitle) {
        super(ptmxFd, settings, true);

        this.issuerTitle = issuerTitle;

        setTermIn(new ParcelFileDescriptor.AutoCloseInputStream(ptmxFd));
        setTermOut(new ParcelFileDescriptor.AutoCloseOutputStream(ptmxFd));
    }

    @Override
    public void initializeEmulator (int columns, int rows) {
        super.initializeEmulator(columns, rows);

        fullyInitialized = true;
    }
}
