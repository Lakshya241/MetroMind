# MetroMind: A Beginner's Complete Guide

> **Who is this for?** Anyone who knows basic Java (variables, loops, classes) but hasn't yet studied data structures or algorithms in depth. We're going to walk through every part of MetroMind — what it does, why it's built the way it is, and what computer-science ideas power it.

[📖 Main Project README](README.md) • [🎓 Viva & DSA Defense Guide](viva_dsa_guide.md)

---

# Part 1: Understanding the Project

## 1. What is MetroMind?

MetroMind is a **route-planning and fare-calculation engine** for the Bengaluru Namma Metro system — built entirely in Java.

You give it two station names (say, "Majestic" and "Whitefield") and it figures out:
- The **best route** to take (shortest, fastest, or fewest changes)
- Exactly **how long** the journey takes
- How **far** you'll travel in kilometres
- The **fare** you'll pay, with any applicable discounts

It then draws a pretty ASCII map of your route right in the terminal, and can save a receipt to a file.

Think of it as a mini-Google-Maps for Namma Metro, running entirely on your computer — no internet required.

---

## 2. Why Was It Built?

Here's the problem MetroMind is solving:

Bengaluru's Namma Metro has **five lines** (Purple, Green, Yellow, Pink, Blue), **63 stations**, and several interchange points where lines cross. If you want to go from one station to another, you might have multiple options:

- Route A: short distance, but lots of line changes
- Route B: longer distance, but direct — less waiting around
- Route C: moderate distance, fastest travel time

A human can't easily compare all three routes in their head, especially across a 63-station network. MetroMind does this computation in **milliseconds** using Dijkstra's algorithm (more on that in Part 3).

It also automates fare calculation, which involves distance slabs, ticket-type discounts, and an airport surcharge — another thing that's error-prone to do manually.

---

## 3. What Technologies Does It Use?

MetroMind is written in **pure Java (JDK 17+)** with **zero external libraries**.

That means:
- No Maven dependencies
- No Gradle plugins
- No third-party route-planning libraries
- No database connectors

**Why is that notable?** When developers build real-world apps, they often reach for ready-made libraries that do the heavy lifting. Using no libraries means we had to implement everything from scratch — the graph, the pathfinding algorithm, the priority queue logic, the fare engine, the file exporter. That's a much deeper learning experience, and it shows you *exactly* how these things work under the hood.

The only things used are Java's own standard library: `java.util.HashMap`, `java.util.PriorityQueue`, `java.util.Scanner`, `java.io.FileWriter`, etc. These come bundled with every Java installation.

---

## 4. The Big Picture: How Does It Work?

Let's trace what happens when you ask MetroMind to plan a journey from **Baiyappanahalli** to **Kempegowda International Airport**:

```
User types origin & destination
         ↓
Partial-match search finds the stations
         ↓
User selects ticket type (Regular, Student, etc.)
         ↓
MetroMindService.planBestJourney() is called
         ↓
RouteEngine runs 3 Dijkstra variants:
  → dijkstraDistance  (minimize km)
  → dijkstraTime      (minimize minutes)
  → dijkstraFewestInterchanges (minimize line changes)
         ↓
Best route picked by "friction score"
         ↓
FareEngine calculates fare from distance + ticket type + surcharges
         ↓
AsciiRouteVisualizer draws the terminal map
         ↓
Fare breakdown is printed
         ↓
JourneySummary is saved to history (up to 25 journeys)
```

Each of those steps is handled by a separate class. This separation of responsibilities is a core principle of good software design called the **Single Responsibility Principle** — each class does one thing well.

---

# Part 2: The Network — How Metro Data Is Stored

## 5. What is a Graph?

Before we look at any code, we need to understand **graphs** — not the bar-chart kind, but the mathematical kind.

A **graph** is simply a set of **nodes** (things) connected by **edges** (relationships between things).

For a metro network:
- Each **station** is a **node**
- Each **track** between two stations is an **edge**

That's it. The whole Namma Metro network is a graph with 63 nodes and around 130 edges (bidirectional — each track goes both ways).

Here's a tiny example:

```
[Majestic] ——————— [Vidhana Soudha] ——————— [Cubbon Park]
     |
[Nadaprabhu Kempegowda Layout]
```

In this mini-graph:
- Majestic, Vidhana Soudha, Cubbon Park, and Nadaprabhu are **nodes**
- The lines between them are **edges**

The graph structure lets us answer questions like: *"What is the shortest path from Majestic to Cubbon Park?"*

---

## 6. What is an Adjacency List?

There are two classic ways to store a graph in memory:

**Option 1: Adjacency Matrix** — a 2D array where `matrix[i][j] = 1` if station i connects to station j. For 63 stations, that's a 63×63 grid = 3,969 cells, most of them empty. Wasteful.

**Option 2: Adjacency List** — for each station, store a *list of its neighbors*. Only connections that actually exist take up memory.

An adjacency list is like asking each person "who are your neighbors?" and writing down the answers:

```
Majestic        → [Vidhana Soudha (Purple), Nadaprabhu Layout (Green)]
Vidhana Soudha  → [Majestic (Purple), Cubbon Park (Purple)]
Cubbon Park     → [Vidhana Soudha (Purple), MG Road (Purple)]
```

MetroMind uses a `HashMap<Station, List<Connection>>` for this — the key is a Station, the value is the list of all connections leaving that station.

**Why HashMap?** Looking up a station's neighbors is an O(1) operation (constant time, regardless of network size). That's very fast.

---

## 7. MetroGraph Class

---
### MetroGraph

**What is it?** The central data structure that holds the entire Namma Metro network — every station and every track.

**Why do we need it?** The route-finding algorithm needs a single place to ask "given a station, what are its neighbors?" MetroGraph provides that in constant time.

**Important fields:**
- `adjacencyList` (`HashMap<Station, List<Connection>>`) — maps each station to its outgoing connections
- `stationIndex` (`TreeMap<String, Station>`) — maps lowercase station names to Station objects for fast lookup

