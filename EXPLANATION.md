# MetroMind: Complete System Architecture, Code & DSA Explanation Manual
### Bengaluru Namma Metro (BMRCL) Rapid Transit Routing & Fare Engine

---

## 1. Executive Summary & Purpose

**MetroMind** is an automated transit pathfinding, dynamic fare auditing, and terminal graphical track visualization engine built in Java. It models the real-world **Bengaluru Namma Metro (BMRCL)** rapid transit network (63 stations across 5 lines).

The codebase is split into two cleanly decoupled files:
1. **[`MetroDSA.java`](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/MetroDSA.java)**: The **Data Structures & Algorithms (DSA)** core. Houses the graph representation, adjacency list, Priority Queue node models, pathfinding algorithms (Dijkstra variants), commuter friction cost heuristics, and route reconstruction.
2. **[`Main.java`](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/Main.java)**: The **Application & Presentation** layer. Houses the interactive console menus, BMRCL distance slab & concession fare computation, network initialization, ASCII track schematic visualizer, receipt/CSV exporters, and automated self-verification test suite.

---

## 2. File Organization & High-Level Architecture

```
New folder (6)/
├── MetroDSA.java     <-- [DSA Layer] Graph Data Structures & Dijkstra Pathfinding Algorithms
├── Main.java         <-- [App Layer] UI, Fare Engine, Network Initializer, Visualizer & Tests
├── README.md         <-- Quick start guide, feature overview & summary table
├── EXPLANATION.md    <-- Complete code walkthrough & algorithmic deep-dive (This document)
├── run.bat           <-- 1-click Windows launcher (javac *.java && java Main)
├── .gitignore        <-- Clean repository rules (ignores *.class and exports/)
└── exports/          <-- Auto-generated travel receipts (.txt) and analytical CSVs
```

### Architectural Separation
```
+--------------------------------------------------------------------------+
|                                USER (CLI)                                |
+--------------------------------------------------------------------------+
                                    |
                                    v
+--------------------------------------------------------------------------+
|                     APPLICATION LAYER (Main.java)                        |
|                                                                          |
|  - Main Menu & Input Sanitization (ConsoleUtils)                         |
|  - MetroMindService (Facade coordinating operations & history)           |
|  - NetworkDataInitializer (Initializes 63 Namma Metro stations & tracks) |
|  - FareEngine & FareBreakdown (BMRCL distance slabs & concessions)       |
|  - AsciiRouteVisualizer (Terminal track schematics & transfer boxes)     |
|  - FileExporter (Printable receipts & analytical CSV reports)            |
|  - Verification Test Suite (10/10 automated validation tests)            |
+--------------------------------------------------------------------------+
                                    |
                                    v
+--------------------------------------------------------------------------+
|               CORE DATA STRUCTURES & ALGORITHMS (MetroDSA.java)          |
|                                                                          |
|  - Station (Vertex with line memberships & operational toggle)           |
|  - Connection (Directed weighted edge: km, minutes, operational status)  |
|  - MetroGraph (Adjacency list Map<Station, List<Connection>>)            |
|  - PathNode (Priority Queue state node with backtrack pointers)          |
|  - RouteEngine (Dijkstra implementations & Commuter Friction Heuristic): |
|      * dijkstraDistance (Min physical distance)                          |
|      * dijkstraTime (State-Expanded Dijkstra with line transfer delay)   |
|      * dijkstraFewestInterchanges (Lexicographical transfer optimizer)   |
|      * findBestRoute (Commuter friction score evaluator)                 |
|  - RouteResult & RouteSegment (Itinerary representation containers)      |
+--------------------------------------------------------------------------+
```

---

## 3. Deep Dive into `MetroDSA.java` (The DSA Core)

`MetroDSA.java` contains all fundamental Data Structures and Algorithms without any UI or file I/O dependencies.

### 3.1 `enum MetroLine`
Represents the transit lines in Bengaluru Namma Metro:
- **PURPLE**: *Challaghatta <-> Whitefield (Kadugodi)* (Transfer penalty: 4.0 min)
- **GREEN**: *Madavara (BIEC) <-> Silk Institute* (Transfer penalty: 4.0 min)
- **YELLOW**: *RV Road <-> Bommasandra (Electronic City)* (Transfer penalty: 3.5 min)
- **PINK**: *Kalena Agrahara <-> Nagawara* (Transfer penalty: 4.0 min)
- **BLUE**: *Central Silk Board <-> KIAL Airport Terminal* (Transfer penalty: 3.0 min)
- **Field `transferPenaltyMinutes`**: Represents the physical duration in minutes required for a commuter to walk between platforms, climb stairways/escalators, and dwell waiting for the connecting train at an interchange hub.

