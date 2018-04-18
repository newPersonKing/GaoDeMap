package com.example.gaodemap.myinterface;

/**
 * Created by emcc-pc on 2018/4/16.
 */

public interface TTS {

    public void init();
    public void playText(String playText);
    public void stopSpeak();
    public void destroy();
    public boolean isPlaying();
    public void setCallback(ICallBack callback);

}