**Important methods:**
- `addStation(Station)` — registers a station in both maps
- `addConnection(Connection)` — adds a directed edge from `from` to `to` in the adjacency list
- `getNeighbors(Station)` — returns the list of connections leaving a station
- `findStation(String name)` — does a partial-match search through `stationIndex` keys

**Simple example:**

```java
MetroGraph graph = new MetroGraph();
graph.addStation(majestic);
graph.addStation(vidhana);
graph.addConnection(new Connection(majestic, vidhana, PURPLE, 0.8, 2.0));
// Now: graph.getNeighbors(majestic) → [Connection to Vidhana Soudha]
```

**Why TreeMap for stationIndex?** A `TreeMap` keeps its keys sorted alphabetically. This is used to present the station list in A-Z order. A regular `HashMap` would give a random order.

---

## 8. Station Class

---
### Station

**What is it?** A data class representing one metro station in the network.

**Why do we need it?** We need to store information about each station — its identity, which lines serve it, and whether it's currently open.

**Important fields:**
- `id` (`String`) — a short unique code (e.g., `"PU-01"`)
- `name` (`String`) — the display name (e.g., `"Baiyappanahalli"`)
- `lines` (`Set<MetroLine>`) — which metro lines stop at this station
- `active` (`boolean`) — whether the station is currently operational (can be set to false during disruption simulation)

**Important methods:**
- `isInterchange()` — returns `true` if `lines.size() > 1` (the station is on more than one line, so you can transfer there)
- `addLine(MetroLine)` — adds a line to this station's set

**Simple example:**

Majestic station:
```
id    = "PU-15"
name  = "Majestic (Nadaprabhu Kempegowda)"
lines = {PURPLE, GREEN}  ← two lines = interchange station!
active = true
```

**What is an interchange station?** It's a station where two or more lines meet. If you're travelling from Whitefield (Purple Line) to Nagawara (Green Line), you *must* change lines somewhere. Interchange stations are where that happens. Majestic is the most important interchange in Bengaluru's metro.

---

## 9. MetroLine Enum

---
### MetroLine

**What is it?** A Java `enum` listing all five metro lines along with their properties.

**Why do we need it?** Each line has a name, a colour, a human-readable description, and a *transfer penalty* — the extra time (in minutes) it takes to get off one line and board another at an interchange station. Using an enum means we can't accidentally mistype a line name, and every line's properties travel together.

**Why an enum and not a class?** Because the lines are a *fixed, known set* that never changes at runtime. Java enums are perfect for things like days of the week, card suits, or metro lines — a closed set of named constants. If you used a plain String like `"Purple"`, you could accidentally write `"purple"` or `"Purpel"` and the compiler wouldn't catch it. With an enum, the compiler checks it for you.

**Fields (each enum constant carries these):**
- `displayName` (`String`) — e.g., `"Purple Line"`
- `description` (`String`) — a brief description
- `transferPenaltyMinutes` (`double`) — extra minutes added when you switch *to* this line

**Transfer penalties:**
| Line   | Penalty |
|--------|---------|
| Purple | 4.0 min |
| Green  | 4.0 min |
| Yellow | 3.5 min |
| Pink   | 4.0 min |
| Blue   | 3.0 min |

**Simple example:**

```java
MetroLine line = MetroLine.PURPLE;
System.out.println(line.displayName);         // "Purple Line"
System.out.println(line.transferPenaltyMinutes); // 4.0
```

---

## 10. Connection Class

---
### Connection

**What is it?** A single directed track segment between two adjacent stations — the "edge" of our graph.

**Why do we need it?** The pathfinding algorithm needs to know, for each step in the journey: which line does this segment belong to? How far is it? How long does it take? A Connection bundles all of that together.

**Important fields:**
- `from` (`Station`) — the starting station of this segment
- `to` (`Station`) — the ending station of this segment
- `line` (`MetroLine`) — which metro line this track belongs to
- `distanceKm` (`double`) — physical distance of this segment in kilometres
- `timeMinutes` (`double`) — travel time for this segment in minutes
- `operational` (`boolean`) — whether this track is currently open (can be disrupted)

**Simple example:**

```
Connection:
  from     = Majestic
  to       = Vidhana Soudha
  line     = PURPLE
  distance = 0.8 km
  time     = 2.0 minutes
  operational = true
```

Note that since the metro runs both ways, there is *also* a separate Connection from Vidhana Soudha → Majestic. That's what "bidirectional each = 2 entries in adjacency list" means. The graph is **directed** (each arrow has a direction), but the data initializer adds both directions for every physical track.

---

# Part 3: Finding the Route — Dijkstra's Algorithm

## 11. The Core Challenge

Here's the problem: given a graph with 63 stations and 130+ connections, how do you find the *best* path from station A to station B?

You could try every possible path — that's called **brute force**. But the number of possible paths grows explosively as the network grows. For a large network this would take way too long.

We need a smarter approach. That's where **Dijkstra's algorithm** comes in.

---

## 12. What is Dijkstra's Algorithm?

Dijkstra's algorithm (named after Dutch computer scientist Edsger Dijkstra, who invented it in 1956) finds the **shortest path** from a starting node to all other nodes in a weighted graph.

"Weighted" means each edge has a cost — in our case, it's either distance, time, or a combined cost.

Let's understand it with a simple city example. Imagine you're in City Centre and want to reach the Airport. The roads are:

```
City Centre → Mall     (5 min)
City Centre → Station  (8 min)
Mall        → Airport  (6 min)
Station     → Airport  (3 min)
```

There are two routes:
- City Centre → Mall → Airport = 5 + 6 = **11 min**
- City Centre → Station → Airport = 8 + 3 = **11 min**

Dijkstra would evaluate both and report they're equal. Here's the idea in plain English:

1. **Start at the origin.** Its cost is 0. All other stations start with cost = infinity.
2. **Pick the unvisited station with the lowest cost so far.** Visit it.
3. **For each neighbor of that station,** calculate: *cost to get here + cost of this edge*. If it's less than what we thought, update that neighbor's cost.
4. **Mark this station as visited** (never revisit it — once you've found the best way to it, you won't find a better one later).
5. **Repeat from step 2** until you reach the destination (or exhaust all stations).

**Pseudocode:**

```
function dijkstra(graph, start, end):
    cost[start] = 0
    cost[every other station] = INFINITY
    priorityQueue = {(cost=0, station=start)}
    visited = {}

    while priorityQueue is not empty:
        current = priorityQueue.removeSmallest()

        if current.station == end:
            return reconstructPath(current)

        if current.station in visited:
            skip

        visited.add(current.station)

        for each connection leaving current.station:
            neighbor = connection.to
            newCost = current.cost + connection.cost

            if newCost < cost[neighbor]:
                cost[neighbor] = newCost
                priorityQueue.add((cost=newCost, station=neighbor, parent=current))

    return "No path found"
```

The key insight: we always process the **cheapest unvisited node first**. This guarantees that the first time we reach the destination, we've found the optimal path.

---

## 13. What is a Priority Queue?

Dijkstra's algorithm always needs "the unvisited station with the lowest cost." How do we efficiently get that?

We use a **Priority Queue** — a special data structure that always gives you the smallest (or largest) element first, regardless of insertion order.

**Hospital analogy:** Imagine an emergency room. Patients arrive at different times, but doctors don't see them in arrival order. They see the **most critical patient first**. If a heart attack patient arrives after a sprained ankle patient, the heart attack is treated first. The "priority" determines who gets served, not the arrival time.

In Dijkstra:
- When we add a station to the queue, we give it a **priority = cost so far**
- When we remove from the queue, we always get the **station with the lowest cost** (not the one added earliest)

Java's `PriorityQueue<E>` does this automatically, as long as the elements implement `Comparable` (they know how to compare themselves).

---

## 14. PathNode Class

---
### PathNode

**What is it?** A wrapper around a station that also remembers how we got there — the accumulated cost, distance, time, transfer count, and a reference to the previous PathNode.

**Why do we need it?** The priority queue contains PathNode objects, not just Stations. When we reach the destination, we need to trace back the path: *which station did we come from? And which station before that?* The chain of `previous` references is how we reconstruct the full route.

Think of it like breadcrumbs in a forest. As you walk deeper, you drop a breadcrumb at each step that points back to where you came from. When you find the treasure (destination), you follow breadcrumbs back to the start to know the exact path you took.

**Important fields:**
- `station` (`Station`) — the station this node represents
- `currentLine` (`MetroLine`) — which metro line we arrived on (crucial for detecting transfers)
- `cost` (`double`) — the accumulated cost used by this Dijkstra variant (distance, time, or interchange score)
- `distanceKm` (`double`) — total physical distance accumulated so far
- `timeMinutes` (`double`) — total travel time accumulated so far
- `transfers` (`int`) — how many line changes have occurred so far
- `previous` (`PathNode`) — the PathNode we came from (the breadcrumb pointer)
- `connectionUsed` (`Connection`) — the actual track segment used to arrive here

**Important methods:**
- `compareTo(PathNode other)` — compares by `cost` field, so the PriorityQueue knows which PathNode has lowest cost

**Simple example:**

After running Dijkstra from Baiyappanahalli to Majestic on the Purple line:

```
PathNode (Majestic, cost=18.0):
  previous → PathNode (Trinity, cost=14.0):
               previous → PathNode (Halasuru, cost=10.0):
                            previous → ... → PathNode (Baiyappanahalli, cost=0.0, previous=null)
```

Tracing back through `.previous` gives us the full path.

---

## 15. The 3 Dijkstra Variants

MetroMind doesn't just find *a* route — it finds the route that is best by three different definitions, and then compares them.

### Variant 1: `dijkstraDistance`

**Goal:** Minimize total kilometres travelled.

**Cost of an edge:** `connection.distanceKm`

This finds the physically shortest route. Sometimes a shorter distance means more line changes, which is annoying — but if you just want the least ground covered, this is your answer.

---

### Variant 2: `dijkstraTime`

**Goal:** Minimize total travel time, including transfer penalties.

**Cost of an edge:** `connection.timeMinutes + (transferPenalty if line changes)`

This is usually the most useful route for commuters. It accounts for the fact that switching lines costs extra time (you have to walk to another platform, wait for the next train). The `transferPenaltyMinutes` from the MetroLine enum is added when you board a new line.

---

### Variant 3: `dijkstraFewestInterchanges`

**Goal:** Minimize the number of line changes, with distance as a tiebreaker.

**Cost of an edge:** `(transfers × 10000) + distanceKm`

The `× 10000` is a clever trick. By multiplying transfers by a huge number, we make one extra transfer always worse than any increase in distance. So the algorithm will *strongly* prefer staying on one line, even if it means travelling farther. Distance only matters as a tiebreaker when transfer counts are equal.

This is the route for someone who hates the hassle of changing trains.

---

## 16. State Expansion: Why We Track (Station, Line)

Here's a subtle but important detail. For the basic distance variant, we track **visited stations** — once you've visited Majestic, you don't visit it again. This works because the first time Dijkstra reaches a station via the cheapest route, that route is optimal.

But for the **time** and **interchange** variants, this rule breaks!

**Example:** Suppose we reach Majestic on the Purple line at time=15. We mark it visited. Later, we might want to reach Majestic on the Green line at time=17 — to continue towards a Green-line destination. But we've already marked Majestic visited, so we skip it!

The fix: instead of tracking visited **stations**, we track visited **(station, line) pairs**. Visiting Majestic on Purple is a different state from visiting Majestic on Green. Both can be valid intermediate steps in different journeys.

This is called **state expansion** — expanding what counts as "a state" in the search to allow more flexibility.

**Pseudocode for time variant:**

