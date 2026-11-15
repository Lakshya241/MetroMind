# MetroMind: Shortest-Route and Fare Computation Engine
## Project Code: CP-042 | Domain: Travel, Transport and Logistics
### Bengaluru Namma Metro Edition (BMRCL Network)

---

## 1. What the Solution Is (Executive Summary)

**MetroMind** is an advanced transit pathfinding and dynamic fare computation engine built as a menu-driven console application in pure Java (JDK 17). It represents the **Bengaluru Namma Metro (BMRCL)** urban rapid rail network as a weighted, bidirectional graph.

The solution directly addresses commuter navigation with:
1. **Automated Best-Route Optimization**: Automatically calculates and delivers the single **Best Route** balancing travel duration, physical distance, and minimal platform transfers.
2. **Terminal Graphical Track Visualization**: Renders an intuitive visual ASCII/Unicode track schematic directly in the terminal, illustrating station stops, line colors, and transfer interchange bridges.
3. **Dynamic BMRCL Fare Engine**: Evaluates transit fares across distance slabs (₹10 to ₹60), applies the high-speed **KIAL Airport Express Corridor** surcharge, and itemizes passenger concession discounts (Smart Card, Student, Senior Citizen).
4. **Resilient Network Management & Disruption Simulator**: Enables live maintenance toggling (closing stations or tracks for repairs) with automatic real-time rerouting around blockages via the Outer Ring Road corridor.
5. **Data Exports (Stretch Features)**: Exports trip receipts (`exports/receipt_*.txt`), analytical turn-by-turn route logs (`exports/route_*.csv`), and full station directories (`exports/metro_network_stations.csv`).

---

## 2. Clean Architecture & Project Structure

The project has zero external dependencies, running out-of-the-box with standard Java 17:

```
src/
└── com/
    └── metromind/
        ├── Main.java                        <-- Direct menu-driven console application
        ├── MetroMindTest.java               <-- Automated verification suite (100% pass)
        ├── model/
        │   ├── MetroLine.java               <-- Enum of Namma Metro lines with transfer penalties
        │   ├── Station.java                 <-- Vertex node with line memberships & active state
        │   ├── Connection.java              <-- Weighted edge with distance, time & operational state
        │   ├── TicketType.java              <-- Concession categories & discount percentages
        │   ├── RouteSegment.java            <-- Leg of a journey on a single transit line
        │   ├── RouteResult.java             <-- Complete itinerary, station list & transfer points
        │   ├── FareBreakdown.java           <-- Granular itemization of fare, slabs & discounts
        │   └── JourneySummary.java          <-- High-level receipt combining route & fare audit
        ├── graph/
        │   └── MetroGraph.java              <-- Adjacency-list graph data structure
        ├── routing/
        │   ├── PathNode.java                <-- Priority Queue state wrapper for Dijkstra
        │   └── RouteEngine.java             <-- Best-route & multi-criteria routing algorithms
        ├── fare/
        │   └── FareEngine.java              <-- Distance slab resolver & concession calculator
        ├── service/
        │   └── MetroMindService.java        <-- Facade coordinating graph, routing & fare logic
        └── util/
            ├── AsciiRouteVisualizer.java    <-- Terminal graphical track map renderer
            ├── NetworkDataInitializer.java  <-- Pre-populates 63 stations & 5 Namma Metro lines
            ├── FileExporter.java            <-- Stretch feature: Text receipts & CSV exports
            └── ConsoleUtils.java            <-- Terminal formatting & safe input sanitizers
```

---

## 3. Bengaluru Namma Metro Network Architecture

The network model encompasses **63 stations** across **5 key lines**:

1. **Purple Line (East-West Corridor - Challaghatta to Whitefield)**:
   - Connects *Challaghatta, Kengeri, Mysuru Road, Vijayanagar, KSR Bengaluru Railway Station, Nadaprabhu Kempegowda Station (Majestic), Vidhana Soudha, MG Road, Indiranagar, Swami Vivekananda Road, Baiyappanahalli, KR Puram, Hoodi, Sri Sathya Sai Hospital, Whitefield (Kadugodi)*.
2. **Green Line (North-South Corridor - Madavara to Silk Institute)**:
   - Connects *Madavara (BIEC), Nagasandra, Peenya Industry, Yeshwanthpur, Sandal Soap Factory, Rajajinagar, Mantri Square Sampige Road, Majestic, Chickpet, KR Market, Lalbagh, South End Circle, Jayanagar, RV Road, Banashankari, JP Nagar, Yelachenahalli, Silk Institute*.
3. **Yellow Line (South & Tech Corridor - RV Road to Bommasandra / Electronics City)**:
   - Connects *RV Road, Ragigudda, Jayadeva Hospital, BTM Layout, Central Silk Board, Bommanahalli, Kudlu Gate, Singasandra, Electronic City, Infosys Foundation Konappana, Bommasandra*.
4. **Pink Line (South-North Tunnel & Cantonment Corridor - Kalena Agrahara to Nagawara)**:
   - Connects *Kalena Agrahara, IIM Bangalore, JP Nagar 4th Phase, Jayadeva Hospital, Dairy Circle, Lakkasandra, Langford Town, MG Road, Shivaji Nagar, Bengaluru Cantonment, Tannery Road, Nagawara*.
