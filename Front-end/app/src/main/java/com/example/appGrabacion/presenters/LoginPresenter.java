package com.example.appGrabacion.presenters;

import android.content.Context;

import com.example.appGrabacion.contracts.LoginContract;
import com.example.appGrabacion.services.LoginService;

public class LoginPresenter implements LoginContract.Presenter {
    private LoginContract.View view;
    private final LoginService service;

    public LoginPresenter(Context ctx) {
        this.service = new LoginService(ctx);
    }

    @Override
    public void attachView(LoginContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void performLogin(String email, String password) {
        if (view != null) view.showLoading();

        service.login(email, password, new LoginService.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                if (view != null) {
                    view.hideLoading();
                    view.navigateToMain();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (view != null) {
                    view.hideLoading();
                    view.showError(t.getMessage());
                }
            }
        });
    }
}
