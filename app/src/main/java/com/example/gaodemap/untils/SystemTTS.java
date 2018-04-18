package com.example.gaodemap.untils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import com.example.gaodemap.myinterface.ICallBack;
import com.example.gaodemap.myinterface.TTS;

import java.util.Locale;

/**
 * Created by emcc-pc on 2018/4/16.
 * 系统语音播报
 */

public class SystemTTS extends UtteranceProgressListener implements TTS,TextToSpeech.OnUtteranceCompletedListener {

    private Context mContext;
    private static SystemTTS singleton;
    private TextToSpeech textToSpeech; // 系统语音播报类
    private boolean isSuccess = true;

    private SystemTTS(Context context){
        this.mContext=context.getApplicationContext();

        textToSpeech=new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                /*系统语音初始化成功*/
                 if(i==TextToSpeech.SUCCESS){
                     int result=textToSpeech.setLanguage(Locale.CHINA);
                     textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                     textToSpeech.setSpeechRate(1.0f);
                     textToSpeech.setOnUtteranceProgressListener(SystemTTS.this);
                     textToSpeech.setOnUtteranceCompletedListener(SystemTTS.this);
                     if (result == TextToSpeech.LANG_MISSING_DATA
                             || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                         //系统不支持中文播报
                         isSuccess = false;
                     }
                 }
            }
        });
    }

    public static SystemTTS getInstance(Context context) {
        if (singleton == null) {
            synchronized (SystemTTS.class) {
                if (singleton == null) {
                    singleton = new SystemTTS(context);
                }
            }
        }
        return singleton;
    }

    /*UtteranceProgressListener*/
    @Override
    public void onStart(String s) {

    }

    @Override
    public void onDone(String s) {

    }

    @Override
    public void onError(String s) {

    }
   /*TTS*/
    @Override
    public void init() {

    }

    @Override
    public void playText(String playText) {
        if(!isSuccess){
            return;
        }
        if(textToSpeech!=null){
            textToSpeech.speak(playText,TextToSpeech.QUEUE_ADD,null,null);
        }
    }

    @Override
    public void stopSpeak() {
           if(textToSpeech!=null){
               textToSpeech.stop();
           }
    }

    @Override
    public void destroy() {
           stopSpeak();
           if(textToSpeech!=null){
               textToSpeech.shutdown();
           }
           singleton=null;
    }



    @Override
    public boolean isPlaying() {
        return false;
    }

    ICallBack callBack = null;
    @Override
    public void setCallback(ICallBack callback) {
        this.callBack=callback;
    }
   /*OnUtteranceCompletedListener    //播报完成回调*/
    @Override
    public void onUtteranceCompleted(String s) {

    }
}
