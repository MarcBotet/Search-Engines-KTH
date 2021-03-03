# Search-Engines-KTH

This code was developed in three different assignments for the course DD2476 Search Engines and Information Retrieval Systems. 

In all the assignments I obtained the maximum grade (A).

The project implements a search engine system with the following characteristics:

- Inverse index construction (storing values in memory using a HashMap)
- Persistent index construction (Storing the index to disk using a dictionary and data file)
- Scalable persistent index (handling large indexes that cannot be fit in memory constructing different subindex and merging them in parallel)
- Boolean intersection search
- Phrase search
- Ranked search
  - TF-IDF (manhattan and euclidean normalization)
  - PageRank (iterative and Montecarlo PageRank)
  - TF-IDF-- PageRank combination
  - HITS (Hypertext Induced Topic Selection)
  
 - Relevance feedback (Rocchio algorithm)
 - K-gram index construction
 - Wildcard queries
 - Spelling correction for multiword queries
