package com.yxy.memo.weight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.yxy.memo.R;
import com.yxy.memo.adapter.SpinnerPopAdapter;
import com.yxy.memo.interfaces.callback_MainActivity_Pop;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
  *
  * @Package:        com.yxy.memo.weight
  * @ClassName:      SpinnerPopuwindow
  * @CreateDate:     2019/12/18 21:40
  * @Description:    具有下拉列表功能的Popuwindow，列表采用ListView组件
 */
public class SpinnerPopuwindow extends PopupWindow {

    private View conentView;
    private ListView listView;
    private SpinnerPopAdapter adapter;
    private Activity context;
    private TextView pop_title;
    private TextView pop_addNewClass; //添加新标签

    /**
     * @method  SpinnerPopuwindow
     * @date: 2019/12/18 21:15
     * @param context 上下文
     * @param flag 标记位，用于判定是否构造出底部的“新增标签”按钮 0：不构造底部按钮 1：构造底部按钮
     * @param string 获取到未打开列表时显示的值
     * @param list 需要显示的列表的集合
     * @param itemsOnClick listview在activity中的点击监听事件
     * @param width SpinnerPopuwindow显示的宽度
     * @param height SpinnerPopuwindow显示的高度
     * @param colorValue SpinnerPopuwindow显示的背景颜色 0x40F44336代表：0x：16进制 40：不透明度为20% F44336：颜色值
     * @return null
     * @description 类的构造方法
     */
    @SuppressLint({"InflateParams", "WrongConstant"})
    public SpinnerPopuwindow(final Activity context, int flag, int width, int height, int colorValue, final String string, final List<String> list, AdapterView.OnItemClickListener itemsOnClick, final callback_MainActivity_Pop callback) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context =context;
        conentView = inflater.inflate(R.layout.popuwindow_spinner, null);
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(width); //传入ViewGroup.LayoutParams.MATCH_PARENT为布局的最大宽
        //    this.setWidth(view.getWidth());
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(height);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 刷新状态
        this.update();
        this.setOutsideTouchable(false);
        // 实例化一个ColorDrawable，并设置颜色，0x：16进制 40：不透明度为20% F44336：颜色值
        ColorDrawable dw = new ColorDrawable(colorValue);//0x40F44336
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
            }
        });
        //解决软键盘挡住弹窗问题
        this.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        //this.setAnimationStyle(R.style.AnimationPreview);
        adapter = new SpinnerPopAdapter(context,list,string); //构造SpinnerPopAdapter
        listView = conentView.findViewById(R.id.listView);
        listView.setOnItemClickListener(itemsOnClick);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setTitleFromSharedPreferences(list.get(i));
                callback.getTitleText(list.get(i));
                dismissPopupWindow();
            }
        });
        pop_title =  conentView.findViewById(R.id.pop_title);
        pop_addNewClass = conentView.findViewById(R.id.pop_addNewClass);
        if (flag == 0){
            pop_addNewClass.setVisibility(View.GONE);
        } else if (flag == 1){
            pop_addNewClass.setVisibility(View.VISIBLE);
            //新增标签按钮的点击事件
            pop_addNewClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddNewTagDialog();
                }
            });
        }

    }

    /**
     * @method  setTitleText
     * @date: 2019/12/18 21:45
     * @param str 标题名称
     * @description 给下拉列表的设置标题
     */
    public void setTitleText(String str){
        pop_title.setText(str);
    }

    /**
     * @method  getText
     * @date: 2019/12/18 21:39
     * @return int
     * @description 获取选中列表中的数据所对应的position
     */
    public int getText(){
        return listView.getCheckedItemPosition();
    }

     /**
      * @method  showPopupWindow
      * @date: 2019/12/18 21:19
      * @param parent View对象（包括LinearLayout，RelativeLayout等等）
      * @description 显示PopupWindow
      */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent);
            //this.showAsDropDown(parent,0,10);
            this.showAtLocation(parent, Gravity.BOTTOM|Gravity.CENTER, 0, 0);
            //darkenBackground(0.9f);//弹出时让页面背景回复给原来的颜色降低透明度，让背景看起来变成灰色
        }
    }

    /**
     * @method  dismissPopupWindow
     * @date: 2019/12/18 21:23
     * @description 关闭PopupWindow
     */
    public void dismissPopupWindow() {
        this.dismiss();
        darkenBackground(1f);//关闭时让页面背景回复为原来的颜色
    }

    /**
     * @method  darkenBackground
     * @date: 2019/12/18 21:27
     * @param bgcolor 浮点型的颜色值
     * @description 改变背景颜色，主要是在PopupWindow弹出时背景变化，通过透明度设置
     */
    private void darkenBackground(Float bgcolor){
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgcolor;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    /**
     * @method  setTitleFromSharedPreferences
     * @date: 2019/12/19 14:38
     * @param titleName 选中标签的名称
     * @return void
     * @description 将选中的标签的名称通过SharedPreferences保存起来
     */
    private void setTitleFromSharedPreferences(String titleName){
        SharedPreferences.Editor editor = context.getSharedPreferences("TitleText",MODE_PRIVATE).edit();
        editor.putString("title",titleName);
        editor.apply();
    }

    /**
     * @method  showAddNewTagDialog
     * @date: 2019/12/27 20:57
     * @return void
     * @description 显示添加新标签时的提示框
     */
    private void showAddNewTagDialog(){
        // 获取view
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_addnewtag, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);//创建一个提示对话框的构造者
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
                    adapter.addNewData(tag); //添加数据
                    adapter.notifyDataSetChanged(); //刷新适配器
                } else {
                    Toast.makeText(context,"添加失败，请输入标签名称",Toast.LENGTH_SHORT).show();
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

}
