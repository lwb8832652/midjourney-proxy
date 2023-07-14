package com.github.novicezk.midjourney.service.store;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.stream.StreamUtil;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.QiNiuYunUploadUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class InMemoryTaskStoreServiceImpl implements TaskStoreService {
	private final TimedCache<String, Task> taskMap;

	Lock lock = new ReentrantLock();

	public InMemoryTaskStoreServiceImpl(Duration timeout) {
		this.taskMap = CacheUtil.newTimedCache(timeout.toMillis());
	}

	@Override
	public void save(Task task) {
		// 上传图片
		if (task.getStatus() == TaskStatus.SUCCESS) {
			try {
				if (lock.tryLock(10, TimeUnit.SECONDS)) {
					// 上传图片至七牛云
					InputStream inputStream = getInputStreamFromUrl(task.getImageUrl());
					String imageUrl = QiNiuYunUploadUtils.uploadFile(inputStream, "draw/mj/" + System.currentTimeMillis() + ".png");
					if (!StringUtils.isNullOrEmpty(imageUrl)) {
						task.setImageUrl(imageUrl);
					}
					task.setImageUrl(imageUrl);
				}
			} catch (QiniuException e) {
				log.error("image upload error", e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.unlock();
			}
		}
		this.taskMap.put(task.getId(), task);

	}

	@Override
	public void delete(String key) {
		this.taskMap.remove(key);
	}

	@Override
	public Task get(String key) {
		return this.taskMap.get(key);
	}

	@Override
	public List<Task> list() {
		return ListUtil.toList(this.taskMap.iterator());
	}

	@Override
	public List<Task> list(TaskCondition condition) {
		return StreamUtil.of(this.taskMap.iterator()).filter(condition).toList();
	}

	@Override
	public Task findOne(TaskCondition condition) {
		return StreamUtil.of(this.taskMap.iterator()).filter(condition).findFirst().orElse(null);
	}

	/**
	 * 根据url下载文件流
	 * @param urlStr
	 * @return
	 */
	public static InputStream getInputStreamFromUrl(String urlStr) {
		InputStream inputStream=null;
		try {
			//url解码
			URL url = new URL(java.net.URLDecoder.decode(urlStr, "UTF-8"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//设置超时间为3秒
			conn.setConnectTimeout(3 * 1000);
			//防止屏蔽程序抓取而返回403错误
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			//得到输入流
			inputStream = conn.getInputStream();
		} catch (IOException e) {

		}
		return inputStream;
	}

}
