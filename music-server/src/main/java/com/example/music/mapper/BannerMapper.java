package com.example.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.music.model.domain.Banner;
import org.apache.poi.ss.formula.functions.T;

/**
* @author asus
* @description 针对表【banner】的数据库操作Mapper
* @Entity generator.domain.Banner
*/
public interface BannerMapper extends BaseMapper<Banner> {


    //int insert();
    int insert(T entity);
}
