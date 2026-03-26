package com.luck.pictureselector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.luck.picture.lib.PictureSelectorPreviewFragment;
import com.luck.picture.lib.animators.AnimationType;
import com.luck.picture.lib.basic.IBridgePictureBehavior;
import com.luck.picture.lib.basic.IBridgeViewLifecycle;
import com.luck.picture.lib.basic.PictureCommonFragment;
import com.luck.picture.lib.basic.PictureSelectionCameraModel;
import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelectionSystemModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.InjectResourceSource;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.engine.VideoPlayerEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnGridItemSelectAnimListener;
import com.luck.picture.lib.interfaces.OnInjectActivityPreviewListener;
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener;
import com.luck.picture.lib.interfaces.OnQueryFilterListener;
import com.luck.picture.lib.interfaces.OnSelectAnimListener;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DensityUtil;
import com.luck.picture.lib.utils.ValueOf;
import com.luck.pictureselector.adapter.GridImageAdapter;
import com.luck.pictureselector.listener.DragListener;
import com.luck.pictureselector.listener.OnItemLongClickListener;
import com.yalantis.ucrop.model.AspectRatio;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import listener.MeOnRecordAudioInterceptListener;
import listener.MeOnResultCallbackListener;
import listener.MeOnSelectLimitTipsListener;
import listener.MeSandboxFileEngine;
import listener.MyExternalPreviewEventListener;
import listener.RecyclerItemTouchHelper;

/**
 * @author：luck
 * @data：2019/12/20 晚上 23:12
 * @描述: Demo
 */

public class MainActivity extends AppCompatActivity implements IBridgePictureBehavior, View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = "PictureSelectorTag";
    private final static String TAG_EXPLAIN_VIEW = "TAG_EXPLAIN_VIEW";
    public final static int ACTIVITY_RESULT = 1;
    public final static int CALLBACK_RESULT = 2;
    public final static int LAUNCHER_RESULT = 3;
    private GridImageAdapter mAdapter;
    private int maxSelectNum = 9;
    private int maxSelectVideoNum = 1;
    private TextView tv_select_num;
    private TextView tv_select_video_num;
    private TextView tv_original_tips;
    private TextView tvDeleteText;
    private RadioGroup rgb_crop;
    private LinearLayout llSelectVideoSize;
    private int aspect_ratio_x = -1, aspect_ratio_y = -1;
    private CheckBox cb_voice, cb_choose_mode, cb_isCamera, cb_isGif,
            cb_preview_img, cb_preview_video, cb_crop, cb_compress,
            cb_mode, cb_hide, cb_crop_circular, cb_styleCrop, cb_showCropGrid,
            cb_showCropFrame, cb_preview_audio, cb_original, cb_single_back,
            cb_custom_camera, cbPage, cbEnabledMask, cbEditor, cb_custom_sandbox, cb_only_dir,
            cb_preview_full, cb_preview_scale, cb_inject_layout, cb_time_axis, cb_WithImageVideo,
            cb_system_album, cb_fast_select, cb_skip_not_gif, cb_not_gif, cb_attach_camera_mode,
            cb_attach_system_mode, cb_camera_zoom, cb_camera_focus, cb_query_sort_order, cb_watermark,
            cb_custom_preview, cb_permission_desc, cb_video_thumbnails, cb_auto_video, cb_selected_anim,
            cb_video_resume, cb_custom_loading;
    private RecyclerView mRecyclerView;
    private int chooseMode = SelectMimeType.ofAll();
    private boolean isHasLiftDelete;
    private boolean needScaleBig = true;
    private boolean needScaleSmall = false;
    private boolean isUseSystemPlayer = false;
    private int language = LanguageConfig.UNKNOWN_LANGUAGE;
    private int x = 0, y = 0;
    private int animationMode = AnimationType.DEFAULT_ANIMATION;
    private PictureSelectorStyle selectorStyle;
    private final List<LocalMedia> mData = new ArrayList<>();
    private ActivityResultLauncher<Intent> launcherResult;
    private int resultMode = LAUNCHER_RESULT;
    private ImageEngine imageEngine;
    private VideoPlayerEngine videoPlayerEngine;
    private FragmentActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        selectorStyle = new PictureSelectorStyle();

        findViewId();

        radioGroupClickListener();

        imageViewClickListener();

        checkBoxClickListener();
        createItemTouchHelper();
        initRecyclerView(savedInstanceState);

        // 注册需要写在onCreate或Fragment onAttach里，否则会报java.lang.IllegalStateException异常
        launcherResult = MeOnHelp.getInstance().createActivityResultLauncher(activity, mAdapter);

        initAttachSystem();

        imageEngine = GlideEngine.createGlideEngine();

        MeOnHelp.getInstance().mAdapterClickListener(
                activity, mAdapter, mItemTouchHelper,
                imageEngine, videoPlayerEngine, selectorStyle,
                language, cb_auto_video, cb_preview_full, cb_video_resume,
                isUseSystemPlayer, cb_custom_loading, chooseMode, cb_preview_scale,
                mRecyclerView, cb_custom_preview, cb_mode, cb_system_album,
                cb_choose_mode, cb_compress, cb_crop, cb_hide, cb_styleCrop,
                cb_showCropFrame, cb_showCropGrid, cb_crop_circular, aspect_ratio_x,
                aspect_ratio_y, cb_not_gif, cb_skip_not_gif, cb_watermark,
                cb_video_thumbnails, cb_original, cb_permission_desc, TAG_EXPLAIN_VIEW,
                cb_attach_system_mode, resultMode, launcherResult,
                cb_custom_camera, cb_camera_focus, cb_camera_zoom, cb_custom_sandbox,
                cbEditor, cb_inject_layout, cb_query_sort_order, cb_time_axis,
                cb_only_dir, cbPage, cb_isCamera, cb_voice, cb_fast_select,
                cb_WithImageVideo, cb_preview_img, cb_preview_video, cb_preview_audio,
                cb_selected_anim, cbEnabledMask, cb_single_back, maxSelectNum,
                maxSelectVideoNum, animationMode, cb_isGif, cb_attach_camera_mode
        );
        // 清除缓存