### 3.2 `class Station implements Serializable, Comparable<Station>`
Represents a **Vertex** in the transit graph:
- **`id`** (`String`): Identifier (e.g., `"PL06"`, `"GL04"`).
- **`name`** (`String`): Official station name (e.g., `"Nadaprabhu Kempegowda Station, Majestic"`).
- **`lines`** (`Set<MetroLine>`): A `HashSet` of lines serving this station. If `lines.size() > 1`, `isInterchange()` returns `true`.
- **`active`** (`boolean`): Maintenance status flag. When `false`, all incoming and outgoing connections through this station are considered blocked.
- **`equals(Object)` & `hashCode()`**: Case-insensitive equality based on station name.
- **`compareTo(Station)`**: Enables natural alphabetical sorting when displaying station directories.

### 3.3 `class Connection implements Serializable`
Represents a **Weighted Directed Edge** in the transit graph:
- **`from`** & **`to`** (`Station`): Origin and destination vertices of the track link.
- **`line`** (`MetroLine`): The metro line operating on this physical track.
- **`distanceKm`** (`double`): Physical distance in kilometers (weight 1).
- **`timeMinutes`** (`double`): Train transit duration in minutes at cruise speed (weight 2).
- **`operational`** (`boolean`): Track maintenance status.
- **`isOperational()`**: Returns `true` only if the track is operational **and** both endpoint stations are active (`operational && from.isActive() && to.isActive()`).

### 3.4 `class MetroGraph implements Serializable`
The core transit graph data structure:
- **`stations`** (`Map<String, Station>`): A `TreeMap<String, Station>(String.CASE_INSENSITIVE_ORDER)` allowing fast $\mathcal{O}(\log V)$ station lookup and partial name search regardless of user capitalization.
- **`adjacencyList`** (`Map<Station, List<Connection>>`): Standard adjacency list representing the graph topology. Each station points to a `List` of outgoing `Connection` objects.
- **Key Methods**:
  - `addStation(Station)`: Adds station to registry and initializes an empty adjacency list.
  - `getOrCreateStation(id, name, lines)`: Factory method ensuring duplicate stations are merged into a single vertex with multiple line tags (crucial for interchange hubs like Majestic).
  - `addConnection(from, to, line, dist, time, bidirectional)`: Creates forward connection, and if `bidirectional == true`, automatically creates the reverse connection.
  - `searchStations(query)`: Case-insensitive substring query returning all matching stations.
  - `toggleStationStatus(stationName, active)`: Live network maintenance simulator — disables a station dynamically.
  - `toggleTrackStatus(from, to, operational)`: Live network maintenance simulator — disables specific track segments between two stations.

### 3.5 `class PathNode implements Comparable<PathNode>`
The state wrapper utilized inside the **Min-Priority Queue** for Dijkstra's algorithm:
- **`station`**: Current station reached.
- **`currentLine`**: The transit line used on the incoming connection.
- **`cost`**: The accumulated objective function value (distance, time, or transfer penalty).
- **`distanceKm`**: Cumulative physical distance traveled.
- **`timeMinutes`**: Cumulative travel time elapsed.
- **`transfers`**: Count of line switches made so far.
- **`previous`**: Pointer to preceding `PathNode` in the optimal path (enables backwards path reconstruction).
- **`connectionUsed`**: Pointer to the specific `Connection` traversed.
- **`compareTo(PathNode)`**: Compares `this.cost` with `other.cost` to enforce Min-Heap order in `PriorityQueue`.

### 3.6 `class RouteEngine` (The Algorithmic Engine)
Implements all pathfinding algorithms:

#### A. Dijkstra Shortest Physical Distance (`dijkstraDistance`)
- **Objective**: Minimize total track kilometers $\sum d_i$.
- **Data Structures**: `PriorityQueue<PathNode>` (Min-Heap) + `Map<Station, Double> minCost`.
- **Time Complexity**: $\mathcal{O}((V + E) \log V)$.
- **Process**: Standard Dijkstra edge relaxation:
  $$\text{if } (\text{dist}[u] + \text{weight}(u, v) < \text{dist}[v]) \implies \text{relax edge}$$

#### B. State-Expanded Dijkstra for Travel Time (`dijkstraTime`)
- **The Challenge**: Standard Dijkstra cannot calculate platform interchange transfer penalties because a station node alone does not know which line train the passenger arrived on.
- **The Solution (State Expansion)**: Expand each vertex into a tuple state:
  $$\text{State} = (\text{Station}, \text{MetroLine})$$
