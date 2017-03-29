package com.cashdivider.permissions;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void writeStorage(View v) {
        PermissionUtils.checkPermission(this, null, getString(R.string.textWriteStoragePermissionNeeded), new PermissionUtils.PermissionListenerCallback() {
            @Override
            public void permissionGranted() {
                Toast.makeText(MainActivity.this, "Approved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void permissionDenied() {
                Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_LONG).show();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void readSmsContactsSnackBar(View v) {
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        PermissionUtils.checkPermission(this, rootView, getString(R.string.textAllPermissionNeeded), new PermissionUtils.PermissionListenerCallback() {
            @Override
            public void permissionGranted() {
                Toast.makeText(MainActivity.this, "Approved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void permissionDenied() {
                Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_LONG).show();
            }
        }, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS);
    }
}