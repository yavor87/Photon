package net.rubisoft.photon;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import com.squareup.picasso.Picasso;

import net.rubisoft.photon.content.PhotoItem;

import java.util.List;

final class ImageGridViewAdapter extends BaseAdapter {
    public ImageGridViewAdapter(Context context, List<PhotoItem> items) {
        mContext = context;
        mItems = items;
    }

    private Context mContext;
    private List<PhotoItem> mItems;


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(mContext);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        PhotoItem item = getItem(position);
        Uri url = item.getThumbnailUri() != null ? item.getThumbnailUri() : item.getFullImageUri();

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(mContext) //
                .load(url) //
                .fit()
//                .resize(500, 500)
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return mItems.size();
    }

    @Override public PhotoItem getItem(int position) {
        return mItems.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}