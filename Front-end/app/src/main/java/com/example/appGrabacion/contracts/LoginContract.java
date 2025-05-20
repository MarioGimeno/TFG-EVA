package com.example.appGrabacion.contracts;

public interface LoginContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void navigateToMain();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void performLogin(String email, String password);
    }
}
