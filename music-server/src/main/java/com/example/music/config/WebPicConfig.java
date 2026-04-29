package com.example.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
/**
 * 集中一下图像的配置类
 **/
@Configuration
public class WebPicConfig implements WebMvcConfigurer {

    //TODO 这个配置类的目的是什么  就是注册了一个类似于拦截器吧  看到对应的资源 会将其修改成相应的地址
    @Value("${local.file.storage-path}") // 确保application.properties中有这个配置
    private String storagePath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = storagePath.endsWith("/") ? storagePath : storagePath + "/";

        //用户头像
        registry.addResourceHandler("/img/avatorImages/**")
                .addResourceLocations("file:" + basePath + "/img/avatorImages/");

        //歌手图片
        registry.addResourceHandler("img/singerPic/**")
                .addResourceLocations("file:" + basePath + "img/singerPic/");

        //歌曲图片
        registry.addResourceHandler("img/songPic/**")
                .addResourceLocations("file:" + basePath + "img/songPic/");

        //歌单图片
        registry.addResourceHandler("img/songlist/**")
                //.addResourceLocations("file:" + basePath + "img/songListPic/");
                .addResourceLocations("file:" + basePath + "/img/songListPic/");

        //主页轮播图
        registry.addResourceHandler("img/swiper/**")
                .addResourceLocations("file:" + basePath + "img/swiper/");

        //歌手图片旧版
        registry.addResourceHandler("/singer/img/**")
                .addResourceLocations("file:" + basePath + "singer/img/");

        //歌曲            重启编译器生效
        registry.addResourceHandler("/song/**")
                .addResourceLocations("file:" + basePath + "/song/");

//        registry.addResourceHandler("song/singer/")
//                .addResourceLocations("file:" + basePath + "song/**");


/* 原版
        registry.addResourceHandler("/img/avatorImages/**")
                .addResourceLocations(Constants.AVATOR_IMAGES_PATH);
        registry.addResourceHandler("/img/singerPic/**")
                .addResourceLocations(Constants.SINGER_PIC_PATH);
        registry.addResourceHandler("/img/songPic/**")
                .addResourceLocations(Constants.SONG_PIC_PATH);
        registry.addResourceHandler("/song/**")
                .addResourceLocations(Constants.SONG_PATH);
        registry.addResourceHandler("/img/songListPic/**")
                .addResourceLocations(Constants.SONGLIST_PIC_PATH);
        registry.addResourceHandler("/img/swiper/**")
                .addResourceLocations(Constants.BANNER_PIC_PATH);
*/
    }
}
