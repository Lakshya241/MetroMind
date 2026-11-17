# MetroMind: Shortest-Route and Fare Computation Engine
### Bengaluru Namma Metro (BMRCL) Edition
> **Category**: Travel, Transport & Logistics | **Language**: Java (JDK 11+) | **Structure**: Modular 2-File Architecture

MetroMind is a high-performance transit routing, fare auditing, and track visualization engine for the **Bengaluru Namma Metro** network. It computes the **Best Recommended Route** for commuters and renders a **Graphical Track Schematic** directly in your terminal.

---

## 📁 Clean Project Structure

```
.
├── MetroDSA.java     # Pure DSA Layer (Graph Adjacency List, Dijkstra Algorithms, Heuristics)
├── Main.java         # Application Layer (CLI Menus, Fare Engine, Network Initializer, Tests)
├── run.bat           # 1-click Windows launcher (javac *.java && java Main)
├── README.md         # Quick start guide & summary
├── EXPLANATION.md    # Complete in-depth manual explaining all code & DSA concepts
├── .gitignore        # Ignores *.class binaries and exports/
└── exports/          # Auto-generated ticket receipts (.txt) and route CSVs
```

---

## ⚡ Quick Start (Run in 1 Step)

No external libraries, Maven, or Gradle needed. Just pure standard Java.

### Option 1: Windows 1-Click Batch Script
- Double-click **`run.bat`** to start the interactive application.
- Or run in terminal: `run.bat --test` to run the 10 automated verification tests.

### Option 2: Command Line (Java 11+)
```bash
# Compile both Java source files
javac *.java

# Launch interactive application
java Main

# Run automated verification tests (10/10)
java Main --test
```

---

## 🚀 Key Features

1. **Automated Best Route Recommendation**:
   - Evaluates travel duration, transfer convenience, and physical distance using a commuter friction score to recommend the optimal path:
     $$\text{Score} = \text{Time (mins)} + (\text{Interchanges} \times 7.0) + (\text{Distance (km)} \times 0.15)$$
2. **Terminal Graphical Track Schematic**:
   - Renders a visual schematic track map directly inside the console with station symbols (`[START] *`, intermediate `o`, transfer boxes with platform dwell buffers, and `[DESTINATION] #`).
3. **Complete Bengaluru Namma Metro Network**:
   - **63 stations across 5 lines**: Purple, Green, Yellow, Pink, and Blue (Airport & ORR) lines.
   - **Key Interchange Hubs**: *Majestic, MG Road, Central Silk Board, KR Puram, Jayadeva Hospital, RV Road, Nagawara, Yeshwanthpur*.
4. **Dynamic Fare & Ticketing Engine**:
   - Distance slab-based pricing (₹10 to ₹60).
   - High-speed KIAL Airport Express surcharge (₹20).
   - 5 Concession categories: Standard Token (0%), Smart Card Peak (10%), Smart Card Off-Peak (20%), Student Pass (25%), Senior Citizen (30%).
5. **Network Disruption Simulator**:
   - Dynamically close or reopen stations and tracks for maintenance, with live rerouting around disruptions.
6. **Data Exports**:
   - Printable ticket receipts (`exports/receipt_*.txt`).
   - Turn-by-turn route path CSV (`exports/route_*.csv`).
   - Complete station directory CSV (`exports/metro_network_stations.csv`).
7. **Built-in 10/10 Verification Test Suite**:
   - Accessible via Menu Option `[7]` or CLI flag `--test`.

---

## 🧠 Data Structures & Algorithms Summary

| Component | File | Data Structure / Algorithm | Time Complexity | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Network Graph** | `MetroDSA.java` | Adjacency List (`Map<Station, List<Connection>>`) | $\mathcal{O}(V + E)$ space | Bidirectional metro topology |
| **Shortest Distance** | `MetroDSA.java` | Dijkstra with Min-Priority Queue | $\mathcal{O}((V + E) \log V)$ | Minimizes total physical kilometers |
| **Fastest Travel Time**| `MetroDSA.java` | State-Expanded Dijkstra $(\text{Station}, \text{Line})$ | $\mathcal{O}((V \cdot L + E) \log (V \cdot L))$ | Accounts for track time + transfer penalties |
| **Fewest Transfers** | `MetroDSA.java` | Lexicographical Dijkstra | $\mathcal{O}((V \cdot L + E) \log (V \cdot L))$ | Minimizes platform interchanges |
| **Best Commuter Route**| `MetroDSA.java` | Friction Score Heuristic | $\mathcal{O}(1)$ evaluation | Balances time, walking transfer strain, and distance |
| **Station Directory**| `MetroDSA.java` | Case-Insensitive `TreeMap` | $\mathcal{O}(\log V)$ lookup | Alphabetical indexing & prefix search |

---

## 📖 In-Depth Manual & Explanation

For an exhaustive, line-by-line explanation of every class, method, data structure, algorithm, and viva interview questions, read **[`EXPLANATION.md`](file:///c:/Users/laksh/OneDrive/Desktop/New%20folder%20(6)/EXPLANATION.md)**.
