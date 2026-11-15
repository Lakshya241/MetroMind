<div align="center">

# 🚇 METROMIND
### High-Performance Shortest-Route & Fare Computation Engine
**Bengaluru Namma Metro (BMRCL) Edition**

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Standard Library](https://img.shields.io/badge/Dependencies-Zero%20External-2ea44f?style=for-the-badge&logo=java&logoColor=white)](src/)
[![Network](https://img.shields.io/badge/Network-63%20Stations%20%7C%205%20Lines-0969da?style=for-the-badge&logo=google-maps&logoColor=white)](src/util/NetworkDataInitializer.java)
[![Algorithm](https://img.shields.io/badge/Algorithm-Dijkstra%20%C3%973%20%2B%20Friction%20Score-8957e5?style=for-the-badge&logo=diagram-next&logoColor=white)](src/route/RouteEngine.java)
[![Test Suite](https://img.shields.io/badge/Verification-10%2F10%20Tests%20Passing-brightgreen?style=for-the-badge&logo=checkmarx&logoColor=white)](src/MetroMindTest.java)

<p align="center">
  <b>An industry-grade transit navigation, multi-criteria route optimization, and automated fare auditing engine designed for the Bengaluru Namma Metro transit network.</b><br>
  Engineered in pure Java (JDK 17+) with state-expanded graph algorithms, slab-based pricing, network disruption simulation, and a graphical terminal track schematic.
</p>

<p align="center">
  <a href="#-project-overview">Overview</a> •
  <a href="#-application-preview">Application Preview</a> •
  <a href="#-key-features">Key Features</a> •
  <a href="#-system-architecture">Architecture</a> •
  <a href="#-routing-algorithm-explained">Routing Engine</a> •
  <a href="#-fare-computation-engine">Fare Engine</a> •
  <a href="#-algorithm-complexity-analysis">Complexity</a> •
  <a href="#-testing--validation">Testing</a> •
  <a href="#-installation--execution">Getting Started</a>
</p>

---

</div>

## 📌 Project Overview

Urban rapid transit networks require commuters to constantly solve a complex multi-objective optimization problem: *balancing physical travel distance, total commute duration, and the physical fatigue of switching train lines at crowded interchange stations.*

**MetroMind** addresses this challenge by modeling the **Bengaluru Namma Metro (BMRCL)** transit grid as a weighted directed multigraph. Rather than presenting raw, uncurated mathematical paths, MetroMind computes candidate routes across three independent objective dimensions and resolves them using a **Commuter Friction Cost Function** to deliver a single, practically optimal route recommendation.

```
+==================================================================================================+
|  COMMUTE CHALLENGE                                        METROMIND SOLUTION                     |
+==================================================================================================+
|  • 63 stations across 5 sprawling lines             ==>   Automated best-route selection         |
|  • Walking and platform transfer delays              ==>   State-expanded Dijkstra with penalties |
|  • 6 distance slabs + 5 discount tiers + surcharges  ==>   Deterministic automated fare engine    |
|  • Unscheduled maintenance track closures           ==>   Real-time disruption rerouting         |
|  • Unclear turn-by-turn platform directions          ==>   Terminal graphical track schematic     |
+==================================================================================================+
```

### Problem Addressed
1. **Transfer Friction:** Traditional shortest-path algorithms only minimize track distance or raw travel time. In reality, transferring platforms at massive interchanges like *Nadaprabhu Kempegowda Station (Majestic)* or *Central Silk Board* introduces 3 to 5 minutes of walking and waiting stress. MetroMind explicitly penalizes transfers.
2. **Deterministic Fare Auditing:** Commuters navigating multi-line journeys frequently miscalculate distance-bracket fares, smart card concessions, and airport surcharges. MetroMind provides transparent, instantaneous fare computation.
3. **Operational Resilience:** When track segments undergo maintenance, passengers need immediate alternative corridors. MetroMind dynamically reroutes journeys via the Outer Ring Road (Blue Line) and cross-city junctions.

---

## 📸 Application Preview

The following captures demonstrate the live, terminal-rendered graphical user interface:

<div align="center">

### 1. Application Startup & Operational Network Directory
![Startup Screen](assets/screenshots/01_welcome_banner.png)
*Initializes the 63-station network, active transit lines, and operational status banner.*

---

### 2. Main Navigation Menu
![Main Navigation Menu](assets/screenshots/02_main_menu.png)
*Structured 7-option console menu with styled boundaries, descriptions, and prompt indicators.*

---

### 3. Intelligent Station Search & Auto-Matching
![Station Selection](assets/screenshots/03_station_search.png)
*Case-insensitive partial keyword search and automated passenger concession category selection.*

---

### 4. Visual Metro Track Schematic (Terminal Graphics)
![Route Track Schematic](assets/screenshots/04_route_schematic.png)
*Terminal schematic featuring corridor overviews, milestone stops, transfer bridge alerts, and trip metrics.*

---

### 5. Multi-Tier Fare Computation & Pass Comparison
![Fare Computation Breakdown](assets/screenshots/05_fare_breakdown.png)
*Transparent breakdown of base slabs, airport surcharges, and concession savings across all pass types.*

---

### 6. Network Disruption Simulator & Live Rerouting
![Network Disruption Simulator](assets/screenshots/06_network_disruption.png)
*Simulating track/station closures with automatic real-time rerouting around blocked transit corridors.*

---

### 7. Session Journey History & Multi-Format File Exports
![Journey History & Exports](assets/screenshots/07_journey_history_exports.png)
*Audit trail of past journeys with export capabilities to printable text tickets and turn-by-turn CSV spreadsheets.*

</div>

---

## ⚡ Key Features

| Feature | Description | Technical Significance |
|---|---|---|
| **Multi-Criteria Route Recommendation** | Evaluates 3 Dijkstra variants simultaneously and scores them via a commuter friction heuristic. | Replaces naive distance minimization with real-world commuter comfort and transfer avoidance. |
| **State-Expanded Dijkstra's Algorithm** | Expands the state space to $(u, L)$ tuples where $u \in \text{Stations}$ and $L \in \text{Lines}$. | Accurately models non-zero transfer walk/dwell penalties ($3.0 - 4.0\text{ min}$) only when switching lines. |
| **Lexicographical Transfer Optimization** | Optimizes line switches as primary criteria and distance as tie-breaker via $(N \times 10,000) + D$. | Guarantees absolute minimum interchange count for passengers with heavy luggage. |
| **Visual Track Schematic** | Custom terminal rendering engine producing box-drawn track schematics. | Zero-dependency terminal visualization with platform switch alerts and metrics. |
| **Slab-Based Fare Engine** | Implements BMRCL distance-bracket pricing (₹10 to ₹60 across 6 slabs). | Deterministic range matching with a minimum fare floor of ₹10.00. |
| **Concession Matrix & Surcharges** | Computes 5 passenger categories (up to 30% discount) plus a ₹20 KIAL Airport Express surcharge. | Models real-world ticketing options (Smart Cards, Off-Peak, Student Passes, Senior Citizens). |
| **Network Disruption Simulator** | Runtime toggling of station and track operational states. | Dynamic graph modification; Dijkstra automatically routes around blocked nodes. |
| **Dynamic Network Expansion** | Add new stations and track edges at runtime without restarting. | Mutates adjacency lists dynamically; immediately available for subsequent queries. |
| **Intelligent Station Search** | Case-insensitive substring matching over alphabetical station directory. | Resilient search handling inputs like `"majestic"`, `"silk board"`, or `"whitefield"`. |
| **Multi-Format Data Exports** | Writes formatted travel receipts (`.txt`), route itineraries (`.csv`), and full network directory (`.csv`). | Structured file I/O adhering to clean separation of presentation and domain models. |

---

## 🛠️ Technology Stack

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   TECHNOLOGY MATRIX                                    │
├──────────────────────────┬─────────────────────────────────────────────────────────────┤
│ Programming Language     │ Java 17 LTS (Pure Standard Library, Zero External Deps)     │
│ Core Data Structures     │ Adjacency List (HashMap<Station, List<Connection>>)         │
│                          │ Self-Balancing BST (TreeMap<String, Station>)               │
│                          │ Min-Priority Queue (PriorityQueue<PathNode> / Binary Heap)  │
│                          │ Set (HashSet<MetroLine>), Dynamic Lists (ArrayList<Station>)│
│ Algorithms               │ Dijkstra's Shortest Path, State Expansion Graph Traversal,  │
│                          │ Lexicographical Optimization, Commuter Friction Heuristic   │
│ Architecture Pattern     │ Layered Architecture with Service Facade & Immutable DTOs   │
│ Console Interface        │ Reusable ANSI Formatting Toolkit with Plain-Text Fallback    │
│ File Export Engine       │ Java I/O (BufferedWriter, FileWriter, CSV generation)        │
│ Build & Verification     │ javac Standard Compiler, Windows Batch Scripts, Custom Test │
└──────────────────────────┴─────────────────────────────────────────────────────────────┘
```

---

## 🏛️ System Architecture

MetroMind follows a strict **Layered Architecture** enforced through a **Service Facade pattern**. The presentation layer (`Main`) communicates exclusively through `MetroMindService`, shielding user interactions from raw graph mutations and pathfinding state.

### High-Level Architecture Diagram

```mermaid
flowchart TD
    subgraph UI_Layer ["Presentation & Terminal UI Layer"]
        CLI["Main.java (Menu Loop & Input Validation)"]
        VIS["AsciiRouteVisualizer.java (Track Schematic)"]
        EXP["FileExporter.java (Receipt & CSV Writers)"]
    end

    subgraph Service_Layer ["Orchestration & Facade Layer"]
        SVC["MetroMindService.java (Unified Application Facade)"]
    end

    subgraph Core_Engine ["Domain Logic & Algorithms"]
        GRAPH["MetroGraph.java (Adjacency List & State)"]
        ROUTE["RouteEngine.java (Dijkstra x3 + Friction)"]
        FARE["FareEngine.java (Distance Slabs & Discounts)"]
        INIT["NetworkDataInitializer.java (Network Topology)"]
    end

    subgraph Domain_Model ["Domain Models & DTOs"]
        STATION["Station.java"]
        CONN["Connection.java"]
        LINES["MetroLine.java (Enum)"]
        TICKETS["TicketType.java (Enum)"]
        RESULT["RouteResult.java"]
        SUMMARY["JourneySummary.java"]
        BREAKDOWN["FareBreakdown.java"]
    end

    CLI --> SVC
    CLI --> VIS
    SVC --> ROUTE
    SVC --> FARE
    SVC --> GRAPH
    SVC --> EXP
    INIT --> GRAPH
    ROUTE --> GRAPH
    ROUTE --> RESULT
    FARE --> BREAKDOWN
    SVC --> SUMMARY

    GRAPH -.-> STATION
    GRAPH -.-> CONN
    CONN -.-> LINES
    SUMMARY -.-> RESULT
    SUMMARY -.-> BREAKDOWN
    BREAKDOWN -.-> TICKETS
```

### Class Diagram

```mermaid
classDiagram
    direction TB

    class Main {
        -Scanner scanner$
        -MetroMindService service$
        -JourneySummary lastJourney$
        +main(String[] args)$
        -handleFindBestRoute()$
        -handleExploreNetwork()$
        -handleViewFareStructure()$
        -handleNetworkOperations()$
        -handleRecentJourneys()$
        -handleExportData()$
    }

    class MetroMindService {
        -MetroGraph graph
        -RouteEngine routeEngine
        -FareEngine fareEngine
        -List~JourneySummary~ recentJourneys
        -AtomicInteger journeyCounter
        +planBestJourney(Station, Station, TicketType) JourneySummary
        +planJourney(Station, Station, Criteria, TicketType) JourneySummary
        +compareRoutes(Station, Station) Map~Criteria, RouteResult~
        +toggleStationStatus(String, boolean) boolean
        +toggleTrackStatus(String, String, boolean) boolean
        +exportReceipt(JourneySummary) String
        +exportRouteCsv(JourneySummary) String
        +exportNetworkCsv() String
    }

    class MetroGraph {
        -Map~String, Station~ stations
        -Map~Station, List~Connection~~ adjacencyList
        +addStation(Station) Station
        +addConnection(Station, Station, MetroLine, double, double, boolean)
        +getConnections(Station) List~Connection~
        +getStation(String) Station
        +searchStations(String) List~Station~
        +toggleStationStatus(String, boolean) boolean
        +toggleTrackStatus(String, String, boolean) boolean
    }

    class RouteEngine {
        -MetroGraph graph
        +findBestRoute(Station, Station) RouteResult
        +findRoute(Station, Station, Criteria) RouteResult
        -dijkstraDistance(Station, Station) RouteResult
        -dijkstraTime(Station, Station) RouteResult
        -dijkstraFewestInterchanges(Station, Station) RouteResult
        -computeFrictionScore(RouteResult) double
        -reconstructRoute(Station, Station, PathNode, String) RouteResult
    }

    class FareEngine {
        +AIRPORT_SURCHARGE: double$
        +MINIMUM_FARE: double$
        -Slab[] SLABS$
        +calculateFare(RouteResult, TicketType) FareBreakdown
        +calculateAllFareOptions(RouteResult) Map~TicketType, FareBreakdown~
        +getSlabForDistance(double) Slab
    }

    Main --> MetroMindService
    MetroMindService --> MetroGraph
    MetroMindService --> RouteEngine
    MetroMindService --> FareEngine
    RouteEngine --> MetroGraph
```

### Component Responsibilities

| Class | Package | Responsibility |
|---|---|---|
| `Main` | Default | Console interaction controller; renders menus, handles input validation loops, and coordinates UI transitions. |
| `MetroMindService` | `service` | Application orchestrator and facade; coordinates routing, fare calculations, session history, and file exports. |
| `MetroGraph` | `graph` | Encapsulates the sparse transit multigraph using an adjacency list and an alphabetical `TreeMap` station index. |
| `RouteEngine` | `route` | Executes the 3 Dijkstra optimization routines and evaluates candidate paths using the commuter friction scoring function. |
| `PathNode` | `route` | Min-heap node storing station identity, line context, cumulative distance, duration, transfer tally, and backtrack pointers. |
| `FareEngine` | `fare` | Evaluates distance slabs, applies ticket category discounts, and calculates airport surcharges. |
| `NetworkDataInitializer` | `util` | Hardcodes the authentic Bengaluru Namma Metro topology (63 stations across 5 lines, bidirectional connections). |
| `AsciiRouteVisualizer` | `util` | Formats and renders the visual track schematic, transfer bridges, and commute metrics bar. |
| `ConsoleUtils` | `util` | ANSI terminal styling, dynamic encoding capability detection, plain-text fallbacks, and safe numeric input sanitizers. |
| `FileExporter` | `util` | Generates formatted travel receipt text documents (`.txt`) and analytical route/network CSV files (`.csv`). |

---

## 🧭 Routing Algorithm Explained

Commuters rarely travel strictly by the shortest distance. Taking a longer geographic route on a direct line is often faster and less exhausting than transferring between two crowded trains. MetroMind solves this through a **two-phase optimization process**:

```mermaid
flowchart LR
    Origin([Origin Station]) & Dest([Destination Station]) --> RE[RouteEngine]

    subgraph Phase1 ["Phase 1: Parallel Tri-Criteria Dijkstra Search"]
        RE --> D1["1. dijkstraDistance()<br>Minimizes track km<br>Weight: connection.distanceKm"]
        RE --> D2["2. dijkstraTime()<br>Minimizes minutes + transfer dwell<br>Weight: connection.timeMinutes + delay"]
        RE --> D3["3. dijkstraFewestInterchanges()<br>Minimizes line changes<br>Weight: (Transfers × 10,000) + Distance"]
    end

    D1 --> R1["RouteCandidate 1<br>(Distance-Optimal)"]
    D2 --> R2["RouteCandidate 2<br>(Time-Optimal)"]
    D3 --> R3["RouteCandidate 3<br>(Interchange-Optimal)"]

    subgraph Phase2 ["Phase 2: Commuter Friction Scoring"]
        R1 & R2 & R3 --> FS["computeFrictionScore()<br>Score = Time + (Transfers × 7.0) + (Distance × 0.15)"]
        FS --> MIN["Rank Candidates<br>Select lowest Score"]
    end

    MIN --> BestRoute([Recommended Best Route])
```

### 1. Three Dijkstra Variants Compared

| Metric | `dijkstraDistance()` | `dijkstraTime()` | `dijkstraFewestInterchanges()` |
|---|---|---|---|
| **Primary Objective** | Minimum physical distance (km) | Minimum travel time (minutes) | Minimum line transfers |
| **Edge Weight** | `c.distanceKm` | `c.timeMinutes + transferPenalty` | $(\text{Transfers} \times 10,000) + \text{c.distanceKm}$ |
| **State Representation** | `Station` node | Expanded `(Station, CurrentLine)` | Expanded `(Station, CurrentLine)` |
| **Transfer Penalty** | Ignored ($0\text{ min}$) | Dynamic ($3.0 - 4.0\text{ min}$ on line switch) | $1$ transfer count increment |
| **Best Used For** | Environmental/energy analysis | Daily rush-hour commuting | Passengers with heavy luggage |

### 2. State Expansion in Dijkstra's Search
In a standard graph, a station $u$ is visited once. However, at an interchange like *Nadaprabhu Kempegowda Station (Majestic)*, a commuter may arrive on the Green Line and either continue on the Green Line ($0\text{ min}$ delay) or transfer to the Purple Line ($+4.0\text{ min}$ delay).

To correctly model this without artificially penalizing through-passengers, MetroMind expands each graph state to a composite key:
$$\text{State Key} = \text{Station Name} + \text{":"} + \text{Line Name}$$
This allows Dijkstra to discover that staying on the current line may be globally optimal even if an interchange station was already traversed by another line.

### 3. Commuter Friction Function
Candidate paths are evaluated against the friction cost function:
$$\mathcal{F}(R) = T + 7.0 \cdot N + 0.15 \cdot D$$

Where:
- $T$ = Total calculated travel duration in minutes (including track travel times and transfer penalties).
- $N$ = Total number of train transfers (interchanges). Each transfer is assigned a friction weight of **$7.0$ points**, representing the physical walking stress, stair/escalator climbing, and connecting train wait times.
- $D$ = Total physical track distance in kilometers, scaled by **$0.15$ points/km** to break time ties in favor of geographically direct corridors.

> [!NOTE]
> The friction score is a multi-objective decision heuristic designed to mirror commuter preferences. It does not claim to compute a theoretical Pareto-optimal frontier, but rather selects the most practical candidate from the three independently optimized Dijkstra runs.

---

## 💳 Fare Computation Engine

MetroMind implements a deterministic, slab-based fare computation engine modeling the Bengaluru Namma Metro transit rules:

### 1. Distance Slabs & Base Pricing

| Distance Bracket | Slab Label | Base Token Fare |
|---|---|---|
| $0.0\text{ km} - 2.0\text{ km}$ | `0.0 - 2.0 km` | **₹10.00** |
| $2.1\text{ km} - 5.0\text{ km}$ | `2.1 - 5.0 km` | **₹20.00** |
| $5.1\text{ km} - 12.0\text{ km}$ | `5.1 - 12.0 km` | **₹30.00** |
| $12.1\text{ km} - 21.0\text{ km}$ | `12.1 - 21.0 km` | **₹40.00** |
| $21.1\text{ km} - 32.0\text{ km}$ | `21.1 - 32.0 km` | **₹50.00** |
| $> 32.0\text{ km}$ | `> 32.0 km` | **₹60.00** |

### 2. Passenger Concession Categories

| Pass Category | Concession | Eligibility & Purpose |
|---|---|---|
| **Standard Token / QR Ticket** | $0\%$ | Standard single-journey fare without discount. |
| **Namma Metro Smart Card (Varshik / NCMC)** | $10\%$ | Stored-value contactless card offering $10\%$ discount on base fare. |
| **Namma Metro Smart Card (Off-Peak / Weekend)** | $20\%$ | Special off-peak concession applicable outside morning/evening rush hours. |
| **Student Concession Pass** | $25\%$ | Verified student subsidized travel pass. |
| **Senior Citizen Pass** | $30\%$ | Senior citizen welfare concession offering $30\%$ discount. |

### 3. Special Corridor Surcharges
- **KIAL Airport Express Corridor:** Commutes originating at or terminating at *KIAL Airport Terminal* incur a flat **+₹20.00 infrastructure surcharge** added to the discounted base fare.

### 4. Calculation Sequence & Worked Example
$$\text{Discount Amount} = \frac{\text{Base Fare} \times \text{Discount Percentage}}{100}$$
$$\text{Payable Fare} = \max\Big(\text{₹}10.00, \; (\text{Base Fare} - \text{Discount Amount}) + \text{Airport Surcharge}\Big)$$

**Worked Example:**
- **Journey:** *Whitefield (Kadugodi)* to *Lalbagh*
- **Total Distance:** $27.00\text{ km}$
- **Applicable Distance Slab:** `21.1 - 32.0 km` $\implies$ Base Fare = **₹50.00**
- **Selected Pass:** Namma Metro Smart Card ($10\%$ discount)
- **Discount Calculated:** $\text{₹}50.00 \times 10\% = -\text{₹}5.00$
- **Airport Surcharge:** ₹0.00 (non-airport corridor)
- **Final Payable Amount:** $\text{₹}50.00 - \text{₹}5.00 =$ **₹45.00**

> [!IMPORTANT]
> Fare slabs, concessions, and surcharges within MetroMind represent an educational simulation modeled after BMRCL tariff frameworks. They should not be used as an official live billing system for Bangalore Metro Rail Corporation Limited.

---

## 🔄 Project Execution Workflow

The complete end-to-end execution lifecycle of a user interaction is shown below:

```mermaid
flowchart TD
    START([Application Launch: Main.main]) --> INIT[NetworkDataInitializer.buildDefaultNetwork]
    INIT --> LOAD[63 Stations & 5 Lines Seeded into MetroGraph]
    LOAD --> WELCOME[Display Welcome Banner & Network Summary]

    WELCOME --> MENU{Main Navigation Menu}

    MENU -->|Option 1| ROUTE_PROMPT[Prompt Origin & Destination Stations]
    ROUTE_PROMPT --> SEARCH_ORIGIN[Intelligent Substring Match: Origin]
    SEARCH_ORIGIN --> SEARCH_DEST[Intelligent Substring Match: Destination]
    SEARCH_DEST --> TICKET_PROMPT[Select Passenger Ticket / Concession]
    TICKET_PROMPT --> BEST_ROUTE[MetroMindService.planBestJourney]
    BEST_ROUTE --> CALC_3[RouteEngine runs 3 Dijkstra Variants]
    CALC_3 --> SCORE[Rank by Friction Score]
    SCORE --> FARE_CALC[FareEngine calculates Base + Surcharge + Concession]
    FARE_CALC --> SCHEMATIC[AsciiRouteVisualizer.renderGraphicalRoute]
    SCHEMATIC --> BREAKDOWN[Display Fare Computation Breakdown]
    BREAKDOWN --> EXPORT_Q{Export Receipt to Text File?}
    EXPORT_Q -->|Yes| DO_RECEIPT[FileExporter.exportJourneyReceipt]
    EXPORT_Q -->|No| SAVE_HIST[Append to Session Journey History]
    DO_RECEIPT --> SAVE_HIST
    SAVE_HIST --> MENU

    MENU -->|Option 2| EXPLORE[Explore Transit Lines, Stations & Interchanges]
    EXPLORE --> MENU

    MENU -->|Option 3| FARE_STRUCT[Display Official BMRCL Fare Tables]
    FARE_STRUCT --> MENU

    MENU -->|Option 4| OPS_SIM[Network Operations Simulator: Toggle Station/Track Status]
    OPS_SIM --> REROUTE_NOTE[Graph edge state updated; future paths automatically rerouted]
    REROUTE_NOTE --> MENU

    MENU -->|Option 5| HIST[View Session Journey History & Inspect Past Trip Schematic]
    HIST --> MENU

    MENU -->|Option 6| EXPORT_MENU[Export Data: Receipt txt / Route CSV / Network CSV]
    EXPORT_MENU --> DO_EXPORTS[FileExporter writes to /exports directory]
    DO_EXPORTS --> MENU

    MENU -->|Option 7| EXIT([Exit Application: Graceful Shutdown])
```

---

## 📂 Project Structure

```
java2/
├── .gitignore                          # Git ignore configuration
├── compile.bat                         # Windows one-click compilation script
├── run.bat                             # Windows one-click application runner
├── test.bat                            # Windows one-click automated test runner
├── README.md                           # Comprehensive industry-grade GitHub documentation
├── explain.md                          # Beginner-friendly code tutorial & walkthrough
├── viva_dsa_guide.md                   # Data structures, algorithms & viva defense guide
│
├── assets/                             # Visual documentation assets
│   └── screenshots/                    # High-resolution terminal capture images
│       ├── 01_welcome_banner.png       # Startup banner and corridor overview
│       ├── 02_main_menu.png            # Main navigation menu
│       ├── 03_station_search.png       # Station selection & auto-match
│       ├── 04_route_schematic.png      # Graphical track schematic
│       ├── 05_fare_breakdown.png       # Multi-pass fare breakdown
│       ├── 06_network_disruption.png   # Disruption simulator & rerouting
│       └── 07_journey_history_exports.png # History audit & export confirmation
│
├── bin/                                # Compiled Java bytecode (.class files)
│   ├── Main.class
│   ├── MetroMindTest.class
│   ├── fare/
│   ├── graph/
│   ├── model/
│   ├── route/
│   ├── service/
│   └── util/
│
├── exports/                            # Generated exports (receipts, CSV spreadsheets)
│   ├── metro_network_stations.csv      # Complete 63-station network directory CSV
│   ├── receipt_MM-1001.txt             # Formal printable transit ticket
│   └── route_MM-1001.csv               # Turn-by-turn route path CSV
│
├── src/                                # Java source code
│   ├── Main.java                       # Application entry point & interactive UI controller
│   ├── MetroMindTest.java              # Standalone 10-test automated verification suite
│   │
│   ├── fare/                           # Fare computation domain
│   │   └── FareEngine.java             # Slabs, airport surcharges, concession discounts
│   │
│   ├── graph/                          # Transit graph implementation
│   │   └── MetroGraph.java             # Adjacency list multigraph with TreeMap station index
│   │
│   ├── model/                          # Domain models and value objects
│   │   ├── Connection.java             # Directed track edge (distance, time, line, operational)
│   │   ├── FareBreakdown.java          # Computed fare audit (base, discount, surcharge, final)
│   │   ├── JourneySummary.java         # Aggregates RouteResult, FareBreakdown, and timestamp
│   │   ├── MetroLine.java              # Transit line enum (color, description, transfer penalty)
│   │   ├── RouteResult.java            # Path entity (stations, connections, segments, metrics)
│   │   ├── RouteSegment.java           # Contiguous leg traversed along a single line
│   │   ├── Station.java                # Station node (id, name, lines set, active status)
│   │   └── TicketType.java             # Ticket category enum (discount percentage, description)
│   │
│   ├── route/                          # Shortest path algorithms
│   │   ├── PathNode.java               # Dijkstra min-heap entry with backtrack parent pointers
│   │   └── RouteEngine.java            # 3 Dijkstra variants + commuter friction score selection
│   │
│   ├── service/                        # Service layer / facade
│   │   └── MetroMindService.java       # Facade orchestrating graph, routing, fare, and exports
│   │
│   └── util/                           # Utilities, formatting, and file I/O
│       ├── AsciiRouteVisualizer.java   # Terminal graphical schematic renderer
│       ├── ConsoleUtils.java           # ANSI color palette, box characters, input validators
│       ├── FileExporter.java           # File I/O for text tickets and route/network CSVs
│       └── NetworkDataInitializer.java # Hardcoded Bengaluru Namma Metro topology
│
└── tools/                              # Developer tool scripts
    └── generate_screenshots.py         # Automated terminal preview image generator
```

---

## 💻 Installation & Execution

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher (tested on OpenJDK 17.0.16 Temurin).
- **Operating System:** Windows 10/11, macOS, or Linux.
- **Terminal:** Windows Terminal, PowerShell, Command Prompt, VS Code Terminal, or any standard Unix terminal.

---

### 1. Windows One-Click Execution (Recommended)

```cmd
:: 1. Compile all source files into bin/
compile.bat

:: 2. Launch the interactive application
run.bat

:: 3. Run the automated 10-test verification suite
test.bat
```

---

### 2. Manual Cross-Platform CLI Execution (Windows, macOS, Linux)

#### Compile
```bash
# Windows
javac -d bin -sourcepath src src\Main.java src\MetroMindTest.java src\model\*.java src\graph\*.java src\route\*.java src\fare\*.java src\service\*.java src\util\*.java

# macOS / Linux
javac -d bin -sourcepath src src/Main.java src/MetroMindTest.java src/model/*.java src/graph/*.java src/route/*.java src/fare/*.java src/service/*.java src/util/*.java
```

#### Run Interactive Application
```bash
java -cp bin Main
```

#### Run Verification Test Suite
```bash
java -cp bin MetroMindTest
```

---

## 🚶 Application Walkthrough

### Option 1: Find Best Route & Visual Track Map
- **Action:** Prompts for origin station, destination station, and passenger ticket category.
- **Intelligence:** Case-insensitive partial matching (e.g. typing `"silk board"` automatically resolves to `"Central Silk Board"`).
- **Output:** Renders the Visual Metro Track Schematic, corridor summary, stop count, transfer bridges with walk/dwell buffers, and an itemized fare calculation comparison across all 5 ticket types. Prompts to export a text receipt.

### Option 2: Explore Transit Lines, Stations & Interchanges
- **Action:** Submenu enabling commuters to inspect:
  1. All 5 operational transit lines and their station counts.
  2. Sequential stations along a chosen line.
  3. Major multi-line interchange hubs (*Majestic, MG Road, RV Road, Silk Board, KR Puram, etc.*).
  4. Search station by keyword/partial name.
  5. Alphabetical directory of all 63 network stations.

### Option 3: View Official BMRCL Fare Structure & Concession Slabs
- **Action:** Displays the distance pricing matrix (₹10 to ₹60), KIAL Airport Express surcharge rules, and concession percentages (10% to 30%).

### Option 4: Network Operations Simulator
- **Action:** Simulates real-world operational events:
  - Toggle station maintenance status (close or reopen a station).
  - Toggle track segment status (block or unblock a track connection).
  - Dynamically add a new metro station to the network.
  - Dynamically build a track connection between two stations.
- **Rerouting Behavior:** Subsequent route calculations immediately avoid closed stations and reroute commuters via alternative lines.

### Option 5: View Recent Journey History
- **Action:** Displays an in-memory audit log of past planned journeys (Trip ID, timestamp, origin, destination, distance, and final payable fare). Allows passengers to re-render the graphical track schematic for any past Trip ID.

### Option 6: Export Data (Stretch Features)
- **Action:** Generates text receipts and spreadsheets in the `exports/` folder:
  - Option 1: Export last journey as printable text ticket (`receipt_MM-*.txt`).
  - Option 2: Export last journey turn-by-turn path to CSV (`route_MM-*.csv`).
  - Option 3: Export entire 63-station network directory to CSV (`metro_network_stations.csv`).

### Option 7: Exit Application
- **Action:** Gracefully shuts down the console application with a farewell banner.

---

## 📊 Algorithm Complexity Analysis

The time and space complexity of MetroMind's core data structures and algorithms are formally analyzed below ($V = 63\text{ stations}$, $E \approx 130\text{ directed edges}$, $L = 5\text{ lines}$, $S = 6\text{ fare slabs}$):

| Operation / Component | Data Structure / Algorithm | Time Complexity | Space Complexity | Technical Explanation |
|---|---|---|---|---|
| **Graph Storage** | Adjacency List (`Map<Station, List<Connection>>`) | — | $\mathcal{O}(V + E)$ | Metro networks are sparse ($E \ll V^2$). Uses linear memory compared to $\mathcal{O}(V^2)$ for an adjacency matrix. |
| **Station Index Lookup** | `TreeMap<String, Station>` (Red-Black BST) | $\mathcal{O}(\log V)$ | $\mathcal{O}(V)$ | Maintains alphabetical station ordering for directory displays while ensuring logarithmic lookup time. |
| **Shortest Distance Search** | `dijkstraDistance()` (`PriorityQueue<PathNode>`) | $\mathcal{O}((V + E) \log V)$ | $\mathcal{O}(V)$ | Standard Dijkstra on a binary min-heap. Each node and edge is extracted and relaxed at most once. |
| **Fastest Travel Time Search** | `dijkstraTime()` (State Expansion) | $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ | $\mathcal{O}(V \cdot L)$ | State space expanded to $(u, L)$. With $L \le 5$, this adds minimal constant overhead while accurately modeling transfer penalties. |
| **Fewest Interchanges Search** | `dijkstraFewestInterchanges()` | $\mathcal{O}((V \cdot L + E) \log(V \cdot L))$ | $\mathcal{O}(V \cdot L)$ | Lexicographical cost function $(N \times 10,000) + D$ guarantees transfer minimization with distance tie-breaking. |
| **Commuter Friction Evaluation** | `computeFrictionScore()` | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | Evaluates the scalar formula over 3 precomputed candidate routes. |
| **Path Reconstruction** | Backtracking `.previous` pointers | $\mathcal{O}(P)$ | $\mathcal{O}(P)$ | Backtracks through $P$ stations on the path ($P \le V$) and reverses the list in-place. |
| **Fare Slab Resolution** | `getSlabForDistance()` | $\mathcal{O}(S) = \mathcal{O}(1)$ | $\mathcal{O}(1)$ | Iterates through 6 fixed distance slabs. Constant time. |
| **Station Partial Search** | Linear Substring Filter | $\mathcal{O}(V \cdot K)$ | $\mathcal{O}(V)$ | Scans $V = 63$ station names of average length $K \approx 18$ characters. Instantaneous (<0.1 ms). |

> [!TIP]
> **Real-World Performance:** In empirical benchmarks, computing all three Dijkstra variants, evaluating the friction score, calculating fares across all 5 pass types, and formatting the route schematic executes in **under 2 milliseconds** on a standard modern CPU.

---

## 🖥️ Sample Execution

Below is a verbatim execution trace from the live terminal application:

```text
+==========================================================================+
|             M E T R O M I N D   T R A N S I T   E N G I N E              |
|           Bengaluru Namma Metro  *  Routing & Fare Computation           |
+==========================================================================+

  Operational Network  63 stations  *  5 Namma Metro lines

  o Purple Line  Challaghatta  <->  Majestic  <->  Whitefield (Kadugodi)
  o Green Line   Madavara (BIEC)  <->  Yeshwanthpur  <->  Majestic  <->  Silk Institute
  o Yellow Line  RV Road  <->  Jayadeva  <->  Central Silk Board  <->  Electronic City
  o Pink Line    Kalena Agrahara  <->  MG Road  <->  Cantonment  <->  Nagawara
  o Blue Line    Central Silk Board  <->  Marathahalli  <->  KR Puram  <->  KIAL Airport

  [i] Ready to navigate your commute. Plan your journey below.

  +---------------------------------------------------------------------+
  |  MAIN MENU                                                          |
  +---------------------------------------------------------------------+
  |  [1]  Find Best Route & Visual Track Map                            |
  |       Smart route  .  graphical schematic  .  fare breakdown        |
  |  [2]  Explore Transit Lines, Stations & Interchanges                |
  |       Network directory  .  line listings  .  junction hubs         |
  |  [3]  View Official BMRCL Fare Structure & Concession Slabs         |
  |       Distance slabs  .  surcharges  .  passenger concessions       |
  |  [4]  Network Operations Simulator                                  |
  |       Maintenance  .  station/track closure  .  dynamic rerouting   |
  |  [5]  View Recent Journey History                                   |
  |       Last 25 journeys  .  re-examine trips                         |
  |  [6]  Export Data                                                   |
  |       Receipt (.txt)  .  route CSV  .  full network CSV             |
  |  [7]  Exit                                                          |
  +---------------------------------------------------------------------+

  > Select an option [1-7]: 1

+==========================================================================+
|   FIND BEST ROUTE -- OPTIMAL COMMUTER PATH                               |
+==========================================================================+

  > Enter Origin Station: Whitefield
    [+] Matched station: Whitefield (Kadugodi) [Purple Line]

  > Enter Destination Station: Lalbagh
    [+] Matched station: Lalbagh [Green Line]

  --- Select Ticket / Pass Category ----------------------------------------
   [1]  Standard Token / QR Ticket                  (0% discount)
   [2]  Namma Metro Smart Card (Varshik / NCMC)      (10% discount)
   [3]  Namma Metro Smart Card (Off-Peak / Weekend)  (20% discount)
   [4]  Student Concession Pass                     (25% discount)
   [5]  Senior Citizen Pass                         (30% discount)

  > Choose ticket type [1-5]: 2

+==========================================================================+
|                        VISUAL METRO TRACK SCHEMATIC                      |
+==========================================================================+
|  CORRIDOR OVERVIEW                                                       |
|  [ Whitefield (Kadugodi) ] ==[Purple Line . 11 stops]==> [ Majestic ] ==[Green Line . 4 stops]==> [ Lalbagh ]|
+==========================================================================+
|  DETAILED ROUTE SCHEMATIC                                                |
|  [START]  Whitefield (Kadugodi)                                          |
|  |   [Purple Line]  2.5 km  3.5 min                                      |
|  o  Sri Sathya Sai Hospital                                              |
|  |   [Purple Line]  2.1 km  3.0 min                                      |
|  o  Hoodi                                                                |
|  |   [Purple Line]  2.4 km  3.5 min                                      |
|  o  Garudacharpalya                                                      |
|  |   [Purple Line]  2.6 km  4.0 min                                      |
|  o KR Puram  [Junction]                                                  |
|  |   [Purple Line]  3.2 km  4.5 min                                      |
|  o  Baiyappanahalli                                                      |
|  |   [Purple Line]  1.5 km  2.5 min                                      |
|  o  Swami Vivekananda Road                                               |
|  |   [Purple Line]  1.1 km  2.0 min                                      |
|  o  Indiranagar                                                          |
|  |   [Purple Line]  3.4 km  5.0 min                                      |
|  o MG Road  [Junction]                                                   |
|  |   [Purple Line]  2.2 km  3.5 min                                      |
|  o  Vidhana Soudha                                                       |
|  |   [Purple Line]  1.8 km  3.0 min                                      |
|  o Nadaprabhu Kempegowda Station, Majestic  [Junction]                   |
|  |                                                                       |
|  +-- INTERCHANGE ------------------------------------------------        |
|  |  <> Nadaprabhu Kempegowda Station, Majestic                           |
|  |  From: Purple Line   ->   To: Green Line                              |
|  |  Walking + dwell: ~4.0 min                                            |
|  +------------------------------------------------------------           |
|  |   [Green Line]  1.4 km  2.5 min                                       |
|  o  Chickpet                                                             |
|  |   [Green Line]  1.2 km  2.0 min                                       |
|  o  Krishna Rajendra Market                                              |
|  |   [Green Line]  1.6 km  2.5 min                                       |
|  [ END ]  Lalbagh                                                        |
+==========================================================================+
|  JOURNEY SUMMARY                                                         |
|  Distance: 27.00 km   Est. Time: ~46 min   Stops: 14   Transfers: 1      |
+==========================================================================+

  --- Fare Computation Breakdown -------------------------------------------
   Selected Category:       Namma Metro Smart Card (Varshik / NCMC)
   Applicable Slab:         21.1 - 32.0 km
   Base Fare:               Rs.50.00
   Discount Applied:        -Rs.5.00  (10% concession)
  --------------------------------------------------------------------------
   FINAL PAYABLE AMOUNT:    Rs.45.00
  --------------------------------------------------------------------------

  --- All Fare Options Comparison ------------------------------------------
   Ticket Type                                 | Base      | Discount  | Payable
  --------------------------------------------------------------------------
   Standard Token / QR Ticket                  | Rs.50.00  | -Rs.0.00  | Rs.50.00
   Namma Metro Smart Card (Varshik / NCMC)     | Rs.50.00  | -Rs.5.00  | Rs.45.00
   Namma Metro Smart Card (Off-Peak / Weekend) | Rs.50.00  | -Rs.10.00 | Rs.40.00
   Student Concession Pass                     | Rs.50.00  | -Rs.12.50 | Rs.37.50
   Senior Citizen Pass                         | Rs.50.00  | -Rs.15.00 | Rs.35.00
  --------------------------------------------------------------------------

  > Export this journey receipt to a text file? (y/N): y
  [+] Receipt exported successfully to: exports\receipt_MM-1001.txt
```

---

## 🧪 Testing & Validation

MetroMind includes a standalone, zero-framework regression test suite implemented in [`MetroMindTest.java`](src/MetroMindTest.java). It exercises the transit graph, Dijkstra algorithms, fare matrices, dynamic disruptions, and file I/O:

```
==================================================
   METROMIND (NAMMA METRO) VERIFICATION SUITE     
==================================================
[TEST 1]  Bengaluru Station Directory & Connectivity  : PASSED (63 stations, 8 interchanges)
[TEST 2]  Direct Purple Line Route (Whitefield->Majestic): PASSED (Distance: 22.80 km, Stops: 11)
[TEST 3]  Direct Green Line Route (Madavara->Silk Inst): PASSED (Direct 0-transfer: 36.30 km)
[TEST 4]  Multi-Line Interchange Route (Whitefield->Lalbagh): PASSED (Transfers: 1 at Majestic)
[TEST 5]  Fare Calculation Slabs & Discounts         : PASSED (Token: ₹50, SmartCard: ₹45, Student: ₹37.5)
[TEST 6]  Airport Line Surcharge (Hebbal->KIAL)       : PASSED (Base: ₹50.0 + Surcharge: ₹20.0 = ₹70.0)
[TEST 7]  Dynamic Disruption Rerouting (Close Majestic): PASSED (Rerouted around closed hub)
[TEST 8]  File Export (Receipt .txt and Route .csv)   : PASSED (Exported files created in exports/)
[TEST 9]  Automated Best Route Optimization           : PASSED (Selected optimal commuter path)
[TEST 10] Terminal Graphical Track Schematic Rendering: PASSED (Visual schematic rendered successfully)
==================================================
   TEST RESULTS: 10 / 10 TESTS PASSED (100.0%)
==================================================
>>> ALL 10 TESTS PASSED! TRANSIT ENGINE FULLY VERIFIED! <<<
```

### Test Coverage Matrix

| Test ID | Test Scenario | Verified Assertions | Status |
|---|---|---|:---:|
| `TEST 1` | Network Topology Verification | 63 unique stations initialized, 8 multi-line interchange hubs identified. | **PASSED** |
| `TEST 2` | Direct Single-Line Routing | Correct station count (11 stops) and distance ($22.80\text{ km}$) with $0$ transfers. | **PASSED** |
| `TEST 3` | Maximum Line Traverse | Full Green Line traversal ($36.30\text{ km}$, Madavara to Silk Institute) with $0$ transfers. | **PASSED** |
| `TEST 4` | Interchange Detection | Accurate pivot identification at *Majestic* connecting Purple and Green lines. | **PASSED** |
| `TEST 5` | Fare Slabs & Concessions | Base slab matching ($21.1 - 32.0\text{ km} = \text{₹}50.00$) and discount calculations. | **PASSED** |
| `TEST 6` | Airport Express Surcharge | Surcharge trigger on KIAL stations ($+\text{₹}20.00$, Base ₹50.00 $\to$ ₹70.00). | **PASSED** |
| `TEST 7` | Dynamic Network Disruption | Toggling station closure removes it from viable paths and reroutes via Outer Ring Road. | **PASSED** |
| `TEST 8` | File Export Generation | Verifies file creation and content integrity for `.txt` receipts and `.csv` files. | **PASSED** |
| `TEST 9` | Best Route Friction Optimization | Confirms candidate comparison selects lowest commuter friction score. | **PASSED** |
| `TEST 10` | Terminal Graphical Rendering | Box drawing alignment, milestone nodes, and metrics bar rendering. | **PASSED** |

---

## 🛡️ Error Handling & Input Validation

MetroMind is engineered to be resilient against invalid inputs, malformed console interactions, and disconnected graph states:

| Scenario / Trigger | Application Handling | Commuter Experience |
|---|---|---|
| **Non-Existent Station Name** | `ConsoleUtils.promptSelectStation()` executes partial search. If no matches exist, warns user and prompts for retry. | Prevents `NullPointerException`; assists user with suggestions. |
| **Ambiguous Partial Station Match** | Search matching multiple stations (e.g. `"Road"`) lists all matching stations numbered 1 to $N$. | Interactive disambiguation menu allows passenger to choose exact target. |
| **Identical Origin & Destination** | Checked prior to graph search. Returns zero-cost `RouteResult` with duration $0.0\text{ min}$. | Informs passenger: `"Origin and destination are identical. Zero travel required."` |
| **Disconnected Network State** | If maintenance closures isolate destination, Dijkstra drains priority queue without reaching target. | Returns `RouteResult.notFound()`. Informs user track segments are closed for maintenance. |
| **Invalid Menu / Numeric Input** | `ConsoleUtils.readInt()` / `readDouble()` uses loop validation catching `NumberFormatException`. | Re-prompts cleanly: `"Please enter a number between 1 and 7."` |
| **Empty Input String** | `ConsoleUtils.readNonEmptyString()` rejects empty newlines. | Re-prompts cleanly: `"Input cannot be empty. Please enter a valid string."` |
| **File I/O Permission Error** | `FileExporter` wraps file operations in `try-with-resources` catching `IOException`. | Prevents crash; displays warning: `"Failed to export receipt: Permission denied."` |
| **Unsupported Terminal Charset** | `ConsoleUtils` tests stdout encoder for `\u2550` and `\u20B9`. | Automatically switches from Unicode boxes to standard ASCII (`+`, `-`, `=`, `|`) and `Rs.`. |

---

## ⚠️ Limitations & Future Roadmap

### Current Limitations
- [x] **In-Memory State Persistence:** Disruption toggles, dynamically added stations, and journey histories reside in JVM memory and reset upon application restart.
- [x] **Alphabetical Line Station Display:** Stations queried along a single line are displayed alphabetically via `TreeMap` rather than in strict geographic track sequence.
- [x] **Static Travel Times:** Speed profiles and inter-station durations are modeled as static averages; live real-time train positioning feeds are not integrated.
- [x] **Session History Capacity:** The recent journeys buffer retains the most recent 25 journeys per session.
- [x] **Airport Corridor Detection Heuristic:** Airport surcharge identification checks station naming patterns (`"airport"` / `"kial"`).

### Future Roadmap
- [ ] **Persistent Relational Database:** Implement an embedded SQLite / H2 database via JDBC to persist journey logs, user preferences, and network disruption events across sessions.
- [ ] **Geographic Station Ordering:** Refactor station collections on lines to use linked topological sequences for true directional track iteration (Up/Down tracks).
- [ ] **Live GTFS Real-Time Feed Integration:** Ingest live BMRCL General Transit Feed Specification (GTFS-RT) streams for real-time train arrival countdowns.
- [ ] **Multi-Modal Transit Integration:** Expand graph nodes to incorporate BMTC Feeder buses, auto stands, and bicycle hubs for first-mile/last-mile route planning.
- [ ] **Interactive Web / GUI Visualizer:** Develop a lightweight JavaFX or React + Vite front-end with interactive SVG metro maps.
- [ ] **JUnit 5 & CI/CD Pipeline:** Migrate standalone `MetroMindTest` assertions to JUnit 5 parameterized tests with GitHub Actions CI automation.
- [ ] **Dynamic Configuration Files:** Externalize distance fare slabs and surcharges to a YAML/JSON configuration file.

---

## 🎓 Learning Outcomes

MetroMind was developed as an advanced practical demonstration of core Computer Science, Data Structures & Algorithms (DSA), and Software Engineering principles:

### 1. Data Structures & Graph Theory
- **Sparse Graph Modeling:** Demonstrated why an **Adjacency List** ($\mathcal{O}(V + E)$) is superior to an Adjacency Matrix ($\mathcal{O}(V^2)$) for real-world transportation grids where average node degree is $\le 4$.
- **Binary Min-Heap Priority Queues:** Leveraged Java’s `PriorityQueue<PathNode>` with edge relaxation to achieve optimal logarithmic extraction time $\mathcal{O}(\log V)$.
- **State Space Expansion:** Overcame the fundamental limitation of standard single-node graphs by modeling transit transfers as state tuples $(u, L)$.
- **Hash-Indexed Lookups:** Utilized `HashMap` for constant-time cost tracking ($\mathcal{O}(1)$) alongside `TreeMap` for alphabetical directory sorting ($\mathcal{O}(\log V)$).

### 2. Software Architecture & Design Patterns
- **Facade Pattern:** Centralized application orchestration inside `MetroMindService`, keeping `Main` decoupled from graph mutations and Dijkstra internals.
- **Value Objects & Immutability:** Created thread-safe, immutable domain transfer objects (`RouteResult`, `FareBreakdown`, `RouteSegment`).
- **Rich Enumerations:** Encapsulated domain logic directly within enums (`MetroLine` stores transfer penalties; `TicketType` stores discount percentages).
- **Graceful Degradation:** Engineered terminal rendering utilities with dynamic character encoding detection to ensure compatibility across diverse operating systems.

---

## 📚 Documentation Reference

For deeper algorithmic proofs, line-by-line code explanations, and academic exam preparation, refer to the accompanying documentation guides:

| Document | Primary Audience | Scope & Content |
|---|---|---|
| 📖 **[explain.md](explain.md)** | Students & Junior Engineers | Comprehensive beginner-friendly walkthrough covering every class, method, data structure, pseudocode, and execution step. |
| 🎓 **[viva_dsa_guide.md](viva_dsa_guide.md)** | Evaluators & Academic Reviewers | Algorithmic proofs, Big-O complexity analysis, graph theory comparisons, and top 10 viva defense questions with model answers. |

---

## 👥 Authors & Academic Context

* **Project:** MetroMind — Shortest-Route and Fare Computation Engine
* **Track:** Advanced Java & Data Structures (DSA) Project
* **Edition:** Bengaluru Namma Metro (BMRCL) Edition

### Academic & Non-Affiliation Disclaimer
*This project is an independent academic software engineering project developed for educational purposes. All transit station names, line designations, and network topologies are modeled after publicly available information regarding the Bengaluru Namma Metro transit network. This project is not affiliated with, authorized by, or endorsed by Bangalore Metro Rail Corporation Limited (BMRCL).*

---

<div align="center">
  <sub>Built with pure Java • Designed for Namma Metro Commuters</sub>
</div>
