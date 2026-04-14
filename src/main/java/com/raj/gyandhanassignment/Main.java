package com.raj.gyandhanassignment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.raj.gyandhanassignment.model.Course;
import com.raj.gyandhanassignment.service.Scraper;

public class Main {
    public static void main(String[] args) {
        
        List<String> courseUrls = Arrays.asList(
            "https://www.coventry.ac.uk/course-structure/ug/fbl/law-llb-hons/?term=2026-27",
            "https://www.coventry.ac.uk/course-structure/ug/fbl/accounting-and-finance-bsc-hons/?term=2026-27",
            "https://www.coventry.ac.uk/course-structure/ug/fbl/business-management-ba-hons/?term=2026-27",
            "https://www.coventry.ac.uk/course-structure/ug/fah/automotive-and-transport-design-mdesba-hons/?term=2026-27",
            "https://www.coventry.ac.uk/course-structure/ug/hls/psychology-bsc-hons/?term=2026-27"
        );

        Scraper scraper = new Scraper();
        List<Course> scrapedCourses = new ArrayList<>();

        System.out.println("Starting extraction for " + courseUrls.size() + " course(s)...");

        for (String url : courseUrls) {
            System.out.println("\nProcessing: " + url);
            
            Course courseData = scraper.scrapeCourse(url);
            scrapedCourses.add(courseData);
            
            // Optional: Print the raw section to the console to help you build your extraction logic
            // System.out.println("--- Raw Data Captured for Logic Building ---");
            // System.out.println(courseData.raw_section); // Uncomment this line to see the raw HTML/Text in your terminal
            // System.out.println("--------------------------------------------");
        }

        scraper.close();

        if (!scrapedCourses.isEmpty()) {
            exportToCsv(scrapedCourses, "courses_output.csv");
        } else {
            System.out.println("No data was scraped.");
        }
    }

    // Helper method to handle the CSV file generation
    private static void exportToCsv(List<Course> courses, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            
            // Write the header row
            writer.write(Course.getCsvHeaders() + "\n");
            
            // Write each course as a row
            for (Course course : courses) {
                writer.write(course.toCsvRow() + "\n");
            }
            
            System.out.println("\nSuccessfully generated " + fileName + " with " + courses.size() + " records.");
            
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }
}