```
visited = Set of (station, line) pairs
state = (station, currentLine)

if state in visited: skip
visited.add(state)

for each connection leaving station:
    newTime = currentTime + connection.timeMinutes
    if connection.line != currentLine:
        newTime += connection.line.transferPenaltyMinutes
    ...
```

---

## 17. Best-Route Selection: The Friction Score

After running all three variants, MetroMind has up to 3 candidate routes. It picks the best one using a **friction score** — a single number that combines time, interchanges, and distance.

**Formula:**

```
friction = time + (interchanges × 7) + (distance × 0.15)
```

The route with the **lowest friction score wins**.

Let's understand each term:

- **`time`** — Travel time in minutes. The most important factor. Direct contribution.
- **`interchanges × 7`** — Each interchange adds 7 "penalty minutes" to the score. This reflects the real-world annoyance of changing trains: physical effort, waiting, uncertainty. Even if the actual transfer penalty is only 3–4 minutes, the *experience* is worse.
- **`distance × 0.15`** — Each extra kilometre adds 0.15 to the score. This is a small factor — just enough to break ties between routes with the same time and interchange count, favouring shorter ones.

**Pseudocode:**

```
function bestRoute(routeA, routeB, routeC):
    for each route in [routeA, routeB, routeC]:
        if route.found:
            route.friction = route.time
                           + (route.interchanges × 7)
                           + (route.distanceKm × 0.15)

    return route with minimum friction score
```

This is a **heuristic** (a scoring rule based on experience/judgment, not a mathematical proof). Different values of the multipliers (7 and 0.15) would give different results. These were tuned to reflect realistic commuter preferences.

---

## 18. Route Reconstruction: Tracing Back Through Parents

Once Dijkstra reaches the destination, it doesn't immediately know the full path. It just knows:
- The total cost
- The final PathNode (at the destination)

To build the full list of stations and connections, we **trace backwards** through the `.previous` chain:

```
destination.previous.previous.previous... until previous == null (the origin)
```

Then we **reverse** the resulting list to get it in origin → destination order.

**Pseudocode:**

```
function reconstructPath(finalNode):
    path = []
    connections = []
    current = finalNode

    while current != null:
        path.add(current.station)
        if current.connectionUsed != null:
            connections.add(current.connectionUsed)
        current = current.previous

    reverse(path)
    reverse(connections)
    return (path, connections)
```

The result is stored in a `RouteResult` object.

---

### RouteResult

**What is it?** A data class that holds everything about a computed route — the full station list, the connections used, the route segments, and the totals.

**Why do we need it?** After Dijkstra runs, we need to pass the result around — to the fare engine, to the visualizer, to the summary. RouteResult is the carrier object.

**Important fields:**
- `origin` (`Station`) — where the journey starts
- `destination` (`Station`) — where the journey ends
- `path` (`List<Station>`) — all stations in order, from origin to destination
- `connections` (`List<Connection>`) — all track segments used
- `segments` (`List<RouteSegment>`) — the journey broken into continuous legs on each line
- `interchanges` (`int`) — number of line changes
- `totalDistanceKm` (`double`) — total distance
- `totalTimeMinutes` (`double`) — total time (including transfer penalties)
- `found` (`boolean`) — whether a route was successfully found (false if no path exists)

---

### RouteSegment

**What is it?** One continuous leg of the journey on a single metro line.

**Why do we need it?** A journey might go Purple from A to E, then switch to Green for F to K. That's two segments. Segments make it easy to explain the journey in human-readable chunks: "Take Purple line from Baiyappanahalli to Majestic, then switch to Green line towards Nagawara."

**Important fields:**
- `startStation` (`Station`) — where this leg begins
- `endStation` (`Station`) — where this leg ends
- `stations` (`List<Station>`) — all intermediate stations on this leg
- `line` (`MetroLine`) — which line this leg is on
- `distanceKm` (`double`) — distance of this leg
- `timeMinutes` (`double`) — time for this leg

---

# Part 4: Calculating the Fare

## 19. The Fare Structure

Namma Metro uses a **slab-based pricing** system. The network is divided into distance ranges, and each range has a fixed fare. This is similar to how many city metros work worldwide.

Here are the 6 slabs:

| Distance         | Base Fare |
|------------------|-----------|
| 0 – 2 km         | ₹10       |
| 2 – 5 km         | ₹20       |
| 5 – 12 km        | ₹30       |
| 12 – 21 km       | ₹40       |
| 21 – 32 km       | ₹50       |
| More than 32 km  | ₹60       |

Notice these slabs use total journey distance, not per-kilometre pricing. So whether you travel 5.1 km or 11.9 km, you pay ₹30.

---

## 20. FareEngine.calculateFare() — Step by Step

---
### FareEngine

**What is it?** The class responsible for computing fares from a RouteResult and a chosen ticket type.

**Why do we need it?** Fare calculation involves several steps (slab lookup, discount application, surcharge), and mixing this logic into the routing code would make both harder to understand and change. Separating it follows the Single Responsibility Principle.

**Important methods:**
- `calculateFare(RouteResult, TicketType)` — the main method; returns a `FareBreakdown`
- `determineBaseFare(double distanceKm)` — looks up the slab and returns the base fare
- `hasAirportSurcharge(RouteResult)` — checks if the route passes through the airport station
- `calculateAllFares(RouteResult)` — runs calculateFare for all 5 ticket types at once

**Step-by-step calculation:**

1. **Get total distance** from RouteResult.totalDistanceKm
2. **Determine base fare** by looking up the slab table
3. **Check for airport surcharge**: if any station in the path has a name matching "KIAL" or "Airport", add ₹20
4. **Look up discount** from the ticket type
5. **Apply discount**: `discountedFare = baseFare - discountAmount`
6. **Add surcharge**: `fareBeforeMin = discountedFare + airportSurcharge`
7. **Apply minimum floor**: `finalFare = max(₹10, fareBeforeMin)`

The floor of ₹10 ensures no ticket ever costs less than the minimum slab.

**Formula summary:**

