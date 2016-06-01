package com.gao.jiefly.licenseplatedetection.bean;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class LicensePlateBean {
    //    要识别的车牌
    private Mat srcMat;
    //    识别出的字符
    private List<CharacterBean> results = new ArrayList<>();
    //    切割好上下部分之后的车牌
    private Mat matH;
    //    车牌的水平投影
    private int[] resultH;
    //    切割好之后的车牌的垂直投影
    private int[] resultV;
    //    水平切割的阈值
    private int thresholdH;
    //    垂直分割字符的阈值
    private int thresholdV;

    public int getThresholdV() {
        return thresholdV;
    }
    public void setThresholdV(int thresholdV) {
        this.thresholdV = thresholdV;
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

    public LicensePlateBean(Mat srcMat) {
        this.srcMat = srcMat;
    }
}
