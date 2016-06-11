package net.rubisoft.photon;

import android.database.Cursor;
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
    static final String[] PROJECTION = { ImageContract.ImageEntry._ID };
    static final int COL_ID = 0;

    private ViewPager mViewPager;
    private ImageCollectionPagerAdapter mAdapter;
    private SparseArray<Integer> mImagesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_detail);

        mImagesMap = new SparseArray<>();
        mAdapter = new ImageCollectionPagerAdapter(getSupportFragmentManager(), mImagesMap);
        mViewPager = (ViewPager) findViewById(R.id.detail_container);
        mViewPager.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ImageContract.ImageEntry.CONTENT_URI, PROJECTION, null, null,
                ImageContract.ImageEntry.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int imageId = getIntent().getIntExtra(IMAGE_ID_EXTRA, -1);
        int imagePosition = -1;

        int i = 0;
        while (data.moveToNext()) {
            int currentId = data.getInt(COL_ID);
            if (imageId == currentId)
                imagePosition = i;

            mImagesMap.put(i++, currentId);
        }
        mAdapter.notifyDataSetChanged();
        if (imagePosition != -1) {
            mViewPager.setCurrentItem(imagePosition);
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
