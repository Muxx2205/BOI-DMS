package pmsby.bulk.delete.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

@Component(service = CSVReaderUtil.class)
public class CSVReaderUtil {

    private static final Log _log = LogFactoryUtil.getLog(CSVReaderUtil.class);

    /**
     * Read Object Entry IDs from CSV file
     * @param file The CSV file
     * @param columnName The column name containing IDs (default "objectEntryId")
     * @param delimiter The delimiter (default ",")
     * @return List of ID strings
     */
    public List<String> readIdsFromCSV(File file, String columnName, String delimiter) {
        List<String> ids = new ArrayList<>();
        
        if (columnName == null || columnName.isEmpty()) {
            columnName = "objectEntryId";
        }
        
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            int idColumnIndex = -1;
            
            // Read header line
            if ((line = br.readLine()) != null) {
                lineNumber++;
                String[] headers = parseCSVLine(line, delimiter);
                
                // Find ID column index
                for (int i = 0; i < headers.length; i++) {
                    if (columnName.equalsIgnoreCase(headers[i].trim())) {
                        idColumnIndex = i;
                        _log.info("Found ID column at index: " + idColumnIndex + " (Column: " + headers[i] + ")");
                        break;
                    }
                }
                
                if (idColumnIndex == -1) {
                    _log.error("ID column '" + columnName + "' not found in CSV header");
                    _log.info("Available columns: " + String.join(", ", headers));
                    return ids;
                }
            }
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] values = parseCSVLine(line, delimiter);
                
                if (idColumnIndex < values.length) {
                    String idValue = values[idColumnIndex].trim();
                    
                    // Remove quotes if present
                    if (idValue.startsWith("\"") && idValue.endsWith("\"")) {
                        idValue = idValue.substring(1, idValue.length() - 1);
                    }
                    
                    if (!idValue.isEmpty()) {
                        ids.add(idValue);
                    }
                }
                
                // Log progress every 50,000 lines
                if (lineNumber % 50000 == 0) {
                    _log.info("Read " + lineNumber + " lines, found " + ids.size() + " IDs");
                }
            }
            
            _log.info("CSV reading completed. Total lines: " + lineNumber + 
                     ", Valid IDs: " + ids.size());
            
        } catch (Exception e) {
            _log.error("Error reading CSV file: " + e.getMessage(), e);
        }
        
        return ids;
    }

    /**
     * Parse a CSV line handling quotes properly
     */
    private String[] parseCSVLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter.charAt(0) && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        
        result.add(field.toString());
        
        return result.toArray(new String[0]);
    }

    /**
     * Count lines in CSV file (fast estimate)
     */
    public int countLines(File file) {
        int lines = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) {
                lines++;
            }
        } catch (Exception e) {
            _log.error("Error counting lines", e);
        }
        return lines;
    }
}