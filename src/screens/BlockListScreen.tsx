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
 * BlockListScreen - Manage manually blocked numbers
 * 
 * Phase 2 Features:
 * - List of blocked numbers
 * - Add new blocked number
 * - Remove from block list
 * - Search and filter
 * - Import/export functionality
 */
export function BlockListScreen() {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [blockedNumbers, setBlockedNumbers] = useState<any[]>([]);
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
    loadBlockedNumbers();
  }, []);

  useEffect(() => {
    filterNumbers();
  }, [blockedNumbers, searchText]);

  const loadBlockedNumbers = async () => {
    try {
      setLoading(true);
      // Mock data for Phase 2
      const mockBlocked = [
        {
          id: '1',
          phoneNumber: '+1-555-0201',
          displayName: 'Spam Caller',
          reason: 'Manual Block',
          dateAdded: Date.now() - 86400000,
        },
        {
          id: '2',
          phoneNumber: '+1-555-0202',
          displayName: 'Telemarketer',
          reason: 'Manual Block',
          dateAdded: Date.now() - 172800000,
        },
      ];
      setBlockedNumbers(mockBlocked);
    } catch (error) {
      console.error('Failed to load blocked numbers:', error);
      Alert.alert('Error', 'Failed to load blocked numbers');
    } finally {
      setLoading(false);
    }
  };

  const filterNumbers = () => {
    let filtered = blockedNumbers;

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
      setBlockedNumbers(
        blockedNumbers.filter((item) => item.id !== selectedNumber.id)
      );
      setShowDeleteDialog(false);
      Alert.alert('Success', 'Number removed from block list');
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadBlockedNumbers();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen message="Loading block list..." />;
  }

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Block List</Text>
      </View>

      {/* Search */}
      <FilterBar
        placeholder="Search blocked numbers..."
        onSearch={setSearchText}
        searchValue={searchText}
      />

      {/* Blocked Numbers List */}
      {filteredNumbers.length === 0 ? (
        <View style={styles.emptyContainer}>
          <EmptyState
            icon="🚫"
            title="No Blocked Numbers"
            message={
              searchText
                ? 'No blocked numbers match your search'
                : 'You haven\'t blocked any numbers yet'
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
              action="BLOCK"
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
      <FAB icon="➕" onPress={() => Alert.alert('Add Number', 'Add new blocked number')} />

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        visible={showDeleteDialog}
        title="Remove from Block List?"
        message={`Remove ${selectedNumber?.phoneNumber} from your block list?`}
        confirmText="Remove"
        cancelText="Cancel"
        isDangerous
        onConfirm={confirmDelete}
        onCancel={() => setShowDeleteDialog(false)}
      />
    </View>
  );
}
