package com.zltech.ctoloadso;

/***********************************************************
 * Copyright © 正链科技（深圳）有限公司. All rights reserved.
 * File com.zltech.ctoloadso
 * Author ID: wuguoqiang Version:  2023/4/21 
 * Description:
 * Others:
 * History:
 * 内部文档 注意保密
 * 正链科技（深圳）有限公司
 * Modification:
 ******************************************************/
public class CApis {

     public    static  CApis  instance;

     public static CApis getInstance(){
         synchronized (CApis.class){
             if (instance==null){
                 instance = new CApis();
             }
         }
         return instance;
     }

     public native  String stringFromJNI(byte[] data);
}
