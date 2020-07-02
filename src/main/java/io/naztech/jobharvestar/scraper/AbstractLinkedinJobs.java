package io.naztech.jobharvestar.scraper;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.naztech.jobharvestar.crawler.Scrapper;
import io.naztech.jobharvestar.model.PropertyDetails;
import io.naztech.jobharvestar.repo.PropertyDetailsRepo;
import io.naztech.jobharvestar.repo.UrlRepo;
import lombok.NonNull;

/**
 * 
 * @author Tanbirul Hashan
 * @since 2020-06-28
 */
@Service
public abstract class AbstractLinkedinJobs extends AbstractScraper implements Scrapper {

	protected ChromeDriver driver;
	protected final Logger log = LoggerFactory.getLogger(getClass());
	private WebDriverWait wait;
	Random rand = new Random(); 
	
	private @Autowired UrlRepo repo;
	private @Autowired PropertyDetailsRepo detRepo;
	
	private static final String DET_PATH = "//div[@class = 'list-card-info']/a";

	@Override
	public void scrapJobs() {
		driver = getChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, 40 );
		driver.get("https://www.zillow.com/homes/Atlanta,-GA_rb/");
		try {
			scrapThroughPagination();
		} catch (InterruptedException e) {
			log.warn("Can not get data", e);
		}
		finally {
			driver.manage().deleteAllCookies();
//			driver.quit();
		}
	}
	
//	private void scrapAll() throws InterruptedException {
//		List<Url> url = repo.findAllByIsScrapped(false);
//		for(int i =0 ; i< 100; i++) {
//			saveDetails(scrapDet(url.get(i).getUrl()), url.get(i));
//			driver.manage().deleteAllCookies();
//			driver.quit();
//			Thread.sleep(TIME_1M*rand.nextInt(15));
//			driver = getChromeDriver();
//			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
//			driver.manage().window().fullscreen();
//		}
//		
//	}
	
	private void scrapThroughPagination() throws InterruptedException {
		for(int i=0 ; i< 20; i++) {
			List<WebElement> list = driver.findElementsByXPath(DET_PATH);
			int length = list.size();
			for(int j = 0 ; j < length ; j++) {
				String url = list.get(j).getAttribute("href");
				list.get(j).click();
				Thread.sleep(TIME_10S * 3);
				saveDetails(scrapDet(url));
				driver.navigate().back();
				wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(DET_PATH)));
				list = driver.findElementsByXPath(DET_PATH);
			}
			driver.findElementByXPath("//a[@title = 'Next page']").click();
			Thread.sleep(TIME_1M);
		}
		
	}
	
	@Transactional
	private void saveDetails(PropertyDetails scrapDet) {
		try {
			detRepo.save(scrapDet);
		}catch (Exception e) {
			log.warn("Failed to save data", e);
		}
	}

	private PropertyDetails scrapDet(@NonNull String url) {
		
		try{
			log.info("Fetching "+url+" ......");
			
			PropertyDetails ob = new PropertyDetails();
			ob.setUrl(url);
			
			String add = driver.findElementsByClassName("ds-address-container").get(0).getText().trim();
			ob.setAddress(add);
			
			String [] parts = add.split(",");
			ob.setCity(parts[parts.length - 2]);
			
			parts = parts[parts.length - 1].split(" ");
			ob.setState(parts[parts.length-2]);
			ob.setZip(parts[parts.length-1]);
			
			ob.setPrice(driver.findElementsByClassName("ds-value").get(0).getText().trim());
			
			List<WebElement> headInfoList = driver.findElementsByXPath("//span[@class = 'ds-bed-bath-living-area']");
			ob.setBed(headInfoList.get(0).findElement(By.tagName("span")).getText());
			ob.setBath(headInfoList.get(1).findElement(By.tagName("span")).getText());
			ob.setLot(headInfoList.get(2).getText());
			
			List<WebElement> det = driver.findElementsByXPath("//span[@class = 'ds-body ds-home-fact-value']");
			ob.setType(det.get(0).getText().trim());
			ob.setBuiltYear(det.get(1).getText().trim());
			ob.setHeating(det.get(2).getText().trim());
			ob.setCooling(det.get(3).getText().trim());
			ob.setParking(det.get(4).getText().trim());
			
			System.out.println(ob.toString());
			return ob;
			
		}catch (Exception e) {
			log.warn("Failed to scrap details : ", e);
			return null;
		}		
	}

//	
//	private void saveUrl(String url){
//		try{
//			repo.save(new Url(url));
//		}catch (Exception e) {
//			log.warn("can not save this url", e.getLocalizedMessage());
//		}
//	}

}
