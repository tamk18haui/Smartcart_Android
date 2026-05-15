package com.gr6.smartcart_android.buyer.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.main.repository.BuyerHomeRepository;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;

import java.util.ArrayList;
import java.util.List;

public class BuyerHomeViewModel extends AndroidViewModel {

    private static final int PAGE_SIZE = 10;

    private final BuyerHomeRepository repository;
    private final MutableLiveData<BuyerHomeState> homeState = new MutableLiveData<>();

    private final List<HomeCategoryResponse> cachedCategories = new ArrayList<>();
    private final List<HomeProductResponse> cachedProducts = new ArrayList<>();

    private int currentPage = 0;
    private boolean lastPage = false;
    private boolean loadingMore = false;
    private boolean firstLoading = false;

    public BuyerHomeViewModel(@NonNull Application application) {
        super(application);
        repository = new BuyerHomeRepository(application);
    }

    public LiveData<BuyerHomeState> getHomeState() {
        return homeState;
    }

    public void loadHome() {
        currentPage = 0;
        lastPage = false;
        loadingMore = false;
        firstLoading = true;

        cachedCategories.clear();
        cachedProducts.clear();

        homeState.setValue(BuyerHomeState.loading());

        repository.getCategories(new BuyerHomeRepository.HomeCallback<List<HomeCategoryResponse>>() {
            @Override
            public void onSuccess(List<HomeCategoryResponse> categories) {
                cachedCategories.clear();

                if (categories != null) {
                    cachedCategories.addAll(categories);
                }

                loadProductsPage(0, true);
            }

            @Override
            public void onError(String message) {
                firstLoading = false;
                homeState.postValue(BuyerHomeState.error(message));
            }
        });
    }

    public void refreshHome() {
        loadHome();
    }

    public void loadMoreProducts() {
        if (firstLoading || loadingMore || lastPage) return;

        loadProductsPage(currentPage + 1, false);
    }

    private void loadProductsPage(int page, boolean firstPage) {
        if (firstPage) {
            firstLoading = true;
        } else {
            loadingMore = true;
        }

        repository.getHomeProductsPage(
                page,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<ProductPageResponse>() {
                    @Override
                    public void onSuccess(ProductPageResponse pageData) {
                        firstLoading = false;
                        loadingMore = false;

                        if (pageData == null) {
                            lastPage = true;
                            homeState.postValue(BuyerHomeState.success(
                                    new ArrayList<>(cachedCategories),
                                    new ArrayList<>(cachedProducts)
                            ));
                            return;
                        }

                        if (firstPage) {
                            cachedProducts.clear();
                        }

                        cachedProducts.addAll(pageData.getContent());
                        currentPage = pageData.getNumber();
                        lastPage = pageData.isLast();

                        homeState.postValue(BuyerHomeState.success(
                                new ArrayList<>(cachedCategories),
                                new ArrayList<>(cachedProducts)
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        firstLoading = false;
                        loadingMore = false;

                        if (cachedProducts.isEmpty()) {
                            homeState.postValue(BuyerHomeState.error(message));
                        }
                    }
                }
        );
    }
}