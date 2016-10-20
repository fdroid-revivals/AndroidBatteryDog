/*******************************************************************************
 * Copyright (c) 2009 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * <p>
 * This file is part of the Android Battery Dog
 * <p>
 * The Android Battery Dog is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * <p>
 * The Android Battery Dog is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with the Android Battery Dog;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package net.sf.andbatdog.batterydog;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends Activity {
    TextView AuthorsHeader;
    TextView AuthorsText;
    TextView LicenseHeader;
    TextView LicenseText;
    TextView LicenseLicense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_info);

        // set up textviews
        AuthorsHeader = (TextView) findViewById(R.id.authorsHeader);
        AuthorsText = (TextView) findViewById(R.id.authorsText);
        LicenseHeader = (TextView) findViewById(R.id.licenseHeader);
        LicenseText = (TextView) findViewById(R.id.licenseText);
        LicenseLicense = (TextView) findViewById(R.id.licenseLicense);

        // set up text styling (bold, font, size, etc.)
        AuthorsHeader.setTypeface(Typeface.DEFAULT_BOLD);
        AuthorsHeader.setTextSize(AuthorsHeader.getTextSize() * (float) 0.5);
        LicenseHeader.setTypeface(Typeface.DEFAULT_BOLD);
        LicenseHeader.setTextSize(LicenseHeader.getTextSize() * (float) 0.5);
        LicenseLicense.setTypeface(Typeface.MONOSPACE);

        // set text
        AuthorsHeader.setText("Authors");
        AuthorsText.setText("This app was originally created on May 24th, 2009 by Ferenc Hechler " +
                "and was worked on on-and-off for the next couple months, when Gary Oberbrunner " +
                "came in and fixed many of the issues that were present in the codebase. " +
                "Then, in late 2016, Greg Willard took the app's codebase, allowed the app to " +
                "work on newer versions of Android, rewrote the UI, and added a couple features " +
                "to it in order to allow it to be once again used as a means of battery life/charging " +
                "analysis.\n\n");
        LicenseHeader.setText("License");
        LicenseText.setText("Android Battery Dog is licensed under the GPLv2 license, which you can find a copy of below.\n");
        LicenseLicense.setText("Copyright (c) 2009 Ferenc Hechler - ferenc_hechler@users.sourceforge.net\n" +
                "\n" +
                "This file is part of the Android Battery Dog\n" +
                "\n" +
                "The Android Battery Dog is free software; " +
                "you can redistribute it and/or modify it under the terms of the GNU " +
                "General Public License as published by the Free Software Foundation; " +
                "either version 2 of the License, or (at your option) any later version.\n" +
                "\n" +
                "The Android Battery Dog is distributed " +
                "in the hope that it will be useful, but WITHOUT ANY WARRANTY; without " +
                "even the implied warranty of MERCHANTABILITY or FITNESS FOR A " +
                "PARTICULAR PURPOSE.  See the GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License " +
                "along with the Android Battery Dog; " +
                "if not, write to the Free Software Foundation, Inc., " +
                "59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
    }
}
