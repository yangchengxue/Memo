package com.yxy.memo.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yxy.memo.R;
import com.yxy.memo.bean.getNoteByNoteidResponse;
import com.yxy.memo.bean.getUserNoteAllTagsResponse;
import com.yxy.memo.bean.saveNoteResponse;
import com.yxy.memo.httpservice.retrofitRequest;
import com.yxy.memo.utils.CalendarReminderUtils;
import com.yxy.memo.utils.EmojiExcludeFilter;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.feezu.liuli.timeselector.TimeSelector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditNoteActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView tv_editTime; //当前笔记的上次保存的时间
    //流式布局
    private TagFlowLayout mFlowLayout;
    private List<String> type = new ArrayList<>();
    private List<String> TagValues = new ArrayList<>();

    private String noteid; //当前的笔记的id
    private String tagName; //当前的标签名称
    private Drawable drawable = null; //当前的笔记的图片
    private String photoLocalUrl = ""; //图片的本地Url
    private int textSize = 15; //文本的大小 ,默认是15dp
    private int textColor = -16777216; //文本的颜色 ,默认是黑色
    private String NotealarmTime = ""; //笔记提醒的时间
    private EditText et_content; //编辑的笔记
    private TextView tv_textSize; //显示字号的控件
    private TextView tv_alarmTimeTip; //提醒的时间
    private LinearLayout LL_alarmTip; //设置了提醒的话，就会出现这个布局
    private File notePhotoFile = null;  //插入笔记中的图片文件
    private int photoIndex = -1; //图片的插入位置
    private ProgressBar progressBar;
    private SeekBar seekBar; //字体调整大小的进度条
    private LinearLayout LL_contentEdit; //编辑文本打开时的布局
    private LinearLayout l5; //取消置顶的线性布局
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        init(); //初始化控件和布局
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_header_back:
                showTipDialog();
                break;
            case R.id.tv_header_right: //保存
                if (notePhotoFile == null){ //如果没有从相册中读取图片
                    if (tagName != null){
                        photoIndex = et_content.getText().toString().indexOf("<photo>");
                        saveNoteWithNoPhoto(tagName,et_content.getText().toString() + "\n", textSize,textColor, String.valueOf(photoIndex),NotealarmTime);
                    } else {
                        Toast.makeText(EditNoteActivity.this,"请选择分类",Toast.LENGTH_SHORT).show();
                    }
                } else { //如果从相册中读取了图片
                    if (tagName != null){
                        saveNoteWithPhoto(tagName,et_content.getText().toString() + "\n", textSize,textColor, String.valueOf(photoIndex),photoLocalUrl,NotealarmTime,notePhotoFile);
                    } else {
                        Toast.makeText(EditNoteActivity.this,"请选择分类",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.l1:
                if (LL_contentEdit.getVisibility() == View.VISIBLE){ //如果已经打开了这个布局，则再次点击时关闭
                    LL_contentEdit.setVisibility(View.GONE);
                } else {
                    LL_contentEdit.setVisibility(View.VISIBLE);
//                    et_content.clearFocus();//失去焦点
                    imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //隐藏软键盘
                }
                break;
            case R.id.l2:
//                startActivity(new Intent(EditNoteActivity.this,TakePhotoActivity.class));
                // 直接启动照相机，照相机照片将会存在默认的文件中
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 1);
                takePhoto();
                break;
            case R.id.l3: //点击添加图片
                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intentToPickPic, 2);
                break;
            case R.id.l4: //添加提醒
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                TimeSelector timeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
                    @Override
                    public void handle(String time) {
                        NotealarmTime = time;
                        LL_alarmTip.setVisibility(View.VISIBLE);
                        tv_alarmTimeTip.setText("系统将于 " + NotealarmTime + " 提醒您执行该日程");
                    }
                }, dateFormat.format(new Date()), "2025-1-1 00:00");
                timeSelector.show();
                break;
            case R.id.l5:
                setNoteAtTheTop(noteid,"no"); //设置取消置顶
                l5.setVisibility(View.GONE);
                break;
            case R.id.icon_addTag:
                showAddNewTagDialog();
                break;
            case R.id.iv_textColor1:
                textColor = getResources().getColor(R.color.TextColor1);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.iv_textColor2:
                textColor = getResources().getColor(R.color.TextColor2);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.iv_textColor3:
                textColor = getResources().getColor(R.color.TextColor3);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.iv_textColor4:
                textColor = getResources().getColor(R.color.TextColor4);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.iv_textColor5:
                textColor = getResources().getColor(R.color.TextColor5);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.iv_textColor6:
                textColor = getResources().getColor(R.color.TextColor6);
                setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,0,et_content.getText().toString().length(),et_content.getSelectionStart());
                break;
            case R.id.tv_cancleAlarm: //取消提醒
                LL_alarmTip.setVisibility(View.GONE);
                NotealarmTime = ""; //提醒时间设置为""
                break;
        }
    }

    /**
     * @method  init
     * @date: 2020/1/8 14:07
     * @return void
     * @description 对activity的布局进行初始化
     */
    @SuppressLint({"ObsoleteSdkInt", "ClickableViewAccessibility"})
    private void init(){
        //使得在主线程中也可进行网络请求
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        progressBar = findViewById(R.id.progressBar);
        mFlowLayout = findViewById(R.id.id_flowlayout);
        noteid = getNoteIdFromLastActivity(); //获取上一个Activity传递过来的noteid，如果没有传递过来，则说明是新建的笔记
        tv_editTime = findViewById(R.id.tv_editTime); //编辑的日期
        et_content = findViewById(R.id.et_content); //编辑的笔记
        seekBar = findViewById(R.id.seekBar); //字体调整大小的进度条
        LL_alarmTip = findViewById(R.id.LL_alarmTip);
        LL_contentEdit = findViewById(R.id.LL_contentEdit); //字体调整大小的进度条
        tv_textSize = findViewById(R.id.tv_textSize);
        tv_alarmTimeTip = findViewById(R.id.tv_alarmTimeTip); //提醒时间
        l5 = findViewById(R.id.l5);
        l5.setOnClickListener(this);
        findViewById(R.id.tv_header_back).setOnClickListener(this); //监听返回按钮
        findViewById(R.id.tv_header_right).setOnClickListener(this); //监听保存按钮
        findViewById(R.id.tv_header_right).setOnClickListener(this); //监听保存按钮
        findViewById(R.id.l1).setOnClickListener(this); //监听清单按钮
        findViewById(R.id.l2).setOnClickListener(this); //监听文本编辑按钮
        findViewById(R.id.l3).setOnClickListener(this); //监听图片按钮
        findViewById(R.id.l4).setOnClickListener(this); //监听分享按钮
        findViewById(R.id.icon_addTag).setOnClickListener(this); //监听添加标签的按钮
        findViewById(R.id.iv_textColor1).setOnClickListener(this);
        findViewById(R.id.iv_textColor2).setOnClickListener(this); //监听文本的颜色
        findViewById(R.id.iv_textColor3).setOnClickListener(this);
        findViewById(R.id.iv_textColor4).setOnClickListener(this);
        findViewById(R.id.iv_textColor5).setOnClickListener(this);
        findViewById(R.id.iv_textColor6).setOnClickListener(this);
        findViewById(R.id.tv_cancleAlarm).setOnClickListener(this);  //关闭提醒
        seekBar.setOnSeekBarChangeListener(this);
        progressBar.bringToFront(); //设置加载图标位于布局顶层
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //隐藏软键盘
        et_content.setFilters(new InputFilter[]{new EmojiExcludeFilter()}); //过滤掉表情

        //监听editText的点击
        et_content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (LL_contentEdit.getVisibility() == View.VISIBLE){ //当编辑本文的布局还显示的时候，不能弹出软键盘
                    LL_contentEdit.setVisibility(View.GONE);
                }
                return false;
            }
        });

        TagValues.add("生活");
        TagValues.add("学习");
        TagValues.add("工作");
        getUserNoteAllTags(); //获取当前用户的所有标签
    }

    /**
     * @method  initTag
     * @date: 2020/1/12 17:18
     * @param mVals 所有的标签数据
     * @param currentSelectedTag 当前所选择的标签的名称
     * @return void
     * @description 方法的作用，用于构建流式标签布局
     */
    private void initTag(final List<String> mVals, String currentSelectedTag){
        //初始化标签数据
        final LayoutInflater mInflater = LayoutInflater.from(this);
        //设置流式布局的适配器
        TagAdapter tagAdapter = new TagAdapter<String>(mVals) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.tv, mFlowLayout, false);
                tv.setText(s);
                return tv;
            }

        };
        for (int i = 0; i<mVals.size(); i++){
            if (mVals.get(i).equals(currentSelectedTag)){
                tagAdapter.setSelectedList(i);
            }
        }
        mFlowLayout.setAdapter(tagAdapter);
        /*点击标签时的回调 selectPosSet为标签的下标，回调形式 [0]   最终的type.size()为所选择的标签*/
        mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener()
        {
            @Override
            public void onSelected(Set<Integer> selectPosSet)
            {
                Iterator<Integer> value = selectPosSet.iterator(); //获取该下标对应的值
                type.clear();
                while(value.hasNext())//判断是否有下一个
                {
                    type.add(mVals.get(value.next()));
                }
                if (type.size() != 0){
                    tagName = type.get(0);  //由于设置了单选模式，所以只会有一种结果
                }

            }
        });
    }

    /**
     * @date: 2020/1/7 17:54
     * @description 创建一个消息处理者，用于处理子线程传递过来的消息，实现异步更新UI
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    initTag(TagValues,tagName); //初始化标签
                    progressBar.setVisibility(View.GONE);
                    break;
                case 2:
                    if (!noteid.equals("")){ //如果不是新建的笔记
                        progressBar.setVisibility(View.VISIBLE); //显示加载动画
                        getNoteInfo(noteid); //获取当前笔记的所有内容
                        et_content.setHint(""); //关闭编辑提醒
                    } else { //如果是新建的笔记
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("今天HH:mm");
                        tv_editTime.setText(dateFormat.format(new Date()));
                        initTag(TagValues,tagName);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * @method  onActivityResult
     * @date: 2019/12/28 18:34
     * @param requestCode 请求代码
     * @param resultCode 结果代码
     * @param data 回调的数据
     * @return void
     * @description 本地相册回调方发。当点击某张照片完成时会回调到onActivityResult，在这里处理照片。(将照片上传至服务器，并且显示在edittext中)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case 1: //从相机中拍照
                    Bitmap bm = null;
                    try {
                        bm = getBitmapFormUri(mUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bm, null,null));
                    photoLocalUrl = getRealPathFromUri(EditNoteActivity.this, mUri); //给图片的本地url赋值
                    notePhotoFile = new File(photoLocalUrl);
                    photoIndex = et_content.getSelectionStart(); //获取当前文本光标的位置
                    insertImg2(bm,photoIndex);
                    break;
                case 2:  //从图库中找照片
                    try {
                        final Uri imageUrix = data.getData();       //该uri是上一个Activity返回的
                        photoLocalUrl = getRealPathFromUri(EditNoteActivity.this, imageUrix);
                        if (photoLocalUrl != null) {
                            notePhotoFile = new File(photoLocalUrl);
                            photoIndex = et_content.getSelectionStart(); //获取当前文本光标的位置
                            insertImg(2, photoIndex, photoLocalUrl);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @method  insertImg
     * @date: 2020/1/7 15:32
     * @param sign 标记 判断插入的图片是根据网络路径插入还是本地路径插入 1：网络路径 2：本地路径
     * @param index 图片插入的位置
     * @param path 图片路径
     * @return android.text.Editable
     * @description 插入图片到本文中
     */
    private void insertImg(int sign, int index, String path){
        int width = (int) (getScreenWidth(EditNoteActivity.this) * 0.9);
        String photoText = "<photo>";//占位
        SpannableString ss = new SpannableString(photoText);//创建富文本
        Bitmap bitmap = null;
        if (sign == 1){
            bitmap = decodeUriAsBitmapFromNet(path); //从网络路径获取到一个bitmap
        } else if (sign == 2 ){
            bitmap = BitmapFactory.decodeFile(path); //从本机绝对路径获取到一个bitmap
//            bitmap = ImageUtils.getSmallBitmap(path,width,480);
        }
        assert bitmap != null; //断言bitmap不为空对象
        drawable = new BitmapDrawable(bitmap);
        drawable.setBounds(0,0,width,width * bitmap.getHeight() / bitmap.getWidth());
//        bitmap.setWidth(width);
//        bitmap.setHeight(width * bitmap.getHeight() / bitmap.getWidth());
        ss.setSpan(new ImageSpan(drawable), 0, photoText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //图片放置的位置
        //将富文本插入EditText
        et_content.getText().insert(index, ss);
    }

    private void insertImg2(Bitmap bitmap, int index){
        int width = (int) (getScreenWidth(EditNoteActivity.this) * 0.9);
        String photoText = "<photo>";//占位
        SpannableString ss = new SpannableString(photoText);//创建富文本
        drawable = new BitmapDrawable(bitmap);
        drawable.setBounds(0,0,width,width * bitmap.getHeight() / bitmap.getWidth());
//        bitmap.setWidth(width);
//        bitmap.setHeight(width * bitmap.getHeight() / bitmap.getWidth());
        ss.setSpan(new ImageSpan(drawable), 0, photoText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //图片放置的位置
        //将富文本插入EditText
        et_content.getText().insert(index, ss);
    }

    /**
     * @method  decodeUriAsBitmapFromNet
     * @date: 2020/1/5 23:08
     * @param url 图片的url
     * @return android.graphics.Bitmap
     * @description 通过网络url获取到bitmap
     */
    private Bitmap decodeUriAsBitmapFromNet(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * @method  getNoteIdFromLastActivity
     * @date: 2019/12/26 14:45
     * @return java.lang.String
     * @description 方法的作用:读取上一个Activity中传递过来的noteid，如果上一个Activity不传noteid，则说明用户是新建的笔记。
     */
    private String getNoteIdFromLastActivity(){
        String noteid = "";
        Bundle bundle = this.getIntent().getExtras();  //Bundle取出存在intent的数据
        if (bundle != null) {
            noteid = bundle.getString("noteid");   //根据键名tag读取上一个Acivity传过来的标签名称
        }
        return noteid;
    }

    /**
     * @method  getCurrentDate
     * @date: 2019/12/20 17:28
     * @return java.lang.String
     * @description 获取系统时间的方法，获取到的格式：2019-12-20 17:28:55
     */
    private String getCurrentDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * @method  getNoteInfo
     * @date: 2020/1/7 15:55
     * @param noteid 笔记的noteid
     * @return void
     * @description 根据笔记的noteid获取某笔记的所有内容
     */
    public void getNoteInfo(String noteid){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.getNoteInfo("Bearer " + token,noteid)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<getNoteByNoteidResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            //发生1给主线程，说明数据加载完成
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            Toast.makeText(EditNoteActivity.this,"内容获取失败" + e.getMessage(),Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onNext(getNoteByNoteidResponse data) {
                            tagName = data.getTag();
                            textSize = Integer.parseInt(data.getTextSize()); //获取笔记的文字大小
                            textColor = Integer.parseInt(data.getTextColor());
                            photoLocalUrl = data.getPhotolocalurl(); //获取图片的本地地址
                            String content = data.getContent();
                            String photourl = data.getPhotourl();
                            String photoindex = data.getPhotoindex();
                            photoIndex = Integer.parseInt(photoindex);
                            String noteLastSavetime = data.getSavetime();
                            et_content.setText(content); //设置笔记文本
                            Editable editable = et_content.getText();
                            NotealarmTime = data.getAlarmtime();
                            String atTheTop = data.getAtTheTop();
                            if (atTheTop.equals("yes")){
                                l5.setVisibility(View.VISIBLE);
                            }
                            if (!NotealarmTime.equals("")){
                                LL_alarmTip.setVisibility(View.VISIBLE);
                                tv_alarmTimeTip.setText("系统将于 " + NotealarmTime + " 提醒您执行该日程");
                            }
                            if (!photoindex.equals(String.valueOf(-1))){ //如果图片的下标不为-1，即如果没有插入图片文件
                                editable.delete(Integer.parseInt(photoindex),Integer.parseInt(photoindex) + 7); //<photo>的长度为7
                            }

                            et_content.setTextSize(textSize); //设置笔记文本的字体大小
                            et_content.setText(editable); //设置笔记文本
                            tv_textSize.setText("字号：" + textSize); //设置字号显示的大小
                            seekBar.setProgress(textSize); //设置字体进度条的进度值
                            tv_editTime.setText(noteLastSavetime); //设置笔记最后保存的时间

                            if (photoLocalUrl != null){
                                try {
                                    insertImg(2, Integer.parseInt(photoindex),photoLocalUrl); //插入图片
                                } catch (Exception e){
                                    if (photourl != null && !photoindex.equals(String.valueOf(-1))){
                                        insertImg(1, Integer.parseInt(photoindex),photourl); //插入图片
                                    }
                                }
                            }


                            //发生1给主线程，说明数据加载完成
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    });
        }
    }

    /**
     * @method  saveNote
     * @date: 2019/12/25 18:06
     * @param tag 笔记标签
     * @param content 笔记内容
     * @return void
     * @description 用来将用户编辑的笔记上传至服务器(未插入图片或以存在图片的情况下) --本接口不上传图片文件
     */
    public void saveNoteWithNoPhoto(final String tag, String content,int textSize,int textColor,String photoindex,String alarmTime){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.saveNoteWithNoPhoto("Bearer " + token,tag,content,String.valueOf(textSize),String.valueOf(textColor),alarmTime,noteid,photoindex)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<saveNoteResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("saveNote   ", e.getMessage());
                        }

                        @Override
                        public void onNext(saveNoteResponse data) {
                            //保存成功
                            setTitleFromSharedPreferences(tag);
                            if (!NotealarmTime.equals("")){ //如果提醒时间不为空
                                CalendarReminderUtils.addCalendarEvent(EditNoteActivity.this,et_content.getText().toString(),noteid,NotealarmTime,0); //设置提醒
                            } else { //如果提醒时间为空
                                CalendarReminderUtils.deleteCalendarEvent(EditNoteActivity.this,noteid); //将日历中的日程取消
                            }
                            Toast.makeText(EditNoteActivity.this,"保存成功 " + data.getCode(),Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    /**
     * @method  saveNote
     * @date: 2019/12/25 18:06
     * @param tag 笔记标签
     * @param content 笔记内容
     * @param textSize 文字大小
     * @param textColor 文字颜色
     * @param photoInsertIndex 图片插入的位置
     * @param photolocalurl 图片的本地url
     * @param photoFile 图片文件
     * @return void
     * @description 用来将用户编辑的笔记上传至服务器(含插入的图片)
     */
    public void saveNoteWithPhoto(final String tag, String content,int textSize,int textColor, String photoInsertIndex,String photolocalurl,String alarmTime,File photoFile){
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), photoFile);
        MultipartBody.Part notePhoto = MultipartBody.Part.createFormData("file", photoFile.getName(), photoRequestBody);

        RequestBody ntag = RequestBody.create(MediaType.parse("text/plain"), tag);
        RequestBody ncontent = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody ntextSize = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(textSize));
        RequestBody ntextColor = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(textColor));
        RequestBody nphotoInsertIndex = RequestBody.create(MediaType.parse("text/plain"), photoInsertIndex);
        RequestBody nnoteid = RequestBody.create(MediaType.parse("text/plain"), noteid);
        RequestBody nphotolocalurl = RequestBody.create(MediaType.parse("text/plain"), photolocalurl);
        RequestBody nalarmTime = RequestBody.create(MediaType.parse("text/plain"), alarmTime);

        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.saveNoteWithPhoto("Bearer " + token,ntag,ncontent,ntextSize,ntextColor,nnoteid,nphotoInsertIndex,nphotolocalurl,nalarmTime,notePhoto)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<saveNoteResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("saveNotex   ", e.getMessage());
                        }

                        @Override
                        public void onNext(saveNoteResponse data) {
                            //保存成功
                            setTitleFromSharedPreferences(tag);
                            if (!NotealarmTime.equals("")){ //如果提醒时间不为空
                                CalendarReminderUtils.addCalendarEvent(EditNoteActivity.this,et_content.getText().toString(),noteid,NotealarmTime,0); //设置提醒
                            } else { //如果提醒时间为空
                                CalendarReminderUtils.deleteCalendarEvent(EditNoteActivity.this,noteid); //将日历中的日程取消
                            }
                            Toast.makeText(EditNoteActivity.this,"保存成功 " + data.getCode(),Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    /**
     * @method  onKeyDown
     * @date: 2019/12/26 21:45
     * @return boolean
     * @description 手机返回键的监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showTipDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @method  showTipDialog
     * @date: 2019/12/26 22:24
     * @return void
     * @description 显示未保存时的提示对话框
     */
    private void showTipDialog(){
        if (et_content.getText().toString().equals("")){ //如果没有输入任何文字，则直接退出，不需要显示提示对话框
            finish();
        } else {
            TextView title = new TextView(this);
            title.setText("你的内容尚未保存");
            title.setPadding(25, 25, 25, 25);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(17);
            title.setTextColor(Color.BLACK);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建一个提示对话框的构造者
//            builder.setTitle("你的内容尚未保存");  //设置对话框的标题
            builder.setCustomTitle(title);
            builder.setMessage("是否放弃更改？");  //设置提示信息
//            builder.setIcon(R.drawable.ic_tip);  //设置对话框的图标
            //设置正面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            //设置反面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }

    /**
     * @method  setTitleFromSharedPreferences
     * @date: 2019/12/19 14:38
     * @param titleName 选中标签的名称
     * @return void
     * @description 将选中的标签的名称通过SharedPreferences保存起来
     */
    public void setTitleFromSharedPreferences(String titleName){
        SharedPreferences.Editor editor = getSharedPreferences("TitleText",MODE_PRIVATE).edit();
        editor.putString("title",titleName);
        editor.apply();
    }

    /**
     * @method  getRealPathFromUri
     * @date: 2019/12/30 15:19
     * @param context 上下文
     * @param contentUri 路径
     * @return java.lang.String
     * @description 将原始路径转为绝对路径
     */
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @method  getScreenWidth
     * @date: 2020/1/7 17:54
     * @param context 上下文
     * * @return int
     * @description 获取屏幕的宽度
     */
    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * @method  getUserNoteAllTags
     * @date: 2020/1/11 14:35
     * @return void
     * @description 方法的作用:获取当前用户的所有笔记的所有标签
     */
    private void getUserNoteAllTags(){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.getUserNoteAllTags("Bearer " + token)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<getUserNoteAllTagsResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("getUserNoteAllTags   ", e.getMessage());
                        }

                        @Override
                        public void onNext(getUserNoteAllTagsResponse data) {
                            for (int i = 0; i<data.getResponse().size(); i++) {
                                if (!data.getResponse().get(i).getTag().equals("生活") && !data.getResponse().get(i).getTag().equals("学习")
                                        && !data.getResponse().get(i).getTag().equals("工作")) {
                                    TagValues.add(data.getResponse().get(i).getTag());
                                }
                            }
                            //完成获取标签的数据后，发生2给主线程，说明所有标签数据加载完成
                            Message message = new Message();
                            message.what = 2;
                            handler.sendMessage(message);
                        }
                    });
        }
    }

    /**
     * @method  setNoteAtTheTop
     * @date: 2020/1/20 20:51
     * @param noteid 笔记的noteid
     * @param atTheTop 置顶 yes:表示置顶 no:表示不置顶
     * @return void
     * @description 方法的作用：设置笔记的置顶
     */
    private void setNoteAtTheTop(String noteid, String atTheTop){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.setNoteAtTheTop("Bearer " + token,noteid,atTheTop)
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
                                Toast.makeText(EditNoteActivity.this,"已取消置顶",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * @method  showAddNewTagDialog
     * @date: 2019/12/27 20:57
     * @return void
     * @description 显示添加新标签时的提示框
     */
    private void showAddNewTagDialog(){
        // 获取view
        View view = LayoutInflater.from(EditNoteActivity.this).inflate(R.layout.dialog_addnewtag, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(EditNoteActivity.this);//创建一个提示对话框的构造者
        builder.setTitle("请输入新标签的名称：");  //设置对话框的标题
        builder.setView(view); // 设置显示的view
        builder.setIcon(R.drawable.ic_edit_black);  //设置对话框的图标
        final EditText et_input = view.findViewById(R.id.et_input);
        //设置正面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tag = et_input.getText().toString();
                if (!tag.equals("")){ //如果输入的内容不为空
                    tagName = tag;
                    TagValues.add(tag);
                    initTag(TagValues,tagName);
                } else {
                    Toast.makeText(EditNoteActivity.this,"添加失败，请输入标签名称",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //设置反面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }


    /**
     * @method  onProgressChanged
     * @date: 2020/1/15 17:44
     * @param seekBar seekBar控件
     * @param i 选择到的进度值
     * @param b 一般为true
     * @return void
     * @description 进度发生改变时会触发
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        textSize = i;
        tv_textSize.setText("字号：" + i);
        setContentTextSizeAndColor(et_content.getText().toString(),photoIndex,textSize,textColor,
                0,
                et_content.getText().toString().length(),
                et_content.getSelectionStart());
    }

    /**
     * @method  onStartTrackingTouch
     * @date: 2020/1/15 17:44
     * @param seekBar seekBar控件
     * @return void
     * @description 按住SeekBar时会触发
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * @method  onStopTrackingTouch
     * @date: 2020/1/15 17:44
     * @param seekBar seekBar控件
     * @return void
     * @description 放开SeekBar时触发
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * @method  setContentTextSizeAndColor
     * @date: 2020/1/15 20:10
     * @param content 需要变换字体大小的字体
     * @param photoIndex 图片所在的索引
     * @param textSize 字体大小
     * @param textColor 字体颜色
     * @param start 起始位置
     * @param end 终点位置
     * @return void
     * @description 方法的作用：调整editText字体的大小和颜色
     */
    private void setContentTextSizeAndColor(String content, int photoIndex, int textSize, int textColor, int start, int end, int selection){
        SpannableString sStr = new SpannableString(content);
        //设置字体大小,第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素
        sStr.setSpan(new AbsoluteSizeSpan(textSize,true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sStr.setSpan(new ForegroundColorSpan(textColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (drawable != null && photoIndex != -1){
            sStr.setSpan(new ImageSpan(drawable), photoIndex, photoIndex + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        et_content.setText(sStr);
        et_content.setSelection(selection); //设置光标位置
    }

//    /**
//     * @method  getTextLeftSelection
//     * @date: 2020/1/16 21:20
//     * @param Selection 当前光标所在的位置
//     * @param content 当前的文本内容
//     * @return int
//     * @description 方法的作用：获取光标坐在的行的左坐标
//     */
//    private int getTextLeftSelection(int Selection,String content) {
//        int Left = 0;
//        for (int i = Selection - 1; i >= 0; i--) {
//            if (content.charAt(i) == '\n' || i == 0) {
//                Left = i;
//                break;
//            }
//        }
//        return Left;
//    }
//
//    /**
//     * @method  getTextRightSelection
//     * @date: 2020/1/16 21:20
//     * @param Selection 当前光标所在的位置
//     * @param content 当前的文本内容
//     * @return int
//     * @description 方法的作用：获取光标坐在的行的右坐标
//     */
//    private int getTextRightSelection(int Selection,String content) {
//        int Right = 0;
//        for (int i = Selection; i < content.length(); i++) {
//            if (content.charAt(i) == '\n' || i == content.length() - 1) {
//                Right = i;
//                break;
//            }
//        }
//        return Right;
//    }

    Uri mUri;
    private void takePhoto() {
        // 步骤一：创建存储照片的文件
        String path = getFilesDir() + File.separator + "images" + File.separator;
        File file = new File(path, "test.jpg");
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //步骤二：Android 7.0及以上获取文件 Uri
            mUri = FileProvider.getUriForFile(EditNoteActivity.this, "com.example.admin.custmerviewapplication", file);
        } else {
            //步骤三：获取文件Uri
            mUri = Uri.fromFile(file);
        }
        //步骤四：调取系统拍照
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(intent, 1);
    }

    public Bitmap getBitmapFormUri(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = getContentResolver().openInputStream(uri);

        //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;//不加载到内存
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;

        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    public Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options<=0)
                break;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}


