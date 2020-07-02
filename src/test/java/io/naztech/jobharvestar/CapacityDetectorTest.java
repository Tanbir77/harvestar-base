package io.naztech.jobharvestar;



import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CapacityDetectorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		System.out.println("Available Processor: "+Runtime.getRuntime().availableProcessors());
		System.out.println("Available Memory allocated for JVM: "+(Runtime.getRuntime().freeMemory()/1024)/1024+"Mb");
	}

}
