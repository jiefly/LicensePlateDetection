package com.gao.jiefly.licenseplatedetection.bean;

import android.graphics.Bitmap;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.getStructuringElement;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class CarPictureBean {
    private static final Size DEFAULT_GAUSSIAN_BLUR_SIZE = new Size(3,3);
    private static final int DEFAULT_DDEPTH = CvType.CV_8U;
    private static final int DEFAULT_DX = 1;
    private static final int DEFAULT_DY = 0;
    private static final int DEFAULT_KSIZE = 3;
    private static final double DEFAULT_SCALE = 1;
    private static final double DEFAULT_DELATE = 0;
    private static final int DEFAULT_TYPE = Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY;
    private static final int DEAFULT_MAX_VALUE = 255;
    private static final int DEFAULT_THRESH = 1;
    private static final Mat DEFAULT_KERNEL = getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3));
    private static final int DEFAULT_ITERATIONS = 1;
    private static final Point DEFAULT_ANCHOR = new Point(1,1);
    private static final List<Mat> DEFAULT_RESULTS = new ArrayList<>();

//    汽车照片名称
    private String picName;
//    汽车照片地址
    private Bitmap picBitmap;
//    高斯滤波参数 kSize
    private Size gaussianBlurSize;
//    Sobel参数
    private int ddepth;
    private int dx;
    private int dy;
    private int kSize;
    private double scale;
    private double delate;

//    二值化参数
    private int type;
    private int maxValue;
    private int thresh;

//    闭操作参数
    private Mat kernel;
    private int iterations;
    private Point anchor;

//    检测出的车牌候选
    private List<Mat> results;

    public CarPictureBean(String picName, Bitmap picBitmap) {
        this.picName = picName;
        this.picBitmap = picBitmap;
        init();
    }

    private void init() {
        gaussianBlurSize = DEFAULT_GAUSSIAN_BLUR_SIZE;
        ddepth = DEFAULT_DDEPTH;
        dx = DEFAULT_DX;
        dy = DEFAULT_DY;
        kSize = DEFAULT_KSIZE;
        scale = DEFAULT_SCALE;
        delate = DEFAULT_DELATE;
        type = DEFAULT_TYPE;
        maxValue = DEAFULT_MAX_VALUE;
        thresh = DEFAULT_THRESH;
        kernel = DEFAULT_KERNEL;
        iterations = DEFAULT_ITERATIONS;
        anchor = DEFAULT_ANCHOR;
        results = DEFAULT_RESULTS;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public Bitmap getPicBitmap() {
        return picBitmap;
    }

    public void setPicBitmap(Bitmap picBitmap) {
        this.picBitmap = picBitmap;
    }

    public Size getGaussianBlurSize() {
        return gaussianBlurSize;
    }

    public void setGaussianBlurSize(Size gaussianBlurSize) {
        this.gaussianBlurSize = gaussianBlurSize;
    }

    public int getDdepth() {
        return ddepth;
    }

    public void setDdepth(int ddepth) {
        this.ddepth = ddepth;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public int getkSize() {
        return kSize;
    }

    public void setkSize(int kSize) {
        this.kSize = kSize;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getDelate() {
        return delate;
    }

    public void setDelate(double delate) {
        this.delate = delate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getThresh() {
        return thresh;
    }

    public void setThresh(int thresh) {
        this.thresh = thresh;
    }

    public Mat getKernel() {
        return kernel;
    }

    public void setKernel(Mat kernel) {
        this.kernel = kernel;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public Point getAnchor() {
        return anchor;
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public List<Mat> getResults() {
        return results;
    }

    public void setResults(List<Mat> results) {
        this.results = results;
    }
}
