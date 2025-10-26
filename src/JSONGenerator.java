import java.util.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class JSONGenerator {

    public static void main(String[] args) {
        generateAllTestFiles();
    }

    public static void generateAllTestFiles() {
        // File 1: 5 small graphs
        generateTestFile("test_small_5graphs.json", 5, 5, 30);

        // File 2: 10 medium graphs  
        generateTestFile("test_medium_10graphs.json", 10, 30, 300);

        // File 3: 10 large graphs
        generateTestFile("test_large_10graphs.json", 10, 300, 1000);

        // File 4: 5 very large graphs
        generateTestFile("test_xlarge_5graphs.json", 5, 1000, 2000);
    }

    private static String getNodeName(int index) {
        StringBuilder name = new StringBuilder();
        while (index >= 0) {
            name.insert(0, (char)('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return name.toString();
    }

    private static void generateTestFile(String filename, int numGraphs, int minVertices, int maxVertices) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write("{\n  \"graphs\": [\n");

            for (int graphId = 1; graphId <= numGraphs; graphId++) {
                int vertices = ThreadLocalRandom.current().nextInt(minVertices, maxVertices + 1);
                int edges = (int)(vertices * (vertices - 1) * ThreadLocalRandom.current().nextDouble(0.1, 0.3));

                writer.write("    {\n");
                writer.write("      \"id\": " + graphId + ",\n");
                writer.write("      \"nodes\": [");

                // Generate nodes
                for (int i = 0; i < vertices; i++) {
                    writer.write("\"" + getNodeName(i) + "\"");
                    if (i < vertices - 1) writer.write(", ");
                }
                writer.write("],\n");

                // Generate edges
                writer.write("      \"edges\": [\n");
                Set<String> edgeSet = new HashSet<>();

                // Ensure connectivity with spanning tree
                for (int i = 1; i < vertices; i++) {
                    int from = ThreadLocalRandom.current().nextInt(i);
                    int weight = ThreadLocalRandom.current().nextInt(1, 100);
                    writer.write("        {\"from\": \"" + getNodeName(from) + "\", \"to\": \"" + getNodeName(i) + "\", \"weight\": " + weight + "}");
                    edgeSet.add(from + "-" + i);
                    if (i < vertices - 1 || edges > vertices - 1) writer.write(",\n");
                    else writer.write("\n");
                }

                // Add random edges
                int added = 0;
                int maxAttempts = edges * 10;
                int attempts = 0;

                while (added < edges - (vertices - 1) && attempts < maxAttempts) {
                    int from = ThreadLocalRandom.current().nextInt(vertices);
                    int to = ThreadLocalRandom.current().nextInt(vertices);
                    attempts++;

                    if (from != to && !edgeSet.contains(from + "-" + to) && !edgeSet.contains(to + "-" + from)) {
                        int weight = ThreadLocalRandom.current().nextInt(1, 100);
                        writer.write("        {\"from\": \"" + getNodeName(from) + "\", \"to\": \"" + getNodeName(to) + "\", \"weight\": " + weight + "}");
                        edgeSet.add(from + "-" + to);
                        added++;
                        if (added < edges - (vertices - 1)) writer.write(",\n");
                        else writer.write("\n");
                    }
                }

                writer.write("      ]\n");
                writer.write("    }");
                if (graphId < numGraphs) writer.write(",");
                writer.write("\n");

                System.out.println("Generated: " + filename + " - Graph " + graphId + " (" + vertices + " vertices, " + edgeSet.size() + " edges)");
            }

            writer.write("  ]\n}");
            writer.close();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}