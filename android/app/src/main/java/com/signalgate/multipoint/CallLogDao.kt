app/src/main/java/com/signal_gate/multi_path/
│
├── data/
│   └── dao/
│       └── CallLogDao.kt       <-- Room Database query interface
│
├── ui/
│   └── viewmodels/
│       └── TelemetryViewModel.kt <-- Koin-injected reactive state coordinator
│
└── service/
    └── AdvancedCallScreeningService.kt <-- Integrating the live UI stream hook