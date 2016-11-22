package util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.zhangshixian.guessmusic.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import data.Const;
import model.IAlertDialogButtonListener;

/**
 * Created by ZHANGSHIXIAN on 2016/11/16.
 */
public class Util {

    private static AlertDialog mAlertDialog;


    public static View getView(Context context, int layoutId){
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout=inflater.inflate(layoutId,null);
        return layout;
    }
    //生成随机汉字
    public static char getRandomChar(){
        String str="";
        int hightPos;
        int lowPos;

        Random random=new Random();
        hightPos=(176+Math.abs(random.nextInt(39)));
        lowPos=(161+Math.abs(random.nextInt(93)));

        byte[] b=new byte[2];
        b[0]=(Integer.valueOf(hightPos)).byteValue();
        b[1]=(Integer.valueOf(lowPos)).byteValue();
        try {
            str=new String(b,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str.charAt(0);

    }

    /**
     * 界面跳转
     * @param context
     * @param des
     */
    public static void startActivity(Context context,Class des){
        Intent intent=new Intent(context,des);
        context.startActivity(intent);
        //关闭当前Activity
        ((Activity)context).finish();
    }

    /**
     * 显示自定义对话框
     * @param context
     * @param msg
     * @param listener
     */
    public static void showDialog(final Context context, String msg, final IAlertDialogButtonListener listener){
        View dialogView=null;

        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        dialogView=getView(context, R.layout.dialog_view);

        ImageButton btnOkView= (ImageButton) dialogView.findViewById(R.id.btn_dialog_ok);
        ImageButton btnCancelView= (ImageButton) dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView textMessageView= (TextView) dialogView.findViewById(R.id.text_dialog_message);
        textMessageView.setText(msg);

        btnOkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.cancel();
                }
                //事件回调
                if (listener!=null) {
                    listener.onClidk();
                }
                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_ENTER);
            }
        });
        btnCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if (mAlertDialog!=null){
                    mAlertDialog.cancel();
                }
                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_STONE_CANCEL);
            }
        });
        //为Dialog设置view
        builder.setView(dialogView);
        mAlertDialog=builder.create();

        //显示对话框
        mAlertDialog.show();
    }

    /**
     * 游戏数据保存
     * @param context
     * @param stagaIndex
     * @param coins
     */
    public static void saveData(Context context,int stagaIndex,
                                int coins){
        FileOutputStream fis=null;
        try {
            fis=context.openFileOutput(Const.FILE_NAME_SAVE_DATA,
                    Context.MODE_PRIVATE);
            DataOutputStream dos=new DataOutputStream(fis);

                dos.writeInt(stagaIndex);
                dos.writeInt(coins);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取游戏数据
     * @param context
     * @return
     */
    public static int[] loadData(Context context){
        FileInputStream fis=null;
        int[] datas={-1,Const.TOTAL_COINS};

        try {
            fis=context.openFileInput(Const.FILE_NAME_SAVE_DATA);
            DataInputStream dis =new DataInputStream(fis);
            datas[Const.INDEX_LOAD_DATA_STAGE]=dis.readInt();
            datas[Const.INDEX_LOAD_DATA_COINS]=dis.readInt();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return datas;
    }
}
