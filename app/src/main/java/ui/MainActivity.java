package ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zhangshixian.guessmusic.R;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import data.Const;
import model.IAlertDialogButtonListener;
import model.IWordButtonClickListener;
import model.Song;
import model.WordButton;
import myui.MyGridView;
import util.MyLog;
import util.MyPlayer;
import util.Util;

public class MainActivity extends AppCompatActivity implements IWordButtonClickListener{

    public final static String TAG="MainActivity";
    /** 答案正确*/
    public final static int STATUS_ANSWER_RIGHT=1;
    /** 答案错误*/
    public final static int STATUS_ANSWER_WRONG=2;
    /** 答案不完整*/
    public final static int STATUS_ANSWER_LACK=3;
    //闪烁次数
    public final static int SPARSH_TIMES=6;

    public final static int ID_DIALOG_DELETE_WORD=1;

    public final static int ID_DIALOG_TIP_ANSWER=2;

    public final static int ID_DIALOG_LACK_COINS=3;


    //唱片相关动画
    private Animation mPanAnim;
    private LinearInterpolator mPanLin;  //线性动画

    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    private ImageView mViewPan;
    private ImageView mViewPanBar;


    //play 按键事件
    private ImageButton mBtnPlayStart;
    //当前动画是否在运行
    private boolean mIsRunning=false;
    //文字框容器
    private ArrayList<WordButton> mAllWords;
    private ArrayList<WordButton> mBtnSelectWords;
    private MyGridView mMyGridView;

    //已选择文字框UI容器
    private LinearLayout mViewWordsContainer;

    //当前歌曲
    private Song mCurrentSong;

    //当前关索引
    private int mCurrentStageIndex=-1;
    private TextView mCurrentStagePassView;
    private TextView mCurrentStageView;
    //过关界面
    private View mPassView;

    //当前金币的数量
    private int mCurrentCoins=Const.TOTAL_COINS;

    //金币view
    private TextView mViewCurrentCoins;

    //当前歌曲名称
    private TextView mCurrentSongNamePassView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //读取数据
        int[] datas=Util.loadData(MainActivity.this);
        mCurrentStageIndex=datas[Const.INDEX_LOAD_DATA_STAGE];
        mCurrentCoins=datas[Const.INDEX_LOAD_DATA_COINS];


        mViewWordsContainer= (LinearLayout) findViewById(R.id.word_select_container);
        mMyGridView= (MyGridView) findViewById(R.id.gridview);
        //注册监听
        mMyGridView.registOnWordButtonClick(this);

        mViewPan=(ImageView) findViewById(R.id.imageView1);
        mViewPanBar=(ImageView) findViewById(R.id.imageView2);

        mViewCurrentCoins= (TextView) findViewById(R.id.txt_bar_coin);
        mViewCurrentCoins.setText(mCurrentCoins+"");

