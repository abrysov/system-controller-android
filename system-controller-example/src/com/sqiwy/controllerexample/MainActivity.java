package com.sqiwy.controllerexample;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_clear_app_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.clearAppData(MainActivity.this,
                        new String[]{"com.android.browser", "com.android.calculator2"});
            }
        });
        findViewById(R.id.btn_install_package).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.installPackage(MainActivity.this,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/menu.apk")));
            }
        });
        findViewById(R.id.btn_system_ui_disable_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.setSystemUiMode(MainActivity.this,
                        SystemControllerHelper.SYSTEM_UI_MODE_DISABLE_ALL);
            }
        });
        findViewById(R.id.btn_system_ui_enable_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.setSystemUiMode(MainActivity.this,
                        SystemControllerHelper.SYSTEM_UI_MODE_ENABLE_ALL);
            }
        });
        findViewById(R.id.btn_system_ui_enable_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.setSystemUiMode(MainActivity.this,
                        SystemControllerHelper.SYSTEM_UI_MODE_ENABLE_NAVIGATION);
            }
        });
        findViewById(R.id.btn_app_installation_enable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.enableInstallApps(MainActivity.this, true);
            }
        });
        findViewById(R.id.btn_app_installation_disable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemControllerHelper.enableInstallApps(MainActivity.this, false);
            }
        });
    }
}