//        clearCache();
    }

    private ItemTouchHelper mItemTouchHelper;

    private void createItemTouchHelper() {
        final RecyclerItemTouchHelper itemTouchHelper = new RecyclerItemTouchHelper(
                mAdapter, needScaleBig, needScaleSmall, tvDeleteText, mDragListener, isHasLiftDelete
        );
        mItemTouchHelper = new ItemTouchHelper(itemTouchHelper.callback);
    }

    private void findViewId() {
        mRecyclerView = findViewById(R.id.recycler);

        tv_select_num = findViewById(R.id.tv_select_num);
        tv_select_video_num = findViewById(R.id.tv_select_video_num);
        llSelectVideoSize = findViewById(R.id.ll_select_video_size);
        tvDeleteText = findViewById(R.id.tv_delete_text);
        tv_original_tips = findViewById(R.id.tv_original_tips);
        rgb_crop = findViewById(R.id.rgb_crop);
        cb_video_thumbnails = findViewById(R.id.cb_video_thumbnails);

        cb_voice = findViewById(R.id.cb_voice);
        cb_choose_mode = findViewById(R.id.cb_choose_mode);
        cb_video_resume = findViewById(R.id.cb_video_resume);
        cb_isCamera = findViewById(R.id.cb_isCamera);
        cb_isGif = findViewById(R.id.cb_isGif);
        cb_watermark = findViewById(R.id.cb_watermark);
        cb_WithImageVideo = findViewById(R.id.cbWithImageVideo);
        cb_system_album = findViewById(R.id.cb_system_album);
        cb_fast_select = findViewById(R.id.cb_fast_select);
        cb_preview_full = findViewById(R.id.cb_preview_full);
        cb_preview_scale = findViewById(R.id.cb_preview_scale);
        cb_inject_layout = findViewById(R.id.cb_inject_layout);
        cb_preview_img = findViewById(R.id.cb_preview_img);
        cb_camera_zoom = findViewById(R.id.cb_camera_zoom);
        cb_camera_focus = findViewById(R.id.cb_camera_focus);
        cb_query_sort_order = findViewById(R.id.cb_query_sort_order);
        cb_custom_preview = findViewById(R.id.cb_custom_preview);
        cb_permission_desc = findViewById(R.id.cb_permission_desc);
        cb_preview_video = findViewById(R.id.cb_preview_video);
        cb_auto_video = findViewById(R.id.cb_auto_video);
        cb_selected_anim = findViewById(R.id.cb_selected_anim);
        cb_time_axis = findViewById(R.id.cb_time_axis);
        cb_custom_loading = findViewById(R.id.cb_custom_loading);
        cb_crop = findViewById(R.id.cb_crop);
        cbPage = findViewById(R.id.cbPage);
        cbEditor = findViewById(R.id.cb_editor);
        cbEnabledMask = findViewById(R.id.cbEnabledMask);
        cb_styleCrop = findViewById(R.id.cb_styleCrop);
        cb_compress = findViewById(R.id.cb_compress);
        cb_mode = findViewById(R.id.cb_mode);
        cb_custom_sandbox = findViewById(R.id.cb_custom_sandbox);
        cb_only_dir = findViewById(R.id.cb_only_dir);
        cb_showCropGrid = findViewById(R.id.cb_showCropGrid);
        cb_showCropFrame = findViewById(R.id.cb_showCropFrame);
        cb_preview_audio = findViewById(R.id.cb_preview_audio);
        cb_original = findViewById(R.id.cb_original);
        cb_single_back = findViewById(R.id.cb_single_back);
        cb_custom_camera = findViewById(R.id.cb_custom_camera);
        cb_hide = findViewById(R.id.cb_hide);
        cb_not_gif = findViewById(R.id.cb_not_gif);
        cb_skip_not_gif = findViewById(R.id.cb_skip_not_gif);
        cb_crop_circular = findViewById(R.id.cb_crop_circular);
        cb_attach_camera_mode = findViewById(R.id.cb_attach_camera_mode);
        cb_attach_system_mode = findViewById(R.id.cb_attach_system_mode);

        rbAll = findViewById(R.id.rb_all);
        rbAll.getId();
    }

    private RadioButton rbAll;

    private void imageViewClickListener() {
        ImageView left_back = findViewById(R.id.left_back);
        ImageView minus = findViewById(R.id.minus);
        ImageView plus = findViewById(R.id.plus);
        ImageView videoMinus = findViewById(R.id.video_minus);
        ImageView videoPlus = findViewById(R.id.video_plus);

        left_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxSelectNum > 1) {
                    maxSelectNum--;
                }
                tv_select_num.setText(String.valueOf(maxSelectNum));
                mAdapter.setSelectMax(maxSelectNum + maxSelectVideoNum);
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSelectNum++;
                tv_select_num.setText(String.valueOf(maxSelectNum));
                mAdapter.setSelectMax(maxSelectNum + maxSelectVideoNum);
            }
        });
        videoMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxSelectVideoNum > 1) {
                    maxSelectVideoNum--;
                }
                tv_select_video_num.setText(String.valueOf(maxSelectVideoNum));
                mAdapter.setSelectMax(maxSelectVideoNum + maxSelectNum);
            }
        });
        videoPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSelectVideoNum++;
                tv_select_video_num.setText(String.valueOf(maxSelectVideoNum));
                mAdapter.setSelectMax(maxSelectVideoNum + maxSelectNum);
            }
        });
    }

    private void radioGroupClickListener() {
        RadioGroup rgb_video_player = findViewById(R.id.rgb_video_player);
        RadioGroup rgb_result = findViewById(R.id.rgb_result);
        RadioGroup rgb_style = findViewById(R.id.rgb_style);
        RadioGroup rgb_animation = findViewById(R.id.rgb_animation);
        RadioGroup rgb_list_anim = findViewById(R.id.rgb_list_anim);
        RadioGroup rgb_photo_mode = findViewById(R.id.rgb_photo_mode);
        RadioGroup rgb_language = findViewById(R.id.rgb_language);
        RadioGroup rgb_engine = findViewById(R.id.rgb_engine);

        rgb_video_player.setOnCheckedChangeListener(this);
        rgb_result.setOnCheckedChangeListener(this);
        rgb_style.setOnCheckedChangeListener(this);
        rgb_animation.setOnCheckedChangeListener(this);
        rgb_list_anim.setOnCheckedChangeListener(this);
        rgb_photo_mode.setOnCheckedChangeListener(this);
        rgb_language.setOnCheckedChangeListener(this);
        rgb_engine.setOnCheckedChangeListener(this);
        rgb_crop.setOnCheckedChangeListener(this);
    }

    private void checkBoxClickListener() {
        cb_mode.setOnCheckedChangeListener(this);
        cb_custom_camera.setOnCheckedChangeListener(this);
        cb_crop.setOnCheckedChangeListener(this);
        cb_only_dir.setOnCheckedChangeListener(this);
        cb_custom_sandbox.setOnCheckedChangeListener(this);
        cb_crop_circular.setOnCheckedChangeListener(this);
        cb_attach_camera_mode.setOnCheckedChangeListener(this);
        cb_attach_system_mode.setOnCheckedChangeListener(this);
        cb_system_album.setOnCheckedChangeListener(this);
        cb_compress.setOnCheckedChangeListener(this);
        cb_not_gif.setOnCheckedChangeListener(this);
        cb_skip_not_gif.setOnCheckedChangeListener(this);

        cb_original.setOnCheckedChangeListener((buttonView, isChecked) ->
                tv_original_tips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        cb_choose_mode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cb_single_back.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            cb_single_back.setChecked(!isChecked && cb_single_back.isChecked());
        });
    }

    private void initRecyclerView(Bundle savedInstanceState) {
        FullyGridLayoutManager manager = new FullyGridLayoutManager(this,
                4, GridLayoutManager.VERTICAL, false
        );

        mRecyclerView.setLayoutManager(manager);
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        if (itemAnimator != null) {
            ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
        }
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4,
                DensityUtil.dip2px(this, 8), false)
        );
        mAdapter = new GridImageAdapter(getContext(), mData);
        mAdapter.setSelectMax(maxSelectNum + maxSelectVideoNum);
        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList("selectorList") != null) {
            mData.clear();
            mData.addAll(savedInstanceState.getParcelableArrayList("selectorList"));
        }

        // 绑定拖拽事件
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void initAttachSystem() {
        tv_select_num.setText(ValueOf.toString(maxSelectNum));
        tv_select_video_num.setText(ValueOf.toString(maxSelectVideoNum));

        String systemHigh = " (仅支持部分api)";
        String systemTips = "使用系统图库" + systemHigh;
        int startIndex = systemTips.indexOf(systemHigh);
        int endOf = startIndex + systemHigh.length();
        SpannableStringBuilder builder = new SpannableStringBuilder(systemTips);
        builder.setSpan(
                new AbsoluteSizeSpan(DensityUtil.dip2px(getContext(), 12)), startIndex, endOf,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        );
        builder.setSpan(
                new ForegroundColorSpan(0xFFCC0000), startIndex, endOf,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        );
        cb_system_album.setText(builder);

        String cameraHigh = " (默认fragment)";
        String cameraTips = "使用Activity承载Camera相机" + cameraHigh;
        int startIndex2 = cameraTips.indexOf(cameraHigh);
        int endOf2 = startIndex2 + cameraHigh.length();
        SpannableStringBuilder builder2 = new SpannableStringBuilder(cameraTips);
        builder2.setSpan(
                new AbsoluteSizeSpan(DensityUtil.dip2px(getContext(), 12)), startIndex2, endOf2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        );
        builder2.setSpan(
                new ForegroundColorSpan(0xFFCC0000), startIndex2, endOf2,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        cb_attach_camera_mode.setText(builder2
        );


        String systemAlbumHigh = " (默认fragment)";
        String systemAlbumTips = "使用Activity承载系统相册" + systemAlbumHigh;
        int startIndex3 = systemAlbumTips.indexOf(systemAlbumHigh);
        int endOf3 = startIndex3 + systemAlbumHigh.length();
        SpannableStringBuilder builder3 = new SpannableStringBuilder(systemAlbumTips);
        builder3.setSpan(
                new AbsoluteSizeSpan(DensityUtil.dip2px(getContext(), 12)), startIndex3, endOf3,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        );
        builder3.setSpan(
                new ForegroundColorSpan(0xFFCC0000), startIndex3, endOf3,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        );
        cb_attach_system_mode.setText(builder3);
    }

    private String[] getNotSupportCrop() {
        if (cb_skip_not_gif.isChecked()) {
            return new String[]{PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()};
        }
        return null;
    }

    private final DragListener mDragListener = new DragListener() {
        @Override
        public void deleteState(boolean isDelete) {
            if (isDelete) {
                if (!TextUtils.equals(getString(R.string.app_let_go_drag_delete), tvDeleteText.getText())) {
                    tvDeleteText.setText(getString(R.string.app_let_go_drag_delete));
                    tvDeleteText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_dump_delete, 0, 0);
                }
            } else {
                if (!TextUtils.equals(getString(R.string.app_drag_delete), tvDeleteText.getText())) {
                    tvDeleteText.setText(getString(R.string.app_drag_delete));
                    tvDeleteText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_normal_delete, 0, 0);
                }
            }
        }

        @Override
        public void dragState(boolean isStart) {
            if (isStart) {
                if (tvDeleteText.getAlpha() == 0F) {
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvDeleteText, "alpha", 0F, 1F);
                    alphaAnimator.setInterpolator(new LinearInterpolator());
                    alphaAnimator.setDuration(120);
                    alphaAnimator.start();
                }
            } else {
                if (tvDeleteText.getAlpha() == 1F) {
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvDeleteText, "alpha", 1F, 0F);
                    alphaAnimator.setInterpolator(new LinearInterpolator());
                    alphaAnimator.setDuration(120);
                    alphaAnimator.start();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.rb_all) {
            rb_all();
        } else if (checkedId == R.id.rb_image) {
            rb_image();
        } else if (checkedId == R.id.rb_video) {
            rb_video();
        } else if (checkedId == R.id.rb_audio) {
            chooseMode = SelectMimeType.ofAudio();
            cb_preview_audio.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.rb_glide) {
            imageEngine = GlideEngine.createGlideEngine();
        } else if (checkedId == R.id.rb_picasso) {
            imageEngine = PicassoEngine.createPicassoEngine();
        } else if (checkedId == R.id.rb_coil) {
            imageEngine = new CoilEngine();
        } else if (checkedId == R.id.rb_media_player) {
            videoPlayerEngine = null;
            isUseSystemPlayer = false;
        } else if (checkedId == R.id.rb_exo_player) {
            videoPlayerEngine = new ExoPlayerEngine();
            isUseSystemPlayer = false;
        } else if (checkedId == R.id.rb_ijk_player) {
            videoPlayerEngine = new IjkPlayerEngine();
            isUseSystemPlayer = false;
        } else if (checkedId == R.id.rb_system_player) {
            isUseSystemPlayer = true;
        } else if (checkedId == R.id.rb_system) {
            language = LanguageConfig.SYSTEM_LANGUAGE;
        } else if (checkedId == R.id.rb_jpan) {
            language = LanguageConfig.JAPAN;
        } else if (checkedId == R.id.rb_tw) {
            language = LanguageConfig.TRADITIONAL_CHINESE;
        } else if (checkedId == R.id.rb_us) {
            language = LanguageConfig.ENGLISH;
        } else if (checkedId == R.id.rb_ka) {
            language = LanguageConfig.KOREA;
        } else if (checkedId == R.id.rb_de) {
            language = LanguageConfig.GERMANY;
        } else if (checkedId == R.id.rb_fr) {
            language = LanguageConfig.FRANCE;
        } else if (checkedId == R.id.rb_spanish) {
            language = LanguageConfig.SPANISH;
        } else if (checkedId == R.id.rb_portugal) {
            language = LanguageConfig.PORTUGAL;
        } else if (checkedId == R.id.rb_ar) {
            language = LanguageConfig.AR;
        } else if (checkedId == R.id.rb_ru) {
            language = LanguageConfig.RU;
        } else if (checkedId == R.id.rb_cs) {
            language = LanguageConfig.CS;
        } else if (checkedId == R.id.rb_kk) {
            language = LanguageConfig.KK;
        } else if (checkedId == R.id.rb_crop_default) {
            aspect_ratio_x = -1;
            aspect_ratio_y = -1;
        } else if (checkedId == R.id.rb_crop_1to1) {
            aspect_ratio_x = 1;
            aspect_ratio_y = 1;
        } else if (checkedId == R.id.rb_crop_3to4) {
            aspect_ratio_x = 3;
            aspect_ratio_y = 4;
        } else if (checkedId == R.id.rb_crop_3to2) {
            aspect_ratio_x = 3;
            aspect_ratio_y = 2;
        } else if (checkedId == R.id.rb_crop_16to9) {
            aspect_ratio_x = 16;
            aspect_ratio_y = 9;
        } else if (checkedId == R.id.rb_launcher_result) {
            resultMode = 0;
        } else if (checkedId == R.id.rb_activity_result) {
            resultMode = 1;
        } else if (checkedId == R.id.rb_callback_result) {
            resultMode = 2;
        } else if (checkedId == R.id.rb_photo_default_animation) {
            PictureWindowAnimationStyle defaultAnimationStyle = new PictureWindowAnimationStyle();
            defaultAnimationStyle.setActivityEnterAnimation(com.luck.picture.lib.R.anim.ps_anim_enter);
            defaultAnimationStyle.setActivityExitAnimation(com.luck.picture.lib.R.anim.ps_anim_exit);
            selectorStyle.setWindowAnimationStyle(defaultAnimationStyle);
        } else if (checkedId == R.id.rb_photo_up_animation) {
            PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
            animationStyle.setActivityEnterAnimation(com.luck.picture.lib.R.anim.ps_anim_up_in);
            animationStyle.setActivityExitAnimation(com.luck.picture.lib.R.anim.ps_anim_down_out);
            selectorStyle.setWindowAnimationStyle(animationStyle);
        } else if (checkedId == R.id.rb_default_style) {
            selectorStyle = new PictureSelectorStyle();
        } else if (checkedId == R.id.rb_white_style) {
            rb_white_style();
        } else if (checkedId == R.id.rb_num_style) {
            rb_num_style();
        } else if (checkedId == R.id.rb_we_chat_style) {
            rb_we_chat_style();
        } else if (checkedId == R.id.rb_default) {
            animationMode = AnimationType.DEFAULT_ANIMATION;
        } else if (checkedId == R.id.rb_alpha) {
            animationMode = AnimationType.ALPHA_IN_ANIMATION;
        } else if (checkedId == R.id.rb_slide_in) {
            animationMode = AnimationType.SLIDE_IN_BOTTOM_ANIMATION;
        }
    }

    private void rb_all() {
        chooseMode = SelectMimeType.ofAll();
        cb_preview_img.setChecked(true);
        cb_preview_video.setChecked(true);
        cb_isGif.setChecked(false);
        cb_preview_video.setChecked(true);
        cb_preview_img.setChecked(true);
        cb_preview_video.setVisibility(View.VISIBLE);
        cb_preview_img.setVisibility(View.VISIBLE);
        llSelectVideoSize.setVisibility(View.VISIBLE);
        cb_compress.setVisibility(View.VISIBLE);
        cb_crop.setVisibility(View.VISIBLE);
        cb_isGif.setVisibility(View.VISIBLE);
        cb_preview_audio.setVisibility(View.GONE);
    }

    private void rb_image() {
        llSelectVideoSize.setVisibility(View.GONE);
        chooseMode = SelectMimeType.ofImage();
        cb_preview_img.setChecked(true);
        cb_preview_video.setChecked(false);
        cb_isGif.setChecked(false);
        cb_preview_video.setChecked(false);
        cb_preview_video.setVisibility(View.GONE);
        cb_preview_img.setChecked(true);
        cb_preview_audio.setVisibility(View.GONE);
        cb_preview_img.setVisibility(View.VISIBLE);
        cb_compress.setVisibility(View.VISIBLE);
        cb_crop.setVisibility(View.VISIBLE);
        cb_isGif.setVisibility(View.VISIBLE);
    }

    private void rb_video() {
        llSelectVideoSize.setVisibility(View.GONE);
        chooseMode = SelectMimeType.ofVideo();
        cb_preview_img.setChecked(false);
        cb_preview_video.setChecked(true);
        cb_isGif.setChecked(false);
        cb_isGif.setVisibility(View.GONE);
        cb_preview_video.setChecked(true);
        cb_preview_video.setVisibility(View.VISIBLE);
        cb_preview_img.setVisibility(View.GONE);
        cb_preview_img.setChecked(false);
        cb_compress.setVisibility(View.GONE);
        cb_preview_audio.setVisibility(View.GONE);
        cb_crop.setVisibility(View.GONE);
    }

    private void rb_white_style() {
        TitleBarStyle whiteTitleBarStyle = new TitleBarStyle();
        whiteTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        whiteTitleBarStyle.setTitleDrawableRightResource(R.drawable.ic_orange_arrow_down);
        whiteTitleBarStyle.setTitleLeftBackResource(com.luck.picture.lib.R.drawable.ps_ic_black_back);
        whiteTitleBarStyle.setTitleTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_black));
        whiteTitleBarStyle.setTitleCancelTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));
        whiteTitleBarStyle.setDisplayTitleBarLine(true);

        BottomNavBarStyle whiteBottomNavBarStyle = new BottomNavBarStyle();
        whiteBottomNavBarStyle.setBottomNarBarBackgroundColor(Color.parseColor("#EEEEEE"));
        whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));

        whiteBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_9b));
        whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_fa632d));
        whiteBottomNavBarStyle.setCompleteCountTips(false);
        whiteBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));
        whiteBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));

        SelectMainStyle selectMainStyle = new SelectMainStyle();
        selectMainStyle.setStatusBarColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        selectMainStyle.setDarkStatusBarBlack(true);
        selectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_9b));
        selectMainStyle.setSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_fa632d));
        selectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_white_preview_selector);
        selectMainStyle.setSelectBackground(com.luck.picture.lib.R.drawable.ps_checkbox_selector);
        selectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_done_front_num);
        selectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));

        selectorStyle.setTitleBarStyle(whiteTitleBarStyle);
        selectorStyle.setBottomBarStyle(whiteBottomNavBarStyle);
        selectorStyle.setSelectMainStyle(selectMainStyle);
    }

    private void rb_num_style() {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_blue));

        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));


        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);
        numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector);

        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_completed);

        selectorStyle.setTitleBarStyle(blueTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
        selectorStyle.setSelectMainStyle(numberBlueSelectMainStyle);
    }

    private void rb_we_chat_style() {
        // 主体风格
        SelectMainStyle numberSelectMainStyle = new SelectMainStyle();
        numberSelectMainStyle.setSelectNumberStyle(true);
        numberSelectMainStyle.setPreviewSelectNumberStyle(false);
        numberSelectMainStyle.setPreviewDisplaySelectGallery(true);
        numberSelectMainStyle.setSelectBackground(com.luck.picture.lib.R.drawable.ps_default_num_selector);
        numberSelectMainStyle.setPreviewSelectBackground(com.luck.picture.lib.R.drawable.ps_preview_checkbox_selector);
        numberSelectMainStyle.setSelectNormalBackgroundResources(com.luck.picture.lib.R.drawable.ps_select_complete_normal_bg);
        numberSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_53575e));
        numberSelectMainStyle.setSelectNormalText(com.luck.picture.lib.R.string.ps_send);
        numberSelectMainStyle.setAdapterPreviewGalleryBackgroundResource(com.luck.picture.lib.R.drawable.ps_preview_gallery_bg);
        numberSelectMainStyle.setAdapterPreviewGalleryItemSize(DensityUtil.dip2px(getContext(), 52));
        numberSelectMainStyle.setPreviewSelectText(com.luck.picture.lib.R.string.ps_select);
        numberSelectMainStyle.setPreviewSelectTextSize(14);
        numberSelectMainStyle.setPreviewSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        numberSelectMainStyle.setPreviewSelectMarginRight(DensityUtil.dip2px(getContext(), 6));
        numberSelectMainStyle.setSelectBackgroundResources(com.luck.picture.lib.R.drawable.ps_select_complete_bg);
        numberSelectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_send_num);
        numberSelectMainStyle.setSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));
        numberSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_black));
        numberSelectMainStyle.setCompleteSelectRelativeTop(true);
        numberSelectMainStyle.setPreviewSelectRelativeBottom(true);
        numberSelectMainStyle.setAdapterItemIncludeEdge(false);

        // 头部TitleBar 风格
        TitleBarStyle numberTitleBarStyle = new TitleBarStyle();
        numberTitleBarStyle.setHideCancelButton(true);
        numberTitleBarStyle.setAlbumTitleRelativeLeft(true);
        if (cb_only_dir.isChecked()) {
            numberTitleBarStyle.setTitleAlbumBackgroundResource(R.drawable.ps_demo_only_album_bg);
        } else {
            numberTitleBarStyle.setTitleAlbumBackgroundResource(com.luck.picture.lib.R.drawable.ps_album_bg);
        }
        numberTitleBarStyle.setTitleDrawableRightResource(com.luck.picture.lib.R.drawable.ps_ic_grey_arrow);
        numberTitleBarStyle.setPreviewTitleLeftBackResource(com.luck.picture.lib.R.drawable.ps_ic_normal_back);

        // 底部NavBar 风格
        BottomNavBarStyle numberBottomNavBarStyle = new BottomNavBarStyle();
        numberBottomNavBarStyle.setBottomPreviewNarBarBackgroundColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_half_grey));
        numberBottomNavBarStyle.setBottomPreviewNormalText(com.luck.picture.lib.R.string.ps_preview);
        numberBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_9b));
        numberBottomNavBarStyle.setBottomPreviewNormalTextSize(16);
        numberBottomNavBarStyle.setCompleteCountTips(false);
        numberBottomNavBarStyle.setBottomPreviewSelectText(com.luck.picture.lib.R.string.ps_preview_num);
        numberBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_white));


        selectorStyle.setTitleBarStyle(numberTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBottomNavBarStyle);
        selectorStyle.setSelectMainStyle(numberSelectMainStyle);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_crop) {
            rgb_crop.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_hide.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_crop_circular.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_styleCrop.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_showCropFrame.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_showCropGrid.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_skip_not_gif.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_not_gif.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (buttonView.getId() == R.id.cb_custom_sandbox) {
            cb_only_dir.setChecked(isChecked);
        } else if (buttonView.getId() == R.id.cb_only_dir) {
            cb_custom_sandbox.setChecked(isChecked);
        } else if (buttonView.getId() == R.id.cb_skip_not_gif) {
            cb_not_gif.setChecked(false);
            cb_skip_not_gif.setChecked(isChecked);
        } else if (buttonView.getId() == R.id.cb_not_gif) {
            cb_skip_not_gif.setChecked(false);
            cb_not_gif.setChecked(isChecked);
        } else if (buttonView.getId() == R.id.cb_mode) {
            cb_attach_camera_mode.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        } else if (buttonView.getId() == R.id.cb_system_album) {
            cb_attach_system_mode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (buttonView.getId() == R.id.cb_custom_camera) {
            cb_camera_zoom.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            cb_camera_focus.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
            } else {
                cb_camera_zoom.setChecked(false);
                cb_camera_focus.setChecked(false);
            }
        } else if (buttonView.getId() == R.id.cb_crop_circular) {
            if (isChecked) {
                x = aspect_ratio_x;
                y = aspect_ratio_y;
                aspect_ratio_x = 1;
                aspect_ratio_y = 1;
            } else {
                aspect_ratio_x = x;
                aspect_ratio_y = y;
            }
            rgb_crop.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if (isChecked) {
                cb_showCropFrame.setChecked(false);
                cb_showCropGrid.setChecked(false);
            } else {
                cb_showCropFrame.setChecked(true);
                cb_showCropGrid.setChecked(true);
            }
        }
    }

    @Override
    public void onSelectFinish(@Nullable PictureCommonFragment.SelectorResult result) {
        if (result == null) {
            return;
        }
        if (result.mResultCode == RESULT_OK) {
            ArrayList<LocalMedia> selectorResult = PictureSelector.obtainSelectorList(result.mResultData);
            MeOnHelp.getInstance().analyticalSelectResults(activity, selectorResult, mAdapter);
        } else if (result.mResultCode == RESULT_CANCELED) {
            Log.i(TAG, "onSelectFinish PictureSelector Cancel");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST || requestCode == PictureConfig.REQUEST_CAMERA) {
                ArrayList<LocalMedia> result = PictureSelector.obtainSelectorList(data);
                MeOnHelp.getInstance().analyticalSelectResults(activity, result, mAdapter);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult PictureSelector Cancel");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null && mAdapter.getData() != null && mAdapter.getData().size() > 0) {
            outState.putParcelableArrayList("selectorList",
                    mAdapter.getData());
        }
    }

    public Context getContext() {
        return this;
    }
}
