package net.rubisoft.photon.categorization;

import android.net.Uri;
import android.support.annotation.Nullable;

import net.rubisoft.photon.HttpUtils;

import org.apache.http.entity.mime.HttpMultipartMode;
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

    static final String CATEGORIZERS_ENDPOINT = "https://api.imagga.com/v1/categorizers";
    static final String CATEGORIZATION_ENDPOINT = "https://api.imagga.com/v1/categorizations/";
    static final String CONTENT_ENDPOINT = "https://api.imagga.com/v1/content";
    static final String SELECTED_CATEGORIZER = "personal_photos";
    static final String key = "";
    static ImaggaCategorizer mInstance;
    static final Set<String> mWebSchemes;

    public static ImaggaCategorizer getInstance() {
        if (mInstance == null)
            mInstance = new ImaggaCategorizer();

        return mInstance;
    }

    @Override
    public String[] getCategories() {
        if (key.isEmpty())
            throw new AssertionError("Imagga key is empty");

        HttpUtils.HttpResponse response;

        try {
            response = HttpUtils.get(CATEGORIZERS_ENDPOINT, key);
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
        ContentIdentificator identificator;
        if (!mWebSchemes.contains(image.getScheme())) {
            // upload
            identificator = new InternalContentIdentificator(uploadImage(image));
        } else {
            identificator = new WebContentIdentificator(image);
        }

        List<Categorization> categorizations = categorize(identificator);
        // store categorization info


        if (identificator instanceof InternalContentIdentificator) {
            deleteImage(((InternalContentIdentificator) identificator).getId());
        }
        return categorizations;
    }

    @Nullable
    private String uploadImage(Uri file) {
        MultipartEntity data = new MultipartEntity(HttpMultipartMode.STRICT);
        data.addPart("image", new FileBody(new File(file.getPath())));

        HttpUtils.HttpResponse response;
        try {
            response = HttpUtils.postUpload(CONTENT_ENDPOINT, key, data);
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
                        return uploadedObj.getString("id");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Nullable
    private List<Categorization> categorize(ContentIdentificator identificator) {
        HttpUtils.HttpResponse response;
        Uri categorizationCall = Uri.parse(CATEGORIZATION_ENDPOINT).buildUpon()
                .appendPath(SELECTED_CATEGORIZER)
                .encodedQuery(identificator.build())
                .build();

        try {
            response = HttpUtils.get(categorizationCall.toString(), key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (response.ResponseCode != 200)
            return null;

        ArrayList<Categorizer.Categorization> categorizations = null;
        try {
            JSONArray resultsArr = new JSONObject(response.Content)
                    .getJSONArray("results");
            if (resultsArr == null || resultsArr.length() == 0)
                return null;

            JSONObject result = (JSONObject) resultsArr.get(0);
            JSONArray categories = result.getJSONArray("categories");
            categorizations = new ArrayList<>();
            for (int i = 0; i < categories.length(); i++) {
                JSONObject catObj = (JSONObject) categories.get(i);
                categorizations.add(new Categorization(catObj.getString("name").replace(' ', '_'),
                        (float) (catObj.getDouble("confidence") / 100d)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categorizations;
    }

    private boolean deleteImage(String id) {
        HttpUtils.HttpResponse response;
        try {
            response = HttpUtils.delete(Uri.parse(CONTENT_ENDPOINT)
                    .buildUpon().appendPath(id).toString(), key);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return response.ResponseCode == 200;
    }

    private interface ContentIdentificator {
        String build();
    }

    private class WebContentIdentificator implements ContentIdentificator {
        public WebContentIdentificator(Uri uri) {
            mUri = uri;
        }

        private Uri mUri;

        @Override
        public String build() {
            return "url=" + mUri.toString();
        }
    }

    private class InternalContentIdentificator implements ContentIdentificator {
        public InternalContentIdentificator(String id) {
            mId = id;
        }

        private String mId;

        public String getId() {
            return mId;
        }

        @Override
        public String build() {
            return "content=" + mId;
        }
    }
}
