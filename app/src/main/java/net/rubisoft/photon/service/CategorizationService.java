package net.rubisoft.photon.service;

import android.app.IntentService;
import android.content.Intent;

import net.rubisoft.photon.HttpUtils;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class CategorizationService extends IntentService {
    public CategorizationService() {
        super("CategorizationService");
    }

    static String key = "";

    @Override
    protected void onHandleIntent(Intent intent) {
        // get image File from intent
        // upload image
        // categorize image
        // store categorization info
        // delete image
    }

    private String uploadImage(File file) {
        MultipartEntity data = new MultipartEntity();
        data.addPart("image", new FileBody(file));

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
                        return uploadedObj.getString("id");
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

    private void deleteImage(String id) {

    }
}
