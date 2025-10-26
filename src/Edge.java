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