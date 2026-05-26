package com.gr6.smartcart_android.buyer.main.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.main.api.BuyerHomeApiService;
import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.buyer.main.response.RecommendationPageResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyerHomeRepository {

    private static final String TAG = "AI_IMAGE_REPO";

    private final Context context;
    private final BuyerHomeApiService apiService;

    public BuyerHomeRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.createService(context, BuyerHomeApiService.class);
    }

    public void getCategories(HomeCallback<List<HomeCategoryResponse>> callback) {
        apiService.getCategories().enqueue(new Callback<BaseResponse<List<HomeCategoryResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<HomeCategoryResponse>>> call,
                    @NonNull Response<BaseResponse<List<HomeCategoryResponse>>> response
            ) {
                handleBaseResponse(response, callback, "Không lấy được danh mục");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<HomeCategoryResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void searchProducts(
            SearchProductRequest request,
            int page,
            int size,
            HomeCallback<ProductPageResponse> callback
    ) {
        apiService.searchProducts(request, page, size).enqueue(new Callback<BaseResponse<ProductPageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ProductPageResponse>> call,
                    @NonNull Response<BaseResponse<ProductPageResponse>> response
            ) {
                handleBaseResponse(response, callback, "Không lấy được sản phẩm");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ProductPageResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getAiTrending(
            int page,
            int size,
            HomeCallback<RecommendationPageResponse> callback
    ) {
        apiService.getAiTrending(page, size).enqueue(new Callback<RecommendationPageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Response<RecommendationPageResponse> response
            ) {
                handleRawResponse(response, callback, "Không lấy được AI gợi ý");
            }

            @Override
            public void onFailure(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getAiPersonal(
            int page,
            int size,
            HomeCallback<RecommendationPageResponse> callback
    ) {
        apiService.getAiPersonal(page, size).enqueue(new Callback<RecommendationPageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Response<RecommendationPageResponse> response
            ) {
                handleRawResponse(response, callback, "Không lấy được AI cá nhân hóa");
            }

            @Override
            public void onFailure(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getAiSearch(
            String keyword,
            int page,
            int size,
            HomeCallback<RecommendationPageResponse> callback
    ) {
        apiService.getAiSearch(keyword, page, size).enqueue(new Callback<RecommendationPageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Response<RecommendationPageResponse> response
            ) {
                handleRawResponse(response, callback, "Không lấy được AI tìm kiếm");
            }

            @Override
            public void onFailure(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void searchByImage(
            Uri imageUri,
            int page,
            int size,
            HomeCallback<RecommendationPageResponse> callback
    ) {
        Log.d(TAG, "===== searchByImage() called =====");
        Log.d(TAG, "imageUri = " + imageUri);
        Log.d(TAG, "page = " + page + ", size = " + size);

        MultipartBody.Part imagePart = createImagePart(imageUri);

        if (imagePart == null) {
            Log.e(TAG, "imagePart null -> không đọc được ảnh");
            callback.onError("Không đọc được ảnh đã chọn");
            return;
        }

        Log.d(TAG, "Bắt đầu gọi API: /api/v3/recommendations/ai/image-search");

        apiService.searchByImage(imagePart, page, size).enqueue(new Callback<RecommendationPageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Response<RecommendationPageResponse> response
            ) {
                Log.d(TAG, "API image-search response code = " + response.code());
                Log.d(TAG, "API image-search isSuccessful = " + response.isSuccessful());

                if (response.errorBody() != null) {
                    try {
                        Log.e(TAG, "API image-search errorBody = " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Không đọc được errorBody", e);
                    }
                }

                RecommendationPageResponse body = response.body();

                if (body != null) {
                    int productSize = body.getProducts() == null ? 0 : body.getProducts().size();

                    Log.d(TAG, "API image-search body != null");
                    Log.d(TAG, "products size = " + productSize);
                    Log.d(TAG, "totalElements = " + body.getTotalElements());
                    Log.d(TAG, "hasMore = " + body.isHasMore());
                    Log.d(TAG, "page = " + body.getPage());
                    Log.d(TAG, "size = " + body.getSize());
                } else {
                    Log.e(TAG, "API image-search body null");
                }

                handleRawResponse(response, callback, "Không tìm kiếm được bằng hình ảnh");
            }

            @Override
            public void onFailure(
                    @NonNull Call<RecommendationPageResponse> call,
                    @NonNull Throwable t
            ) {
                Log.e(TAG, "API image-search failure", t);
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private MultipartBody.Part createImagePart(Uri imageUri) {
        if (imageUri == null) {
            Log.e(TAG, "createImagePart: imageUri null");
            return null;
        }

        try {
            Log.d(TAG, "createImagePart: uri = " + imageUri);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                Log.e(TAG, "createImagePart: inputStream null");
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];

            int read;

            while ((read = inputStream.read(data)) != -1) {
                buffer.write(data, 0, read);
            }

            inputStream.close();

            byte[] bytes = buffer.toByteArray();

            Log.d(TAG, "Ảnh đọc được bytes = " + bytes.length);

            if (bytes.length == 0) {
                Log.e(TAG, "createImagePart: bytes length = 0");
                return null;
            }

            String mimeType = context.getContentResolver().getType(imageUri);

            if (mimeType == null || mimeType.trim().isEmpty()) {
                mimeType = "image/jpeg";
            }

            Log.d(TAG, "mimeType = " + mimeType);

            MediaType mediaType = MediaType.parse(mimeType);

            if (mediaType == null) {
                Log.e(TAG, "mediaType null, fallback image/jpeg");
                mediaType = MediaType.parse("image/jpeg");
            }

            RequestBody requestBody = RequestBody.create(mediaType, bytes);

            String fileName = "smartcart_image_search_" + System.currentTimeMillis() + ".jpg";

            Log.d(TAG, "fileName = " + fileName);
            Log.d(TAG, "multipart field name = file");

            return MultipartBody.Part.createFormData(
                    "file",
                    fileName,
                    requestBody
            );

        } catch (Exception e) {
            Log.e(TAG, "createImagePart error", e);
            return null;
        }
    }

    private <T> void handleBaseResponse(
            Response<BaseResponse<T>> response,
            HomeCallback<T> callback,
            String defaultMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(defaultMessage + ". Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<T> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        callback.onSuccess(body.getData());
    }

    private <T> void handleRawResponse(
            Response<T> response,
            HomeCallback<T> callback,
            String defaultMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(defaultMessage + ". Mã lỗi: " + response.code());
            return;
        }

        T body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        callback.onSuccess(body);
    }

    public interface HomeCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }
}