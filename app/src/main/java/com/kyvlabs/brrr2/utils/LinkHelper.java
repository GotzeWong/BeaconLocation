package com.kyvlabs.brrr2.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Gotze on 2016/5/30.
 */
public class LinkHelper {
    public static void goToUrl(Context context, String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = null;
        String type = null;
        try {
            type = LinkHelper.getMimeType(url);
            launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(null != type) {
                if(type.contains("video"))
                    launchBrowser.setDataAndType(uriUrl, "video/*");
                if(type.contains("audio"))
                    launchBrowser.setDataAndType(uriUrl, "audio/*");
                if(type.contains("image"))
                    launchBrowser.setDataAndType(uriUrl, "image/*");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        context.startActivity(launchBrowser);
    }

    public static String getType(String strUrl){

        BufferedInputStream bis = null;
        HttpURLConnection urlconnection = null;
        URL url = null;
        String type =null;
        try {
            url = new URL(strUrl);
        urlconnection = (HttpURLConnection) url.openConnection();
        urlconnection.connect();
        bis = new BufferedInputStream(urlconnection.getInputStream());
        type = HttpURLConnection.guessContentTypeFromStream(bis);
        urlconnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return type;
    }

    public static String getMimeType(String fileUrl)
            throws java.io.IOException
    {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileUrl);
        Log.d("UrlType", "Url: " + fileUrl);
        Log.d("UrlType", "type: " + type);
        return type;
    }
}
