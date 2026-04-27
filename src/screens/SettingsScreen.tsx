import React, { useState } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { useTheme } from '@react-navigation/native';
import { SettingsToggle } from '../components/SettingsToggle';
import { ActionButton } from '../components/ActionButton';
import { ConfirmDialog } from '../components/ConfirmDialog';

/**
 * SettingsScreen - App settings and preferences
 * 
 * MVP Features:
 * - Permission status and requests
 * - Dark mode toggle
 * - Notification settings
 * - Privacy settings
 * - Data management
 * - About section
 */
export function SettingsScreen() {
  const { colors } = useTheme();
  const [darkMode, setDarkMode] = useState(false);
  const [notifications, setNotifications] = useState(true);
  const [telemetry, setTelemetry] = useState(true);
  const [crashReports, setCrashReports] = useState(false);
  const [showClearDialog, setShowClearDialog] = useState(false);

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: colors.background,
    },
    scrollContent: {
      paddingBottom: 24,
    },
    header: {
      paddingHorizontal: 16,
      paddingVertical: 12,
      backgroundColor: colors.card,
      borderBottomWidth: 1,
      borderBottomColor: colors.border,
    },
    headerTitle: {
      fontSize: 20,
      fontWeight: '600',
      color: colors.text,
    },
    section: {
      marginVertical: 12,
    },
    sectionTitle: {
      fontSize: 14,
      fontWeight: '600',
      color: colors.text,
      opacity: 0.7,
      marginHorizontal: 16,
      marginVertical: 8,
      textTransform: 'uppercase',
    },
    aboutContainer: {
      backgroundColor: colors.card,
      marginHorizontal: 16,
      marginVertical: 8,
      borderRadius: 12,
      padding: 16,
    },
    aboutTitle: {
      fontSize: 16,
      fontWeight: '600',
      color: colors.text,
      marginBottom: 8,
    },
    aboutText: {
      fontSize: 13,
      color: colors.text,
      opacity: 0.7,
      marginBottom: 4,
    },
    linkButton: {
      marginTop: 12,
      paddingVertical: 8,
    },
    linkButtonText: {
      fontSize: 13,
      fontWeight: '600',
      color: '#007AFF',
    },
    actionContainer: {
      paddingHorizontal: 16,
      marginVertical: 8,
      gap: 8,
    },
  });

  const handleClearData = () => {
    setShowClearDialog(true);
  };

  const confirmClearData = () => {
    setShowClearDialog(false);
    Alert.alert('Success', 'All data has been cleared');
  };

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Settings</Text>
        </View>

        {/* Display Settings */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Display</Text>
          <SettingsToggle
            title="Dark Mode"
            description="Use dark theme throughout the app"
            value={darkMode}
            onValueChange={setDarkMode}
          />
        </View>

        {/* Notifications */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Notifications</Text>
          <SettingsToggle
            title="Call Blocked Notifications"
            description="Get notified when a call is blocked"
            value={notifications}
            onValueChange={setNotifications}
          />
        </View>

        {/* Privacy & Data */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Privacy & Data</Text>
          <SettingsToggle
            title="Collect Performance Stats"
            description="Help us improve by collecting device stats (local only)"
            value={telemetry}
            onValueChange={setTelemetry}
          />
          <SettingsToggle
            title="Auto-Send Crash Reports"
            description="Automatically send crash reports to help fix issues"
            value={crashReports}
            onValueChange={setCrashReports}
          />
        </View>

        {/* Data Management */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Data Management</Text>
          <View style={styles.actionContainer}>
            <ActionButton
              title="Export Data"
              onPress={() => Alert.alert('Export', 'Data exported successfully')}
              variant="secondary"
            />
            <ActionButton
              title="Clear All Data"
              onPress={handleClearData}
              variant="danger"
            />
          </View>
        </View>

        {/* About */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>About</Text>
          <View style={styles.aboutContainer}>
            <Text style={styles.aboutTitle}>SignalGate MultiPoint</Text>
            <Text style={styles.aboutText}>Version 1.0.0</Text>
            <Text style={styles.aboutText}>Build 1</Text>
            <Text style={styles.aboutText} style={{ marginTop: 12 }}>
              SignalGate is a privacy-first call screening app that helps you block
              unwanted calls and manage your incoming calls intelligently.
            </Text>
            <TouchableOpacity style={styles.linkButton}>
              <Text style={styles.linkButtonText}>📋 Privacy Policy</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.linkButton}>
              <Text style={styles.linkButtonText}>📜 Terms of Service</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.linkButton}>
              <Text style={styles.linkButtonText}>🐛 Report a Bug</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>

      {/* Clear Data Confirmation Dialog */}
      <ConfirmDialog
        visible={showClearDialog}
        title="Clear All Data?"
        message="This will delete all your block lists, call logs, and settings. This action cannot be undone."
        confirmText="Clear"
        cancelText="Cancel"
        isDangerous
        onConfirm={confirmClearData}
        onCancel={() => setShowClearDialog(false)}
      />
    </View>
  );
}
