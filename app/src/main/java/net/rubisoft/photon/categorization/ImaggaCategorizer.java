package net.rubisoft.photon.categorization;

import android.net.Uri;

import net.rubisoft.photon.HttpUtils;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ImaggaCategorizer implements Categorizer {
    private ImaggaCategorizer() { }

    static {
        HashSet<String> schemes = new HashSet<>();
        schemes.add("http");
        schemes.add("https");
        schemes.add("ftp");
        mWebSchemes = schemes;
    }

    static String key = "";
    static ImaggaCategorizer mInstance;
    static Set<String> mWebSchemes;

    public static ImaggaCategorizer getInstance() {
        if (mInstance == null)
            mInstance = new ImaggaCategorizer();

        return mInstance;
    }

    @Override
    public String[] getCategories() {
        return new String[0];
    }

    @Override
    public Categorization categorizeImage(Uri image) {
        boolean hostedOnImagga = false;
        if (!mWebSchemes.contains(image.getScheme())) {
            // upload
            image = uploadImage(image);
            hostedOnImagga = true;
        }

        // categorize image
        // store categorization info

        if (hostedOnImagga) {
            deleteImage(image);
        }
        return null;
    }

    private Uri uploadImage(Uri file) {
        MultipartEntity data = new MultipartEntity();
        data.addPart("image", new FileBody(new File(file.getPath())));

        HttpUtils.HttpResponse response;
        try {
            response = HttpUtils.postUpload("https://api.imagga.com/v1/content", key, data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (response.ResponseCode == 200) {
            JSONObject result = response.Content;
            try {
                if (result.getString("status").equals("success")) {
                    JSONObject uploadedObj = (JSONObject) result.getJSONArray("uploaded").get(0);
                    if (uploadedObj != null) {
                        return new Uri.Builder()
                                .scheme("https")
                                .authority("api.imagga.com/v1/content/")
                                .path(uploadedObj.getString("id"))
                                .build();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void categorizeImage(String id) {

    }

    private boolean deleteImage(Uri imageUri) {
        HttpUtils.HttpResponse response;
        try {
            response = HttpUtils.delete(imageUri.toString(), key);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return response.ResponseCode == 200;
    }
}
