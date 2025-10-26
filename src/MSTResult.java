import java.util.List;

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