package com.yxy.memo.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yxy.memo.R;
import com.yxy.memo.httpservice.retrofitRequest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterUserActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_userName;
    private EditText et_userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        findViewById(R.id.tv_header_back).setOnClickListener(this);
        findViewById(R.id.bt_Register).setOnClickListener(this);
        et_userName = findViewById(R.id.et_userName);
        et_userPassword = findViewById(R.id.et_userPassword);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_header_back:
                finish();
                break;
            case R.id.bt_Register:
                RegisterUser(et_userName.getText().toString(),et_userPassword.getText().toString());
                break;
        }
    }

    public void RegisterUser(String uname, String upassword) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        request.RegisterUser(uname, upassword)
                .subscribeOn(Schedulers.io())//IO线程加载数据
                .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(RegisterUserActivity.this, "注册失败" + e.getMessage() , Toast.LENGTH_SHORT).show();
                        Log.d("ggggg   ",e.getMessage());
                    }

                    @Override
                    public void onNext(String data) {
                        Toast.makeText(RegisterUserActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
