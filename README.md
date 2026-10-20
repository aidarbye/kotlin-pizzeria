# Pizzeria Roma Android (HW2)

An Android app built with Kotlin, Jetpack Compose, Material 3 and a ViewModel. It continues HW1 (the Kotlin console pizzeria): the same models (`MenuItem`, `Pizza`, `Drink`, `Dessert`, `Order`, sealed `OrderStatus`) and the `Pizzeria` business logic now power a real UI.

The user can browse the menu, search and filter it, build a cart, place an order and watch it go through cooking and delivery in real time.

## How to run

Requirements: Android Studio (recent, AGP 9), JDK 21, Android SDK 36.

1. Open the project folder in Android Studio and wait for Gradle sync.
2. Pick an emulator or device (Android 8.0+, API 26) and run the `app` configuration.

From the command line: `./gradlew :app:installDebug`.

## Project structure

```
app/src/main/java/com/example/
 ├─ MainActivity.kt                  entry point, logs Activity lifecycle
 ├─ contract/Preparable.kt           interface (from HW1)
 ├─ model/                           Models.kt, Order.kt, OrderStatus.kt (from HW1), Category.kt
 ├─ service/Pizzeria.kt              business logic from HW1: search, discounts, stats, processOrder
 ├─ data/SampleData.kt               the menu (was in HW1 Main.kt)
 └─ ui/
     ├─ PizzeriaUiState.kt           immutable screen state
     ├─ PizzeriaViewModel.kt         state + logic, exposes StateFlow, handles events
     ├─ PizzeriaApp.kt               Scaffold, top bar, bottom navigation, connects VM to screens
     ├─ theme/Theme.kt               Material 3 color scheme (light + dark), shapes
     ├─ components/                  reusable composables
     └─ screens/                     MenuScreen, OrdersScreen
```

## Where the requirements are demonstrated

| Requirement | Where |
|---|---|
| Kotlin + Compose + Material 3, consistent styling | `PizzeriaTheme` with a custom color scheme; Material 3 `Scaffold`, `CenterAlignedTopAppBar`, `NavigationBar`, `Card`, `FilterChip`, `AlertDialog`, `Badge` |
| ≥ 3 reusable composables | `SearchField`, `CategoryFilterRow`, `LabeledCheckbox`, `MenuItemCard`, `MenuItemImage`, `QuantityStepper`, `CartBar`, `OrderCard`, `StatusLabel`, `StatsCard`, `CheckoutDialog` |
| `Row`, `Column`, `Box` | `MenuItemCard` (Row of image + Column of texts), `MenuItemImage` (Box to center the image in a circle), `OrdersScreen` empty state (Box + Column) |
| `LazyColumn`, `Card`, `Text`, `Image` | menu and orders lists are `LazyColumn`s of `Card`s; `MenuItemImage` uses `Image` |
| User interactions | search, category filter, vegetarian filter, add/remove from cart, checkout with discount, cancel an order, expand a card |
| Input components | `OutlinedTextField` (search, name, address), `Checkbox` (vegetarian only, student discount), `Button`, `FilterChip`, icon buttons |
| UI updates on actions | every event goes to the ViewModel, which emits a new `PizzeriaUiState`; order status updates arrive from a coroutine |
| `remember` / `rememberSaveable` | `remember`: expanded state in `MenuItemCard`; `rememberSaveable`: selected tab, open dialog, checkout form fields |
| ViewModel, UI separated from state | `PizzeriaViewModel` owns all state and logic; screens are stateless and only get values + callbacks (state hoisting). Only `PizzeriaApp` touches the ViewModel |
| Behavior on Activity recreation | see the scenario below |

## remember vs rememberSaveable vs ViewModel

| | Where it lives | Rotation | Process death | Use for |
|---|---|---|---|---|
| `remember` | in the composition | **lost** | lost | cheap UI-only state you can lose (card expanded, animation) |
| `rememberSaveable` | composition + saved instance state `Bundle` | kept | **kept** | small UI state the user expects back (typed text, selected tab, dialog open). Only Bundle-able types, small size |
| `ViewModel` | `ViewModelStore`, outside the Activity | kept | **lost** (unless `SavedStateHandle`) | screen state and business logic, long-running work (`viewModelScope`) |

How it shows up in the app itself do these, then rotate the device:

| Do this | After rotation | Why |
|---|---|---|
| Tap a menu card to expand its description | collapsed again | `remember` in `MenuItemCard`: it lives in the composition, which is rebuilt with the new Activity |
| Open "Оформить", type name and address, tick the discount | dialog still open, text and checkbox kept | `rememberSaveable` in `CheckoutDialog` / `PizzeriaApp`: written to the saved instance state Bundle |
| Switch to the "Заказы" tab | still on "Заказы" | selected tab is `rememberSaveable` |
| Type a search query, pick a category, add items to the cart | query, filters and cart kept | stored in `PizzeriaViewModel`, the same instance is reused |
| Place an order and rotate while it is "Готовится" | progress keeps going | `processOrder` runs in `viewModelScope`, not tied to the Activity |

Process death (put the app in background, then `adb shell am kill com.example.pizzeria`, or *Developer options → Don't keep activities*, and reopen): the selected tab and an open checkout form come back (`rememberSaveable`), but the cart and orders are gone the ViewModel was recreated from scratch.

Logcat (tag `PizzeriaLifecycle`) shows `onDestroy, isChangingConfigurations=true` and a new `onCreate` on every rotation, while `ViewModel created` appears only once.

## What changed from HW1

- `MenuItem` got an `id` and `details()`; `Order` became an immutable data class (status changes create a copy so Compose notices them).
- `Pizzeria` no longer prints or stores orders: it is pure logic. `processOrder` reports statuses through a lambda (`onStatus`) instead of `println`, and the ViewModel runs it in `viewModelScope`.
- Higher-order functions are still used: `filterMenu(predicate)` powers search, `totalWithDiscount(order, rule)` applies the student discount.
