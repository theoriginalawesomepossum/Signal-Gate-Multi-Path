import { describe, it, expect, beforeEach } from "vitest";
import { PatternRulesService } from "./pattern-rules";

describe("Pattern Rules Service", () => {
  beforeEach(() => {
    PatternRulesService.reset();
  });

  describe("addPrefixRule", () => {
    it("should add a prefix rule", () => {
      const rule = PatternRulesService.addPrefixRule("+1800", "Block 800 numbers");

      expect(rule).toBeDefined();
      expect(rule?.pattern).toBe("+1800");
      expect(rule?.type).toBe("PREFIX");
      expect(rule?.enabled).toBe(true);
    });

    it("should reject empty pattern", () => {
      const rule = PatternRulesService.addPrefixRule("", "Test");

      expect(rule).toBeNull();
    });

    it("should reject empty label", () => {
      const rule = PatternRulesService.addPrefixRule("+1800", "");

      expect(rule).toBeNull();
    });

    it("should prevent duplicates", () => {
      PatternRulesService.addPrefixRule("+1800", "First");
      const rule2 = PatternRulesService.addPrefixRule("+1800", "Second");

      expect(rule2).toBeNull();
    });
  });

  describe("addAreaCodeRule", () => {
    it("should add an area code rule", () => {
      const rule = PatternRulesService.addAreaCodeRule("212", "Block NYC area code");

      expect(rule).toBeDefined();
      expect(rule?.pattern).toBe("212");
      expect(rule?.type).toBe("AREA_CODE");
    });
  });

  describe("addCountryCodeRule", () => {
    it("should add a country code rule", () => {
      const rule = PatternRulesService.addCountryCodeRule("+44", "Block UK numbers");

      expect(rule).toBeDefined();
      expect(rule?.type).toBe("COUNTRY_CODE");
    });
  });

  describe("addRegexRule", () => {
    it("should add a valid regex rule", () => {
      const rule = PatternRulesService.addRegexRule("^\\+1(555|666)\\d{7}$", "Block specific patterns");

      expect(rule).toBeDefined();
      expect(rule?.type).toBe("REGEX");
    });

    it("should reject invalid regex", () => {
      const rule = PatternRulesService.addRegexRule("[invalid(", "Test");

      expect(rule).toBeNull();
    });
  });

  describe("getRules", () => {
    it("should return all rules", () => {
      PatternRulesService.addPrefixRule("+1800", "First");
      PatternRulesService.addAreaCodeRule("212", "Second");

      const rules = PatternRulesService.getRules();

      expect(rules.length).toBe(2);
    });
  });

  describe("getEnabledRules", () => {
    it("should return only enabled rules", () => {
      const rule1 = PatternRulesService.addPrefixRule("+1800", "First");
      const rule2 = PatternRulesService.addAreaCodeRule("212", "Second");

      PatternRulesService.disableRule(rule2!.id);

      const enabled = PatternRulesService.getEnabledRules();

      expect(enabled.length).toBe(1);
      expect(enabled[0].id).toBe(rule1!.id);
    });
  });

  describe("getRulesByType", () => {
    it("should return rules of specific type", () => {
      PatternRulesService.addPrefixRule("+1800", "First");
      PatternRulesService.addPrefixRule("+1900", "Second");
      PatternRulesService.addAreaCodeRule("212", "Third");

      const prefixRules = PatternRulesService.getRulesByType("PREFIX");

      expect(prefixRules.length).toBe(2);
      expect(prefixRules.every((r) => r.type === "PREFIX")).toBe(true);
    });
  });

  describe("enableRule / disableRule", () => {
    it("should enable and disable rules", () => {
      const rule = PatternRulesService.addPrefixRule("+1800", "Test");

      PatternRulesService.disableRule(rule!.id);
      expect(PatternRulesService.getRule(rule!.id)?.enabled).toBe(false);

      PatternRulesService.enableRule(rule!.id);
      expect(PatternRulesService.getRule(rule!.id)?.enabled).toBe(true);
    });
  });

  describe("deleteRule", () => {
    it("should delete a rule", () => {
      const rule = PatternRulesService.addPrefixRule("+1800", "Test");

      expect(PatternRulesService.getRules().length).toBe(1);

      const deleted = PatternRulesService.deleteRule(rule!.id);

      expect(deleted).toBe(true);
      expect(PatternRulesService.getRules().length).toBe(0);
    });
  });

  describe("updateRule", () => {
    it("should update rule label and notes", () => {
      const rule = PatternRulesService.addPrefixRule("+1800", "Old Label");
      const updated = PatternRulesService.updateRule(rule!.id, "New Label", "New notes");

      expect(updated?.label).toBe("New Label");
      expect(updated?.notes).toBe("New notes");
    });
  });

  describe("matches", () => {
    beforeEach(() => {
      PatternRulesService.addPrefixRule("+1800", "Block 800");
      PatternRulesService.addAreaCodeRule("212", "Block NYC");
      PatternRulesService.addCountryCodeRule("+44", "Block UK");
    });

    it("should match prefix rule", () => {
      const match = PatternRulesService.matches("+18005551234");

      expect(match).toBeDefined();
      expect(match?.pattern).toBe("+1800");
    });

    it("should match area code rule", () => {
      const match = PatternRulesService.matches("+12125551234");

      expect(match).toBeDefined();
      expect(match?.pattern).toBe("212");
    });

    it("should match country code rule", () => {
      const match = PatternRulesService.matches("+442071838750");

      expect(match).toBeDefined();
      expect(match?.pattern).toBe("+44");
    });

    it("should not match disabled rules", () => {
      const rule = PatternRulesService.getRules()[0];
      PatternRulesService.disableRule(rule.id);

      const match = PatternRulesService.matches("+18005551234");

      expect(match).toBeNull();
    });

    it("should not match non-matching numbers", () => {
      const match = PatternRulesService.matches("+14155551234");

      expect(match).toBeNull();
    });
  });

  describe("getAllMatches", () => {
    it("should return all matching rules", () => {
      PatternRulesService.addPrefixRule("+1", "Block US");
      PatternRulesService.addAreaCodeRule("415", "Block SF");
      PatternRulesService.addRegexRule("^\\+1415555\\d{4}$", "Block specific SF");

      const matches = PatternRulesService.getAllMatches("+14155551234");

      expect(matches.length).toBe(3);
    });
  });

  describe("getStats", () => {
    beforeEach(() => {
      PatternRulesService.addPrefixRule("+1800", "First");
      PatternRulesService.addPrefixRule("+1900", "Second");
      PatternRulesService.addAreaCodeRule("212", "Third");
      PatternRulesService.addCountryCodeRule("+44", "Fourth");

      const rule = PatternRulesService.getRules()[0];
      PatternRulesService.disableRule(rule.id);
    });

    it("should calculate correct statistics", () => {
      const stats = PatternRulesService.getStats();

      expect(stats.totalRules).toBe(4);
      expect(stats.enabledRules).toBe(3);
      expect(stats.disabledRules).toBe(1);
      expect(stats.rulesByType.PREFIX).toBe(2);
      expect(stats.rulesByType.AREA_CODE).toBe(1);
      expect(stats.rulesByType.COUNTRY_CODE).toBe(1);
    });
  });

  describe("exportAsCSV", () => {
    it("should export rules as CSV", () => {
      PatternRulesService.addPrefixRule("+1800", "Block 800", "Telemarketer");
      PatternRulesService.addAreaCodeRule("212", "Block NYC");

      const csv = PatternRulesService.exportAsCSV();

      expect(csv).toContain("Pattern");
      expect(csv).toContain("+1800");
      expect(csv).toContain("PREFIX");
      expect(csv).toContain("Block 800");
    });
  });

  describe("importFromCSV", () => {
    it("should import rules from CSV", () => {
      const csv = `Pattern,Type,Label,Enabled,Notes,Created
"+1800","PREFIX","Block 800","Yes","Telemarketer","2026-01-01T00:00:00.000Z"
"212","AREA_CODE","Block NYC","Yes","","2026-01-01T00:00:00.000Z"`;

      const result = PatternRulesService.importFromCSV(csv);

      expect(result.imported).toBe(2);
      expect(result.failed).toBe(0);
      expect(PatternRulesService.getRules().length).toBe(2);
    });

    it("should handle invalid rows", () => {
      const csv = `Pattern,Type,Label,Enabled,Notes,Created
"+1800","PREFIX","Block 800","Yes","","2026-01-01T00:00:00.000Z"
"invalid","INVALID_TYPE","Test","Yes","","2026-01-01T00:00:00.000Z"`;

      const result = PatternRulesService.importFromCSV(csv);

      expect(result.imported).toBe(1);
      expect(result.failed).toBe(1);
    });
  });

  describe("clearAll", () => {
    it("should clear all rules", () => {
      PatternRulesService.addPrefixRule("+1800", "First");
      PatternRulesService.addAreaCodeRule("212", "Second");

      const cleared = PatternRulesService.clearAll();

      expect(cleared).toBe(2);
      expect(PatternRulesService.getRules().length).toBe(0);
    });
  });

  describe("regex pattern matching", () => {
    it("should match complex regex patterns", () => {
      PatternRulesService.addRegexRule("^\\+1(555|666)\\d{7}$", "Block 555/666");

      expect(PatternRulesService.matches("+15551234567")).toBeDefined();
      expect(PatternRulesService.matches("+16661234567")).toBeDefined();
      expect(PatternRulesService.matches("+14151234567")).toBeNull();
    });
  });
});
