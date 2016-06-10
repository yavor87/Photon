package net.rubisoft.photon;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageCategoriesCursorAdapter extends CursorAdapter {
    public ImageCategoriesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.image_category_information, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.category_name);
        textView.setText(cursor.getString(ImageFragment.COL_CATEGORY_NAME));
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.category_confidence);
        progressBar.setProgress((int)(cursor.getFloat(ImageFragment.COL_CATEGORY_CONFIDENCE) * 100));
    }
}
