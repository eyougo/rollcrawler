package com.eyougo.rollcrawler.dao;

import com.eyougo.rollcrawler.manage.CrawlerManager;
import com.eyougo.rollcrawler.parser.DefaultUrlParser;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.util.Set;

/**
 * User: mei
 * Date: 5/16/14
 * Time: 00:33
 */
public class RedisUrlDao implements UrlDao{
    private static final String URLSET_KEY = "urls";
    private static final String PARSED_URLSET_KEY = "parsed_urls";
    private static final String WAITPARSE_URLSET_KEY = "waitparse_urls";
    private static final String URLCOUNT_KEY = "url_count";

    private StringRedisTemplate redisTemplate;
    @Override
    public void addUrl(final String url) {
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForSet().add(URLSET_KEY, url);
                operations.opsForHash().increment(URLCOUNT_KEY, DefaultUrlParser.getTopDomain(url), 1);
                operations.exec();
                return null;
            }
        };
        redisTemplate.execute(sessionCallback);
    }

    @Override
    public Boolean hasAdded(String url) {
        return redisTemplate.opsForSet().isMember(URLSET_KEY, url);
    }

    @Override
    public Boolean hasAddedParsed(String url) {
        return redisTemplate.opsForSet().isMember(PARSED_URLSET_KEY, url);
    }

    @Override
    public void addUrlParsed(String url) {
        redisTemplate.opsForSet().add(PARSED_URLSET_KEY, url);
    }

    @Override
    public void addWaitParse(String url, int rank) {
        redisTemplate.opsForZSet().add(WAITPARSE_URLSET_KEY, url, rank);
    }

    @Override
    public Set<String> waitParseUrls() {
        return redisTemplate.opsForZSet().range(WAITPARSE_URLSET_KEY, 0, CrawlerManager.WAIT_PARSE_MAX);
    }

    @Override
    public void removeLastWaitParse() {
        Set<String> set = redisTemplate.opsForZSet().reverseRange(WAITPARSE_URLSET_KEY, 0, 1);
        redisTemplate.opsForZSet().remove(WAITPARSE_URLSET_KEY, set.toArray());
    }

    @Override
    public void removeFirstWaitParse() {
        Set<String> set = redisTemplate.opsForZSet().range(WAITPARSE_URLSET_KEY, 0, 1);
        redisTemplate.opsForZSet().remove(WAITPARSE_URLSET_KEY, set.toArray());
    }

    @Override
    public void clearWaitParse() {
         redisTemplate.delete(WAITPARSE_URLSET_KEY);
    }

    @Override
    public Pair<String, Double> getFirstWaitParse() {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().rangeWithScores(WAITPARSE_URLSET_KEY, 0 ,1);
        for (ZSetOperations.TypedTuple<String> tuple : set) {
            Pair<String, Double> pair = new ImmutablePair<String, Double>(tuple.getValue(), tuple.getScore());
            return pair;
        }
        return null;
    }

    @Override
    public Pair<String, Double> getLastWaitParse() {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().reverseRangeWithScores(WAITPARSE_URLSET_KEY, 0, 1);
        for (ZSetOperations.TypedTuple<String> tuple : set) {
            Pair<String, Double> pair = new ImmutablePair<String, Double>(tuple.getValue(), tuple.getScore());
            return pair;
        }
        return null;
    }

    @Override
    public Boolean hasAddedWaitParse(String url) {
        return redisTemplate.opsForZSet().rank(WAITPARSE_URLSET_KEY, url) != null;
    }

    @Override
    public Long urlCount() {
        return redisTemplate.opsForSet().size(URLSET_KEY);
    }

    @Override
    public Long parsedUrlCount() {
        return redisTemplate.opsForSet().size(PARSED_URLSET_KEY);
    }

    @Override
    public Long waitParseUrlCount() {
        return redisTemplate.opsForZSet().size(WAITPARSE_URLSET_KEY);
    }

    @Override
    public long getHostCount(String url) {
        return NumberUtils.toLong(ObjectUtils.toString(redisTemplate.opsForHash().get(URLCOUNT_KEY, DefaultUrlParser.getTopDomain(url))));
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
