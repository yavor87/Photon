package net.rubisoft.photon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.rubisoft.photon.content.ImageContract;

/**
 * A fragment representing a list of Photos.
 */
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoListFragment() {
    }

    private static final int LOADER_ID = 0;

    private static final String[] IMAGE_COLUMNS = {
            ImageContract.ImageEntry._ID,
            ImageContract.ImageEntry.THUMBNAIL_URI,
            ImageContract.ImageEntry.IMAGE_URI
    };
    static final int COL_IMAGE_ID = 0;
    static final int COL_THUMB_URI = 1;
    static final int COL_IMAGE_URI = 2;

    private OnImageSelectedListener mListener;
    private ImageGridViewAdapter mImageAdapter;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 101;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        } else {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnImageSelectedListener) {
            mListener = (OnImageSelectedListener) context;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST && grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.list);
        mImageAdapter = new ImageGridViewAdapter(getContext(), null, 0);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (mListener != null)
                    mListener.onItemSelected(cursor.getInt(COL_IMAGE_ID));
            }
        });
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        return new CursorLoader(context, ImageContract.ImageEntry.CONTENT_URI, IMAGE_COLUMNS, null, null, ImageContract.ImageEntry.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mImageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }

    public interface OnImageSelectedListener {
        void onItemSelected(int imageId);
    }
}
