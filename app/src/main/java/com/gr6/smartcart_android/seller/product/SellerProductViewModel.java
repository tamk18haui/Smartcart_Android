package com.gr6.smartcart_android.seller.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.seller.product.repository.SellerProductRepository;
import com.gr6.smartcart_android.seller.product.request.ProductRequest;
import com.gr6.smartcart_android.seller.product.response.CategoryResponse;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerProductViewModel extends AndroidViewModel {

    private final SellerProductRepository repository;

    private final MutableLiveData<ProductCreateState> createState = new MutableLiveData<>();
    private final MutableLiveData<ProductListState> productListState = new MutableLiveData<>();
    private final MutableLiveData<ProductDetailState> productDetailState = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryResponse>> categoryState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> brandState = new MutableLiveData<>();

    public SellerProductViewModel(@NonNull Application application) {
        super(application);
        repository = new SellerProductRepository(application);
        createState.setValue(ProductCreateState.idle());
    }

    public LiveData<ProductCreateState> getCreateState() {
        return createState;
    }

    public LiveData<ProductListState> getProductListState() {
        return productListState;
    }

    public LiveData<ProductDetailState> getProductDetailState() {
        return productDetailState;
    }

    public LiveData<List<CategoryResponse>> getCategoryState() {
        return categoryState;
    }

    public LiveData<List<String>> getBrandState() {
        return brandState;
    }

    public void createProduct(ProductRequest request) {
        createState.setValue(ProductCreateState.loading());

        repository.submitProduct(request, new SellerProductRepository.ProductCallback<ProductResponse>() {
            @Override
            public void onSuccess(ProductResponse data, String message) {
                createState.postValue(ProductCreateState.success(data, message));
            }

            @Override
            public void onError(String message) {
                createState.postValue(ProductCreateState.error(message));
            }
        });
    }

    public void loadProductsByShop(Long shopId, int page, int size) {
        productListState.setValue(ProductListState.loading());

        repository.loadProductsByShop(shopId, page, size, new SellerProductRepository.ProductCallback<PageResponse<ProductResponse>>() {
            @Override
            public void onSuccess(PageResponse<ProductResponse> data, String message) {
                List<ProductResponse> products = data == null || data.getData() == null
                        ? new ArrayList<>()
                        : data.getData();

                productListState.postValue(ProductListState.success(products, message));
            }

            @Override
            public void onError(String message) {
                productListState.postValue(ProductListState.error(message));
            }
        });
    }

    public void loadProductForSeller(Long productId) {
        productDetailState.setValue(ProductDetailState.loading());

        repository.loadProductForSeller(productId, new SellerProductRepository.ProductCallback<ProductResponse>() {
            @Override
            public void onSuccess(ProductResponse data, String message) {
                productDetailState.postValue(ProductDetailState.success(data, message));
            }

            @Override
            public void onError(String message) {
                productDetailState.postValue(ProductDetailState.error(message));
            }
        });
    }

    public void loadCategories() {
        repository.loadCategories(new SellerProductRepository.ProductCallback<List<CategoryResponse>>() {
            @Override
            public void onSuccess(List<CategoryResponse> data, String message) {
                categoryState.postValue(data == null ? new ArrayList<>() : data);
            }

            @Override
            public void onError(String message) {
                categoryState.postValue(new ArrayList<>());
            }
        });
    }

    public void loadBrandSuggestions(String keyword) {
        repository.loadBrandSuggestions(keyword, new SellerProductRepository.ProductCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data, String message) {
                brandState.postValue(data == null ? new ArrayList<>() : data);
            }

            @Override
            public void onError(String message) {
                brandState.postValue(new ArrayList<>());
            }
        });
    }
}