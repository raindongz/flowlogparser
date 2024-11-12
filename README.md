# AWS VPC-Flow-log parser

### Introduction
* This is AWS VPC flow log parser which maps a plain text flow log file into a file contains Tag Counts and Port/Protocol Combination Counts based on the provided lookup table file.


### Assumption
* There will be three input files within same directory with the parser program:
    1. **flow_logs.txt**
    2. **lookup_table.csv**
    3. **protocol_numbers.csv**

* The output file will be **output_counts.csv** which contains Port/Protocol Combination Counts and Tag Counts
* Assume the program only supports default log format, not custom and the only version that is supported is 2.
* Assume for the "Port/Protocol Combination Counts", it means for each unique pairs of dstPort/Protocol, count the frequency for each pair.
* Assume there is no duplicate mapping for lookup_table file(example: exist both 31,udp,SV_P3 and 31,udp,SV_P2, if so the previous one will be overwritten).
* Assume for invalid row in input file, the default action is to skip this line.
* Assume the protocols in lookup_table file follows the guidline in **https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml** and are all lower cases.

### Environment

* MacBook M2
* Intellij idea
* Java 17 Amazon Corretto
* Git

### Exceptions

* Throw FileNotFoundException if flow_logs.txt or lookup_table.csv is not found.
* Throw custom FileIsEmptyException if flow_logs.txt or lookup_table.csv or protocol_numbers.csv is empty.
* Throw I/O Exception if any error happens during write to file process


### Instructions of how to run the program
1. download and set up JDK 17 Amazon Corretto.
2. put all input files into same directory with FlowLogParser.java
3. go to the directory where FlowLogParser.java located(src)
4. compile the program: javac FlowLogParser.java
5. Run the program: java FlowLogParser
6. **Note**: if you are using the intellij Idea default "Run" button, will need to add a "/src" in front of each file path.

### Test cases

1. success: tesed with 9.3MB flow log file and 10000 records lookup_table.csv file, works as expected.
2. failed: tested with empty flow log file, received "flow_logs.txt file is Empty" runtime exception, works as expected.
3. failed: tested with empty look table file, received "lookup_table file is empty" runtime exception, works as expected.
4. success: tested with some of invalid content in flow_logs.txt(number elements of some rows are not 14). skipped the corrsponded lins, works as expected.
5. success: tested with some of invalid content in lookup_table.csv(number elements of some rows are not 3). skipped the corrsponded lins, works as expected.