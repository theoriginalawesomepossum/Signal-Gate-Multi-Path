app/src/main/java/com/signal_gate/multi_path/
│
├── data/
│   └── models/
│       ├── CallLogItem.kt       <-- Telemetry data model for call logs
│       └── ThreatSource.kt      <-- Data source state model
│
├── ui/
│   ├── screens/
│   │   ├── CallLogScreen.kt     <-- High-performance LazyColumn log
│   │   └── SourcesScreen.kt     <-- Managed feeds with glassmorphic cards
│   │
│   └── components/
│       └── GlassmorphicCard.kt  <-- Reusable layout wrapper for items