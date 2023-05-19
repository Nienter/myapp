package com.minilab.myapplication;

import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileLock;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) {

//        String className = MainActivity.class.getName();
//        System.out.println(className);
//        Season.valueOf("SUMMER").print();
//        System.out.println(Season.class.getClass().getSuperclass().getName());
//        File file = new File("assets");
//        if (file.isDirectory()) {
//            System.out.println("1");
//            File[] files = file.listFiles();
//            for (int i = 0; i < files.length; i++) {
//                m370a(files[i]);
//            }
//        }
//        m370a(new File("t1_11_off.png"));

//        m370a(null, null, null, null);
//        System.out.println(android.os.Build.VERSION.SDK_INT);
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            LocalDate localDate = LocalDate.now();
//            System.out.println(localDate);
////        }
    }

    public synchronized static void m370a(File file) {
        FileInputStream open = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream outputStream = null;
        BufferedOutputStream ot = null;
        try {
            byte[] bytes = "X)!YOkd!H~x(h+v2WY2}YKUWNo41q;k2CL5R2Qfy@Xa;Wx,pZI".getBytes("UTF-8");
//            AssetManager assets = context.getAssets();
            open = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[16384];
            while (true) {
                int read = open.read(bArr, 0, 16384);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            int length = byteArray.length;
            byte[] bArr2 = new byte[length];
            int i = 0;
            for (int i2 = 0; i2 < byteArray.length; i2++) {
                bArr2[i2] = (byte) (byteArray[i2] ^ bytes[i % bytes.length]);
                i++;
                if (i >= 50) {
                    i = 0;
                }
            }
            File file1 = new File("test");
            if (!file1.exists()) {
                file1.mkdir();
            }
//            outputStream = new FileOutputStream(file1.getName()+"/"+file.getName());
//            byteArrayOutputStream.writeTo(outputStream);
//            ot = new BufferedOutputStream(outputStream);
//            ot.write(bArr2);
            fileToBytes(bArr2, "test/", file.getName());
//            return BitmapFactory.decodeByteArray(bArr2, 0, length);
        } catch (IOException unused) {
            System.out.println("e");
//            return null;
        } finally {
//            if (open != null) {
//                try {
//                    open.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (byteArrayOutputStream != null) {
//                try {
//                    byteArrayOutputStream.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (ot != null) {
//                try {
//                    ot.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
        }
    }

    public static void fileToBytes(byte[] bytes, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {

            file = new File(filePath + fileName);
            if (!file.getParentFile().exists()) {
                //文件夹不存在 生成
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
