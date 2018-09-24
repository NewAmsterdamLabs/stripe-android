package com.stripe.android;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;

// Written by Kasim Rangwala
// https://stackoverflow.com/a/41050037/9099498

public class OkHttp3Helper {

    public static final String TAG;
    private static final okhttp3.OkHttpClient client;

    static {
        TAG = OkHttp3Helper.class.getSimpleName();
        client = new okhttp3.OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private Context context;

    public OkHttp3Helper(Context context) {
        this.context = context;
    }

    /**
     * <strong>Uses:</strong><br/>
     * <p>
     * {@code
     * ArrayMap<String, String> formField = new ArrayMap<>();}
     * <br/>
     * {@code formField.put("key1", "value1");}<br/>
     * {@code formField.put("key2", "value2");}<br/>
     * {@code formField.put("key3", "value3");}<br/>
     * <br/>
     * {@code String response = helper.postToServer("http://www.example.com/", formField);}<br/>
     * </p>
     *
     * @param url       String
     * @param formField android.support.v4.util.ArrayMap
     * @return response from server in String format
     * @throws Exception
     */
    @NonNull
    public String postToServer(@NonNull String url, @Nullable ArrayMap<String, String> formField)
            throws Exception {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(url);
        if (formField != null) {
            okhttp3.FormBody.Builder formBodyBuilder = new okhttp3.FormBody.Builder();
            for (Map.Entry<String, String> entry : formField.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
            requestBuilder.post(formBodyBuilder.build());
        }
        okhttp3.Request request = requestBuilder.build();
        okhttp3.Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException(response.message());
        }
        return response.body().string();
    }

    /**
     * <strong>Uses:</strong><br/>
     * <p>
     * {@code
     * ArrayMap<String, String> formField = new ArrayMap<>();}
     * <br/>
     * {@code formField.put("key1", "value1");}<br/>
     * {@code formField.put("key2", "value2");}<br/>
     * {@code formField.put("key3", "value3");}<br/>
     * <br/>
     * {@code
     * ArrayMap<String, File> filePart = new ArrayMap<>();}
     * <br/>
     * {@code filePart.put("key1", new File("pathname"));}<br/>
     * {@code filePart.put("key2", new File("pathname"));}<br/>
     * {@code filePart.put("key3", new File("pathname"));}<br/>
     * <br/>
     * {@code String response = helper.postToServer("http://www.example.com/", formField, filePart);}<br/>
     * </p>
     *
     * @param url       String
     * @param formField android.support.v4.util.ArrayMap
     * @param filePart  android.support.v4.util.ArrayMap
     * @return response from server in String format
     * @throws Exception
     */
    @NonNull
    public String postMultiPartToServer(@NonNull String url,
                                        @Nullable ArrayMap<String, String> formField,
                                        @Nullable ArrayMap<String, File> filePart,
                                        @Nullable String bearerToken)
            throws Exception {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(url);
        if (formField != null || filePart != null) {
            okhttp3.MultipartBody.Builder multipartBodyBuilder = new okhttp3.MultipartBody.Builder();
            multipartBodyBuilder.setType(okhttp3.MultipartBody.FORM);
            if (formField != null) {
                for (Map.Entry<String, String> entry : formField.entrySet()) {
                    multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
            if (filePart != null) {
                for (Map.Entry<String, File> entry : filePart.entrySet()) {
                    File file = entry.getValue();
                    String path = file.getPath();
                    if(path.contains("content:/com"))
                        path = path.replace("content:/","content://");
                    Uri uri = Uri.parse(path);
                    MediaType type;

                    String scheme = uri.getScheme();

                    if (scheme != null && scheme.equals(ContentResolver.SCHEME_CONTENT))
                        type = getMediaType(uri);
                    else
                        type = getMediaType(file.toURI());

                    multipartBodyBuilder.addFormDataPart(
                            entry.getKey(),
                            file.getName(),
                            okhttp3.RequestBody.create(type, file)
                    );
                }
            }
            requestBuilder.post(multipartBodyBuilder.build());
        }
        requestBuilder.addHeader("Authorization", bearerToken);

        okhttp3.Request request = requestBuilder.build();
        okhttp3.Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException(response.message());
        }
        return response.body().string();
    }

    private okhttp3.MediaType getMediaType(URI uri1) {
        Uri uri = Uri.parse(uri1.toString());
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    extension.toLowerCase());
        }
        return okhttp3.MediaType.parse(mimeType);
    }

    private okhttp3.MediaType getMediaType(Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            mimeType = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }
        return okhttp3.MediaType.parse(mimeType);
    }
}
