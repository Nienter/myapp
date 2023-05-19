package com.minilab.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class C4900a {
    /* renamed from: a */
    public static Bitmap m370a(Context context, String str) {
        try {
            byte[] bytes = "X)!YOkd!H~x(h+v2WY2}YKUWNo41q;k2CL5R2Qfy@Xa;Wx,pZI".getBytes("UTF-8");
            AssetManager assets = context.getAssets();
            InputStream open = assets.open(str);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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
            return BitmapFactory.decodeByteArray(bArr2, 0, length);
        } catch (IOException unused) {
            return null;
        }
    }
}