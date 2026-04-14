package com.raj.gyandhanassignment.model;

import java.lang.reflect.Field;

public class Course {

    public String program_course_name = "NA"; //done
    public String university_name = "NA"; //done
    public String course_website_url = "NA"; //done
    public String campus = "NA"; //sone
    public String country = "NA"; //done
    public String address = "NA"; //done
    public String study_level = "NA";
    public String course_duration = "NA"; //done
    public String all_intakes_available = "NA";
    public String mandatory_documents_required = "NA";
    public String yearly_tuition_fee = "NA";
    public String scholarship_availability = "NA";
    public String gre_gmat_mandatory_min_score = "NA";
    public String indian_regional_institution_restrictions = "NA";
    public String class_12_boards_accepted = "NA";
    public String gap_year_max_accepted = "NA";
    public String min_duolingo = "NA";
    public String english_waiver_class12 = "NA";
    public String english_waiver_moi = "NA";
    public String min_ielts = "NA";
    public String kaplan_test_of_english = "NA";
    public String min_pte = "NA";
    public String min_toefl = "NA";
    public String ug_academic_min_gpa = "NA";
    public String twelfth_pass_min_cgpa = "NA";
    public String mandatory_work_exp = "NA";
    public String max_backlogs = "NA";

    // RAW SECTION FOR DEBUGGING/BUILDING LOGIC
    public String raw_section = "";

    // Helper to extract headers for CSV
    public static String getCsvHeaders() {
        StringBuilder headers = new StringBuilder();
        for (Field field : Course.class.getDeclaredFields()) {
            if (field.getName().equals("raw_section")) continue; // Skip raw section for CSV
            headers.append(field.getName()).append(",");
        }
        return headers.substring(0, headers.length() - 1);
    }

    // Helper to extract values for CSV
    public String toCsvRow() {
        StringBuilder row = new StringBuilder();
        for (Field field : Course.class.getDeclaredFields()) {
            if (field.getName().equals("raw_section")) continue; // Skip raw section for CSV
            
            try {
                String value = (String) field.get(this);
                if (value != null) {
                    // Escape quotes and wrap in quotes to prevent CSV breakage
                    value = value.replace("\"", "\"\"");
                    row.append("\"").append(value).append("\",");
                } else {
                    row.append("\"NA\",");
                }
            } catch (IllegalAccessException e) {
                row.append("\"NA\",");
            }
        }
        return row.substring(0, row.length() - 1);
    }
}