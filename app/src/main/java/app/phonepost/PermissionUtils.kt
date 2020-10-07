package app.phonepost


import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionUtils(
    private val activity: Activity,
    private val callback: PermissionResultCallback,
    private val requestCode: Int
) {

    private var permissionsNeeded = ArrayList<String>()

    val TAG = "PermissionsUtil"

    fun checkPermission(permissions: List<String>) {
        Log.d(TAG, "In check permissions. ${permissions.joinToString(",")}")
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions(permissions)) {
                callback.allPermissionsGranted(requestCode)
                Log.i(TAG, "granted")
                Log.i(TAG, "to callback")
            }
        } else {
            callback.allPermissionsGranted(requestCode)

            Log.i(TAG, "granted")
            Log.i(TAG, "to callback")
        }
    }


    private fun checkAndRequestPermissions(permissions: List<String>): Boolean {
        if (permissions.isNotEmpty()) {
            permissionsNeeded = ArrayList<String>()

            for (permission in permissions) {
                val hasPermission = ContextCompat.checkSelfPermission(activity, permission)

                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission)
                }

            }

            if (permissionsNeeded.isNotEmpty()) {
                getPermissions()
                /*if (!activity.getIsPermissionDialogShown()) {
                    activity.showPermissionDialog()
                }
                else{
                    getPermissions()
                }*/
                return false
            }
        }

        return true
    }

    fun getPermissions() {
        ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(), requestCode)
    }


    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            this.requestCode ->
                if (grantResults.isNotEmpty()) {
                    val perms = HashMap<String, Int>()

                    for (i in permissions.indices) {
                        perms[permissions[i]] = grantResults[i]
                    }

                    val pendingPermissions = ArrayList<String>()

                    for (permission in permissionsNeeded) {
                        if (perms[permission] != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    activity,
                                    permission
                                )
                            )
                                pendingPermissions.add(permission)
                            else {
                                callback.permissionsNeverAskAgain(requestCode)
                                return
                            }
                        }

                    }

                    if (pendingPermissions.size > 0) {
                        showMessageOKCancel(
                            DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkPermission(
                                        pendingPermissions
                                    )
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        if (permissions.size == pendingPermissions.size)
                                            callback.permissionsDenied(requestCode)
                                        else
                                            callback.partialPermissionsGranted(
                                                requestCode,
                                                pendingPermissions
                                            )
                                    }
                                }
                            })

                    } else {
                        callback.allPermissionsGranted(requestCode)
                    }


                }
        }
    }


    fun showMessageOKCancel(listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.permission_request)
            .setPositiveButton(R.string.ok, listener)
            .setNegativeButton(R.string.cancel, listener)
            .setCancelable(false)
            .create()
            .show()
    }

}