        //初始化动画
        mPanAnim= AnimationUtils.loadAnimation(this,R.anim.rotate);
        mPanLin=new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPanBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarInAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_45);
        mBarInLin=new LinearInterpolator();
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarOutAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_d_45);
        mBarOutLin=new LinearInterpolator();
        //mBarOutAnim.setFillAfter(true);
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRunning=false;
                mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBtnPlayStart= (ImageButton) findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handlePlayButton();
                // Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
            }
        });
        //初始化游戏关卡
        initCurrentStageData();
        //处理删除按钮
        handleDeleteWord();
        //处理提示按键
        handleTipAnswer();

        ImageButton btn_back= (ImageButton) findViewById(R.id.btn_bar_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void handlePlayButton(){
        if (mViewPanBar!=null) {
            if (!mIsRunning) {
                mIsRunning = true;
                //开始拨杆进入动画
                mViewPanBar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.INVISIBLE);

                //播放音乐
                MyPlayer.playSong(MainActivity.this,
                        mCurrentSong.getSongFileName());

            }
        }
    }

    @Override
    protected void onPause() {
        //保存游戏数据
        Util.saveData(MainActivity.this,mCurrentStageIndex-1,
                mCurrentCoins);

        mViewPan.clearAnimation();
        //暂停音乐
        MyPlayer.stopTheSong(MainActivity.this);
        super.onPause();
    }
    private Song loadStageSongInfo(int stageIndex){
        Song song=new Song();
        String[] stage=Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return song;
    }

    /**
     * 加载当前关的数据
     */
    private void initCurrentStageData(){
        //读取当前关的歌曲信息
        mCurrentSong=loadStageSongInfo(++mCurrentStageIndex);
        //初始化已选择数据
        mBtnSelectWords=initWordSelect();

        //初始化已选择框
        mBtnSelectWords=initWordSelect();
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(140,140);
        //清空原来的答案
        mViewWordsContainer.removeAllViews();

        for (int i=0;i<mBtnSelectWords.size();i++){
            mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,params);
        }
        //显示当前关的索引
        mCurrentStageView= (TextView) findViewById(R.id.text_current_stage);
        if (mCurrentStageView!=null){
            mCurrentStageView.setText((mCurrentStageIndex+1)+"");
        }
        //获得数据
        mAllWords=initAllWord();
        //更新数据-MyGridView
        mMyGridView.updateData(mAllWords);

        //一开始播放音乐
        //handlePlayButton();


    }
    //初始化待选文字框
    private ArrayList<WordButton> initAllWord(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();
        //获得所有待选文字
        String[] words=generateWords();
        for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            WordButton button=new WordButton();
            button.mWordString=words[i];
            data.add(button);
        }
        return data;
    }

    //初始化已选文字框
    private ArrayList<WordButton> initWordSelect(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();
        for (int i = 0; i < mCurrentSong.getNameLegth(); i++) {
            View convertView= Util.getView(MainActivity.this,R.layout.self_ui_gridview_item);
            final WordButton holder=new WordButton();

            holder.mViewButton= (Button) convertView.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable=false;
            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTheAnswer(holder);
                }
            });

            data.add(holder);
        }
        return data;
    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        //Toast.makeText(MainActivity.this,wordButton.mindex+"",Toast.LENGTH_SHORT).show();
        setSelectWord(wordButton);
        //获得答案状态
        int checkResult=checkTheAnswer();
        //检查答案
        if (checkResult==STATUS_ANSWER_RIGHT){
            //过关并且获得奖励
            handlePassEvent();
            //Toast.makeText(MainActivity.this,"OOOOOk", Toast.LENGTH_SHORT).show();

        }else if (checkResult==STATUS_ANSWER_WRONG){
            //闪烁文字并提示用户
            sparkTheWrods();
        }else if (checkResult==STATUS_ANSWER_LACK){
            //设置文字颜色为白色
            for (int i = 0; i < mBtnSelectWords.size(); i++) {
                mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);

            }
        }
    }

    /**
     *
     * 处理过关界面
     */
    private void handlePassEvent(){
        //显示过关界面
        mPassView=(LinearLayout) this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);
        //停止未完成的动画
        mViewPan.clearAnimation();
        //停止正在播放的音乐
        MyPlayer.stopTheSong(MainActivity.this);
        //播放音效
        MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_STONE_COIN);
        //增加金币
        mCurrentCoins+=Const.ADD_COIN;
        mViewCurrentCoins.setText(mCurrentCoins+"");


        //当前关的索引
        mCurrentStagePassView= (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView!=null){
            mCurrentStagePassView.setText((mCurrentStageIndex+1)+"");
        }
        //显示歌曲名称
        mCurrentSongNamePassView=(TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentStagePassView!=null){
            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
        }
        //下一关按键处理
        ImageButton btnPass= (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judegAppPassed()){
                    //进入通关界面
                    Util.startActivity(MainActivity.this, AllPassView.class);
                }else {
                    //开始下一关
                    mPassView.setVisibility(View.GONE);
                    //加载关卡数据
                    initCurrentStageData();
                }
            }
        });

        //分享到微信
        ImageButton weixin= (ImageButton) findViewById(R.id.btn_share);
        weixin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }

    /**
     * 判断是否通关
     * @return
     */
    private boolean judegAppPassed(){
        return mCurrentStageIndex==Const.SONG_INFO.length-1;
    }

    //清除答案
    private void clearTheAnswer(WordButton wordButton){
        wordButton.mViewButton.setText("");
        wordButton.mWordString="";
        wordButton.mIsVisiable=false;
        setButtonVisiable(mAllWords.get(wordButton.mindex),View.VISIBLE);
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);

        }
    }


    //设置答案

    private void setSelectWord(WordButton wordButton){
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                //设置答案文字框的内容及可见性
                mBtnSelectWords.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectWords.get(i).mIsVisiable=false;
                mBtnSelectWords.get(i).mWordString=wordButton.mWordString;
                //记录索引
                MyLog.d(TAG,mBtnSelectWords.get(i).mindex+"");

                mBtnSelectWords.get(i).mindex=wordButton.mindex;
                //设置待选框的可见性
                setButtonVisiable(wordButton,View.INVISIBLE);
                break;

            }
        }
    }
    //设置待选文字框是否可见
    private void setButtonVisiable(WordButton button,int visibility){
        button.mViewButton.setVisibility(visibility);
        button.mIsVisiable=(visibility==View.VISIBLE)?true:false;
        MyLog.d(TAG,button.mIsVisiable+"");

    }
    //生成所有待选文字
    private String[] generateWords(){
        Random random=new Random();
        String[] words=new String[MyGridView.COUNTS_WORDS];
        //存入歌名
        for (int i = 0; i < mCurrentSong.getNameLegth(); i++) {
                words[i]=mCurrentSong.getNameCharacters()[i]+"";
        }
        //获取随机文字并存入数组
        for (int i=mCurrentSong.getNameLegth();i<MyGridView.COUNTS_WORDS;i++)
        {
                words[i]=Util.getRandomChar()+"";
        }
        //打乱文字顺序
        for (int i = MyGridView.COUNTS_WORDS-1; i >=0 ; i--) {
            int index=random.nextInt(i+1);
            String buf=words[index];
            words[index]=words[i];
            words[i]=buf;
        }

        return words;
    }

    //检查答案
    private int checkTheAnswer(){
        //先检查长度
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            //如果有空的，说明答案不完整
            if (mBtnSelectWords.get(i).mWordString.length()==0) {
                return STATUS_ANSWER_LACK;
            }
        }
        //答案完整，继续检查正确性
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            sb.append(mBtnSelectWords.get(i).mWordString);
        }
        return (sb.toString().equals(mCurrentSong.getSongName()))?STATUS_ANSWER_RIGHT:STATUS_ANSWER_WRONG;

    }
    //文字闪烁
    private void sparkTheWrods(){
        //定时器相关
        TimerTask task=new TimerTask() {
            boolean mChange=false;
            int mSpardTimes=0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++mSpardTimes>SPARSH_TIMES){
                            return;
                        }
                        //执行闪烁逻辑，交替显示红色和白色文字
                        for (int i = 0; i < mBtnSelectWords.size(); i++) {
                            mBtnSelectWords.get(i).mViewButton.setTextColor(
                                    mChange?Color.RED :Color.WHITE);
                        }
                        mChange=!mChange;
                    }
                });
            }
        };
        Timer timer=new Timer();
        timer.schedule(task,1,150);
    }

    private void tipAnswer(){

        boolean tipWord=false;
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                //减少金币数量
                if (!handleCoins(-getTipConis()))
                {
                    //金币数量不够
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                    return;
                }
                //根据当前的答案框条件选择对应的文字并填入
                onWordButtonClick(findIsAnswerWord(i));
                tipWord=true;


                break;
            }

        }
        //没有找到可以填充的答案
        if (!tipWord){
            //闪烁文字提示用户
            sparkTheWrods();
        }



    }

    /**
     * 找到一个答案文字
     * @param index  答案框的索引
     * @return
     */
    private WordButton findIsAnswerWord(int index){

        WordButton button=null;
        for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            button=mAllWords.get(i);
            if (button.mWordString.equals(""+mCurrentSong.getNameCharacters()[index])){
                return button;
            }


        }
        return null;
    }














    /**
     * 删除文字
     */
    private void  deleteOneWord(){

        if (currentAllWordsIsVisiableSize()!=mCurrentSong.getNameLegth()) {
            //减少金币
            if (!handleCoins(-getDeleteWordCoins())){
                //金币不够
                showConfirmDialog(ID_DIALOG_LACK_COINS);
                return;
            }

            //将这个索引对应的WordButton设置为不可见
            setButtonVisiable(findNotAnswerWord(), View.INVISIBLE);
        }else {
            //已经只剩下答案了
            return;
        }

    }

    /**
     * 找到一个不是答案的文字，并且当前是可见的
     * @return
     */
    private WordButton findNotAnswerWord(){
        Random random=new Random();
        WordButton button=null;
        while (true){
            int index=random.nextInt(MyGridView.COUNTS_WORDS);
            button=mAllWords.get(index);
            if (button.mIsVisiable && !isTheAnswerWord(button)){
                return button;
            }
        }
    }
    private int currentAllWordsIsVisiableSize(){
        int size=0;
        WordButton button=null;
        for (int i = 0; i < mAllWords.size(); i++) {
            button=mAllWords.get(i);
            if (button.mIsVisiable){
                size++;
            }
        }
        return size;
    }

    /**
     * 判断某个文字是否为答案
     * @param word
     * @return
     */
    private boolean isTheAnswerWord(WordButton word){
        boolean result=false;
        for (int i = 0; i < mCurrentSong.getNameLegth(); i++) {
            if (word.mWordString.equals(""+mCurrentSong.getNameCharacters()[i])){
                result=true;
                break;
            }

        }
        return result;
    }


    /**
     * 增加或者减少指定数量的金币
     * @param data
     * @return true 增加/减少成功 ，false失败
     */
    private boolean handleCoins(int data){
        //判断当前总的金币数量是否可被减少
        if (mCurrentCoins+data>=0){
            mCurrentCoins+=data;
            mViewCurrentCoins.setText(mCurrentCoins+"");
            return true;
        }else {
            //金币不够

            return false;
        }
    }

    /**
     * 从配置文件里读取删除操作所要用的金币
     * @return
     */
    private int getDeleteWordCoins(){
        return this.getResources().getInteger(R.integer.pay_delete_word);
    }

    /**
     *
     * @return
     */
    private int getTipConis(){
        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }

    /**
     * 处理删除文字事件
     */
    private void handleDeleteWord() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteOneWord();
                showConfirmDialog(ID_DIALOG_DELETE_WORD);
            }
        });
    }

    /**
     * 处理提示按键事件
     */
    private void handleTipAnswer(){
        ImageButton button= (ImageButton) findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //tipAnswer();
                showConfirmDialog(ID_DIALOG_TIP_ANSWER);
            }
        });
    }


    //自定义AlertDialog事件响应
    //删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener =
            new IAlertDialogButtonListener() {
                @Override
                public void onClidk() {
                    //执行事件
                    deleteOneWord();
                }
            };
    //自定义AlertDialog事件响应
    //答案提示
    private IAlertDialogButtonListener mBtnOkTipAnswerListener =
            new IAlertDialogButtonListener() {
                @Override
                public void onClidk() {
                    //执行事件
                    tipAnswer();
                }
            };

    //自定义AlertDialog事件响应
    //金币缺少
    private IAlertDialogButtonListener mBtnOkLackCoinsListener =
            new IAlertDialogButtonListener() {
                @Override
                public void onClidk() {
                    //执行事件

                }
            };

    /**
     * 显示对话框
     * @param id
     */
    private void showConfirmDialog(int id){
        switch (id){
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this,
                        "确认花掉"+getDeleteWordCoins()+"个金币去掉一个错误答案？",
                        mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this,
                        "确认花掉"+getTipConis()+"个金币获得一个文字提示？",
                        mBtnOkTipAnswerListener);
                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this,
                        "金币不足,去商店补充？",
                        mBtnOkLackCoinsListener);
                break;

        }
    }


}
