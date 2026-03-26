package listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.CropEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.pictureselector.ImageLoaderUtils;
import com.luck.pictureselector.MeOnHelp;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.io.File;
import java.util.ArrayList;

public class ImageCropEngine implements CropEngine {
    public FragmentActivity activity;
    private CheckBox cb_hide, cb_styleCrop, cb_showCropFrame,
            cb_showCropGrid, cb_crop_circular,  cb_not_gif, cb_skip_not_gif;
    private int aspect_ratio_x, aspect_ratio_y;
    private PictureSelectorStyle selectorStyle;
    public ImageCropEngine(FragmentActivity activity, CheckBox cb_hide, CheckBox cb_styleCrop,
                           CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
                           int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
                           PictureSelectorStyle selectorStyle) {
        this.activity = activity;
        this.cb_hide = cb_hide;
        this.cb_styleCrop = cb_styleCrop;
        this.cb_showCropFrame = cb_showCropFrame;
        this.cb_showCropGrid = cb_showCropGrid;
        this.cb_crop_circular = cb_crop_circular;
        this.cb_not_gif = cb_not_gif;
        this.cb_skip_not_gif = cb_skip_not_gif;

        this.aspect_ratio_x = aspect_ratio_x;
        this.aspect_ratio_y = aspect_ratio_y;

        this.selectorStyle = selectorStyle;
    }

    @Override
    public void onStartCrop(Fragment fragment, LocalMedia currentLocalMedia,
                            ArrayList<LocalMedia> dataSource, int requestCode) {
        String currentCropPath = currentLocalMedia.getAvailablePath();
        Uri inputUri;
        if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(currentCropPath)) {
            inputUri = Uri.parse(currentCropPath);
        } else {
            inputUri = Uri.fromFile(new File(currentCropPath));
        }
        String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
        Uri destinationUri = Uri.fromFile(
                new File(
                        MeOnHelp.getInstance().getSandboxPath(activity), fileName
                )
        );
        UCrop.Options options = MeOnHelp.getInstance().buildOptions(
                activity,cb_hide, cb_styleCrop,cb_showCropFrame, cb_showCropGrid, cb_crop_circular,
                aspect_ratio_x, aspect_ratio_y, cb_not_gif, cb_skip_not_gif, selectorStyle
        );
        ArrayList<String> dataCropSource = new ArrayList<>();
        for (int i = 0; i < dataSource.size(); i++) {
            LocalMedia media = dataSource.get(i);
            dataCropSource.add(media.getAvailablePath());
        }
        UCrop uCrop = UCrop.of(inputUri, destinationUri, dataCropSource);
        //options.setMultipleCropAspectRatio(buildAspectRatios(dataSource.size()));
        uCrop.withOptions(options);
        uCrop.setImageEngine(new UCropImageEngine() {
            @Override
            public void loadImage(Context context, String url, ImageView imageView) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return;
                }
                Glide.with(context).load(url).override(180, 180).into(imageView);
            }

            @Override
            public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
            }
        });
        uCrop.start(fragment.requireActivity(), fragment, requestCode);
    }
}
