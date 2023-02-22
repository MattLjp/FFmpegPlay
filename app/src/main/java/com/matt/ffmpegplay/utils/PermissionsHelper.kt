package com.matt.ffmpegplay.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

/**
 * Created by Liaojp
 * @Date: 2022/7/11
 * @email: 329524627@qq.com
 * @Description:
 */
class PermissionsHelper {
    private var permissionLaunch: ActivityResultLauncher<Array<String>>? = null
    private var openSettingLaunch: ActivityResultLauncher<Intent>? = null
    private var alertDialog: AlertDialog? = null
    private var permissionsCallback: ((Boolean) -> Unit)? = null

    private var mPermissions: Array<String> = arrayOf()

    //通过的权限
    var grantedPermissions: Set<String> = setOf()
        private set

    //未通过的权限
    var deniedPermissions: Set<String> = setOf()
        private set

    //拒绝并且点了“不再询问”权限
    var alwaysDeniedPermissions: List<String> = listOf()
        private set


    /**
     * 必须在activity的onResume之前初始化
     */
    fun initPermissions(activity: ComponentActivity) {
        permissionLaunch =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                //通过的权限
                grantedPermissions = result.filterValues { it }.keys

                //未通过的权限
                deniedPermissions = result.keys - grantedPermissions

                //拒绝并且点了“不再询问”权限
                alwaysDeniedPermissions = try {
                    deniedPermissions.filterNot {
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                    }
                } catch (e: Exception) {
                    listOf()
                }
                if (alwaysDeniedPermissions.isNotEmpty()) {
                    showOpenSettingDialog(activity)
                }
                permissionsCallback?.invoke(grantedPermissions.size == result.size)
            }

        openSettingLaunch = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionLaunch?.launch(mPermissions)
        }

    }


    /**
     * 请求权限
     * @param permissionsArr Array<String>?
     */
    fun requestPermissions(
        context: Context,
        permissionsArr: Array<String>? = null,
        callback: ((Boolean) -> Unit)? = null
    ) {
        permissionsCallback = callback
        mPermissions = permissionsArr ?: getAllPermissions(context)
        permissionLaunch?.launch(mPermissions)
    }


    /**
     * 收集需要申请的权限, 例如 [permission.READ_EXTERNAL_STORAGE]
     * 默认会自动探测需要的权限, 但是自动探测可能失败
     */
    private fun getAllPermissions(context: Context): Array<String> {
        return try {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            ).requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        } ?: arrayOf()
    }


    /**
     * 显示打开应用的设置页面对话框
     *
     */
    private fun showOpenSettingDialog(context: Context) {
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
            alertDialog = null
        }
        alertDialog = AlertDialog.Builder(context)
            .setTitle("提示")
            .setMessage("当前应用所需要的权限已经被您禁用，请在设置中打开权限后才能继续使用")
            .setPositiveButton("打开设置") { dialog, _ ->
                dialog.dismiss()
                openSettingActivity(context)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 显示打开应用的设置页面对话框 只有一个按钮的
     *
     */
    private fun showOpenSettingDialogSingle(context: Context) {
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
            alertDialog = null
        }
        alertDialog = AlertDialog.Builder(context)
            .setTitle("提示")
            .setMessage("当前应用所需要的权限已经被您禁用，请在设置中打开权限后才能继续使用")
            .setPositiveButton("打开设置") { dialog, _ ->
                dialog.dismiss()
                openSettingActivity(context)
            }
            .setCancelable(false)
            .show()
    }


    /**
     * 关闭对话框
     */
    fun dismissDialog() {
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
            alertDialog = null
        }
    }

    private fun openSettingActivity(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        openSettingLaunch?.launch(intent)
    }

}