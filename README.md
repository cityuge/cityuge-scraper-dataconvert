# CityU AIMS Scraper Data Converter

After running the [CityU AIMS Scraper](https://github.com/swiftzer/cityu-aims-scraper)
for scraping the course add/drop status, there are many raw data files generated.
This program convert the raw data files into JSON files for each course
which are useful for visualizing on web or other further studies.

In this program, it reads the scraped CSV files one by one in ascending order and
write the records into the corresponding course file in CSV format.
Then, the program reads the course files and generate the course JSON files.

## Sample JSON output

````
{
  "code": "GE1127",
  "credit": 3,
  "department": "School of Creative Media",
  "levels": [
    "A",
    "B"
  ],
  "logRecords": [
    {
      "availableSeats": 12,
      "capacity": 16,
      "waitlistAvailable": "N",
      "webEnabled": true,
      "timestamp": 1439945710000
    },
    {
      "availableSeats": 12,
      "capacity": 16,
      "waitlistAvailable": "Y",
      "webEnabled": true,
      "timestamp": 1439945771000
    },
    {
      "availableSeats": 0,
      "capacity": 60,
      "waitlistAvailable": "FULL",
      "webEnabled": true,
      "timestamp": 1441670443000
    }
  ],
  "title": "Money and Art: Exchange and Transaction As Themes in Art-works"
}
````

Notes:

* Objects in `logRecords` are sorted by timestamp.
* `waitlistAvailable` can be `Y`, `N` or `FULL`.