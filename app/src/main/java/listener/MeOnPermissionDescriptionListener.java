package listener;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener;
import com.luck.pictureselector.MeOnHelp;

public class MeOnPermissionDescriptionListener implements OnPermissionDescriptionListener {

    private String viewTag = "";

    public MeOnPermissionDescriptionListener(String tag) {
        viewTag = tag;
    }

    @Override
    public void onPermissionDescription(Fragment fragment, String[] permissionArray) {
        View rootView = fragment.requireView();
        if (rootView instanceof ViewGroup) {
            MeOnHelp.getInstance().addPermissionDescription(
                    false, (ViewGroup) rootView, permissionArray, viewTag
            );
        }
    }

    @Override
    public void onDismiss(Fragment fragment) {
        MeOnHelp.getInstance().removePermissionDescription((ViewGroup) fragment.requireView(), viewTag);
    }
}