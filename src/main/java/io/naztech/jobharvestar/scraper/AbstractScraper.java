package io.naztech.jobharvestar.scraper;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import io.naztech.jobharvestar.crawler.Scrapper;
import io.naztech.jobharvestar.utils.RandomUserAgent;
import io.naztech.jobharvestar.utils.WordToNumbers;

/**
 * Provides common functionality to web site {@link Scrapper} implementations.
 * 
 * @author Imtiaz Rahi
 * @author Mahmud Rana
 * @author Tanbirul Hashan
 * @since 2019-01-13
 */
public abstract class AbstractScraper implements Scrapper {
	private final Logger log = LoggerFactory.getLogger(getClass());
	protected static final int USER_ID = 100001;
	/** Time units in seconds. Values are defined in mili seconds */
	protected static final int TIME_1S = 1000, TIME_4S = 4000, TIME_5S = 5000, TIME_10S = 10 * TIME_1S;
	protected static final int TIME_1M = 60 * TIME_1S;
	/**
	 * Flag to denote whether scraper thread should be running or not. Set
	 * <code>false</code> by external force.
	 */
	private boolean runIt = false;
	/** Job parse count */
	private int jobCount = 0;
	

	@Value("${naztech.webscrapper.job-list-buffer:10}")
	private int jobListBufferSize;
	/** Whether job tagging (skill and location) is enabled in configuration */
	@Value("${naztech.webscrapper.enable-tagging:false}")
	private boolean isTaggingEnabled;
	@Value("${selenium.webdriver.chrome-driver:webdrivers/chromedriver.exe}")
	protected String chromeExePath;

	

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.warn("Hostname or IP was not found", e);
		}
		return "?";
	}

	
	/**
	 * Returns page count from total job count and jobs per page.
	 * 
	 * @param jobCount   Job count as string
	 * @param jobPerPage Number of jobs per list page
	 * @return Page count
	 */
	public int getPageCount(String jobCount, int jobPerPage) {
		if (StringUtils.isBlank(jobCount)) return 0;
		try {
			int totalJobs = Integer.parseInt(jobCount);
			int totalPage = totalJobs / jobPerPage;
			return totalJobs % jobPerPage > 0 ? ++totalPage : totalPage;
		} catch (NumberFormatException e) {
			log.warn("{} failed to parse job count from value {}", getSiteName(), jobCount);
		}
		return 0;
	}

	
	/**
	 * Returns {@link LocalDate} instance from the given date formats.
	 * 
	 * @param val     String value of date
	 * @param formats Expected {@link DateTimeFormatter} formats
	 * @return {@link LocalDate} instance
	 */
	protected LocalDate parseDate(String val, DateTimeFormatter... formats) {
		for (DateTimeFormatter fmt : formats) {
			LocalDate ob = parseDate(val, fmt);
			if (ob != null) return ob;
		}
		log.warn(getSiteName() + " failed to parse date " + val);
		return null;
	}

	private LocalDate parseDate(String val, DateTimeFormatter df) {
		try {
			return LocalDate.parse(val, df);
		} catch (DateTimeParseException e) {
			// NOTE: do not put log here. intentionally left blank
		}
		return null;
	}

	/**
	 * Returns {@link LocalDate} instance from string value. <br>
	 * e.g.
	 * 
	 * <pre>
	 * "one day ago", "1 day ago", "Two days ago", "two day ago", "2 days ago", "Seven days ago",
	 * "one month ago", "1 month ago", "Two months ago", "two month ago", "2 months ago", "Seven months ago"
	 * </pre>
	 * 
	 * @param val String to convert
	 * @return {@link LocalDate} instance
	 */
	protected LocalDate parseAgoDates(String val) {
		if (StringUtils.isBlank(val)) return null;
		val = val.toLowerCase();

		LocalDate now = LocalDate.now();
		if ("today".equals(val)) return now;
		if ("yesterday".equals(val)) return now.minusDays(1);

		val = val.replace("ago", "").trim();
		String[] parts = val.split(" ");
		parts[0] = parts[0].replace("+", "");

		if ("hours".contains(parts[1])) return now;
		Long number = WordToNumbers.getNumber(parts[0]);
		if ("days".contains(parts[1])) return now.minusDays(number);
		if ("weeks".contains(parts[1])) return now.minusWeeks(number);
		if ("months".contains(parts[1])) return now.minusMonths(number);
		if ("years".contains(parts[1])) return now.minusYears(number);
		return null;
	}

	protected String getMonthTitleCase(String pattern, String val) {
		if (pattern == null || pattern.isEmpty()) return null;
		Pattern pt = Pattern.compile(pattern);
		Matcher mt = pt.matcher(val);
		mt.find();
		return mt.replaceFirst(mt.group().toUpperCase());
	}

	

	public boolean isStopped() {
		return !runIt;
	}

	@Override
	public void stopIt() {
		if (log.isTraceEnabled()) log.trace("Received stop signal");
		this.runIt = false;
	}

	/**
	 * Returns selenium {@link WebDriver} instance of Google Chrome browser.
	 * 
	 * @param Path of Google Chrome web driver binary
	 * @return Google Chrome {@link WebDriver} instance
	 */
	protected ChromeDriver getChromeDriver() {
		ChromeDriverService service = new ChromeDriverService.Builder()
				.usingDriverExecutable(new File(chromeExePath)).usingAnyFreePort().build();
		
		
		
		ChromeOptions opts = new ChromeOptions().addArguments("--no-sandbox")
				.addArguments("--disable-dev-shm-usage", "--disable-extensions", "disable-infobars")
				.addArguments(RandomUserAgent.getRandomUserAgent())
				.setHeadless(false).setExperimentalOption("useAutomationExtension", false);
		
		ChromeDriver driver = new ChromeDriver(service, opts);
		/* Developer should increase page load timeout in their scraper class when needed */
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
		return driver;
	}
	
	/**
	 * Returns htmlUnit {@link WebClient} instance of Firefox and Google Chrome browser.
	 * 
	 * @return Firefox {@link WebClient} instance
	 * 
	 * @return Google Chrome {@link WebClient} instance
	 */	
	protected WebClient getRandomClient() {
		List<WebClient> clientList = getClientList();
		return clientList.get(RandomUtils.nextInt(0, clientList.size()));
	}
	
	protected List<WebClient> getClientList() {
		List<WebClient> clientList = new ArrayList<>();
		clientList.add(getChromeClient());
		clientList.add(getFirefoxClient());		
		return clientList;
	}

	/**
	 * Returns a htmlunit {@link WebClient} instance of Firefox browser.
	 * 
	 * @return Firefox {@link WebClient} instance
	 */
	protected WebClient getFirefoxClient() {
		WebClient ob = new WebClient(BrowserVersion.FIREFOX_52);
		setWebClientOptions(ob.getOptions());
		setWebClientPreferences(ob);
		return ob;
	}

	/**
	 * Returns a htmlunit {@link WebClient} instance of Google Chrome browser.
	 * 
	 * @return Google Chrome {@link WebClient} instance
	 */
	protected WebClient getChromeClient() {
		WebClient ob = new WebClient(BrowserVersion.CHROME);
		setWebClientOptions(ob.getOptions());
		setWebClientPreferences(ob);
		return ob;
	}

	private void setWebClientPreferences(WebClient ob) {
		ob.waitForBackgroundJavaScript(10000);
		ob.setJavaScriptTimeout(15000);

		ob.setAjaxController(new NicelyResynchronizingAjaxController());
		ob.getCookieManager().setCookiesEnabled(true);
	}

	private void setWebClientOptions(WebClientOptions opts) {
		opts.setDoNotTrackEnabled(true);
		opts.setThrowExceptionOnScriptError(false);
		opts.setThrowExceptionOnFailingStatusCode(false);
		opts.setTimeout(30000);
		opts.setUseInsecureSSL(true);
	}

	/**
	 * Returns the text of a PDF document after parsing it.
	 * 
	 * @param url URL of PDF document
	 * @return Text of whole PDF document
	 */
	protected String getTextFromPdf(String url) {
		PdfReader reader = null;
		try {
			// TODO check whether url need to encode or not
			if (!urlEncoded(url)) url = URLEncoder.encode(url, "UTF-8");
			reader = new PdfReader(new URL(url));
			StringBuffer sb = new StringBuffer();
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
			for (int i = 1; i <= reader.getNumberOfPages(); i++) sb.append(parser.processContent(i, strategy).getResultantText());
			return sb.toString();
		} catch (MalformedURLException e) {
			log.warn("Failed to load PDF " + url, e);
		} catch (IOException e) {
			log.warn("Failed to parse PDF " + url, e);
		} finally {
			if (reader != null) reader.close();
		}
		return url;
	}

	private boolean urlEncoded(String url) {
		return !url.contains(" ");
	}

	/**
	 * Increment the total job parsed counter and returns the incremented value.
	 * 
	 * @return Total job count
	 */
	private int incrementJobCount() {
		return ++jobCount;
	}

	/**
	 * Returns total number of job objects processed (parsed) so far.
	 * 
	 * @return Number of jobs processed
	 */
	public int getJobCount() {
		return jobCount;
	}

	/**
	 * Returns the job site short name.
	 * 
	 * @return Site short name;
	 */
	@Override
	public abstract String getSiteName();

	/**
	 * Returns the host name or the base URL of the job site. <br>
	 * Set this from the site URL returned from {@link SiteMetaData#getUrl()}.
	 * 
	 * <pre>
	 * URL from config: https://jobs.jpmorganchase.com/ListJobs/All
	 * Site host should be: https://jobs.jpmorganchase.com
	 * </pre>
	 * 
	 * @return Base URL
	 */
	protected abstract String getBaseUrl();

}
