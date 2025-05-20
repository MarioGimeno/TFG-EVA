package com.example.appGrabacion.contracts;

import java.util.List;

public interface GenericListContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showItems(List<?> items);
        void showError(String message);
    }
    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadItems(String type, int categoryId);
    }
}
