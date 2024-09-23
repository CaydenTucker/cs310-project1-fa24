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
import java.util.Arrays;
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
    if (csv.isEmpty()) {
        return " ";
    }

    String[] headers = csv.get(0);
    HashMap<String, Integer> headerMap = createHeaderMap(headers);
    
    JsonObject typeMap = new JsonObject();
    JsonObject subjectsMap = new JsonObject();
    JsonObject courseMap = new JsonObject();
    JsonArray jsonArray = new JsonArray();

    for (int i = 1; i < csv.size(); i++) {
        String[] row = csv.get(i);
        processCsvRow(row, headerMap, typeMap, subjectsMap, courseMap, jsonArray);
    }

    return createFinalJsonObject(typeMap, subjectsMap, courseMap, jsonArray);
}

private HashMap<String, Integer> createHeaderMap(String[] headers) {
    HashMap<String, Integer> headerMap = new HashMap<>();
    for (int i = 0; i < headers.length; i++) {
        headerMap.put(headers[i], i);
    }
    return headerMap;
}

private void processCsvRow(String[] row, HashMap<String, Integer> headerMap,
                           JsonObject typeMap, JsonObject subjectsMap,
                           JsonObject courseMap, JsonArray jsonArray) {
    String type = row[headerMap.get(TYPE_COL_HEADER)];
    String schedule = row[headerMap.get(SCHEDULE_COL_HEADER)];
    typeMap.putIfAbsent(type, schedule);

    String subjectId = extractSubjectId(row[headerMap.get(NUM_COL_HEADER)]);
    subjectsMap.putIfAbsent(subjectId, row[headerMap.get(SUBJECT_COL_HEADER)]);

    String num = row[headerMap.get(NUM_COL_HEADER)];
    String numWithoutCaps = num.replaceAll("[A-Z]", "").replaceAll("\\s", "");
    addCourseDetails(row, headerMap, courseMap, num, subjectId, numWithoutCaps);

    JsonObject sectionInfo = createSectionInfo(row, headerMap, numWithoutCaps, type, subjectId);
    jsonArray.add(sectionInfo);
}

private String extractSubjectId(String num) {
    return num.replaceAll("\\d", "").replaceAll("\\s", "");
}

private void addCourseDetails(String[] row, HashMap<String, Integer> headerMap,
                               JsonObject courseMap, String num, String subjectId,
                               String numWithoutCaps) {
    if (!courseMap.containsKey(num)) {
        JsonObject course = new JsonObject();
        course.put(SUBJECTID_COL_HEADER, subjectId);
        course.put(NUM_COL_HEADER, numWithoutCaps);
        course.put(DESCRIPTION_COL_HEADER, row[headerMap.get(DESCRIPTION_COL_HEADER)]);
        course.put(CREDITS_COL_HEADER, Integer.parseInt(row[headerMap.get(CREDITS_COL_HEADER)]));
        courseMap.put(num, course);
    }
}

private JsonObject createSectionInfo(String[] row, HashMap<String, Integer> headerMap,
                                      String numWithoutCaps, String type, String subjectId) {
    JsonObject sectionInfo = new JsonObject();
    sectionInfo.put(CRN_COL_HEADER, Integer.parseInt(row[headerMap.get(CRN_COL_HEADER)]));
    sectionInfo.put(SECTION_COL_HEADER, row[headerMap.get(SECTION_COL_HEADER)]);
    sectionInfo.put(START_COL_HEADER, row[headerMap.get(START_COL_HEADER)]);
    sectionInfo.put(END_COL_HEADER, row[headerMap.get(END_COL_HEADER)]);
    sectionInfo.put(DAYS_COL_HEADER, row[headerMap.get(DAYS_COL_HEADER)]);
    sectionInfo.put(WHERE_COL_HEADER, row[headerMap.get(WHERE_COL_HEADER)]);
    sectionInfo.put(INSTRUCTOR_COL_HEADER, createInstructorsArray(row[headerMap.get(INSTRUCTOR_COL_HEADER)]));
    sectionInfo.put(NUM_COL_HEADER, numWithoutCaps);
    sectionInfo.put(TYPE_COL_HEADER, type);
    sectionInfo.put(SUBJECTID_COL_HEADER, subjectId);
    return sectionInfo;
}

private JsonArray createInstructorsArray(String allInstructors) {
    JsonArray instructorsArray = new JsonArray();
    List<String> instructors = Arrays.asList(allInstructors.split(", "));
    for (String instructor : instructors) {
        instructorsArray.add(instructor);
    }
    return instructorsArray;
}

public String convertJsonToCsvString(JsonObject json) {
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");

    // Define the CSV header
    String[] header = {
        CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER,
        DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER,
        CREDITS_COL_HEADER, START_COL_HEADER, END_COL_HEADER,
        DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER,
        INSTRUCTOR_COL_HEADER
    };
    csvWriter.writeNext(header);

    processJsonToCsv(json, csvWriter);
    return writer.toString();
}

private void processJsonToCsv(JsonObject json, CSVWriter csvWriter) {
    JsonObject typeMap = (JsonObject) json.get("scheduletype");
    JsonObject subjectMap = (JsonObject) json.get("subject");
    JsonObject courseMap = (JsonObject) json.get("course");
    JsonArray jsonArray = (JsonArray) json.get("section");

    for (int i = 0; i < jsonArray.size(); i++) {
        JsonObject sectionDetails = jsonArray.getMap(i);
        writeCsvRecord(sectionDetails, typeMap, subjectMap, courseMap, csvWriter);
    }
}

private void writeCsvRecord(JsonObject sectionDetails, JsonObject typeMap,
                             JsonObject subjectMap, JsonObject courseMap,
                             CSVWriter csvWriter) {
    String crn = String.valueOf(sectionDetails.get(CRN_COL_HEADER));
    String subjectId = (String) sectionDetails.get(SUBJECTID_COL_HEADER);
    String num = subjectId + " " + sectionDetails.get(NUM_COL_HEADER);
    String section = (String) sectionDetails.get(SECTION_COL_HEADER);
    String type = (String) sectionDetails.get(TYPE_COL_HEADER);
    String start = (String) sectionDetails.get(START_COL_HEADER);
    String end = (String) sectionDetails.get(END_COL_HEADER);
    String days = (String) sectionDetails.get(DAYS_COL_HEADER);
    String where = (String) sectionDetails.get(WHERE_COL_HEADER);
    String instructor = concatenateInstructors((JsonArray) sectionDetails.get(INSTRUCTOR_COL_HEADER));
    String schedule = (String) typeMap.get(type);

    JsonObject courseDetails = (JsonObject) courseMap.get(num);
    String description = (String) courseDetails.get(DESCRIPTION_COL_HEADER);
    String credits = String.valueOf(courseDetails.get(CREDITS_COL_HEADER));
    String subjectName = (String) subjectMap.get(subjectId);

    String[] record = {crn, subjectName, num, description, section, type, credits, start, end, days, where, schedule, instructor};
    csvWriter.writeNext(record);
}

private String concatenateInstructors(JsonArray instructorArray) {
    StringBuilder instructorBuilder = new StringBuilder();
    for (int j = 0; j < instructorArray.size(); j++) {
        instructorBuilder.append(instructorArray.getString(j));
        if (j < instructorArray.size() - 1) {
            instructorBuilder.append(", ");
        }
    }
    return instructorBuilder.toString();
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