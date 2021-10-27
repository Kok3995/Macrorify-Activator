package com.kok_emm.activator;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BootReceiver extends BroadcastReceiver {
    @SuppressLint("CheckResult")
    @Override
    public void onReceive(Context context, Intent intent) {
        NativeService nativeService = new NativeService();

        Completable.fromAction(() -> {
                nativeService.start(context);
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {}, t -> {
            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
