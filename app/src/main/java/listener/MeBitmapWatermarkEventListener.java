package listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.interfaces.OnBitmapWatermarkEventListener;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.pictureselector.ImageUtil;
import com.luck.pictureselector.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MeBitmapWatermarkEventListener implements OnBitmapWatermarkEventListener {
    private final String targetPath;

    public MeBitmapWatermarkEventListener(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public void onAddBitmapWatermark(Context context, String srcPath, String mimeType, OnKeyValueResultCallbackListener call) {
        if (PictureMimeType.isHasHttp(srcPath) || PictureMimeType.isHasVideo(mimeType)) {
            // 网络图片和视频忽略，有需求的可自行扩展
            call.onCallback(srcPath, "");
        } else {
            // 暂时只以图片为例
            Glide.with(context).asBitmap().sizeMultiplier(0.6F).load(srcPath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap watermark = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_mark_win);
                    Bitmap watermarkBitmap = ImageUtil.createWaterMaskRightTop(context, resource, watermark, 15, 15);
                    watermarkBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                    watermarkBitmap.recycle();
                    FileOutputStream fos = null;
                    String result = null;
                    try {
                        File targetFile = new File(targetPath, DateUtils.getCreateFileName("Mark_") + ".jpg");
                        fos = new FileOutputStream(targetFile);
                        fos.write(stream.toByteArray());
                        fos.flush();
                        result = targetFile.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        PictureFileUtils.close(fos);
                        PictureFileUtils.close(stream);
                    }
                    if (call != null) {
                        call.onCallback(srcPath, result);
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    if (call != null) {
                        call.onCallback(srcPath, "");
                    }
                }
            });
        }
    }
}
