import java.util.*;
import java.io.*;
import java.nio.file.*;

// Class to represent an edge in the graph
class Edge implements Comparable<Edge> {
    int src, dest, weight;
    String from, to; // Store original node names

    public Edge(int src, int dest, int weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    public Edge(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return this.weight - other.weight;
    }

    @Override
    public String toString() {
        if (from != null && to != null) {
            return from + " - " + to + " (" + weight + ")";
        } else {
            return src + " - " + dest + " (" + weight + ")";
        }
    }
}

// Class for Union-Find data structure
class UnionFind {
    private int[] parent;
    private int[] rank;

    public UnionFind(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX != rootY) {
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }
}

// Class to represent MST result for output
class MSTResult {
    public int graphId;
    public int totalWeight;
    public List<Edge> mstEdges;

    public MSTResult(int graphId, int totalWeight, List<Edge> mstEdges) {
        this.graphId = graphId;
        this.totalWeight = totalWeight;
        this.mstEdges = mstEdges;
    }
}

public class KruskalAlgorithm {

    // Function to find Minimum Spanning Tree using Kruskal's algorithm
    public static List<Edge> kruskalMST(List<Edge> edges, int vertices) {
        // Convert node names to indices
        List<Edge> indexedEdges = new ArrayList<>();
        Map<String, Integer> nodeToIndex = new HashMap<>();
        int index = 0;

        // Build node to index mapping
        for (Edge edge : edges) {
            if (!nodeToIndex.containsKey(edge.from)) {
                nodeToIndex.put(edge.from, index++);
            }
            if (!nodeToIndex.containsKey(edge.to)) {
                nodeToIndex.put(edge.to, index++);
            }
        }

        // Create edges with indices
        for (Edge edge : edges) {
            int srcIndex = nodeToIndex.get(edge.from);
            int destIndex = nodeToIndex.get(edge.to);
            indexedEdges.add(new Edge(srcIndex, destIndex, edge.weight));
        }

        // Kruskal's algorithm
        Collections.sort(indexedEdges);
        List<Edge> mst = new ArrayList<>();
        UnionFind uf = new UnionFind(vertices);

        int edgesAdded = 0;
        int i = 0;

        while (edgesAdded < vertices - 1 && i < indexedEdges.size()) {
            Edge nextEdge = indexedEdges.get(i++);
            int rootSrc = uf.find(nextEdge.src);
            int rootDest = uf.find(nextEdge.dest);

            if (rootSrc != rootDest) {
                // Convert back to original node names for output
                Edge originalEdge = new Edge(
                        getKeyFromValue(nodeToIndex, nextEdge.src),
                        getKeyFromValue(nodeToIndex, nextEdge.dest),
                        nextEdge.weight
                );
                mst.add(originalEdge);
                uf.union(rootSrc, rootDest);
                edgesAdded++;
            }
        }

        return mst;
    }

    // Helper method to get node name from index
    private static String getKeyFromValue(Map<String, Integer> map, int value) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Utility function to calculate total weight of MST
    public static int getMSTWeight(List<Edge> mst) {
        return mst.stream().mapToInt(edge -> edge.weight).sum();
    }

