package net.rubisoft.photon;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;


import net.rubisoft.photon.content.ImageContract;
import net.rubisoft.photon.content.ImageProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ImageProviderTest extends ProviderTestCase2<ImageProvider> {
    public ImageProviderTest() {
        super(ImageProvider.class, ImageContract.CONTENT_AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getType_forCategory_returnsCorrectContentType() {
        Uri uri = ImageContract.CategoryEntry.buildCategoryUri(1);
        String type = getMockContentResolver().getType(uri);
        Assert.assertEquals(ImageContract.CategoryEntry.CONTENT_ITEM_TYPE, type);
    }

    @Test
    public void getType_forCategories_returnsCorrectContentType() {
        Uri uri = ImageContract.CategoryEntry.CONTENT_URI;
        String type = getMockContentResolver().getType(uri);
        Assert.assertEquals(ImageContract.CategoryEntry.CONTENT_TYPE, type);
    }

    @Test
    public void getType_forImage_returnsCorrectContentType() {
        Uri uri = ImageContract.ImageEntry.buildImageUri(1);
        String type = getMockContentResolver().getType(uri);
        Assert.assertEquals(ImageContract.ImageEntry.CONTENT_ITEM_TYPE, type);
    }

    @Test
    public void getType_forImages_returnsCorrectContentType() {
        Uri uri = ImageContract.ImageEntry.CONTENT_URI;
        String type = getMockContentResolver().getType(uri);
        Assert.assertEquals(ImageContract.ImageEntry.CONTENT_TYPE, type);
    }

    @Test
    public void getType_forImagesWithCategories_returnsCorrectContentType() {
        Uri uri = ImageContract.ImageEntry.buildImageWithCategoriesUri(2);
        String type = getMockContentResolver().getType(uri);
        Assert.assertEquals(ImageContract.CategoryEntry.CONTENT_TYPE, type);
    }

    @Test
    public void insertingCategory_insertsValidRecordInDB() {
        Uri result = getMockContentResolver().insert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategory());

        Assert.assertNotNull(result);
        Cursor data = getMockContentResolver().query(result, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("landscape",
                data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.CategoryEntry._ID)));
    }

    @Test
    public void queryCategories_getsValidCategories() {
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Uri dataUri = ImageContract.CategoryEntry.CONTENT_URI;
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("landscape",
                data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.CategoryEntry._ID)));

        data.moveToNext();
        Assert.assertEquals("sky",
                data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.CategoryEntry._ID)));
    }

    @Test
    public void queryCategory_getsValidCategory() {
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Uri dataUri = ImageContract.CategoryEntry.buildCategoryUri(2);
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("sky",
                data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.CategoryEntry._ID)));
    }

    private ContentValues getValidCategory() {
        return getValidCategories()[0];
    }

    private ContentValues[] getValidCategories() {
        ContentValues[] values = new ContentValues[2];
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(ImageContract.CategoryEntry.NAME, "landscape");
        values[0] = contentValues1;
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ImageContract.CategoryEntry.NAME, "sky");
        values[1] = contentValues2;
        return values;
    }
}
