package com.zltech.ctoloadso.log;

import android.util.Log;


import com.zltech.ctoloadso.ApplicationLoader;
import com.zltech.ctoloadso.BuildConfig;
import com.zltech.ctoloadso.SharePreferenceUtils;
import com.zltech.ctoloadso.time.FastDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Locale;


public class FileLog implements FileLogObserver.IFileListener{
    private OutputStreamWriter streamWriter = null;
    private FastDateFormat dateFormat = null;
    private DispatchQueue logQueue = null;
    private File currentFile = null;
    private File File_aclogs = null;
    private boolean initied;
    private FileLogObserver fileLogObserver;
    private int MaxSize = 150; //aclog 日志总值 150M
    private final static String tag = "ZDAN_App";
    private String[] Aclogs = {"logcat_0.log","logcat_1.log","logcat_2.log"};

    private static volatile FileLog Instance = null;
    public static FileLog getInstance() {
        FileLog localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLog.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileLog();
                }
            }
        }
        return localInstance;
    }

    public FileLog() {
        init();
    }
    public static long getFileLength(String filePath){
        long fileSize = 0;
        File f = new File(filePath);
        if (f.exists() && f.isFile()){
            fileSize = f.length();
        }else{
            Log.e("getFileSize","file doesn't exist or is not a file");
        }
        return fileSize;
    }


    public void init() {
        if (initied) {
            return;
        }
        StringBuffer init_buffer  = new StringBuffer();
        try {
            if (ApplicationLoader.applicationContext == null) {
                return;
            }
            File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
            if (sdCard == null) {
                return;
            }
            File_aclogs = new File(sdCard.getParent() + "/zdan/aclogs");
            File_aclogs.mkdirs();
            String print ="FileLog "+File_aclogs.getPath()+" aclogs  size ="+File_aclogs.length();
            System.out.println(print);
            init_buffer.append(print+"\n");
            for (File fileP:File_aclogs.listFiles()){
                String print1 ="FileLog "+" aclogs : "+ fileP.getPath() +"  size ="+fileP.length();
                System.out.println(print1);
                init_buffer.append(print1+"\n");
            }
            clearOthrelog();
            String logtag =(String)SharePreferenceUtils.get(ApplicationLoader.applicationContext,"logtag",Aclogs[0]);
            long logtag1 = 0;
            long logtag2 = 0;
            long logtag3 = 0;
            for (int i = 0; i <Aclogs.length ; i++) {
                File file = new File(File_aclogs, Aclogs[i]);
                if (!file.exists()) {
                    file.createNewFile();
                    String print1 = "FileLog " + "new create ：" + file.getName();
                    System.out.println(print1);
                    init_buffer.append(print1 + "\n");
                }
                if (i == 0) { logtag1 = file.length();}
                if (i == 1) {logtag2 = file.length();}
                if (i == 2) { logtag3 = file.length();}
            }
            if (logtag2==0&&logtag3==0){
                float file_size = (float) (logtag1* 1.0/ (1024 * 1024));
                if (file_size<MaxSize/3) {
                    logtag = Aclogs[0];
                }else {
                    logtag = Aclogs[1];
                }
            }

            currentFile = new File(File_aclogs,logtag);

        } catch (Exception e) {
            e.printStackTrace();
        }
        dateFormat = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss", Locale.CHINA);
        try {
            logQueue = new DispatchQueue("logQueue");
            FileOutputStream stream  ;
            float file_size = (float) (currentFile.length()* 1.0/ (1024 * 1024));
            if (file_size<MaxSize/3){
                stream = new FileOutputStream(currentFile,true);
                String print =currentFile.getName() +" append size ="+file_size;
                System.out.println(print);
                init_buffer.append(print+"\n");
            }else{
                currentFile = changWirteFile(currentFile);
                stream = new FileOutputStream(currentFile);
            }
            int index = 0;
            for (int i = 0; i < Aclogs.length; i++) {
                if (currentFile.getName().contains(Aclogs[i])){
                    index = i;
                }
            }
            fileLogObserver = new FileLogObserver(currentFile,MaxSize/3,this);
            fileLogObserver.startWatching();
            SharePreferenceUtils.put(ApplicationLoader.applicationContext,"logtag",Aclogs[index]);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("========== start log " + dateFormat.format(System.currentTimeMillis()) + " ========== \n");
            //   streamWriter.write( init_buffer.toString()+ "-----\n");
            streamWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initied = true;
    }

    private void onlysave_one(File dir) {
        String today_name = dateFormat.format(new Date());
        if (dir.isDirectory()) {
            dir.listFiles(pathname -> {
                if (!pathname.getName().contains(today_name)) {
                    pathname.delete();
                }
                float size = (float) (pathname.length()* 1.0/ (1024 * 1024));
                if (size>=1024){ //
                    try {
                        pathname.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            });
        }
    }

    public static void ensureInitied() {
        getInstance().init();
    }

    public static void e(final String message, final Throwable exception) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }
        ensureInitied();
        Log.e(tag, message+"", exception);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis())+ " E/tmessages: " + message + "\n");
                    getInstance().streamWriter.write(exception.toString());
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final String message) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }
        ensureInitied();
        Log.e(tag, message+"");
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis())+" E/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final Throwable e) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }
        ensureInitied();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    StackTraceElement[] stack = e.getStackTrace();
                    for (int a = 0; a < stack.length; a++) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + stack[a] + "\n");
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            e.printStackTrace();
        }
    }

    public static void d(final String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        ensureInitied();
        Log.d(tag, message+"");
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis())+ " D/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void w(final String message) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }
        ensureInitied();
        Log.w(tag, message+"");
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis())+ " W/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void i(final String message) {
//        if (!BuildConfig.DEBUG) {
//            return;
//        }
        ensureInitied();
        Log.w(tag, message+"");
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis())+ " I/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void cleanupLogs() {
        ensureInitied();
        File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        if (sdCard == null) {
            return;
        }
        File dir = new File(sdCard.getAbsolutePath() + "/aclogs");
        File[] files = dir.listFiles();
        if (files != null) {
            for (int a = 0; a < files.length; a++) {
                File file = files[a];
                if (getInstance().currentFile != null && file.getAbsolutePath().equals(getInstance().currentFile.getAbsolutePath())) {
                    continue;
                }
                file.delete();
            }
        }
    }
    private void clearOthrelog(){
        File_aclogs.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!name.contains("logcat")){
                    new File(dir,name).delete();
                }
                return false;
            }
        });
    }

    @Override
    public void FileWriteListen(File Observerfile,float size) {
        currentFile = changWirteFile(Observerfile);
        fileLogObserver = new FileLogObserver(currentFile,MaxSize/3,Instance);
        fileLogObserver.startWatching();

        try {
            streamWriter.close();
            FileOutputStream stream  ;
            float file_size = (float) (currentFile.length()* 1.0/ (1024 * 1024));
            if (file_size<MaxSize/3){
                stream = new FileOutputStream(currentFile,true);
                System.out.println(currentFile.getName() +" append size ="+file_size);
            }else{
                currentFile.createNewFile();
                stream = new FileOutputStream(currentFile);
            }
            int index = 0;
            for (int i = 0; i < Aclogs.length; i++) {
                if (currentFile.getName().contains(Aclogs[i])){
                    index = i;
                }
            }
            SharePreferenceUtils.put(ApplicationLoader.applicationContext,"logtag",Aclogs[index]);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("========== start log " + dateFormat.format(System.currentTimeMillis()) + " ========== \n");;
            streamWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        float currentFile_size = (float) (currentFile.length()* 1.0/ (1024 * 1024));
        System.out.println("FileLog "+currentFile.getName()+" >> "+currentFile_size);
        try {
            float file_size = (float) (Observerfile.length()* 1.0/ (1024 * 1024));
            System.out.println("FileLog "+Observerfile.getName()+" >> "+file_size);

        }catch (Exception e){}
    }

    private File changWirteFile(File curFile)  {
        String[] names = curFile.getName().split("_");
        int tag =Integer.parseInt(names[1].substring(0,1));
        int next_tag = tag==2?0:tag+1;
        System.out.println("FileLog "+curFile.getName()+" tag>> "+tag);
        File[] files  = File_aclogs.listFiles();
        for (int i = 0; i <files.length ; i++) { //其他文件超了清空文件
            File file = files[i];
            float file_size = (float) (file.length()* 1.0/ (1024 * 1024));
            System.out.println("FileLog "+file.getName()+" >> "+file_size);
            if (!file.getName().contains(curFile.getName())&&file_size>=MaxSize/3){
                try {
                    file.createNewFile();
                }catch (Exception e){
                    System.out.println("FileLog "+file.getName()+" 清除失败 ");
                }
            }
        }
        String next_name = Aclogs[next_tag];
        return  new File(File_aclogs,next_name);

    }
}
