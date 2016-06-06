package net.rubisoft.photon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import com.squareup.picasso.Picasso;

import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.data.ImageProvider;

import java.util.List;

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
        String url = cursor.getString(PhotoListFragment.COL_IMAGE_URI);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .fit()
//                .resize(500, 500)
                .into((ImageView) view);
    }
}