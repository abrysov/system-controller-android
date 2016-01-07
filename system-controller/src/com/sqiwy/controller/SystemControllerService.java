package com.sqiwy.controller;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.sqiwy.controller.util.SystemUtils;

public class SystemControllerService extends IntentService {
    private static final String ACTION_CLEAR_APP_DATA = "com.sqiwy.controller.action.CLEAR_APP_DATA";
    private static final String ACTION_INSTALL_PACKAGE = "com.sqiwy.controller.action.INSTALL_PACKAGE";
    private static final String ACTION_SET_SYSTEM_UI_MODE = "com.sqiwy.controller.action.SET_SYSTEM_UI_MODE";
    private static final String ACTION_SET_CHROME_TO_DESKTOP_MODE = "com.sqiwy.controller.action.SET_CHROME_TO_DESKTOP_MODE";
    private static final String ACTION_REBOOT = "com.sqiwy.controller.action.REBOOT";
    private static final String ACTION_ENABLE_INSTALL_APPS = "com.sqiwy.controller.action.ENABLE_INSTALL_APPS";

    private static final String EXTRA_PACKAGE_NAMES = "com.sqiwy.controller.extra.PACKAGE_NAMES";
    private static final String EXTRA_PACKAGE_URI = "com.sqiwy.controller.extra.PACKAGE_URI";
    private static final String EXTRA_LAUNCH_APP = "com.sqiwy.controller.extra.LAUNCH_APP";
    private static final String EXTRA_SYSTEM_UI_MODE = "com.sqiwy.controller.extra.SYSTEM_UI_MODE";
    private static final String EXTRA_IS_APP_INSTALLATION_ENABLED = "com.sqiwy.controller.extra.IS_APP_INSTALLATION_ENABLED";

    private static final int SYSTEM_UI_MODE_DISABLE_ALL = 0;
    private static final int SYSTEM_UI_MODE_ENABLE_ALL = 1;
    private static final int SYSTEM_UI_MODE_ENABLE_NAVIGATION = 2;

    public SystemControllerService() {
        super("SystemControllerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_CLEAR_APP_DATA.equals(action)) {
            SystemUtils.clearAppData(this, intent.getStringArrayExtra(EXTRA_PACKAGE_NAMES));
        } else if (ACTION_INSTALL_PACKAGE.equals(action)) {
            SystemUtils.installPackage(this, (Uri) intent.getParcelableExtra(EXTRA_PACKAGE_URI),
                    intent.getBooleanExtra(EXTRA_LAUNCH_APP, true));
        } else if (ACTION_SET_SYSTEM_UI_MODE.equals(action)) {
            setSystemUiMode(intent.getIntExtra(EXTRA_SYSTEM_UI_MODE, SYSTEM_UI_MODE_ENABLE_ALL));
        } else if (ACTION_SET_CHROME_TO_DESKTOP_MODE.equals(action)) {
        	SystemUtils.setChromeToDesktopMode(this);
        } else if (ACTION_REBOOT.equals(action)) {
        	SystemUtils.reboot(this);
        } else if(ACTION_ENABLE_INSTALL_APPS.equals(action)) {
        	SystemUtils.enableInstallApps(this, intent.getBooleanExtra(EXTRA_IS_APP_INSTALLATION_ENABLED, true));        	
        }
    }

    private void setSystemUiMode(int mode) {
        switch (mode) {
            case SYSTEM_UI_MODE_DISABLE_ALL:
                SystemUtils.setSystemBarsVisibility(false);
                break;
            case SYSTEM_UI_MODE_ENABLE_ALL:
                SystemUtils.disableMultiWindowMode(this);
                SystemUtils.setSystemBarsVisibility(true);
                SystemUtils.disableSystemUiFeatures(this, SystemUtils.DISABLE_NONE);
                break;
            case SYSTEM_UI_MODE_ENABLE_NAVIGATION:
                SystemUtils.disableMultiWindowMode(this);
                SystemUtils.setSystemBarsVisibility(true);
                SystemUtils.disableSystemUiFeatures(this, SystemUtils.DISABLE_ALL
                        & ~(SystemUtils.DISABLE_BACK | SystemUtils.DISABLE_HOME
                        | SystemUtils.DISABLE_SYSTEM_INFO | SystemUtils.DISABLE_CLOCK));
                break;
        }
    }
}
