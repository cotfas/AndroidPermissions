package com.cashdivider.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import java.util.List;

public class PermissionUtils {

    private static final boolean SHOW_RATIONALE_DIALOG_ON_FIRST = true; // Issue when using false + Dexter 4.0

    private static boolean permissionListenerCallbackExecuted = false; // Internally used
    private static boolean isPermissionRequestExecuted = false; // Internally used


    /**
     * Checking permission
     *
     *  If rootView activity is null - on rationale will show text as dialog
     *  If rootView activity is is not null - on rationale will show text as snack bar
     *  If message is null - will not show any rationale dialog/snack-bar
     *
     * @param activity - Activity context
     * @param rootView - ViewGroup rootView = (ViewGroup) view.findViewById(android.R.id.content);
     * @param message - Message of the rationale dialog/snack-bar
     * @param permissionListenerCallback - Callback for getting permission approved/denied state
     * @param permissions - Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, etc
     */
    public static void checkPermission(final Activity activity, final ViewGroup rootView, final String message, final PermissionListenerCallback permissionListenerCallback, final String... permissions) {

        final Context context = activity;

        if (permissionListenerCallback == null) {
            return;
        }
        if (context == null) {
            executeCallback(permissionListenerCallback, false);
            return;
        }
        if (permissions == null) {
            executeCallback(permissionListenerCallback, false);
            return;
        }
        if (isPermissionAlreadyApproved(activity, permissions)) {
            executeCallback(permissionListenerCallback, true);
            return;
        }
        if (permissions.length == 1) {
            /*
                Single permission check
            */
            checkSinglePermission(activity, rootView, message, permissionListenerCallback, permissions[0]);
            return;
        }

        /*
            Multiple permission check
         */
        MultiplePermissionsListener multiplePermissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    // All Permissions Granted
                    executeCallback(permissionListenerCallback, true);
                } else {
                    // Permissions denied
                    executeCallback(permissionListenerCallback, false);
                }

