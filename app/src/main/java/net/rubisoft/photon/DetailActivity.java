package net.rubisoft.photon;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;

import net.rubisoft.photon.content.ImageContract;

public class DetailActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String IMAGE_ID_EXTRA = DetailActivity.class.getPackage().toString() + ".IMAGE_ID";
    public static final String CATEGORY_ID_EXTRA = DetailActivity.class.getPackage().toString() + ".CATEGORY_ID";
    static final String[] PROJECTION = { ImageContract.ImageEntry._ID };
    static final int COL_ID = 0;

    private ViewPager mViewPager;
    private ImageCollectionPagerAdapter mAdapter;
    private SparseArray<Integer> mImagesMap;
    private int mStartImageId = -1;
    private int mCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState != null) {
            mStartImageId = savedInstanceState.getInt(IMAGE_ID_EXTRA);
            mCategoryId = savedInstanceState.getInt(CATEGORY_ID_EXTRA);
        } else {
            mStartImageId = getIntent().getIntExtra(IMAGE_ID_EXTRA, -1);
            mCategoryId = getIntent().getIntExtra(CATEGORY_ID_EXTRA, PhotoListFragment.ALL_CATEGORIES);
        }

        mImagesMap = new SparseArray<>();
        mAdapter = new ImageCollectionPagerAdapter(getSupportFragmentManager(), mImagesMap);
        mViewPager = (ViewPager) findViewById(R.id.detail_container);
        mViewPager.setAdapter(mAdapter);

        Bundle args = null;
        if (mCategoryId != PhotoListFragment.ALL_CATEGORIES) {
            args = new Bundle();
            args.putInt(CATEGORY_ID_EXTRA, mCategoryId);
        }
        getSupportLoaderManager().initLoader(0, args, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(IMAGE_ID_EXTRA, mStartImageId);
        outState.putInt(CATEGORY_ID_EXTRA, mCategoryId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri dataUri;
        if (args != null && args.containsKey(CATEGORY_ID_EXTRA)) {
            // get images from this category
            dataUri = ImageContract.CategoryEntry.buildImagesForCategoryUri(args.getInt(CATEGORY_ID_EXTRA));
        } else {
            dataUri = ImageContract.ImageEntry.CONTENT_URI;
        }
        return new CursorLoader(this, dataUri, PROJECTION, null, null,
                ImageContract.ImageEntry.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int startImagePosition = -1;

        int i = 0;
        while (data.moveToNext()) {
            int currentId = data.getInt(COL_ID);
            if (mStartImageId == currentId)
                startImagePosition = i;

            mImagesMap.put(i++, currentId);
        }
        mAdapter.notifyDataSetChanged();
        if (startImagePosition != -1) {
            mViewPager.setCurrentItem(startImagePosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImagesMap.clear();
        mAdapter.notifyDataSetChanged();
    }

    public class ImageCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public ImageCollectionPagerAdapter(FragmentManager fm, SparseArray<Integer> positionToImageId) {
            super(fm);
            mPositionToImageId = positionToImageId;
        }

        private SparseArray<Integer> mPositionToImageId;

        @Override
        public Fragment getItem(int position) {
            int imageId = mPositionToImageId.get(position, POSITION_NONE);
            return ImageFragment.newInstance(imageId);
        }

        @Override
        public int getCount() {
            return mPositionToImageId.size();
        }
    }
}
