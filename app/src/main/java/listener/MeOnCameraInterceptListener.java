package listener;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.luck.lib.camerax.CameraImageEngine;
import com.luck.lib.camerax.SimpleCameraX;
import com.luck.picture.lib.interfaces.OnCameraInterceptListener;
import com.luck.pictureselector.MeOnHelp;

public class MeOnCameraInterceptListener implements OnCameraInterceptListener {
    private Context context;
    private String tag;
    private CheckBox cb_camera_focus, cb_camera_zoom, cb_custom_sandbox, cb_permission_desc;
    public MeOnCameraInterceptListener(
            Context context, String tag, CheckBox cb_camera_focus,
            CheckBox cb_camera_zoom, CheckBox cb_custom_sandbox,
            CheckBox cb_permission_desc
    ) {
        this.context = context;
        this.tag = tag;
        this.cb_camera_focus = cb_camera_focus;
        this.cb_camera_zoom = cb_camera_zoom;
        this.cb_custom_sandbox = cb_custom_sandbox;
        this.cb_permission_desc = cb_permission_desc;
    }

    @Override
    public void openCamera(Fragment fragment, int cameraMode, int requestCode) {
        SimpleCameraX camera = SimpleCameraX.of();
        camera.isAutoRotation(true);
        camera.setCameraMode(cameraMode);
        camera.setVideoFrameRate(25);
        camera.setVideoBitRate(3 * 1024 * 1024);
        camera.isDisplayRecordChangeTime(true);
        camera.isManualFocusCameraPreview(cb_camera_focus.isChecked());
        camera.isZoomCameraPreview(cb_camera_zoom.isChecked());
        camera.setOutputPathDir(
                MeOnHelp.getInstance().getSandboxCameraOutputPath(context, cb_custom_sandbox)
        );
        camera.setPermissionDeniedListener(
                MeOnHelp.getInstance().getSimpleXPermissionDeniedListener(cb_permission_desc)
        );
        camera.setPermissionDescriptionListener(
                MeOnHelp.getInstance().getSimpleXPermissionDescriptionListener(
                        cb_permission_desc, tag
                )
        );
        camera.setImageEngine(new CameraImageEngine() {
            @Override
            public void loadImage(Context context, String url, ImageView imageView) {
                Glide.with(context).load(url).into(imageView);
            }
        });
        camera.start(fragment.requireActivity(), fragment, requestCode);
    }
}
