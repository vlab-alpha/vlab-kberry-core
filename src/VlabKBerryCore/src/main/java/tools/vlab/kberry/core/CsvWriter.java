package tools.vlab.kberry.core;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CsvWriter {

    @Getter
    private final Path file;
    private final List<String> columns;
    private final Map<String, String> currentRow = new LinkedHashMap<>();

    private CsvWriter(Path file, String... columns) {
        this.file = file;
        this.columns = List.of(columns);
        writeHeader();
    }

    public static CsvWriter build(Path path, String... columns) {
        return new CsvWriter(path, columns);
    }

    public static CsvWriter build(String filename, String... columns) {
        return new CsvWriter(Path.of(filename), columns);
    }

    private void writeHeader() {
        try {
            if (Files.exists(file)) return;
            Files.writeString(file, String.join(",", columns) + "\n",
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV header", e);
        }
    }

    public CsvWriter setCol(String column, Object value) {
        currentRow.put(column, value != null ? value.toString() : "");
        return this;
    }

    public void writeLine() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(currentRow.getOrDefault(columns.get(i), ""));
        }
        sb.append("\n");

        try {
            Files.writeString(file, sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV line", e);
        }

        currentRow.clear();
    }

}