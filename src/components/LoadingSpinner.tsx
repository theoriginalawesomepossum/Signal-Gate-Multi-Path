import React from 'react';
import { View, ActivityIndicator, StyleSheet, Text } from 'react-native';
import { useTheme } from '@react-navigation/native';

interface LoadingSpinnerProps {
  size?: 'small' | 'large';
  message?: string;
  fullScreen?: boolean;
}

/**
 * LoadingSpinner - Display loading indicator
 * 
 * Used when data is being fetched or processed.
 * Can be full screen or inline.
 */
export function LoadingSpinner({
  size = 'large',
  message,
  fullScreen = false,
}: LoadingSpinnerProps) {
  const { colors } = useTheme();

  const styles = StyleSheet.create({
    container: {
      flex: fullScreen ? 1 : 0,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: fullScreen ? colors.background : 'transparent',
      paddingVertical: fullScreen ? 0 : 24,
    },
    message: {
      marginTop: 16,
      fontSize: 14,
      color: colors.text,
      opacity: 0.7,
    },
  });

  return (
    <View style={styles.container}>
      <ActivityIndicator
        size={size === 'small' ? 'small' : 'large'}
        color="#007AFF"
      />
      {message && <Text style={styles.message}>{message}</Text>}
    </View>
  );
}
