# 🚹 Java Garbage Collection Algorithms – Serial, Parallel, G1 GC

![Java](https://img.shields.io/badge/Java-11%2B-blue.svg)  
![License](https://img.shields.io/badge/license-MIT-green.svg)  
![Status](https://img.shields.io/badge/status-educational-orange.svg)

## 📖 Description

This project provides an **educational implementation** of the main **Garbage Collection (GC)** algorithms used in the **Java Virtual Machine (JVM)**:

-   ✅ **Serial GC**: simple, sequential, ideal for small environments.
    
-   ✅ **Parallel GC**: multi-threaded, optimized for high throughput.
    
    

The project includes **pure Java** implementations simulating the internal behavior of each garbage collector, aimed at **learning, demonstration, and educational purposes**.

----------

## 🎯 Objectives

-   Demonstrate how the key GC algorithms work.
    
-   Illustrate concepts like **Mark**, **Sweep**, **Compact**, and **Region-based Collection**.
    
-   Help understand the **impact** of each strategy on memory management.
    
-   Serve as a foundation for those wanting to **understand or teach** about **automatic memory management**.
    

----------

## 🏗️ Project Structure

```
src/
 ├── SerialGarbageCollectir.java
 ├── ParallelGarbageCollector.java
 ├── GCObject.java
 └── Main.java

```

-   `GCObject.java`: basic object model participating in garbage collection.
    
-   `SerialGarbageCollectir.java`: sequential Mark-and-Sweep implementation.
    
-   `ParallelGarbageCollector.java`: parallel garbage collection using multiple threads.
    
    
-   `Main.java`: example usage and demonstration of each algorithm.
    

----------

## 🛠️ Technologies Used

-   ☕ **Java 11+**
    
-   ✅ **Object-Oriented Programming** paradigm
    
-   ✅ **Multithreading** (in Parallel GC)
    
    

----------

## 🚀 How to Run

1.  Clone the repository:
    
    ```bash
    git clone https://github.com/lucaszbastos/java-garbage-collector.git
    
    ```
    
2.  Compile the project:
    
    ```bash
    javac java-garbage-collector/**/*.java
    
    ```
    
3.  Run the application:
    
    ```bash
    java -cp java-garbage-collector Main <objectsCount> <mode> <youngSize> <oldSize> <rootObjectsCount> <promotionThreshold> <numThreads>
    
    ```
    The arguments must be passed in this order:

	1.  **`objectsCount`** → number of objects to allocate in the heap (simulation).
    
	2.  **`mode`** → GC algorithm:
    
	    -   `1` = Serial GC
        
	    -   `2` = Parallel GC
        
	3.  **`youngSize`** → size of the young generation heap (array length, must be >0).
    
	4.  **`oldSize`** → size of the old generation heap (array length, must be >0).
    
	5.  **`rootObjectsCount`** → number of root objects created (must be >0 and ≤ `oldSize`).
    
	6.  **`promotionThreshold`** → number of GC cycles an object must survive before being promoted to old generation(must be >0).
    
	7.  **`numThreads`** _(only if `mode=2`)_ → number of worker threads used in Parallel GC.

	Example:
	```bash
	java -cp java-garbage-collector Main 50 2 10 20 1 2 4 
	   ```

	-   50 objects to allocate
    
	-   Mode `2` → Parallel GC
    
	-   Young heap size = 10
    
	-   Old heap size = 20
    
	-   1 root object
    
	-   Promotion threshold = 2
    
	-   4 worker threads

----------

## 🔍 What You'll Learn

-   How the JVM manages memory.
    
-   Differences between the main GC algorithms.
    
-   Fundamental concepts: **Mark-Sweep-Compact**, **STW (Stop-The-World)**, **Heap Segmentation**.
    
-   How parallelism improves garbage collection performance.
    
    

----------

## ✅ Sample Output

	Added Ref Obj1 as reference to Root
	Promoting Young Objects
	Print Heap After allocation:
	Young Heap
	[Root, age: 2][Obj1, age: 1][ ][ ][ ]
	Old Heap:
	[ ][ ][ ]

	Allocating in Young Heap: Obj2
	Print Heap before allocation:
	Young Heap
	[Root, age: 2][Obj1, age: 1][ ][ ][ ]
	Old Heap:
	[ ][ ][ ]

	Added Ref Obj2 as reference to Obj1
	Promoting Young Objects
	Print Heap After allocation:
	Young Heap
	[Root, age: 3][Obj1, age: 2][Obj2, age: 1][ ][ ]
	Old Heap:
	[ ][ ][ ]
----------

## 🎲 Extra Simulation Methods

To better simulate how a JVM behaves, the project uses some helper methods:

-   **`randomReference(GCObject newObject)`** → randomly attaches a new object as a reference to an existing one in the heap.  
    This simulates how objects reference each other in real programs.
    
-   **`randomDeletion(GCObject[] heap)`** → randomly deletes an object from the heap, simulating the "end of life" of an object that is no longer used.  
    This forces the GC cycle to later reclaim memory for unreachable objects.
    

These methods make the simulation more dynamic and help demonstrate GC behavior in different scenarios.

----------

## ⚠️ Disclaimer

This project is **not a real garbage collector implementation**.  
It is an **educational adaptation** of the concepts used in JVM garbage collection, meant only for **illustration and learning purposes**.

Real JVM GCs like Serial, Parallel, and G1 are **far more complex and optimized** than this simulation.

----------

## 📢 Contact

-   Author: **Lucas Bastos**
    
-   Email: **[[bastoslucas1501@gmail.com](mailto:your@email.com)]**

----------