5. **Blue Line (Outer Ring Road & Airport Express - Silk Board to KIAL Airport Terminal)**:
   - Connects *Central Silk Board, HSR Layout, Bellandur (Ecospace), Kadubeesanahalli, Marathahalli, Doddanekundi, KR Puram, Kalyan Nagar, Nagawara, Hebbal, Yeshwanthpur (ORR Connector), Yelahanka, Doddajala, KIAL Airport Terminal*.

### Key Interchange Hubs:
- **Nadaprabhu Kempegowda Station, Majestic**: Mega Interchange (Purple $\leftrightarrow$ Green).
- **MG Road**: Central Business District Interchange (Purple $\leftrightarrow$ Pink).
- **KR Puram (Krishnarajapuram)**: Major IT Hub Interchange (Purple $\leftrightarrow$ Blue).
- **Rashtreeya Vidyalaya (RV) Road**: South Bengaluru Interchange (Green $\leftrightarrow$ Yellow).
- **Jayadeva Hospital**: Multi-level Interchange (Yellow $\leftrightarrow$ Pink).
- **Central Silk Board**: Vital Tech Junction (Yellow $\leftrightarrow$ Blue).
- **Nagawara**: North Bengaluru Interchange (Pink $\leftrightarrow$ Blue).
- **Yeshwanthpur / Hebbal**: Orbital ORR Connector (Green $\leftrightarrow$ Blue).

---

## 4. How the Solution Works

### 4.1 Automated Best-Route Selection
In `RouteEngine.java`, `findBestRoute()` evaluates candidate paths across three criteria:
- Minimum travel time (minutes)
- Minimum line switches (interchanges)
- Minimum physical distance (km)

It scores candidate paths using a commuter friction function:
$$\text{Score} = \text{Travel Time} + (\text{Interchanges} \times 7.0) + (\text{Distance} \times 0.15)$$
The route with the lowest friction score is selected as the **Best Recommended Route**.

### 4.2 Terminal Graphical Track Visualizer
In `AsciiRouteVisualizer.java`, the system renders a text-mode graphical schematic:
- **Corridor Overview**: High-level station-to-station chain with line names.
- **Detailed Track Schematic**: Box-drawing elements displaying origin (`[START] *`), intermediate stops (`o`), junction markers, and transfer bridge blocks showing walking/dwell buffers.
- **Milestone Metrics**: Total kilometers, minutes, station count, and transfers.

### 4.3 Fare Computation Engine
The `FareEngine.java` computes ticket costs based on distance slabs and passenger categories:
- `0.0 - 2.0 km`: ₹ 10.00
- `2.1 - 5.0 km`: ₹ 20.00
- `5.1 - 12.0 km`: ₹ 30.00
- `12.1 - 21.0 km`: ₹ 40.00
- `21.1 - 32.0 km`: ₹ 50.00
- `> 32.0 km`: ₹ 60.00
- **Airport Surcharge**: +₹20.00 for journeys touching *KIAL Airport Terminal*.
- **Concessions**: Standard Token (0%), Smart Card Peak (10%), Smart Card Off-Peak (20%), Student Pass (25%), Senior Citizen (30%).

---

## 5. Verification Checklist

| Rule Requirement | Implementation in MetroMind | Status |
| :--- | :--- | :--- |
| **Language** | Pure Java (JDK 17), zero external dependencies | **COMPLIANT** |
| **Interface** | Direct menu-driven console CLI | **COMPLIANT** |
| **Core Feature 1** | Metro Network Modeling & Station Directory (63 stations, 5 lines) | **COMPLETED** |
| **Core Feature 2** | Automated Best Route Finding (Commuter friction optimization) | **COMPLETED** |
| **Core Feature 3** | Terminal Graphical Visualization (ASCII/Unicode track map) | **COMPLETED** |
| **Core Feature 4** | Platform Transfer Penalties (Real-world walking/waiting buffers) | **COMPLETED** |
| **Core Feature 5** | Dynamic BMRCL Fare Engine (Slabs, Airport surcharge, concessions) | **COMPLETED** |
| **Stretch Feature 1** | Export Journey Receipt to formatted text file (`exports/receipt_*.txt`) | **COMPLETED** |
| **Stretch Feature 2** | Export Turn-by-Turn Route Path to analytical CSV (`exports/route_*.csv`) | **COMPLETED** |
| **Stretch Feature 3** | Export Complete Metro Network Directory to CSV (`exports/metro_network_stations.csv`) | **COMPLETED** |
| **Stretch Feature 4** | Station Maintenance / Disruption Simulator with live rerouting | **COMPLETED** |
| **Automated Tests** | `MetroMindTest.java` running 10 unit & integration tests | **100% PASSED** |

---

## 6. How to Compile and Run

### Option 1: Using Windows Batch Scripts
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

### Option 2: Using Standard CLI Commands
```powershell
# Compile all source files
javac -d bin -sourcepath src src\Main.java src\MetroMindTest.java src\model\*.java src\graph\*.java src\route\*.java src\fare\*.java src\service\*.java src\util\*.java

# Launch Console Application
java -cp bin Main

# Run Automated Test Suite
java -cp bin MetroMindTest
```
