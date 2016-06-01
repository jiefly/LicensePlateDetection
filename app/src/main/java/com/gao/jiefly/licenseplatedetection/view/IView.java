package com.gao.jiefly.licenseplatedetection.view;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public interface IView {
    void showLicensePlate(List<Bitmap> bitmaps);
    void showCharacters(List<Bitmap> characters);
}
