package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        JsonObject json = new JsonObject();
        
        //Creating maps for both subjects and courses
        HashMap<String, String> subjects = new HashMap<>();
        HashMap<String, JsonObject> courses = new HashMap<>();
        JsonArray sections = new JsonArray();
        
        Iterator<String[]> iterator = csv.iterator();
        
        //skips the header row
        iterator.next();
        
        while (iterator.hasNext()){
            String[] row = iterator.next();
            
            String crn = row[0];
            String subject = row[1];
            String num = row[2];
            String description = row[3];
            String section = row[4];
            String type = row[5];
            String credits = row[6];
            String start = row[7];
            String end  = row[8];
            String days = row[9];
            String where = row[10];
            String schedule = row[11];
            String instructor = row[12];
            
            //Populate subject map
            subjects.put(subject, row[1]); 
            
            //Builds course object
            String courseId = subject + " " + num;
            if (!courses.containsKey(courseId)){
                JsonObject course = new JsonObject();
                course.put(SUBJECTID_COL_HEADER, subject);
                course.put(NUM_COL_HEADER, num);
                course.put(DESCRIPTION_COL_HEADER, description);
                course.put(CREDITS_COL_HEADER, Integer.parseInt(credits));
                courses.put(courseId, course);
            }
            
            JsonObject sectionJson = new JsonObject();
            sectionJson.put(CRN_COL_HEADER, Integer.parseInt(crn));
            sectionJson.put(SUBJECTID_COL_HEADER, subject);
            sectionJson.put(NUM_COL_HEADER, num);
            sectionJson.put(SECTION_COL_HEADER, section);
            sectionJson.put(TYPE_COL_HEADER, type);
            sectionJson.put(START_COL_HEADER, start);
            sectionJson.put(END_COL_HEADER, end);
            sectionJson.put(DAYS_COL_HEADER, days);
            sectionJson.put(WHERE_COL_HEADER, where);
            sectionJson.put(INSTRUCTOR_COL_HEADER, new ArrayList<>(List.of(instructor)));
            
            sections.add(sectionJson);
            
        }
        
        //Add maps to the JSON object
        json.put("subject", subjects);
        json.put("course", courses);
        json.put("section", sections);
        
        //Convert to JSON string and return 
        return Jsoner.serialize(json);
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        return ""; // remove this!
        
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}