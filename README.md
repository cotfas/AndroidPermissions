# Permissions
Android Permission Wrapper Simplified

This Wrapper helps you to simplify the whole Android Permission Flow by showing the dialogs and snack-bar (if needed) in order for the user to understand and accept permissions easily. When a single/ or multiple permission is garanted (by the user or already accepted) the proper callback is automatically triggered.

It is based on 
- Dexter Library: https://github.com/Karumi/Dexter 
- Material Dialogs https://github.com/afollestad/material-dialogs

# Usage

In order to use import the gradle dependencies:

    /*Permission Library*/
    compile 'com.karumi:dexter:4.0.0'
    /*Material Dialogs*/
    compile 'com.afollestad.material-dialogs:core:0.9.4.2'
    
And add PermissionUtils.java to your project


Example for requesting one permission:

    public void writeStorage(View v) {
        PermissionUtils.checkPermission(this, null, getString(R.string.textWriteStoragePermissionNeeded), new PermissionUtils.PermissionListenerCallback() {
            @Override
            public void permissionGranted() {
                Toast.makeText(MainActivity.this, "Approved", Toast.LENGTH_LONG).show();
                // Permission passed - add your logic here
            }

            @Override
            public void permissionDenied() {
                Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_LONG).show();
                // Permission denied - add your logic here if needed
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    
Example for requesting multiple permissions:


    public void readSmsContactsSnackBar(View v) {
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        PermissionUtils.checkPermission(this, rootView, getString(R.string.textAllPermissionNeeded), new PermissionUtils.PermissionListenerCallback() {
            @Override
            public void permissionGranted() {
                Toast.makeText(MainActivity.this, "Approved", Toast.LENGTH_LONG).show();
                // Permission passed - add your logic here
            }

            @Override
            public void permissionDenied() {
                Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_LONG).show();
                // Permission denied - add your logic here if needed
            }
        }, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS);
    }

# Note

When using PermissionUtils from Dialog, the activity rootView must be set to null and instead of showing the snack-bar will show a dialog when the user tick the "Do not show again" checkbox.

# Used in

Cash Divider Money Management App - https://play.google.com/store/apps/details?id=com.cashdivider.app

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
