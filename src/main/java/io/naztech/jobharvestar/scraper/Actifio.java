package io.naztech.jobharvestar.scraper;

import org.springframework.stereotype.Service;

/**
 * Actifio URL:
 * https://www.linkedin.com/jobs/search/?f_C=399246&locationId=OTHERS.worldwide&pageNum=0&position=1
 * 
 * @author rafayet.hossain
 * @since 2019-03-11
 */
@Service
public class Actifio extends AbstractLinkedinJobs {


	@Override
	public String getSiteName() {
		return null;
	}

	@Override
	protected String getBaseUrl() {
		return null;
	}
}
