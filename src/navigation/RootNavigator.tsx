import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { StyleSheet, Text } from 'react-native';
import { useTheme } from '@react-navigation/native';

// Import MVP screens
import { DashboardScreen } from '../screens/DashboardScreen';
import { CallLogScreen } from '../screens/CallLogScreen';
import { SettingsScreen } from '../screens/SettingsScreen';

// Placeholder screens for future implementation
const BlockAllowListScreen = () => null;
const PatternRulesScreen = () => null;
const SourcesScreen = () => null;
const PermissionWizardScreen = () => null;

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

/**
 * TabNavigator - Main tab-based navigation for MVP
 * 
 * MVP includes:
 * - Dashboard (home)
 * - Call Log
 * - Settings
 * 
 * Future tabs:
 * - Block/Allow Lists
 * - Pattern Rules
 * - Data Sources
 */
function TabNavigator() {
  const { colors } = useTheme();

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: '#007AFF',
        tabBarInactiveTintColor: colors.text,
        tabBarStyle: {
          ...styles.tabBar,
          backgroundColor: colors.card,
          borderTopColor: colors.border,
        },
        tabBarIcon: ({ focused, color }) => {
          let icon = '📱';

          if (route.name === 'Dashboard') {
            icon = focused ? '📊' : '📈';
          } else if (route.name === 'CallLog') {
            icon = focused ? '📞' : '☎️';
          } else if (route.name === 'Settings') {
            icon = focused ? '⚙️' : '🔧';
          }

          return <Text style={{ fontSize: 20, color }}>{icon}</Text>;
        },
        tabBarLabel: ({ focused, color }) => {
          let label = 'Tab';

          if (route.name === 'Dashboard') {
            label = 'Dashboard';
          } else if (route.name === 'CallLog') {
            label = 'Call Log';
          } else if (route.name === 'Settings') {
            label = 'Settings';
          }

          return (
            <Text
              style={{
                color,
                fontSize: 11,
                fontWeight: focused ? '600' : '400',
              }}
            >
              {label}
            </Text>
          );
        },
      })}
    >
      <Tab.Screen
        name="Dashboard"
        component={DashboardScreen}
        options={{
          title: 'Dashboard',
        }}
      />
      <Tab.Screen
        name="CallLog"
        component={CallLogScreen}
        options={{
          title: 'Call Log',
        }}
      />
      <Tab.Screen
        name="Settings"
        component={SettingsScreen}
        options={{
          title: 'Settings',
        }}
      />
    </Tab.Navigator>
  );
}

/**
 * RootNavigator - Main app navigation
 * 
 * Includes:
 * - Permission Wizard (first time setup)
 * - Main App (tab-based navigation)
 */
export default function RootNavigator() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <Stack.Screen
        name="PermissionWizard"
        component={PermissionWizardScreen}
        options={{
          animationEnabled: false,
        }}
      />
      <Stack.Screen
        name="MainApp"
        component={TabNavigator}
        options={{
          animationEnabled: false,
        }}
      />
    </Stack.Navigator>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    borderTopWidth: 1,
  },
});
