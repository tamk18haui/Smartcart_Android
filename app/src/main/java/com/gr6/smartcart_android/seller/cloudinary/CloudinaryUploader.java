package com.gr6.smartcart_android.seller.cloudinary;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.gr6.smartcart_android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Upload trực tiếp từ Android lên Cloudinary bằng unsigned upload preset.
 * Không để API secret trong app Android.
 */
public class CloudinaryUploader {

    private static boolean initialized = false;

    private CloudinaryUploader() {
    }

    public static synchronized void init(Context context) {
        if (initialized) return;

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", context.getString(R.string.cloudinary_cloud_name));
        config.put("secure", true);

        try {
            MediaManager.init(context.getApplicationContext(), config);
        } catch (IllegalStateException ignored) {
            // MediaManager đã init ở chỗ khác.
        }

        initialized = true;
    }

    public static void uploadMany(
            Context context,
            List<Uri> uris,
            UploadManyCallback callback
    ) {
        init(context);

        if (uris == null || uris.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<String> urls = new ArrayList<>();
        uploadAt(context, uris, 0, urls, callback);
    }

    private static void uploadAt(
            Context context,
            List<Uri> uris,
            int index,
            List<String> urls,
            UploadManyCallback callback
    ) {
        if (index >= uris.size()) {
            callback.onSuccess(urls);
            return;
        }

        String preset = context.getString(R.string.cloudinary_upload_preset);
        String folder = context.getString(R.string.cloudinary_folder);

        MediaManager.get()
                .upload(uris.get(index))
                .unsigned(preset)
                .option("folder", folder)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        callback.onProgress(index + 1, uris.size());
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Object secureUrl = resultData.get("secure_url");
                        if (secureUrl == null) {
                            Object url = resultData.get("url");
                            if (url != null) {
                                urls.add(String.valueOf(url));
                            }
                        } else {
                            urls.add(String.valueOf(secureUrl));
                        }
                        uploadAt(context, uris, index + 1, urls, callback);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onError(error == null ? "Upload ảnh thất bại" : error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onProgress(index + 1, uris.size());
                    }
                })
                .dispatch();
    }

    public interface UploadManyCallback {
        void onProgress(int current, int total);

        void onSuccess(@NonNull List<String> urls);

        void onError(String message);
    }
}
