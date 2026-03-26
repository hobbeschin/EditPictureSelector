package com.luck.pictureselector;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.lib.camerax.listener.OnSimpleXPermissionDeniedListener;
import com.luck.lib.camerax.listener.OnSimpleXPermissionDescriptionListener;
import com.luck.picture.lib.PictureSelectorPreviewFragment;
import com.luck.picture.lib.basic.IBridgeViewLifecycle;
import com.luck.picture.lib.basic.PictureSelectionCameraModel;
import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelectionSystemModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.InjectResourceSource;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.ExtendLoaderEngine;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.engine.VideoPlayerEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.interfaces.OnBitmapWatermarkEventListener;
import com.luck.picture.lib.interfaces.OnCameraInterceptListener;
import com.luck.picture.lib.interfaces.OnCustomLoadingListener;
import com.luck.picture.lib.interfaces.OnGridItemSelectAnimListener;
import com.luck.picture.lib.interfaces.OnInjectActivityPreviewListener;
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener;
import com.luck.picture.lib.interfaces.OnMediaEditInterceptListener;
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener;
import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener;
import com.luck.picture.lib.interfaces.OnPreviewInterceptListener;
import com.luck.picture.lib.interfaces.OnQueryFilterListener;
import com.luck.picture.lib.interfaces.OnSelectAnimListener;
import com.luck.picture.lib.interfaces.OnVideoThumbnailEventListener;
import com.luck.picture.lib.permissions.PermissionConfig;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DensityUtil;
import com.luck.picture.lib.utils.MediaUtils;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.picture.lib.utils.StyleUtils;
import com.luck.picture.lib.widget.MediumBoldTextView;
import com.luck.pictureselector.adapter.GridImageAdapter;
import com.luck.pictureselector.listener.OnItemLongClickListener;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import listener.ImageCropEngine;
import listener.ImageFileCropEngine;
import listener.MeBitmapWatermarkEventListener;
import listener.MeExtendLoaderEngine;
import listener.MeOnCameraInterceptListener;
import listener.MeOnInjectLayoutResourceListener;
import listener.MeOnMediaEditInterceptListener;
import listener.MeOnPermissionDeniedListener;
import listener.MeOnPermissionDescriptionListener;
import listener.MeOnPreviewInterceptListener;
import listener.MeOnRecordAudioInterceptListener;
import listener.MeOnResultCallbackListener;
import listener.MeOnSelectLimitTipsListener;
import listener.MeOnSimpleXPermissionDeniedListener;
import listener.MeOnSimpleXPermissionDescriptionListener;
import listener.MeOnVideoThumbnailEventListener;
import listener.MeSandboxFileEngine;
import listener.MyExternalPreviewEventListener;

public class MeOnHelp {
    private final String TAG = "";
    public static final class SingleTon {
        public static final MeOnHelp INSTANCE = new MeOnHelp();
    }

    public static MeOnHelp getInstance() {
        return SingleTon.INSTANCE;
    }

    public void addPermissionDescription(boolean isHasSimpleXCamera, ViewGroup viewGroup,
                                          String[] permissionArray, String viewTag) {
        int dp10 = DensityUtil.dip2px(viewGroup.getContext(), 10);
        int dp15 = DensityUtil.dip2px(viewGroup.getContext(), 15);
        MediumBoldTextView view = new MediumBoldTextView(viewGroup.getContext());
        view.setTag(viewTag);
        view.setTextSize(14);
        view.setTextColor(Color.parseColor("#333333"));
        view.setPadding(dp10, dp15, dp10, dp15);

        String title;
        String explain;

        if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
            title = "相机权限使用说明";
            explain = "相机权限使用说明\n用户app用于拍照/录视频";
        } else if (TextUtils.equals(permissionArray[0], Manifest.permission.RECORD_AUDIO)) {
            if (isHasSimpleXCamera) {
                title = "麦克风权限使用说明";
                explain = "麦克风权限使用说明\n用户app用于录视频时采集声音";
            } else {
                title = "录音权限使用说明";
                explain = "录音权限使用说明\n用户app用于采集声音";
            }
        } else {
            title = "存储权限使用说明";
            explain = "存储权限使用说明\n用户app写入/下载/保存/读取/修改/删除图片、视频、文件等信息";
        }
        int startIndex = 0;
        int endOf = startIndex + title.length();
        SpannableStringBuilder builder = new SpannableStringBuilder(explain);
        builder.setSpan(new AbsoluteSizeSpan(DensityUtil.dip2px(viewGroup.getContext(), 16)), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(0xFF333333), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(builder);
        view.setBackground(ContextCompat.getDrawable(viewGroup.getContext(), R.drawable.ps_demo_permission_desc_bg));

        if (isHasSimpleXCamera) {
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = DensityUtil.getStatusBarHeight(viewGroup.getContext());
            layoutParams.leftMargin = dp10;
            layoutParams.rightMargin = dp10;
            viewGroup.addView(view, layoutParams);
        } else {
            ConstraintLayout.LayoutParams layoutParams =
                    new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topToBottom = R.id.title_bar;
            layoutParams.leftToLeft = ConstraintSet.PARENT_ID;
            layoutParams.leftMargin = dp10;
            layoutParams.rightMargin = dp10;
            viewGroup.addView(view, layoutParams);
        }
    }

