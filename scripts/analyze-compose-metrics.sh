#!/bin/bash

##############################################################################
# Compose Metrics Analyzer Script
# Automates the process of building, analyzing, and reporting Compose metrics
##############################################################################

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android"
APP_BUILD_DIR="$ANDROID_DIR/app/build"
METRICS_DIR="$APP_BUILD_DIR/compose_metrics"
REPORTS_DIR="$PROJECT_ROOT/compose_metrics/reports"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
REPORT_DIR="$REPORTS_DIR/metrics_$TIMESTAMP"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

##############################################################################
# Functions
##############################################################################

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

show_help() {
    cat << EOF
Compose Metrics Analyzer

USAGE:
    bash scripts/analyze-compose-metrics.sh [OPTIONS]

OPTIONS:
    --report            Generate JSON report (default)
    --open              Open metrics directory after analysis
    --clean             Clean metrics before building
    --help              Show this help message

EXAMPLES:
    # Run analysis with report
    bash scripts/analyze-compose-metrics.sh --report

    # Run analysis and open directory
    bash scripts/analyze-compose-metrics.sh --report --open

    # Clean and rebuild
    bash scripts/analyze-compose-metrics.sh --clean --report

EOF
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check if gradle wrapper exists
    if [ ! -f "$ANDROID_DIR/gradlew" ]; then
        print_error "Gradle wrapper not found at $ANDROID_DIR/gradlew"
        exit 1
    fi
    
    # Check Python
    if ! command -v python3 &> /dev/null; then
        print_warning "Python3 not found - will skip JSON report generation"
        PYTHON_AVAILABLE=false
    else
        PYTHON_AVAILABLE=true
        print_success "Python3 found: $(python3 --version)"
    fi
}

clean_build() {
    print_info "Cleaning build..."
    cd "$ANDROID_DIR"
    ./gradlew clean 2>&1 | tail -5
    print_success "Build cleaned"
}

build_with_metrics() {
    print_info "Building with Compose metrics enabled..."
    cd "$ANDROID_DIR"
    
    # Use assembleRelease to get release build or assembleDebug if you prefer
    if ./gradlew :app:assembleRelease --info 2>&1 | tail -20; then
        print_success "Build completed"
    else
        print_error "Build failed"
        exit 1
    fi
}

copy_metrics() {
    print_info "Copying metrics to reports directory..."
    
    if [ ! -d "$METRICS_DIR" ]; then
        print_error "Metrics directory not found: $METRICS_DIR"
        print_info "Make sure your build.gradle has the metrics configuration"
        exit 1
    fi
    
    mkdir -p "$REPORT_DIR"
    cp "$METRICS_DIR"/*.txt "$REPORT_DIR/" 2>/dev/null || true
    
    if [ -f "$REPORT_DIR/classes.txt" ]; then
        print_success "Metrics copied to: $REPORT_DIR"
    else
        print_error "No metrics files found in: $METRICS_DIR"
        exit 1
    fi
}

generate_report() {
    print_info "Generating analysis report..."
    
    if [ "$PYTHON_AVAILABLE" = true ]; then
        local json_output="$REPORT_DIR/analysis.json"
        
        if python3 "$PROJECT_ROOT/tools/metrics-analysis/analyze_metrics.py" \
            "$REPORT_DIR" --json "$json_output" 2>/dev/null; then
            print_success "Report generated: $json_output"
        else
            print_warning "Python analysis failed - using text reports only"
        fi
    else
        print_warning "Python not available - skipping JSON report"
    fi
}

print_summary() {
    print_header "ANALYSIS SUMMARY"
    
    if [ -f "$REPORT_DIR/classes.txt" ]; then
        local stable=$(grep -c "STABLE class:" "$REPORT_DIR/classes.txt" || echo "0")
        local unstable=$(grep -c "UNSTABLE class:" "$REPORT_DIR/classes.txt" || echo "0")
        local total=$((stable + unstable))
        
        echo ""
        echo "📊 CLASS STABILITY:"
        echo "  Total: $total"
        echo "  Stable: $stable"
        echo "  Unstable: $unstable"
        
        if [ "$unstable" -gt 0 ]; then
            echo ""
            echo "⚠️  Top unstable classes:"
            grep "UNSTABLE class:" "$REPORT_DIR/classes.txt" | head -5 | while read line; do
                echo "  $line"
            done
        fi
    fi
    
    if [ -f "$REPORT_DIR/composables.txt" ]; then
        local skippable=$(grep -c "skippable: true" "$REPORT_DIR/composables.txt" || echo "0")
        local not_skippable=$(grep -c "skippable: false" "$REPORT_DIR/composables.txt" || echo "0")
        local total=$((skippable + not_skippable))
        local percentage=0
        
        if [ "$total" -gt 0 ]; then
            percentage=$((skippable * 100 / total))
        fi
        
        echo ""
        echo "🎯 COMPOSABLE SKIPPABILITY:"
        echo "  Total: $total"
        echo "  Skippable: $skippable ($percentage%)"
        echo "  Not Skippable: $not_skippable"
        
        if [ "$percentage" -lt 90 ]; then
            echo ""
            echo "💡 Recommendation: Target >90% skippable composables"
        fi
    fi
    
    echo ""
    echo "📁 Reports saved to: $REPORT_DIR"
    echo ""
}

open_directory() {
    print_info "Opening metrics directory..."
    
    if command -v open &> /dev/null; then
        # macOS
        open "$REPORT_DIR"
    elif command -v xdg-open &> /dev/null; then
        # Linux
        xdg-open "$REPORT_DIR"
    elif command -v explorer &> /dev/null; then
        # Windows
        explorer "$(cygpath -w "$REPORT_DIR")"
    else
        print_warning "Cannot open directory - please open manually: $REPORT_DIR"
    fi
}

##############################################################################
# Main Script
##############################################################################

# Parse arguments
CLEAN_BUILD=false
GENERATE_REPORT=false
OPEN_DIR=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --report)
            GENERATE_REPORT=true
            shift
            ;;
        --open)
            OPEN_DIR=true
            shift
            ;;
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Execute workflow
print_header "COMPOSE METRICS ANALYZER"

check_prerequisites

if [ "$CLEAN_BUILD" = true ]; then
    clean_build
fi

build_with_metrics

copy_metrics

if [ "$GENERATE_REPORT" = true ]; then
    generate_report
fi

print_summary

if [ "$OPEN_DIR" = true ]; then
    open_directory
fi

print_success "Analysis complete!"
