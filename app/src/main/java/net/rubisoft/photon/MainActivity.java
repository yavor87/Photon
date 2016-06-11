package net.rubisoft.photon;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.rubisoft.photon.content.ImageContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        PhotoListFragment.OnImageSelectedListener{

    private static final String PHOTO_LIST = "photo_list";
    private static final String[] PROJECTION = { ImageContract.CategoryEntry._ID,
            ImageContract.CategoryEntry.NAME };

    private SimpleCursorAdapter mCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, new PhotoListFragment(), PHOTO_LIST)
                .commit();

        mCategoriesAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null, new String[]{ ImageContract.CategoryEntry.NAME }, new int[] { android.R.id.text1 }, 0);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(mCategoriesAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                int categoryId = cursor.getInt(cursor.getColumnIndex(ImageContract.CategoryEntry._ID));
                PhotoListFragment fragment = (PhotoListFragment) getSupportFragmentManager().findFragmentByTag(PHOTO_LIST);
                fragment.DisplayCategory(categoryId);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemSelected(int imageId, int categoryId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.IMAGE_ID_EXTRA, imageId);
        if (categoryId != PhotoListFragment.ALL_CATEGORIES) {
            intent.putExtra(DetailActivity.CATEGORY_ID_EXTRA, categoryId);
        }
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ImageContract.CategoryEntry.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCategoriesAdapter.swapCursor(addAllCategoriesOption(data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCategoriesAdapter.swapCursor(null);
    }

    private Cursor addAllCategoriesOption(Cursor categoriesCursor) {
        MatrixCursor allOption = new MatrixCursor(categoriesCursor.getColumnNames());
        allOption.addRow(new Object[]{ PhotoListFragment.ALL_CATEGORIES, "all"});
        return new MergeCursor(new Cursor[]{ allOption, categoriesCursor} );
    }
}
