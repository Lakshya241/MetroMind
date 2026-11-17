<div align="center">

# MetroMind

**Plan practical routes and estimate fares across a modeled Bengaluru Namma Metro network.**

[![Java](https://img.shields.io/badge/Java-11%2B-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/)
[![Console application](https://img.shields.io/badge/interface-console-2f6f9f)](#)
[![Repository](https://img.shields.io/badge/GitHub-Lakshya241%2FMetroMind-181717?logo=github)](https://github.com/Lakshya241/MetroMind)

</div>

MetroMind is a Java command-line transit routing and fare computation project. It models stations and connections as a graph, compares routes by distance, travel time, or transfers, and provides a recommended route with a fare breakdown. The network data is a project model; fares and station details should not be treated as live BMRCL information.

## Features

- **Route planning:** shortest-distance, fastest-time, fewest-transfer, and recommended-route options.
- **Route visualization:** terminal schematic with line segments, transfer points, and journey metrics.
- **Modeled network:** 63 stations across Purple, Green, Yellow, Pink, and Blue (Airport & ORR) lines.
- **Fare estimates:** distance slabs, ticket concessions, a ₹10 minimum fare, and a ₹20 KIAL Airport surcharge.
- **Disruption simulation:** close or reopen stations and track segments; subsequent route searches avoid unavailable connections.
- **Network exploration:** browse lines, stations, and interchanges, or search station names.
- **Exports and tests:** write journey receipts and CSV data; run the built-in 10-test verification suite.

## Preview

This is an interactive terminal application. Its route view prints a corridor overview and an ASCII track schematic; no screenshot assets are included in the repository.

## Tech stack

- **Language:** Java (JDK 11 or later)
- **Dependencies:** Java standard library; no Maven or Gradle required
- **Interface:** interactive command-line menu

## Architecture

```text
CLI user
   │
   ▼
Main.java — menus, service, network setup, fare engine, visualization, exports, tests
   │
   ▼
MetroDSA.java — graph model and route algorithms
```

The network uses an adjacency list and a case-insensitive `TreeMap` station registry. Dijkstra-based searches optimize distance and time; travel-time search tracks `(station, line)` state to account for transfers. The fewest-transfer search prioritizes transfers, then distance. Recommended-route selection scores candidate routes with:

```text
Score = time (minutes) + 7 × transfers + 0.15 × distance (km)
```

## Getting started

### Prerequisites

Install JDK 11 or later and ensure `java` and `javac` are available on your `PATH`.

### Run from the repository root

```bash
javac *.java
java Main
```

Run the verification suite instead:

```bash
java Main --test
```

On Windows, `run.bat` compiles the Java files and launches the application. Pass `--test` to run the test suite:

```bat
run.bat
run.bat --test
```

## Project structure

```text
MetroMind/
├── Main.java        # CLI, fare engine, network setup, exports, verification tests
├── MetroDSA.java    # Graph model and routing algorithms
├── run.bat          # Windows compile-and-run launcher
├── EXPLANATION.md   # Detailed implementation and DSA guide
└── exports/         # Destination for generated receipts and CSV exports
```

Generated exports include `receipt_*.txt`, `route_*.csv`, and `metro_network_stations.csv`.

## Author

**Lakshya241** · [GitHub profile](https://github.com/Lakshya241) · [Repository](https://github.com/Lakshya241/MetroMind)

For the implementation walkthrough and algorithm details, see [`EXPLANATION.md`](EXPLANATION.md).
