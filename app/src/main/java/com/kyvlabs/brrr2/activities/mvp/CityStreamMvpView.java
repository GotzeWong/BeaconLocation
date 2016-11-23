package com.kyvlabs.brrr2.activities.mvp;


import com.kyvlabs.brrr2.activities.base.MvpView;

public interface CityStreamMvpView extends MvpView {
//    void addCard(Beacon beacon);

    void showError(String s);

    void stopLoadIndicator();
}
