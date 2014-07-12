package com.eyougo.rollcrawler.task;

import com.eyougo.rollcrawler.dao.UrlDao;
import com.eyougo.rollcrawler.manage.CrawlerManager;
import com.eyougo.rollcrawler.parser.DefaultUrlParser;
import com.eyougo.rollcrawler.parser.UrlParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * User: mei
 * Date: 7/13/14
 * Time: 01:07
 */
public class UrlHostCrawlerTask extends UrlCrawlerTask{
    private static final Log LOG = LogFactory.getLog(UrlHostCrawlerTask.class);
    public UrlHostCrawlerTask(String url, int rank, UrlParser urlParser, UrlDao urlDao) {
        super(url, rank, urlParser, urlDao);
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
        String domain = DefaultUrlParser.getTopDomain(url);

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

            // 判断childUrlHost是否已添加
            CrawlerManager.ADD_LOCK.lock();
            try {
                if (!urlDao.hasAdded(host) && urlParser.isValidUrl(host)) {
                    urlDao.addUrl(host);
                    LOG.debug("url: ====" + url + ", add host:" + host);
                }
            } finally {
                CrawlerManager.ADD_LOCK.unlock();
            }
            boolean isValid = urlParser.isValidUrl(childUrl);
            if (!isValid) {
                continue;
            }
            LOG.debug("url: ====" + url + ", childUrl:" + childUrl + " is valid");

            // 判断childUrl是否已加入解析

            LOG.debug("url: ====" + url + ", childUrl:" + childUrl + " start add wait parse");
            String childDomain = DefaultUrlParser.getTopDomain(childUrl);
            CrawlerManager.ADD_WAIT_PARSE_LOCK.lock();
            try {
                if (urlDao.hasAddedParsed(childUrl)) {
                    continue;
                }
                if (urlDao.hasAddedWaitParse(childUrl)) {
                    continue;
                }

                int childRank = 0;
                if (StringUtils.equalsIgnoreCase(childDomain, domain)) {
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
}
