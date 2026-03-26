package listener;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.MediaUtils;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.pictureselector.MeOnHelp;
import com.luck.pictureselector.adapter.GridImageAdapter;

import java.util.ArrayList;

public class MeOnResultCallbackListener implements OnResultCallbackListener<LocalMedia> {
    private final String TAG = "";
    public FragmentActivity activity;
    public GridImageAdapter mAdapter;

    public MeOnResultCallbackListener(FragmentActivity activity, GridImageAdapter mAdapter) {
        this.activity = activity;
        this.mAdapter = mAdapter;
    }

    @Override
    public void onResult(ArrayList<LocalMedia> result) {
        MeOnHelp.getInstance().analyticalSelectResults(activity, result, mAdapter);
    }

    @Override
    public void onCancel() {
        Log.i("TAG", "PictureSelector Cancel");
    }
}
