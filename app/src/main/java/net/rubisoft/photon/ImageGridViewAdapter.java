package net.rubisoft.photon;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class ImageGridViewAdapter extends CursorAdapter {
    public ImageGridViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        SquaredImageView view = new SquaredImageView(context);
        view.setScaleType(CENTER_CROP);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get the image URL for the current position.
        String url = cursor.getString(PhotoListFragment.COL_THUMB_URI);
        if (url == null || url.isEmpty())
            url = cursor.getString(PhotoListFragment.COL_IMAGE_URI);

        // Trigger the download of the URL asynchronously into the image view.
        Glide.with(context) //
                .load(url) //
                .fitCenter()
//                .resize(500, 500)
                .into((ImageView) view);
    }
}