```
finalFare = max(₹10, (baseFare - discount) + airportSurcharge)
```

---

## 21. The 5 Ticket Types and Their Discounts

---
### TicketType

**What is it?** An enum listing the five categories of metro tickets and concession passes, each with a discount rate.

**Important fields:**
- `title` (`String`) — human-readable category name
- `discountPercentage` (`double`) — percentage discount applied to base fare
- `description` (`String`) — passenger eligibility explanation

| Ticket Type | Discount | Eligibility |
|---|---|---|
| Standard Token / QR Ticket | 0% | Full base fare single journey token |
| Namma Metro Smart Card (Varshik / NCMC) | 10% | Contactless rechargeable card with 10% concession |
| Namma Metro Smart Card (Off-Peak / Weekend) | 20% | Off-peak and weekend concession |
| Student Concession Pass | 25% | Subsidized student travel pass |
| Senior Citizen Pass | 30% | Senior citizen welfare concession |

**Worked Numerical Example:**

Consider a commute from **Whitefield (Kadugodi)** to **Lalbagh**:
- Route distance: **27.00 km**
- Distance slab: `21.1 - 32.0 km` $\implies$ Base Fare = **₹50.00**
- Selected ticket type: **Namma Metro Smart Card** (10% discount)
- Discount amount: $50.00 \times 10\% = -\text{₹}5.00$
- Airport surcharge: **₹0.00** (does not traverse the airport corridor)
- Final payable amount: $\max(10.00, (50.00 - 5.00) + 0.00) =$ **₹45.00**

---

## 22. Airport Surcharge — When and How Applied

The airport terminal station (*KIAL Airport Terminal*) is special. It is located on the **Blue Line (Airport & ORR)**, and traveling to or from it incurs an extra **₹20.00 infrastructure surcharge** on top of the discounted base fare.

**How detection works:** `RouteResult.usesAirportExpress()` inspects all stations in the computed path and edge connections for names matching `"airport"` or `"kial"`. If matched, `FareEngine` applies `AIRPORT_SURCHARGE = 20.0`.

**Why a surcharge?** High-speed airport express corridors require dedicated civil infrastructure, luggage handling, and premium operating costs, mirroring international airport rail links.

**When is it applied?** Only when the computed route actually traverses the KIAL Airport station (e.g. Hebbal to KIAL Airport: Base ₹50.00 + Surcharge ₹20.00 = ₹70.00). Normal urban commutes incur ₹0.00 surcharge.

---

## 23. FareBreakdown

---
### FareBreakdown

**What is it?** A snapshot of exactly how a fare was calculated — every step documented.

**Why do we need it?** When we show the fare to the user, we don't just show the final number. We show the working: "Here's what distance you travelled, here's the slab, here's the base fare, here's your discount, here's the surcharge, here's your total." This transparency helps users understand and trust the result.

**Important fields:**
- `distanceKm` (`double`) — total journey distance
- `slabName` (`String`) — which slab applies (e.g., "12–21 km")
- `baseFare` (`double`) — fare before discounts
- `airportSurcharge` (`double`) — 0 or 20
- `ticketType` (`TicketType`) — which ticket type was used
- `discountAmount` (`double`) — the rupee value of the discount
- `finalFare` (`double`) — the amount the passenger pays

---

# Part 5: The Service Layer

## 24. MetroMindService

---
### MetroMindService

**What is it?** A **facade** (a simplified front door) that orchestrates MetroGraph, RouteEngine, and FareEngine together so that the rest of the app (and Main.java) doesn't need to know the internal details.

**Why do we need it?** Think of a restaurant analogy. When you order a meal, you tell the waiter what you want. You don't walk into the kitchen yourself, grab ingredients, start the stove, and cook. The waiter (service layer) coordinates the kitchen staff (graph + route engine + fare engine) on your behalf.

Main.java calls service methods like `planBestJourney()` and gets back a complete `JourneySummary`. It doesn't know or care how routes are computed or fares calculated.

**Important methods:**
- `planBestJourney(String origin, String destination, TicketType)` — the main orchestration method: finds the route, calculates fares for all ticket types, creates a JourneySummary
- `getJourneyHistory()` — returns the list of past journeys
- `getGraph()` — provides access to the network graph (for station listing, etc.)
- `getRouteEngine()` — provides access to the route engine

**Simple example:**

```java
MetroMindService service = new MetroMindService(graph, routeEngine, fareEngine);
JourneySummary summary = service.planBestJourney("Majestic", "Whitefield", TicketType.STUDENT);
// summary now contains the best route AND all fare options AND a unique journey ID
```

---

## 25. JourneySummary

---
### JourneySummary

**What is it?** A complete record of one planned journey — bundling together the route, all fare options, the selected ticket, and a unique ID.

**Why do we need it?** We want to store the journey in history and be able to display it again later. JourneySummary is the single object that holds everything.

**Important fields:**
- `journeyId` (`String`) — a unique identifier in the format `MM-NNNN` (e.g., `MM-0042`)
- `timestamp` (`LocalDateTime`) — when the journey was planned
- `routeResult` (`RouteResult`) — the route that was computed
- `fareOptions` (`Map<TicketType, FareBreakdown>`) — fares for all 5 ticket types
- `selectedTicketType` (`TicketType`) — the type the user chose

**Simple example:**

```
JourneySummary:
  journeyId   = "MM-0003"
  timestamp   = 2024-01-15 14:32
  route       = Baiyappanahalli → ... → Majestic (Purple, 18 min, 9.5 km)
  fareOptions = {Regular: ₹30, Student: ₹24, ...}
  selected    = STUDENT
```

---

## 26. Journey History: How 25 Journeys Are Stored

MetroMindService keeps a list of past JourneySummary objects. The maximum size is 25.

**Why 25?** It's a practical limit — enough to be useful for reviewing recent journeys, without consuming too much memory for an in-memory app.

**How it works:** When a new journey is planned, it's added to the beginning (or end) of the history list. If the list already has 25 entries, the oldest one is removed to make room.

