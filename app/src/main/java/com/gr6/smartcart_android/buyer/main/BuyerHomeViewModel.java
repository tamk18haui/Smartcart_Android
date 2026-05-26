package com.gr6.smartcart_android.buyer.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.main.repository.BuyerHomeRepository;
import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.buyer.main.response.RecommendationPageResponse;
import com.gr6.smartcart_android.common.storage.TokenManager;

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

    private String currentKeyword = "";
    private Long currentCategoryId = null;

    public BuyerHomeViewModel(@NonNull Application application) {
        super(application);
        repository = new BuyerHomeRepository(application);
    }

    public LiveData<BuyerHomeState> getHomeState() {
        return homeState;
    }

    public void loadHome() {
        firstLoading = true;
        currentKeyword = "";
        currentCategoryId = null;
        currentPage = 0;
        lastPage = false;

        homeState.setValue(BuyerHomeState.loading());

        repository.getCategories(new BuyerHomeRepository.HomeCallback<List<HomeCategoryResponse>>() {
            @Override
            public void onSuccess(List<HomeCategoryResponse> categories) {
                cachedCategories.clear();

                if (categories != null) {
                    cachedCategories.addAll(categories);
                }

                loadAiHomePage(0, true);
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

    public void searchProducts(String keyword) {
        currentKeyword = keyword == null ? "" : keyword.trim();
        currentCategoryId = null;

        if (currentKeyword.isEmpty()) {
            loadAiHomePage(0, true);
        } else {
            loadAiSearchPage(0, true);
        }
    }

    public void filterByCategory(Long categoryId) {
        currentKeyword = "";
        currentCategoryId = categoryId;
        loadNormalProductsPage(0, true);
    }

    public void clearCategoryFilter() {
        currentKeyword = "";
        currentCategoryId = null;
        loadAiHomePage(0, true);
    }

    public void loadMoreProducts() {
        if (loadingMore || lastPage || firstLoading) return;

        int nextPage = currentPage + 1;

        if (isAiHomeMode()) {
            loadAiHomePage(nextPage, false);
        } else if (isAiSearchMode()) {
            loadAiSearchPage(nextPage, false);
        } else {
            loadNormalProductsPage(nextPage, false);
        }
    }

    private boolean isAiHomeMode() {
        boolean emptyKeyword = currentKeyword == null || currentKeyword.trim().isEmpty();
        return emptyKeyword && currentCategoryId == null;
    }

    private boolean isAiSearchMode() {
        boolean hasKeyword = currentKeyword != null && !currentKeyword.trim().isEmpty();
        return hasKeyword && currentCategoryId == null;
    }

    private void loadAiHomePage(
            int page,
            boolean reset
    ) {
        preparePageState(reset);

        boolean hasToken = TokenManager.getInstance(getApplication()).hasToken();

        if (hasToken) {
            repository.getAiPersonal(
                    page,
                    PAGE_SIZE,
                    new BuyerHomeRepository.HomeCallback<RecommendationPageResponse>() {
                        @Override
                        public void onSuccess(RecommendationPageResponse data) {
                            handleAiSuccess(data, reset);
                        }

                        @Override
                        public void onError(String message) {
                            loadAiTrendingPage(page, reset);
                        }
                    }
            );
        } else {
            loadAiTrendingPage(page, reset);
        }
    }

    private void loadAiTrendingPage(
            int page,
            boolean reset
    ) {
        repository.getAiTrending(
                page,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<RecommendationPageResponse>() {
                    @Override
                    public void onSuccess(RecommendationPageResponse data) {
                        handleAiSuccess(data, reset);
                    }

                    @Override
                    public void onError(String message) {
                        finishLoading();

                        if (cachedProducts.isEmpty()) {
                            homeState.postValue(BuyerHomeState.error(message));
                        }
                    }
                }
        );
    }

    private void loadAiSearchPage(
            int page,
            boolean reset
    ) {
        preparePageState(reset);

        repository.getAiSearch(
                currentKeyword,
                page,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<RecommendationPageResponse>() {
                    @Override
                    public void onSuccess(RecommendationPageResponse data) {
                        handleAiSuccess(data, reset);
                    }

                    @Override
                    public void onError(String message) {
                        loadNormalProductsPage(page, reset);
                    }
                }
        );
    }

    private void loadNormalProductsPage(
            int page,
            boolean reset
    ) {
        preparePageState(reset);

        SearchProductRequest request = SearchProductRequest.ofKeywordAndCategory(
                currentKeyword,
                currentCategoryId
        );

        repository.searchProducts(
                request,
                page,
                PAGE_SIZE,
                new BuyerHomeRepository.HomeCallback<ProductPageResponse>() {
                    @Override
                    public void onSuccess(ProductPageResponse data) {
                        finishLoading();

                        if (data == null) {
                            postCurrentHome();
                            return;
                        }

                        currentPage = data.getPageIndexZeroBased();
                        lastPage = data.isLast();

                        if (reset) {
                            cachedProducts.clear();
                        }

                        cachedProducts.addAll(data.getProducts());

                        postCurrentHome();
                    }

                    @Override
                    public void onError(String message) {
                        finishLoading();

                        if (cachedProducts.isEmpty()) {
                            homeState.postValue(BuyerHomeState.error(message));
                        }
                    }
                }
        );
    }

    private void handleAiSuccess(
            RecommendationPageResponse data,
            boolean reset
    ) {
        finishLoading();

        if (data == null) {
            postCurrentHome();
            return;
        }

        currentPage = data.getPageIndexZeroBased();
        lastPage = data.isLast();

        if (reset) {
            cachedProducts.clear();
        }

        cachedProducts.addAll(data.getProducts());

        postCurrentHome();
    }

    private void preparePageState(boolean reset) {
        if (reset) {
            firstLoading = true;
            currentPage = 0;
            lastPage = false;
            cachedProducts.clear();
            homeState.setValue(BuyerHomeState.loading());
        } else {
            loadingMore = true;
        }
    }

    private void finishLoading() {
        firstLoading = false;
        loadingMore = false;
    }

    private void postCurrentHome() {
        homeState.postValue(BuyerHomeState.success(
                new ArrayList<>(cachedCategories),
                new ArrayList<>(cachedProducts)
        ));
    }
}