# Data Sources Dashboard Implementation

## Overview

The Data Sources panel is the **Multi-Point Hub** of the Signal-Gate application. It provides a centralized interface for managing all data sources that feed into the call screening engine.

## Architecture

### Components

1. **DashboardFragment** - UI container for the data sources list
2. **DataSourceAdapter** - RecyclerView adapter for rendering individual source items
3. **DashboardViewModel** - State management and business logic
4. **DataSourceRepository** - Data access layer and persistence
5. **SignalGateDatabase** - Room database for persistent storage

### Data Flow

```
UI (Fragment/Adapter)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Access)
    ↓
Database (Room/Persistence)
```

## The 8 Data Sources

The application manages the following data sources:

| # | Source Name | Type | Purpose |
|---|---|---|---|
| 1 | App Whitelist | Local CSV | Trusted phone numbers |
| 2 | App Blocklist | Local CSV | Blocked phone numbers |
| 3 | GitHub Community Blocklist | Remote URL | Community-maintained blocklist |
| 4 | Local File 1 | Local CSV | User-provided local file |
| 5 | Local File 2 | Local CSV | User-provided local file |
| 6 | Archived Spamlist | Local CSV | Historical spam data |
| 7 | User Remote URL 1 | Remote URL | User-configured remote source |
| 8 | User Remote URL 2 | Remote URL | User-configured remote source |

## UI Components

### Toggle Slider (Green)

- **Purpose**: Enable/disable a data source
- **Behavior**: 
  - When toggled ON (green): Source is active, LED turns blue
  - When toggled OFF (gray): Source is inactive, LED turns gray
- **Persistence**: State is automatically saved to the database

### LED Indicator (Blue/Gray)

- **Purpose**: Visual indicator of source status
- **Colors**:
  - **Blue (#00D1FF)**: Source is enabled and active
  - **Gray (#718096)**: Source is disabled or inactive
- **Logic**: LED state is driven by the `isEnabled` property of the source

### Status Text

- **Active**: Source is enabled and ready to use
- **Inactive**: Source is disabled and not being used
- **Disabled**: Source is intentionally turned off

## Implementation Details

### State Management

The ViewModel maintains the LED states through a `StateFlow`:

```kotlin
private val _ledStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
val ledStates: StateFlow<Map<Int, Boolean>> = _ledStates.asStateFlow()
```

This map tracks which sources are enabled:
- `true` → LED is blue (enabled)
- `false` → LED is gray (disabled)

### Toggle Logic

When a user toggles a source:

1. The switch listener calls `viewModel.toggleSourceEnabled(sourceId, isEnabled)`
2. ViewModel calls `repository.toggleSourceEnabled(sourceId, isEnabled)`
3. Repository updates the database via `sourceDao.updateSourceEnabled()`
4. Database change triggers the `dataSources` Flow
5. ViewModel observes the change and updates `_ledStates`
6. Adapter observes `_ledStates` and updates the LED indicator UI

### Persistence

All source state changes are persisted to the Room database:

```kotlin
@Query("UPDATE sources SET isEnabled = :isEnabled WHERE id = :id")
suspend fun updateSourceEnabled(id: Int, isEnabled: Boolean)
```

This ensures that:
- User preferences are saved across app sessions
- State is consistent across the entire application
- Database is the single source of truth

## UI Layout

### item_data_source.xml

Each source item displays:

```
[LED] [Icon] [Name/Type] [Entries] [Health] [LastSync] [Switch] [Sync] [Settings]
```

- **LED Indicator**: 8dp circle, blue when enabled, gray when disabled
- **Source Icon**: Indicates type (CSV, URL, etc.)
- **Name & Type**: Source name and data type
- **Entries Count**: Number of entries in this source
- **Health Status**: Current health state (Active/Inactive/Error)
- **Last Sync**: Timestamp of last synchronization
- **Toggle Switch**: Green when enabled, gray when disabled
- **Sync Button**: Manual sync trigger
- **Settings Button**: Access source settings

## Database Schema

### SourceEntity

```kotlin
@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "CSV", "XLSX", "URL", "MANUAL"
    val pathOrUrl: String,
    val isEnabled: Boolean = true,  // Controls LED state
    val lastSynced: Long = 0,
    val priority: Int = 0,
    val entriesCount: Int = 0,
    val healthStatus: String = "UNKNOWN",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

## Color Scheme

- **LED On (Blue)**: `#00D1FF` (neon_blue)
- **LED Off (Gray)**: `#718096` (text_muted)
- **Switch On**: `#00C853` (status_low - green)
- **Switch Off**: `#718096` (text_muted - gray)

## Future Enhancements

1. **Settings Dialog**: Implement the settings button to allow:
   - Edit source name and path/URL
   - Change priority
   - Delete source
   - View sync history

2. **Sync Engine**: Implement actual data synchronization:
   - Fetch data from remote URLs
   - Parse CSV/XLSX files
   - Validate and deduplicate entries
   - Update health status

3. **Advanced Filtering**: Add filters to show:
   - Only enabled sources
   - Only sources with errors
   - Sources by type

4. **Bulk Operations**: 
   - Select multiple sources
   - Bulk enable/disable
   - Bulk sync

5. **Import/Export**:
   - Import sources from file
   - Export source configuration
   - Share source lists

## Testing

### Manual Testing Checklist

- [ ] Toggle a source on/off
- [ ] Verify LED changes color (blue/gray)
- [ ] Verify status text updates (Active/Inactive)
- [ ] Close and reopen app
- [ ] Verify toggle state persists
- [ ] Verify LED state persists
- [ ] Tap sync button
- [ ] Verify sync updates last sync time
- [ ] Tap settings button
- [ ] Verify UI responds to all interactions

### Unit Tests

Test the ViewModel:
```kotlin
@Test
fun testToggleSourceEnabled() {
    // Verify database is updated
    // Verify LED state changes
}

@Test
fun testLedStateTracking() {
    // Verify LED states map is updated
    // Verify LED colors change appropriately
}
```

## Troubleshooting

### LED not changing color

- Check that `source_led_indicator` view exists in layout
- Verify `bg_led_indicator.xml` drawable is created
- Check that `neon_blue` and `text_muted` colors are defined

### Toggle not persisting

- Verify database is initialized
- Check that `sourceDao.updateSourceEnabled()` is being called
- Verify Room migrations are applied

### ViewModel not updating UI

- Verify Fragment is observing ViewModel flows
- Check that `lifecycleScope.launch` is used for Flow collection
- Verify coroutines are not being cancelled prematurely

## References

- [Android Room Documentation](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [Android ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
