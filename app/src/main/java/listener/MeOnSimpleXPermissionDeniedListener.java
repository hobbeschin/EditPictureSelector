package listener;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.luck.lib.camerax.listener.OnSimpleXPermissionDeniedListener;
import com.luck.lib.camerax.permissions.SimpleXPermissionUtil;
import com.luck.picture.lib.dialog.RemindDialog;

public class MeOnSimpleXPermissionDeniedListener implements OnSimpleXPermissionDeniedListener {

    @Override
    public void onDenied(Context context, String permission, int requestCode) {
        String tips;
        if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
            tips = "缺少麦克风权限\n可能会导致录视频无法采集声音";
        } else {
            tips = "缺少相机权限\n可能会导致不能使用摄像头功能";
        }
        RemindDialog dialog = RemindDialog.buildDialog(context, tips);
        dialog.setButtonText("去设置");
        dialog.setButtonTextColor(0xFF7D7DFF);
        dialog.setContentTextColor(0xFF333333);
        dialog.setOnDialogClickListener(new RemindDialog.OnDialogClickListener() {
            @Override
            public void onClick(View view) {
                SimpleXPermissionUtil.goIntentSetting((Activity) context, requestCode);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
