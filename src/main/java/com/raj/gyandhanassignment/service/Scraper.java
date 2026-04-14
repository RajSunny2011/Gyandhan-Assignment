package com.raj.gyandhanassignment.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import com.raj.gyandhanassignment.model.Course;

public class Scraper {
    private Playwright playwright;
    private Browser browser;

    public Scraper() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true) // Set to false to see the browser
        );
    }

    public Course scrapeCourse(String url) {
        Course course = new Course();
        course.course_website_url = url;

        // Use a try-with-resources block for the Page so it auto-closes after scraping
        try (Page page = browser.newPage()) {
            int maxRetries = 3;
            int attempt = 0;
            boolean pageLoaded = false;
            System.out.println("Navigating to: " + url);
            page.navigate(url);
            
            // Wait for specific content to load and attempt again on timeout
            while (attempt < maxRetries && !pageLoaded) {
                try {
                    attempt++;
                    page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(30000)); 
                    
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                    pageLoaded = true; 
                    
                } catch (Exception e) {
                    System.err.println("Attempt " + attempt + " failed.");
                    Thread.sleep(2000); 
                }
            }

            if (!pageLoaded) {
                throw new RuntimeException("ERROR: Failed to load page after " + maxRetries + " attempts.");
            }

            // POPULATE RAW SECTION
            try {
                course.raw_section = page.locator("body").innerText();
            } catch (Exception e) {
                course.raw_section = "Could not load raw body.";
            }

            // TARGETED EXTRACTION LOGIC
            // University Name, Country and Address
            JSONObject universityJson = getJsonLdByType(page, "CollegeOrUniversity");
            if (universityJson != null) {
                // University Name
                course.university_name = universityJson.optString("name", "Coventry University");
                System.out.println("University Name: " + course.university_name);

                JSONObject addressObj = universityJson.optJSONObject("address");
                
                if (addressObj != null) {
                    // Country Code
                    course.country = addressObj.optString("addressCountry", "NA");
                    System.out.println("Country Code: " + course.country);

                    // Assress
                    String street = addressObj.optString("streetAddress", "");
                    String locality = addressObj.optString("addressLocality", "");
                    String zip = addressObj.optString("postalCode", "");

                    String fullAddress = "";
                    if (!street.isEmpty()) fullAddress += street;
                    if (!locality.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + locality;
                    if (!zip.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + zip;
                    
                    course.address = fullAddress.isEmpty() ? "NA" : fullAddress;
                    System.out.println("Address: " + course.address);
                }
            }

            // Course Name
            JSONObject courseJson = getJsonLdByType(page, "Course");
            if (courseJson != null) {
                course.program_course_name = courseJson.optString("name", "NA");
                System.out.println("Course Name: " + course.program_course_name);
            }

            // Grabbing Every Script
            List<Locator> allScripts = page.locator("script").all();
            String dataLayerScript = "NA";

            for (Locator script : allScripts) {
                String text = script.textContent();
                
                if (text != null && text.contains("levelOfStudy") && text.contains("dataLayer.push")) {
                    dataLayerScript = text;
                    break;
                }
            }

            if (!dataLayerScript.equals("NA")) {
                course.study_level = extractWithRegex(dataLayerScript, "'levelOfStudy':\\s*'([^']+)'");
                course.course_duration = extractWithRegex(dataLayerScript, "'studyMode':\\s*'([^']+)'");
                course.all_intakes_available = extractWithRegex(dataLayerScript, "'term':\\s*'([^']+)'");
            } else {
                System.out.println("Could not find the dataLayer script block.");
            }
            System.out.println("Study Level: " + course.study_level);
            System.out.println("Duration: " + course.course_duration);
            System.out.println("Tntake Terms: " + course.all_intakes_available);

            // Yearly fees
            String rawFee = extractText(page, ".Fees-International-FullTime");
            course.yearly_tuition_fee = extractWithRegex(rawFee, "((?:[£$€₹]|Rs\\.?\\s*)[0-9,]+)");
            System.out.println("Fee: " + course.yearly_tuition_fee);

            // min ielts
            String rawRequirementsText = extractText(page, "li:has-text('IELTS')");
            System.out.println("Requirements Text: " + rawRequirementsText);
            course.min_ielts = rawRequirementsText;
            // course.min_ielts = extractWithRegex(rawRequirementsText, "IELTS.*?([0-9]\\.[0-9])");

        } catch (Exception e) {
            System.err.println("Error scraping " + url + ": " + e.getMessage());
        }

        return course;
    }

    // Helper method to safely extract text and return "NA" if missing
    private String extractText(Page page, String selector) {
        try {
            Locator locator = page.locator(selector).first();
            
            // Check if the element exists in the HTML at all (count > 0)
            if (locator.count() > 0) {
                String text = locator.textContent();
                if (text != null) {
                    return text.trim().replaceAll("\\s+", " ");
                }
            }
        } catch (Exception e) {
            // Ignore exception, fallback to returning "NA"
        }
        return "NA";
    }

    public void close() {
        browser.close();
        playwright.close();
    }

    private JSONObject getJsonLdByType(Page page, String targetType) {
        // Grab all JSON-LD script tags
        List<Locator> jsonScripts = page.locator("script[type='application/ld+json']").all();

        for (Locator script : jsonScripts) {
            try {
                String jsonText = script.textContent();
                if (jsonText == null || jsonText.trim().isEmpty()) continue;

                // Parse the string into a workable JSON Object
                JSONObject jsonObject = new JSONObject(jsonText);
                
                // Check if this is the block we are looking for
                if (targetType.equals(jsonObject.optString("@type"))) {
                    return jsonObject; // Return the whole object to be used temporarily
                }
            } catch (Exception e) {
                // Ignore malformed JSON and just check the next one
            }
        }
        return null; // Return null if the specific type wasn't found on the page
    }

    private String extractWithRegex(String rawText, String regexPattern) {
        if (rawText == null || rawText.equals("NA")) return "NA";
        
        Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rawText);
        
        if (matcher.find()) {
            return matcher.group(1).trim(); // Returns the part captured inside the (parentheses)
        }
        return "NA";
    }
}