package com.yxy.memo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yxy.memo.R;
import com.yxy.memo.weight.CheckableLayout;

import java.util.List;

/**
  *
  * @Package:        com.yxy.memo.adapter
  * @ClassName:      SpinnerPopAdapter
  * @CreateDate:     2019/12/18 21:54
  * @Description:    SpinnerPopuwindow的布局中的ListView的适配器
 */
public class SpinnerPopAdapter extends BaseAdapter {
    private List<String> content;
    private Context context;
    private LayoutInflater mInflater;
    private String whichItemisCheck;

    public SpinnerPopAdapter(Context context,List<String> content,String whichItemisCheck){
        this.context = context;
        this.content = content;
        this.whichItemisCheck = whichItemisCheck;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return content == null ? 0 : content.size();
    }

    @Override
    public Object getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.listviewitem_popuwindow_spinner,null);
            viewHolder = new ViewHolder();
            viewHolder.tv_spinner = convertView.findViewById(R.id.tv_spinner);
            viewHolder.check = convertView.findViewById(R.id.checkableLayout);
            viewHolder.iv_isCheck = convertView.findViewById(R.id.iv_isCheck);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String spinnerText = content.get(position);
        viewHolder.tv_spinner.setText(spinnerText);
        if (whichItemisCheck.equals(spinnerText)){
            viewHolder.iv_isCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iv_isCheck.setVisibility(View.GONE);
        }
        return convertView;
    }
    public static class ViewHolder{
        TextView tv_spinner;
        public CheckableLayout check;
        public ImageView iv_isCheck;
    }

    public void addNewData(String tag){
        content.add(tag);
    }
}
