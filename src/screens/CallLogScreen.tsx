import React, { useEffect, useState } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
  Text,
  RefreshControl,
} from 'react-native';
import { useTheme } from '@react-navigation/native';
import { CallListItem } from '../components/CallListItem';
import { FilterBar } from '../components/FilterBar';
import { EmptyState } from '../components/EmptyState';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ActionButton } from '../components/ActionButton';

/**
 * CallLogScreen - Display all processed calls
 * 
 * MVP Features:
 * - List of all calls with action taken
 * - Search functionality
 * - Filter by action (blocked/allowed)
 * - Tap to view details
 * - Refresh functionality
 */
export function CallLogScreen() {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [calls, setCalls] = useState<any[]>([]);
  const [filteredCalls, setFilteredCalls] = useState<any[]>([]);
  const [searchText, setSearchText] = useState('');
  const [filterAction, setFilterAction] = useState<'ALL' | 'BLOCK' | 'ALLOW'>('ALL');

  const styles = StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: colors.background,
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
      marginBottom: 8,
    },
    filterContainer: {
      flexDirection: 'row',
      gap: 8,
      marginTop: 12,
    },
    filterButton: {
      paddingHorizontal: 12,
      paddingVertical: 6,
      borderRadius: 6,
      borderWidth: 1,
      borderColor: colors.border,
    },
    filterButtonActive: {
      backgroundColor: '#007AFF',
      borderColor: '#007AFF',
    },
    filterButtonText: {
      fontSize: 12,
      fontWeight: '600',
      color: colors.text,
    },
    filterButtonTextActive: {
      color: '#FFFFFF',
    },
    listContainer: {
      flex: 1,
    },
    emptyContainer: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
    },
  });

  useEffect(() => {
    loadCalls();
  }, []);

  useEffect(() => {
    filterCalls();
  }, [calls, searchText, filterAction]);

  const loadCalls = async () => {
    try {
      setLoading(true);
      // Mock data for MVP
      const mockCalls = [
        {
          id: '1',
          phoneNumber: '+1-555-0101',
          displayName: 'Unknown',
          action: 'BLOCK' as const,
          reason: 'PATTERN_RULE',
          timestamp: Date.now() - 3600000,
        },
        {
          id: '2',
          phoneNumber: '+1-555-0102',
          displayName: 'Mom',
          action: 'ALLOW' as const,
          reason: 'CONTACTS',
          timestamp: Date.now() - 1800000,
        },
        {
          id: '3',
          phoneNumber: '+1-555-0103',
          displayName: 'Unknown',
          action: 'BLOCK' as const,
          reason: 'MANUAL_BLOCK',
          timestamp: Date.now() - 900000,
        },
      ];
      setCalls(mockCalls);
    } catch (error) {
      console.error('Failed to load calls:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterCalls = () => {
    let filtered = calls;

    // Filter by action
    if (filterAction !== 'ALL') {
      filtered = filtered.filter((call) => call.action === filterAction);
    }

    // Filter by search text
    if (searchText) {
      filtered = filtered.filter(
        (call) =>
          call.phoneNumber.includes(searchText) ||
          call.displayName.toLowerCase().includes(searchText.toLowerCase())
      );
    }

    setFilteredCalls(filtered);
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadCalls();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen message="Loading call log..." />;
  }

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Call Log</Text>
      </View>

      {/* Search and Filter */}
      <FilterBar
        placeholder="Search by number or name..."
        onSearch={setSearchText}
        searchValue={searchText}
      />

      {/* Action Filters */}
      <View style={styles.filterContainer}>
        {['ALL', 'BLOCK', 'ALLOW'].map((action) => (
          <ActionButton
            key={action}
            title={action}
            onPress={() => setFilterAction(action as any)}
            variant={filterAction === action ? 'primary' : 'secondary'}
            size="small"
          />
        ))}
      </View>

      {/* Call List */}
      {filteredCalls.length === 0 ? (
        <View style={styles.emptyContainer}>
          <EmptyState
            icon="📞"
            title="No Calls Yet"
            message={
              searchText || filterAction !== 'ALL'
                ? 'No calls match your filters'
                : 'Your call log will appear here'
            }
          />
        </View>
      ) : (
        <FlatList
          style={styles.listContainer}
          data={filteredCalls}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <CallListItem
              phoneNumber={item.phoneNumber}
              displayName={item.displayName}
              action={item.action}
              reason={item.reason}
              timestamp={item.timestamp}
              onPress={() => {
                console.log('Call details:', item);
              }}
            />
          )}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
          }
        />
      )}
    </View>
  );
}
