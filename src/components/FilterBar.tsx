import React from 'react';
import { View, TextInput, StyleSheet, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';

interface FilterBarProps {
  placeholder?: string;
  onSearch: (text: string) => void;
  onFilterPress?: () => void;
  searchValue?: string;
}

/**
 * FilterBar - Search and filter input component
 * 
 * Used on Call Log, Block List, and other screens
 * to provide search and filtering capabilities.
 */
export function FilterBar({
  placeholder = 'Search...',
  onSearch,
  onFilterPress,
  searchValue = '',
}: FilterBarProps) {
  const { colors } = useTheme();

  const styles = StyleSheet.create({
    container: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: 16,
      paddingVertical: 12,
      backgroundColor: colors.card,
      borderBottomWidth: 1,
      borderBottomColor: colors.border,
    },
    searchInput: {
      flex: 1,
      backgroundColor: colors.background,
      borderRadius: 8,
      paddingHorizontal: 12,
      paddingVertical: 8,
      fontSize: 14,
      color: colors.text,
      marginRight: onFilterPress ? 8 : 0,
    },
    filterButton: {
      padding: 8,
      borderRadius: 8,
      backgroundColor: colors.background,
    },
    filterIcon: {
      fontSize: 18,
      color: colors.text,
    },
  });

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.searchInput}
        placeholder={placeholder}
        placeholderTextColor={colors.text}
        value={searchValue}
        onChangeText={onSearch}
      />
      {onFilterPress && (
        <TouchableOpacity
          style={styles.filterButton}
          onPress={onFilterPress}
          activeOpacity={0.7}
        >
          <Text style={styles.filterIcon}>⚙️</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}
