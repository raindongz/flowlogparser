import custom_exceptions.FileIsEmptyException;

import java.io.*;
import java.util.*;

/**
 * @Author: RUNDONG ZHONG
 * @Date: NOV/12/2024
 * @Param1: protocol_numbers.csv
 * @Param2: lookup_table.csv
 * @Param3: flow_logs.txt
 * @Description: This program will read all three input files and parse flow logs to a output.csv which will contains the count of matched tag and the count the frequency of unique combination
 * @Thorws: FileNotFoundException if flow_logs.txt or lookup_table.csv is not found.
 * Custom FileIsEmptyException if flow_logs.txt or lookup_table.csv or protocol_numbers.csv is empty.
 * I/O Exception if any error happens during write to file process.
 */
public class FlowLogParser {

    // this hash map will store the tags read from lookup table, key is combination of (dstport,protocol), value is corresponded tag.
    private static final Map<String, String> tagLookup = new HashMap<>();

    // this hash map will store the tag counts which will be printed to the output file.
    private static final Map<String, Integer> tagCounts = new HashMap<>();

    // this hash map will store the unique combination of (dstport,protocol) count and will be written to the output file.
    private static final Map<String, Integer> portProtocolCounts = new HashMap<>();

    // this hash map will read the protocol list input file and store the protocol number and protocol name, key is protocol number as string, value is protocol name.
    private static final Map<String, String> protocolsMap = new HashMap<>();

    public static void main(String[] args) {
        String protocolNumFile = new File("").getAbsolutePath()+"/protocol_numbers.csv";
        String lookupFile = new File("").getAbsolutePath()+"/lookup_table.csv";
        String flowLogFile = new File("").getAbsolutePath()+"/flow_logs.txt";
        String outputFile = new File("").getAbsolutePath()+"/output.csv";

        try {
            loadProtocolNumber(protocolNumFile);
            loadTagLookup(lookupFile);
            parseFlowLogs(flowLogFile);
            writeOutput(outputFile);
        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    private static void loadProtocolNumber(String protocolNumFile){
        Scanner scanner = null;
        // 1. if file not found throw exception
        try {
            scanner = new Scanner(new File(protocolNumFile));
        } catch (FileNotFoundException e) {
            System.err.println("Error reading protocal number file" + e.getMessage());
            throw new RuntimeException(e);
        }

        // 2. if file is empty throw custom exception
        if(!scanner.hasNextLine()){
            System.err.println("protocol number file is empty");
            throw new FileIsEmptyException("protocol_number.csv file is Empty");
        }

        // 3. skip the first header line(column name, example: Decimal, keywords... )
        scanner.nextLine();

        // 4. for each row of protocol number, put that into a hashmap. protocol number as key and protocol name as value.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip lines that do not start with a number(to skip the invalid line)
            if (!line.matches("^\\d.*")) {
                continue;
            }
            String[] parts = line.split(",");
            if(parts[1].isBlank()) {
                continue;
            }
            String protocolNumber = parts[0].trim();
            String protocolName = parts[1].trim().toLowerCase();
            protocolsMap.put(protocolNumber, protocolName);
        }
    }

    // Load the lookup table using Scanner
    private static void loadTagLookup(String lookupFile) {
        Scanner scanner = null;
        // 1. if file not found throw exception
        try {
            scanner = new Scanner(new File(lookupFile));
        } catch (FileNotFoundException e) {
            System.err.println("Error reading lookup table" + e.getMessage());
            throw new RuntimeException(e);
        }

        // 2. if file is empty throw custom exception
        if(!scanner.hasNextLine()){
            System.err.println("lookup_table file is empty");
            throw new FileIsEmptyException("lookup_table.csv file is Empty");
        }

        // 3. skip the first header line(column name, example: dstport,protocol,tag )
        scanner.nextLine();

        // 4. for each row of lookup table, put that into a hashmap. dstPort,protocol as key and tag as value.
        while (scanner.hasNextLine()) {
            // 4.1 get rid of potential Non-Breaking Space (NBSP)
            String line = scanner.nextLine().replace("\u00A0","");
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String dstPort = parts[0].trim();
                String protocol = parts[1].trim().toLowerCase();
                String tag = parts[2].trim();
                String portProtocol = dstPort + "," + protocol;
                tagLookup.put(portProtocol, tag);
            }
        }

    }

    // Parse the flow logs using Scanner and update counts
    private static void parseFlowLogs(String flowLogFile) {
        Scanner scanner = null;
        // 1. if file not found throw exception
        try {
            scanner = new Scanner(new File(flowLogFile));
        } catch (FileNotFoundException e) {
            System.err.println("Error reading flow log file" + e.getMessage());
            throw new RuntimeException(e);
        }

        // 2. if file is empty throw custom exception
        if(!scanner.hasNextLine()){
            System.err.println("Flow logs file is empty");
            throw new FileIsEmptyException("flow_logs.txt file is Empty");
        }

        // 3. read each row in flow log, check with protocol table and combine the dstPort and protocol,
        // update the tagCounts map and portProtocolCounts map.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // 3.1 split the row with white space
            String[] parts = line.trim().split("\\s+");
            // 3.2 Skip invalid lines
            if (parts.length != 14) {
                continue;
            }

            String dstPort = parts[6];
            String protocol = protocolsMap.getOrDefault(parts[7], "unknown").toLowerCase();
            String portProtocol = dstPort + "," + protocol;

            // Retrieve tag or mark as "Untagged"
            String tag = tagLookup.getOrDefault(portProtocol, "Untagged");

            // Update tag count
            tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);

            // Update port/protocol combination count
            portProtocolCounts.put(portProtocol, portProtocolCounts.getOrDefault(portProtocol, 0) + 1);
        }
    }


    // Write output to a output.csv file, if file not exist, create a new file, otherwise overwrite
    private static void writeOutput(String outputFile) throws IOException {
        File file = new File(outputFile);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Tag Counts:\n");
            writer.write("Tag,Count\n");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }

            writer.write("\nPort/Protocol Combination Counts:\n");
            writer.write("Port,Protocol,Count\n");
            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                String[] portProtocol = entry.getKey().split(",");
                writer.write(portProtocol[0] + "," + portProtocol[1] + "," + entry.getValue() + "\n");
            }
        }

    }
}