- **Cost Function**:
  $$\text{Weight} = \text{Track Time} + \begin{cases} 0, & \text{if } \text{Line}_{\text{prev}} == \text{Line}_{\text{next}} \\ \text{Line Transfer Delay (3.0 to 4.0 min)}, & \text{if } \text{Line}_{\text{prev}} \ne \text{Line}_{\text{next}} \end{cases}$$
- **Time Complexity**: $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ where $L \le 5$ lines.

#### C. Lexicographically Optimized Dijkstra for Fewest Interchanges (`dijkstraFewestInterchanges`)
- **Objective**: Minimize platform transfers first; minimize track distance second.
- **Cost Weighting**:
  $$\text{Cost} = (\text{Transfers} \times 10,000.0) + \text{Distance (km)}$$
- Large coefficient $10,000$ ensures that any path with fewer transfers will strictly dominate a path with more transfers, while using physical distance as the tie-breaker.

#### D. Automated Best Commuter Route (`findBestRoute`)
- Computes three candidate routes:
  1. $R_{\text{time}}$ (Fastest Travel Time)
  2. $R_{\text{transfers}}$ (Fewest Line Interchanges)
  3. $R_{\text{distance}}$ (Shortest Physical Distance)
- Evaluates candidates against the **Commuter Friction Cost Function**:
  $$\text{Friction Score} = \text{Travel Time (min)} + (\text{Interchanges} \times 7.0) + (\text{Distance (km)} \times 0.15)$$
- **Why this heuristic works**:
  - In real transit, changing platforms involves walking across multi-level concourses, ticket gates, stairs/elevators, and waiting for the next train schedule. Commuter studies equate 1 transfer to approximately $7.0$ minutes of physical exertion/stress.
  - If a direct single-line route takes only 2 extra minutes of riding time, commuters overwhelmingly prefer it over switching trains. However, if staying on one line causes an enormous circuitous detour (+25 km), the formula automatically selects the transfer route.

#### E. Backtracking & Segment Grouping (`reconstructRoute`)
- Follows `PathNode.previous` pointers backwards from the destination to the origin.
- Reverses the list to obtain natural forward order.
- Iterates through consecutive connections to detect line changes and groups them into high-level `RouteSegment` objects (e.g., Segment 1: Whitefield to Majestic on Purple Line; Segment 2: Majestic to Lalbagh on Green Line).

---

## 4. Deep Dive into `Main.java` (Application & Presentation)

`Main.java` coordinates user interactions, fare computation, data formatting, and automated verification tests.

### 4.1 `enum TicketType`
Models official BMRCL ticketing and concession categories:
1. **TOKEN**: Single journey token / QR paper ticket (0% discount).
2. **SMART_CARD**: Namma Metro Smart Card (Varshik / NCMC) (10% discount).
3. **SMART_CARD_OFFPEAK**: Smart Card during non-peak hours and weekends (20% discount).
4. **STUDENT_PASS**: Student concession pass (25% discount).
5. **SENIOR_CITIZEN**: Senior citizen pass (30% discount).

### 4.2 `class FareEngine` & `class FareBreakdown`
Computes passenger fares according to official BMRCL transit policy:
- **Distance Slabs**:
  - $0.0 \text{ to } 2.0\text{ km} \rightarrow ₹10.00$
  - $2.1 \text{ to } 5.0\text{ km} \rightarrow ₹20.00$
  - $5.1 \text{ to } 12.0\text{ km} \rightarrow ₹30.00$
  - $12.1 \text{ to } 21.0\text{ km} \rightarrow ₹40.00$
  - $21.1 \text{ to } 32.0\text{ km} \rightarrow ₹50.00$
  - $> 32.0\text{ km} \rightarrow ₹60.00$
- **Airport Corridor Infrastructure Surcharge**:
  - $+₹20.00$ added automatically when origin or destination is `KIAL Airport Terminal`.
- **Concessions**: Base fare is reduced by the ticket discount percentage, with a minimum fare floor of $₹10.00$.

