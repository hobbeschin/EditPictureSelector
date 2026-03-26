package listener;

import android.Manifest;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.dialog.RemindDialog;
import com.luck.picture.lib.interfaces.OnCallbackListener;
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener;
import com.luck.picture.lib.permissions.PermissionConfig;
import com.luck.picture.lib.permissions.PermissionUtil;

public class MeOnPermissionDeniedListener implements OnPermissionDeniedListener {

    @Override
    public void onDenied(Fragment fragment, String[] permissionArray,
                         int requestCode, OnCallbackListener<Boolean> call) {
        String tips;
        if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
            tips = "缺少相机权限\n可能会导致不能使用摄像头功能";
        } else if (TextUtils.equals(permissionArray[0], Manifest.permission.RECORD_AUDIO)) {
            tips = "缺少录音权限\n访问您设备上的音频、媒体内容和文件";
        } else {
            tips = "缺少存储权限\n访问您设备上的照片、媒体内容和文件";
        }
        RemindDialog dialog = RemindDialog.buildDialog(fragment.getContext(), tips);
        dialog.setButtonText("去设置");
        dialog.setButtonTextColor(0xFF7D7DFF);
        dialog.setContentTextColor(0xFF333333);
        dialog.setOnDialogClickListener(new RemindDialog.OnDialogClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtil.goIntentSetting(fragment, requestCode);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}