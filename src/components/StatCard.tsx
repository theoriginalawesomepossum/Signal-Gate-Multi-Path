import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '@react-navigation/native';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  color?: string;
  onPress?: () => void;
}

/**
 * StatCard - Display a statistic with title, value, and optional icon
 * 
 * Used on Dashboard screen to show call statistics, device info, etc.
 */
export function StatCard({
  title,
  value,
  subtitle,
  icon,
  color = '#007AFF',
  onPress,
}: StatCardProps) {
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
      borderLeftWidth: 4,
      borderLeftColor: color,
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 2 },
      shadowOpacity: 0.1,
      shadowRadius: 4,
      elevation: 3,
    },
    leftContent: {
      flex: 1,
    },
    title: {
      fontSize: 12,
      color: colors.text,
      opacity: 0.7,
      marginBottom: 4,
      fontWeight: '500',
    },
    value: {
      fontSize: 24,
      fontWeight: 'bold',
      color: colors.text,
      marginBottom: subtitle ? 4 : 0,
    },
    subtitle: {
      fontSize: 12,
      color: colors.text,
      opacity: 0.6,
    },
    iconContainer: {
      width: 50,
      height: 50,
      borderRadius: 25,
      backgroundColor: color,
      opacity: 0.1,
      justifyContent: 'center',
      alignItems: 'center',
      marginLeft: 12,
    },
  });

  return (
    <View style={styles.container}>
      <View style={styles.leftContent}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.value}>{value}</Text>
        {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
      </View>
      {icon && <View style={styles.iconContainer}>{icon}</View>}
    </View>
  );
}
