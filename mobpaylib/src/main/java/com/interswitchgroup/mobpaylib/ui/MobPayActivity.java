package com.interswitchgroup.mobpaylib.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.interswitchgroup.mobpaylib.MobPay;
import com.interswitchgroup.mobpaylib.R;
import com.interswitchgroup.mobpaylib.api.model.ErrorWrapper;
import com.interswitchgroup.mobpaylib.api.model.TransactionResponse;
import com.interswitchgroup.mobpaylib.databinding.ActivityMobPayBinding;
import com.interswitchgroup.mobpaylib.interfaces.TransactionFailureCallback;
import com.interswitchgroup.mobpaylib.interfaces.TransactionSuccessCallback;
import com.interswitchgroup.mobpaylib.model.Customer;
import com.interswitchgroup.mobpaylib.model.Merchant;
import com.interswitchgroup.mobpaylib.model.Payment;
import com.interswitchgroup.mobpaylib.ui.fragments.PlaceHolderFragment;
import com.interswitchgroup.mobpaylib.ui.fragments.card.CardPaymentFragment;
import com.interswitchgroup.mobpaylib.ui.fragments.card.PaymentVm;
import com.interswitchgroup.mobpaylib.utils.AndroidUtils;
import com.interswitchgroup.mobpaylib.utils.NetUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class MobPayActivity extends DaggerAppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public Merchant merchant;
    public Customer customer;
    public Payment payment;
    public String clientId;
    public String clientSecret;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private PaymentVm paymentVm;
    private MobPay mobPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.merchant = (Merchant) getIntent().getSerializableExtra("merchant");
        this.customer = (Customer) getIntent().getSerializableExtra("customer");
        this.payment = (Payment) getIntent().getSerializableExtra("payment");
        this.clientId = getIntent().getStringExtra("clientId");
        this.clientSecret = getIntent().getStringExtra("clientSecret");
        this.mobPay = MobPay.getInstance(this.clientId, this.clientSecret);
        paymentVm = ViewModelProviders.of(this, viewModelFactory).get(PaymentVm.class);
        paymentVm.setMobPay(mobPay);
        paymentVm.setCustomer(this.customer);
        paymentVm.setMerchant(this.merchant);
        paymentVm.setPayment(this.payment);
        paymentVm.setOnSuccess(new TransactionSuccessCallback() {
            @Override
            public void onSuccess(final TransactionResponse transactionResponse) {
                paymentVm.getLoading().set(false);
                View dialogView = LayoutInflater.from(MobPayActivity.this).inflate(R.layout.result_dialog, (ViewGroup) getWindow().getDecorView().getRootView(), false);
                TextView title = dialogView.findViewById(R.id.dialog_result_title);
                ImageView imageView = dialogView.findViewById(R.id.dialog_image);
                TextView message = dialogView.findViewById(R.id.dialog_message);
                imageView.setImageDrawable(ContextCompat.getDrawable(MobPayActivity.this, R.drawable.happy_face));
                title.setText(R.string.payment_successful_title);
                message.setText("Your transaction was completed successfully, your payment reference is " + transactionResponse.getTransactionOrderId());
                final AlertDialog dialog = new AlertDialog.Builder(MobPayActivity.this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mobPay.getTransactionSuccessCallback().onSuccess(transactionResponse);
                        MobPayActivity.this.finish();
                    }
                });
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.mobpaySuccess));
                    }
                });
                dialog.show();
            }
        });
        paymentVm.setOnFailure(new TransactionFailureCallback() {
            @Override
            public void onError(final Throwable error) {
                paymentVm.getLoading().set(false);
                View dialogView = LayoutInflater.from(MobPayActivity.this).inflate(R.layout.result_dialog, (ViewGroup) getWindow().getDecorView().getRootView(), false);
                ImageView imageView = dialogView.findViewById(R.id.dialog_image);
                TextView title = dialogView.findViewById(R.id.dialog_result_title);
                TextView message = dialogView.findViewById(R.id.dialog_message);
                imageView.setImageDrawable(ContextCompat.getDrawable(MobPayActivity.this, R.drawable.sad_face));
                title.setText(R.string.payment_failed_title);
                ErrorWrapper errorWrapper = NetUtil.parseError(error);
                if (errorWrapper != null) {
                    message.setText(errorWrapper.getError().getCode() + " " + errorWrapper.getError().getMessage() + " " + errorWrapper.getError().getStatusMessage());
                } else {
                    message.setText(error.getMessage());
                }
                message.setText(R.string.failed_payment_message);
                AlertDialog dialog = new AlertDialog.Builder(MobPayActivity.this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MobPayActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mobPay.getTransactionFailureCallback().onError(error);
                        MobPayActivity.this.finish();
                    }
                });
                dialog.show();
            }
        });
        ActivityMobPayBinding activityMobPayBinding = DataBindingUtil.setContentView(this, R.layout.activity_mob_pay);
        activityMobPayBinding.setLifecycleOwner(this);
        activityMobPayBinding.setPaymentVm(paymentVm);

        final FrameLayout progressOverlay = activityMobPayBinding.loading.progressOverlay;
        Glide.with(this)
                .load(R.drawable.running_man)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into((ImageView) findViewById(R.id.running_man));
        paymentVm.getLoading().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (((ObservableBoolean) sender).get()) {
                    try {
                        // Try to hide keyboard
                        // Check if no view has focus:
                        View view = MobPayActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AndroidUtils.animateView(progressOverlay, View.VISIBLE, 1, 200);
                } else {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                }
            }
        });

        Toolbar toolbar = activityMobPayBinding.toolbar;
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        TabLayout tabLayout = findViewById(R.id.tabs);

        final MobPay.PaymentChannel[] allChannels = MobPay.PaymentChannel.class.getEnumConstants();
        final List<MobPay.PaymentChannel> mobPayChannels = MobPay.getChannels();
        for (MobPay.PaymentChannel paymentChannel : allChannels) {
            mSectionsPagerAdapter.addTab(paymentChannel);
        }

        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (!mobPayChannels.contains(allChannels[i])) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                View tabView = tab.view;
                tabView.setEnabled(false);
                tabView.setClickable(false);
            }
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int currentPosition;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int newPosition) {
                int nextPosition = newPosition;
                do {
                    if (mobPayChannels.contains(allChannels[nextPosition])) {
                        mViewPager.setCurrentItem(nextPosition);
                        break;
                    }
                    if (newPosition > currentPosition) {
                        nextPosition++;
                    } else {
                        nextPosition--;
                    }
                    nextPosition = Math.abs(nextPosition % allChannels.length);
                } while (nextPosition != newPosition);

                currentPosition = mViewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(Arrays.asList(allChannels).indexOf(mobPayChannels.get(0)));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<MobPay.PaymentChannel> paymentChannels = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CardPaymentFragment();
                default:
                    return PlaceHolderFragment.newInstance(paymentChannels.get(position).value);
            }
        }

        @Override
        public int getCount() {
            return paymentChannels.size();
        }

        public void addTab(MobPay.PaymentChannel paymentChannel) {
            paymentChannels.add(paymentChannel);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return paymentChannels.get(position).value;
        }
    }

    @Override
    public void onBackPressed() {
        this.quit();
    }

    public void quit() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        mobPay.getTransactionFailureCallback().onError(new Exception(getString(R.string.user_cancelled_payment_message)));
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to quit?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();      //Hurry up and free that precious RAM!
    }
}