This is the **sliding window** pattern — always keep the most recent N items.

---

# Part 6: The User Interface

## 27. How Main.java Loops and Handles User Input

Main.java is the entry point — the class with the `public static void main(String[] args)` method.

**Program flow:**

```
main()
  |
  ├── printWelcomeBanner()   // show the app logo/title
  |
  └── loop forever:
        printMainMenu()      // show the 7 options
        choice = readInt()   // get user's selection (1–7)
        
        switch(choice):
          case 1 → handleFindBestRoute()       // Plan best route, visual schematic & fare
          case 2 → handleExploreNetwork()      // Browse lines, stations, junctions & search
          case 3 → handleViewFareStructure()   // Official BMRCL slabs & concessions
          case 4 → handleNetworkOperations()   // Disruption simulation, station/track toggles
          case 5 → handleRecentJourneys()      // Audit log of session journeys
          case 6 → handleExportData()          // Export receipts, route CSV, network CSV
          case 7 → printExitMessage()          // Graceful exit & farewell banner
```

This is a classic **menu-driven program** pattern. The program is always in one of two states: showing the menu, or handling a user's choice.

**Why a loop?** So the user can plan multiple journeys without restarting the app. After each action is complete, the loop brings them back to the main menu.

---

## 28. How Station Search Works (Partial Match)

When a user types "Majest" instead of "Majestic", the app still finds the right station. How?

MetroGraph's `findStation()` method does a **partial-match search**:

1. The user's input is converted to lowercase
2. The `stationIndex` TreeMap's keys (lowercase station names) are scanned
3. Any key that **contains** the user's input as a substring is considered a match

```java
// Pseudo-code for partial match:
for (String stationName : stationIndex.keySet()) {
    if (stationName.contains(userInput.toLowerCase())) {
        matches.add(stationIndex.get(stationName));
    }
}
```

If exactly one station matches, it's selected automatically. If multiple stations match, the user is shown the list and asked to pick one. If nothing matches, the user is prompted to try again.

**Why this matters:** Real users make typos, use partial names ("ITPL" for the ITPL station), and don't know exact station names. Partial matching makes the app forgiving and user-friendly.

---

## 29. AsciiRouteVisualizer

---
### AsciiRouteVisualizer

**What is it?** A utility class that draws a text-based visual map of the computed route in the terminal.

**Why do we need it?** A list of station names is functional but hard to read as a route. A visual schematic — with stations in boxes, tracks connecting them, and line names labeled — is much more intuitive. Since this is a terminal app (no graphical window), we use ASCII/Unicode characters to simulate a diagram.

**How it's built:**

1. Take the `RouteResult.segments` (each is one continuous leg on a line)
2. For each segment, draw a "track" using box-drawing characters (`─`, `│`, `╔`, etc.) and line-colour badges
3. At interchange stations, show the transfer between lines
4. Display times and distances for each segment

**Simple example output (conceptual):**

```
 PURPLE LINE
 [Baiyappanahalli] ─── [Halasuru] ─── [Trinity] ─── [Majestic]
                                                          │
                                                     TRANSFER TO
                                                      GREEN LINE
                                                          │
                                        [Majestic] ─── [Sandal Soap Factory] ─── [Nagawara]
```

**Important methods:**
- `visualizeRoute(RouteResult)` — the main method that builds and prints the full schematic

---

## 30. ConsoleUtils

---
### ConsoleUtils

**What is it?** A collection of static helper methods for reading user input safely and printing formatted output.

**Why do we need it?** Reading from a Scanner without error handling is fragile. If a user types "abc" when you expect a number, the program crashes. ConsoleUtils wraps Scanner interactions in validation loops so bad input is caught gracefully.

**Important methods:**
- `readInt(Scanner, String prompt, int min, int max)` — repeatedly asks for input until the user enters a valid integer in [min, max]
- `readDouble(Scanner, String prompt, double min, double max)` — same for decimals
- `readNonEmptyString(Scanner, String prompt)` — keeps asking until the user types something non-empty
- `pressEnterToContinue(Scanner)` — pauses until the user hits Enter
- `printHeader(String title)` — prints a styled section header
- `printSuccess(String msg)` — prints a success message (green `[+]` prefix)
- `printError(String msg)` — prints an error message (red `[x]` prefix)
- `printWarning(String msg)` — prints a warning (yellow `[!]` prefix)
- `printInfo(String msg)` — prints an informational note (cyan `[i]` prefix)

**Simple example:**

```java
int choice = ConsoleUtils.readInt(scanner, "Enter choice (1-7): ", 1, 7);
// If user types "9" → "Invalid. Please enter between 1 and 7."
// If user types "abc" → "Invalid input. Please enter a number."
// Keeps asking until valid.
```

**ANSI Colour Support:** ConsoleUtils supports ANSI escape codes for coloured terminal output. Modern terminals (Windows Terminal, VS Code terminal, IntelliJ) support these. If colours cause issues, there's a fallback to plain text.

---

# Part 7: Data Export

## 31. FileExporter

---
### FileExporter

**What is it?** A utility class that writes journey data to files on disk — in both human-readable (`.txt`) and machine-readable (`.csv`) formats.

**Why do we need it?** Sometimes you want to keep a record of your planned journey — maybe to share it, or to open it in Excel. FileExporter handles that without polluting other classes with file I/O code.

**What it generates:**

1. **Journey Receipt (.txt)** — A formatted text receipt for a specific JourneySummary, showing all journey details and fare breakdown. Readable by anyone.

2. **Route Data (.csv)** — A CSV (Comma-Separated Values) file for a specific route. Each row is one station in the path, with time and distance columns. Can be opened in Excel or Google Sheets.

3. **Network Export (.csv)** — A full export of all stations and connections in the network. Useful for analysis or visualization in other tools.

**How it writes files:**

Java's `FileWriter` and `BufferedWriter` are used. The class:
1. Opens/creates the file
2. Writes headers (for CSV)
3. Iterates through the data and writes each row
4. Closes the file

