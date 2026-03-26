package listener;

import android.content.Context;

import com.luck.picture.lib.engine.ExtendLoaderEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.LocalMediaFolder;
import com.luck.picture.lib.interfaces.OnQueryAlbumListener;
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener;
import com.luck.picture.lib.interfaces.OnQueryDataResultListener;
import com.luck.picture.lib.loader.SandboxFileLoader;
import com.luck.pictureselector.MeOnHelp;

import java.util.ArrayList;
import java.util.List;

public class MeExtendLoaderEngine implements ExtendLoaderEngine {

    @Override
    public void loadAllAlbumData(Context context,
                                 OnQueryAllAlbumListener<LocalMediaFolder> query) {
        LocalMediaFolder folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(
                        context, MeOnHelp.getInstance().getSandboxPath(context)
                );
        List<LocalMediaFolder> folders = new ArrayList<>();
        folders.add(folder);
        query.onComplete(folders);
    }

    @Override
    public void loadOnlyInAppDirAllMediaData(Context context,
                                             OnQueryAlbumListener<LocalMediaFolder> query) {
        LocalMediaFolder folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(
                        context, MeOnHelp.getInstance().getSandboxPath(context)
                );
        query.onComplete(folder);
    }

    @Override
    public void loadFirstPageMediaData(Context context, long bucketId, int page, int pageSize, OnQueryDataResultListener<LocalMedia> query) {
        LocalMediaFolder folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(
                        context, MeOnHelp.getInstance().getSandboxPath(context)
                );
        query.onComplete(folder.getData(), false);
    }

    @Override
    public void loadMoreMediaData(Context context, long bucketId, int page, int limit, int pageSize, OnQueryDataResultListener<LocalMedia> query) {

    }
}
