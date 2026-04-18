import React, { useEffect } from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider, useSelector } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import * as SplashScreen from 'expo-splash-screen';
import store, { RootState } from './store/store';
import { LoginScreen } from './screens/LoginScreen';
import { RegisterScreen } from './screens/RegisterScreen';
import { HomeScreen } from './screens/HomeScreen';
import { ARHuntingScreen } from './screens/ARHuntingScreen';
import { HeatMapScreen } from './screens/HeatMapScreen';
import { LeaderboardScreen } from './screens/LeaderboardScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { initDatabase } from './services/offlineStorage';
import { startSyncInterval } from './services/syncService';

SplashScreen.preventAutoHideAsync();

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const AuthStack = () => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    <Stack.Screen name='Login' component={LoginScreen} />
    <Stack.Screen name='Register' component={RegisterScreen} />
  </Stack.Navigator>
);

const AppStack = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: true,
      tabBarActiveTintColor: '#FF6B6B',
      tabBarInactiveTintColor: '#999999',
    }}
  >
    <Tab.Screen name='Home' component={HomeScreen} options={{ title: 'Ghost Hunter' }} />
    <Tab.Screen name='ARHunting' component={ARHuntingScreen} options={{ title: 'AR Hunt' }} />
    <Tab.Screen name='HeatMap' component={HeatMapScreen} options={{ title: 'Heat Maps' }} />
    <Tab.Screen name='Leaderboard' component={LeaderboardScreen} options={{ title: 'Leaderboard' }} />
    <Tab.Screen name='Profile' component={ProfileScreen} options={{ title: 'Profile' }} />
  </Tab.Navigator>
);

const RootNavigator = () => {
  const { isAuthenticated } = useSelector((state) => state.user);
  return (
    <NavigationContainer>
      {isAuthenticated ? <AppStack /> : <AuthStack />}
    </NavigationContainer>
  );
};

const AppContainer = () => {
  useEffect(() => {
    const initApp = async () => {
      try {
        await initDatabase();
        startSyncInterval(30000); // Sync every 30 seconds
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
      <StatusBar style='dark' />
    </SafeAreaProvider>
  );
};

export default function App() {
  return (
    <Provider store={store}>
      <AppContainer />
    </Provider>
  );
}
