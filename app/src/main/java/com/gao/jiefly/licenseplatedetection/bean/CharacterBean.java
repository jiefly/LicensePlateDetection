package com.gao.jiefly.licenseplatedetection.bean;

import org.opencv.core.Mat;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class CharacterBean {
    private Mat srcMat;

    public CharacterBean(Mat srcMat) {
        this.srcMat = srcMat;
    }

    public Mat getSrcMat() {
        return srcMat;
    }
}
