# MetroMind: Shortest-Route and Fare Computation Engine
### Bengaluru Namma Metro (BMRCL) Edition
> **Project Code**: CP-042 | **Category**: Travel, Transport and Logistics | **Level**: Advanced  
> **Track Language**: Java (JDK 17+) | **Type**: Menu-Driven Console Application with Terminal Graphical Maps

MetroMind is a high-performance transit navigation and automated fare auditing engine designed for the **Bengaluru Namma Metro** transit network. It automatically computes the **Best Recommended Route** for commuters and renders a **Graphical Track Schematic** directly inside the terminal.

---

## Key Features

1. **Automated Best Route Selection**:
   - Evaluates travel duration, transfer convenience, and physical distance using a commuter friction score to directly recommend the best path.
2. **Terminal Graphical Track Visualization**:
   - Renders a visual schematic track map directly in the console with station symbols (`[START] *`, intermediate `o`, transfer boxes, and `[DESTINATION] #`).
3. **Namma Metro Transit Network (63 Stations, 5 Lines)**:
   - Full modeling of the **Purple, Green, Yellow, Pink, and Blue (Airport & ORR)** lines.
   - Major interchange hubs: *Majestic, MG Road, Central Silk Board, KR Puram, Jayadeva Hospital, RV Road, Nagawara*.
4. **Dynamic Fare & Ticketing Engine**:
   - Distance slab-based pricing (₹10 to ₹60).
   - High-speed KIAL Airport Express surcharge (₹20).
   - 5 Concession categories: Standard Token (0%), Smart Card Peak (10%), Smart Card Off-Peak (20%), Student Pass (25%), Senior Citizen (30%).
5. **Network Disruption Simulator**:
   - Dynamically close/reopen stations or tracks with live rerouting via the Outer Ring Road corridor.
6. **Data Exports (Stretch Features)**:
   - Printable text ticket receipts (`exports/receipt_*.txt`).
   - Turn-by-turn route path CSV (`exports/route_*.csv`).
   - Complete station directory CSV (`exports/metro_network_stations.csv`).

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
javac -d bin -sourcepath src src\com\metromind\Main.java src\com\metromind\MetroMindTest.java src\com\metromind\model\*.java src\com\metromind\graph\*.java src\com\metromind\routing\*.java src\com\metromind\fare\*.java src\com\metromind\service\*.java src\com\metromind\util\*.java

# Run Console Application
java -cp bin com.metromind.Main

# Run Automated Test Suite
java -cp bin com.metromind.MetroMindTest
```

---

## Documentation
* **[`explain.md`](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/explain.md)**: Full system architecture, Bengaluru network design, and problem-solution manual.
* **[`viva_dsa_guide.md`](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/viva_dsa_guide.md)**: Algorithms, Data Structures (DSA), and Top Viva Questions & Answers.