### 4.3 `class NetworkDataInitializer`
Initializes the **63 stations** across Bengaluru's rapid transit system:
- **Purple Line (16 stations)**: Challaghatta, Kengeri, Mysuru Road, Vijayanagar, KSR Bengaluru Railway Station, Majestic, Vidhana Soudha, MG Road, Indiranagar, SV Road, Baiyappanahalli, KR Puram, Garudacharpalya, Hoodi, Sri Sathya Sai Hospital, Whitefield (Kadugodi).
- **Green Line (17 stations)**: Madavara (BIEC), Nagasandra, Peenya Industry, Yeshwanthpur, Sandal Soap Factory, Rajajinagar, Sampige Road, Majestic, Chickpet, KR Market, Lalbagh, South End Circle, Jayanagar, RV Road, Banashankari, JP Nagar, Yelachenahalli, Silk Institute.
- **Yellow Line (11 stations)**: RV Road, Ragigudda, Jayadeva Hospital, BTM Layout, Central Silk Board, Bommanahalli, Kudlu Gate, Singasandra, Electronic City, Infosys Foundation Konappana, Bommasandra.
- **Pink Line (11 stations)**: Kalena Agrahara, IIM Bangalore, JP Nagar 4th Phase, Jayadeva Hospital, Dairy Circle, Lakkasandra, Langford Town, MG Road, Shivaji Nagar, Bengaluru Cantonment, Tannery Road, Nagawara.
- **Blue Line / ORR & Airport (13 stations)**: Central Silk Board, HSR Layout, Bellandur, Kadubeesanahalli, Marathahalli, Doddanekundi, KR Puram, Kalyan Nagar, Nagawara, Hebbal, Yeshwanthpur, Yelahanka, Doddajala, KIAL Airport Terminal.
- **Major Interchanges**:
  - *Majestic*: Purple $\leftrightarrow$ Green
  - *MG Road*: Purple $\leftrightarrow$ Pink
  - *Central Silk Board*: Yellow $\leftrightarrow$ Blue
  - *KR Puram*: Purple $\leftrightarrow$ Blue
  - *Jayadeva Hospital*: Yellow $\leftrightarrow$ Pink
  - *RV Road*: Green $\leftrightarrow$ Yellow
  - *Nagawara*: Pink $\leftrightarrow$ Blue
  - *Yeshwanthpur*: Green $\leftrightarrow$ Blue

### 4.4 `class AsciiRouteVisualizer`
Renders graphical ASCII schematics directly in the terminal:
1. **Corridor Overview**:
   ```
   [Whitefield] ==(Purple Line: 11 stops)=>> [Majestic] ==(Green Line: 4 stops)=>> [Lalbagh]
   ```
2. **Track Map Schematic**:
   - `[START] *` Origin station.
   - Intermediate stations displayed with `o`.
   - Transfer boxes with double border `#====#` showing platform transfer instructions and walking dwell buffer.
   - `[DESTINATION] #` Destination station.
3. **Summary Metrics Bar**: Total kilometers, minutes, stop count, and transfer count.

### 4.5 `class FileExporter`
- **`exportJourneyReceipt`**: Writes a formatted receipt (`exports/receipt_*.txt`) with journey summary, segment breakdown, and fare audit.
- **`exportRouteToCsv`**: Writes turn-by-turn CSV with distance and time intervals (`exports/route_*.csv`).
- **`exportNetworkDirectoryCsv`**: Dumps all 63 stations, lines, and operational statuses (`exports/metro_network_stations.csv`).

### 4.6 `class ConsoleUtils`
Utility class providing safe terminal formatting, divider lines, and robust user input sanitization (`readInt`, `readDouble`, `readNonEmptyString`, `pressEnterToContinue`).

### 4.7 Automated Test Suite (`runVerificationTests()`)
Contains 10 automated unit and integration tests verifying:
1. Network size & interchange hub connectivity ($\ge 30$ stations, $\ge 5$ interchanges).
2. Direct single-line journey routing (Whitefield $\rightarrow$ Majestic on Purple Line).
3. Direct Green Line traversal (Madavara $\rightarrow$ Silk Institute).
4. Multi-line interchange pathfinding with transfer detection (Whitefield $\rightarrow$ Lalbagh via Majestic).
5. Fare calculation slabs and concession discount formulas.
6. Airport Express corridor surcharge application ($+₹20.00$).
7. Dynamic disruption rerouting (closing Majestic forces rerouting via outer corridors).
8. File export creation (receipt `.txt`, route `.csv`, network `.csv`).
9. Automated Best Route optimization logic.
10. ASCII route visualizer rendering without exceptions.

---

## 5. Walkthrough of Main Menu Features

When running `java Main`, you are presented with an 8-option interactive console:

| Option | Feature | What Happens Under the Hood |
| :--- | :--- | :--- |
| **[1]** | **Find Best Route & Visual Track Map** | Prompts for origin, destination, and ticket type. Executes `RouteEngine.findBestRoute()`, renders ASCII track schematic, prints itemized fare breakdown, compares all concession passes, and prompts for text receipt export. |
| **[2]** | **Explore Transit Network** | Sub-menu to inspect all 5 transit lines, inspect stations along a line, list all 8 interchange hubs, search stations by keyword, or list all 63 stations alphabetically. |
| **[3]** | **View Official Fare Structure** | Displays BMRCL distance slabs, the ₹20 Airport surcharge rule, and concession percentage discounts. |
| **[4]** | **Network Operations Simulator** | Maintenance mode simulator: close or reopen any station or track segment. Subsequent route calculations automatically route around closed infrastructure. Also allows adding custom stations or tracks. |
| **[5]** | **Recent Journey History** | Displays the session's last 25 planned routes with trip IDs, timestamps, and payable fares. Allows selecting any past trip ID to re-render its track schematic. |
| **[6]** | **Export Data** | Exports last journey receipt (`.txt`), route path (`.csv`), or the complete 63-station network directory (`.csv`). |
| **[7]** | **Run Verification Tests** | Runs the complete 10/10 test suite directly inside the console session. |
| **[8]** | **Exit** | Terminates the console session with a farewell banner. |

---

## 6. How to Compile & Run

### Method 1: Using `run.bat` (Recommended on Windows)
Simply double-click **`run.bat`** or run:
```cmd
run.bat
```
To run the automated verification test suite:
```cmd
run.bat --test
```

### Method 2: Command Line (Java 11+)
```bash
# Compile both Java source files
javac *.java

# Launch interactive application
java Main

# Run automated tests
java Main --test
```

---

## 7. Data Structures & Algorithms Summary Table

| Requirement / Task | Chosen Data Structure | Algorithm | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **Metro Network Graph** | Adjacency List (`Map<Station, List<Connection>>`) | Graph Representation | $\mathcal{O}(1)$ edge insert | $\mathcal{O}(V + E)$ |
| **Station Registry & Lookup** | `TreeMap<String, Station>` | Balanced BST Search | $\mathcal{O}(\log V)$ | $\mathcal{O}(V)$ |
| **Shortest Track Distance** | `PriorityQueue<PathNode>` | Dijkstra's Algorithm | $\mathcal{O}((V + E) \log V)$ | $\mathcal{O}(V)$ |
| **Fastest Travel Time** | `PriorityQueue<PathNode>` | State-Expanded Dijkstra | $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ | $\mathcal{O}(V \cdot L)$ |
| **Fewest Transfers** | `PriorityQueue<PathNode>` | Lexicographical Dijkstra | $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ | $\mathcal{O}(V \cdot L)$ |
| **Best Commuter Route** | Candidate Array of Routes | Friction Score Heuristic | $\mathcal{O}(1)$ evaluation | $\mathcal{O}(1)$ |
| **Path Reconstruction** | Linked Node Chain (`previous`) | Backtracking & Reverse | $\mathcal{O}(K)$ ($K = \text{stops}$) | $\mathcal{O}(K)$ |
| **Disruption Avoidance** | Boolean flag checks on nodes/edges | Filtered Dijkstra Traversal | $\mathcal{O}((V + E) \log V)$ | $\mathcal{O}(V)$ |

---

## 8. Viva & Technical Interview Defense Questions

1. **Why is an Adjacency List used instead of an Adjacency Matrix?**
   - Metro graphs are very sparse: each station connects to only 2 adjacent stations (or 3-4 at interchanges). An adjacency matrix would consume $\mathcal{O}(V^2)$ space (most cells containing 0). An adjacency list consumes only $\mathcal{O}(V + E)$ space and allows iterating over neighbors in $\mathcal{O}(\text{degree}(v))$ time.

2. **Why does travel time optimization require State Expansion in Dijkstra?**
   - Standard Dijkstra tracks only the vertex $v$. However, platform transfer delays (3 to 4 minutes) only occur when a commuter switches from Line A to Line B. Staying on Line A through an interchange station incurs zero transfer delay. Expanding state to $(\text{Station}, \text{CurrentLine})$ allows the cost function to inspect the incoming line and conditionally penalize line switches.

3. **What is the Commuter Friction Cost Function?**
   - Formula: $\text{Score} = \text{Time (min)} + (\text{Interchanges} \times 7.0) + (\text{Distance} \times 0.15)$.
   - Platform transfers add significant physical and mental fatigue (stairs, crowds, platform changes, waiting). Setting 1 transfer equal to 7 minutes of travel friction prevents recommending jarring multi-transfer routes that save only 30 seconds of track time.

4. **How are station disruptions handled dynamically?**
   - When a station or track segment is toggled inactive (`active = false`), `Connection.isOperational()` immediately returns `false`. During the next Dijkstra traversal, edge exploration skips inoperable connections, allowing the Priority Queue to naturally find an alternate path through surrounding lines.