    public void removePermissionDescription(ViewGroup viewGroup, String viewTag) {
        View tagExplainView = viewGroup.findViewWithTag(viewTag);
        viewGroup.removeView(tagExplainView);
    }

    public String getSandboxPath(Context context) {
        File externalFilesDir = context.getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "Sandbox");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }

    public String getSandboxCameraOutputPath(Context context, CheckBox cb) {
        if (cb.isChecked()) {
            File externalFilesDir = context.getExternalFilesDir("");
            File customFile = new File(externalFilesDir.getAbsolutePath(), "Sandbox");
            if (!customFile.exists()) {
                customFile.mkdirs();
            }
            return customFile.getAbsolutePath() + File.separator;
        } else {
            return "";
        }
    }

    public OnSimpleXPermissionDeniedListener getSimpleXPermissionDeniedListener(CheckBox cb) {
        return cb.isChecked() ? new MeOnSimpleXPermissionDeniedListener() : null;
    }

    public OnSimpleXPermissionDescriptionListener getSimpleXPermissionDescriptionListener(CheckBox cb, String tag) {
        return cb.isChecked() ? new MeOnSimpleXPermissionDescriptionListener(tag) : null;
    }

    public OnPermissionDeniedListener getPermissionDeniedListener(CheckBox cb) {
        return cb.isChecked() ? new MeOnPermissionDeniedListener() : null;
    }

    public OnBitmapWatermarkEventListener getAddBitmapWatermarkListener(Context context, CheckBox cb) {
        return cb.isChecked() ? new MeBitmapWatermarkEventListener(getSandboxMarkDir(context)) : null;
    }

    public String getSandboxMarkDir(Context context) {
        File externalFilesDir = context.getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "Mark");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }

    public OnVideoThumbnailEventListener getVideoThumbnailEventListener(Context context, CheckBox cb) {
        return cb.isChecked() ? new MeOnVideoThumbnailEventListener(getVideoThumbnailDir(context)) : null;
    }

    public OnMediaEditInterceptListener getCustomEditMediaEvent(
            FragmentActivity activity, CheckBox cb, CheckBox cb_hide, CheckBox cb_styleCrop,
            CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
            int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
            PictureSelectorStyle selectorStyle) {
        return cb.isChecked() ? new MeOnMediaEditInterceptListener(
                MeOnHelp.getInstance().getSandboxPath(activity), buildOptions(
                activity, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid, cb_crop_circular,
                aspect_ratio_x, aspect_ratio_y, cb_not_gif, cb_skip_not_gif, selectorStyle
        )
        ) : null;
    }

    public OnPreviewInterceptListener getPreviewInterceptListener(CheckBox cb) {
        return cb.isChecked() ? new MeOnPreviewInterceptListener() : null;
    }

    public OnPermissionDescriptionListener getPermissionDescriptionListener(CheckBox cb, String tag) {
        return cb.isChecked() ? new MeOnPermissionDescriptionListener(tag) : null;
    }

    public OnCustomLoadingListener getCustomLoadingListener(CheckBox cb) {
        if (cb.isChecked()) {
            return new OnCustomLoadingListener() {
                @Override
                public Dialog create(Context context) {
                    return new CustomLoadingDialog(context);
                }
            };
        }
        return null;
    }

    public OnInjectLayoutResourceListener getInjectLayoutResource(CheckBox cb) {
        return cb.isChecked() ? new MeOnInjectLayoutResourceListener() : null;
    }

    public ExtendLoaderEngine getExtendLoaderEngine() {
        return new MeExtendLoaderEngine();
    }

    public OnCameraInterceptListener getCustomCameraEvent(
            Context context, CheckBox cb, String tag, CheckBox cb_camera_focus,
            CheckBox cb_camera_zoom, CheckBox cb_custom_sandbox,
            CheckBox cb_permission_desc
            ) {
        return cb.isChecked() ? new MeOnCameraInterceptListener(
                context, tag, cb_camera_focus,
                cb_camera_zoom, cb_custom_sandbox,
                cb_permission_desc
        ) : null;
    }

    public ImageCropEngine getCropEngine(
            FragmentActivity activity, CheckBox cb, CheckBox cb_hide, CheckBox cb_styleCrop,
            CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
            int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
            PictureSelectorStyle selectorStyle
            ) {
        return cb.isChecked() ? new ImageCropEngine(
                activity, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid,
                cb_crop_circular, aspect_ratio_x, aspect_ratio_y, cb_not_gif,
                cb_skip_not_gif, selectorStyle) : null;
    }

    public ImageFileCropEngine getCropFileEngine(
            FragmentActivity activity, CheckBox cb, CheckBox cb_hide, CheckBox cb_styleCrop,
            CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
            int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
            PictureSelectorStyle selectorStyle) {
        return cb.isChecked() ? new ImageFileCropEngine(
                activity, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid,
                cb_crop_circular, aspect_ratio_x, aspect_ratio_y, cb_not_gif,
                cb_skip_not_gif, selectorStyle
        ) : null;
    }

    public ImageFileCompressEngine getCompressFileEngine(FragmentActivity activity, CheckBox cb) {
        return cb.isChecked() ? new ImageFileCompressEngine() : null;
    }

    public String getVideoThumbnailDir(Context context) {
        File externalFilesDir = context.getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "Thumbnail");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }

    public String getSandboxAudioOutputPath(Context context, CheckBox cb) {
        if (cb.isChecked()) {
            File externalFilesDir = context.getExternalFilesDir("");
            File customFile = new File(externalFilesDir.getAbsolutePath(), "Sound");
            if (!customFile.exists()) {
                customFile.mkdirs();
            }
            return customFile.getAbsolutePath() + File.separator;
        } else {
            return "";
        }
    }

    public UCrop.Options buildOptions(FragmentActivity activity, CheckBox cb_hide, CheckBox cb_styleCrop,
              CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular,
              int aspect_ratio_x, int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif,
              PictureSelectorStyle selectorStyle) {
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(!cb_hide.isChecked());
        options.setFreeStyleCropEnabled(cb_styleCrop.isChecked());
        options.setShowCropFrame(cb_showCropFrame.isChecked());
        options.setShowCropGrid(cb_showCropGrid.isChecked());
        options.setCircleDimmedLayer(cb_crop_circular.isChecked());
        options.withAspectRatio(aspect_ratio_x, aspect_ratio_y);
        options.setCropOutputPathDir(
                MeOnHelp.getInstance().getSandboxPath(activity)
        );
        options.isCropDragSmoothToCenter(false);
        options.setSkipCropMimeType(getNotSupportCrop(cb_skip_not_gif));
        options.isForbidCropGifWebp(cb_not_gif.isChecked());
        options.isForbidSkipMultipleCrop(true);
        options.setMaxScaleMultiplier(100);
        if (selectorStyle != null && selectorStyle.getSelectMainStyle().getStatusBarColor() != 0) {
            SelectMainStyle mainStyle = selectorStyle.getSelectMainStyle();
            boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
            int statusBarColor = mainStyle.getStatusBarColor();
            options.isDarkStatusBarBlack(isDarkStatusBarBlack);
            if (StyleUtils.checkStyleValidity(statusBarColor)) {
                options.setStatusBarColor(statusBarColor);
                options.setToolbarColor(statusBarColor);
            } else {
                options.setStatusBarColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_grey));
                options.setToolbarColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_grey));
            }
            TitleBarStyle titleBarStyle = selectorStyle.getTitleBarStyle();
            if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleTextColor())) {
                options.setToolbarWidgetColor(titleBarStyle.getTitleTextColor());
            } else {
                options.setToolbarWidgetColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_white));
            }
        } else {
            options.setStatusBarColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_grey));
            options.setToolbarColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_grey));
            options.setToolbarWidgetColor(ContextCompat.getColor(activity, com.luck.picture.lib.R.color.ps_color_white));
        }
        return options;
    }

    public String[] getNotSupportCrop(CheckBox cb_skip_not_gif) {
        if (cb_skip_not_gif.isChecked()) {
            return new String[]{PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()};
        }
        return null;
    }

    public void analyticalSelectResults(FragmentActivity activity, ArrayList<LocalMedia> result, GridImageAdapter mAdapter) {
        for (LocalMedia media : result) {
            Log.e(TAG, "analyticalSelectResults: inputMsg=============" + media.getInputMsg());
            if (media.getWidth() == 0 || media.getHeight() == 0) {
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    MediaExtraInfo imageExtraInfo = MediaUtils.getImageSize(activity, media.getPath());
                    media.setWidth(imageExtraInfo.getWidth());
                    media.setHeight(imageExtraInfo.getHeight());
                } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    MediaExtraInfo videoExtraInfo = MediaUtils.getVideoSize(activity, media.getPath());
                    media.setWidth(videoExtraInfo.getWidth());
                    media.setHeight(videoExtraInfo.getHeight());
                }
            }
            Log.i(TAG, "文件名: " + media.getFileName());
            Log.i(TAG, "是否压缩:" + media.isCompressed());
            Log.i(TAG, "压缩:" + media.getCompressPath());
            Log.i(TAG, "初始路径:" + media.getPath());
            Log.i(TAG, "绝对路径:" + media.getRealPath());
            Log.i(TAG, "是否裁剪:" + media.isCut());
            Log.i(TAG, "裁剪路径:" + media.getCutPath());
            Log.i(TAG, "是否开启原图:" + media.isOriginal());
            Log.i(TAG, "原图路径:" + media.getOriginalPath());
            Log.i(TAG, "沙盒路径:" + media.getSandboxPath());
            Log.i(TAG, "水印路径:" + media.getWatermarkPath());
            Log.i(TAG, "视频缩略图:" + media.getVideoThumbnailPath());
            Log.i(TAG, "原始宽高: " + media.getWidth() + "x" + media.getHeight());
            Log.i(TAG, "裁剪宽高: " + media.getCropImageWidth() + "x" + media.getCropImageHeight());
            Log.i(TAG, "文件大小: " + PictureFileUtils.formatAccurateUnitFileSize(media.getSize()));
            Log.i(TAG, "文件时长: " + media.getDuration());
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isMaxSize = result.size() == mAdapter.getSelectMax();
                int oldSize = mAdapter.getData().size();
                mAdapter.notifyItemRangeRemoved(0, isMaxSize ? oldSize + 1 : oldSize);
                mAdapter.getData().clear();

                mAdapter.getData().addAll(result);
                mAdapter.notifyItemRangeInserted(0, result.size());
            }
        });
    }

    public ActivityResultLauncher<Intent> createActivityResultLauncher(FragmentActivity activity, GridImageAdapter mAdapter) {
        return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        if (resultCode == activity.RESULT_OK) {
                            ArrayList<LocalMedia> selectList = PictureSelector.obtainSelectorList(result.getData());
                            MeOnHelp.getInstance().analyticalSelectResults(activity, selectList, mAdapter);
                        } else if (resultCode == activity.RESULT_CANCELED) {
                            Log.i(TAG, "onActivityResult PictureSelector Cancel");
                        }
                    }
                });
    }

    public void mAdapterClickListener(
            FragmentActivity activity, GridImageAdapter mAdapter, ItemTouchHelper mItemTouchHelper,
            ImageEngine imageEngine, VideoPlayerEngine videoPlayerEngine, PictureSelectorStyle selectorStyle,
            int language, CheckBox cb_auto_video, CheckBox cb_preview_full, CheckBox cb_video_resume,
            boolean isUseSystemPlayer, CheckBox cb_custom_loading, int chooseMode, CheckBox cb_preview_scale,
            RecyclerView mRecyclerView, CheckBox cb_custom_preview, CheckBox cb_mode, CheckBox cb_system_album,
            CheckBox cb_choose_mode, CheckBox cb_compress, CheckBox cb_crop, CheckBox cb_hide, CheckBox cb_styleCrop,
            CheckBox cb_showCropFrame, CheckBox cb_showCropGrid, CheckBox cb_crop_circular, int aspect_ratio_x,
            int aspect_ratio_y, CheckBox cb_not_gif, CheckBox cb_skip_not_gif, CheckBox cb_watermark,
            CheckBox cb_video_thumbnails, CheckBox cb_original, CheckBox cb_permission_desc, String tag,
            CheckBox cb_attach_system_mode, int resultMode, ActivityResultLauncher<Intent> launcherResult,
            CheckBox cb_custom_camera, CheckBox cb_camera_focus, CheckBox cb_camera_zoom, CheckBox cb_custom_sandbox,
            CheckBox cbEditor, CheckBox cb_inject_layout, CheckBox cb_query_sort_order, CheckBox cb_time_axis,
            CheckBox cb_only_dir, CheckBox cbPage, CheckBox cb_isCamera, CheckBox cb_voice, CheckBox cb_fast_select,
            CheckBox cb_WithImageVideo, CheckBox cb_preview_img, CheckBox cb_preview_video, CheckBox cb_preview_audio,
            CheckBox cb_selected_anim, CheckBox cbEnabledMask, CheckBox cb_single_back, int maxSelectNum,
            int maxSelectVideoNum, int animationMode, CheckBox cb_isGif, CheckBox cb_attach_camera_mode) {
        mAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // 预览图片、视频、音频
                PictureSelector.create(activity)
                        .openPreview()
                        .setImageEngine(imageEngine)
                        .setVideoPlayerEngine(videoPlayerEngine)
                        .setSelectorUIStyle(selectorStyle)
                        .setLanguage(language)
                        .isAutoVideoPlay(cb_auto_video.isChecked())
                        .isLoopAutoVideoPlay(cb_auto_video.isChecked())
                        .isPreviewFullScreenMode(cb_preview_full.isChecked())
                        .isVideoPauseResumePlay(cb_video_resume.isChecked())
                        .isUseSystemVideoPlayer(isUseSystemPlayer)
                        .setCustomLoadingListener(
                                MeOnHelp.getInstance().getCustomLoadingListener(cb_custom_loading)
                        )
                        .isPreviewZoomEffect(
                                chooseMode != SelectMimeType.ofAudio() &&
                                        cb_preview_scale.isChecked(), mRecyclerView
                        )
                        .setAttachViewLifecycle(new IBridgeViewLifecycle() {
                            @Override
                            public void onViewCreated(Fragment fragment, View view, Bundle savedInstanceState) {
//                                PictureSelectorPreviewFragment previewFragment = (PictureSelectorPreviewFragment) fragment;
//                                MediumBoldTextView tvShare = view.findViewById(R.id.tv_share);
//                                tvShare.setVisibility(View.VISIBLE)
//                                previewFragment.addAminViews(tvShare);
//                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tvShare.getLayoutParams();
//                                layoutParams.topMargin = cb_preview_full.isChecked() ? DensityUtil.getStatusBarHeight(getContext()) : 0;
//                                tvShare.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        PicturePreviewAdapter previewAdapter = previewFragment.getAdapter();
//                                        ViewPager2 viewPager2 = previewFragment.getViewPager2();
//                                        LocalMedia media = previewAdapter.getItem(viewPager2.getCurrentItem());
//                                        ToastUtils.showToast(fragment.getContext(), "自定义分享事件:" + viewPager2.getCurrentItem());
//                                    }
//                                });
                            }

                            @Override
                            public void onDestroy(Fragment fragment) {
//                                if (cb_preview_full.isChecked()) {
//                                    // 如果是全屏预览模式且是startFragmentPreview预览，回到自己的界面时需要恢复一下自己的沉浸式状态
//                                    // 以下提供2种解决方案:
//                                    // 1.通过ImmersiveManager.immersiveAboveAPI23重新设置一下沉浸式
//                                    int statusBarColor = ContextCompat.getColor(getContext(), R.color.ps_color_grey);
//                                    int navigationBarColor = ContextCompat.getColor(getContext(), R.color.ps_color_grey);
//                                    ImmersiveManager.immersiveAboveAPI23(MainActivity.this,
//                                            true, true,
//                                            statusBarColor, navigationBarColor, false);
//                                    // 2.让自己的titleBar的高度加上一个状态栏高度且内容PaddingTop下沉一个状态栏的高度
//                                }
                            }
                        })
                        .setInjectLayoutResourceListener(new OnInjectLayoutResourceListener() {
                            @Override
                            public int getLayoutResourceId(Context context, int resourceSource) {
                                return resourceSource == InjectResourceSource.PREVIEW_LAYOUT_RESOURCE
                                        ? R.layout.ps_custom_fragment_preview
                                        : InjectResourceSource.DEFAULT_LAYOUT_RESOURCE;
                            }
                        })
                        .setExternalPreviewEventListener(
                                new MyExternalPreviewEventListener(mAdapter)
                        )
                        .setInjectActivityPreviewFragment(new OnInjectActivityPreviewListener() {
                            @Override
                            public PictureSelectorPreviewFragment onInjectPreviewFragment() {
                                return cb_custom_preview.isChecked() ? CustomPreviewFragment.newInstance() : null;
                            }
                        })
                        .startActivityPreview(position, true, mAdapter.getData());
            }

            @Override
            public void openPicture() {
                boolean mode = cb_mode.isChecked();
                if (mode) {
                    // 进入系统相册
                    if (cb_system_album.isChecked()) {
                        PictureSelectionSystemModel systemGalleryMode = PictureSelector.create(activity)
                                .openSystemGallery(chooseMode)
                                .setSelectionMode(cb_choose_mode.isChecked() ? SelectModeConfig.MULTIPLE : SelectModeConfig.SINGLE)
                                .setCompressEngine(
                                        MeOnHelp.getInstance().getCompressFileEngine(activity, cb_compress)
                                )
                                .setCropEngine(
                                        MeOnHelp.getInstance().getCropFileEngine(
                                                activity, cb_crop, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid,
                                                cb_crop_circular, aspect_ratio_x, aspect_ratio_y, cb_not_gif,
                                                cb_skip_not_gif, selectorStyle
                                        )
                                )
                                .setSkipCropMimeType(getNotSupportCrop(cb_skip_not_gif))
                                .setSelectLimitTipsListener(new MeOnSelectLimitTipsListener())
                                .setAddBitmapWatermarkListener(
                                        MeOnHelp.getInstance().getAddBitmapWatermarkListener(
                                                activity, cb_watermark
                                        )
                                )
                                .setVideoThumbnailListener(
                                        MeOnHelp.getInstance().getVideoThumbnailEventListener(
                                                activity, cb_video_thumbnails
                                        )
                                )
                                .setCustomLoadingListener(
                                        MeOnHelp.getInstance().getCustomLoadingListener(cb_custom_loading)
                                )
                                .isOriginalControl(cb_original.isChecked())
                                .setPermissionDescriptionListener(
                                        MeOnHelp.getInstance().getPermissionDescriptionListener(
                                                cb_permission_desc, tag
                                        ))
                                .setSandboxFileEngine(new MeSandboxFileEngine());
                        forSystemResult(activity, systemGalleryMode, cb_attach_system_mode, resultMode, mAdapter, launcherResult);
                    } else {
                        // 进入相册
                        PictureSelectionModel selectionModel = PictureSelector.create(activity)
                                .openGallery(chooseMode)
                                .setSelectorUIStyle(selectorStyle)
                                .setImageEngine(imageEngine)
                                .setVideoPlayerEngine(videoPlayerEngine)
                                .setCropEngine(MeOnHelp.getInstance().getCropFileEngine(
                                        activity, cb_crop, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid,
                                        cb_crop_circular, aspect_ratio_x, aspect_ratio_y, cb_not_gif,
                                        cb_skip_not_gif, selectorStyle
                                ))
                                .setCompressEngine(MeOnHelp.getInstance().getCompressFileEngine(activity, cb_compress))
                                .setSandboxFileEngine(new MeSandboxFileEngine())
                                .setCameraInterceptListener(
                                        MeOnHelp.getInstance().getCustomCameraEvent(
                                                activity, cb_custom_camera, tag, cb_camera_focus,
                                                cb_camera_zoom, cb_custom_sandbox, cb_permission_desc
                                        )
                                )
                                .setRecordAudioInterceptListener(new MeOnRecordAudioInterceptListener(tag))
                                .setSelectLimitTipsListener(new MeOnSelectLimitTipsListener())
                                .setEditMediaInterceptListener(
                                        MeOnHelp.getInstance().getCustomEditMediaEvent(
                                                activity, cbEditor, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid, cb_crop_circular,
                                                aspect_ratio_x, aspect_ratio_y, cb_not_gif, cb_skip_not_gif, selectorStyle
                                        )
                                )
                                .setPermissionDescriptionListener(
                                        MeOnHelp.getInstance().getPermissionDescriptionListener(
                                                cb_permission_desc, tag
                                        )
                                )
                                .setPreviewInterceptListener(
                                        MeOnHelp.getInstance().getPreviewInterceptListener(cb_custom_preview)
                                )
                                .setPermissionDeniedListener(
                                        MeOnHelp.getInstance().getPermissionDeniedListener(cb_permission_desc)
                                )
                                .setAddBitmapWatermarkListener(
                                        MeOnHelp.getInstance().getAddBitmapWatermarkListener(
                                                activity, cb_watermark
                                        )
                                )
                                .setVideoThumbnailListener(
                                        MeOnHelp.getInstance().getVideoThumbnailEventListener(
                                                activity, cb_video_thumbnails
                                        )
                                )
                                .isAutoVideoPlay(cb_auto_video.isChecked())
                                .isLoopAutoVideoPlay(cb_auto_video.isChecked())
                                .isUseSystemVideoPlayer(isUseSystemPlayer)
                                .isPageSyncAlbumCount(true)
                                .setCustomLoadingListener(
                                        MeOnHelp.getInstance().getCustomLoadingListener(cb_custom_loading)
                                )
                                .setQueryFilterListener(new OnQueryFilterListener() {
                                    @Override
                                    public boolean onFilter(LocalMedia media) {
                                        return false;
                                    }
                                })
                                //.setExtendLoaderEngine(getExtendLoaderEngine())
                                .setInjectLayoutResourceListener(
                                        MeOnHelp.getInstance().getInjectLayoutResource(cb_inject_layout)
                                )
                                .setSelectionMode(cb_choose_mode.isChecked() ? SelectModeConfig.MULTIPLE : SelectModeConfig.SINGLE)
                                .setLanguage(language)
                                .setQuerySortOrder(cb_query_sort_order.isChecked() ? MediaStore.MediaColumns.DATE_MODIFIED + " ASC" : "")
                                .setOutputCameraDir(chooseMode == SelectMimeType.ofAudio()
                                        ? MeOnHelp.getInstance().getSandboxAudioOutputPath(activity, cb_custom_sandbox)
                                        : MeOnHelp.getInstance().getSandboxCameraOutputPath(activity, cb_custom_sandbox))
                                .setOutputAudioDir(chooseMode == SelectMimeType.ofAudio()
                                        ? MeOnHelp.getInstance().getSandboxAudioOutputPath(activity, cb_custom_sandbox)
                                        : MeOnHelp.getInstance().getSandboxCameraOutputPath(activity, cb_custom_sandbox))
                                .setQuerySandboxDir(chooseMode == SelectMimeType.ofAudio()
                                        ? MeOnHelp.getInstance().getSandboxAudioOutputPath(activity, cb_custom_sandbox)
                                        : MeOnHelp.getInstance().getSandboxCameraOutputPath(activity, cb_custom_sandbox))
                                .isDisplayTimeAxis(cb_time_axis.isChecked())
                                .isOnlyObtainSandboxDir(cb_only_dir.isChecked())
                                .isPageStrategy(cbPage.isChecked())
                                .isOriginalControl(cb_original.isChecked())
                                .isDisplayCamera(cb_isCamera.isChecked())
                                .isOpenClickSound(cb_voice.isChecked())
                                .setSkipCropMimeType(getNotSupportCrop(cb_skip_not_gif))
                                .isFastSlidingSelect(cb_fast_select.isChecked())
                                //.setOutputCameraImageFileName("luck.jpeg")
                                //.setOutputCameraVideoFileName("luck.mp4")
                                .isWithSelectVideoImage(cb_WithImageVideo.isChecked())
                                .isPreviewFullScreenMode(cb_preview_full.isChecked())
                                .isVideoPauseResumePlay(cb_video_resume.isChecked())
                                .isPreviewZoomEffect(cb_preview_scale.isChecked())
                                .isPreviewImage(cb_preview_img.isChecked())
                                .isPreviewVideo(cb_preview_video.isChecked())
                                .isPreviewAudio(cb_preview_audio.isChecked())
                                .setGridItemSelectAnimListener(cb_selected_anim.isChecked() ? new OnGridItemSelectAnimListener() {

                                    @Override
                                    public void onSelectItemAnim(View view, boolean isSelected) {
                                        AnimatorSet set = new AnimatorSet();
                                        set.playTogether(
                                                ObjectAnimator.ofFloat(view, "scaleX", isSelected ? 1F : 1.12F, isSelected ? 1.12f : 1.0F),
                                                ObjectAnimator.ofFloat(view, "scaleY", isSelected ? 1F : 1.12F, isSelected ? 1.12f : 1.0F)
                                        );
                                        set.setDuration(350);
                                        set.start();
                                    }
                                } : null)
                                .setSelectAnimListener(cb_selected_anim.isChecked() ? new OnSelectAnimListener() {
                                    @Override
                                    public long onSelectAnim(View view) {
                                        Animation animation = AnimationUtils.loadAnimation(activity, com.luck.picture.lib.R.anim.ps_anim_modal_in);
                                        view.startAnimation(animation);
                                        return animation.getDuration();
                                    }
                                } : null)
                                //.setQueryOnlyMimeType(PictureMimeType.ofGIF())
                                .isMaxSelectEnabledMask(cbEnabledMask.isChecked())
                                .isDirectReturnSingle(cb_single_back.isChecked())
                                .setMaxSelectNum(maxSelectNum)
                                .setMaxVideoSelectNum(maxSelectVideoNum)
                                .setRecyclerAnimationMode(animationMode)
                                .isGif(cb_isGif.isChecked())
                                .setSelectedData(mAdapter.getData());
                        forSelectResult(activity,  mAdapter, selectionModel, resultMode, launcherResult);
                    }
                } else {
                    // 单独拍照
                    PictureSelectionCameraModel cameraModel = PictureSelector.create(activity)
                            .openCamera(chooseMode)
                            .setCameraInterceptListener(
                                    MeOnHelp.getInstance().getCustomCameraEvent(
                                            activity, cb_custom_camera, tag, cb_camera_focus,
                                            cb_camera_zoom, cb_custom_sandbox, cb_permission_desc
                                    )
                            )
                            .setRecordAudioInterceptListener(new MeOnRecordAudioInterceptListener(tag))
                            .setCropEngine(MeOnHelp.getInstance().getCropFileEngine(
                                    activity, cb_crop, cb_hide, cb_styleCrop, cb_showCropFrame, cb_showCropGrid,
                                    cb_crop_circular, aspect_ratio_x, aspect_ratio_y, cb_not_gif,
                                    cb_skip_not_gif, selectorStyle
                            ))
                            .setCompressEngine(MeOnHelp.getInstance().getCompressFileEngine(activity, cb_compress))
                            .setSelectLimitTipsListener(new MeOnSelectLimitTipsListener())
                            .setAddBitmapWatermarkListener(
                                    MeOnHelp.getInstance().getAddBitmapWatermarkListener(
                                            activity, cb_watermark
                                    )
                            )
                            .setVideoThumbnailListener(
                                    MeOnHelp.getInstance().getVideoThumbnailEventListener(
                                            activity, cb_video_thumbnails
                                    )
                            )
                            .setCustomLoadingListener(
                                    MeOnHelp.getInstance().getCustomLoadingListener(cb_custom_loading)
                            )
                            .setLanguage(language)
                            .setSandboxFileEngine(new MeSandboxFileEngine())
                            .isOriginalControl(cb_original.isChecked())
                            .setPermissionDescriptionListener(
                                    MeOnHelp.getInstance().getPermissionDescriptionListener(
                                            cb_permission_desc, tag
                                    )
                            )
                            .setOutputAudioDir(
                                    MeOnHelp.getInstance().getSandboxAudioOutputPath(activity, cb_custom_sandbox)
                            )
                            .setSelectedData(mAdapter.getData());
                    forOnlyCameraResult(activity,  mAdapter, cameraModel, cb_attach_camera_mode, resultMode, launcherResult);
                }
            }
        });

        mAdapter.setItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(RecyclerView.ViewHolder holder, int position, View v) {
                int itemViewType = holder.getItemViewType();
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    mItemTouchHelper.startDrag(holder);
                }
            }
        });
    }

    private void forSystemResult(
            FragmentActivity activity, PictureSelectionSystemModel model, CheckBox cb_attach_system_mode,
            int resultMode, GridImageAdapter mAdapter, ActivityResultLauncher<Intent> launcherResult
    ) {
        if (cb_attach_system_mode.isChecked()) {
            switch (resultMode) {
                case MainActivity.ACTIVITY_RESULT:
                    model.forSystemResultActivity(PictureConfig.REQUEST_CAMERA);
                    break;
                case MainActivity.CALLBACK_RESULT:
                    model.forSystemResultActivity(new MeOnResultCallbackListener(activity, mAdapter));
                    break;
                default:
                    model.forSystemResultActivity(launcherResult);
                    break;
            }
        } else {
            if (resultMode == MainActivity.CALLBACK_RESULT) {
                model.forSystemResult(new MeOnResultCallbackListener(activity, mAdapter));
            } else {
                model.forSystemResult();
            }
        }
    }

    private void forSelectResult(
            FragmentActivity activity,  GridImageAdapter mAdapter, PictureSelectionModel model,
            int resultMode, ActivityResultLauncher<Intent> launcherResult) {
        switch (resultMode) {
            case MainActivity.ACTIVITY_RESULT:
                model.forResult(PictureConfig.CHOOSE_REQUEST);
                break;
            case MainActivity.CALLBACK_RESULT:
                model.forResult(new MeOnResultCallbackListener(activity, mAdapter));
                break;
            default:
                model.forResult(launcherResult);
                break;
        }
    }

    private void forOnlyCameraResult(
            FragmentActivity activity,  GridImageAdapter mAdapter, PictureSelectionCameraModel model,
            CheckBox cb_attach_camera_mode, int resultMode, ActivityResultLauncher<Intent> launcherResult
    ) {
        if (cb_attach_camera_mode.isChecked()) {
            switch (resultMode) {
                case MainActivity.ACTIVITY_RESULT:
                    model.forResultActivity(PictureConfig.REQUEST_CAMERA);
                    break;
                case MainActivity.CALLBACK_RESULT:
                    model.forResultActivity(new MeOnResultCallbackListener(activity, mAdapter));
                    break;
                default:
                    model.forResultActivity(launcherResult);
                    break;
            }
        } else {
            if (resultMode == MainActivity.CALLBACK_RESULT) {
                model.forResult(new MeOnResultCallbackListener(activity, mAdapter));
            } else {
                model.forResult();
            }
        }
    }
}