                /*
                    Case for permanently denied and no Snack Bar
                 */
                if (rootView == null) {
                    boolean isPermanentlyDenied = false;
                    for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {

                       if (response.isPermanentlyDenied()) {
                           isPermanentlyDenied = true;
                       }
                    }
                    if (isPermanentlyDenied) {
                        showPermissionRationale(context, message);
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                if (SHOW_RATIONALE_DIALOG_ON_FIRST) {
                    token.continuePermissionRequest();
                    return;
                }
                // Show Dialog Rationale
                showPermissionRationale(context, message, token, null);
            }
        };
        if (rootView != null && !TextUtils.isEmpty(message)) {
            // Add snack bar
            multiplePermissionsListener =
                    new CompositeMultiplePermissionsListener(multiplePermissionsListener,
                            SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(rootView,
                                    message)
                                    .withOpenSettingsButton(context.getString(R.string.textPermissionRationaleButton))
                                    .withCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onShown(Snackbar snackbar) {
                                            // Event handler for when the given SnackBar has been dismissed
                                        }
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            // Event handler for when the given SnackBar is visible
                                        }
                                    })
                                    .build());
        }

        final MultiplePermissionsListener listener = multiplePermissionsListener;
        final DialogRationaleCallback dialogRationaleCallback = new DialogRationaleCallback() {
            @Override
            public void onContinue() {
                Dexter.withActivity(activity)
                        .withPermissions(permissions)
                        .withListener(listener)
                        .check();
            }

            @Override
            public void onCancel() {
            }
        };
        if (SHOW_RATIONALE_DIALOG_ON_FIRST) {
            showPermissionRationale(context, message, null, dialogRationaleCallback);
        } else {
            dialogRationaleCallback.onContinue();
        }
    }

    /**
     * Checking single permission
     *
     * @param activity
     * @param rootView
     * @param message
     * @param permissionListenerCallback
     * @param permission
     */
    private static void checkSinglePermission(final Activity activity, final ViewGroup rootView, final String message, final PermissionListenerCallback permissionListenerCallback, final String permission) {

        final Context context = activity;

        if (permissionListenerCallback == null) {
            return;
        }
        if (context == null) {
            executeCallback(permissionListenerCallback, false);
            return;
        }
        if (permission == null) {
            executeCallback(permissionListenerCallback, false);
            return;
        }
        if (isPermissionAlreadyApproved(activity, permission)) {
            executeCallback(permissionListenerCallback, true);
            return;
        }

        /*
            Single permission check
         */

        PermissionListener singlePermissionsListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                // Permission Granted
                executeCallback(permissionListenerCallback, true);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                // Permission Denied
                executeCallback(permissionListenerCallback, false);

                /*
                    Case for permanently denied and no Snack Bar
                 */
                if (rootView == null && response.isPermanentlyDenied()) {
                    showPermissionRationale(context, message);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                if (SHOW_RATIONALE_DIALOG_ON_FIRST) {
                    token.continuePermissionRequest();
                    return;
                }
                // Show Dialog Rationale
                showPermissionRationale(context, message, token, null);
            }
        };
        if (rootView != null && !TextUtils.isEmpty(message)) {
            // Add snack bar
            singlePermissionsListener = new CompositePermissionListener(singlePermissionsListener,
                    SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                            message)
                            .withOpenSettingsButton(context.getString(R.string.textPermissionRationaleButton))
                            .withCallback(new Snackbar.Callback() {
                                @Override
                                public void onShown(Snackbar snackbar) {
                                    // Event handler for when the given SnackBar has been dismissed
                                }
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    // Event handler for when the given SnackBar is visible
                                }
                            })
                            .build());
        }


        final PermissionListener listener = singlePermissionsListener;
        final DialogRationaleCallback dialogRationaleCallback = new DialogRationaleCallback() {
            @Override
            public void onContinue() {
                Dexter.withActivity(activity)
                        .withPermission(permission)
                        .withListener(listener)
                        .check();
            }

            @Override
            public void onCancel() {
            }
        };
        if (SHOW_RATIONALE_DIALOG_ON_FIRST) {
            showPermissionRationale(context, message, null, dialogRationaleCallback);
        } else {
            dialogRationaleCallback.onContinue();
        }
    }

    /**
     * Showing dialog permission rationale
     *
     * @param context
     * @param message
     * @param token
     * @param dialogRationaleCallback
     */
    private static void showPermissionRationale(final Context context, final String message, final PermissionToken token, final DialogRationaleCallback dialogRationaleCallback) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(message)) {
            if (dialogRationaleCallback != null) {
                dialogRationaleCallback.onContinue();
            }
            if (token != null) {
                token.continuePermissionRequest();
            }
            return;
        }

        new MaterialDialog.Builder(context)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        if (dialogRationaleCallback != null) {
                            dialogRationaleCallback.onContinue();
                        }

                        if (token != null) {
                            token.continuePermissionRequest();
                        }
                        isPermissionRequestExecuted = true;

                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        if (dialogRationaleCallback != null) {
                            dialogRationaleCallback.onCancel();
                        }

                        if (token != null) {
                            token.cancelPermissionRequest();
                        }
                        isPermissionRequestExecuted = true;

                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);

                        if (dialogRationaleCallback != null) {
                            dialogRationaleCallback.onCancel();
                        }

                        if (token != null) {
                            token.cancelPermissionRequest();
                        }
                        isPermissionRequestExecuted = true;

                        dialog.dismiss();
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        isPermissionRequestExecuted = false;

                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        if (!isPermissionRequestExecuted) {
                            if (token != null) {
                                token.cancelPermissionRequest();
                            }
                        }
                    }
                })
                .content(message)
                .positiveText(context.getString(R.string.textContinue))
                .autoDismiss(false)
                .build()
                .show();
    }

    /**
     * Show dialog permission rationale
     *
     * @param context
     * @param message
     */
    private static void showPermissionRationale(final Context context, final String message) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(message)) {
            return;
        }

        new MaterialDialog.Builder(context)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        startInstalledAppDetailsActivity(context);

                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);

                        dialog.dismiss();
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                })
                .content(message)
                .positiveText(context.getString(R.string.textPermissionRationaleButton))
                .autoDismiss(false)
                .build()
                .show();
    }

    /**
     * Sending permission update
     *
     * @param permissionListenerCallback
     * @param permissionGranted
     */
    private static void executeCallback(final PermissionListenerCallback permissionListenerCallback, final boolean permissionGranted) {
        if (permissionListenerCallbackExecuted) {
            return;
        }
        permissionListenerCallbackExecuted = true;

        // Run on UI
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if (permissionListenerCallback != null) {
                    if (permissionGranted) {
                        permissionListenerCallback.permissionGranted();
                    } else {
                        permissionListenerCallback.permissionDenied();
                    }
                }
                permissionListenerCallbackExecuted = false;
            }
        });
    }

    /**
     * Checking if the permission(permissions) are already approved
     *
     * @param context
     * @param requestedPermissions
     * @return
     */
    public static boolean isPermissionAlreadyApproved(Context context, String... requestedPermissions) {

        if (!isLollipopAndUp()) {
            return true;
        }

        if (context == null) {
            return false;
        }
        if (requestedPermissions == null) {
            return false;
        }

        boolean permissionGranted = false;
        for (String permission : requestedPermissions) {
            if (TextUtils.isEmpty(permission)) {
                continue;
            }

            int res = ContextCompat.checkSelfPermission(context, permission);
            permissionGranted = (res == PackageManager.PERMISSION_GRANTED);

            if (!permissionGranted) {
                return false;
            }
        }
        return permissionGranted;
    }

    /**
     * Permission callback listener
     */
    public interface PermissionListenerCallback {
        void permissionGranted();
        void permissionDenied();
    }

    private interface DialogRationaleCallback {
        void onContinue();
        void onCancel();
    }

    /**
     * Detecting if is Lollipop and UP
     * @return
     */
    private static boolean isLollipopAndUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        return false;
    }

    /**
     * Starting installed application detail app settings
     *
     * @param context
     */
    private static void startInstalledAppDetailsActivity(final Context context) {
        if (context == null) {
            return;
        }
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }
}