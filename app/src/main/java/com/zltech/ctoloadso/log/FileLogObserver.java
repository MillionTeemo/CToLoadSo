package com.zltech.ctoloadso.log;

import android.os.FileObserver;

import java.io.File;

/***********************************************************
 ******************************************************/
public class FileLogObserver extends FileObserver {
    protected String mPath ;
    protected String filename ;
    protected int mMask;
    public static final int DEFAULT_MASK = CREATE | MODIFY | DELETE;
    protected File Observerfile;
    public IFileListener iFileListener;
    public int  outMaxSize = 100 ;  //单位M

    public FileLogObserver(File file, IFileListener iFileListener){
        this(file.getPath(),DEFAULT_MASK);
        this.Observerfile = file;
        this.iFileListener = iFileListener;
    }

    public FileLogObserver(File file, int outMaxSize, IFileListener iFileListener){
        this(file.getPath(),DEFAULT_MASK);
        this.outMaxSize = outMaxSize;
        this.Observerfile = file;
        this.iFileListener = iFileListener;
    }

    public FileLogObserver(String path , int mask){
        super(path , mask);
        mMask = mask;
        System.out.println("add FileLogObserver :" +path);
    }

    public  interface IFileListener{
        void FileWriteListen(File file,float size);
    }
    private  long time = 0l;
    @Override
    public void onEvent(int i, String path) {
        int event = i&FileObserver.ALL_EVENTS;
        switch (event){
            case FileObserver.CREATE:

                break;
            case FileObserver.DELETE:
                try {
                    File file = new File(Observerfile.getPath());
                    file.createNewFile();
                    System.out.println("删除后重新创建 Observerfile :" + Observerfile.getPath());
                }catch (Exception e){}

                break;
            case FileObserver.MODIFY:
                if (System.currentTimeMillis()-time<10*1000){
                    return;
                }
                float size = (float) (Observerfile.length()* 1.0/ (1024 * 1024));
                if (size >= outMaxSize) {
                    System.out.println(Observerfile.getName() +" size to MAX!  >>" + size);
                    if (iFileListener!=null){
                        iFileListener.FileWriteListen(Observerfile,size);
                    }
                }
                time = System.currentTimeMillis();
                break;
        }
    }
}
