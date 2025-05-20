package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.RegisterContract;

public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View view;
    private final RegisterContract.Service service;

    public RegisterPresenter(RegisterContract.Service service) {
        this.service = service;
    }

    @Override
    public void attachView(RegisterContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void register(String fullName, String email, String password) {
        if (view == null) return;
        view.showLoading();

        service.performRegister(fullName, email, password, new RegisterContract.Service.Callback() {
            @Override
            public void onSuccess(String token) {
                if (view == null) return;
                view.hideLoading();
                view.showSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                if (view == null) return;
                view.hideLoading();
                view.showError(errorMessage);
            }
        });
    }

}
