package com.example.gaodemap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by emcc-pc on 2018/4/17.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            Toast.makeText(context,"卸载了:"  + packageName + "包名的程序",Toast.LENGTH_SHORT).show();
        }
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            Toast.makeText(context,"卸载了:"  + packageName + "包名的程序",Toast.LENGTH_SHORT).show();
        }
    }
}
