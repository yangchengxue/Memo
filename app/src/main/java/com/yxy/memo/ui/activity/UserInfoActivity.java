package com.yxy.memo.ui.activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.yxy.memo.R;
import com.yxy.memo.bean.getUserInfoResponse;
import com.yxy.memo.httpservice.retrofitRequest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SimpleDraweeView userDrawee;  //用户头像
    private EditText tv_userName;  //用户名
    private EditText tv_mobilephone;  //用户手机号
    private TextView tv_uid;  //用户UID
    private CheckBox CK_boy; //男按钮
    private CheckBox CK_girl; //女按钮
    private String uname = "",ugender = ""; //用户名，性别


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        userDrawee = findViewById(R.id.userDrawee);
        tv_userName = findViewById(R.id.tv_userName);
        tv_mobilephone = findViewById(R.id.tv_mobilephone);
        tv_uid = findViewById(R.id.tv_uid);
        CK_boy = findViewById(R.id.CK_boy);
        CK_girl = findViewById(R.id.CK_girl);
        findViewById(R.id.tv_header_back).setOnClickListener(this);
        findViewById(R.id.RL_1).setOnClickListener(this);
        findViewById(R.id.tv_header_right).setOnClickListener(this);
        findViewById(R.id.bt_exitLogin).setOnClickListener(this);
        CK_boy.setOnCheckedChangeListener(this);
        CK_girl.setOnCheckedChangeListener(this);
//        if (getSexData().equals("男")) CK_boy.setChecked(true);
//        if (getSexData().equals("女")) CK_girl.setChecked(true);

        getCurrentUserInfo();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_header_back:
                finish();
                break;
            case R.id.RL_1:

                break;
            case R.id.tv_header_right:
                if (CK_boy.isChecked()) {
                    ugender = "男";
                } else if (CK_girl.isChecked()) {
                    ugender = "女";
                }
                UpdateUserInfo(uname,tv_mobilephone.getText().toString(),ugender);
                break;
            case R.id.bt_exitLogin: //退出登录按钮
                exitLogin();
                break;
        }
    }

    /**
     * @method  getCurrentUserInfo
     * @date: 2019/12/26 16:58
     * @return void
     * @description 获取当前用户的信息，并且如果获取成功，则第一个按钮显示为“退出登录”
     */
    private void getCurrentUserInfo(){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.getUserAllInfo("Bearer " + token)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<getUserInfoResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(UserInfoActivity.this,"Token凭证过期，请重新登录",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(getUserInfoResponse data) {
                            String uicoPath = data.getUicon();
                            if (uicoPath != null){ //如果用户有头像
                                userDrawee.setImageURI(Uri.parse("http://182.92.208.230:8080/image/" + uicoPath.substring(16)));
                            }
                            tv_uid.setText(data.getUid());
                            tv_userName.setText(data.getUname());
                            uname = data.getUname();
                            String gender = data.getGender();
                            if (gender != null){
                                if (gender.equals("男")) CK_boy.setChecked(true);
                                if (gender.equals("女")) CK_girl.setChecked(true);
                                ugender = gender;
                            }
                            if (data.getUmobile() != null){
                                tv_mobilephone.setText(data.getUmobile());
                            }
//                            tv_loginStatus.setText("退出登录");
                        }
                    });
        }
    }

    /**
     * @method  getCurrentUserInfo
     * @date: 2019/12/26 16:58
     * @return void
     * @description 用户提交修改用户的信息
     */
    private void UpdateUserInfo(String uname,String umobile,String gender){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.UpdateUserInfo("Bearer " + token,uname,umobile,gender)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(String data) {
                            if (data.equals("true")){
                                Toast.makeText(UserInfoActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
        }
    }

    /**
     * @method  exitLogin
     * @date: 2020/1/15 14:33
     * @return void
     * @description 方法的作用：用于退出登录
     */
    private void exitLogin(){
        saveTokenInSharedPreferences("");
        Toast.makeText(UserInfoActivity.this,"已退出登录",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton == CK_boy && isChecked) {
            CK_girl.setChecked(false);
            CK_boy.setChecked(true);
        }
        if (compoundButton == CK_girl && isChecked) {
            CK_boy.setChecked(false);
            CK_girl.setChecked(true);
        }
    }

    /**
     * @method  saveTokenInSharedPreferences
     * @date: 2019/12/26 17:06
     * @param token 之前保存在本地的token
     * @return void
     * @description 方法的作用：用于将保存在本地的token的值改变
     */
    public void saveTokenInSharedPreferences(String token){
        SharedPreferences.Editor editor = getSharedPreferences("Token",MODE_PRIVATE).edit();
        editor.putString("token",token);
        editor.apply();
    }
}
