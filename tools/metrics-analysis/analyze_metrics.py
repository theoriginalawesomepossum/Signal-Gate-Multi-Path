#!/usr/bin/env python3
"""
Compose Metrics Analyzer
Parses Jetpack Compose Compiler Metrics and identifies optimization opportunities.
"""

import json
import os
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple


class ComposeMetricsAnalyzer:
    """Analyzes Compose compiler metrics reports."""
    
    def __init__(self, metrics_dir: str):
        """Initialize analyzer with metrics directory."""
        self.metrics_dir = Path(metrics_dir)
        self.classes_file = self.metrics_dir / "classes.txt"
        self.composables_file = self.metrics_dir / "composables.txt"
        self.metrics_file = self.metrics_dir / "composables-metrics.txt"
        
    def validate(self) -> bool:
        """Check if metrics directory exists and contains required files."""
        if not self.metrics_dir.exists():
            print(f"❌ Metrics directory not found: {self.metrics_dir}")
            return False
        
        required_files = [self.classes_file, self.composables_file]
        missing = [f for f in required_files if not f.exists()]
        
        if missing:
            print(f"⚠️  Missing files: {missing}")
            return False
        
        print(f"✅ Found metrics in: {self.metrics_dir}")
        return True
    
    def parse_classes(self) -> Dict:
        """Parse classes.txt and categorize by stability."""
        result = {
            "stable": [],
            "unstable": [],
            "total": 0
        }
        
        if not self.classes_file.exists():
            return result
        
        with open(self.classes_file, 'r') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                
                result["total"] += 1
                
                if "UNSTABLE class:" in line:
                    # Extract class name
                    class_name = line.split("UNSTABLE class:")[-1].strip()
                    result["unstable"].append(class_name)
                elif "STABLE class:" in line:
                    class_name = line.split("STABLE class:")[-1].strip()
                    result["stable"].append(class_name)
        
        return result
    
    def parse_composables(self) -> Dict:
        """Parse composables.txt and categorize by skippability."""
        result = {
            "skippable": [],
            "not_skippable": [],
            "total": 0
        }
        
        if not self.composables_file.exists():
            return result
        
        with open(self.composables_file, 'r') as f:
            for line in f:
                line = line.strip()
                if not line or "composable:" not in line.lower():
                    continue
                
                result["total"] += 1
                
                if "skippable: true" in line or "skippable: false" in line:
                    if "skippable: true" in line:
                        # Extract composable name
                        name = line.split("composable:")[-1].split("skippable:")[0].strip()
                        result["skippable"].append(name)
                    else:
                        name = line.split("composable:")[-1].split("skippable:")[0].strip()
                        result["not_skippable"].append(name)
        
        return result
    
    def calculate_metrics(self, classes: Dict, composables: Dict) -> Dict:
        """Calculate optimization metrics."""
        total_composables = composables["total"]
        skippable = len(composables["skippable"])
        unstable_classes = len(classes["unstable"])
        
        return {
            "total_composables": total_composables,
            "skippable_composables": skippable,
            "skippable_percentage": (skippable / total_composables * 100) if total_composables > 0 else 0,
            "not_skippable_composables": len(composables["not_skippable"]),
            "total_classes": classes["total"],
            "stable_classes": len(classes["stable"]),
            "unstable_classes": unstable_classes,
            "stability_percentage": ((classes["total"] - unstable_classes) / classes["total"] * 100) if classes["total"] > 0 else 0,
        }
    
    def analyze(self) -> Dict:
        """Run complete analysis."""
        if not self.validate():
            return {}
        
        classes = self.parse_classes()
        composables = self.parse_composables()
        metrics = self.calculate_metrics(classes, composables)
        
        return {
            "timestamp": datetime.now().isoformat(),
            "metrics_dir": str(self.metrics_dir),
            "classes": classes,
            "composables": composables,
            "metrics": metrics,
            "recommendations": self._get_recommendations(classes, composables, metrics)
        }
    
    def _get_recommendations(self, classes: Dict, composables: Dict, metrics: Dict) -> List[str]:
        """Generate optimization recommendations."""
        recs = []
        
        if metrics["skippable_percentage"] < 90:
            recs.append(f"⚠️  Skippable composables at {metrics['skippable_percentage']:.1f}% (target: >90%)")
        
        if metrics["unstable_classes"] > 0:
            recs.append(f"⚠️  Found {metrics['unstable_classes']} unstable classes - these cause unnecessary recompositions")
        
        if len(composables["not_skippable"]) > 0 and metrics["unstable_classes"] > 0:
            recs.append("💡 Try making unstable classes @Immutable or extracting stable state")
        
        if metrics["skippable_percentage"] >= 90:
            recs.append("✅ Great! Most composables are skippable")
        
        return recs
    
    def print_report(self, analysis: Dict):
        """Print formatted analysis report."""
        if not analysis:
            return
        
        m = analysis["metrics"]
        c = analysis["composables"]
        cl = analysis["classes"]
        
        print("\n" + "="*60)
        print("📊 COMPOSE METRICS ANALYSIS REPORT")
        print("="*60)
        
        print(f"\n📈 COMPOSABLES")
        print(f"  Total: {m['total_composables']}")
        print(f"  Skippable: {m['skippable_composables']} ({m['skippable_percentage']:.1f}%)")
        print(f"  Not Skippable: {m['not_skippable_composables']}")
        
        print(f"\n🔧 CLASSES")
        print(f"  Total: {m['total_classes']}")
        print(f"  Stable: {m['stable_classes']}")
        print(f"  Unstable: {m['unstable_classes']}")
        print(f"  Stability: {m['stability_percentage']:.1f}%")
        
        if cl["unstable"]:
            print(f"\n⚠️  TOP UNSTABLE CLASSES:")
            for cls in cl["unstable"][:10]:
                print(f"    - {cls}")
            if len(cl["unstable"]) > 10:
                print(f"    ... and {len(cl['unstable']) - 10} more")
        
        print(f"\n💡 RECOMMENDATIONS:")
        for rec in analysis["recommendations"]:
            print(f"  {rec}")
        
        print("\n" + "="*60 + "\n")
    
    def save_json(self, analysis: Dict, output_path: str):
        """Save analysis as JSON."""
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(output_file, 'w') as f:
            json.dump(analysis, f, indent=2)
        
        print(f"📄 Report saved to: {output_file}")


def main():
    """Main entry point."""
    if len(sys.argv) < 2:
        print("Usage: python3 analyze_metrics.py <metrics_dir> [--json output.json]")
        print("\nExample:")
        print("  python3 analyze_metrics.py android/app/build/compose_metrics/")
        print("  python3 analyze_metrics.py android/app/build/compose_metrics/ --json report.json")
        sys.exit(1)
    
    metrics_dir = sys.argv[1]
    output_json = None
    
    if "--json" in sys.argv:
        idx = sys.argv.index("--json")
        if idx + 1 < len(sys.argv):
            output_json = sys.argv[idx + 1]
    
    analyzer = ComposeMetricsAnalyzer(metrics_dir)
    analysis = analyzer.analyze()
    
    if analysis:
        analyzer.print_report(analysis)
        if output_json:
            analyzer.save_json(analysis, output_json)
    else:
        print("❌ Analysis failed")
        sys.exit(1)


if __name__ == "__main__":
    main()
