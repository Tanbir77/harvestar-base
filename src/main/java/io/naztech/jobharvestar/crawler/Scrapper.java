package io.naztech.jobharvestar.crawler;

import org.springframework.scheduling.annotation.Async;

/**
 * Web site scrapper launch interface.
 * 
 * @author Imtiaz Rahi
 * @since 2019-01-13
 */
public interface Scrapper {
	/** Scrap job information from career site after reading {@link SiteMetaData} */
	@Async
	void scrapJobs();;

	String getSiteName();

	void stopIt();
}
