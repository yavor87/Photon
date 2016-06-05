package net.rubisoft.photon;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

import com.squareup.picasso.Picasso;

import net.rubisoft.photon.data.ImageProvider;

import java.util.List;

final class SampleGridViewAdapter extends BaseAdapter {
    private final Context context;
    private final List<Uri> urls;

    public SampleGridViewAdapter(Context context, List<Uri> urls) {
        this.context = context;
        this.urls = urls;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        Uri url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .fit()
//                .resize(500, 500)
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public Uri getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}