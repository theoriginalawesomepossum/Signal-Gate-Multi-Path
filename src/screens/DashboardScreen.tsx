import React, { useEffect, useState } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
  RefreshControl,
  Text,
} from 'react-native';
import { useTheme } from '@react-navigation/native';
import { StatCard } from '../components/StatCard';
import { ActionButton } from '../components/ActionButton';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { TelemetryService } from '../lib/services/telemetry';
import { CallScreeningIntegration } from '../lib/services/call-screening-integration';

/**
 * DashboardScreen - Main dashboard showing call statistics and device info
 * 
 * MVP Features:
 * - Call statistics (total, blocked, allowed)
 * - Device health information
 * - Recent activity summary
 * - Quick action buttons
 * - Refresh functionality
 */
export function DashboardScreen() {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [stats, setStats] = useState<any>(null);

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
      paddingVertical: 16,
      backgroundColor: colors.card,
      borderBottomWidth: 1,
      borderBottomColor: colors.border,
    },
    headerTitle: {
      fontSize: 28,
      fontWeight: 'bold',
      color: colors.text,
    },
    headerSubtitle: {
      fontSize: 14,
      color: colors.text,
      opacity: 0.7,
      marginTop: 4,
    },
    section: {
      marginVertical: 12,
    },
    sectionTitle: {
      fontSize: 16,
      fontWeight: '600',
      color: colors.text,
      marginHorizontal: 16,
      marginVertical: 12,
    },
    quickActionsContainer: {
      flexDirection: 'row',
      paddingHorizontal: 16,
      gap: 12,
      marginVertical: 12,
    },
    actionButtonContainer: {
      flex: 1,
    },
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      const telemetry = TelemetryService.getInstance();
      await telemetry.collectDeviceStats();

      const allStats = telemetry.getAllStats();
      setStats(allStats);
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadStats();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen message="Loading statistics..." />;
  }

  return (
    <View style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>SignalGate</Text>
          <Text style={styles.headerSubtitle}>
            Your intelligent call screening assistant
          </Text>
        </View>

        {/* Call Statistics Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📊 Call Statistics</Text>
          <StatCard
            title="Total Calls Processed"
            value={stats?.usage?.callsProcessed || 0}
            subtitle="Since app installation"
            color="#007AFF"
          />
          <StatCard
            title="Calls Blocked"
            value={stats?.usage?.callsBlocked || 0}
            subtitle={`${
              stats?.usage?.callsProcessed
                ? Math.round(
                    (stats.usage.callsBlocked / stats.usage.callsProcessed) * 100
                  )
                : 0
            }% of total`}
            color="#FF3B30"
          />
          <StatCard
            title="Calls Allowed"
            value={stats?.usage?.callsAllowed || 0}
            subtitle={`${
              stats?.usage?.callsProcessed
                ? Math.round(
                    (stats.usage.callsAllowed / stats.usage.callsProcessed) * 100
                  )
                : 0
            }% of total`}
            color="#34C759"
          />
        </View>

        {/* Device Health Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>📱 Device Health</Text>
          <StatCard
            title="Device Model"
            value={stats?.device?.deviceModel || 'Unknown'}
            color="#5AC8FA"
          />
          <StatCard
            title="Android Version"
            value={`Android ${stats?.device?.androidVersion || 'Unknown'}`}
            color="#5AC8FA"
          />
          <StatCard
            title="Available RAM"
            value={`${Math.round((stats?.device?.ramAvailable || 0) / 1024)}GB`}
            color="#5AC8FA"
          />
          <StatCard
            title="Battery"
            value={`${stats?.device?.batteryPercentage || 0}%`}
            color="#34C759"
          />
        </View>

        {/* Performance Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>⚡ Performance</Text>
          <StatCard
            title="Avg Screening Time"
            value={`${Math.round(stats?.usage?.averageProcessingTime || 0)}ms`}
            subtitle="Lower is better"
            color="#FF9500"
          />
          <StatCard
            title="Memory Usage"
            value={`${stats?.device?.memoryUsage || 0}MB`}
            color="#FF9500"
          />
          <StatCard
            title="Crashes"
            value={stats?.usage?.crashCount || 0}
            subtitle="Errors handled gracefully"
            color="#34C759"
          />
        </View>

        {/* Quick Actions Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>⚙️ Quick Actions</Text>
          <View style={styles.quickActionsContainer}>
            <View style={styles.actionButtonContainer}>
              <ActionButton
                title="Call Log"
                onPress={() => {
                  console.log('Navigate to Call Log');
                }}
                variant="primary"
              />
            </View>
            <View style={styles.actionButtonContainer}>
              <ActionButton
                title="Block List"
                onPress={() => {
                  console.log('Navigate to Block List');
                }}
                variant="secondary"
              />
            </View>
          </View>
          <View style={styles.quickActionsContainer}>
            <View style={styles.actionButtonContainer}>
              <ActionButton
                title="Settings"
                onPress={() => {
                  console.log('Navigate to Settings');
                }}
                variant="secondary"
              />
            </View>
            <View style={styles.actionButtonContainer}>
              <ActionButton
                title="Refresh"
                onPress={onRefresh}
                variant="secondary"
              />
            </View>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}
