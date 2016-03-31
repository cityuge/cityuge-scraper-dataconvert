package com.cityuge.dataconvert.data;

import java.util.List;

/**
 * The course add/drop log JSON object.
 */
public class CourseAddDropLog {
    public String code;
    public int credit;
    public String department;
    public String[] levels;
    public List<LogRecord> logRecords;
    public String title;

    public static class LogRecord {
        public int availableSeats;
        public int capacity;
        public String waitlistAvailable;
        public boolean webEnabled;
        public long timestamp;
    }
}
