package com.eyougo.rollcrawler.manage;

import com.eyougo.rollcrawler.dao.UrlDao;
import com.eyougo.rollcrawler.parser.UrlParser;
import com.eyougo.rollcrawler.task.UrlCrawlerTask;
import com.eyougo.rollcrawler.task.UrlHostCrawlerTask;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: mei
 * Date: 5/17/14
 * Time: 00:46
 */
public class CrawlerManager {
    private static final Log LOG = LogFactory.getLog(CrawlerManager.class);

    public static final Lock ADD_LOCK = new ReentrantLock();

    public static final Lock ADD_WAIT_PARSE_LOCK = new ReentrantLock();

    public static final Lock ADD_PARSED_LOCK = new ReentrantLock();

    public static final int WAIT_PARSE_MAX = 1000;

    public static final int PARSE_RANK_MAX = 2;

    public static final int SAME_DOMAIN_MAX = 5;

    public static volatile boolean CRAWLER_ON = true;

    private UrlParser urlParser;
    private UrlDao urlDao;
    private ThreadPoolTaskExecutor urlCrawlerTaskExecutor;

    private Thread crawlerThread;

    public void init() {

    }

    public void start(String seedUrl) {
        crawlerThread = new CrawlerHostThread();
        crawlerThread.setDaemon(true);
        CRAWLER_ON = true;
        urlDao.addWaitParse(seedUrl, 0);
        crawlerThread.start();
    }

    public void stop() {
        CRAWLER_ON = false;
    }

    public void clear() {
        urlDao.clearWaitParse();
    }

    public String redis() {
        return "URL count:" + urlDao.urlCount() + ", PARSED URL count:" + urlDao.parsedUrlCount()
                + ", WAIT PARSE URL count:" + urlDao.waitParseUrlCount()
                + ",<br/><br/> WAIT PARSE URL set:" + urlDao.waitParseUrls();
    }

    public String crawler() {
        int activeCount = urlCrawlerTaskExecutor.getActiveCount();
        int poolSize = urlCrawlerTaskExecutor.getPoolSize();
        ThreadPoolExecutor executor = urlCrawlerTaskExecutor.getThreadPoolExecutor();

        long taskCount = executor.getTaskCount();
        long completedCount = executor.getCompletedTaskCount();
        int largestPoolSize = executor.getLargestPoolSize();
        return "crawler task executor <br/> active count:" + activeCount + " <br> pool size:" + poolSize
                + " <br> taskCount:" + taskCount + " <br> completeCount:" + completedCount + " <br> largetPoolSize:" + largestPoolSize;
    }

    private class CrawlerThread extends Thread {
        @Override
        public void run() {
            while (CRAWLER_ON) {
                Pair<String, Double> url = urlDao.getFirstWaitParse();
                if (url == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                } else {
                    UrlCrawlerTask crawlerTask = new UrlCrawlerTask(url.getLeft(),
                            url.getRight().intValue(),
                            urlParser, urlDao);
                    try {
                        urlCrawlerTaskExecutor.execute(crawlerTask);
                        urlDao.removeFirstWaitParse();
                    } catch (RejectedExecutionException e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {

                        }
                    }
                }
            }
        }
    }

    private class CrawlerHostThread extends Thread {
        @Override
        public void run() {
            while (CRAWLER_ON) {
                try {
                    Pair<String, Double> url = urlDao.getFirstWaitParse();
                    if (url == null) {
                        Thread.sleep(1000);
                    } else {
                        UrlHostCrawlerTask crawlerTask = new UrlHostCrawlerTask(url.getLeft(),
                                url.getRight().intValue(),
                                urlParser, urlDao);
                        try {
                            urlCrawlerTaskExecutor.execute(crawlerTask);
                            urlDao.removeFirstWaitParse();
                        } catch (RejectedExecutionException e) {
                            Thread.sleep(1000);
                        }
                    }
                }catch (Exception e){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
    }

    public void setUrlParser(UrlParser urlParser) {
        this.urlParser = urlParser;
    }

    public void setUrlDao(UrlDao urlDao) {
        this.urlDao = urlDao;
    }

    public void setUrlCrawlerTaskExecutor(ThreadPoolTaskExecutor urlCrawlerTaskExecutor) {
        this.urlCrawlerTaskExecutor = urlCrawlerTaskExecutor;
    }
}
