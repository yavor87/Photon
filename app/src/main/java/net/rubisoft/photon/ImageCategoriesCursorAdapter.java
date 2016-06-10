package net.rubisoft.photon;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ImageCategoriesCursorAdapter extends CursorAdapter {
    public ImageCategoriesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new TextView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view).setText(cursor.getString(ImageFragment.COL_CATEGORI_NAME));
    }
}
