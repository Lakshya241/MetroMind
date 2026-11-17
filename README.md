<div align="center">

# MetroMind

**Bengaluru Namma Metro — Transit Routing & Fare Engine**

[![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/)
[![DSA](https://img.shields.io/badge/Algorithm-Dijkstra%20%2F%20State--Expanded-6C4AB6)](https://github.com/Lakshya241/MetroMind)
[![Network](https://img.shields.io/badge/Network-63%20Stations%20%7C%205%20Lines-28A745)](https://github.com/Lakshya241/MetroMind)
[![Tests](https://img.shields.io/badge/Tests-10%2F10%20Passing-brightgreen)](https://github.com/Lakshya241/MetroMind)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-Lakshya241%2FMetroMind-181717?logo=github)](https://github.com/Lakshya241/MetroMind)

</div>

---

## Overview

MetroMind is a **Java command-line transit routing and fare computation engine** that models the Bengaluru Namma Metro (BMRCL) network as a weighted directed graph. It finds optimal routes across 63 stations on 5 lines using multiple Dijkstra variants, renders ASCII track schematics, computes BMRCL distance-slab fares with concession passes, and simulates live network disruptions.

> **Note:** This is a project model. Fares, distances, and station data are approximations and do not represent live BMRCL information.

---

## Features

- 🗺️ **Multi-objective routing** — shortest distance, fastest time, fewest transfers, and a scored *recommended route*
- 🔀 **State-expanded Dijkstra** — models per-line transfer penalties (3–4 min) for accurate travel-time calculation
- 💡 **Commuter Friction Heuristic** — `Score = Time + (Transfers × 7) + (Distance × 0.15)` to recommend the most practical route
- 🖥️ **ASCII track visualizer** — corridor overview and station-by-station terminal schematic with transfer boxes
- 🎫 **BMRCL fare engine** — 6 distance slabs (₹10–₹60), ₹20 Airport surcharge, 5 ticket/concession types
- 🚧 **Disruption simulator** — close/reopen stations or track segments; routing avoids unavailable infrastructure instantly
- 📋 **Journey history** — session log of last 25 trips with timestamps, trip IDs, and re-renderable schematics
- 📁 **Export** — printable text receipts (`.txt`) and analytical CSVs (route path, full network directory)
- ✅ **Built-in test suite** — 10 automated verification tests covering routing, fares, disruptions, and exports

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      USER  (CLI)                        │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              APPLICATION LAYER  (Main.java)             │
│                                                         │
│  ConsoleUtils · MetroMindService · FareEngine           │
│  NetworkDataInitializer · AsciiRouteVisualizer          │
│  FileExporter · Verification Test Suite                 │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│          DSA CORE  (MetroDSA.java)                      │
│                                                         │
│  MetroGraph (Adjacency List)  ·  Station (Vertex)       │
│  Connection (Weighted Edge)   ·  PathNode (PQ State)    │
│  RouteEngine: dijkstraDistance · dijkstraTime           │
│               dijkstraFewestInterchanges · findBestRoute│
└─────────────────────────────────────────────────────────┘
```

| Algorithm | Objective | Complexity |
|---|---|---|
| `dijkstraDistance` | Minimize track distance (km) | O((V + E) log V) |
| `dijkstraTime` | Minimize travel time with transfer delays | O((V·L + E) log(V·L)) |
| `dijkstraFewestInterchanges` | Minimize transfers, then distance | O((V·L + E) log(V·L)) |
| `findBestRoute` | Commuter friction score across all 3 candidates | O(1) evaluation |

---

## Network

5 lines · 63 stations · 8 interchange hubs

| Line | Route | Stations |
|---|---|---|
| 🟣 Purple | Challaghatta ↔ Whitefield (Kadugodi) | 16 |
| 🟢 Green | Madavara (BIEC) ↔ Silk Institute | 17 |
| 🟡 Yellow | RV Road ↔ Bommasandra (Electronic City) | 11 |
| 🩷 Pink | Kalena Agrahara ↔ Nagawara | 11 |
| 🔵 Blue | Central Silk Board ↔ KIAL Airport Terminal | 13 |

**Key interchanges:** Majestic (Purple↔Green) · MG Road (Purple↔Pink) · Central Silk Board (Yellow↔Blue) · KR Puram (Purple↔Blue) · Jayadeva Hospital (Yellow↔Pink) · RV Road (Green↔Yellow) · Nagawara (Pink↔Blue) · Yeshwanthpur (Green↔Blue)

---

## Fare Structure

| Distance | Base Fare | Airport Surcharge |
|---|---|---|
| 0 – 2 km | ₹10 | +₹20 (KIAL journeys) |
| 2.1 – 5 km | ₹20 | |
| 5.1 – 12 km | ₹30 | |
| 12.1 – 21 km | ₹40 | |
| 21.1 – 32 km | ₹50 | |
| > 32 km | ₹60 | |

Concession passes: Token (0%) · Smart Card (10%) · Smart Card Off-Peak (20%) · Student (25%) · Senior Citizen (30%) · Minimum fare floor: ₹10

---

## Getting Started

### Prerequisites

- JDK 11 or later
- `java` and `javac` on your `PATH`

### Run (Windows — Recommended)

```bat
run.bat
```

```bat
run.bat --test
```

### Run (Any Platform)

```bash
# Compile
javac *.java

# Launch interactive app
java Main

# Run verification tests
java Main --test
```

---

## Project Structure

```
MetroMind/
├── Main.java          # CLI, fare engine, network setup, visualizer, exports, tests
├── MetroDSA.java      # Graph model and Dijkstra routing algorithms
├── run.bat            # Windows one-click compile-and-run launcher
├── EXPLANATION.md     # Full implementation & DSA deep-dive
└── exports/           # Auto-generated receipts (.txt) and CSVs
```

For a complete walkthrough of every class, algorithm, and design decision, see [`EXPLANATION.md`](EXPLANATION.md).

---

## Menu Reference

| Option | Feature |
|---|---|
| `[1]` | Find Best Route & ASCII Track Map |
| `[2]` | Explore Transit Network (lines, stations, interchanges) |
| `[3]` | View Official Fare Structure |
| `[4]` | Network Operations Simulator (close / reopen stations & tracks) |
| `[5]` | Recent Journey History |
| `[6]` | Export Data (receipt `.txt`, route `.csv`, network directory `.csv`) |
| `[7]` | Run Verification Tests (10/10) |
| `[8]` | Exit |

---

## Author

**Lakshya241** · [GitHub](https://github.com/Lakshya241) · [Repository](https://github.com/Lakshya241/MetroMind)
