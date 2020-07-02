package io.naztech.jobharvestar.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for the controller classes which start, stops the scraper classes.
 *  
 * @author Imtiaz Rahi
 * @since 2019-02-13
 */
public class AbstractScraperLauncher {
	private static final int STATION_CAPACITY = getStationCapacity();

	private static CountDownLatch latch;
	private static AtomicInteger counter = new AtomicInteger(0);

	/** Maintain list of currently running scraper instances */
	protected static Map<String, Scrapper> runningScrapers = new HashMap<>();

	@Autowired protected Map<String, Scrapper> scrappers;

	protected void checkCapacityBeforeStart() throws InterruptedException {
		counter.getAndIncrement();
		/* counter is reduced by scraper class upon exit */
		if (counter.get() > STATION_CAPACITY) {
			latch = new CountDownLatch(1);
			latch.await();
		}
	}

	/**
	 * Decrements the count of total scraper received from queue.<br>
	 * Specially needed by scraper sclass upon finished.
	 */
	public static void decrementCount() {
		counter.getAndDecrement();
		if (latch != null) latch.countDown();
	}

	private static int getStationCapacity() {
		Runtime currentRun = Runtime.getRuntime();
		/* Leave one processor thread free for OS */
		int processor = currentRun.availableProcessors() - 1;
		/* Memory available in mega bytes */
		int memory = (int) (currentRun.maxMemory() / 1024) / 1024;
		// TODO how much memory needed for a scraper class run. check with jconsole.
		int ramNeed = 512;
		int capacity = memory / ramNeed;
		return capacity < processor ? capacity : processor;
	}

	

	public int getCapacity() {
		return STATION_CAPACITY;
	}
}
