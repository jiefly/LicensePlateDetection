package com.gao.jiefly.licenseplatedetection.model;

import com.gao.jiefly.licenseplatedetection.bean.CharacterBean;

import java.util.List;

/**
 * Created by jiefly on 2016/6/1.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public interface IcharactersSegmentationModel {
    List<CharacterBean> getCharacter(int threshold,int num);
}
