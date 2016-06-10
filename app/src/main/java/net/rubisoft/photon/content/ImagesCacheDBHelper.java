package net.rubisoft.photon.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImagesCacheDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "imagesCache.db";

    public ImagesCacheDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                ImageContract.CategoryEntry.TABLE_NAME + " (" +
                ImageContract.CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ImageContract.CategoryEntry.NAME + " TEXT UNIQUE NOT NULL, " +
                "UNIQUE (" + ImageContract.CategoryEntry.NAME +
                ") ON CONFLICT IGNORE" + " );";

        final String SQL_CREATE_IMAGES_TABLE = "CREATE TABLE " +
                ImageContract.ImageEntry.TABLE_NAME + " (" +
                ImageContract.ImageEntry._ID + " INTEGER UNIQUE NOT NULL," +
                ImageContract.ImageEntry.DATE_TAKEN + " INTEGER NOT NULL, " +
                ImageContract.ImageEntry.IMAGE_URI + " TEXT NOT NULL, " +
                ImageContract.ImageEntry.THUMBNAIL_URI + " TEXT, " +
                "UNIQUE (" + ImageContract.ImageEntry._ID +
                ") ON CONFLICT REPLACE" + " );";

        final String SQL_CREATE_CATEGORIZED_IMAGES_TABLE = "CREATE TABLE " +
                ImageContract.CategorizedImageEntry.TABLE_NAME + " (" +
                ImageContract.CategorizedImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ImageContract.CategorizedImageEntry.IMAGE_ID + " INTEGER NOT NULL, " +
                ImageContract.CategorizedImageEntry.CATEGORY_ID + " INTEGER NOT NULL, " +
                ImageContract.CategorizedImageEntry.CONFIDENCE + " REAL, " +
                "FOREIGN KEY(" + ImageContract.CategorizedImageEntry.IMAGE_ID +
                ") REFERENCES " + ImageContract.ImageEntry.TABLE_NAME + "(" +
                ImageContract.ImageEntry._ID + "), " +
                "FOREIGN KEY(" + ImageContract.CategorizedImageEntry.CATEGORY_ID +
                ") REFERENCES " + ImageContract.CategoryEntry.TABLE_NAME + "(" +
                ImageContract.CategoryEntry._ID + "));";

        final String SQL_CREATE_IMAGE_CATEGORIES_VIEW = "CREATE VIEW " +
                ImageContract.ImageCategoriesView.VIEW_NAME + " AS SELECT " +
                ImageContract.ImageCategoriesView.IMAGE_ID + ", " +
                ImageContract.CategorizedImageEntry.CATEGORY_ID + " AS " +
                ImageContract.ImageCategoriesView._ID + ", " +
                ImageContract.ImageCategoriesView.NAME + ", " +
                ImageContract.ImageCategoriesView.CONFIDENCE + " FROM " +
                ImageContract.CategorizedImageEntry.TABLE_NAME + " JOIN " +
                ImageContract.CategoryEntry.TABLE_NAME + " ON " +
                ImageContract.CategorizedImageEntry.TABLE_NAME + "." +
                ImageContract.CategorizedImageEntry.CATEGORY_ID + " = " +
                ImageContract.CategoryEntry.TABLE_NAME + "." + ImageContract.CategoryEntry._ID;

        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_IMAGES_TABLE);
        db.execSQL(SQL_CREATE_CATEGORIZED_IMAGES_TABLE);
        db.execSQL(SQL_CREATE_IMAGE_CATEGORIES_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ImageContract.CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImageContract.ImageEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImageContract.CategorizedImageEntry.TABLE_NAME);
        db.execSQL("DROP VIEW IF EXISTS " + ImageContract.ImageCategoriesView.VIEW_NAME);
        onCreate(db);
    }
}
