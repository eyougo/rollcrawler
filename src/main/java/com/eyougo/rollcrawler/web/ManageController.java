package com.eyougo.rollcrawler.web;

import com.eyougo.rollcrawler.manage.CrawlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: mei
 * Date: 5/17/14
 * Time: 00:59
 */
@Controller
@RequestMapping("/manage")
public class ManageController {
    @Autowired
    private CrawlerManager crawlerManager;

    @RequestMapping("/index")
    public String index() {
        return "index.ftl";
    }

    @RequestMapping("/start")
    public String start(@RequestParam String seedUrl) {
        crawlerManager.start(seedUrl);
        return "redirect:/manage/index";
    }

    @RequestMapping("/stop")
    public String stop() {
        crawlerManager.stop();
        return "redirect:/manage/index";
    }

    @RequestMapping("/clear")
    public String clear() {
        crawlerManager.clear();
        return "redirect:/manage/index";
    }

    @RequestMapping("/crawler")
    @ResponseBody
    public String crawler() {
        return crawlerManager.crawler();
    }

    @RequestMapping("/redis")
    @ResponseBody
    public String redis() {
        return crawlerManager.redis();
    }
}
