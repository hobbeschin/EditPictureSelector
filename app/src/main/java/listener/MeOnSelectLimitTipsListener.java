package listener;

import android.content.Context;

import androidx.annotation.Nullable;

import com.luck.picture.lib.config.SelectLimitType;
import com.luck.picture.lib.config.SelectorConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnSelectLimitTipsListener;
import com.luck.picture.lib.utils.ToastUtils;

public class MeOnSelectLimitTipsListener implements OnSelectLimitTipsListener {

    @Override
    public boolean onSelectLimitTips(Context context, @Nullable LocalMedia media, SelectorConfig config, int limitType) {
        if (limitType == SelectLimitType.SELECT_MIN_SELECT_LIMIT) {
            ToastUtils.showToast(context, "图片最少不能低于" + config.minSelectNum + "张");
            return true;
        } else if (limitType == SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT) {
            ToastUtils.showToast(context, "视频最少不能低于" + config.minVideoSelectNum + "个");
            return true;
        } else if (limitType == SelectLimitType.SELECT_MIN_AUDIO_SELECT_LIMIT) {
            ToastUtils.showToast(context, "音频最少不能低于" + config.minAudioSelectNum + "个");
            return true;
        }
        return false;
    }
}
