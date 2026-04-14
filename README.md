# Course Data Scraper
Written in Java using Microsoft Playwright and the org.json library.

## Dependencies
* Java JDK
* [Apache Maven](https://maven.apache.org/)

### Java Packages used:
* Playwright
* org.json

## Setup Steps
1. Clone or extract the project folder.
2. Open a terminal in the project root (where `pom.xml` is located).
3. Run: `mvn clean install`

## How to Run
1. In the terminal, run: `mvn exec:java`
2. The scraper will automatically extract the courses and terminate.

## Output Format
* **File:** Generates `coventry_courses_output.csv`.
* **Records:** Contains exactly 5 structured course objects.
* **Missing Data:** Unlisted fields gracefully default to `"NA"`.
* **Raw Text:** Complex fields (e.g., English requirements, accepted boards) capture raw text to prioritize correct information over strict formatting.
* **Integrity:** All data is extracted directly from official `coventry.ac.uk` web pages without using third-party sources.