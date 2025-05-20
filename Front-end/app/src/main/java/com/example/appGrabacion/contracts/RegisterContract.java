package com.example.appGrabacion.contracts;

public interface RegisterContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void register(String fullName, String email, String password);
    }

    interface Service {
        void performRegister(String fullName, String email, String password, Callback callback);
        interface Callback {
            void onSuccess(String token);
            void onError(String errorMessage);
        }

    }
}
