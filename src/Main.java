import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Detailed MST Algorithms Analysis ===\n");

        String filename = "test_large_10graphs.json";
        detailedComparison(filename);

    }

    public static void detailedComparison(String filename) {
        System.out.println("Analyzing: " + filename);
        System.out.println("=".repeat(80));

        // Run algorithms
        List<MSTResult> kruskalResults = KruskalAlgorithm.processGraphsFromFile(filename);
        List<MSTResult> primResults = PrimAlgorithm.processGraphsFromFile(filename);

        if (kruskalResults == null || primResults == null ||
                kruskalResults.size() != primResults.size()) {
            System.out.println("Error: Cannot compare - results are invalid");
            return;
        }

        // Performance testing with multiple runs
        int runs = 3;
        long totalKruskalTime = 0;
        long totalPrimTime = 0;

        for (int run = 1; run <= runs; run++) {
            System.out.println("\nRun #" + run + ":");

            long kStart = System.nanoTime();
            KruskalAlgorithm.processGraphsFromFile(filename);
            long kEnd = System.nanoTime();
            long kTime = (kEnd - kStart) / 1000000;
            totalKruskalTime += kTime;

            long pStart = System.nanoTime();
            PrimAlgorithm.processGraphsFromFile(filename);
            long pEnd = System.nanoTime();
            long pTime = (pEnd - pStart) / 1000000;
            totalPrimTime += pTime;

            System.out.println("  Kruskal: " + kTime + " ms, Prim: " + pTime + " ms");
        }

        long avgKruskalTime = totalKruskalTime / runs;
        long avgPrimTime = totalPrimTime / runs;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("FINAL RESULTS FOR: " + filename);
        System.out.println("=".repeat(80));

        System.out.printf("Average Performance (%d runs):\n", runs);
        System.out.printf("  Kruskal's Algorithm: %d ms\n", avgKruskalTime);
        System.out.printf("  Prim's Algorithm:    %d ms\n", avgPrimTime);
        System.out.printf("  Difference:          %d ms (%s)\n",
                Math.abs(avgKruskalTime - avgPrimTime),
                avgKruskalTime < avgPrimTime ? "Kruskal faster" : "Prim faster");

        // Detailed graph-by-graph analysis
        System.out.println("\nGraph-by-Graph Analysis:");
        System.out.println("-".repeat(80));

        int perfectMatches = 0;
        int totalVertices = 0;
        int totalEdges = 0;

        for (int i = 0; i < kruskalResults.size(); i++) {
            MSTResult k = kruskalResults.get(i);
            MSTResult p = primResults.get(i);

            boolean weightsMatch = k.totalWeight == p.totalWeight;
            boolean edgeCountMatch = k.mstEdges.size() == p.mstEdges.size();

            if (weightsMatch && edgeCountMatch) {perfectMatches++;};

            System.out.printf("Graph %d:\n", k.graphId);
            System.out.printf("  MST Weight: Kruskal=%d, Prim=%d %s\n",
                    k.totalWeight, p.totalWeight, weightsMatch ? "✓" : "✗ MISMATCH!");
            System.out.printf("  Edge Count: Kruskal=%d, Prim=%d %s\n",
                    k.mstEdges.size(), p.mstEdges.size(), edgeCountMatch ? "✓" : "✗");

            // Count vertices and edges for statistics
            totalVertices += estimateVertices(k.mstEdges);
            totalEdges += k.mstEdges.size();

            // Show first few edges for verification
            if (k.mstEdges.size() <= 10) {
                System.out.println("  Kruskal MST Edges: " + k.mstEdges);
                System.out.println("  Prim MST Edges:    " + p.mstEdges);
            } else {
                System.out.println("  Kruskal MST Edges: [showing first 5] " +
                        k.mstEdges.subList(0, Math.min(5, k.mstEdges.size())));
                System.out.println("  Prim MST Edges:    [showing first 5] " +
                        p.mstEdges.subList(0, Math.min(5, p.mstEdges.size())));
            }
            System.out.println();
        }

        // Summary statistics
        System.out.println("SUMMARY STATISTICS:");
        System.out.println("-".repeat(40));
        System.out.println("Total graphs processed: " + kruskalResults.size());
        System.out.println("Perfect matches: " + perfectMatches + "/" + kruskalResults.size());
        System.out.printf("Average vertices per graph: %.1f\n", (double)totalVertices / kruskalResults.size());
        System.out.printf("Average edges per graph: %.1f\n", (double)totalEdges / kruskalResults.size());
        System.out.printf("Performance ratio: %.2f (Kruskal/Prim)\n", (double)avgKruskalTime / avgPrimTime);

    }

    private static int estimateVertices(List<Edge> edges) {
        Set<String> vertices = new HashSet<>();
        for (Edge edge : edges) {
            vertices.add(edge.from);
            vertices.add(edge.to);
        }
        return vertices.size();
    }

}