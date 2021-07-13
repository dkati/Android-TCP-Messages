package com.serial.tcplistener;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.serial.tcplistener.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding __binder;
    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        __binder = ActivityMainBinding.inflate(getLayoutInflater());
        View view = __binder.getRoot();
        setContentView(view);
        mActivity = this;

    }
}