package com.interswitchgroup.mobpaylib.di.vms;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.interswitchgroup.mobpaylib.ui.fragments.card.CardVm;
import com.interswitchgroup.mobpaylib.ui.fragments.card.PaymentVm;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(CardVm.class)
    abstract ViewModel bindMapCardVm(CardVm cardVm);

    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(PaymentVm.class)
    abstract ViewModel bindMapPaymentVm(PaymentVm paymentVm);

    @Binds
    @Singleton
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory viewModelFactory);
}