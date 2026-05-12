package tools.vlab.kberry.core;

import java.util.Arrays;
import java.util.List;

public class CsvReader {

    private final List<String> headers;
    private final List<String[]> rows;
    private int currentRow = -1;

    private CsvReader(String csv) {
        String[] lines = csv.strip().split("\\r?\\n");
        this.headers = Arrays.asList(lines[0].split(","));
        this.rows = Arrays.stream(lines, 1, lines.length)
                .filter(line -> !line.isBlank())
                .map(line -> line.split(",", -1))
                .toList();
    }

    public static CsvReader build(String csv) {
        return new CsvReader(csv);
    }

    public boolean nextRow() {
        if (currentRow + 1 < rows.size()) {
            currentRow++;
            return true;
        }
        return false;
    }

    public String getCol(String columnName) {
        if (currentRow < 0) throw new IllegalStateException("Call nextRow() first");

        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equalsIgnoreCase(columnName.trim())) {
                String[] row = rows.get(currentRow);
                return i < row.length ? row[i].trim() : "";
            }
        }

        throw new IllegalArgumentException("Column not found: " + columnName);
    }
}