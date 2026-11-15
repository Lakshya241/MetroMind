# MetroMind: Shortest-Route and Fare Computation Engine
### Bengaluru Namma Metro (BMRCL) Edition
> **Project Code**: CP-042 | **Category**: Travel, Transport and Logistics | **Level**: Advanced  
> **Track Language**: Java (JDK 17+) | **Type**: Menu-Driven Console Application

MetroMind is a high-performance transit navigation and automated fare auditing engine designed for the **Bengaluru Namma Metro** transit network. It implements weighted graph algorithms (Dijkstra with state expansion) to compute optimal travel paths based on shortest distance, travel time with transfer delays, and transfer minimization, alongside dynamic fare slabs and concession tiers.

---

## Key Features

### 5 Core Features (Compulsory)
1. **Transit Graph Modeling & Station Directory**:
   - Models **63 stations** across **5 Namma Metro lines** (Purple, Green, Yellow, Pink, Blue) and major interchange hubs (Majestic, MG Road, Silk Board, KR Puram, Jayadeva Hospital, RV Road).
   - Real-time line exploration, station search with auto-suggest, and terminal directions.
2. **Shortest Physical Distance Routing**:
   - Computes minimum path in kilometers using Dijkstra's algorithm.
3. **Fastest Travel Time Routing**:
   - Computes fastest trip duration including interchange transfer walking penalties (e.g., 4-minute transfer buffers at Majestic or MG Road).
4. **Fewest Interchanges Routing**:
   - Minimizes train switches via lexicographical state-expanded optimization.
5. **Dynamic Fare & Ticketing Engine**:
   - Distance slab-based pricing (₹10 to ₹60).
   - High-speed KIAL Airport Express surcharge (₹20).
   - 5 Concession categories: Standard Token (0%), Namma Metro Smart Card Peak (10%), Smart Card Off-Peak (20%), Student Pass (25%), Senior Citizen (30%).

### Stretch Features (Implemented)
- **Ticket Receipt Export**: Generates printable `.txt` journey tickets (`exports/receipt_*.txt`).
- **Route Path CSV Export**: Exports turn-by-turn routing steps with distances to CSV (`exports/route_*.csv`).
- **Complete Station Directory CSV**: Exports full network topology to CSV (`exports/metro_network_stations.csv`).
- **Disruption Simulator**: Dynamically closes/reopens stations (e.g. closing Majestic) with live automatic rerouting via Outer Ring Road lines.
- **Recent Journey Audit History**: Caches recent trip plans for rapid re-inspection.

---

## Quick Start & Execution

### Windows One-Click Scripts
- **Compile Project**:
  ```cmd
  compile.bat
  ```
- **Run Interactive Application**:
  ```cmd
  run.bat
  ```
- **Run Automated Verification Tests**:
  ```cmd
  test.bat
  ```

### Manual Compilation & Execution
```bash
# Compile all source files
javac -d bin src/com/metromind/*.java src/com/metromind/**/*.java

# Run Console Application
java -cp bin com.metromind.Main

# Run Automated Test Suite
java -cp bin com.metromind.MetroMindTest
```

---

## Documentation
For an in-depth breakdown of algorithms, data structures, complexity analysis, and viva/code review defense preparation, see **[explain.md](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/explain.md)**.
