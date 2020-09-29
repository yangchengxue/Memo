package com.yxy.memo.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.yxy.memo.R;
import com.yxy.memo.bean.ContentEntry;
import com.yxy.memo.bean.getUserInfoResponse;
import com.yxy.memo.httpservice.retrofitRequest;
import com.yxy.memo.interfaces.callback_MainActivity_Pop;
import com.yxy.memo.weight.SwipeRecycler;
import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
  *
  * @Package:        com.yxy.memo.adapter
  * @ClassName:      ContentEntryAdapter
  * @CreateDate:     2019/12/19 22:16
  * @Description:    SwipeRecycler的适配器，用于配置主界面显示的备忘录条目的数据
 */
public class ContentEntryAdapter extends RecyclerView.Adapter<ContentEntryAdapter.ViewHolder>{

    private Context context;
    private SwipeRecycler recycler;
    private ArrayList<ContentEntry> contentEntries;
    callback_MainActivity_Pop callback_mainActivity_pop;

    //条目短按接口
    public interface OnItemClickListener {
        void setOnItemClickListener(View view,int position);
    }
    //条目长按接口
    public interface OnLongClickListener {
        void setOnLongClickListener(View view, int position);
    }

    //声明接口
    private OnItemClickListener mOnItemClickListener;
    private OnLongClickListener mOnLongClickListener;

    //创建条目点击的方法，用变量接收一下接口对象
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener=onItemClickListener;
    }
    //创建条目长按的方法，用变量接收一下接口对象
    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
        this.mOnLongClickListener=onLongClickListener;
    }

    public ContentEntryAdapter(Context context, callback_MainActivity_Pop callback_mainActivity_pop, SwipeRecycler recycler, ArrayList<ContentEntry> contentEntries){
        this.context = context;
        this.recycler = recycler;
        this.contentEntries = contentEntries;
        this.callback_mainActivity_pop = callback_mainActivity_pop;
    }

    @NonNull
    @Override
    public ContentEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerviewitem_contententry, recycler, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = recycler.getScreenWidth() + recycler.dp2px(180);
        view.setLayoutParams(layoutParams);

        View main = view.findViewById(R.id.LinearLayout_Item);
        ViewGroup.LayoutParams mainLayoutParams = main.getLayoutParams();
        mainLayoutParams.width = recycler.getScreenWidth();
        main.setLayoutParams(mainLayoutParams);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContentEntryAdapter.ViewHolder holder, final int position) {

        //请注意，为了有良好的体验，请记得每次点击事件生效时，关闭菜单
        //recycler.closeEx();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recycler.closeEx();
            }
        });

        holder.tv_content.setText(contentEntries.get(position).getContent());
        holder.tv_tag.setText(contentEntries.get(position).getTag());
        holder.tv_date.setText(contentEntries.get(position).getDate());
        String notephotoUrl = contentEntries.get(position).getNotePhotoUrl();
        if (notephotoUrl != null){
            holder.sdv_notePhoto.setImageURI(Uri.parse(notephotoUrl));
        }
        if (!contentEntries.get(position).getNoteAlarmTime().equals("")){ //如果对应条目的提醒时间不为空，则将闹钟显示出来
            holder.iv_alarm.setVisibility(View.VISIBLE);
            holder.RL_Item.setBackground(context.getResources().getDrawable(R.drawable.buttonclickstyle_alarmitem)); //改变条目背景色
        }
        if (contentEntries.get(position).getAtTheTop().equals("yes")){ //如果置顶
            holder.RL_Item.setBackground(context.getResources().getDrawable(R.drawable.buttonclickstyle_topitem)); //改变条目背景色
        }

        //点击事件
        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTipAlertDialog(position);
            }
        });

        holder.btnTop.setOnClickListener(new View.OnClickListener() { //置顶按钮
            @Override
            public void onClick(View view) {
                recycler.closeEx();
                //置顶
                setNoteAtTheTop(contentEntries.get(position).getNoteid(),"yes");
                contentEntries.get(position).setAtTheTop("yes"); //让这个下标的元素的置顶设置为yes
                ContentEntry temp = contentEntries.remove(position);
                contentEntries.add(0, temp);
                notifyItemChanged(position, 0);
                notifyItemRangeChanged(0, contentEntries.size());
            }
        });

        //条目短按
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.setOnItemClickListener(holder.tv_content,position);//控件和条目下标
            }
        });
        //条目长按
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOnLongClickListener.setOnLongClickListener(holder.tv_content,position);
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return contentEntries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView btnDel;
        private ImageView btnTop;
        private TextView tv_content;
        private TextView tv_tag;
        private TextView tv_date;
        private SimpleDraweeView sdv_notePhoto;
        private ImageView iv_alarm;
        private RelativeLayout RL_Item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnDel = itemView.findViewById(R.id.btn_del);
            btnTop = itemView.findViewById(R.id.btn_top);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_tag = itemView.findViewById(R.id.tv_tag);
            tv_date = itemView.findViewById(R.id.tv_date);
            sdv_notePhoto = itemView.findViewById(R.id.sdv_notePhoto);
            iv_alarm = itemView.findViewById(R.id.iv_alarm);
            RL_Item = itemView.findViewById(R.id.RL_Item);
        }
    }

    public ArrayList<ContentEntry> getListData(){
        return contentEntries;
    }



    /**
     * @method  deleteAnote
     * @date: 2019/12/26 15:41
     * @param index 数据的索引，代表要删除的是哪一条数据
     * @return void
     * @description 方法的作用：用于删除某条笔记。
     */
    private void deleteAnote(int index){
        SharedPreferences pref = context.getSharedPreferences("Token",MODE_PRIVATE);
        String token = pref.getString("token","");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://182.92.208.230:8080/demo-0.0.1-SNAPSHOT/")
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .build();
        retrofitRequest request = retrofit.create(retrofitRequest.class);
        if (!token.equals("")) {
            request.deleteNote("Bearer " + token,contentEntries.get(index).getNoteid())
                    .subscribeOn(Schedulers.io())//IO线程加载数据
                    .observeOn(AndroidSchedulers.mainThread())//主线程显示数据
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("deletenoteerror   ", e.getMessage());
                        }

                        @Override
                        public void onNext(String data) {
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * @method  showTipAlertDialog
     * @date: 2019/12/26 22:11
     * @param position 传入将要删除的数据的索引
     * @return void
     * @description 显示提示删除对话框
     */
    private void showTipAlertDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);//创建一个提示对话框的构造者
        builder.setTitle("      移除这条记录     ");  //设置对话框的标题
        builder.setMessage("是否永久移除这条记录？");  //设置提示信息
        builder.setIcon(R.drawable.ic_delete_tip);  //设置对话框的图标

        //设置正面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recycler.closeEx();
                //删除
                String content = contentEntries.get(position).getContent();
                deleteAnote(position);
                contentEntries.remove(position);
                callback_mainActivity_pop.getNotesSum(contentEntries.size()); //返回当前笔记的总数给MainActivity
                notifyItemRemoved(position);
                notifyItemRangeChanged(0, contentEntries.size());
            }
        });

        //设置反面的按钮，输入new DialogInterface.OnClickListener()对自动弹出方法。
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recycler.closeEx();
            }
        });

        builder.show();
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
        SharedPreferences pref = context.getSharedPreferences("Token",MODE_PRIVATE);
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
                            Log.d("getuserallnoteserror   ", e.getMessage());
                        }

                        @Override
                        public void onNext(String data) {

                        }
                    });
        }
    }
}
