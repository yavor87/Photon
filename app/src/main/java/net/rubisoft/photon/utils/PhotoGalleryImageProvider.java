package net.rubisoft.photon.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import net.rubisoft.photon.content.PhotoItem;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryImageProvider {
    private static final String[] imageProjection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, };
    private static final String[] thumbProjection = { MediaStore.Images.Thumbnails.DATA };

    public static @Nullable List<PhotoItem> getDevicePhotos(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)
            return null;
        ContentResolver resolver = context.getContentResolver();

        final Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return null;
        }

        int i = 0;
        ArrayList<PhotoItem> results = new ArrayList<>(cursor.getCount());
        try {
            if (cursor.moveToFirst()) {
                final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                do {
                    int imageId = cursor.getInt(idColumn);
                    Uri imageUri = Uri.parse("file:" + cursor.getString(dataColumn));
                    Uri thumbUri = null;

                    // Obtain thumbnail
                    Cursor thumbnail = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                            thumbProjection,
                            MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                            new String[]{Integer.toString(imageId)}, null);
                    try {
                        if (thumbnail != null && thumbnail.moveToFirst()) {
                            thumbUri = Uri.parse("file:" + thumbnail.getString(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if (thumbnail != null)
                            thumbnail.close();
                    }

                    results.add(new PhotoItem(imageId, thumbUri, imageUri));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return results;
    }
}
