package myui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.example.zhangshixian.guessmusic.R;

import java.util.ArrayList;

import model.IWordButtonClickListener;
import model.WordButton;
import util.Util;

/**
 * Created by ZHANGSHIXIAN on 2016/11/16.
 */
public class MyGridView extends GridView {
    public final static int COUNTS_WORDS=24;

    private ArrayList<WordButton> mArrayList=new ArrayList<WordButton>();
    private MyGridAdapter myAdapter;
    private Context mContext;

    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonListener;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        myAdapter=new MyGridAdapter();
        this.setAdapter(myAdapter);
        mContext=context;


    }

    public void updateData(ArrayList<WordButton> list){
        mArrayList=list;
        //重新设置数据源
        setAdapter(myAdapter);
    }



    class MyGridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final WordButton holder;
            if (convertView==null) {
                convertView = Util.getView(mContext, R.layout.self_ui_gridview_item);
                holder = mArrayList.get(position);
                //加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale);
                //设置动画延迟时间
                mScaleAnimation.setStartOffset(position * 100);
                holder.mindex = position;
                if (holder.mViewButton==null){
                    holder.mViewButton = (Button) convertView.findViewById(R.id.item_btn);
                    holder.mViewButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWordButtonListener.onWordButtonClick(holder);
                    }
                });
            }
                convertView.setTag(holder);
            }else {
                holder=(WordButton) convertView.getTag();

            }
            holder.mViewButton.setText(holder.mWordString);
            //播放动画
            convertView.startAnimation(mScaleAnimation);
            return convertView;
        }
    }
    //注册监听接口
    public void registOnWordButtonClick(IWordButtonClickListener listener){
        mWordButtonListener=listener;
    }
}
