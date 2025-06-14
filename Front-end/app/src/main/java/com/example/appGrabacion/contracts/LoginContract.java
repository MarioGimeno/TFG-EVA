package com.example.appGrabacion.contracts;

public interface LoginContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void navigateToMain();
        void showLoginSuccess(String userName);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void performLogin(String email, String password);
    }

    interface Service {
        public interface LoginCallback {
            void onSuccess(String token, String fullName);
            void onError(Throwable t);
        }

        void login(String email, String password, LoginCallback callback);
    }
}
