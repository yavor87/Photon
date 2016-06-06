package net.rubisoft.photon.content;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PhotoItem {
    private int mImageId;
    private Uri mThumbnailUri;
    private Uri mFullImageUri;

    public PhotoItem(int imageId, @Nullable Uri thumbnailUri, @NonNull Uri fullImageUri) {
        mImageId = imageId;
        this.mThumbnailUri = thumbnailUri;
        this.mFullImageUri = fullImageUri;
    }

    public int getImageId() {
        return mImageId;
    }

    @Nullable
    public Uri getThumbnailUri() {
        return mThumbnailUri;
    }

    @NonNull
    public Uri getFullImageUri() {
        return mFullImageUri;
    }
}
