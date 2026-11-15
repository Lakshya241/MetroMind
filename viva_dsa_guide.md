# MetroMind: Algorithms, Data Structures (DSA) & Viva Defense Guide
## Project Code: CP-042 | Bengaluru Namma Metro Edition (BMRCL)

[📖 Main Project README](README.md) • [📘 Beginner's Guide (explain.md)](explain.md)

---

## 1. Core Algorithms Used

### 1.1 Automated Best-Route Optimization Algorithm
* **File Reference**: [`RouteEngine.java`](src/route/RouteEngine.java) $\rightarrow$ `findBestRoute()`
* **Purpose**: Automatically evaluates, scores, and delivers the single **Best Recommended Route** for the commuter without requiring them to decipher complex algorithmic choices.
* **Mechanism**:
  - Computes candidate paths across three dimensions:
    1. $R_{\text{time}}$: Fastest Travel Time Path
    2. $R_{\text{transfers}}$: Fewest Interchanges Path
    3. $R_{\text{distance}}$: Shortest Physical Distance Path
  - Evaluates each candidate route using a **Commuter Friction Cost Function**:
    $$\text{Score} = \text{Travel Time (minutes)} + (\text{Interchange Count} \times 7.0) + (\text{Distance (km)} \times 0.15)$$
  - **Why this works**: Platform transfers in crowded metro systems add high physical friction (walking between levels, climbing stairs, waiting for connecting trains) equivalent to ~7 minutes of travel stress. However, if staying on one line causes an extreme detour (e.g., +25 km), the formula automatically selects the faster route. The route with the lowest overall friction score is returned as the **Best Recommended Route**.

---

### 1.2 Dijkstra’s Algorithm (Shortest Physical Distance)
* **File Reference**: [`RouteEngine.java`](src/route/RouteEngine.java) $\rightarrow$ `dijkstraDistance()`
* **Purpose**: Computes the optimal path minimizing total track kilometers.
* **Mechanism**:
  - Uses a **Min-Priority Queue** (`PriorityQueue<PathNode>`) to greedily select the station with the smallest known cumulative distance.
  - Applies **Edge Relaxation**:
    $$\text{if } (d[u] + \text{weight}(u, v) < d[v]) \implies d[v] = d[u] + \text{weight}(u, v)$$
  - Terminates search once the destination station is extracted from the priority queue.
* **Time Complexity**: $\mathcal{O}((V + E) \log V)$

---

### 1.3 State-Expanded Dijkstra’s Algorithm (Fastest Travel Time)
* **File Reference**: [`RouteEngine.java`](src/route/RouteEngine.java) $\rightarrow$ `dijkstraTime()`
* **Purpose**: Finds the fastest route in minutes, accurately modeling the physical delay of switching trains at interchange stations (e.g., *Majestic*, *MG Road*, or *Central Silk Board*).
* **The Problem with Standard Graphs**: Arriving at an interchange station on one line and staying on that same line incurs $0$ transfer delay. Switching lines requires walking between platforms, staircases, and waiting for the connecting train ($+4.0$ minutes). Standard graphs cannot represent this because nodes only represent stations, not line transitions.
* **Mechanism**:
  - State space is expanded to a tuple:
    $$\text{State} = (\text{Station}, \text{CurrentLine})$$
  - Cost function dynamically adds transfer penalties:
    $$\text{Edge Weight} = \text{Track Time} + \begin{cases} 0, & \text{if } L_{\text{current}} == L_{\text{next}} \\ \text{Transfer Penalty (4.0 min)}, & \text{if } L_{\text{current}} \ne L_{\text{next}} \end{cases}$$
* **Time Complexity**: $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ where $L$ is number of lines ($L \le 5$).

---

### 1.4 Lexicographical Multi-Criteria Optimization (Fewest Interchanges)
* **File Reference**: [`RouteEngine.java`](src/route/RouteEngine.java) $\rightarrow$ `dijkstraFewestInterchanges()`
* **Purpose**: Prioritizes minimizing platform transfers for passengers with luggage or during peak hours.
* **Mechanism**:
  - Employs a composite priority function:
    $$\text{Cost} = (\text{Total Transfers} \times 10,000) + \text{Distance in km}$$
  - The multiplier ($10,000$) guarantees that any route with $k$ transfers strictly dominates any route with $k+1$ transfers.
  - Distance serves as an automatic tie-breaker among routes sharing the exact same minimum transfer count.

---

### 1.5 Terminal Graphical Route Rendering Algorithm
* **File Reference**: [`AsciiRouteVisualizer.java`](src/util/AsciiRouteVisualizer.java) $\rightarrow$ `renderGraphicalRoute()`
* **Purpose**: Generates an intuitive ASCII/Unicode schematic diagram directly in the console.
* **Mechanism**:
  - Scans sequential segments and edge connections.
  - Emits milestone nodes (`[START] *`, intermediate `o`, junction markers `[Junction]`).
  - Detects line transitions and injects a prominent **Transfer Bridge Box** displaying the disembarking line, boarding line, and platform buffer minutes.
  - Terminates at `[DESTINATION] #` with an aggregate commute metrics summary.

---

### 1.6 Slab-Based Range Resolution Algorithm
* **File Reference**: [`FareEngine.java`](src/fare/FareEngine.java) $\rightarrow$ `getSlabForDistance()`
* **Mechanism**:
  - Matches cumulative distance against BMRCL distance slabs:
    $$[0.0 - 2.0\text{ km}], [2.1 - 5.0\text{ km}], [5.1 - 12.0\text{ km}], [12.1 - 21.0\text{ km}], [21.1 - 32.0\text{ km}], [> 32.0\text{ km}]$$
  - Applies corridor surcharges (+₹20 for *KIAL Airport Terminal*) and concession discount matrices.

---

## 2. Core Data Structures (DSA) Used

| Data Structure | Java Type | File | Justification & Complexity |
| :--- | :--- | :--- | :--- |
| **Weighted Graph** | Adjacency List (`Map<Station, List<Connection>>`) | `MetroGraph.java` | Metro networks are sparse graphs ($E \ll V^2$). Space complexity is $\mathcal{O}(V + E)$ vs $\mathcal{O}(V^2)$ for an Adjacency Matrix. Neighbor exploration is $\mathcal{O}(\text{degree}(u))$. |
| **Min-Priority Queue** | `PriorityQueue<PathNode>` (Binary Min-Heap) | `RouteEngine.java` | Provides $\mathcal{O}(\log V)$ extraction of minimum cost node during Dijkstra's search. |
| **Hash Map** | `HashMap<Station, Double>`, `HashMap<String, Double>` | `RouteEngine.java` | Allows $\mathcal{O}(1)$ average-time lookup and update of visited states and minimum costs. |
| **Self-Balancing BST** | `TreeMap<String, Station>` | `MetroGraph.java` | Maintains station directory sorted alphabetically with $\mathcal{O}(\log V)$ case-insensitive search. |
| **Hash Set** | `HashSet<MetroLine>` | `Station.java` | Provides $\mathcal{O}(1)$ membership testing to check if a station is an interchange hub (`lines.size() > 1`). |
| **Dynamic Array** | `ArrayList<Station>`, `ArrayList<Connection>` | Throughout models | Fast $\mathcal{O}(1)$ random access and cache-friendly sequential iteration for path reconstruction and visual rendering. |
| **Enumerations** | `enum MetroLine`, `enum TicketType` | `model/` | Type-safe domain models encapsulating line names, transfer penalties, and discount percentages. |

---

## 3. Top Viva & Code Review Questions & Answers

### Q1: Why did you choose Dijkstra’s Algorithm instead of BFS or DFS?
> **Answer**:
> "BFS (Breadth-First Search) only finds the shortest path in unweighted graphs where every edge has the exact same weight. In a real metro system, tracks between stations have varying physical distances (e.g., 1.2 km vs 6.5 km) and travel times. DFS does not guarantee the shortest path at all. Dijkstra’s Algorithm is mathematically optimal because it systematically handles weighted, non-negative edges."

---

### Q2: Why use an Adjacency List instead of an Adjacency Matrix?
> **Answer**:
> "The Bengaluru Metro network is a sparse graph. With 63 stations, each station connects to only 2 to 4 adjacent stations.
> - An **Adjacency Matrix** would require $63 \times 63 = 3,969$ memory cells, where over 95% are empty zeroes ($\mathcal{O}(V^2)$ space).
> - An **Adjacency List** uses only $\mathcal{O}(V + E)$ space. Moreover, iterating over a station's adjacent connections takes $\mathcal{O}(\text{degree}(u))$, which is far faster than scanning an entire matrix row of size $V$."

---

### Q3: How does your system determine the "Best Route"?
> **Answer**:
> "In `RouteEngine.java`, the `findBestRoute()` method computes candidate paths across Fastest Time, Fewest Transfers, and Shortest Distance. It evaluates each candidate using a commuter friction function:
> $$\text{Score} = \text{Travel Time} + (\text{Interchanges} \times 7.0) + (\text{Distance} \times 0.15)$$
> This automatically penalizes line transfers (since walking between platforms adds physical effort and delay) while ensuring commuters aren't sent on massive physical detours. The route with the lowest friction score is delivered directly to the user."

---

### Q4: How is the Graphical Route represented in the terminal?
> **Answer**:
> "In `AsciiRouteVisualizer.java`, we implemented a text-mode graphical schematic using Unicode box-drawing characters (`+`, `|`, `#`, `*`, `o`). It renders:
> 1. A **Corridor Overview** summarizing line legs and stop counts.
> 2. A **Detailed Track Schematic** displaying origin, intermediate stations, and line badges.
> 3. An **Interchange Transfer Box** showing exact disembarking/boarding lines and transfer dwell buffers.
> 4. An aggregate **Metrics Bar** with total distance, travel duration, stops, and transfers."

---

### Q5: Can your engine handle negative edge weights? Do we have negative weights here?
> **Answer**:
> "No, Dijkstra’s algorithm assumes non-negative edge weights (for negative weights, Bellman-Ford would be required). In real-world transportation networks, physical distances and travel times are always strictly positive ($\text{distance} > 0, \text{time} > 0$). Therefore, Dijkstra is the optimal algorithm with $\mathcal{O}((V + E) \log V)$ time complexity."

---

### Q6: What is the Time and Space Complexity of your project?
> **Answer**:
> - **Space Complexity**:
>   - Graph Storage: $\mathcal{O}(V + E)$
>   - Priority Queue State Storage: $\mathcal{O}(V \cdot L)$, where $L \le 5$ is the number of metro lines.
> - **Time Complexity**:
>   - Best Route / Fastest Time: $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$.
>   - With $V = 63$ stations and $L = 5$ lines, computation finishes in under $2\text{ milliseconds}$."

---

### Q7: How does your system simulate a station closure or track disruption?
> **Answer**:
> "Each `Station` and `Connection` object has a boolean `active` / `operational` flag. When an operator sets a station to closed (e.g. `service.toggleStationStatus("Nadaprabhu Kempegowda Station, Majestic", false)`), `Connection.isOperational()` returns `false` for all tracks entering or leaving that station. During pathfinding, Dijkstra ignores non-operational edges and dynamically reroutes passengers around the blockage via the Outer Ring Road Blue Line corridor or alternative lines."

---

### Q8: How does the Fare Computation Engine calculate ticket prices?
> **Answer**:
> "It uses a multi-tier pipeline:
> 1. Maps cumulative route distance to the BMRCL slab (₹10 for 0-2 km up to ₹60 for >32 km).
> 2. Checks if the journey connects to the *KIAL Airport Express Corridor*; if so, applies a ₹20 surcharge.
> 3. Computes passenger concessions (10% off for Smart Card, 20% off for Off-Peak, 25% off for Students, 30% off for Senior Citizens).
> 4. Enforces a minimum fare floor of ₹10.00."

---

### Q9: What happens if there is no route between two stations?
> **Answer**:
> "If the network is disconnected or line closures block all possible paths, Dijkstra’s algorithm drains the priority queue without reaching the destination. The engine returns `RouteResult.notFound()`, which has `found = false`. The UI handles this cleanly without throwing exceptions, informing the passenger that the stations are unreachable due to maintenance closures."

---

### Q10: How did you implement the Stretch Features (File Export)?
> **Answer**:
> "In accordance with project rules, we built `FileExporter.java` using standard Java I/O (`BufferedWriter` and `FileWriter`). It exports:
> 1. Printable text tickets (`exports/receipt_*.txt`) with full segment breakdowns and fare audits.
> 2. Analytical turn-by-turn route logs (`exports/route_*.csv`) with distances and time deltas per stop.
> 3. Complete station directory exports (`exports/metro_network_stations.csv`)."
