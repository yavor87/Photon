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
                ImagesCache.Category.TABLE_NAME + " (" +
                ImagesCache.Category._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ImagesCache.Category.NAME + " TEXT UNIQUE NOT NULL, " +
                "UNIQUE (" + ImagesCache.Category.NAME +
                ") ON CONFLICT IGNORE" + " );";

        final String SQL_CREATE_IMAGES_TABLE = "CREATE TABLE " +
                ImagesCache.Image.TABLE_NAME + " (" +
                ImagesCache.Image._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ImagesCache.Image.URI + " TEXT UNIQUE NOT NULL, " +
                "UNIQUE (" + ImagesCache.Image.URI +
                ") ON CONFLICT IGNORE" + " );";

        final String SQL_CREATE_CATEGORIZED_IMAGES_TABLE = "CREATE TABLE " +
                ImagesCache.CategorizedImage.TABLE_NAME + " (" +
                ImagesCache.CategorizedImage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ImagesCache.CategorizedImage.IMAGE_ID + " INTEGER NOT NULL, " +
                ImagesCache.CategorizedImage.CATEGORY_ID + " INTEGER NOT NULL, " +
                ImagesCache.CategorizedImage.CONFIDENCE + " REAL, " +
                "FOREIGN KEY(" + ImagesCache.CategorizedImage.IMAGE_ID +
                ") REFERENCES " + ImagesCache.Image.TABLE_NAME + "(" +
                ImagesCache.Image._ID + ")" +
                "FOREIGN KEY(" + ImagesCache.CategorizedImage.CATEGORY_ID +
                ") REFERENCES " + ImagesCache.Category.TABLE_NAME + "(" +
                ImagesCache.Category._ID + ");";

        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_IMAGES_TABLE);
        db.execSQL(SQL_CREATE_CATEGORIZED_IMAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ImagesCache.Category.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImagesCache.Image.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImagesCache.CategorizedImage.TABLE_NAME);
        onCreate(db);
    }
}
