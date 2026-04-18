import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import { Camera } from 'expo-camera';
import * as Location from 'expo-location';
import NetInfo from '@react-native-community/netinfo';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store/store';
import { startSession, addMeasurement, endSession } from '../store/slices/telemetrySlice';
import { Button } from '../components/Button';
import { saveTelemetryOffline } from '../services/offlineStorage';

export const ARHuntingScreen = ({ navigation }) => {
  const [hasPermission, setHasPermission] = useState(null);
  const [signalStrength, setSignalStrength] = useState(-70);
  const [isOnline, setIsOnline] = useState(true);
  const dispatch = useDispatch();
  const { isRecording } = useSelector((state) => state.telemetry);
  const { user } = useSelector((state) => state.user);

  useEffect(() => {
    (async () => {
      const { status: cameraStatus } = await Camera.requestCameraPermissionsAsync();
      const { status: locationStatus } = await Location.requestForegroundPermissionsAsync();
      setHasPermission(cameraStatus === 'granted' && locationStatus === 'granted');
    })();

    const unsubscribe = NetInfo.addEventListener(state => {
      setIsOnline(!!state.isConnected);
    });
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    let interval;
    if (isRecording) {
      interval = setInterval(async () => {
        const newSignal = -40 - Math.floor(Math.random() * 50);
        setSignalStrength(newSignal);
        
        const location = await Location.getCurrentPositionAsync({});
        const measurement = {
          rssi: newSignal,
          latency_ms: 20 + Math.floor(Math.random() * 30),
          packet_loss_percent: Math.random() * 2,
          device_orientation: { pitch: 0, roll: 0, yaw: 0 },
          position_approximation: { x: 0, y: 0, z: 0 },
          location_accuracy_meters: location.coords.accuracy || 5,
          timestamp: new Date().toISOString(),
        };

        dispatch(addMeasurement(measurement));

        if (!isOnline && user) {
          await saveTelemetryOffline({
            user_id: user.id,
            latitude: location.coords.latitude,
            longitude: location.coords.longitude,
            rssi: newSignal,
            wifi_ssid: 'GhostWiFi',
            timestamp: measurement.timestamp
          });
        }
      }, 2000);
    }
    return () => clearInterval(interval);
  }, [isRecording, isOnline, user]);

  const handleToggleHunt = () => {
    if (isRecording) {
      dispatch(endSession());
      Alert.alert('Hunt Ended', isOnline ? 'Your telemetry data has been synced.' : 'You are offline. Data saved locally and will sync later.');
    } else {
      dispatch(startSession(Math.random().toString(36).substring(7)));
    }
  };

  if (hasPermission === null) return <View style={styles.container}><Text>Requesting permissions...</Text></View>;
  if (hasPermission === false) return <View style={styles.container}><Text>No access to camera or location</Text></View>;

  return (
    <View style={styles.container}>
      <Camera style={styles.camera} type={Camera.Constants.Type.back}>
        <View style={styles.overlay}>
          <View style={styles.statsContainer}>
            <Text style={styles.statLabel}>WiFi Signal ({isOnline ? 'Online' : 'Offline'})</Text>
            <Text style={[styles.statValue, { color: signalStrength > -60 ? '#4ECDC4' : '#FF6B6B' }]}>
              {signalStrength} dBm
            </Text>
          </View>
          <View style={styles.ghostRadar}>
            <View style={styles.radarCircle}>
              <View style={[styles.radarPing, { opacity: isRecording ? 1 : 0.3 }]} />
            </View>
            <Text style={styles.radarText}>{isRecording ? 'Scanning for Ghosts...' : 'Radar Offline'}</Text>
          </View>
          <Button
            title={isRecording ? 'Stop Hunting' : 'Start Hunting'}
            onPress={handleToggleHunt}
            variant={isRecording ? 'secondary' : 'primary'}
            style={styles.huntButton}
          />
        </View>
      </Camera>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  camera: { flex: 1 },
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.3)', padding: 20, justifyContent: 'space-between' },
  statsContainer: { backgroundColor: 'rgba(0,0,0,0.6)', padding: 15, borderRadius: 12, alignSelf: 'flex-start', marginTop: 40 },
  statLabel: { color: '#FFFFFF', fontSize: 14 },
  statValue: { fontSize: 24, fontWeight: 'bold' },
  ghostRadar: { alignItems: 'center', justifyContent: 'center' },
  radarCircle: { width: 200, height: 200, borderRadius: 100, borderWidth: 2, borderColor: '#4ECDC4', justifyContent: 'center', alignItems: 'center' },
  radarPing: { width: 10, height: 10, borderRadius: 5, backgroundColor: '#4ECDC4' },
  radarText: { color: '#FFFFFF', marginTop: 20, fontSize: 18, fontWeight: 'bold' },
  huntButton: { marginBottom: 40 },
});
