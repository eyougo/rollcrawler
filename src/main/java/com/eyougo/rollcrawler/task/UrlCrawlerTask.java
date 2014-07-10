package com.eyougo.rollcrawler.task;

import com.eyougo.rollcrawler.dao.UrlDao;
import com.eyougo.rollcrawler.manage.CrawlerManager;
import com.eyougo.rollcrawler.parser.UrlParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;


public class UrlCrawlerTask implements Runnable {
    private static final Log LOG = LogFactory.getLog(UrlCrawlerTask.class);
    private UrlParser urlParser;
    private UrlDao urlDao;
    private String url;
    private int rank;

    public UrlCrawlerTask(String url, int rank, UrlParser urlParser,
                          UrlDao urlDao) {
        super();
        this.urlDao = urlDao;
        this.urlParser = urlParser;
        this.url = url;
        this.rank = rank;
    }

    @Override
    public void run() {
        if (!CrawlerManager.CRAWLER_ON) {
            return;
        }
        LOG.debug("UrlCrawlerTask start, url: ====" + url);
        List<String> childUrls = urlParser.parseUrls(url);
        LOG.debug("url: ====" + url + " has children count:" + childUrls.size());

        String host = urlParser.getUrlHost(url);
        for (String childUrl : childUrls) {
            if (!CrawlerManager.CRAWLER_ON) {
                return;
            }
            if (StringUtils.equalsIgnoreCase(childUrl, url)) {
                continue;
            }
            if (urlDao.getHostCount(childUrl) >= CrawlerManager.SAME_DOMAIN_MAX) {
                continue;
            }
            boolean isValid = urlParser.isValidUrl(childUrl);
            if (!isValid) {
                continue;
            }
            LOG.debug("url: ====" + url + ", childUrl:" + childUrl + " is valid");

            // 判断childUrl是否已添加
            CrawlerManager.ADD_LOCK.lock();
            try {
                if (!urlDao.hasAdded(childUrl)) {
                    urlDao.addUrl(childUrl);
                    LOG.debug("url: ====" + url + ", add childUrl:" + childUrl);
                }
            } finally {
                CrawlerManager.ADD_LOCK.unlock();
            }
            // 判断childUrl是否已加入解析

            LOG.debug("url: ====" + url + ", childUrl:" + childUrl + " start add wait parse");
            String childHost = urlParser.getUrlHost(childUrl);
            CrawlerManager.ADD_WAIT_PARSE_LOCK.lock();
            try {
                if (urlDao.hasAddedParsed(childUrl)) {
                    continue;
                }
                if (urlDao.hasAddedWaitParse(childUrl)) {
                    continue;
                }

                int childRank = 0;
                if (StringUtils.equalsIgnoreCase(childHost, host)) {
                    childRank = this.rank + 1;
                }
                if (childRank > CrawlerManager.PARSE_RANK_MAX) {
                    continue;
                }
                Long waitParseCount = urlDao.waitParseUrlCount();
                if (waitParseCount < CrawlerManager.WAIT_PARSE_MAX) {
                    urlDao.addWaitParse(childUrl, childRank);
                    LOG.debug("url: ====" + url + ",add wait parse url:" + childUrl + ", rank:" + childRank);
                } else {
                    if (urlDao.getLastWaitParse().getRight() > childRank) {
                        urlDao.removeLastWaitParse();
                        urlDao.addWaitParse(childUrl, childRank);
                        LOG.debug("url: ====" + url + ",add wait parse url:" + childUrl + ", rank:" + childRank);
                    }
                }
            } finally {
                CrawlerManager.ADD_WAIT_PARSE_LOCK.unlock();
            }
        }

        LOG.debug("url: ====" + url + ", start add parsed");
        CrawlerManager.ADD_PARSED_LOCK.lock();
        try {
            if (!urlDao.hasAddedParsed(url)) {
                urlDao.addUrlParsed(url);
                LOG.debug("url: ====" + url + ", has add parsed");
            }
        } finally {
            CrawlerManager.ADD_PARSED_LOCK.unlock();
        }
        LOG.debug("UrlCrawlerTask end, url: ====" + url);
    }

    public String getUrl() {
        return url;
    }
}
