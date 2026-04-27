/**
 * SignalGate-MultiPoint Database Schema
 * 
 * Uses Drizzle ORM to define the database structure for:
 * - Block/Allow lists with source tracking
 * - Data sources (local files, remote URLs)
 * - Sync history and status
 */

import { sql } from "drizzle-orm";
import { integer, sqliteTable, text, real, primaryKey } from "drizzle-orm/sqlite-core";

/**
 * DataSource Table
 * Tracks all block/allow list sources (local files and remote URLs)
 */
export const dataSourcesTable = sqliteTable("data_sources", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull(), // e.g., "My Personal Blocks"
  type: text("type").notNull(), // "local_file" | "remote_url"
  sourceUrl: text("source_url"), // Path or URL
  enabled: integer("enabled").default(1).notNull(), // 1 = enabled, 0 = disabled
  lastSync: integer("last_sync"), // Unix timestamp
  syncStatus: text("sync_status").default("pending"), // "pending" | "success" | "error"
  syncError: text("sync_error"), // Error message if sync failed
  rowCount: integer("row_count").default(0), // Number of entries from this source
  createdAt: integer("created_at").default(sql`(strftime('%s', 'now'))`),
  updatedAt: integer("updated_at").default(sql`(strftime('%s', 'now'))`),
});

/**
 * BlockEntry Table
 * Stores phone numbers and patterns to block
 */
export const blockEntriesTable = sqliteTable(
  "block_entries",
  {
    id: integer("id").primaryKey({ autoIncrement: true }),
    phoneNumber: text("phone_number").notNull(), // E.164 format or pattern
    action: text("action").default("BLOCK"), // "BLOCK" | "ALLOW" (for override entries)
    label: text("label"), // e.g., "Telemarketer - Solar"
    priority: integer("priority").default(3), // 1 (Low) to 5 (High)
    sourceId: integer("source_id").references(() => dataSourcesTable.id, {
      onDelete: "cascade",
    }),
    isPattern: integer("is_pattern").default(0), // 1 = regex/prefix pattern, 0 = exact number
    isManual: integer("is_manual").default(0), // 1 = user-added, 0 = from import
    createdAt: integer("created_at").default(sql`(strftime('%s', 'now'))`),
    updatedAt: integer("updated_at").default(sql`(strftime('%s', 'now'))`),
  },
  (table) => ({
    phoneSourceUnique: primaryKey({
      columns: [table.phoneNumber, table.sourceId],
    }),
  })
);

/**
 * AllowEntry Table
 * Stores phone numbers and patterns to always allow (whitelist)
 */
export const allowEntriesTable = sqliteTable(
  "allow_entries",
  {
    id: integer("id").primaryKey({ autoIncrement: true }),
    phoneNumber: text("phone_number").notNull(), // E.164 format or pattern
    label: text("label"), // e.g., "My Doctor"
    sourceId: integer("source_id").references(() => dataSourcesTable.id, {
      onDelete: "cascade",
    }),
    isPattern: integer("is_pattern").default(0), // 1 = regex/prefix pattern, 0 = exact number
    isManual: integer("is_manual").default(0), // 1 = user-added, 0 = from import
    createdAt: integer("created_at").default(sql`(strftime('%s', 'now'))`),
    updatedAt: integer("updated_at").default(sql`(strftime('%s', 'now'))`),
  },
  (table) => ({
    phoneSourceUnique: primaryKey({
      columns: [table.phoneNumber, table.sourceId],
    }),
  })
);

/**
 * CallLog Table
 * Tracks blocked and allowed calls for user review
 */
export const callLogTable = sqliteTable("call_log", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  phoneNumber: text("phone_number").notNull(),
  callerName: text("caller_name"), // If available
  action: text("action").notNull(), // "BLOCKED" | "ALLOWED"
  reason: text("reason"), // Why it was blocked/allowed
  sourceId: integer("source_id").references(() => dataSourcesTable.id, {
    onDelete: "set null",
  }),
  timestamp: integer("timestamp").default(sql`(strftime('%s', 'now'))`),
});

/**
 * Settings Table
 * Stores app configuration and preferences
 */
export const settingsTable = sqliteTable("settings", {
  key: text("key").primaryKey(),
  value: text("value").notNull(),
  type: text("type").default("string"), // "string" | "number" | "boolean" | "json"
});

/**
 * SyncHistory Table
 * Tracks all sync operations for debugging and recovery
 */
export const syncHistoryTable = sqliteTable("sync_history", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  sourceId: integer("source_id").references(() => dataSourcesTable.id, {
    onDelete: "cascade",
  }),
  status: text("status").notNull(), // "success" | "error" | "partial"
  entriesAdded: integer("entries_added").default(0),
  entriesUpdated: integer("entries_updated").default(0),
  entriesRemoved: integer("entries_removed").default(0),
  errorMessage: text("error_message"),
  syncDuration: integer("sync_duration"), // milliseconds
  timestamp: integer("timestamp").default(sql`(strftime('%s', 'now'))`),
});

/**
 * Type definitions for easier use in the app
 */
export type DataSource = typeof dataSourcesTable.$inferSelect;
export type NewDataSource = typeof dataSourcesTable.$inferInsert;

export type BlockEntry = typeof blockEntriesTable.$inferSelect;
export type NewBlockEntry = typeof blockEntriesTable.$inferInsert;

export type AllowEntry = typeof allowEntriesTable.$inferSelect;
export type NewAllowEntry = typeof allowEntriesTable.$inferInsert;

export type CallLogEntry = typeof callLogTable.$inferSelect;
export type NewCallLogEntry = typeof callLogTable.$inferInsert;

export type Setting = typeof settingsTable.$inferSelect;
export type NewSetting = typeof settingsTable.$inferInsert;

export type SyncHistoryEntry = typeof syncHistoryTable.$inferSelect;
export type NewSyncHistoryEntry = typeof syncHistoryTable.$inferInsert;
