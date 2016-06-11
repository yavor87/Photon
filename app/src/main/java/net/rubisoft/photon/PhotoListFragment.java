package net.rubisoft.photon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.service.CacheService;

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

    public static final int ALL_CATEGORIES = -1;

    private static final String LOG_TAG = PhotoListFragment.class.getSimpleName();

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 101;
    private static final int LOADER_ID = 0;
    private static final String ARG_CATEGORY_ID = "category_id";
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
    private int mCategoryId = ALL_CATEGORIES;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mCategoryId = savedInstanceState.getInt(DetailActivity.CATEGORY_ID_EXTRA);
        }

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
            Intent cacheServiceIntent = new Intent(getContext(), CacheService.class);
            cacheServiceIntent.putExtra(CacheService.MODE_KEY, CacheService.MODE_IMAGES);
            getActivity().startService(cacheServiceIntent);

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
                if (mListener != null) {
                    int imageId = cursor.getInt(COL_IMAGE_ID);
                    Log.v(LOG_TAG, "Clicked image " + Integer.toString(imageId));
                    mListener.onItemSelected(imageId, mCategoryId);
                }
            }
        });
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        Uri dataUri;
        if (args != null && args.containsKey(ARG_CATEGORY_ID)) {
            // get images from this category
            dataUri = ImageContract.CategoryEntry.buildImagesForCategoryUri(args.getInt(ARG_CATEGORY_ID));
        } else {
            dataUri = ImageContract.ImageEntry.CONTENT_URI;
        }
        return new CursorLoader(context, dataUri, IMAGE_COLUMNS, null, null, ImageContract.ImageEntry.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mImageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DetailActivity.CATEGORY_ID_EXTRA, mCategoryId);
    }

    public void DisplayCategory(int categoryId) {
        mCategoryId = categoryId;
        Log.v(LOG_TAG, "DisplayCategory " + Integer.toString(categoryId));
        Bundle args = null;
        if (categoryId != ALL_CATEGORIES) {
            args = new Bundle();
            args.putInt(ARG_CATEGORY_ID, categoryId);
        }
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public interface OnImageSelectedListener {
        void onItemSelected(int imageId, int categoryId);
    }
}
