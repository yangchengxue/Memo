package com.yxy.memo.config;

import android.app.Application;
import android.content.Context;

/**
  *
  * @Package:        com.yxy.memo.config
  * @ClassName:      MyApplication
  * @CreateDate:     2020/1/8 13:58
  * @Description:    Application，程序最先执行的类
 */
public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}

