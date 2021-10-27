package com.kok_emm.activator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.kok_emm.activator.databinding.ActivityMainBinding;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMainBinding binding;
    Disposable disposable;
    NativeService nativeService;
    private boolean lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_main, null, false);

        binding.mainActivate.setOnClickListener(this);

        nativeService = new NativeService();

        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        if (disposable != null)
            disposable.dispose();
    }

    @Override
    public void onClick(View view) {
        if (lock)
            return;

        lock = true;

        disposable = Completable.fromAction(() -> {
            if (nativeService.isServiceRunning()) {
                nativeService.stop();
            } else {
                nativeService.start(this);
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
            this.checkStatus();
            lock = false;
        }, t -> {
            this.checkStatus();
            Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
            lock = false;
        });
    }

    private void checkStatus() {
        if (binding == null)
            return;

        if (nativeService.isServiceRunning()) {
            binding.mainStatus.setText("Running");
            binding.mainStatus.setTextColor(Color.GREEN);
            binding.mainActivate.setText("STOP");
        } else {
            binding.mainStatus.setText("Not Running");
            binding.mainStatus.setTextColor(Color.RED);
            binding.mainActivate.setText("START");
        }
    }
}