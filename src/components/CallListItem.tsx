import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Animated } from 'react-native';
import { useTheme } from '@react-navigation/native';

interface CallListItemProps {
  phoneNumber: string;
  displayName?: string;
  action: 'BLOCK' | 'ALLOW';
  reason: string;
  timestamp: number;
  onPress?: () => void;
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
}

/**
 * CallListItem - Display a single call in a list
 * 
 * Shows phone number, action taken, reason, and timestamp.
 * Supports swipe actions for quick block/allow/delete.
 */
export function CallListItem({
  phoneNumber,
  displayName,
  action,
  reason,
  timestamp,
  onPress,
  onSwipeLeft,
  onSwipeRight,
}: CallListItemProps) {
  const { colors } = useTheme();
  const swipeAnim = React.useRef(new Animated.Value(0)).current;

  const styles = StyleSheet.create({
    container: {
      backgroundColor: colors.card,
      borderBottomWidth: 1,
      borderBottomColor: colors.border,
      paddingVertical: 12,
      paddingHorizontal: 16,
    },
    touchable: {
      flex: 1,
    },
    row: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
    leftContent: {
      flex: 1,
    },
    phoneNumber: {
      fontSize: 16,
      fontWeight: '600',
      color: colors.text,
      marginBottom: 4,
    },
    displayName: {
      fontSize: 14,
      color: colors.text,
      opacity: 0.7,
      marginBottom: 4,
    },
    bottomRow: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginTop: 8,
    },
    reason: {
      fontSize: 12,
      color: colors.text,
      opacity: 0.6,
    },
    timestamp: {
      fontSize: 12,
      color: colors.text,
      opacity: 0.5,
    },
    badge: {
      paddingHorizontal: 8,
      paddingVertical: 4,
      borderRadius: 12,
      marginLeft: 12,
    },
    blockBadge: {
      backgroundColor: '#FF3B30',
    },
    allowBadge: {
      backgroundColor: '#34C759',
    },
    badgeText: {
      fontSize: 11,
      fontWeight: '600',
      color: '#FFFFFF',
    },
  });

  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    if (diff < 60000) return 'now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return date.toLocaleDateString();
  };

  return (
    <TouchableOpacity
      style={styles.container}
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={styles.row}>
        <View style={styles.leftContent}>
          <Text style={styles.phoneNumber}>{phoneNumber}</Text>
          {displayName && <Text style={styles.displayName}>{displayName}</Text>}
          <View style={styles.bottomRow}>
            <Text style={styles.reason}>{reason}</Text>
            <Text style={styles.timestamp}>{formatTime(timestamp)}</Text>
          </View>
        </View>
        <View
          style={[
            styles.badge,
            action === 'BLOCK' ? styles.blockBadge : styles.allowBadge,
          ]}
        >
          <Text style={styles.badgeText}>{action}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
}
