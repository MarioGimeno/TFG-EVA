package com.example.appGrabacion.contracts;

public interface RegisterContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showSuccess();
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void register(String email, String password);
    }

    interface Service {
        interface Callback {
            void onSuccess();
            void onError(String errorMessage);
        }
        void performRegister(String email, String password, Callback callback);
    }
}
