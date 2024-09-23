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
        
        JsonArray sectionArray = new JsonArray();
        LinkedHashMap<String, LinkedHashMap<String, Object>> courseMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> scheduleTypeMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> subjectMap = new LinkedHashMap<>();
        
        Iterator<String[]> iterator = csv.iterator();
        String[] headerline = iterator.next();
        
        while (iterator.hasNext()){
            String[] row = iterator.next();
            JsonObject sectionJson = new JsonObject();
            
            for (int i = 0; i < headerline.length; i++){
                sectionJson.put(headerline[i], row[i]);
            }
            
            scheduleTypeMap.put(sectionJson.get(TYPE_COL_HEADER).toString(), sectionJson.get(SCHEDULE_COL_HEADER).toString());
            
            String[] courseName = sectionJson.get(NUM_COL_HEADER).toString().split(" ");
            subjectMap.put(courseName[0], sectionJson.get(SUBJECT_COL_HEADER).toString());
            
            LinkedHashMap<String, Object> courseDetails = courseMap.getOrDefault(sectionJson.get(NUM_COL_HEADER).toString(), new LinkedHashMap<>());
            courseDetails.put(SUBJECTID_COL_HEADER, courseName[0]);
            courseDetails.put(NUM_COL_HEADER, courseName[1]);
            courseDetails.put(DESCRIPTION_COL_HEADER, sectionJson.get(DESCRIPTION_COL_HEADER));
            courseDetails.put(CREDITS_COL_HEADER, Integer.parseInt(sectionJson.get(CREDITS_COL_HEADER).toString()));
            courseMap.put(sectionJson.get(NUM_COL_HEADER).toString(), courseDetails);
            
            LinkedHashMap<String, Object> sectionDetails = new LinkedHashMap<>();
            sectionDetails.put(CRN_COL_HEADER, Integer.parseInt(sectionJson.get(CRN_COL_HEADER).toString()));
            sectionDetails.put(SUBJECT_COL_HEADER, courseName[0]);
            sectionDetails.put(NUM_COL_HEADER, courseName[1]);
            sectionDetails.put(SECTION_COL_HEADER, sectionJson.get(SECTION_COL_HEADER));
            sectionDetails.put(TYPE_COL_HEADER, sectionJson.get(TYPE_COL_HEADER));
            sectionDetails.put(START_COL_HEADER, sectionJson.get(START_COL_HEADER));
            sectionDetails.put(END_COL_HEADER, sectionJson.get(END_COL_HEADER));
            sectionDetails.put(DAYS_COL_HEADER, sectionJson.get(DAYS_COL_HEADER));
            
            String[] instructors = sectionJson.get(INSTRUCTOR_COL_HEADER).toString().split(", ");
            sectionDetails.put(INSTRUCTOR_COL_HEADER, instructors);
            
            sectionArray.add(sectionDetails);
        }
        
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("scheduleType", scheduleTypeMap);
        jsonObject.put("subject", subjectMap);
        jsonObject.put("course", courseMap);
        jsonObject.put("section", sectionArray);
        
        return Jsoner.serialize(jsonObject);
        
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        
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