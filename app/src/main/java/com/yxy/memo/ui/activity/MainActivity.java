package com.yxy.memo.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yxy.memo.R;
import com.yxy.memo.adapter.ContentEntryAdapter;
import com.yxy.memo.bean.ContentEntry;
import com.yxy.memo.bean.LoginResponse;
import com.yxy.memo.bean.getNotesResponse;
import com.yxy.memo.bean.getUserInfoResponse;
import com.yxy.memo.bean.getUserNoteAllTagsResponse;
import com.yxy.memo.httpservice.retrofitRequest;
import com.yxy.memo.interfaces.callback_MainActivity_Pop;
import com.yxy.memo.utils.CalendarReminderUtils;
import com.yxy.memo.weight.SpinnerPopuwindow;
import com.yxy.memo.weight.SwipeRecycler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
  *
  * @Package:        com.yxy.memo.ui.activity
  * @ClassName:      MainActivity
  * @CreateDate:     2019/12/18 20:55
  * @Description:    APP的主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, callback_MainActivity_Pop {

    private List<String> testData;
    private AppBarLayout appBarLayout;
    private TextView tv_title;
    private SimpleDraweeView image_userPhoto;  //用户头像
    private SwipeRecycler recyclerview;  //笔记列表
    private ContentEntryAdapter contentEntryAdapter;
    private DrawerLayout drawer_layout;
    private TextView tv_userName;
    private TextView tv_loginStatus; //登录状态
    private TextView tv_lastLoginTime; //最后登录的时间
    private RelativeLayout RL_noContent; //没有笔记的时候显示的图文
    private String curreentTag; //用户保存当前选中的标签是什么

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this); //初始化Fresco图片加载库
        setContentView(R.layout.activity_main);
        requestPermissions();//申请权限
        init();//初始化操作
    }

    /**
     * @method  init
     * @date: 2019/12/25 22:31
     * @return void
     * @description 初始化操作
     */
    private void init(){
        appBarLayout = findViewById(R.id.appBarLayout);
        tv_title = findViewById(R.id.tv_title);
        recyclerview = findViewById(R.id.recyclerview);
        image_userPhoto = findViewById(R.id.image_userPhoto);
        LinearLayout linearLayout_Title = findViewById(R.id.LinearLayout_Title);
        RL_noContent = findViewById(R.id.RL_noContent);
        linearLayout_Title.setOnClickListener(this);
        image_userPhoto.setOnClickListener(this);
        findViewById(R.id.LL1).setOnClickListener(this);
        findViewById(R.id.LL2).setOnClickListener(this);
        findViewById(R.id.LL3).setOnClickListener(this);
        findViewById(R.id.LL4).setOnClickListener(this);
        tv_userName = findViewById(R.id.tv_userName);
        tv_loginStatus = findViewById(R.id.tv_loginStatus);
        tv_lastLoginTime = findViewById(R.id.tv_lastLoginTime);
        drawer_layout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        //下拉刷新控件
        SmartRefreshLayout refreshLayout = findViewById(R.id.refreshLayout);

        //mDrawerLayout与mToolbar关联起来
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name);
        //初始化状态
        actionBarDrawerToggle.syncState();

        //点击编辑新的笔记
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_loginStatus.getText().equals("用户管理")){ //表示处于已登录
                    startActivity(new Intent(MainActivity.this,EditNoteActivity.class));
                } else { //如果处于未登录状态
                    Toast.makeText(MainActivity.this,"亲，登录后将为您开启该服务^_^",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }

            }
        });

        //下拉刷新
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(1000);//传入false表示刷新失败
                Toast.makeText(MainActivity.this,"刷新成功",Toast.LENGTH_SHORT).show();
                if (isNetworkConnected(MainActivity.this)){ //如果有网络连接
                    if (getTokenInSharedPreferences().equals("")){ //token为空说明未登录
                        tv_userName.setText("未登录");
                        tv_loginStatus.setText("登录/注册");
                        image_userPhoto.setImageURI("");
                        initSwipeRecycler(new ArrayList<ContentEntry>());//初始化笔记列表，设置列表为null
                        RL_noContent.setVisibility(View.VISIBLE);
                    } else { //不为空则处于登录状态
                        curreentTag = getTitleFromSharedPreferences();
                        if (curreentTag.equals("全部")){
                            tv_title.setText("全部");
                            getCurrentUserAllNotes(); //查询当前用户所有的笔记数据
                        } else {
                            tv_title.setText(curreentTag);
                            getCurrentUserNotesByTag(curreentTag); //查询当前用户某标签的笔记数据
                        }
                        initTagData();//初始化tag数据
                        getCurrentUserInfo(); //获取当前用户的信息
                    }
                } else {
                    Toast.makeText(MainActivity.this,"网络不可用，无法启用服务",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //上拉加载更多
//        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//                refreshLayout.finishLoadMore(1000);//传入false表示加载失败
////                Toast.makeText(MainActivity.this,"没有更多数据了",Toast.LENGTH_SHORT).show();
//            }
//        });

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

    }

    /**
     * @method  initSwipeRecycler
     * @date: 2019/12/25 22:36
     * @param listData 列表的数据
     * @return void
     * @description 初始化SwipeRecycler，包括设置数据、设置适配器、设置条目点击事件
     */
    private void initSwipeRecycler(ArrayList<ContentEntry> listData){
        contentEntryAdapter = new ContentEntryAdapter(this,this,recyclerview,listData);
//        recyclerview.setNestedScrollingEnabled(false);  //recyclerView设置禁止嵌套滑动
        recyclerview.setAdapter(contentEntryAdapter);
        //条目短按
        contentEntryAdapter.setOnItemClickListener(new ContentEntryAdapter.OnItemClickListener() {
            @Override
            public void setOnItemClickListener(View view, int position) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("tag", contentEntryAdapter.getListData().get(position).getTag());  //写入标签
                bundle.putString("content", contentEntryAdapter.getListData().get(position).getContent());  //写入笔记内容
                bundle.putString("noteid", contentEntryAdapter.getListData().get(position).getNoteid());  //写入笔记的id，用于判断是否是已经存在的笔记
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //条目长按
        contentEntryAdapter.setOnLongClickListener(new ContentEntryAdapter.OnLongClickListener() {
            @Override
            public void setOnLongClickListener(View view, int position) {

            }
        });
    }

    /**
     * @method  onResume
     * @date: 2019/12/25 17:28
     * @return void
     * @description activity每次回到栈顶都刷新一遍数据
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkConnected(this)){ //如果有网络连接
            if (getTokenInSharedPreferences().equals("")){ //token为空说明未登录
                tv_userName.setText("未登录");
                tv_loginStatus.setText("登录/注册");
                image_userPhoto.setImageURI("");
                initSwipeRecycler(new ArrayList<ContentEntry>());//初始化笔记列表，设置列表为null
                RL_noContent.setVisibility(View.VISIBLE);
            } else { //不为空则处于登录状态
                curreentTag = getTitleFromSharedPreferences();
                if (curreentTag.equals("全部")){
                    tv_title.setText("全部");
                    getCurrentUserAllNotes(); //查询当前用户所有的笔记数据
                } else {
                    tv_title.setText(curreentTag);
                    getCurrentUserNotesByTag(curreentTag); //查询当前用户某标签的笔记数据
                }
                initTagData();//初始化tag数据
                getCurrentUserInfo(); //获取当前用户的信息
            }
        } else {
            Toast.makeText(this,"网络不可用，无法启用服务",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @date: 2019/12/20 17:07
     * @description 为SpinnerPopuwindow弹出窗口的item实现监听（这里没有用到点击处理，因为在SpinnerPopuwindow类内部通过回调处理过了。。）
     */
    private AdapterView.OnItemClickListener itemsOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            String value = testData.get(mSpinnerPopuwindow.getText());
//            //tv_type.setText(value);
//            mSpinnerPopuwindow.dismissPopupWindow();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {  //右侧菜单按钮的点击监听

            return true;
        } else if (id == android.R.id.home){ //左侧导航按钮的点击监听
//            startActivity(new Intent(MainActivity.this,RegisterUserActivity.class));
            drawer_layout.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.LinearLayout_Title:
                SpinnerPopuwindow mSpinnerPopuwindow = new SpinnerPopuwindow(
                        MainActivity.this,0, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0xFF574545, curreentTag, testData, itemsOnClick, MainActivity.this);
                mSpinnerPopuwindow.showPopupWindow(appBarLayout);
                mSpinnerPopuwindow.setTitleText("选择标签分类");//给下拉列表设置标题
                break;
            case R.id.LL1:
                if (tv_loginStatus.getText().equals("用户管理")){ //如果处于登录状态
                    startActivity(new Intent(MainActivity.this,UserInfoActivity.class));
                } else { //否则跳转到登录界面
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }
                break;
            case R.id.LL2:

                break;
            case R.id.LL3:
                startActivity(new Intent(MainActivity.this,FeedbackActivity.class));
                break;
            case R.id.LL4:
                break;
            case R.id.image_userPhoto:
                if (tv_loginStatus.getText().equals("用户管理")){ //打开本地图片
                    Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                    // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
                    intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intentToPickPic, 1);
                } else { //否则跳转到登录界面
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }

                break;
        }
    }

    /**
     * @method  GetTitleSharedPreferences
     * @date: 2019/12/19 14:35
     * @return java.lang.String
     * @description 返回上一次点击的标签名称
     */
    public String getTitleFromSharedPreferences(){
        SharedPreferences pref = getSharedPreferences("TitleText",MODE_PRIVATE);
        return pref.getString("title","");  //后面的参数为如果找不到对应值，就返回什么样的默认值
    }

    /**
     * @method  getTitleText
     * @date: 2019/12/19 15:38
     * @param result 从SpinnerPopuwindow中回调的标签值
     * @return void
     * @description 回调方法，当SpinnerPopuwindow中的ListView的item被点击之后，将item对应的标签值回调
     */
    @Override
    public void getTitleText(String result) {
        if (result.equals("全部")){
            getCurrentUserAllNotes(); //查询所有笔记内容
        } else {
            getCurrentUserNotesByTag(result); //查询某标签下的所有笔记内容
        }
        curreentTag = getTitleFromSharedPreferences(); //获取上次点击的标签的名称
        if (!curreentTag.equals("")){
            tv_title.setText(curreentTag);
        } else {
            tv_title.setText("全部");
        }
    }

    /**
     * @method  getNotesSum
     * @date: 2019/12/26 16:36
     * @param result 从ContentEntryAdapter回调过来的值，为0则说明数据为空
     * @return void
     * @description 回调方法，将笔记总数从ContentEntryAdapter传给MainActivity。当总数为0 的时候，查询所有笔记
     */
    @Override
    public void getNotesSum(int result) {
        initTagData();//初始化tag数据
        if (result == 0){
            tv_title.setText("全部");
            getCurrentUserAllNotes(); //查询所有笔记内容
        }
    }

    /**
     * @method  getCurrentUserInfo
     * @date: 2019/12/26 16:58
     * @return void
     * @description 获取当前用户的信息，并且如果获取成功，则第一个按钮显示为“用户管理”
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
                            Toast.makeText(MainActivity.this,"Token凭证过期，请重新登录",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(getUserInfoResponse data) {
                            String uicoPath = data.getUicon();
                            if (uicoPath != null){ //如果用户有头像
                                image_userPhoto.setImageURI(Uri.parse("http://182.92.208.230:8080/image/" + uicoPath.substring(16)));
                            }
                            tv_userName.setText(data.getUname());
                            tv_lastLoginTime.setVisibility(View.VISIBLE);
                            tv_lastLoginTime.setText("最后登录时间：" + data.getLastlogintime());
                            tv_loginStatus.setText("用户管理");
                        }
                    });
        }
    }

    /**
     * @method  getCurrentUserAllNotes
     * @date: 2019/12/26 15:53
     * @return void
     * @description 方法的作用:获取当前用户的所有笔记内容，并显示到RecyclerView中。
     */
    private void getCurrentUserAllNotes(){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.getUserAllNotes("Bearer " + token)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<getNotesResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("getuserallnoteserror   ", e.getMessage());
                        }

                        @Override
                        public void onNext(getNotesResponse data) {
                            ArrayList<ContentEntry> ContentEntrylist = new ArrayList<>();
                            if (data.getResponse().size() != 0){ //如果笔记的总数不等于0
                                RL_noContent.setVisibility(View.GONE); //隐藏内容为空时候的图标
                                for (int i = 0;i<data.getResponse().size();i++){
                                    if (!data.getResponse().get(i).getPhotoindex().equals(String.valueOf(-1))){ //如果有图片
                                        ContentEntrylist.add(new ContentEntry(removePhotoText(data.getResponse().get(i).getContent(),
                                                Integer.parseInt(data.getResponse().get(i).getPhotoindex())),
                                                data.getResponse().get(i).getTag(),
                                                data.getResponse().get(i).getSavetime(),
                                                data.getResponse().get(i).getNoteid(),
                                                data.getResponse().get(i).getPhotourl(),
                                                data.getResponse().get(i).getAlarmtime(),
                                                data.getResponse().get(i).getAtTheTop()));
                                        if (data.getResponse().get(i).getAtTheTop().equals("yes")){ //如果有处于置顶的条目，将其条目的下标添加到list中
                                            ContentEntrylist.add(0, ContentEntrylist.remove(i)); //置顶
                                        }
                                    }else { //如果没有图片
                                        ContentEntrylist.add(new ContentEntry(data.getResponse().get(i).getContent(),
                                                data.getResponse().get(i).getTag(),
                                                data.getResponse().get(i).getSavetime(),
                                                data.getResponse().get(i).getNoteid(), "",
                                                data.getResponse().get(i).getAlarmtime(),
                                                data.getResponse().get(i).getAtTheTop()));
                                        if (data.getResponse().get(i).getAtTheTop().equals("yes")){ //如果有处于置顶的条目，将其条目的下标添加到list中
                                            ContentEntrylist.add(0, ContentEntrylist.remove(i)); //置顶
                                        }
                                    }
                                }
                                initSwipeRecycler(ContentEntrylist);//初始化笔记列表
                            } else { //如果笔记总数等于0
                                initSwipeRecycler(ContentEntrylist);//初始化笔记列表
                                RL_noContent.setVisibility(View.VISIBLE); //显示内容为空时候的图标
                            }
                            ContentEntrylist = null; //防止发生内存泄漏

                        }
                    });
        }
    }

    /**
     * @method  getCurrentUserNotesByTag
     * @date: 2019/12/27 14:58
     * @return void
     * @description 方法的作用:获取当前用户某标签的所有笔记，并显示到RecyclerView中。
     */
    private void getCurrentUserNotesByTag(String tag){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.getUserNotesByTag("Bearer " + token,tag)
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<getNotesResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("getuserallnoteserror   ", e.getMessage());
                        }

                        @Override
                        public void onNext(getNotesResponse data) {
                            ArrayList<ContentEntry> ContentEntrylist = new ArrayList<>();
                            if (data.getResponse().size() != 0){ //如果笔记的总数不等于0
                                RL_noContent.setVisibility(View.GONE); //隐藏内容为空时候的图标
                                for (int i = 0;i<data.getResponse().size();i++){
                                    if (!data.getResponse().get(i).getPhotoindex().equals(String.valueOf(-1))){ //如果有图片
                                        ContentEntrylist.add(new ContentEntry(removePhotoText(data.getResponse().get(i).getContent(),
                                                Integer.parseInt(data.getResponse().get(i).getPhotoindex())),
                                                data.getResponse().get(i).getTag(),
                                                data.getResponse().get(i).getSavetime(),
                                                data.getResponse().get(i).getNoteid(),
                                                data.getResponse().get(i).getPhotourl(),
                                                data.getResponse().get(i).getAlarmtime(),
                                                data.getResponse().get(i).getAtTheTop()));
                                        if (data.getResponse().get(i).getAtTheTop().equals("yes")){ //如果有处于置顶的条目，将其条目的下标添加到list中
                                            ContentEntrylist.add(0, ContentEntrylist.remove(i)); //置顶
                                        }
                                    }else { //如果没有图片
                                        ContentEntrylist.add(new ContentEntry(data.getResponse().get(i).getContent(),
                                                data.getResponse().get(i).getTag(),
                                                data.getResponse().get(i).getSavetime(),
                                                data.getResponse().get(i).getNoteid(),"",
                                                data.getResponse().get(i).getAlarmtime(),
                                                data.getResponse().get(i).getAtTheTop()));
                                        if (data.getResponse().get(i).getAtTheTop().equals("yes")){ //如果有处于置顶的条目，将其条目的下标添加到list中
                                            ContentEntrylist.add(0, ContentEntrylist.remove(i)); //置顶
                                        }
                                    }
                                }
                                initSwipeRecycler(ContentEntrylist);//初始化笔记列表
                            } else { //如果笔记总数等于0
                                initSwipeRecycler(ContentEntrylist);//初始化笔记列表
                                RL_noContent.setVisibility(View.VISIBLE); //显示内容为空时候的图标
                            }
                            ContentEntrylist = null; //防止发生内存泄漏

                        }
                    });
        }
    }

    public String removePhotoText(String s, int photoindex) {
        if (photoindex == -1){
            return s;
        } else {
            return s.substring(0, photoindex) + "\n" + s.substring(photoindex + 7);
        }
    }

    /**
     * @method  getTokenInSharedPreferences
     * @date: 2020/1/15 14:27
     * @return java.lang.String
     * @description 方法的作用:从SharedPreferences获取token
     */
    public String getTokenInSharedPreferences(){
        SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
        return pref.getString("token","");
    }

    /**
     * @method onKeyDown
     * @date: 2019/12/26 18:09
     * @param [keyCode, event]
     * @return boolean
     * @description 手机返回键的监听 连续按两次才能成功退出程序
     */
    private long mExitTime; //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            drawer_layout.closeDrawers(); //关闭侧滑菜单
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * @method  onActivityResult
     * @date: 2019/12/28 18:34
     * @param requestCode 请求代码
     * @param resultCode 结果代码
     * @param data 回调的数据
     * @return void
     * @description 本地相册回调方发。当点击某张照片完成时会回调到onActivityResult，在这里处理照片。
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case 1: {
                    // 获取图片
                    try {
                        //该uri是上一个Activity返回的
//                        String imageUri = String.valueOf(data.getData());
                        final Uri imageUrix = data.getData();
                        String imageUri = getRealPathFromUri(MainActivity.this,imageUrix);
                        Log.d("filefileimageUri", " " +  imageUri); //  content://media/external/images/media/1851285
                        if (imageUri != null) {
                            File file = new File(imageUri);
//                            Log.d("filefile", " " +  file.getPath()); //content:/media/external/images/media/1851285
                            RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), file); //application/octet-stream
                            MultipartBody.Part uesrIcon = MultipartBody.Part.createFormData("file", file.getName(), photoRequestBody);
                            SharedPreferences pref = getSharedPreferences("Token",MODE_PRIVATE);
                            String token = pref.getString("token","");
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                                    .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                                    .build();
                            retrofitRequest request = retrofit.create(retrofitRequest.class);
                            if (!token.equals("")) {
                                request.uploadUserIcon("Bearer " + token, uesrIcon)
                                        .subscribeOn(Schedulers.io())//IO线程加载数据
                                        .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                                        .subscribe(new Subscriber<String>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.d("uploadUserIcon   ", e.getMessage());
                                            }

                                            @Override
                                            public void onNext(String Data) {
                                                image_userPhoto.setImageURI(imageUrix);
                                                Toast.makeText(MainActivity.this,"头像更换成功",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @method  initTagData
     * @date: 2020/1/11 14:43
     * @return void
     * @description 方法的作用：初始化笔记的标签数据
     */
    private void initTagData(){
        //默认的标签数据
        testData = new ArrayList<>();
        String str1 = "全部";
        testData.add(str1);
        String str2 = "生活";
        testData.add(str2);
        String str3 = "学习";
        testData.add(str3);
        String str4 = "工作";
        testData.add(str4);
        getUserNoteAllTags(); //获取当前用户的所有标签
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
                            for (int i = 0; i<data.getResponse().size(); i++){
                                if (!data.getResponse().get(i).getTag().equals("全部") && !data.getResponse().get(i).getTag().equals("生活") &&
                                        !data.getResponse().get(i).getTag().equals("学习") && !data.getResponse().get(i).getTag().equals("工作") ){
                                    testData.add(data.getResponse().get(i).getTag());
                                }
                            }
                        }
                    });
        }
    }

    /**
     * @method  requestPermissions
     * @date: 2020/1/19 14:50
     * @return void
     * @description 方法的作用:动态申请手机权限
     */
    private void requestPermissions(){
        List<String> permissionList = new ArrayList<>();
        //读取内存卡权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        //写入内存卡权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //读取内存卡权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_CALENDAR);
        }
        //写入内存卡权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_CALENDAR);
        }

        //相机权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (!permissionList.isEmpty()){  //申请的集合不为空时，表示有需要申请的权限
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1);
        }else { //所有的权限都已经授权过了

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1://获取多个权限
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){ //如果权限被拒绝
                            String s = permissions[i];
                            Toast.makeText(this,s+"权限被拒绝",Toast.LENGTH_SHORT).show();
                            System.exit(0); //退出应用
                        }else{ //授权成功了
                            //do Something
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * @method
     * @date: 2020/1/20 22:29
     * @description 方法的作用：判断是否有网络连接
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
