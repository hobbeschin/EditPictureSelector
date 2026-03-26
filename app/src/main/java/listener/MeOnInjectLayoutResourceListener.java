package listener;

import android.content.Context;
import android.util.Log;

import com.luck.picture.lib.config.InjectResourceSource;
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener;
import com.luck.pictureselector.R;

public class MeOnInjectLayoutResourceListener implements OnInjectLayoutResourceListener {

    @Override
    public int getLayoutResourceId(Context context, int resourceSource) {
        switch (resourceSource) {
            case InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE:
                Log.e("TAG", "getLayoutResourceId: 图片选择，加载了相机胶卷--PictureSelectorEngineImp");
                return R.layout.ps_custom_fragment_selector;
            case InjectResourceSource.PREVIEW_LAYOUT_RESOURCE:
                return R.layout.ps_custom_fragment_preview;
            case InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE:
                return R.layout.ps_custom_item_grid_image;
            case InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE:
                return R.layout.ps_custom_item_grid_video;
            case InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE:
                return R.layout.ps_custom_item_grid_audio;
            case InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE:
                return R.layout.ps_custom_album_folder_item;
            case InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE:
                return R.layout.ps_custom_preview_image;
            case InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE:
                return R.layout.ps_custom_preview_video;
            case InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE:
                return R.layout.ps_custom_preview_gallery_item;
            default:
                return 0;
        }
    }
}
