package app.phonepost

interface PermissionResultCallback {
    fun allPermissionsGranted(requestCode: Int)
    fun permissionsNeverAskAgain(requestCode: Int)
    fun permissionsDenied(requestCode: Int)
    fun partialPermissionsGranted(requestCode: Int, pendingPermissions: ArrayList<String>)
}