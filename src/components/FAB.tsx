import React from 'react';
import { TouchableOpacity, StyleSheet, Text } from 'react-native';

interface FABProps {
  icon: string;
  onPress: () => void;
  color?: string;
}

/**
 * FAB - Floating Action Button
 * 
 * Used on screens like Block List, Allow List, Pattern Rules
 * to provide quick access to add new items.
 */
export function FAB({ icon, onPress, color = '#007AFF' }: FABProps) {
  const styles = StyleSheet.create({
    fab: {
      position: 'absolute',
      bottom: 24,
      right: 24,
      width: 56,
      height: 56,
      borderRadius: 28,
      backgroundColor: color,
      justifyContent: 'center',
      alignItems: 'center',
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.3,
      shadowRadius: 8,
      elevation: 8,
    },
    icon: {
      fontSize: 28,
      color: '#FFFFFF',
    },
  });

  return (
    <TouchableOpacity style={styles.fab} onPress={onPress} activeOpacity={0.8}>
      <Text style={styles.icon}>{icon}</Text>
    </TouchableOpacity>
  );
}
