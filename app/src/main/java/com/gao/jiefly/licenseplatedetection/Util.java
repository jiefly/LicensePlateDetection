package com.gao.jiefly.licenseplatedetection;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by jiefly on 2016/5/25.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class Util {

    /*
    * 计算两个点之间的距离
    * */
    public static int getLenFromTwoPoint(Point start,Point end){
        return (int) Math.sqrt((start.x - end.x)*(start.x - end.x)+(start.y - end.y)*(start.y - end.y));
    }
    /**
     * 保存图片
     * 参数picName:要保存的图片的名称
     * 参数bitmap：保存图片的bitmap
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<String> getPicPath(String dirPath, Context context) {
        List<String> paths = new ArrayList<>();
        String selection = MediaStore.Images.Media.DATA + " like %?";
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

    /*
    * 获取一个文件夹中所有.jpg文件路径
    * 参数fileAbsolutePath：文件夹绝对路径
    * 返回值vecFile:所有.jpg图片路径的集合
    * */
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

    public static MatOfPoint getMaxMatOfPoint(List<MatOfPoint> matOfPoints) {

        if (matOfPoints.size() <= 0)
            return null;
        MatOfPoint maxMatOfPoint = matOfPoints.get(0);
        for (MatOfPoint matOfPoint : matOfPoints) {
            if (maxMatOfPoint.height() < matOfPoint.height()) {
                maxMatOfPoint = matOfPoint;
            }
        }
        return maxMatOfPoint;
    }

    /*
    * 用于分割车牌中的字符
    * 大致流程为：遍历行中的投影值，
    * 1.找出第一个大于阈值的投影值，将其位置设置为字符的左边界，
    * 2.在找出左边界之后，遍历到的第一个小于阈值的投影值，将其位置设置为字符的右边界
    * 3.以此类推，找出所有七个字符（八个字符，包括第二和第三之间的小圆点）
    *
    * 参数mat:要分割的图像的mat
    * 参数resultV:mat的垂直投影值
    * 参数minValue：阈值
    * 参数charCount：要分割的字符个数
    * 返回值：分割后的字符图片集合
    * */
    public static List<Mat> cutV(Mat mat, int[] resultV, int minValue, int charCount) {
        int len = resultV.length;
        int count = charCount;
//      存储分割后的mat
        List<Mat> results = new ArrayList<>();

        int startX = 0;
        int endX = 0;
        for (int i = 0; i < len; i++) {
            if (startX == 0) {
                if (resultV[i] > minValue) {
                    if (count == 8) {
                        if (afterValueIsVaild(resultV,i,len/20,minValue,1))
                            startX = i;
                    }else {
                        if (afterValueIsVaild(resultV,i,len/100,minValue,1))
                            startX = i;
                    }
                }
            } else {
                if (resultV[i] < minValue) {
                    endX = i;
                    count--;
                    results.add(getRoiMat(mat, startX, endX));
                    if (count <= 0)
                        return results;
                    startX = 0;
                    endX = 0;
//                    为了防止两个字符之间只间隔一个像素，而发生的后面一个字符的漏检
                    i-=1;
                }
            }
        }
        return results;
    }

    /*
    * 用来检测当前坐标之后的一些值是否也符合要求
    * 参数values:待检测的数组
    * 参数currentPosition：检测的起始点
    * 参数num：需要检测起始点之后的多少位数字
    * 参数minValue：检测的阈值
    * 参数type：判断是否有效的方法类型（type为1代表大于等于为有效，type为0代表小于等于为有效）
    * */
    public static boolean afterValueIsVaild(int[] values, int currentPosition, int num, int minValue, int type) {
        if (values.length - currentPosition<num){
            Log.e("afterValueIsVaild","数组长度小于num+currentPosition，可能发生数组越界");
            return false;
        }

        if (type == 0) {
            for (int i = currentPosition; i < currentPosition + num; i++) {
                if (values[i + 1] > minValue) {
                    return false;
                }
            }
            return true;
        } else if (type == 1) {
            for (int i = currentPosition; i < currentPosition + num-1; i++) {
                if (values[i + 1] < minValue) {
                    return false;
                }
            }
            return true;
        } else {
            Log.e("afterValueIsVaild", "请输入正确的比较类型，为1代表大于等于为有效，为0代表小于等于为有效");
        }
        return false;
    }

    /*
    * 用于切割车牌上下不必要的部分
    * 参数mat:要分割的图像的mat
    * 参数result:mat的垂直投影值
    * 参数minValue：阈值H
    * 返回值：切割好的图片的Mat
    * */
    public static Mat cutH(Mat mat, int[] resultH, int minValue) {
        int len = resultH.length;
        int top = 0;
        int bottom = 0;
        int minBefore = 1000;
        int minAfter = 1000;
//        求前三分之一中的最小值，以此作为top检测的阈值的基值
        for (int i = 0; i<len/3;i++){
            if (resultH[i]<minBefore)
                minBefore = resultH[i];
        }

//        求后三分之二中的最小值，以此作为bottom检测的阈值的基值
        for (int i = 2*len/3; i<len;i++){
            if (resultH[i]<minAfter)
                minAfter = resultH[i];
        }
//        在前三分之一中找出上半部分分割点
        for (int i = 0; i < len / 3; i++) {
            if (resultH[i] < minValue+minBefore) {
                if (checkAfterPointIsRight(resultH, minValue+minBefore, i)) {
                    top = i;
                }
            }
        }
//        在后三分之一中找出后半部分分割点
//        下半部分也需要验证分割点的正确性
        for (int i = (2 * len) / 3; i < len; i++) {
            if (resultH[i] < minValue +minAfter && i > top) {

//                如果下部分割点再总长度的六分之五以前则需要进一步验证这个分割点的正确性
                if (i < 9 * len / 10) {
//                    验证之后的十个点是否也都小于阈值，都小于阈值才被判断为正确的分割点
                    boolean isTrue = true;
                    for (int j = i; j < i + 2; j++) {
                        if (resultH[j] > minAfter) {
                            isTrue = false;
                            break;
                        }
                    }
                    if (isTrue) {
                        bottom = i;
                        break;
                    }
                } else {
                    bottom = i;
                    break;
                }
            }
        }
        Point start = new Point(0, top);
        Point end = new Point(mat.cols(), bottom);
        Rect roi = new Rect(start, end);
        return new Mat(mat, roi);
    }

    public static Mat getRoiMat(Mat mat, int start, int end) {
        Point startPoint = new Point(start, 0);
        Point endPoint = new Point(end, mat.rows());
        Rect roi = new Rect(startPoint, endPoint);
        return new Mat(mat, roi);
    }

    /*
    * type为1时返回垂直方向投影，key为V
    * type为0时返回水平方向投影，key为H
    * type为其他时返回的是水平和垂直方向投影
    * */
    public static Map<String, int[]> Projection(Mat mat, int type) {
        /*
        * 1.将mat灰度化
        * 2.二值化
        * 3.逐列扫描mat，记录下累加值
        * 4.返回一个一维数组
        * */
        Mat grayMat = new Mat();
        Mat binMat = new Mat();
        Map<String, int[]> result = new HashMap<>();

//        灰度化
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
//        二值化
        Imgproc.threshold(grayMat, binMat, 150, 255, Imgproc.THRESH_OTSU);
        int row = binMat.rows();
        int col = binMat.cols();
        if (type == 1) {
            int[] resultV = new int[col];
//        垂直方向投影，用来分割字符
            for (int i = 0; i < col; i++) {
                for (int j = 0; j < row; j++) {
                    double[] value = binMat.get(j, i);
                    if (value[0] > 0) {
                        resultV[i] += 1;
                    }
                }
            }
            Log.e("jiefly", resultV.length + "");
            result.put("V", resultV);
        } else if (type == 0) {
            int[] resultH = new int[row];
//        水平方向投影，用来切割出字符多余的上下边
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    double[] value = binMat.get(i, j);
                    if (value[0] > 0) {
                        resultH[i] += 1;
                    }
                }
            }

            result.put("H", resultH);
        } else {
            int[] resultV = new int[col];
//        垂直方向投影，用来分割字符
            for (int i = 0; i < col; i++) {
                for (int j = 0; j < row; j++) {
                    double[] value = binMat.get(j, i);
                    if (value[0] > 0) {
                        resultV[i] += 1;
                    }
                }
            }
            result.put("V", resultV);
            int[] resultH = new int[row];
//        水平方向投影，用来切割出字符多余的上下边
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    double[] value = binMat.get(i, j);
                    if (value[0] > 0) {
                        resultH[i] += 1;
                    }
                }
            }

            result.put("H", resultH);
        }
        return result;
    }

    /*
    * 用来验证上下切割时的正确性
    * 判断起点之后的若干个点是否也符合条件
    * 判断三分之一的高度内的点
    * */
    public static boolean checkAfterPointIsRight(int[] results, int minValue, int currentPosition) {
        for (int i = currentPosition; i < currentPosition + results.length / 10; i++) {
//            之后的1/10区间内都得大于阈值
            if (results[i + 1] < minValue)
                return false;
        }
        return true;
    }
}
