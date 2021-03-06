package Tests.FrontEndTests;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FirefoxTests  {
	
	static final int NUM_TESTS = 5; // UPDATE THIS NUMBER WHEN MORE TESTS ARE CREATED
	
    static String pss = "";
    static String modelName  = "";
    static String products = "";
    static String accounts = "";
    static int frequency = 0;
    static String date = "";
    static String text = "";
    static File log;
    static FileWriter w;
    static boolean pass = true;
    
	public static void main(String[] args) throws IOException {

		log = new File("log.txt");
		w = new FileWriter(log, true);

		w.write("===============================================\n");
		w.write("Starting test run - Firefox Tests " + new Date() +  "\n\n");
		
		long start = System.nanoTime();
		
		System.setProperty("webdriver.gecko.driver","geckodriver.exe");
		WebDriver driver = new FirefoxDriver();

		// Set the website URL
		driver.get("http://localhost:8080/Diversity/pages/index.html?role_desc=DESIGNER");

		boolean create = testCreate(driver);
		boolean edit = testEdit(driver);
		boolean view = testView(driver);
		boolean extract = testExtraction(driver);
		boolean delete = testDelete(driver);
		
		long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

		w.write("-----------------------------------------------\n");
		w.write("Test run finished. Results: \n");
		w.write("Test 1 - Create Opinion Model: " + (create ? "passed\n" : "failed\n"));
		w.write("Test 2 - Edit Opinion Model: " + (edit ? "passed\n" : "failed\n"));
		w.write("Test 3 - View Opinion Model: " + (view ? "passed\n" : "failed\n"));
		w.write("Test 4 - View Opinion Extraction: " + (extract ? "passed\n" : "failed\n"));
		w.write("Test 5 - Delete Opinion Model: " + (delete ? "passed\n" : "failed\n"));
		
		int passed = 0;
		
		if (create) {
			passed++;
		}
		
		if (edit) {
			passed++;
		}
		
		if (view) {
			passed++;
		}
		
		if (extract) {
			passed++;
		}
		
		if (delete) {
			passed++;
		}
		
		w.write("\nTests passed: " + passed);
		w.write("\nTests failed: " + (NUM_TESTS - passed));
		w.write("\nElapsed time: " + elapsed + " milliseconds");
		
		w.close();
		driver.close();
	}

	/** Test 1 - Create new model
     * Steps: 
     * 1. Click Create Opinion Model
     * 2. Select PSS (this test always selects the 3rd option)
     * 3. Fill in the model name
     * 4. Select some final products
     * 5. Add social network and user name
     * 6. Define update frequency
     * 7. Define start date
     * 8. Submit
     * 9. Open edit page to check if data matches
     * 10. Delete the created test model
     *
     * @param driver - the selenium webdriver
     * @return - true if the test passes, otherwise returns false
     * @throws IOException - if an error occurs with the log.txt file
     */
    public static boolean testCreate(WebDriver driver) throws IOException {
    	w.write("Starting Create Opinion Model Test\n\n");
        driver.findElement(By.linkText("Create Opinion Model")).click();
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            public Boolean apply(WebDriver d) {
            	if (!d.getCurrentUrl().contains("models.html")) {
            		try {
						w.write("Page was not redirected to model creation after clicking. Stopping test run.\n");
						pass = false;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		return false;
            	}
            	//Find and set the PSS dropdown
            	WebElement pssBox = d.findElement(By.id("pss"));
            	Select pssList = new Select(pssBox);
            	pssList.selectByIndex(2);
            	pss = pssList.getFirstSelectedOption().getText();
            	
            	//Generate a random 32 bit integer name for the model name to avoid duplicates
            	WebElement modelNameBox = d.findElement(By.id("model_name"));
            	modelName = (new BigInteger(32,new Random())).toString();
            	modelNameBox.sendKeys(modelName);
            	
            	//Select the first two options from the product selection tree
            	WebElement product1 = d.findElement(By.id("j2_1"));
            	WebElement product2 = d.findElement(By.id("j2_2"));
            	product1.click();
            	product2.click();
            	products = product1.getText() + ";" + product2.getText() + ";";
            	
               	// Select social network and type a name twice (add two accounts)
            	WebElement socialNetworkBox = d.findElement(By.id("new_name"));
            	WebElement userNameBox = d.findElement(By.id("new_URI"));
            	WebElement button = d.findElement(By.className("glyphicon"));
            	userNameBox.sendKeys("First User");
            	Select socialNetworkList = new Select(socialNetworkBox);
            	socialNetworkList.selectByIndex(0);
            	accounts += socialNetworkList.getFirstSelectedOption().getAttribute("value") + " / " + userNameBox.getAttribute("value") + ";";
            	button.click();
            	userNameBox.sendKeys("Second User");
            	socialNetworkList.selectByIndex(1);
            	accounts += socialNetworkList.getFirstSelectedOption().getAttribute("value") + " / " + userNameBox.getAttribute("value") + ";";
            	button.click();
            	
            	// Select the checkboxes added by the account definition
            	boolean tmp = false; // to ignore the first checkbox (final product)
            	String checkboxes = "//*[@type='checkbox']";
            	List<WebElement> elementToClick = d.findElements(By.xpath(checkboxes));
            	for (WebElement AllCheck : elementToClick) {
            		if (tmp) {
            			AllCheck.click();
            		} else {
            			tmp = true;
            		}
            	}
            	
            	// Set the update frequency to 13 days
            	WebElement frequencyBox = d.findElement(By.id("frequency"));
            	frequencyBox.clear();
            	frequencyBox.sendKeys("3");
            	frequency = Integer.parseInt(frequencyBox.getAttribute("value"));
            	
            	// Set a start date
            	WebElement dateCheck = d.findElement(By.id("start_date"));
            	WebElement dateBox = d.findElement(By.id("date_input"));
            	dateBox.sendKeys("12152017");
            	date = dateBox.getAttribute("value");
            	// Submit the form
        		d.findElement(By.id("submit")).click();
        		(new WebDriverWait(d, 10)).until(new ExpectedCondition<Boolean>() {
        			public Boolean apply(WebDriver d) {
        				Alert alert = d.switchTo().alert();
        				text = alert.getText();
                		alert.accept();
                		return true;
        			}
        		});
            	
            	if (text.contains("Successfully added model")) {
            		try {
						w.write("Model " + modelName + " created. The test will now attempt to edit the model to check if the data is correct.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	  	return true;
            	} else {
            		return false;
            	}
            }
        });
        
        
        if (driver.getCurrentUrl().contains("index.html")) {
        	
        } else {
        	w.write("Page was not redirected to index.html after creating model. Stopping test run. \nTest Create Opinion Model failed.\n");
        	pass = false;
        	return false;
        }
        driver.findElement(By.linkText("Edit Opinion Model")).click();
        Select modelsList = new Select(driver.findElement(By.id("Models")));
        WebElement el = null;
        boolean modelExists = false;
        for (WebElement model : modelsList.getOptions()) {
        	if (model.getText().equals(modelName)) {
        		el = model;
        		break;
        	}
        }
        
        if (el == null) {
            w.write("Model " + modelName + " was not added or was added with an incorrect name. Stopping test run. \nTest Create Opinion Model failed.\n");
        	pass = false;
            return false;
        }
        
        w.write("Attempting to open edit page...\n");
        modelsList.selectByIndex(modelsList.getOptions().indexOf(el));
        driver.findElement(By.id("view_edit")).click();
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            /* (non-Javadoc)
             * @see com.google.common.base.Function#apply(java.lang.Object)
             */
            public Boolean apply(WebDriver d) {
            	if (!d.getCurrentUrl().contains("models.html")) {
            		try {
						w.write("Page was not redirected after clicking Edit Model. Stopping test run\n");
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            		return false;
            	} else {
            		try {
						w.write("Edit page opened successfully. Now checking if data matches...\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	//Get all elements on the edit page
            	WebElement pssBox = d.findElement(By.id("pss"));
            	Select pssList = new Select(pssBox);
            	String pssEdit = pssList.getFirstSelectedOption().getText();
            	WebElement modelNameBox = d.findElement(By.id("model_name"));
            	List<WebElement> productsList = d.findElements(By.className("jstree-clicked"));
            	String selectedProducts = "";
            	
            	for (WebElement product : productsList) {
            		selectedProducts += product.getText() + ";";
            	}
            	List<WebElement> userList = d.findElements(By.name("user"));
            	String selectedUsers = "";
            	for (WebElement user : userList) {
            		selectedUsers += user.getText() + ";";
            	}
            	int freq = Integer.parseInt(d.findElement(By.id("frequency")).getAttribute("value"));
				
            	//Check each element to see if it matches with the saved data
            	if (!pss.equals(pssEdit)) {
            		try {
						w.write("PSS does not match. Expected " + pss + ", got " + pssEdit + " instead.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            	}
            	
            	if (!modelName.equals(modelNameBox.getAttribute("value"))) {
            		try {
						w.write("Model name does not match. Expected " + modelName + ", got " + modelNameBox.getAttribute("value") + " instead.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            	}
            	
            	if (!products.equals(selectedProducts)) {
            		try {
						w.write("Selected products list does not match. Expected " + products + ", got " + selectedProducts + " instead.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            	}
            	if (!accounts.equals(selectedUsers)) {
            		try {
						w.write("User list does not match. Expected " + accounts + ", got " + selectedUsers + " instead.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            	}
            	if (frequency != freq) {
            		try {
						w.write("Update frequency does not match. Expected " + frequency + ", got " + freq + " instead.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		pass = false;
            	}
            	
            	d.findElement(By.id("submit2")).click();
            	
            	if (pass) {
            		try {
						w.write("All fields match the input data.\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	return true;
            }});
        
        
        w.write("Test Create Opinion Model reached the end.\n");
        if (pass) {
        	w.write("All steps were completed successfully. \n");
        	return true;
        } else {
        	w.write("Errors ocurred during the execution of this test. Please check this log for additional details.\n");
        	return false;
        }
        
    }
    
    private static boolean testExtraction(WebDriver driver) {
    	
    	driver.findElement(By.id("model_box")).click();
    	
    	(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				if (!d.getCurrentUrl().contains("opinion_extraction")) {
					pass = false;
					return false;
				}
				return true;
			}
		});
    	
    	driver.findElement(By.id("home")).click();
		return pass;
		// TODO Auto-generated method stub
		
	}

	private static boolean testDelete(WebDriver driver) {
		driver.findElement(By.linkText("Delete Opinion Model")).click();
        Select modelsList2 = new Select(driver.findElement(By.id("Models")));
        WebElement el2 = null;
        for (WebElement model : modelsList2.getOptions()) {
        	if (model.getText().equals(modelName)) {
        		el2 = model;
        		break;
        	}
        }
        if (el2 != null) {
        	
            modelsList2.selectByIndex(modelsList2.getOptions().indexOf(el2));
            driver.findElement(By.id("view_delete")).click();
            
    		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
    			public Boolean apply(WebDriver d) {
    				Alert alert = d.switchTo().alert();
            		alert.accept();
            		return true;
    			}
    		});
    		
    		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
    			public Boolean apply(WebDriver d) {
    				Alert alert = d.switchTo().alert();
            		alert.accept();
            		return true;
    			}
    		});
        }
		return pass;
		// TODO Auto-generated method stub
		
	}

	private static boolean testView(WebDriver driver) throws IOException {
		driver.findElement(By.linkText("View Opinion Model")).click();
        Select modelsList2 = new Select(driver.findElement(By.id("Models")));
        WebElement el2 = null;
        for (WebElement model : modelsList2.getOptions()) {
        	if (model.getText().equals(modelName)) {
        		el2 = model;
        		break;
        	}
        }
        if (el2 != null) {	
            modelsList2.selectByIndex(modelsList2.getOptions().indexOf(el2));
            driver.findElement(By.id("view_select")).click();
        
    		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
    			public Boolean apply(WebDriver d) {
    				if (!d.getCurrentUrl().contains("models.html")) {
    					try {
							w.write("Page was not redirected successfully. Stopping test run.\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					pass = false;
    					return false;
    				}
    				
    				if (d.findElement(By.tagName("h1")).getText().contains("View")) {
    					try {
							w.write("View opinion model page was opened successfully.\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					pass = true;
    					return true;
    				}
    				
    				
    				return false;
    			}
    		});
        }
        
        driver.findElement(By.id("submit2")).click();
        
        w.write("Test View Opinion Model reached the end.\n");
        if (pass) {
        	w.write("All steps were completed successfully. \n");
        	return true;
        } else {
        	w.write("Errors ocurred during the execution of this test. Please check this log for additional details.\n");
        	return false;
        }
	}

	private static boolean testEdit(WebDriver driver) throws IOException {
		driver.findElement(By.linkText("Edit Opinion Model")).click();
        Select modelsList2 = new Select(driver.findElement(By.id("Models")));
        WebElement el2 = null;
        for (WebElement model : modelsList2.getOptions()) {
        	if (model.getText().equals(modelName)) {
        		el2 = model;
        		break;
        	}
        }
        if (el2 != null) {	
            modelsList2.selectByIndex(modelsList2.getOptions().indexOf(el2));
            driver.findElement(By.id("view_edit")).click();
        
    		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
    			public Boolean apply(WebDriver d) {
    				if (!d.getCurrentUrl().contains("models.html")) {
    					try {
							w.write("Page was not redirected successfully. Stopping test run.\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					pass = false;
    					return false;
    				}
    				
    				if (d.findElement(By.tagName("h1")).getText().contains("Edit")) {
    					try {
							w.write("Edit opinion model page was opened successfully.\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					pass = true;
    					return true;
    				}
    				
    				
    				return false;
    			}
    		});
        }
        
        driver.findElement(By.id("submit2")).click();
        
        w.write("Test View Opinion Model reached the end.\n");
        if (pass) {
        	w.write("All steps were completed successfully. \n");
        	return true;
        } else {
        	w.write("Errors ocurred during the execution of this test. Please check this log for additional details.\n");
        	return false;
        }
	}
}

 
