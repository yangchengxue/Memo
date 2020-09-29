package com.yxy.memo.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import com.yxy.memo.config.MyApplication;

/**
  *
  * @Package:        com.yxy.memo.utils
  * @ClassName:      EmojiExcludeFilter
  * @CreateDate:     2020/1/10 14:41
  * @Description:    用于EditText过滤表情的类
 */
public class EmojiExcludeFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            int type = Character.getType(source.charAt(i));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                Toast.makeText(MyApplication.getContext(),"抱歉，暂不支持输入表情~_~",Toast.LENGTH_SHORT).show();
                return "";
            }
        }
        return null;
    }
}
