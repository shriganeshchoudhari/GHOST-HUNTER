import React, { useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import * as SplashScreen from 'expo-splash-screen';

import store from './store/store';

// Keep the splash screen visible while we fetch resources
SplashScreen.preventAutoHideAsync();

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

/**
 * Authentication Stack - Shown when user is not authenticated
 */
const AuthStack = () => {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
        animationEnabled: true,
      }}
    >
      <Stack.Screen name="Login" component={() => null} />
      <Stack.Screen name="Register" component={() => null} />
    </Stack.Navigator>
  );
};

/**
 * Main App Stack - Shown when user is authenticated
 */
const AppStack = () => {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: true,
        tabBarActiveTintColor: '#FF6B6B',
        tabBarInactiveTintColor: '#999999',
      }}
    >
      <Tab.Screen
        name="Home"
        component={() => null}
        options={{
          title: 'Ghost Hunter',
          tabBarLabel: 'Home',
        }}
      />
      <Tab.Screen
        name="ARHunting"
        component={() => null}
        options={{
          title: 'AR Hunting',
          tabBarLabel: 'Hunt',
        }}
      />
      <Tab.Screen
        name="HeatMap"
        component={() => null}
        options={{
          title: 'Heat Maps',
          tabBarLabel: 'Maps',
        }}
      />
      <Tab.Screen
        name="Leaderboard"
        component={() => null}
        options={{
          title: 'Leaderboard',
          tabBarLabel: 'Leaderboard',
        }}
      />
      <Tab.Screen
        name="Profile"
        component={() => null}
        options={{
          title: 'Profile',
          tabBarLabel: 'Profile',
        }}
      />
    </Tab.Navigator>
  );
};

/**
 * Root Navigator - Manages authentication state
 */
const RootNavigator = () => {
  return (
    <NavigationContainer>
      <AppStack />
    </NavigationContainer>
  );
};

/**
 * App Container - Redux Provider and Root Navigator
 */
const AppContainer = () => {
  useEffect(() => {
    const initApp = async () => {
      try {
        // Initialize app (restore tokens, load user data, etc.)
        await SplashScreen.hideAsync();
      } catch (error) {
        console.error('Failed to initialize app:', error);
      }
    };

    initApp();
  }, []);

  return (
    <SafeAreaProvider>
      <RootNavigator />
      <StatusBar barStyle="dark-content" />
    </SafeAreaProvider>
  );
};

/**
 * Main App Export
 */
export default function App() {
  return (
    <Provider store={store}>
      <AppContainer />
    </Provider>
  );
}
