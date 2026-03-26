package listener;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.pictureselector.adapter.GridImageAdapter;

public class MyExternalPreviewEventListener implements OnExternalPreviewEventListener {
    public GridImageAdapter mAdapter;

    public MyExternalPreviewEventListener(GridImageAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public void onPreviewDelete(int position) {
        mAdapter.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public boolean onLongPressDownload(Context context, LocalMedia media) {
        return false;
    }
}
