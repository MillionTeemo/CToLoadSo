package com.zltech.ctoloadso;

/***********************************************************
 * Copyright © 正链科技（深圳）有限公司. All rights reserved.
 * File com.luck.newmainfram
 * Author ID: wuguoqiang Version:  2023/7/6 
 * Description:
 * Others:
 * History:
 * 内部文档 注意保密
 * 正链科技（深圳）有限公司
 * Modification:
 ******************************************************/
public class CrashHandleApi {
    public  static CrashHandleApi intence;

    public static  CrashHandleApi getIntence(){
        synchronized (CrashHandleApi.class){
            if (intence==null){
                intence = new CrashHandleApi();
            }
        }
        return intence;
    }

    public native void  setCrashCapture();
}
