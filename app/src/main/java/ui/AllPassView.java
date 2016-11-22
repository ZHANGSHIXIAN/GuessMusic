package ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.zhangshixian.guessmusic.R;

import data.Const;
import util.Util;

/**
 * Created by ZHANGSHIXIAN on 2016/11/22.
 */
public class AllPassView extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_pass_view);
        FrameLayout view= (FrameLayout) findViewById(R.id.layout_bar_coin);
        view.setVisibility(View.GONE);
        ImageButton btn_back= (ImageButton) findViewById(R.id.btn_bar_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Util.saveData(AllPassView.this,-1, Const.TOTAL_COINS);
    }
}
