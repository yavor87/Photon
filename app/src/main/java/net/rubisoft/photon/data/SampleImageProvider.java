package net.rubisoft.photon.data;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleImageProvider implements ImageProvider {
    static final String BASE = "http://i.imgur.com/";
    static final String EXT = ".jpg";
    static final ArrayList<Uri> URLS;
    static {
        URLS = new ArrayList<>();
        String[] data = new String[]{
                BASE + "CqmBjo5" + EXT, BASE + "zkaAooq" + EXT, BASE + "0gqnEaY" + EXT,
                BASE + "9gbQ7YR" + EXT, BASE + "aFhEEby" + EXT, BASE + "0E2tgV7" + EXT,
                BASE + "P5JLfjk" + EXT, BASE + "nz67a4F" + EXT, BASE + "dFH34N5" + EXT,
                BASE + "FI49ftb" + EXT, BASE + "DvpvklR" + EXT, BASE + "DNKnbG8" + EXT,
                BASE + "yAdbrLp" + EXT, BASE + "55w5Km7" + EXT, BASE + "NIwNTMR" + EXT,
                BASE + "DAl0KB8" + EXT, BASE + "xZLIYFV" + EXT, BASE + "HvTyeh3" + EXT,
                BASE + "Ig9oHCM" + EXT, BASE + "7GUv9qa" + EXT, BASE + "i5vXmXp" + EXT,
                BASE + "glyvuXg" + EXT, BASE + "u6JF6JZ" + EXT, BASE + "ExwR7ap" + EXT,
                BASE + "Q54zMKT" + EXT, BASE + "9t6hLbm" + EXT, BASE + "F8n3Ic6" + EXT,
                BASE + "P5ZRSvT" + EXT, BASE + "jbemFzr" + EXT, BASE + "8B7haIK" + EXT,
                BASE + "aSeTYQr" + EXT, BASE + "OKvWoTh" + EXT, BASE + "zD3gT4Z" + EXT,
                BASE + "z77CaIt" + EXT
        };
        for (String uriStr : data) {
            URLS.add(Uri.parse(uriStr));
        }
    }

    @Override
    public List<Uri> getImages() {
        Collections.shuffle(URLS);
        return URLS;
    }
}