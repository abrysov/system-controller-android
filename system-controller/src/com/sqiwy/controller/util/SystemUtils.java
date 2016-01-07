package com.sqiwy.controller.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.sqiwy.controller.BuildConfig;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class SystemUtils {
    private static final String TAG = SystemUtils.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    // Copied from PackageManager.java.
    private static final int INSTALL_REPLACE_EXISTING = 2;
    private static final int INSTALL_SUCCEEDED = 1;

    // Copied from StatusBarManager.java.
    public static final int DISABLE_EXPAND = 0x00010000;
    public static final int DISABLE_NOTIFICATION_ICONS = 0x00020000;
    public static final int DISABLE_NOTIFICATION_ALERTS = 0x00040000;
    public static final int DISABLE_NOTIFICATION_TICKER = 0x00080000;
    public static final int DISABLE_SYSTEM_INFO = 0x00100000;
    public static final int DISABLE_HOME = 0x00200000;
    public static final int DISABLE_BACK = 0x00400000;
    public static final int DISABLE_CLOCK = 0x00800000;
    public static final int DISABLE_RECENT = 0x01000000;
    public static final int DISABLE_SEARCH = 0x02000000;
    public static final int DISABLE_NONE = 0x00000000;
    public static final int DISABLE_ALL = DISABLE_EXPAND | DISABLE_NOTIFICATION_ICONS
            | DISABLE_NOTIFICATION_ALERTS | DISABLE_NOTIFICATION_TICKER
            | DISABLE_SYSTEM_INFO | DISABLE_RECENT | DISABLE_HOME | DISABLE_BACK | DISABLE_CLOCK
            | DISABLE_SEARCH;

    private SystemUtils() {
    }

    public static void clearAppData(Context context, String[] packageNames) {
        if (packageNames == null || packageNames.length == 0 || !RootTools.isAccessGiven()) {
            return;
        }
        if (DEBUG) {
            Log.v(TAG, "Clear app data for " + Arrays.toString(packageNames));
        }

        // Kill the background processes associated with the specified packages
        // and clear content of "/data/data/<package_name>" except "lib" folder.
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String[] commands = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; i++) {
            String packageName = packageNames[i];
            am.killBackgroundProcesses(packageName);
            commands[i] = "find /data/data/" + packageName + "/* ! -name \"lib\" -print0 | "
                    + "xargs -0 rm -rf";
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            RootTools.getShell(true).add(new CommandCapture(0, commands) {
                @Override
                protected void commandFinished() {
                    super.commandFinished();
                    latch.countDown();
                }
            });

            // Wait until the command is completed.
            latch.await();
        } catch (Exception e) {
            Log.w(TAG, "Failed to clear app data", e);
        }

        // clear app data using package manager.
        String[] clearCommands = new String[packageNames.length];
        for (int i = 0; i < packageNames.length; i++) {
            String packageName = packageNames[i];
            clearCommands[i] = "pm clear " + packageName;
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            RootTools.getShell(true).add(new CommandCapture(0, clearCommands) {
                @Override
                protected void commandFinished() {
                    super.commandFinished();
                    latch.countDown();
                }
            });

            // Wait until the command is completed.
            latch.await();
        } catch (Exception e) {
            Log.w(TAG, "Failed to clear app data", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void installPackage(final Context context, Uri packageUri, final boolean launchApp) {
        if (packageUri == null) {
            return;
        }
        if (DEBUG) {
            Log.v(TAG, "Install package for " + packageUri);
        }
        try {
            // Call PackageManager#installPackage method via reflection.
            final CountDownLatch latch = new CountDownLatch(1);
            final PackageManager pm = context.getPackageManager();
            Method installPackageMethod = pm.getClass().getMethod("installPackage",
                    new Class[]{Uri.class, IPackageInstallObserver.class, int.class, String.class});
            installPackageMethod.invoke(pm, packageUri, new IPackageInstallObserver.Stub() {
                @Override
                public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                    if (returnCode == INSTALL_SUCCEEDED) {
                        if (launchApp) {
                            Intent intent = pm.getLaunchIntentForPackage(packageName);
                            if (intent != null) {
                                context.startActivity(intent);
                            }
                        }
                    } else {
                        Log.w(TAG, "Failed to install the package " + packageName);
                    }
                    latch.countDown();
                }
            }, INSTALL_REPLACE_EXISTING, null);

            // Wait until the package is installed.
            latch.await();
        } catch (Exception e) {
            Log.w(TAG, "Failed to install a package", e);
        }
    }

    @SuppressWarnings("MagicConstant")
    public static void disableSystemUiFeatures(Context context, int features) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method disableMethod = statusBarManager.getClass().getMethod("disable",
                    new Class[]{int.class});
            disableMethod.invoke(statusBarManager, features);

        } catch (Exception e) {
            Log.w(TAG, "Failed to disable system UI features", e);
        }
    }

    public static void setSystemBarsVisibility(boolean isVisible) {
        if (!RootTools.isAccessGiven()) {
            return;
        }
        String command = isVisible ? "am startservice --user 0 -n com.android.systemui/.SystemUIService"
                : "service call activity 42 s16 com.android.systemui";
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            RootTools.getShell(true).add(new CommandCapture(0, command) {
                @Override
                protected void commandFinished() {
                    super.commandFinished();
                    latch.countDown();
                }
            });

            // Wait until the command is completed.
            latch.await();
        } catch (Exception e) {
            Log.w(TAG, "Failed to set system bars visibility", e);
        }
    }

    public static void disableMultiWindowMode(Context context) {
        try {
            Log.v(TAG, "Disable the MULTI_WINDOW mode");
            Settings.System.putInt(context.getContentResolver(), "multi_window_mode", 0);
            Settings.System.putInt(context.getContentResolver(), "multi_window_button_show", 0);
        } catch (Throwable ex) {
            Log.e(TAG, "Cannot get permissions to disable the MULTI_WINDOW mode");
        }
    }

    /**
     * How to make chrome launch in desktop mode:
     * http://droidnerds.com/content/how-to-permanently-have-desktop-mode-on-chrome-mobile-for-android/
     * <ul>
     * <li>Creates special file for Chrome to launch in desktop mode</li>
     * <li>Gives user access to the file</li>
     * </ul>
     */
    public static void setChromeToDesktopMode(Context context) {
    	
    	if (!RootTools.isAccessGiven()) {
            return;
        }
    	
    	if (DEBUG) {
            Log.d(TAG, "Setting chrome to desktop mode");
        }
        
    	String commandCreateChromeHackLauncher =
    			"echo 'chrome --user-agent=\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1\"'" +
    			" > /data/local/chrome-command-line";
    	String commandGiveAccessToHackLauncher = "chmod 755 /data/local/chrome-command-line";
    	
    	if (DEBUG) {
	    	Log.d(TAG, "Command: " + commandCreateChromeHackLauncher);
	    	Log.d(TAG, "Command: " + commandGiveAccessToHackLauncher);
    	}
    	
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            
            // Add file and give permissions
            RootTools.getShell(true).add(new CommandCapture(0, commandCreateChromeHackLauncher, commandGiveAccessToHackLauncher) {
                @Override
                protected void commandFinished() {
                    super.commandFinished();
                    latch.countDown();
                    
                    if (DEBUG) {
                    	Log.d(TAG, "commandFinished");
                    }
                }
                
                @Override
                public void commandCompleted(int id, int exitcode) {
                	super.commandCompleted(id, exitcode);
                	if (DEBUG) {
                		Log.d(TAG, "commandCompleted " + id + " " + exitcode);
                	}
                }
                
                @Override
                public void commandOutput(int id, String line) {
                	super.commandOutput(id, line);
                	if (DEBUG) {
                		Log.d(TAG, "commandOutput " + id + " " + line);
                	}
                }
                
                @Override
                public void commandTerminated(int id, String reason) {
                	super.commandTerminated(id, reason);
                	if (DEBUG) {
                		Log.d(TAG, "commandTerminated " + id + " " + reason);
                	}
                }
            });

            // Wait until the command is completed.
            latch.await(60, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to set Chrome to desktop mode", e);
        }
        
        // We have to kill Chrome so next time user restarts it and changes we added are applied.
        killApp(context, "com.android.chrome");
    }
    
    public static void killApp(Context context, String appPackage) {
    	try {
	    	
    		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    	activityManager.killBackgroundProcesses(appPackage);
	    	
	    	if (DEBUG) {
	    		Log.d(TAG, "Successfully killed: " + appPackage);
	    	}
	    	
    	} catch (Exception e) {
    		Log.e(TAG, "Failed to kill app: " + appPackage, e);
    	}
    }
    
    public static void reboot(Context context) {
    	/*
    	 * Doesn't work:
    	 * PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    	 * pm.reboot(null);*/
    	
    	if (!RootTools.isAccessGiven()) {
            return;
        }
    	
    	if (DEBUG) {
            Log.d(TAG, "Rebooting device");
        }
        
    	String commanReboot = "reboot";
    	
    	if (DEBUG) {
	    	Log.d(TAG, "Command: " + commanReboot);
    	}
    	
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            
            // Add file and give permissions
            RootTools.getShell(true).add(new CommandCapture(0, commanReboot) {
                @Override
                protected void commandFinished() {
                    super.commandFinished();
                    latch.countDown();
                    
                    if (DEBUG) {
                    	Log.d(TAG, "commandFinished");
                    }
                }
                
                @Override
                public void commandCompleted(int id, int exitcode) {
                	super.commandCompleted(id, exitcode);
                	if (DEBUG) {
                		Log.d(TAG, "commandCompleted " + id + " " + exitcode);
                	}
                }
                
                @Override
                public void commandOutput(int id, String line) {
                	super.commandOutput(id, line);
                	if (DEBUG) {
                		Log.d(TAG, "commandOutput " + id + " " + line);
                	}
                }
                
                @Override
                public void commandTerminated(int id, String reason) {
                	super.commandTerminated(id, reason);
                	if (DEBUG) {
                		Log.d(TAG, "commandTerminated " + id + " " + reason);
                	}
                }
            });

            // Wait until the command is completed.
            latch.await(60, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to set Chrome to desktop mode", e);
        }
    }
    
    public static void enableInstallApps(Context context, boolean isAppInstallationEnabled) {
    	
    	try {
    	
        	if (DEBUG) {
                
        		Log.d(TAG, isAppInstallationEnabled ? "Enabling app installation..." : "Disabling app installation...");
            }
        	
        	if (!RootTools.isAccessGiven()) {
        		
            	if (DEBUG) {
                    
            		Log.d(TAG, "No root access given. Exit.");
                }
                return;
            }
        	
        	final String mountPoint = "/system/app/";
        	final String packageInstallerFile = "/system/app/PackageInstaller.apk";
        	final String packageInstallerBakFile = "/system/app/PackageInstaller.apk.bak";        	
        	
        	if( ((true == isAppInstallationEnabled) && (!RootTools.exists(packageInstallerFile))) ||
        		((false == isAppInstallationEnabled) && (!RootTools.exists(packageInstallerBakFile)))) {
        		
        		final CountDownLatch latch = new CountDownLatch(1);
        		final String command = isAppInstallationEnabled ? 
        				"mv " + packageInstallerBakFile + " " + packageInstallerFile :
        				"mv " + packageInstallerFile + " " + packageInstallerBakFile;
        		
            	if (DEBUG) {
            		
        	    	Log.d(TAG, "Command: " + command);
            	}
            	
            	if(false == RootTools.remount(mountPoint, "rw")) {

            		throw new Exception("Failed to mount /system/app as RW");
            	}
            	
            	// 
                RootTools.getShell(true).add(new CommandCapture(0, command) {
                	
                    @Override
                    protected void commandFinished() {
                    	
                        super.commandFinished();
                        
                        latch.countDown();
                        
                        if (DEBUG) {
                        	
                        	Log.d(TAG, "commandFinished");
                        }
                    }
                    
                    @Override
                    public void commandCompleted(int id, int exitcode) {
                    	
                    	super.commandCompleted(id, exitcode);
                    	
                    	if (DEBUG) {
                    		
                    		Log.d(TAG, "commandCompleted " + id + " " + exitcode);
                    	}
                    }
                    
                    @Override
                    public void commandOutput(int id, String line) {
                    	
                    	super.commandOutput(id, line);
                    	
                    	if (DEBUG) {
                    		
                    		Log.d(TAG, "commandOutput " + id + " " + line);
                    	}
                    }
                    
                    @Override
                    public void commandTerminated(int id, String reason) {
                    	
                    	super.commandTerminated(id, reason);
                    	
                    	if (DEBUG) {
                    		
                    		Log.d(TAG, "commandTerminated " + id + " " + reason);
                    	}
                    }
                });

                // Wait until the command is completed.
                latch.await(60, TimeUnit.SECONDS);
                
                RootTools.remount(mountPoint, "r");
        	}
        	else {
        	
            	if (DEBUG) {
            		
            		Log.d(TAG, isAppInstallationEnabled ? "App installation already enabled" : "App installation already disabled");
            	}
        	}
        	
        	if (DEBUG) {
        		
        		Log.d(TAG, "done.");
        	}
    	}
    	catch(Throwable error) {
    		
    		Log.w(TAG, isAppInstallationEnabled ? "Failed to enable app installation" : "Failed to disable app installation", error);
    	}
    }
}
