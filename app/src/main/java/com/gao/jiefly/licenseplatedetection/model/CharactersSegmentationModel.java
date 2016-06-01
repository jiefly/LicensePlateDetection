package com.gao.jiefly.licenseplatedetection.model;

import android.util.Log;

import com.gao.jiefly.licenseplatedetection.Util;
import com.gao.jiefly.licenseplatedetection.bean.CharacterBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class CharactersSegmentationModel implements IcharactersSegmentationModel {
    //    要识别的车牌
    private Mat srcMat;
    //    识别出的字符
    private List<CharacterBean> results = new ArrayList<>();
    //    切割好上下部分之后的车牌
    private Mat matH;
    //    车牌的水平投影
    private int[] resultH;
    //    切割好之后的车牌的垂直投影

    public LicensePlateBean getLicensePlateBean() {
        return mLicensePlateBean;
    }

    public void setLicensePlateBean(LicensePlateBean licensePlateBean) {
        mLicensePlateBean = licensePlateBean;
    }

    public Mat getSrcMat() {
        return srcMat;
    }

    public void setSrcMat(Mat srcMat) {
        this.srcMat = srcMat;
    }

    public List<CharacterBean> getResults() {
        return results;
    }

    public void setResults(List<CharacterBean> results) {
        this.results = results;
    }

    public Mat getMatH() {
        return matH;
    }

    public void setMatH(Mat matH) {
        this.matH = matH;
    }

    public int[] getResultH() {
        return resultH;
    }

    public void setResultH(int[] resultH) {
        this.resultH = resultH;
    }

    public int[] getResultV() {
        return resultV;
    }

    public void setResultV(int[] resultV) {
        this.resultV = resultV;
    }

    public int getThresholdH() {
        return thresholdH;
    }

    public void setThresholdH(int thresholdH) {
        this.thresholdH = thresholdH;
    }

    public int getThresholdV() {
        return thresholdV;
    }

    public void setThresholdV(int thresholdV) {
        this.thresholdV = thresholdV;
    }

    private int[] resultV;
    //    水平切割的阈值
    private int thresholdH;
    //    垂直分割字符的阈值
    private int thresholdV;

    public CharactersSegmentationModel(LicensePlateBean licensePlateBean) {
        mLicensePlateBean = licensePlateBean;
        srcMat = mLicensePlateBean.getSrcMat();
        thresholdH = srcMat.cols() / 5;
    }

    private LicensePlateBean mLicensePlateBean;

    @Override
    public List<CharacterBean> getCharacter(int threshold, int num) {
//        获取待分割车牌的水平方向投影，用于切除上下部分多余的地方
        resultH = Projection(srcMat, 0).get("H");
/*//        获取resultH中的最小值，作为切割的阈值
        int minH = 1000;
        for (int x:resultH){
            if (x<minH){
                minH = x;
            }
        }*/
//        切除上下部多余部分

        matH = cutH(srcMat, resultH, thresholdH);
//        获取切除时候的mat的垂直方向的投影
        resultV = Projection(matH, 1).get("V");
//       获取resultV中的最小值
        int minV = 1000;
        for (int x : resultV) {
            if (x < minV)
                minV = x;
        }
        thresholdV = minV + 1;
        results = cutV(matH, resultV, thresholdV, 8);
        return results;
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

    public static Mat cutH(Mat mat, int[] resultH, int minValue) {
        int len = resultH.length;
        int top = 0;
        int bottom = 0;
        int minBefore = 1000;
        int minAfter = 1000;
//        求前三分之一中的最小值，以此作为top检测的阈值的基值
        for (int i = 0; i < len / 3; i++) {
            if (resultH[i] < minBefore)
                minBefore = resultH[i];
        }

//        求后三分之二中的最小值，以此作为bottom检测的阈值的基值
        for (int i = 2 * len / 3; i < len; i++) {
            if (resultH[i] < minAfter)
                minAfter = resultH[i];
        }
//        在前三分之一中找出上半部分分割点
        for (int i = 0; i < len / 3; i++) {
            if (resultH[i] < minValue + minBefore) {
                if (Util.checkAfterPointIsRight(resultH, minValue + minBefore, i)) {
                    top = i;
                }
            }
        }
//        在后三分之一中找出后半部分分割点
//        下半部分也需要验证分割点的正确性
        for (int i = (2 * len) / 3; i < len; i++) {
            if (resultH[i] < minValue + minAfter && i > top) {

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
    public static List<CharacterBean> cutV(Mat mat, int[] resultV, int minValue, int charCount) {
        int len = resultV.length;
        int count = charCount;
//      存储分割后的mat
        List<CharacterBean> results = new ArrayList<>();

        int startX = 0;
        int endX = 0;
        for (int i = 0; i < len; i++) {
            if (startX == 0) {
                if (resultV[i] > minValue) {
                    if (count == 8) {
                        if (Util.afterValueIsVaild(resultV, i, len / 20, minValue, 1))
                            startX = i;
                    } else {
                        if (Util.afterValueIsVaild(resultV, i, len / 100, minValue, 1))
                            startX = i;
                    }
                }
            } else {
                if (resultV[i] < minValue) {
                    endX = i;
                    count--;
                    results.add(new CharacterBean(Util.getRoiMat(mat, startX, endX)));
                    if (count <= 0)
                        return results;
                    startX = 0;
                    endX = 0;
//                    为了防止两个字符之间只间隔一个像素，而发生的后面一个字符的漏检
                    i -= 1;
                }
            }
        }
        return results;
    }
}
