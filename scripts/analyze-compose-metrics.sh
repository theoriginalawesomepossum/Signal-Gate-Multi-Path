#!/bin/bash
##############################################################################
# SignalGate Multi-Port - Compose Metrics Analyzer (Enhanced)
##############################################################################

set -e

SCRIPT_DIR="\( (cd " \)(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android"
METRICS_DIR="$ANDROID_DIR/app/build/compose_metrics"
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
SignalGate Compose Metrics Analyzer

USAGE:
    bash scripts/analyze-compose-metrics.sh [OPTIONS]

OPTIONS:
    --debug             Use Debug build (faster - RECOMMENDED)
    --release           Use Release build
    --clean             Clean before building
    --open              Open report directory
    --help              Show help
EOF
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    if [ ! -f "$ANDROID_DIR/gradlew" ]; then
        print_error "Gradle wrapper not found!"
        exit 1
    fi
    print_success "Prerequisites OK"
}

clean_build() {
    print_info "Cleaning build..."
    cd "$ANDROID_DIR"
    ./gradlew clean --quiet
    print_success "Clean completed"
}

build_with_metrics() {
    local variant=${1:-Debug}
    print_info "Building :app:assemble$variant with metrics..."
    cd "$ANDROID_DIR"
    
    ./gradlew :app:assemble$variant \
        -PcomposeCompilerReports=true \
        -PcomposeCompilerMetrics=true \
        --quiet

    print_success "Build completed ($variant)"
}

copy_metrics() {
    print_info "Copying metrics..."
    if [ ! -d "$METRICS_DIR" ]; then
        print_error "Metrics not found. Check build.gradle configuration."
        exit 1
    fi
    mkdir -p "$REPORT_DIR"
    cp -r "$METRICS_DIR"/* "$REPORT_DIR/" 2>/dev/null || true
    print_success "Metrics copied to $REPORT_DIR"
}

print_summary() {
    print_header "ANALYSIS SUMMARY"
    echo "📁 Report: $REPORT_DIR"
    if [ -f "$REPORT_DIR/classes.txt" ]; then
        local unstable=$(grep -c "UNSTABLE" "$REPORT_DIR/classes.txt" || echo "0")
        echo "🔍 Unstable Classes: $unstable"
    fi
}

# Main
VARIANT="Debug"
CLEAN=false
OPEN_DIR=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --release) VARIANT="Release"; shift ;;
        --debug) VARIANT="Debug"; shift ;;
        --clean) CLEAN=true; shift ;;
        --open) OPEN_DIR=true; shift ;;
        --help) show_help; exit 0 ;;
        *) print_error "Unknown option"; show_help; exit 1 ;;
    esac
done

print_header "SIGNALGATE COMPOSE METRICS ANALYZER"

check_prerequisites
[ "$CLEAN" = true ] && clean_build

build_with_metrics "$VARIANT"
copy_metrics

if [ -f "$PROJECT_ROOT/tools/metrics-analysis/analyze_metrics.py" ]; then
    print_info "Running Python analyzer..."
    python3 "$PROJECT_ROOT/tools/metrics-analysis/analyze_metrics.py" "$REPORT_DIR"
fi

print_summary

if [ "$OPEN_DIR" = true ]; then
    print_info "Opening report directory..."
    if command -v open &> /dev/null; then open "$REPORT_DIR"; fi
fi

print_success "Analysis complete!"
