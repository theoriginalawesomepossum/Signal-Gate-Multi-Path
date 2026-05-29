#!/bin/bash
##############################################################################
# Compose Metrics Analyzer Script - Improved for SignalGate
##############################################################################

set -e  # Exit on error

SCRIPT_DIR="\( (cd " \)(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android"
APP_BUILD_DIR="$ANDROID_DIR/app/build"
METRICS_DIR="$APP_BUILD_DIR/compose_metrics"
REPORTS_DIR="$PROJECT_ROOT/compose_metrics/reports"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
REPORT_DIR="$REPORTS_DIR/metrics_$TIMESTAMP"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "\( {BLUE}======================================== \){NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "\( {BLUE}======================================== \){NC}"
}

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error()   { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_info()    { echo -e "${BLUE}ℹ️  $1${NC}"; }

show_help() {
    cat << EOF
Compose Metrics Analyzer for SignalGate Multi-Port

USAGE:
    bash scripts/analyze-compose-metrics.sh [OPTIONS]

OPTIONS:
    --debug             Use Debug build (faster - RECOMMENDED)
    --release           Use Release build (slower, more accurate)
    --clean             Clean before building
    --open              Open report directory after analysis
    --help              Show this help message

EXAMPLES:
    bash scripts/analyze-compose-metrics.sh --debug --open
    bash scripts/analyze-compose-metrics.sh --clean --debug
EOF
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    if [ ! -f "$ANDROID_DIR/gradlew" ]; then
        print_error "Gradle wrapper not found at $ANDROID_DIR/gradlew"
        exit 1
    fi
    print_success "Gradle wrapper found"
}

clean_build() {
    print_info "Cleaning build..."
    cd "$ANDROID_DIR"
    ./gradlew clean --quiet
    print_success "Build cleaned"
}

build_with_metrics() {
    local variant=${1:-Debug}
    print_info "Building with Compose metrics ($variant variant)..."
    cd "$ANDROID_DIR"
    
    ./gradlew :app:assemble$variant \
        -PcomposeCompilerReports=true \
        -PcomposeCompilerMetrics=true \
        --quiet

    print_success "Build completed successfully"
}

copy_metrics() {
    print_info "Copying metrics to report directory..."
    
    if [ ! -d "$METRICS_DIR" ]; then
        print_error "Metrics directory not found: $METRICS_DIR"
        print_info "Tip: Ensure composeCompilerReports is enabled in build.gradle"
        exit 1
    fi

    mkdir -p "$REPORT_DIR"
    cp "$METRICS_DIR"/*.txt "$REPORT_DIR/" 2>/dev/null || true
    print_success "Metrics copied to: $REPORT_DIR"
}

# ... (keep your existing generate_report, print_summary, open_directory functions)

##############################################################################
# Main Script
##############################################################################

VARIANT="Debug"
CLEAN_BUILD=false
OPEN_DIR=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --debug)   VARIANT="Debug"; shift ;;
        --release) VARIANT="Release"; shift ;;
        --clean)   CLEAN_BUILD=true; shift ;;
        --open)    OPEN_DIR=true; shift ;;
        --help)    show_help; exit 0 ;;
        *)         print_error "Unknown option: $1"; show_help; exit 1 ;;
    esac
done

print_header "SIGNALGATE COMPOSE METRICS ANALYZER"

check_prerequisites

if [ "$CLEAN_BUILD" = true ]; then
    clean_build
fi

build_with_metrics "$VARIANT"
copy_metrics

# Run Python analyzer if available
if [ -f "$PROJECT_ROOT/tools/metrics-analysis/analyze_metrics.py" ]; then
    print_info "Running advanced Python analysis..."
    python3 "$PROJECT_ROOT/tools/metrics-analysis/analyze_metrics.py" "$REPORT_DIR"
else
    print_warning "Python analyzer not found"
fi

print_summary

if [ "$OPEN_DIR" = true ]; then
    open_directory
fi

print_success "Analysis complete!"
