package io.naztech.jobharvestar.scraper;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.naztech.jobharvestar.crawler.Scrapper;

/**
 * All job sites of Indeed parsing class.
 * 
 *  Extended Class'es:
 * <ul>
 * <li><a href="https://ca.indeed.com/Igm-Financial-jobs">Igm Financial</a> 
 * <li><a href="https://www.indeed.com/q-Keycorp-jobs.html">Key Corp</a> 
 * <li><a href="https://www.indeed.com/q-Japan-Exchange-jobs.html">Japan Exchange Group</a> 
 * <li><a href="https://www.indeed.com/jobs?q=Anchorage%20Capital&vjk=8b7d3310ee3fccfe">Anchorage Capital Group</a> 
 * <li><a href="https://www.indeed.com/jobs?q=Discovery+Capital+Management&vjk=61d674ad3390cbe5">Discovery Capital Management</a> 
 * <li><a href="https://www.indeed.com/q-Pacific-Investment-Management-Company-jobs.html">Pacific Investment Management</a> 
 * <li><a href="https://www.indeed.com/jobs?q=Medlink&vjk=d63cfe45c4f348a8#">Medilinker</a>
 * <li><a href="https://www.indeed.com/cmp/Hippo-Insurance/jobs">Hippo Insurance</a>
 * <li><a href="https://www.indeed.com/jobs?q=JD+Finance&l=">JdFinance</a>
 * <li><a href="https://www.indeed.com/q-Ofo-jobs.html">Ofo</a>
 *</ul>
 * @author Tanbirul Hashan
 * @since 2019-02-26
 */
@Service
public abstract class AbstractIndeed extends AbstractScraper implements Scrapper {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	private static final String JOB_LOC_PATH = "//div[@class='jobsearch-InlineCompanyRating icl-u-xs-mt--xs  jobsearch-DesktopStickyContainer-companyrating']/div";
	private static final String APPLY_PATH = "//div[@class='icl-u-lg-hide']/a";
	protected WebClient webClient = null;

	@Override
	public void scrapJobs() {
		System.out.println("RUNEED>>>>>>>>>>>>>>>>>>>>>");
		webClient = getFirefoxClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
	}

	
	public void getScrapedJobs() throws FailingHttpStatusCodeException, IOException, InterruptedException {
//		HtmlPage page = webClient.getPage(siteMeta.getUrl());
//		Thread.sleep(2000);
//		while (page != null) {
//			if (isStopped()) throw new PageScrapingInterruptedException();
//			List<HtmlAnchor> anchorList = page.getByXPath(getJobsPath());
//			Collection<HtmlElement> dateList = page.getByXPath(getDatespath());
//			Iterator<HtmlElement> postedDates = dateList.iterator();
//			for (HtmlAnchor htmlAnchor : anchorList) {
//				String jobUrl = htmlAnchor.getHrefAttribute();
//				Job job = new Job(getBaseUrl()+ jobUrl);
//				job.setTitle(htmlAnchor.asText().trim());
//				job.setName(job.getTitle());
//				job.setPostedDate(parseAgoDates(postedDates.next().asText()));
//				saveJob(getJobDetails(job), siteMeta);
//			}
//			page = getNextPage(page);
//		}
	}

	protected HtmlPage getNextPage(HtmlPage page) throws InterruptedException, IOException {
		List<HtmlAnchor> paginationAnchrList = page.getByXPath("//div[@class='pagination']/a");
		int len = paginationAnchrList.size();
		if (!paginationAnchrList.get(len - 1).getByXPath("span/span[@class='np']").isEmpty()) {
			page = paginationAnchrList.get(len - 1).click();
			Thread.sleep(TIME_1S);
			return page;
		} else log.info("Next page traversal ended");
		return null;
	}

	

	protected String getDatespath() {
		return "//span[@class='date']";
	}

	protected String getJobsPath() {
		return "//div[@class='title']/a";
	}

}
