/*
 * Copyright (c) 2021 sreenathofficial.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sreenathofficial.webcrawler.api.service;

import com.sreenathofficial.webcrawler.api.model.dto.CrawlMetadata;
import com.sreenathofficial.webcrawler.api.model.dto.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Profile("default")
public class HtmlWebCrawler implements WebCrawler {

    Logger logger = LoggerFactory.getLogger(HtmlWebCrawler.class);

    @Autowired
    private HtmlLinkFinder linkFinder;

    @Autowired
    private LinkDomainMatcher linkDomainMatcher;

    /**
     * Method used to initiate crawling of a single URL
     * @param url
     * Wraps the url in Link object. Link object is the data structure used to store the sitemap
     * Initialises new Set object to hold crawled urls temporarily so that no repeat happens
     * Calls the crawl method which implements the crawl logic
     * @return CrawlMetadata
     */
    @Override
    public CrawlMetadata crawl(final String url) {
        final CrawlMetadata crawlMetadata = new CrawlMetadata(new Link(url), new HashSet<>());
        crawl(crawlMetadata.getRootLink(), crawlMetadata);
        return crawlMetadata;
    }

    /**
     * Receives Link object and CrawlMetadata object as parameters
     * @param link used to store url and urls in the web page pointed to by the link.url
     * @param crawlMetadata used to store discovered urls temporarily
     * Checks if url already crawled by searching urls stored in crawlMetadata.discoveredUrls
     * If yes returns, if not stores the url in crawlMetadata.discoveredUrls
     * Finds the links in the web page returned by link.url.
     * Inserts the links found from the link.url in link.childLinks
     * Calls the method recursively on discovered link.childLinks
     */
    private void crawl(Link link, CrawlMetadata crawlMetadata) {

        try {

            logger.info("Crawling url: {}", link.getUrl());

            final Set<String> discoveredUrls = crawlMetadata.getDiscoveredUrls();

            if(discoveredUrls.contains(link.getUrl().toLowerCase())){
                logger.info("Already crawled url: {}", link.getUrl());
                return;
            }else{
                discoveredUrls.add(link.getUrl().toLowerCase());
            }

            if(linkDomainMatcher.match(crawlMetadata.getRootLink(), link)) {
                link.setChildLinks(linkFinder.findChildLinks(link));
            }else{
                logger.info("url: {}, does not belong to root domain", link.getUrl());
                return;
            }

            for(Link childLink: link.getChildLinks()){
                if(!discoveredUrls.contains(childLink.getUrl().toLowerCase())) {
                    crawl(childLink, crawlMetadata);
                }
            }

        } catch(Exception e){
            logger.error(e.getMessage(), e);
        }

    }

}
