package net.rubisoft.photon;

import org.apache.http.entity.mime.MultipartEntity;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 3000;

    public static HttpResponse postUpload(String address, String token, MultipartEntity data) throws IOError, IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Authorization", "Basic " + token);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        OutputStream request = conn.getOutputStream();
        try {
            data.writeTo(request);
        } finally {
            request.flush();
            request.close();
        }

        HttpUtils.HttpResponse response = new HttpUtils.HttpResponse();
        response.ResponseCode = conn.getResponseCode();
        InputStream res = conn.getInputStream();
        try {
            response.Content = toString(res);
        } finally {
            res.close();
        }

        conn.disconnect();

        return response;
    }

    public static HttpResponse get(String address, String token) throws IOError, IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Authorization", "Basic " + token);

        HttpUtils.HttpResponse response = new HttpUtils.HttpResponse();
        response.ResponseCode = conn.getResponseCode();
        InputStream res = conn.getInputStream();
        try {
            response.Content = toString(res);
        } finally {
            res.close();
        }

        conn.disconnect();

        return response;
    }

    public static HttpResponse delete(String address, String token) throws IOError, IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("DELETE");
        conn.addRequestProperty("Authorization", "Basic " + token);

        HttpUtils.HttpResponse response = new HttpUtils.HttpResponse();
        response.ResponseCode = conn.getResponseCode();

        conn.disconnect();

        return response;
    }

    public static String toString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        r.close();
        return total.toString();
    }

    public static class HttpResponse {
        public static HttpResponse ErrorResponse(Exception error) {
            HttpResponse response = new HttpResponse();
            response.Error = error;
            return response;
        }

        public String Content;
        public int ResponseCode;
        public Exception Error;
    }
}
