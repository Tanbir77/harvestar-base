package io.naztech.jobharvestar.crawler;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Start scraping site as scheduled in the background. <br>
 * Add {@code dev} in spring.profiles.active to use this feature.
 * 
 * @author Imtiaz Rahi
 * @since 2019-01-16
 */
@Component
@Profile("!prod")
public class ScrapInSchedule extends AbstractScraperLauncher {

	@Value("${naztech.webscrapper.enabledScrappers:}")
	private String[] enabledScrappers;

	@Value("${naztech.webscrapper.disableScrappers:}")
	private String[] disabledScrappers;

	/** Launch active scrapers parallelly */
	@Scheduled(fixedRate = 45632000, initialDelay = 500)
	public void launchScrappers() {
		

		List<String> enabled = Arrays.asList(enabledScrappers);
		List<String> disabled = Arrays.asList(disabledScrappers);
		if (enabled.isEmpty() && disabled.isEmpty()) return;
		/* Run only the enabled ones or which are not disabled */
		Predicate<Entry<String, Scrapper>> isActive = it -> {
			return enabled.isEmpty() ? !disabled.contains(it.getKey()) : enabled.contains(it.getKey());
		};

		scrappers.entrySet().stream().filter(isActive).forEach(it -> {
			synchronized (runningScrapers) { runningScrapers.put(it.getKey(), it.getValue()); }
			it.getValue().scrapJobs();
		});
	}

}
