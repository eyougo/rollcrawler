package com.eyougo.rollcrawler.dao;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

/**
 * User: mei
 * Date: 5/16/14
 * Time: 00:32
 */
public interface UrlDao {

    void addUrl(String url);

    Boolean hasAdded(String url);

    Boolean hasAddedParsed(String url);

    void addUrlParsed(String url);

    void addWaitParse(String url, int rank);

    Set<String> waitParseUrls();

    void removeFirstWaitParse();

    void clearWaitParse();

    Pair<String,Double> getFirstWaitParse();

    Boolean hasAddedWaitParse(String url);

    Long urlCount();

    Long parsedUrlCount();

    Long waitParseUrlCount();

    long getHostCount(String url);
}
