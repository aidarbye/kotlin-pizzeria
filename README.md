# Kotlin Pizzeria

A small Kotlin/JVM console application that simulates a pizzeria: a menu with pizzas, drinks and desserts, customer orders, and asynchronous order processing (cooking and delivery) with coroutines.

## How to run

Requirements: JDK 21 and IntelliJ IDEA (Kotlin/JVM project, Gradle).

1. Open the project folder in IntelliJ IDEA and wait for the Gradle sync to finish (it downloads `kotlinx-coroutines-core`).
2. Open `src/main/kotlin/com/example/Main.kt`.
3. Click the green run icon next to `fun main()`.

The program prints the menu, processes three orders in parallel (status updates of different orders interleave in the log) and finishes with statistics.

## Project structure

```
src/main/kotlin/com/example/
 ├─ Main.kt                  entry point, creates the menu and orders, launches coroutines
 ├─ contract/Preparable.kt   interface
 ├─ model/
 │   ├─ Models.kt            MenuItem, Pizza, Drink, Dessert
 │   ├─ Order.kt             Customer, OrderLine, Order
 │   └─ OrderStatus.kt       sealed class + extension function toText()
 └─ service/Pizzeria.kt      business logic, collections, higher-order functions, suspend function
```

## Where the requirements are demonstrated

| Requirement | Where |
|---|---|
| Variables, data types, conditions, loops | everywhere: `if` and `for` in `Pizzeria.processOrder`, `for` in `Pizzeria.printMenu`, `when` in `OrderStatus.toText()` |
| List, Set, Map | `List` for the menu and order lines; `Set` for pizza toppings and `Pizzeria.allToppings()`; `Map` in `Pizzeria.ordersByStatus()` and `Pizzeria.popularity()` |
| map, filter, reduce | `Pizzeria.vegetarianPizzas()` (filter), `Pizzeria.popularity()` (map/flatMap/groupBy), `Pizzeria.totalRevenue()` (map + reduce), `Order.total()` (sumOf) |
| Functions, higher-order functions, lambdas | `Pizzeria.filterMenu(predicate)` and `Pizzeria.totalWithDiscount(order, rule)` take functions as parameters; called with lambdas in `Main.kt` |
| Classes and objects | `Pizzeria`, `Order`, `MenuItem` and its subclasses |
| Inheritance | `abstract class MenuItem` is extended by `Pizza`, `Drink`, `Dessert` (`Models.kt`) |
| Interfaces and polymorphism | `Preparable` is implemented by `Pizza` and `Dessert`; `describe()` is overridden in every subclass and called through `List<MenuItem>` |
| Data class | `Customer`, `OrderLine` (and the data subclasses of `OrderStatus`) |
| Sealed class | `OrderStatus` (`Received`, `Cooking`, `Delivering`, `Delivered`, `Cancelled`), handled with an exhaustive `when` |
| Suspend function and coroutine | `Pizzeria.processOrder` is a `suspend` function using `delay`; `Main.kt` starts one coroutine per order with `launch` inside `runBlocking` and waits with `joinAll` |
