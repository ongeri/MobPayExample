package com.interswitchgroup.mobpaylib.ui.fragments.card;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;

import com.interswitchgroup.mobpaylib.MobPay;
import com.interswitchgroup.mobpaylib.interfaces.TransactionFailureCallback;
import com.interswitchgroup.mobpaylib.interfaces.TransactionSuccessCallback;
import com.interswitchgroup.mobpaylib.model.Card;
import com.interswitchgroup.mobpaylib.model.Customer;
import com.interswitchgroup.mobpaylib.model.Merchant;
import com.interswitchgroup.mobpaylib.model.Payment;

import javax.inject.Inject;

public class PaymentVm extends ViewModel {
    private String LOG_TAG = this.getClass().getSimpleName();
    private MobPay mobPay;
    private ObservableBoolean loading = new ObservableBoolean(false);
    private Merchant merchant;
    private Payment payment;
    private Customer customer;
    private TransactionSuccessCallback onSuccess;
    private TransactionFailureCallback onFailure;

    @Inject
    public PaymentVm() {
    }

    public MobPay getMobPay() {
        return mobPay;
    }

    public void setMobPay(MobPay mobPay) {
        this.mobPay = mobPay;
    }

    public ObservableBoolean getLoading() {
        return loading;
    }

    public void setLoading(ObservableBoolean loading) {
        this.loading = loading;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setOnSuccess(TransactionSuccessCallback onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnFailure(TransactionFailureCallback onFailure) {
        this.onFailure = onFailure;
    }

    public void makeCardPayment(Card card) {
        loading.set(true);
        mobPay.makeCardPayment(
                card,
                merchant,
                payment,
                customer,
                onSuccess,
                onFailure);
    }
}
