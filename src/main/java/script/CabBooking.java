package script;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.Select;

public class CabBooking implements Callable<String> {

	private final String URL = "<URL>";
	private final boolean GUI_MODE = true;

	private final String username;
	private final String password;
	private final String destination;
	private final Map<String, String> userResponse;

	public CabBooking(String username, String password, String destination, Map<String, String> userResponse) {
		super();
		this.username = username;
		this.password = password;
		this.destination = destination;
		this.userResponse = userResponse;
	}

	private boolean isLoginSuccessful(WebDriver driver) {
		try {
			Thread.sleep(1500);
			driver.findElement(By.cssSelector("input[ng-click='destSelected()']")).click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String login(WebDriver driver) {
		System.out.println("Entering username and password.");
		driver.findElement(By.cssSelector("input[ng-model='SSO']")).sendKeys(username);
		driver.findElement(By.cssSelector("input[ng-model='pass']")).sendKeys(password);
		driver.findElement(By.cssSelector("input[value='Login']")).click();
		System.out.println("Submitted the login form. Waiting for response...");
		if (isLoginSuccessful(driver)) {
			return "success";
		} else {
			if (isLoginSuccessful(driver)) {
				return "success";
			}
			return "failure";
		}
	}

	private String bookCab(WebDriver driver) {
		String message = "error occured";
		System.out.println("Selecting the destination and time...");
		new Select(driver.findElement(By.cssSelector("[ng-model='routeSelected']"))).selectByValue(destination);
		new Select(driver.findElement(By.cssSelector("[ng-model='timeSelected']")))
				.selectByVisibleText(userResponse.get("time"));
		driver.findElement(By.cssSelector("input[value='Kensington']")).click();
		int count = 0;
		try {
			do {
				driver.findElement(By.cssSelector("[ng-click='clickBook(timeSelected)'][ng-disabled='isBookDisabled']"))
						.click();
				try {
					driver.findElement(By.cssSelector("[ng-click='yes()']")).click();
					Thread.sleep(1000);
					driver.findElement(By.cssSelector(".modal-footer [ng-click='close()']")).click();
				} catch (NoSuchElementException e) {
					driver.findElement(By.cssSelector(".modal-footer [ng-click='close()']")).click();
				}
				if (driver.findElement(By.cssSelector("[ng-click='bookingCancel()']")).isDisplayed()) {
					System.out.println("Taking a screenshot...");
					File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(srcFile, new File("c:\\tmp\\screenshot.png"));
					message = "Booked Succesfully! Please see the screenshot in C->temp folder";
				} else {
					System.out.println("Try booking prior to one hour. but we are trying..."+ (++count));
				}
			} while (!driver.findElement(By.cssSelector("[ng-click='bookingCancel()']")).isDisplayed());
			return message;
		} catch (IOException e) {
			return "Can not capture screenshot!";
		} catch (Exception e) {
			File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			message = "Your seat is already booked! \nPlease see the screenshot in C->temp folder for detailed information ";
			try {
				FileUtils.copyFile(srcFile, new File("c:\\tmp\\screenshot.png"));
			} catch (IOException e1) {
				return "Can not capture screenshot!";
			}
		} finally {
			driver.quit();
		}
		return message;

	}

	private String cancelCab(WebDriver driver) {
		try {
			if (driver.findElement(By.cssSelector("[ng-click='bookingCancel()']")).isDisplayed()) {
				driver.findElement(By.cssSelector("[ng-click='bookingCancel()']")).click();
				Thread.sleep(500);
				driver.findElement(By.cssSelector("[ng-click='yes()']")).click();
				Thread.sleep(1000);
				System.out.println("Taking a screenshot...");
				File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(srcFile, new File("c:\\tmp\\Cancel_screenshot.png"));
				driver.findElement(By.cssSelector(".modal-footer [ng-click='close()']")).click();
				if (driver.findElement(By.cssSelector("[ng-click='bookingCancel()']")).isDisplayed()) {
					driver.quit();
					return "Failed to cancel booking.  Please see the screenshot in C->temp folder";
				} else {
					return "Successfully cancelled booking.  Please see the screenshot in C->temp folder";
				}
			} else {
				return "Sorry! it seems that you have not yet booked your cab.";
			}
		} catch (InterruptedException e) {
			System.out.println("Thread Exception");
			return "Failed due to thread termination";
		} catch (IOException e) {
			System.out.println("Can not capture screenshot!");
			return "But please manually check if booked or try to cancel cab.";
		} finally {
			driver.quit();
		}

	}

	public String call() throws Exception {
		WebDriver driver;
		if (GUI_MODE) {
			System.setProperty("webdriver.gecko.driver",
					"C:\\Users\\\\Downloads\\geckodriver-v0.15.0-win64\\geckodriver.exe");
			driver = new FirefoxDriver();
		} else {
			System.setProperty("phantomjs.binary.path",
					"C:\\Users\\\\Downloads\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
			driver = new PhantomJSDriver();
		}

		if ("Book Cab".equalsIgnoreCase(userResponse.get("operation"))) {
			System.out.println("We are booking a cab for you. Just relax! We will notify you soon........");
			System.out.println("------------ CAB INFORMATION ------------\n" + "DESTINATION: "
					+ destination.toUpperCase() + "\n" + "TIME: " + userResponse.get("time"));
			System.out.println("Processing your request...");
		}
		System.out.println("Opening the cab website...");
		driver.get(URL);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		if ("success".equalsIgnoreCase(login(driver))) {
			driver.manage().timeouts().implicitlyWait(20, TimeUnit.MILLISECONDS);
			System.out.println("Login successfull");
			if ("Book Cab".equalsIgnoreCase(userResponse.get("operation"))) {
				return bookCab(driver);
			} else {
				return cancelCab(driver);
			}
		} else {
			driver.quit();
			System.out.println("For more details please see the screenshot in C->temp folder ");
			return "Login Failed";
		}
	}

}
