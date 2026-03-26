package listener;

import android.Manifest;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.interfaces.OnRecordAudioInterceptListener;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.permissions.PermissionResultCallback;
import com.luck.picture.lib.utils.ToastUtils;
import com.luck.pictureselector.MeOnHelp;

public class MeOnRecordAudioInterceptListener implements OnRecordAudioInterceptListener {
    private String viewTag = "";

    public MeOnRecordAudioInterceptListener(String tag) {
        viewTag = tag;
    }

    @Override
    public void onRecordAudio(Fragment fragment, int requestCode) {
        String[] recordAudio = {Manifest.permission.RECORD_AUDIO};
        if (PermissionChecker.isCheckSelfPermission(fragment.getContext(), recordAudio)) {
            startRecordSoundAction(fragment, requestCode);
        } else {
            MeOnHelp.getInstance().addPermissionDescription(
                    false, (ViewGroup) fragment.requireView(), recordAudio, viewTag
            );
            PermissionChecker.getInstance().requestPermissions(fragment,
                    new String[]{Manifest.permission.RECORD_AUDIO}, new PermissionResultCallback() {
                        @Override
                        public void onGranted() {
                            MeOnHelp.getInstance().removePermissionDescription(
                                    (ViewGroup) fragment.requireView(), viewTag
                            );
                            startRecordSoundAction(fragment, requestCode);
                        }

                        @Override
                        public void onDenied() {
                            MeOnHelp.getInstance().removePermissionDescription(
                                    (ViewGroup) fragment.requireView(), viewTag
                            );
                        }
                    });
        }
    }

    private void startRecordSoundAction(Fragment fragment, int requestCode) {
        Intent recordAudioIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (recordAudioIntent.resolveActivity(fragment.requireActivity().getPackageManager()) != null) {
            fragment.startActivityForResult(recordAudioIntent, requestCode);
        } else {
            ToastUtils.showToast(fragment.getContext(), "The system is missing a recording component");
        }
    }
}
