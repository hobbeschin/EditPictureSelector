package listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.interfaces.OnVideoThumbnailEventListener;
import com.luck.picture.lib.utils.PictureFileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MeOnVideoThumbnailEventListener implements OnVideoThumbnailEventListener {
    private final String targetPath;

    public MeOnVideoThumbnailEventListener(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    public void onVideoThumbnail(Context context, String videoPath, OnKeyValueResultCallbackListener call) {
        Glide.with(context).asBitmap().sizeMultiplier(0.6F).load(videoPath).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resource.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                FileOutputStream fos = null;
                String result = null;
                try {
                    File targetFile = new File(targetPath, "thumbnails_" + System.currentTimeMillis() + ".jpg");
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
                    call.onCallback(videoPath, result);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (call != null) {
                    call.onCallback(videoPath, "");
                }
            }
        });
    }
}
