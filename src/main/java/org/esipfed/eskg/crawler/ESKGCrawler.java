/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esipfed.eskg.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.any23.plugin.crawler.SiteCrawler;

/**
 * Primary interface for implementing basic site crawler's 
 * to extract semantic content of small/medium size sites.
 */
public class ESKGCrawler {

  private static SiteCrawler crawler;

  /**
   * Default constructor.
   */
  public ESKGCrawler() {
    try {
      crawler = new SiteCrawler(File.createTempFile("eskg_crawl", ""));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // We should use Commons CLI here and implement
    // a CLI which shadows that provided in Any23
    // https://github.com/apache/any23/blob/master/plugins/basic-crawler/src/main/java/org/apache/any23/cli/Crawler.java#L51-L75

    //Once the CommonsCLI logic is in place, we can 
    // progress with populating the crawler object 
    // configuration as follows
    try {
      crawler = new SiteCrawler(File.createTempFile("eskg_crawl", ""));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //crawler.setMaxDepth(maxDepth);
    //crawler.setMaxPages(maxPages);
    //crawler.setNumOfCrawlers(n);
    //crawler.setPolitenessDelay(millis);
    crawler.setWebCrawler(SiteCrawler.DEFAULT_WEB_CRAWLER);
    
    //Once you build Any23 locally documentation on starting and stopping a crawl 
    // is available at
    // any23/target/site/apidocs/index.html?org/apache/any23/plugin/crawler/SiteCrawler.html
  }

}
