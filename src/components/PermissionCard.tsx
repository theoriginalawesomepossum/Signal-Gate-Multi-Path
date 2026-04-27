import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';

interface PermissionCardProps {
  name: string;
  description: string;
  granted: boolean;
  onRequest?: () => void;
}

/**
 * PermissionCard - Display permission status and request button
 * 
 * Used on Settings and Onboarding screens to show permission status.
 */
export function PermissionCard({
  name,
  description,
  granted,
  onRequest,
}: PermissionCardProps) {
  const { colors } = useTheme();

  const styles = StyleSheet.create({
    container: {
      backgroundColor: colors.card,
      borderRadius: 12,
      padding: 16,
      marginVertical: 8,
      marginHorizontal: 16,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      borderWidth: 1,
      borderColor: granted ? '#34C759' : '#FF3B30',
    },
    leftContent: {
      flex: 1,
    },
    name: {
      fontSize: 16,
      fontWeight: '600',
      color: colors.text,
      marginBottom: 4,
    },
    description: {
      fontSize: 13,
      color: colors.text,
      opacity: 0.7,
    },
    statusContainer: {
      flexDirection: 'row',
      alignItems: 'center',
      marginLeft: 12,
    },
    statusBadge: {
      paddingHorizontal: 12,
      paddingVertical: 6,
      borderRadius: 6,
      marginRight: 8,
    },
    grantedBadge: {
      backgroundColor: '#34C759',
    },
    deniedBadge: {
      backgroundColor: '#FF3B30',
    },
    statusText: {
      fontSize: 12,
      fontWeight: '600',
      color: '#FFFFFF',
    },
    requestButton: {
      paddingHorizontal: 16,
      paddingVertical: 8,
      borderRadius: 6,
      backgroundColor: '#007AFF',
    },
    requestButtonText: {
      fontSize: 13,
      fontWeight: '600',
      color: '#FFFFFF',
    },
  });

  return (
    <View style={styles.container}>
      <View style={styles.leftContent}>
        <Text style={styles.name}>{name}</Text>
        <Text style={styles.description}>{description}</Text>
      </View>
      <View style={styles.statusContainer}>
        {granted ? (
          <View
            style={[styles.statusBadge, styles.grantedBadge]}
          >
            <Text style={styles.statusText}>✓ Granted</Text>
          </View>
        ) : (
          <>
            <View style={[styles.statusBadge, styles.deniedBadge]}>
              <Text style={styles.statusText}>✗ Denied</Text>
            </View>
            {onRequest && (
              <TouchableOpacity
                style={styles.requestButton}
                onPress={onRequest}
              >
                <Text style={styles.requestButtonText}>Request</Text>
              </TouchableOpacity>
            )}
          </>
        )}
      </View>
    </View>
  );
}
