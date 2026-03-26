package listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.pictureselector.ImageLoaderUtils;
import com.luck.pictureselector.MeOnHelp;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.util.ArrayList;

public class ImageFileCropEngine implements CropFileEngine {
    public FragmentActivity activity;
    private CheckBox cb_hide, cb_styleCrop, cb_showCropFrame,
            cb_showCropGrid, cb_crop_circular,  cb_not_gif, cb_skip_not_gif;
    private int aspect_ratio_x, aspect_ratio_y;
    private PictureSelectorStyle selectorStyle;
    public ImageFileCropEngine(
            FragmentActivity activity, CheckBox cb_hide, CheckBox cb_styleCrop,
            CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
            int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
            PictureSelectorStyle selectorStyle
    ) {
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
    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri,
                            ArrayList<String> dataSource, int requestCode) {
        UCrop.Options options = MeOnHelp.getInstance().buildOptions(
                activity,cb_hide, cb_styleCrop,cb_showCropFrame, cb_showCropGrid, cb_crop_circular,
                aspect_ratio_x, aspect_ratio_y, cb_not_gif, cb_skip_not_gif, selectorStyle
        );
        UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
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
                Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (call != null) {
                            call.onCall(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (call != null) {
                            call.onCall(null);
                        }
                    }
                });
            }
        });
        uCrop.start(fragment.requireActivity(), fragment, requestCode);
    }
}
