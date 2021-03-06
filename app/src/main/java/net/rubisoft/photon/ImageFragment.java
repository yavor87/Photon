package net.rubisoft.photon;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import net.rubisoft.photon.content.ImageContract;

/**
 * Fragment, containing a single image and a list of categories.
 */
public class ImageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_IMAGE_ID = "image_id";
    private static final int IMAGE_LOADER = 1;
    private static final int CATEGORIES_LOADER = 2;
    private static final String[] IMAGE_PROJECTION = { ImageContract.ImageEntry.IMAGE_URI };
    private static final String[] IMAGE_CATEGORY_PROJECTION = {
            ImageContract.ImageCategoriesView._ID,
            ImageContract.ImageCategoriesView.NAME,
            ImageContract.ImageCategoriesView.CONFIDENCE
    };
    static final int COL_IMAGE_URI = 0;
    static final int COL_CATEGORY_ID = 0;
    static final int COL_CATEGORY_NAME = 1;
    static final int COL_CATEGORY_CONFIDENCE = 2;

    private int mImageID;
    private ImageView mImageView;
    private LinearLayout mCategoriesView;
    private ImageCategoriesCursorAdapter mAdapter;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageId Image ID
     * @return A new instance of fragment ImageFragment.
     */
    public static ImageFragment newInstance(int imageId) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_ID, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageID = getArguments().getInt(ARG_IMAGE_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(IMAGE_LOADER, null, this);
        getLoaderManager().initLoader(CATEGORIES_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        mImageView = (ImageView) view.findViewById(R.id.detail_image);
        mCategoriesView = (LinearLayout) view.findViewById(R.id.detail_category_list);

        mAdapter = new ImageCategoriesCursorAdapter(getContext(), null, 0);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == IMAGE_LOADER) {
            Uri uri = ImageContract.ImageEntry.buildImageUri(mImageID);
            return new CursorLoader(getContext(), uri,
                    IMAGE_PROJECTION, null, null, null);
        } else {
            Uri uri = ImageContract.ImageEntry.buildImageWithCategoriesUri(mImageID);
            return new CursorLoader(getContext(), uri, IMAGE_CATEGORY_PROJECTION, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == IMAGE_LOADER) {
            if (data.moveToFirst()) {
                String imageUri = data.getString(COL_IMAGE_URI);
                Glide.with(this)
                        .load(imageUri)
                        .into(mImageView);
            }
        } else {
            mAdapter.swapCursor(data);
            populateCategories();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == IMAGE_LOADER) {
            mImageView.setImageURI(null);
        } else {
            mAdapter.swapCursor(null);
            populateCategories();
        }
    }

    private void populateCategories() {
        mCategoriesView.removeAllViews();
        final int adapterCount = mAdapter.getCount();

        for (int i = 0; i < adapterCount; i++) {
            View item = mAdapter.getView(i, null, null);
            mCategoriesView.addView(item);
        }
    }
}