**Simple example output (receipt.txt):**

```
MetroMind Journey Receipt
Journey ID: MM-0007
Date: 2024-01-15 14:32

Route: Baiyappanahalli → Majestic
Line: Purple
Distance: 9.5 km | Time: 18 min | Interchanges: 0

Ticket Type: Student
Base Fare: ₹30 | Discount: ₹6 (20%) | Final Fare: ₹24
```

---

# Part 8: The Test Suite

## 32. MetroMindTest

The project includes a **test file** (`MetroMindTest.java`) that verifies the core functionality works correctly.

Writing tests is a professional practice called **Test-Driven Development (TDD)** or just unit testing. The idea: rather than manually running the app every time you change something, automated tests do it for you in seconds.

**General categories of tests in a project like this:**

- **Graph construction tests** — Does adding a station actually add it? Does adding a connection appear in the adjacency list?
- **Route-finding tests** — Does a known journey (e.g., Baiyappanahalli to Majestic on Purple) find the correct route with the right distance and time?
- **Fare calculation tests** — Given a known distance, does the fare engine return the correct slab? Do discounts apply correctly? Does the airport surcharge trigger only for airport routes?
- **Service layer tests** — Does `planBestJourney()` return a valid JourneySummary?
- **Edge case tests** — What happens if origin == destination? What if a station doesn't exist? What if a path is blocked?

Tests give you confidence that changes in one part of the code haven't accidentally broken something else. This is called catching **regressions**.

---

# Part 9: Running the Project

## 33. Prerequisites

Before you can run MetroMind, you need:

1. **Java Development Kit (JDK) 17 or later**
   - Check with: `java -version` in a terminal
   - Download from: https://adoptium.net or https://oracle.com/java

2. **A terminal / command prompt**
   - Windows: PowerShell or Command Prompt
   - Mac/Linux: Terminal

3. **The source code** in the `java2` project folder

No external libraries, build tools, or IDE required (though an IDE like IntelliJ or VS Code makes life easier).

---

## 34. How to Compile and Run

The project uses a standard `src/` folder structure. Here's how to compile and run from the command line:

**Step 1: Navigate to the project folder**

```bash
cd c:\Users\shiv2\OneDrive\Desktop\PPT\CP\java2
```

**Step 2: Compile the Java sources**

You can use the provided Windows batch script:
```cmd
compile.bat
```

Or run `javac` directly:
```bash
javac -d bin -sourcepath src src/Main.java src/MetroMindTest.java src/model/*.java src/graph/*.java src/route/*.java src/fare/*.java src/service/*.java src/util/*.java
```

This tells `javac` (the Java compiler):
- `-d bin` → put the compiled `.class` files in the `bin/` folder
- `-sourcepath src` → the root for Java packages is `src/`

**Step 3: Run the application**

Run via batch script:
```cmd
run.bat
```

Or manually:
```bash
java -cp bin Main
```

This tells `java` (the JVM):
- `-cp bin` → look for compiled `.class` files in the `bin/` folder
- `Main` → start executing from the `Main` class

To run the automated verification test suite:
```cmd
test.bat
```
or
```bash
java -cp bin MetroMindTest
```

**You should see the MetroMind welcome banner and the main menu.**

---

## 35. Common Errors and Fixes

| Error Message | Likely Cause | Fix |
|---|---|---|
| `'javac' is not recognized` | JDK not installed or not in PATH | Install JDK 17+ and add to PATH |
| `error: package does not exist` | Wrong sourcepath or missing file | Make sure you're in the project root and all files are present |
| `ClassNotFoundException: Main` | Compiled to wrong folder | Re-run `compile.bat` or `javac -d bin ...`, then `java -cp bin Main` |
| `NoSuchElementException` on Scanner | Running with piped input (not interactive) | Run normally with keyboard input |
| `NullPointerException` on route | Station not found | Check spelling; use partial names |
| Garbled characters in terminal | ANSI codes not supported | Try a modern terminal like Windows Terminal |

---

# Part 10: What We Learned

## 36. OOP Concepts Used

MetroMind is a showcase of core **Object-Oriented Programming** principles:

**Encapsulation:**
Each class hides its internals. `MetroGraph` manages its `adjacencyList` privately — other classes can't directly mess with it. They must use `addStation()`, `getNeighbors()`, etc. This prevents accidental corruption of the data.

**Enums:**
`MetroLine` and `TicketType` are enums — a Java feature for a fixed set of named constants. Enums are better than plain Strings because:
- Compiler enforces valid values
- Each constant can carry extra data (like `transferPenaltyMinutes`)
- Switch statements on enums get exhaustiveness warnings

**Generics:**
Collections like `HashMap<Station, List<Connection>>` and `PriorityQueue<PathNode>` use Java generics. The `<Type>` part tells the compiler what kind of objects the collection holds, so you get type safety without casting.

**Interfaces and Comparable:**
`PathNode` implements `Comparable<PathNode>` so that `PriorityQueue` knows how to order PathNodes by cost. This is an example of **programming to an interface** — the PriorityQueue doesn't care what a PathNode is; it just needs to know it can be compared.

**Single Responsibility Principle:**
Each class has one job. MetroGraph stores the network. RouteEngine finds routes. FareEngine calculates fares. MetroMindService orchestrates. Main handles UI. No class does another's job.

**Facade Pattern:**
MetroMindService is a facade — it simplifies a complex subsystem (graph + multiple engines) behind a simple interface. Main.java calls `service.planBestJourney()` and doesn't need to know anything about Dijkstra or fare slabs.

---

## 37. Data Structures Used

