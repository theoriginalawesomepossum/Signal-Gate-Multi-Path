# Compose Metrics Analysis Tools

This directory contains scripts and tools for analyzing Jetpack Compose Compiler Metrics.

## Files

- `analyze_metrics.py` - Python script to parse and analyze metrics reports
- `README.md` - This file

## Usage

### Basic Analysis
```bash
python3 tools/metrics-analysis/analyze_metrics.py \
  --metrics-dir android/app/build/compose_metrics/
```

### Generate JSON Report
```bash
python3 tools/metrics-analysis/analyze_metrics.py \
  --metrics-dir android/app/build/compose_metrics/ \
  --output compose_metrics/reports/report.json
```

### Interactive Mode
```bash
python3 tools/metrics-analysis/analyze_metrics.py --interactive
```

## Features

- ✅ Parse `classes.txt` - Identify unstable classes
- ✅ Parse `composables.txt` - Find non-skippable composables
- ✅ Generate JSON reports for tracking
- ✅ Calculate optimization metrics
- ✅ Suggest fixes for common patterns

## Requirements

- Python 3.7+
- Standard library only (no external dependencies)

## Troubleshooting

See `compose_metrics/ANALYSIS_GUIDE.md` for detailed troubleshooting.