    public static List<MSTResult> processGraphsFromFile(String filename) {
        List<MSTResult> results = new ArrayList<>();

        try {
            // Read entire file
            String fullContent = new String(Files.readAllBytes(Paths.get(filename)))
                    .replaceAll("\\s+", " ");

            // ✅ Locate the "graphs" array
            int graphsStart = fullContent.indexOf("\"graphs\"");
            if (graphsStart == -1) {
                System.err.println("Error: No 'graphs' array found in input file!");
                return results;
            }

            int arrayStart = fullContent.indexOf("[", graphsStart);
            int arrayEnd = findMatchingBracket(fullContent, arrayStart, '[', ']');
            String graphsArray = fullContent.substring(arrayStart + 1, arrayEnd);

            int pos = 0;
            while ((pos = graphsArray.indexOf("{", pos)) != -1) {
                int graphEnd = findMatchingBracket(graphsArray, pos, '{', '}');
                String graphStr = graphsArray.substring(pos, graphEnd + 1);

                // ✅ Parse graph ID
                int idStart = graphStr.indexOf("\"id\":") + 5;
                int idEnd = graphStr.indexOf(",", idStart);
                if (idEnd == -1) idEnd = graphStr.indexOf("}", idStart);
                int graphId = Integer.parseInt(graphStr.substring(idStart, idEnd).trim());

                // ✅ Parse nodes
                int nodesStart = graphStr.indexOf("\"nodes\":") + 8;
                int nodesEnd = graphStr.indexOf("]", nodesStart);
                String nodesStr = graphStr.substring(nodesStart, nodesEnd);
                String[] nodes = extractNodes(nodesStr);

                // ✅ Parse edges (robust)
                int edgesStart = graphStr.indexOf("\"edges\":") + 8;
                int edgesEnd = findMatchingBracket(graphStr, edgesStart, '[', ']');
                String edgesStr = graphStr.substring(edgesStart + 1, edgesEnd);
                List<Edge> edges = extractEdges(edgesStr);

                System.out.println("Processing Graph " + graphId + " with " + nodes.length + " nodes and " + edges.size() + " edges");

                List<Edge> mst = kruskalMST(edges, nodes.length);
                int totalWeight = getMSTWeight(mst);
                results.add(new MSTResult(graphId, totalWeight, mst));

                pos = graphEnd + 1;
            }

        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }



    private static String[] extractNodes(String nodesStr) {
        List<String> nodes = new ArrayList<>();
        String[] parts = nodesStr.split(",");
        for (String part : parts) {
            String node = part.trim().replace("\"", "");
            if (!node.isEmpty()) {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[0]);
    }

    private static List<Edge> extractEdges(String edgesStr) {
        List<Edge> edges = new ArrayList<>();

        // Normalize whitespace and remove trailing brackets or commas
        String clean = edgesStr.replaceAll("\\s+", " ").trim();

        // ✅ Regex-based parsing (much more reliable)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\\{\\s*\"from\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"to\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"weight\"\\s*:\\s*(\\d+)\\s*\\}"
        );
        java.util.regex.Matcher matcher = pattern.matcher(clean);

        while (matcher.find()) {
            String from = matcher.group(1);
            String to = matcher.group(2);
            int weight = Integer.parseInt(matcher.group(3));
            edges.add(new Edge(from, to, weight));
        }

        System.out.println("  → Parsed " + edges.size() + " edges");
        return edges;
    }


    private static int findMatchingBracket(String str, int start, char open, char close) {
        int count = 1;
        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == open) count++;
            else if (c == close) count--;

            if (count == 0) return i;
        }
        return str.length();
    }


    // Write results to JSON file
    public static void writeResultsToFile(List<MSTResult> results, String filename) {
        try {
            FileWriter writer = new FileWriter(filename);

            writer.write("{\n");
            writer.write("  \"results\": [\n");

            for (int i = 0; i < results.size(); i++) {
                MSTResult result = results.get(i);

                writer.write("    {\n");
                writer.write("      \"graphId\": " + result.graphId + ",\n");
                writer.write("      \"totalWeight\": " + result.totalWeight + ",\n");
                writer.write("      \"mstEdges\": [\n");

                for (int j = 0; j < result.mstEdges.size(); j++) {
                    Edge edge = result.mstEdges.get(j);
                    writer.write("        {\n");
                    writer.write("          \"from\": \"" + edge.from + "\",\n");
                    writer.write("          \"to\": \"" + edge.to + "\",\n");
                    writer.write("          \"weight\": " + edge.weight + "\n");
                    writer.write("        }");
                    if (j < result.mstEdges.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }

                writer.write("      ]\n");
                writer.write("    }");
                if (i < results.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }

            writer.write("  ]\n");
            writer.write("}\n");

            writer.close();
            System.out.println("✓ Results successfully written to: " + filename);

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Kruskal's Algorithm with Simple File I/O ===\n");

        // Try to read from file, fall back to hardcoded data if it fails
        List<MSTResult> results = processGraphsFromFile("ass_3_input.json");

        // Display results
        for (MSTResult result : results) {
            System.out.println("\nGraph " + result.graphId + " Results:");
            System.out.println("  Total MST Weight: " + result.totalWeight);
            System.out.println("  MST Edges:");
            for (Edge edge : result.mstEdges) {
                System.out.println("    " + edge);
            }
        }

        // Write all results to output file
        writeResultsToFile(results, "ass_3_output.json");

        System.out.println("\n✓ Processing complete! Check ass_3_output.json for results.");
    }
}