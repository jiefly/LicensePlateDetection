package com.gao.jiefly.licenseplatedetection;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.MatOfPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by jiefly on 2016/5/25.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class Util {
    /**
     * 保存方法
     */
    public static void saveBitmap(String picName, Bitmap bitmap) {
        Log.e("jiefly", "保存图片");
        File f = new File("/sdcard/licensePlate", picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("jiefly", "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<String> getPicPath(String dirPath, Context context) {
        List<String> paths = new ArrayList<>();
        String selection = MediaStore.Images.Media.DATA +  " like %?";
        String[] selectionArgs = {dirPath + "%"};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                paths.add(cursor.getString(cursor.getColumnIndex("")));
            } while (cursor.moveToNext());
        }
        return paths;
    }

    public static Vector<String> GetImageFileName(String fileAbsolutePath) {
        Vector<String> vecFile = new Vector<String>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                // 判断是否为MP4结尾
                if (filename.trim().toLowerCase().endsWith(".jpg")) {
                    vecFile.add(filename);
                }
            }
        }
        return vecFile;
    }
    public static MatOfPoint getMaxMatOfPoint(List<MatOfPoint> matOfPoints){
        if (matOfPoints.size()<=0)
            return null;
        MatOfPoint maxMatOfPoint = matOfPoints.get(0);
       for (MatOfPoint matOfPoint:matOfPoints){
           if (maxMatOfPoint.height()<matOfPoint.height()){
               maxMatOfPoint = matOfPoint;
           }
       }
        return maxMatOfPoint;
    }
}
