package com.gao.jiefly.licenseplatedetection.presenter;

import android.graphics.Bitmap;

import com.gao.jiefly.licenseplatedetection.bean.CarPictureBean;
import com.gao.jiefly.licenseplatedetection.bean.CharacterBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;
import com.gao.jiefly.licenseplatedetection.model.CharactersSegmentationModel;
import com.gao.jiefly.licenseplatedetection.model.IcharactersSegmentationModel;
import com.gao.jiefly.licenseplatedetection.model.LocateLicensePlateModel;
import com.gao.jiefly.licenseplatedetection.view.IView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class Presenter {
    private IcharactersSegmentationModel mSegmentationModel;
    public LocateLicensePlateModel mLicensePlateModel;
    private CarPictureBean mCarPictureBean;
    private LicensePlateBean mLicensePlateBean;
    private IView mIView;

    public Presenter(IView IView) {
        mIView = IView;
    }

    /*
    * 获取定位的车牌候选集合
    * */
    public List<LicensePlateBean> LocateLicensePlate(CarPictureBean carPictureBean) {
        mLicensePlateModel = new LocateLicensePlateModel(carPictureBean);
        return mLicensePlateModel.locateLicensePlateByShape(carPictureBean);
    }

    /*
    * 设置待检测的图片
    * */
    public void setCarPicture(Bitmap carBitmap) {
        mCarPictureBean = new CarPictureBean("测试", carBitmap);
        mLicensePlateModel = new LocateLicensePlateModel(mCarPictureBean);
    }

    /*
    * 设置带分割的车牌
    * */
    public void setLicensePlate(Bitmap licensePlate) {
        Mat licensePlateMat = new Mat();
        Utils.bitmapToMat(licensePlate, licensePlateMat);
        mLicensePlateBean = new LicensePlateBean(licensePlateMat);
    }

    /*
    * 显示检测结果
    * */
    public void showLocateResult() {
        LocateLicensePlate(mCarPictureBean);
        Bitmap srcBitmap = mCarPictureBean.getPicBitmap();
        Bitmap guassBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Bitmap sobelBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Bitmap binBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Bitmap ExBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mLicensePlateModel.getSobelMat(), sobelBitmap);
        Utils.matToBitmap(mLicensePlateModel.getMorphologyExMat(), ExBitmap);
        Utils.matToBitmap(mLicensePlateModel.getBinMat(), binBitmap);
        Utils.matToBitmap(mLicensePlateModel.getGuassMat(),guassBitmap);
        Utils.matToBitmap(mLicensePlateModel.getGrayMat(),grayBitmap);
        Utils.matToBitmap(mLicensePlateModel.getResultMat(),resultBitmap);
        /*Bitmap testBitmap = Bitmap.createBitmap(mLicensePlateModel.getSrcMat().width(),mLicensePlateModel.getSrcMat().height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mLicensePlateModel.getSrcMat(),testBitmap);
        srcBitmap = testBitmap*/;

        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(srcBitmap);
        bitmapList.add(guassBitmap);
        bitmapList.add(grayBitmap);
        bitmapList.add(sobelBitmap);
        bitmapList.add(binBitmap);
        bitmapList.add(ExBitmap);
        bitmapList.add(resultBitmap);

        if (LocateLicensePlate(mCarPictureBean).size() > 0)
            mLicensePlateBean = LocateLicensePlate(mCarPictureBean).get(0);
        for (LicensePlateBean licensePlateBean:LocateLicensePlate(mCarPictureBean)){
            if (mLicensePlateBean.getSrcMat().width()<licensePlateBean.getSrcMat().width()){
                mLicensePlateBean = licensePlateBean;
            }
        }
        mIView.showLicensePlate(bitmapList);
    }

    /*
    * 显示分割结果
    * */
    public void showCharacters() {
        List<Bitmap> characters = new ArrayList<>();
        mSegmentationModel = new CharactersSegmentationModel(mLicensePlateBean);
        List<CharacterBean> characterBeanList = mSegmentationModel.getCharacter(10, 8);
        for (CharacterBean characterBean : characterBeanList) {
            characters.add(getBitmapFromMat(characterBean.getSrcMat()));
        }
        mIView.showCharacters(characters);
    }

    private Bitmap getBitmapFromMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

}
