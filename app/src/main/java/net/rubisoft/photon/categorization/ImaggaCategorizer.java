package net.rubisoft.photon.categorization;

import android.net.Uri;

import net.rubisoft.photon.utils.HttpUtils;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        HttpUtils.HttpResponse response;
        Uri categorizationCall =  new Uri.Builder()
                .scheme("https")
                .authority("api.imagga.com")
                .appendPath("v1").appendPath("categorizers")
                .build();

        try {
            response = HttpUtils.get(categorizationCall.toString(), key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String[] categories = null;
        if (response.ResponseCode == 200) {
            try {
                JSONArray categorizersArray = new JSONArray(response.Content);
                JSONArray labels = null;
                for (int i = 0; i < categorizersArray.length(); i++) {
                    JSONObject categorizerObj = (JSONObject) categorizersArray.get(i);
                    if (categorizerObj.getString("id").equals("personal_photos")) {
                        labels = categorizerObj.getJSONArray("labels");
                        break;
                    }
                }
                if (labels != null) {
                    categories = new String[labels.length()];
                    for (int i = 0; i < labels.length(); i++) {
                        categories[i] = labels.getString(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return categories;
    }

    @Override
    public List<Categorization> categorizeImage(Uri image) {
        boolean hostedOnImagga = false;
        if (!mWebSchemes.contains(image.getScheme())) {
            // upload
            image = uploadImage(image);
            hostedOnImagga = true;
        }

        List<Categorization> categorizations = categorize(image);
        // store categorization info


        if (hostedOnImagga) {
            deleteImage(image);
        }
        return categorizations;
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
            try {
                JSONObject result = new JSONObject(response.Content);
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

    private List<Categorization> categorize(Uri image) {
        HttpUtils.HttpResponse response;
        Uri categorizationCall =  new Uri.Builder()
                .scheme("https")
                .authority("api.imagga.com")
                .appendPath("v1").appendPath("categorizations").appendPath("personal_photos")
                .encodedQuery("url=" + image.toString())
                .build();

        try {
            response = HttpUtils.get(categorizationCall.toString(), key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ArrayList<Categorizer.Categorization> categorizations = null;
        try {
            JSONObject result = (JSONObject) new JSONObject(response.Content)
                    .getJSONArray("results").get(0);
            JSONArray categories = result.getJSONArray("categories");
            categorizations = new ArrayList<>();
            for (int i = 0; i < categories.length(); i++) {
                JSONObject catObj = (JSONObject) categories.get(i);
                categorizations.add(new Categorization(catObj.getString("name"),
                        (float) (catObj.getDouble("confidence") / 100d)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categorizations;
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
