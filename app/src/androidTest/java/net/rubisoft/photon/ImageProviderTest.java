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

    //
    // Insert
    //
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
    public void insertingImage_insertsValidRecordInDB() {
        Uri result = getMockContentResolver().insert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImage());

        Assert.assertNotNull(result);
        Cursor data = getMockContentResolver().query(result, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("file://local/img1.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    @Test
    public void insertingCategoryForImage_insertsValidRecordInDB() {
        getMockContentResolver().insert(ImageContract.ImageEntry.CONTENT_URI, getValidImage());
        getMockContentResolver().insert(ImageContract.CategoryEntry.CONTENT_URI, getValidCategory());
        Uri result = getMockContentResolver().insert(ImageContract.ImageEntry.buildImageWithCategoriesUri(1),
                getValidCategorization(1));

        Assert.assertNotNull(result);
        Cursor data = getMockContentResolver().query(result, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.CategorizedImageEntry.IMAGE_ID)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.CategorizedImageEntry.CATEGORY_ID)));
        Assert.assertEquals(0.6, data.getFloat(
                data.getColumnIndex(ImageContract.CategorizedImageEntry.CONFIDENCE)), 0.001);
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    //
    // Bulk Insert
    //
    @Test
    public void bulkInsertingCategories_insertsValidRecordInDB() {
        int result = getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Assert.assertEquals(2, result);
    }

    @Test
    public void bulkInsertingImages_insertsValidRecordInDB() {
        int result = getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());

        Assert.assertEquals(2, result);
    }

    @Test
    public void bulkInsertingCategorizations_insertsValidRecordInDB() {
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI, getValidCategories());
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI, getValidImages());

        int result = getMockContentResolver().bulkInsert(ImageContract.ImageEntry.buildImageWithCategoriesUri(1),
                getValidCategorizations(1));

        Assert.assertEquals(2, result);
    }

    //
    // Query
    //
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

    @Test
    public void queryImages_getsValidImages() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());

        Uri dataUri = ImageContract.ImageEntry.CONTENT_URI;
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("file://local/img1.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));

        data.moveToNext();
        Assert.assertEquals("file://local/img2.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    @Test
    public void queryUncategorizedImages_getsAllImages() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Uri dataUri = ImageContract.ImageEntry.uncategorizedImagesUri();
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("file://local/img1.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(1,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));

        data.moveToNext();
        Assert.assertEquals("file://local/img2.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    @Test
    public void queryUncategorizedImages_afterCategorizing1Image_getsSingleImage() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());
        getMockContentResolver().insert(ImageContract.ImageEntry.buildImageWithCategoriesUri(1),
                getValidCategorization(1));

        Uri dataUri = ImageContract.ImageEntry.uncategorizedImagesUri();
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("file://local/img2.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    @Test
    public void queryUncategorizedImages_afterCategorizingAllImages_getsNoImages() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());
        getMockContentResolver().insert(ImageContract.ImageEntry.buildImageWithCategoriesUri(1),
                getValidCategorization(1));
        getMockContentResolver().insert(ImageContract.ImageEntry.buildImageWithCategoriesUri(2),
                getValidCategorization(2));

        Uri dataUri = ImageContract.ImageEntry.uncategorizedImagesUri();
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.getCount());
    }

    @Test
    public void queryImage_getsValidImage() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());

        Uri dataUri = ImageContract.ImageEntry.buildImageUri(2);
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("file://local/img2.png",
                data.getString(data.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        Assert.assertEquals(2,
                data.getInt(data.getColumnIndex(ImageContract.ImageEntry._ID)));
    }

    @Test
    public void queryImageCategories_returnsZero() {
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Uri dataUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(2);
        Cursor data = getMockContentResolver().query(dataUri, null, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.getCount());
    }

    @Test
    public void queryImageCategories_returnsCorrectCategories() {
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI, getValidCategories());
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI, getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.buildImageWithCategoriesUri(1),
                getValidCategorizations(1));

        getMockContentResolver().bulkInsert(ImageContract.ImageEntry.CONTENT_URI,
                getValidImages());
        getMockContentResolver().bulkInsert(ImageContract.CategoryEntry.CONTENT_URI,
                getValidCategories());

        Uri dataUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(1);
        Cursor data = getMockContentResolver().query(dataUri, new String[] { ImageContract.CategoryEntry.NAME }, null, null, null);
        Assert.assertNotNull(data);
        Assert.assertEquals(2, data.getCount());
        data.moveToFirst();
        Assert.assertEquals("landscape", data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
        data.moveToNext();
        Assert.assertEquals("sky", data.getString(data.getColumnIndex(ImageContract.CategoryEntry.NAME)));
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

    private ContentValues getValidImage() {
        return getValidImages()[0];
    }

    private ContentValues[] getValidImages() {
        ContentValues[] values = new ContentValues[2];
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(ImageContract.ImageEntry._ID, 1);
        contentValues1.put(ImageContract.ImageEntry.IMAGE_URI, "file://local/img1.png");
        values[0] = contentValues1;
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ImageContract.ImageEntry._ID, 2);
        contentValues2.put(ImageContract.ImageEntry.IMAGE_URI, "file://local/img2.png");
        values[1] = contentValues2;
        return values;
    }

    private ContentValues getValidCategorization(int imageId) {
        return getValidCategorizations(imageId)[0];
    }

    private ContentValues[] getValidCategorizations(int imageId) {
        ContentValues[] values = new ContentValues[2];
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(ImageContract.CategorizedImageEntry.CATEGORY_ID, 1);
        contentValues1.put(ImageContract.CategorizedImageEntry.IMAGE_ID, imageId);
        contentValues1.put(ImageContract.CategorizedImageEntry.CONFIDENCE, 0.6);
        values[0] = contentValues1;
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ImageContract.CategorizedImageEntry.CATEGORY_ID, 2);
        contentValues2.put(ImageContract.CategorizedImageEntry.IMAGE_ID, imageId);
        contentValues2.put(ImageContract.CategorizedImageEntry.CONFIDENCE, 0.4);
        values[1] = contentValues2;
        return values;
    }
}
