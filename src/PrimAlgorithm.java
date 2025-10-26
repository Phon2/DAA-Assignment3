import java.util.*;
import java.io.*;
import java.nio.file.*;


// Class to represent a node in the priority queue for Prim's algorithm
class PrimNode implements Comparable<PrimNode> {
    int vertex;
    int key; // Minimum weight to connect to MST
    int parent; // Parent vertex in MST

    public PrimNode(int vertex, int key, int parent) {
        this.vertex = vertex;
        this.key = key;
        this.parent = parent;
    }

    @Override
    public int compareTo(PrimNode other) {
        return this.key - other.key;
    }
}



public class PrimAlgorithm {

    // Function to find Minimum Spanning Tree using Prim's algorithm
    public static List<Edge> primMST(List<Edge> edges, int vertices) {
        // Convert node names to indices and build adjacency list
        Map<String, Integer> nodeToIndex = new HashMap<>();
        Map<Integer, String> indexToNode = new HashMap<>();
        int index = 0;

        // Build node to index mapping
        for (Edge edge : edges) {
            if (!nodeToIndex.containsKey(edge.from)) {
                nodeToIndex.put(edge.from, index);
                indexToNode.put(index, edge.from);
                index++;
            }
            if (!nodeToIndex.containsKey(edge.to)) {
                nodeToIndex.put(edge.to, index);
                indexToNode.put(index, edge.to);
                index++;
            }
        }

        // Build adjacency list
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adj.add(new ArrayList<>());
        }

        for (Edge edge : edges) {
            int u = nodeToIndex.get(edge.from);
            int v = nodeToIndex.get(edge.to);
            int weight = edge.weight;

            adj.get(u).add(new int[]{v, weight});
            adj.get(v).add(new int[]{u, weight});
        }

        // Prim's algorithm
        boolean[] inMST = new boolean[vertices];
        int[] parent = new int[vertices];
        int[] key = new int[vertices];

        Arrays.fill(key, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<PrimNode> pq = new PriorityQueue<>();
        key[0] = 0;
        pq.offer(new PrimNode(0, 0, -1));

        while (!pq.isEmpty()) {
            PrimNode node = pq.poll();
            int u = node.vertex;

            if (inMST[u]) continue;
            inMST[u] = true;

            for (int[] neighbor : adj.get(u)) {
                int v = neighbor[0];
                int weight = neighbor[1];

                if (!inMST[v] && weight < key[v]) {
                    key[v] = weight;
                    parent[v] = u;
                    pq.offer(new PrimNode(v, key[v], u));
                }
            }
        }

        // Build MST edges from parent array
        List<Edge> mst = new ArrayList<>();
        for (int i = 1; i < vertices; i++) {
            if (parent[i] != -1) {
                String fromNode = indexToNode.get(parent[i]);
                String toNode = indexToNode.get(i);
                mst.add(new Edge(fromNode, toNode, key[i]));
            }
        }

        return mst;
    }

    // Helper method to get node name from index (same as Kruskal)
    private static String getKeyFromValue(Map<String, Integer> map, int value) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Utility function to calculate total weight of MST (same as Kruskal)
    public static int getMSTWeight(List<Edge> mst) {
        return mst.stream().mapToInt(edge -> edge.weight).sum();
    }

    // File processing methods (same structure as Kruskal)
    public static List<MSTResult> processGraphsFromFile(String filename) {
        List<MSTResult> results = new ArrayList<>();

        try {
            // Read entire file
            String fullContent = new String(Files.readAllBytes(Paths.get(filename)))
                    .replaceAll("\\s+", " ");

            // Locate the "graphs" array
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

                // Parse graph ID
                int idStart = graphStr.indexOf("\"id\":") + 5;
                int idEnd = graphStr.indexOf(",", idStart);
                if (idEnd == -1) idEnd = graphStr.indexOf("}", idStart);
                int graphId = Integer.parseInt(graphStr.substring(idStart, idEnd).trim());

                // Parse nodes
                int nodesStart = graphStr.indexOf("\"nodes\":") + 8;
                int nodesEnd = graphStr.indexOf("]", nodesStart);
                String nodesStr = graphStr.substring(nodesStart, nodesEnd);
                String[] nodes = extractNodes(nodesStr);

                // Parse edges
                int edgesStart = graphStr.indexOf("\"edges\":") + 8;
                int edgesEnd = findMatchingBracket(graphStr, edgesStart, '[', ']');
                String edgesStr = graphStr.substring(edgesStart + 1, edgesEnd);
                List<Edge> edges = extractEdges(edgesStr);

                System.out.println("Processing Graph " + graphId + " with " + nodes.length + " nodes and " + edges.size() + " edges");

                // Use Prim's algorithm instead of Kruskal's
                List<Edge> mst = primMST(edges, nodes.length);
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

        // Regex-based parsing
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

    // Write results to JSON file (same as Kruskal)
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

    // Performance comparison method
    public static void compareWithKruskal(String filename) {
        System.out.println("\n=== Performance Comparison: Prim vs Kruskal ===");

        List<MSTResult> primResults = processGraphsFromFile(filename);
        List<MSTResult> kruskalResults = KruskalAlgorithm.processGraphsFromFile(filename);

        if (primResults.size() == kruskalResults.size()) {
            for (int i = 0; i < primResults.size(); i++) {
                MSTResult prim = primResults.get(i);
                MSTResult kruskal = kruskalResults.get(i);

                System.out.println("\nGraph " + prim.graphId + ":");
                System.out.println("  Prim's MST Weight: " + prim.totalWeight);
                System.out.println("  Kruskal's MST Weight: " + kruskal.totalWeight);
                System.out.println("  Weights Match: " + (prim.totalWeight == kruskal.totalWeight));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Prim's Algorithm with File I/O ===\n");

        // Process graphs using Prim's algorithm
        List<MSTResult> results = processGraphsFromFile("ass_3_input.json");

        // Display results
        for (MSTResult result : results) {
            System.out.println("\nGraph " + result.graphId + " Results:");
            System.out.println("  Total MST Weight: " + result.totalWeight);
            System.out.println("  MST Edges (" + result.mstEdges.size() + "):");
            for (Edge edge : result.mstEdges) {
                System.out.println("    " + edge);
            }
        }

        // Write all results to output file
        writeResultsToFile(results, "prim_output.json");

        System.out.println("\n✓ Prim's algorithm processing complete! Check prim_output.json for results.");

        // Optional: Compare with Kruskal
        // compareWithKruskal("ass_3_input.json");
    }
}