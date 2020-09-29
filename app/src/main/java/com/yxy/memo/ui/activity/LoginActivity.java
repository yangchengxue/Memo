package com.yxy.memo.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.yxy.memo.R;
import com.yxy.memo.bean.LoginResponse;
import com.yxy.memo.httpservice.retrofitRequest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private AutoCompleteTextView at_userName;
    private EditText at_userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        at_userName = findViewById(R.id.at_userName);
        at_userPassword = findViewById(R.id.at_userPassword);
        findViewById(R.id.tv_header_back).setOnClickListener(this);
        findViewById(R.id.bt_sign).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_header_back:
                finish();
                break;
            case R.id.bt_sign:
                //登录
                LoginUser(at_userName.getText().toString(),at_userPassword.getText().toString());
                break;
            case R.id.register:
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
                break;
        }
    }

    /**
     * @method  LoginUser
     * @date: 2019/12/26 22:34
     * @param uname 用户名
     * @param upassword 密码
     * @return void
     * @description 用户的登录
     */
    public void LoginUser(String uname, String upassword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        request.LoginUser(uname, upassword)
                .subscribeOn(Schedulers.io())//IO线程加载数据
                .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                .subscribe(new Subscriber<LoginResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(LoginActivity.this, "登录失败" + e.getMessage() , Toast.LENGTH_SHORT).show();
                        Log.d("denglushibai   ",e.getMessage());
                    }

                    @Override
                    public void onNext(LoginResponse data) {
                        Toast.makeText(LoginActivity.this, "欢迎您，" + data.getUname(), Toast.LENGTH_SHORT).show();
                        saveTokenInSharedPreferences(data.getToken()); //保存token
                        finish();
                    }
                });
    }

    /**
     * @method  saveTokenInSharedPreferences
     * @date: 2019/12/25 17:21
     * @param token 登录成功后服务器返回的token
     * @return void
     * @description 将登录成功后服务器返回的token通过SharedPreferences保存起来
     */
    public void saveTokenInSharedPreferences(String token){
        SharedPreferences.Editor editor = getSharedPreferences("Token",MODE_PRIVATE).edit();
        editor.putString("token",token);
        editor.apply();
    }
}
