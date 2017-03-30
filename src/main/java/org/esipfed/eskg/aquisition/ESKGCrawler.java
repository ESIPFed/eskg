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
package org.esipfed.eskg.aquisition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.any23.plugin.crawler.CrawlerListener;
import org.apache.any23.plugin.crawler.SiteCrawler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;

/**
 * Primary interface for implementing basic site crawler's to extract semantic
 * content of small/medium size sites.
 */
public class ESKGCrawler {

  private static final Logger LOG = LoggerFactory.getLogger(ESKGCrawler.class);

  private static final String SEED_OPT = "seedUrl";
  private static final String FILTER_OPT = "pageFilter";
  private static final String STORAGE_OPT = "storageFolder";
  private static final String CRAWLER_OPT = "numCrawlers";
  private static final String PAGES_OPT = "maxPages";
  private static final String DEPTH_OPT = "maxDepth";
  private static final String POLITE_OPT = "politenessDelay";

  private static SiteCrawler crawler;

  private static Pattern pageFilter = Pattern.compile(SiteCrawler.DEFAULT_PAGE_FILTER_RE);

  private static File storageFolder = new File(System.getProperty("java.io.tmpdir"), "crawler-metadata-" + UUID.randomUUID().toString());

  private static int numCrawlers = SiteCrawler.DEFAULT_NUM_OF_CRAWLERS;

  private static int maxPages = Integer.MAX_VALUE;

  private static int maxDepth = Integer.MAX_VALUE;

  private static int politenessDelay = Integer.MAX_VALUE;

  private static URL seedUrl;

  /**
   * Default constructor.
   */
  private ESKGCrawler() {
    try {
      crawler = new SiteCrawler(File.createTempFile("eskg_crawl", ""));
    } catch (IOException e) {
      LOG.error("Error whilst creating ESKG site crawler: {}.", e);
    }
  }

  private static void crawl(SiteCrawler crawler) throws InterruptedException {
    final Set<String> distinctPages = new HashSet<>();
    crawler.addListener(new CrawlerListener() {
      @Override
      public void visitedPage(Page page) {
        distinctPages.add( page.getWebURL().getURL() );
        Iterator<String> it = distinctPages.iterator();
        while (it.hasNext()) {
          LOG.info("Fetching page - " + it.next());
        }
      }
    });
    try {
      crawler.start(seedUrl, pageFilter, false);
    } catch (Exception e) {
      LOG.error("Error whilst starting crawl. {}", e);
    }
    synchronized (ESKGCrawler.class) {
      try {
        ESKGCrawler.class.wait(15 * 1000);
      } catch (InterruptedException e) {
        LOG.error("Crawler has been interrupted: {}", e);
        throw new InterruptedException();
      }
    }
    crawler.stop();

    LOG.info("Distinct pages: " + distinctPages.size());

  }

  /**
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {

    Option sOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("seed").required(true).longOpt(SEED_OPT)
        .desc("An individual seed URL used to bootstrap the crawl.").build();

    Option pfOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("filter").required(false).longOpt(FILTER_OPT)
        .desc("Regex used to filter out page URLs during crawling.").build();

    Option sfOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("storage").required(false).longOpt(STORAGE_OPT)
        .desc("Folder used to store crawler temporary data.").build();

    Option ncOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("nCrawlers").required(false).longOpt(CRAWLER_OPT)
        .desc("Sets the number of crawlers.").build();

    Option mpOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("mPages").required(false).longOpt(PAGES_OPT)
        .desc("Max number of pages before interrupting crawl.").build();

    Option mdOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("mDepth").required(false).longOpt(DEPTH_OPT)
        .desc("Max allowed crawler depth.").build();

    Option pdOpt = Option.builder().hasArg(true).numberOfArgs(1)
        .argName("pDelay").required(false).longOpt(POLITE_OPT)
        .desc("Politeness delay in milliseconds.").build();

    Options opts = new Options();
    opts.addOption(sOpt).addOption(pfOpt).addOption(sfOpt).addOption(ncOpt).addOption(mpOpt)
    .addOption(mdOpt).addOption(pdOpt);

    DefaultParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(opts, args);
    } catch (ParseException e) {
      LOG.error("Failed to parse command line {}", e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(ESKGCrawler.class.getSimpleName(), opts);
      System.exit(-1);
    }

    if (cmd.hasOption(SEED_OPT)) {
      try {
        seedUrl = new URI(cmd.getOptionValue(SEED_OPT)).toURL();
      } catch (MalformedURLException | URISyntaxException e) {
        LOG.error("Error whilst creating seed URL. {}", e);
      }
    }
    if (cmd.hasOption(FILTER_OPT)) {
      pageFilter = Pattern.compile(cmd.getOptionValue(FILTER_OPT));
    }
    if (cmd.hasOption(STORAGE_OPT)) {
      try {
        crawler = new SiteCrawler(File.createTempFile(cmd.getOptionValue(STORAGE_OPT), ""));
      } catch (IOException e) {
        LOG.error("Error whilst creating ESKG site crawler: {}.", e);
      }
    } else {
      crawler = new SiteCrawler(storageFolder);
    }
    if (cmd.hasOption(CRAWLER_OPT)) {
      numCrawlers = Integer.parseInt(cmd.getOptionValue(CRAWLER_OPT));
    }
    if (cmd.hasOption(PAGES_OPT)) {
      maxPages = Integer.parseInt(cmd.getOptionValue(PAGES_OPT));
    }
    if (cmd.hasOption(DEPTH_OPT)) {
      maxDepth = Integer.parseInt(cmd.getOptionValue(DEPTH_OPT));
    }
    if (cmd.hasOption(POLITE_OPT)) {
      politenessDelay = Integer.parseInt(cmd.getOptionValue(POLITE_OPT));
    }

    crawler.setMaxDepth(maxDepth);
    LOG.info("Setting max depth to: {}", maxDepth);
    crawler.setMaxPages(maxPages);
    LOG.info("Setting max pages to: {}", maxPages);
    crawler.setNumOfCrawlers(numCrawlers);
    LOG.info("Setting number of crawlers to: {}", numCrawlers);
    crawler.setPolitenessDelay(politenessDelay);
    LOG.info("Setting crawler politeness to: {}", politenessDelay);
    crawler.setWebCrawler(SiteCrawler.DEFAULT_WEB_CRAWLER);

    crawl(crawler);
  }
}