| Data Structure | Where Used | Why |
|---|---|---|
| `HashMap<K,V>` | adjacencyList in MetroGraph | O(1) lookup of neighbors by station |
| `TreeMap<K,V>` | stationIndex in MetroGraph | Alphabetically sorted station names |
| `PriorityQueue<E>` | Dijkstra's min-heap | Always get lowest-cost node first |
| `ArrayList<E>` | path, connections, segments in RouteResult | Ordered list of route elements |
| `Set<MetroLine>` | Station.lines | Unordered, no-duplicate set of lines |
| `Map<TicketType, FareBreakdown>` | JourneySummary.fareOptions | Map all 5 fare options at once |
| `List<JourneySummary>` | Journey history | Last 25 journeys |

**HashMap vs TreeMap — when to use which:**

- Use **HashMap** when you want the fastest possible lookup (O(1)) and don't care about order
- Use **TreeMap** when you need keys sorted (O(log n) operations, but alphabetical iteration)

MetroGraph uses both: HashMap for neighbor lookups during Dijkstra (needs speed), TreeMap for station index (needs alphabetical listing in the UI).

---

## 38. Graph Theory Fundamentals

This project is an application of **graph theory** — a branch of mathematics and computer science dealing with networks of connected nodes.

**Key terms encountered:**

| Term | Meaning | In MetroMind |
|---|---|---|
| **Graph** | Nodes + edges | Metro network |
| **Node / Vertex** | A point in the graph | A station |
| **Edge** | A connection between nodes | A track segment |
| **Directed graph** | Edges have direction | Connections are one-way (but added both ways) |
| **Weighted graph** | Edges have costs | Connections have distanceKm and timeMinutes |
| **Path** | Sequence of nodes from A to B | A journey route |
| **Shortest path** | The path with minimum total cost | What Dijkstra finds |
| **Cycle** | A path that returns to its starting node | Possible in metro networks |

**Why directed?** Even though metro trains run both ways, we model it as a directed graph. Each physical track becomes two directed edges. This allows modeling one-way sections (if they ever exist) and keeps the model consistent.

**Connected graph:** A graph where every node can reach every other node. MetroMind assumes the network is connected — that's true for a real metro where every station is reachable from any other station.

---

## 39. Algorithm Design: Dijkstra and Heuristic Scoring

**Dijkstra's Algorithm — Key Properties:**

| Property | Value |
|---|---|
| **What it solves** | Single-source shortest path |
| **Works on** | Graphs with non-negative edge weights |
| **Time complexity** | O((V + E) log V) with a binary heap / priority queue |
| **Space complexity** | O(V) for distance and path arrays |
| **Guarantee** | Always finds the optimal path (if edge weights are non-negative) |

Where V = number of vertices (stations = 63), E = number of edges (connections ≈ 130).

For MetroMind's size, Dijkstra completes in microseconds.

**Why not A* (A-star)?** A* is a faster alternative to Dijkstra that uses a *heuristic* (an estimate of remaining distance) to focus the search. It's commonly used in GPS navigation. For 63 stations, Dijkstra is already fast enough that A* provides no meaningful improvement — and implementing geographic coordinates for the heuristic would add complexity without benefit.

**Heuristic Scoring (Friction Score):**

The friction score is a **multi-objective decision function**. When you have multiple valid routes and need to pick "the best one overall," you can't just optimize for one criterion (pure time might mean lots of transfers; fewest transfers might mean much longer journey). The friction score is a weighted combination:

```
friction = time + (interchanges × 7) + (distance × 0.15)
```

This is a classic technique in **operations research** called a **weighted sum method** — convert a multi-objective problem into a single objective by assigning weights to each criterion.

The weights (7 and 0.15) are **tunable parameters**. Different weight values would give different "personalities" to the route selector:
- Higher interchange weight → strongly avoids transfers even at time cost
- Higher distance weight → more strongly prefers shorter physical routes
- Current values reflect a commuter-centric balance: time matters most, then comfort (fewer changes), then distance

---

# Appendix: Class Reference Quick-Guide

| Class | Package | Purpose |
|---|---|---|
| `Station` | `model` | A metro station node |
| `Connection` | `model` | A track segment edge |
| `MetroLine` | `model` | Enum of the 5 lines |
| `TicketType` | `model` | Enum of the 5 ticket types |
| `RouteResult` | `model` | A computed route |
| `RouteSegment` | `model` | One continuous leg on one line |
| `FareBreakdown` | `model` | Fare calculation details |
| `JourneySummary` | `model` | Complete journey record |
| `MetroGraph` | `graph` | The adjacency-list graph |
| `RouteEngine` | `route` | Dijkstra variants + best-route selection |
| `PathNode` | `route` | Priority queue entry with parent tracking |
| `FareEngine` | `fare` | Slab-based fare calculation |
| `MetroMindService` | `service` | Facade orchestrating all engines |
| `ConsoleUtils` | `util` | Input validation + formatted output |
| `AsciiRouteVisualizer` | `util` | Terminal route diagram |
| `FileExporter` | `util` | CSV/TXT file export |
| `NetworkDataInitializer` | `util` | Hardcodes all 63 stations + connections |
| `Main` | (default) | Entry point, menu loop |

---

# Closing Thoughts

MetroMind is a **full-stack console application** that touches nearly every core concept of introductory computer science and Java programming:

- **Data modeling** (how to represent real-world things as classes)
- **Graph theory** (networks, nodes, edges)
- **Algorithm design** (Dijkstra's shortest path)
- **Data structures** (HashMap, PriorityQueue, TreeMap, ArrayList, Set)
- **OOP principles** (encapsulation, enums, generics, Comparable)
- **Architecture patterns** (facade, service layer, separation of concerns)
- **User interface design** (input validation, partial matching, ASCII visualization)
- **File I/O** (CSV and TXT export)
- **Testing** (automated test suite)

The fact that it's built with **no external libraries** means every piece of functionality — from the graph traversal to the terminal colours — was written from scratch. That's not the most efficient approach for production software, but it's the best way to *learn*.

If you've read and understood this document, you now have a solid grounding in graph algorithms, data structures, and software design that goes well beyond what most introductory programming courses cover. Well done!

---

*MetroMind — Bengaluru Namma Metro Routing & Fare Computation Engine*
*Built with ♥ in pure Java*
