package com.cityuge.dataconvert;

import com.cityuge.dataconvert.data.CourseAddDropLog;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert the scraped AIMS add/drop data in CSV format to JSON format.
 * <p>
 * This program reads the scraped CSV files one by one in ascending order and write the records into the corresponding
 * course file in CSV format. Then, the program reads the course files and generate the course JSON files. The generated
 * JSON files are useful for visualizing on web or other further studies.
 * <p>
 * When running this program, it is assumed that the timezone of the device is set as Hong Kong Time.
 */
public class DataConvert {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Missing path argument");
            return;
        }

        // Clean files
        deleteFolder(Paths.get(args[0], "/intermediates").toFile());
        deleteFolder(Paths.get(args[0], "/products").toFile());

        // Read CSV files
        Files.walk(Paths.get(args[0]))
                .filter(path -> FileSystems.getDefault().getPathMatcher("glob:*.csv").matches(path.getFileName()))
                .sorted((o1, o2) -> {
                    Calendar o1Cal = parseTimestampInFileName(o1.getFileName().toString());
                    Calendar o2Cal = parseTimestampInFileName(o2.getFileName().toString());
                    if (o1Cal != null && o2Cal != null) {
                        return o1Cal.compareTo(o2Cal);
                    }
                    return 0;
                })
                .forEach(path -> {
                    System.out.println("Creating intermediates: " + path);
                    try {
                        Calendar currentFileTime = parseTimestampInFileName(path.getFileName().toString());
                        CSVReader csvReader = new CSVReader(new FileReader(path.toFile()));
                        String[] nextLine;
                        boolean isFirstLine = true;
                        while ((nextLine = csvReader.readNext()) != null) {
                            if (isFirstLine) {
                                isFirstLine = false;
                                continue;
                            }
                            Path outputPath = Paths.get(args[0], "/intermediates", nextLine[2] + ".csv");
                            Files.createDirectories(outputPath.getParent());
                            CSVWriter csvWriter = new CSVWriter(new FileWriter(outputPath.toFile(), true));
                            String[] writeLine = Arrays.copyOf(nextLine, nextLine.length + 1);
                            writeLine[nextLine.length] = Long.toString(currentFileTime.getTimeInMillis());
                            csvWriter.writeNext(writeLine);
                            csvWriter.close();
                        }
                        csvReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        // Write JSON files
        Gson gson = new Gson();
        Files.walk(Paths.get(args[0], "/intermediates"))
                .filter(path -> FileSystems.getDefault().getPathMatcher("glob:*.csv").matches(path.getFileName()))
                .forEach(path -> {
                    System.out.println("Creating product: " + path);
                    try {
                        CSVReader csvReader = new CSVReader(new FileReader(path.toFile()));
                        Path outputPath = Paths.get(args[0], "/products", path.getFileName().toString().toLowerCase().replace(".csv", ".json"));
                        Files.createDirectories(outputPath.getParent());
                        FileWriter fileWriter = new FileWriter(outputPath.toFile());
                        CourseAddDropLog courseAddDropLog = new CourseAddDropLog();
                        courseAddDropLog.logRecords = new LinkedList<>();
                        String[] currLine;
                        String[] prevLine = null;
                        while ((currLine = csvReader.readNext()) != null) {
                            CourseAddDropLog.LogRecord logRecord = new CourseAddDropLog.LogRecord();
                            logRecord.availableSeats = Integer.parseInt(currLine[0]);
                            logRecord.capacity = Integer.parseInt(currLine[1]);
                            logRecord.waitlistAvailable = currLine[7];
                            logRecord.webEnabled = currLine[8].equals("true");
                            logRecord.timestamp = Long.parseLong(currLine[9]);
                            courseAddDropLog.logRecords.add(logRecord);
                            prevLine = currLine;
                        }
                        if (prevLine != null) {
                            courseAddDropLog.code = prevLine[2];
                            courseAddDropLog.credit = Integer.parseInt(prevLine[3]);
                            courseAddDropLog.department = prevLine[4];
                            String[] levels = prevLine[5].split(",");
                            Arrays.sort(levels);
                            courseAddDropLog.levels = levels;
                            courseAddDropLog.title = prevLine[6];
                            fileWriter.write(gson.toJson(courseAddDropLog, CourseAddDropLog.class));
                            csvReader.close();
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        System.out.println("\nAll done!");
    }

    /**
     * Extract the timestamp stated in the file name.
     *
     * @param fileName the file name of the CSV file
     * @return calendar object with parsed timestamp
     */
    private static Calendar parseTimestampInFileName(String fileName) {
        final Pattern pattern = Pattern.compile("(\\d+)-(\\d+)-(\\d+)_(\\d+)-(\\d+)-(\\d+).*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            return new GregorianCalendar(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)) - 1,
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5)),
                    Integer.parseInt(matcher.group(6))
            );
        }
        return null;
    }

    /**
     * Delete folders recursively.
     *
     * @param folder the folder to be deleted
     */
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
