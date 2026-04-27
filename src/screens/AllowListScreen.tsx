import React, { useEffect, useState } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
  Text,
  RefreshControl,
  Alert,
} from 'react-native';
import { useTheme } from '@react-navigation/native';
import { CallListItem } from '../components/CallListItem';
import { FilterBar } from '../components/FilterBar';
import { EmptyState } from '../components/EmptyState';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { FAB } from '../components/FAB';
import { ConfirmDialog } from '../components/ConfirmDialog';

/**
 * AllowListScreen - Manage manually allowed numbers
 * 
 * Phase 2 Features:
 * - List of allowed numbers
 * - Add new allowed number
 * - Remove from allow list
 * - View smart allow-list (auto-learned)
 * - Search and filter
 */
export function AllowListScreen() {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [allowedNumbers, setAllowedNumbers] = useState<any[]>([]);
  const [filteredNumbers, setFilteredNumbers] = useState<any[]>([]);
  const [searchText, setSearchText] = useState('');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [selectedNumber, setSelectedNumber] = useState<any>(null);

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
    loadAllowedNumbers();
  }, []);

  useEffect(() => {
    filterNumbers();
  }, [allowedNumbers, searchText]);

  const loadAllowedNumbers = async () => {
    try {
      setLoading(true);
      // Mock data for Phase 2
      const mockAllowed = [
        {
          id: '1',
          phoneNumber: '+1-555-0301',
          displayName: 'Mom',
          reason: 'Manual Allow',
          dateAdded: Date.now() - 604800000,
        },
        {
          id: '2',
          phoneNumber: '+1-555-0302',
          displayName: 'Work',
          reason: 'Manual Allow',
          dateAdded: Date.now() - 259200000,
        },
      ];
      setAllowedNumbers(mockAllowed);
    } catch (error) {
      console.error('Failed to load allowed numbers:', error);
      Alert.alert('Error', 'Failed to load allowed numbers');
    } finally {
      setLoading(false);
    }
  };

  const filterNumbers = () => {
    let filtered = allowedNumbers;

    if (searchText) {
      filtered = filtered.filter(
        (item) =>
          item.phoneNumber.includes(searchText) ||
          item.displayName.toLowerCase().includes(searchText.toLowerCase())
      );
    }

    setFilteredNumbers(filtered);
  };

  const handleDeleteNumber = (number: any) => {
    setSelectedNumber(number);
    setShowDeleteDialog(true);
  };

  const confirmDelete = () => {
    if (selectedNumber) {
      setAllowedNumbers(
        allowedNumbers.filter((item) => item.id !== selectedNumber.id)
      );
      setShowDeleteDialog(false);
      Alert.alert('Success', 'Number removed from allow list');
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadAllowedNumbers();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen message="Loading allow list..." />;
  }

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Allow List</Text>
      </View>

      {/* Search */}
      <FilterBar
        placeholder="Search allowed numbers..."
        onSearch={setSearchText}
        searchValue={searchText}
      />

      {/* Allowed Numbers List */}
      {filteredNumbers.length === 0 ? (
        <View style={styles.emptyContainer}>
          <EmptyState
            icon="✅"
            title="No Allowed Numbers"
            message={
              searchText
                ? 'No allowed numbers match your search'
                : 'You haven\'t added any numbers to your allow list'
            }
          />
        </View>
      ) : (
        <FlatList
          style={styles.listContainer}
          data={filteredNumbers}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <CallListItem
              phoneNumber={item.phoneNumber}
              displayName={item.displayName}
              action="ALLOW"
              reason={item.reason}
              timestamp={item.dateAdded}
              onPress={() => handleDeleteNumber(item)}
            />
          )}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
          }
        />
      )}

      {/* FAB to add number */}
      <FAB icon="➕" onPress={() => Alert.alert('Add Number', 'Add new allowed number')} />

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        visible={showDeleteDialog}
        title="Remove from Allow List?"
        message={`Remove ${selectedNumber?.phoneNumber} from your allow list?`}
        confirmText="Remove"
        cancelText="Cancel"
        isDangerous
        onConfirm={confirmDelete}
        onCancel={() => setShowDeleteDialog(false)}
      />
    </View>
  );
}
