package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;
import com.gr6.smartcart_android.seller.product.response.VariantResponse;
import com.gr6.smartcart_android.seller.shop.repository.SellerShopRepository;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerProductsFragment extends Fragment {

    private static final String ARG_SHOP_ID = "shop_id";

    private static final String SORT_DEFAULT = "DEFAULT";
    private static final String SORT_STOCK_ASC = "STOCK_ASC";
    private static final String SORT_SOLD_DESC = "SOLD_DESC";
    private static final String SORT_RATING_DESC = "RATING_DESC";

    private RecyclerView recyclerProducts;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private TextView btnAddProduct;

    private TextView chipSortDefault;
    private TextView chipSortStock;
    private TextView chipSortSold;
    private TextView chipSortRating;

    private SellerProductManageAdapter adapter;
    private SellerProductViewModel viewModel;
    private SellerShopRepository shopRepository;

    private Long shopId;
    private boolean firstLoadDone = false;

    private String sortMode = SORT_DEFAULT;
    private final List<ProductResponse> allProducts = new ArrayList<>();

    public SellerProductsFragment() {
        super(R.layout.fragment_seller_products);
    }

    public static SellerProductsFragment newInstance(Long shopId) {
        SellerProductsFragment fragment = new SellerProductsFragment();
        Bundle args = new Bundle();

        if (shopId != null) {
            args.putLong(ARG_SHOP_ID, shopId);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        initViewModel();
        bindViews(view);
        setupRecyclerView();
        setupActions();
        observeViewModel();

        firstLoadDone = true;
        loadProducts();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null && firstLoadDone) {
            loadProducts();
        }
    }

    private void readArguments() {
        Bundle args = getArguments();

        if (args != null && args.containsKey(ARG_SHOP_ID)) {
            shopId = args.getLong(ARG_SHOP_ID);
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SellerProductViewModel.class);
        shopRepository = new SellerShopRepository(requireContext());
    }

    private void bindViews(@NonNull View view) {
        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);

        chipSortDefault = view.findViewById(R.id.chipSortDefault);
        chipSortStock = view.findViewById(R.id.chipSortStock);
        chipSortSold = view.findViewById(R.id.chipSortSold);
        chipSortRating = view.findViewById(R.id.chipSortRating);
    }

    private void setupRecyclerView() {
        adapter = new SellerProductManageAdapter(product -> {
            if (product == null || product.getProductId() == null) {
                Toast.makeText(requireContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), SellerProductDetailActivity.class);
            intent.putExtra(SellerProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
            startActivity(intent);
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerProducts.setAdapter(adapter);
        recyclerProducts.setHasFixedSize(false);
    }

    private void setupActions() {
        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddProductActivity.class))
        );

        if (chipSortDefault != null) {
            chipSortDefault.setOnClickListener(v -> changeSortMode(SORT_DEFAULT));
        }

        if (chipSortStock != null) {
            chipSortStock.setOnClickListener(v -> changeSortMode(SORT_STOCK_ASC));
        }

        if (chipSortSold != null) {
            chipSortSold.setOnClickListener(v -> changeSortMode(SORT_SOLD_DESC));
        }

        if (chipSortRating != null) {
            chipSortRating.setOnClickListener(v -> changeSortMode(SORT_RATING_DESC));
        }

        updateSortChips();
    }

    private void observeViewModel() {
        viewModel.getProductListState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            showLoading(state.isLoading());

            if (state.isSuccess()) {
                renderProducts(state.getProducts());
            } else if (state.isError()) {
                renderError(state.getMessage());
            }
        });
    }

    private void loadProducts() {
        if (shopId == null || shopId <= 0) {
            loadShopThenProducts();
            return;
        }

        viewModel.loadProductsByShop(shopId, 1, 100);
    }

    private void loadShopThenProducts() {
        showLoading(true);

        shopRepository.loadMyShopInfo(new SellerShopRepository.ShopCallback<SellerShopInfoResponse>() {
            @Override
            public void onSuccess(SellerShopInfoResponse data, String message) {
                shopId = data == null ? null : data.getShopId();

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (shopId == null || shopId <= 0) {
                        renderError("Không tìm thấy shop để tải sản phẩm");
                    } else {
                        viewModel.loadProductsByShop(shopId, 1, 100);
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() ->
                        renderError(message == null ? "Không tìm thấy shop" : message)
                );
            }
        });
    }

    private void renderProducts(List<ProductResponse> products) {
        showLoading(false);

        allProducts.clear();

        if (products != null) {
            allProducts.addAll(products);
        }

        applySortAndRender();
    }

    private void applySortAndRender() {
        List<ProductResponse> displayProducts = new ArrayList<>(allProducts);

        sortProducts(displayProducts);

        adapter.submitList(displayProducts);

        boolean empty = displayProducts.isEmpty();

        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            txtEmpty.setText("Chưa có sản phẩm nào. Bấm Thêm sản phẩm để đăng bán.");
        }
    }

    private void changeSortMode(String newSortMode) {
        sortMode = newSortMode == null || newSortMode.trim().isEmpty()
                ? SORT_DEFAULT
                : newSortMode;

        updateSortChips();
        applySortAndRender();
    }

    private void updateSortChips() {
        setSortChipActive(chipSortDefault, SORT_DEFAULT.equals(sortMode));
        setSortChipActive(chipSortStock, SORT_STOCK_ASC.equals(sortMode));
        setSortChipActive(chipSortSold, SORT_SOLD_DESC.equals(sortMode));
        setSortChipActive(chipSortRating, SORT_RATING_DESC.equals(sortMode));
    }

    private void setSortChipActive(TextView chip, boolean active) {
        if (chip == null || getContext() == null) return;

        chip.setSelected(active);
        chip.setTextColor(ContextCompat.getColor(
                requireContext(),
                active ? R.color.text_white : R.color.text_primary
        ));
        chip.setBackgroundResource(
                active
                        ? R.drawable.bg_seller_button_primary
                        : R.drawable.bg_seller_chip_soft
        );
    }

    private void sortProducts(List<ProductResponse> products) {
        if (products == null || products.size() <= 1) {
            return;
        }

        switch (sortMode) {
            case SORT_STOCK_ASC:
                products.sort((p1, p2) -> Integer.compare(totalStock(p1), totalStock(p2)));
                break;

            case SORT_SOLD_DESC:
                products.sort((p1, p2) -> Integer.compare(soldQuantity(p2), soldQuantity(p1)));
                break;

            case SORT_RATING_DESC:
                products.sort((p1, p2) -> Double.compare(averageRating(p2), averageRating(p1)));
                break;

            case SORT_DEFAULT:
            default:
                /*
                 * Giữ nguyên thứ tự API trả về để không ảnh hưởng hành vi cũ.
                 */
                break;
        }
    }

    private int totalStock(ProductResponse product) {
        if (product == null || product.getVariants() == null) {
            return 0;
        }

        int total = 0;

        for (VariantResponse variant : product.getVariants()) {
            if (variant != null) {
                total += Math.max(variant.getStockQuantity(), 0);
            }
        }

        return total;
    }

    private int soldQuantity(ProductResponse product) {
        if (product == null || product.getSoldQuantity() == null) {
            return 0;
        }

        return Math.max(product.getSoldQuantity(), 0);
    }

    private double averageRating(ProductResponse product) {
        if (product == null || product.getAverageRating() == null) {
            return 0.0;
        }

        return Math.max(product.getAverageRating(), 0.0);
    }

    private void renderError(String message) {
        showLoading(false);

        allProducts.clear();
        adapter.submitList(new ArrayList<>());

        recyclerProducts.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
        txtEmpty.setText(message == null ? "Không tải được sản phẩm" : message);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            txtEmpty.setVisibility(View.GONE);
        }
    }
}
