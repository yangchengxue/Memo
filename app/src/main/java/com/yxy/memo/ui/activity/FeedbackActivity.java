package com.yxy.memo.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yxy.memo.R;
import com.yxy.memo.bean.getUserNoteAllTagsResponse;
import com.yxy.memo.httpservice.retrofitRequest;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**反馈留言界面*/
public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText et_content;
    private TextView RemainText;
    private TagFlowLayout mFlowLayout;
    private String[] mVals = new String[]{"功能", "bug", "建议", "其他"};
    private List<String> type = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        init();
    }

    /**
     * @method  init
     * @date: 2020/1/14 22:58
     * @return void
     * @description 方法的作用:初始化控件和布局
     */
    private void init(){
        et_content = findViewById(R.id.et_content);
        RemainText = findViewById(R.id.RemainText);
        mFlowLayout = findViewById(R.id.id_flowlayout);
        findViewById(R.id.tv_header_back).setOnClickListener(this);
        findViewById(R.id.bt_save).setOnClickListener(this);
        final LayoutInflater mInflater = LayoutInflater.from(this);
        EditTextSet(et_content,RemainText,R.string.ETfeedBack,"1000");

        mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener()
        {
            @Override
            public void onSelected(Set<Integer> selectPosSet)
            {
                Iterator<Integer> value = selectPosSet.iterator();
                type.clear();
                while(value.hasNext())//判断是否有下一个
                {
                    type.add(mVals[value.next()]);
                }
            }
        });
        /**设置标签布局*/
        mFlowLayout.setAdapter(new TagAdapter<String>(mVals) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.tv,
                        mFlowLayout, false);
                tv.setText(s);
                return tv;
            }

        });
    }

    /**
     * @method  onClick
     * @date: 2020/1/14 22:56
     * @return void
     * @description 方法的作用：控件的点击
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_header_back:   //返回键
                finish();
                break;
            case R.id.bt_save:  //提交
                Submission();
                break;
        }
    }

    /**
     * @method  Submission
     * @date: 2020/1/14 22:55
     * @return void
     * @description 方法的作用：提交用户的反馈类型和反馈的文本，并作相关逻辑处理
     */
    public void Submission(){
        StringBuilder sss = new StringBuilder();
        for (int i=0; i<type.size(); i++){
            sss.append(type.get(i)).append(",");
        }
        if (!et_content.getText().toString().equals("") && sss.toString().equals("")){ //如果内容不为空且类型为空
            Toast.makeText(FeedbackActivity.this,"请选择反馈类型",Toast.LENGTH_SHORT).show();
        } else if (et_content.getText().toString().equals("") && !sss.toString().equals("")){ //如果内容为空且类型不为空
            Toast.makeText(FeedbackActivity.this,"请输入您的意见或建议",Toast.LENGTH_SHORT).show();
        } else if (et_content.getText().toString().equals("") && sss.toString().equals("")){ //如果内容为空且类型为空
            Toast.makeText(FeedbackActivity.this,"请输入您的意见或建议",Toast.LENGTH_SHORT).show();
        } else if (!et_content.getText().toString().equals("") && !sss.toString().equals("")){ //如果内容不为空且类型不为空
            postFeedBack(et_content.getText().toString(),sss.toString());
        }
    }

    /**
     * @method  EditTextSet
     * @date: 2020/1/14 22:49
     * @param editText Edittext控件
     * @param RemainText 显示剩余字数的TextView控件
     * @param hint 提示的文字
     * @param Remain 可输入的字数
     * @return void
     * @description 方法的作用：配置EditText以及监听字数
     */
    private void EditTextSet(EditText editText, final TextView RemainText, int hint, final String Remain) {
        editText.setHint(hint);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                RemainText.setText(s.length()+"/"+Remain);
            }
        });
    }

    /**
     * @method  postFeedBack
     * @date: 2020/1/14 22:48
     * @param content 反馈的内容
     * @param type 反馈的类型
     * @return void
     * @description 方法的作用：用于提交用户的反馈信息
     */
    private void postFeedBack(String content, String type){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.postFeedBack("Bearer " + token, content, type)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("postFeedBack   ", e.getMessage());
                        }

                        @Override
                        public void onNext(String data) {
                            if (data.equals("true")){
                                Toast.makeText(FeedbackActivity.this,"提交成功，感谢您的反馈！",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
        }
    }

}
