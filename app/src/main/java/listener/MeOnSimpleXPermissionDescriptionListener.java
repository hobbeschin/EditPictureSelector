package listener;

import android.content.Context;
import android.view.ViewGroup;

import com.luck.lib.camerax.listener.OnSimpleXPermissionDescriptionListener;
import com.luck.pictureselector.MeOnHelp;

public class MeOnSimpleXPermissionDescriptionListener implements OnSimpleXPermissionDescriptionListener {
    private String viewTag = "";

    public MeOnSimpleXPermissionDescriptionListener(String tag) {
        viewTag = tag;
    }

    @Override
    public void onPermissionDescription(Context context, ViewGroup viewGroup, String permission) {
        MeOnHelp.getInstance().addPermissionDescription(
                true, viewGroup, new String[]{permission}, viewTag);
    }

    @Override
    public void onDismiss(ViewGroup viewGroup) {
        MeOnHelp.getInstance().removePermissionDescription(viewGroup, viewTag);
    }
}
