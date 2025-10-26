# Assignment3 
Name: Alikhan Nurzhan
Group: SE-2429

Algorithm Performance Results (for test_large_10graphs.json)
Algorithm	Execution Time	  Theoretical Complexity	   Correctness
Kruskal's	 1171 ms	         O(E log E) + O(E α(V))	  10/10 perfect
Prim's	   1055 ms	            O(E log V)	          10/10 perfect
2. Algorithm Comparison
Theoretical vs Practical Performance

Theoretical Expectation:
For extremely sparse graphs (E ≈ V):

  Kruskal: O(V log V) + O(V α(V)) ≈ O(V log V)

  Prim: O(V log V)

  Expected: Similar performance, slight edge to Kruskal

Actual Results:

  Prim: 1055 ms, Kruskal: 1171 ms

  Prim 10.9% faster - contradicts theoretical expectation

  Performance ratio: 1.11 (Kruskal/Prim)

Analysis of Discrepancy:

  Implementation overhead: Kruskal's edge sorting and union-find operations may have higher constant factors

  Memory access: Prim's adjacency list may have better cache locality

  Java-specific factors: Collections sorting and object creation overhead in Kruskal

 Conclusions and Recommendations
Revised Algorithm Selection Guidelines

Choose Kruskal's Algorithm When:

  Theoretical simplicity is valued

  Edge list is the natural input format

  Implementing on systems with highly optimized sorting

  Dealing with extremely sparse graphs (in theory)

Choose Prim's Algorithm When:

  Practical performance is critical

  Working with Java or similar managed languages

  Graph representation can be pre-processed to adjacency lists

  Consistent performance across varying densities is needed

Surprising Finding:
Despite theoretical predictions favoring Kruskal for sparse graphs, Prim demonstrated better practical performance. This suggests that implementation details and language-specific optimizations can override theoretical advantages.
