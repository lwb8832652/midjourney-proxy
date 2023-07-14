package com.github.novicezk.midjourney.util;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 七牛云文件上传工具类
 * @date 2023/1/6
 */
public class QiNiuYunUploadUtils {

    /** access和secret的key */
    private static final String ACCESS_KEY = "HNR57KvjZlgiPdR3-IjRG9WTQuQR2EoMB1w5C3S-";
    private static final String SECRET_KEY = "3spTSB7bcfNg8g0vkLtcmNIkAjPQFIJBMQJPlh-V";

    /** 空间名称 */
    private static final String BUCTKET_NAME = "bucket-qutu";

    /** 外链域名(这里也可以不写，在实际需求中拼接也可以) */
    private static final String DOMAIN = "https://qiniu.wiyitools.com/";

    /**
     * @desc 上传文件
     * 		文件路径切记开头不要带"/"，否则会识别为空文件夹,导致虽然上传成功，但是访问不到，
     * 			空间内也没有文件
     * @param inputStream : 文件流
     * @param folderUrl : 存储文件路径(格式为:file/img/001.jpg)
     * @return
     */
    public static String uploadFile(InputStream inputStream, String folderUrl) throws QiniuException {
        //创建上传对象,Region标识存储空间所在地域(地域填写你的空间所在地域)
        Configuration configuration = new Configuration(Region.huadong());
        UploadManager uploadManager = new UploadManager(configuration);
        //构建密钥
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        //获取token
        String token = auth.uploadToken(BUCTKET_NAME);
        //上传文件
//        uploadManager.
        Response response = uploadManager.put(inputStream, folderUrl, token, null, null);
        JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
        //里面的key就是文件上传的路径,和传来的folderUrl是一样的(可以打印整个jsonObject)
        // 官网这里是建议创建一个实体类来接收，返回的数据是json格式的，其实转json或者实体类接收
        // 都一样
        return DOMAIN + jsonObject.get("key");

    }

    public static void main(String[] args) throws IOException {
        String imageUrl = "https://qiniu.wiyitools.com/FpqhEDn-rHrFLkwF8-D9uS4-SJrJ";
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        String test123 = QiNiuYunUploadUtils.uploadFile(inputStream, "draw/mj/test_123.jpg");
        System.out.printf(test123);
    }

}

