package com.gao.jiefly.licenseplatedetection.model;

import com.gao.jiefly.licenseplatedetection.bean.CarPictureBean;
import com.gao.jiefly.licenseplatedetection.bean.LicensePlateBean;

import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public interface ILocateLicensePlateModel  {
    List<LicensePlateBean> locateLicensePlateByColor(CarPictureBean carPictureBean);
    List<LicensePlateBean> locateLicensePlateByShape(CarPictureBean carPictureBean);
}
