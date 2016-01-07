package com.sqiwy.controllerexample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

public final class SystemControllerHelper {
    private static final String ACTION_CLEAR_APP_DATA = "com.sqiwy.controller.action.CLEAR_APP_DATA";
    private static final String ACTION_INSTALL_PACKAGE = "com.sqiwy.controller.action.INSTALL_PACKAGE";
    private static final String ACTION_SET_SYSTEM_UI_MODE = "com.sqiwy.controller.action.SET_SYSTEM_UI_MODE";
    private static final String ACTION_ENABLE_INSTALL_APPS = "com.sqiwy.controller.action.ENABLE_INSTALL_APPS";
    
    private static final String EXTRA_PACKAGE_NAMES = "com.sqiwy.controller.extra.PACKAGE_NAMES";
    private static final String EXTRA_PACKAGE_URI = "com.sqiwy.controller.extra.PACKAGE_URI";
    private static final String EXTRA_LAUNCH_APP = "com.sqiwy.controller.extra.LAUNCH_APP";
    private static final String EXTRA_SYSTEM_UI_MODE = "com.sqiwy.controller.extra.SYSTEM_UI_MODE";
    private static final String EXTRA_IS_APP_INSTALLATION_ENABLED = "com.sqiwy.controller.extra.IS_APP_INSTALLATION_ENABLED";

    public static final int SYSTEM_UI_MODE_DISABLE_ALL = 0;
    public static final int SYSTEM_UI_MODE_ENABLE_ALL = 1;
    public static final int SYSTEM_UI_MODE_ENABLE_NAVIGATION = 2;

    private SystemControllerHelper() {
    }

    public static void clearAppData(Context context, List<String> packageNames) {
        clearAppData(context, packageNames.toArray(new String[packageNames.size()]));
    }

    public static void clearAppData(Context context, String[] packageNames) {
        Intent intent = new Intent(ACTION_CLEAR_APP_DATA);
        intent.putExtra(EXTRA_PACKAGE_NAMES, packageNames);
        context.startService(intent);
    }

    public static void installPackage(Context context, Uri packageUri) {
        installPackage(context, packageUri, true);
    }

    public static void installPackage(Context context, Uri packageUri, boolean launchApp) {
        Intent intent = new Intent(ACTION_INSTALL_PACKAGE);
        intent.putExtra(EXTRA_PACKAGE_URI, packageUri);
        intent.putExtra(EXTRA_LAUNCH_APP, launchApp);
        context.startService(intent);
    }

    public static void setSystemUiMode(Context context, int mode) {
        Intent intent = new Intent(ACTION_SET_SYSTEM_UI_MODE);
        intent.putExtra(EXTRA_SYSTEM_UI_MODE, mode);
        context.startService(intent);
    }
    
    public static void enableInstallApps(Context context, boolean enable) {
        Intent intent = new Intent(ACTION_ENABLE_INSTALL_APPS);
        intent.putExtra(EXTRA_IS_APP_INSTALLATION_ENABLED, enable);
        context.startService(intent);
    }
}
