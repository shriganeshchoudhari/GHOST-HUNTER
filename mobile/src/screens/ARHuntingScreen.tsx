import React, { useState, useEffect } from "react";
import { View, Text, StyleSheet, TouchableOpacity, Alert } from "react-native";
import { Camera } from "expo-camera";
import * as Location from "expo-location";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store/store";
import { startSession, addMeasurement, endSession } from "../store/slices/telemetrySlice";
import { Button } from "../components/Button";

export const ARHuntingScreen = ({ navigation }: any) => {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [signalStrength, setSignalStrength] = useState(-70);
  const dispatch = useDispatch();
  const { isRecording } = useSelector((state: RootState) => state.telemetry);

  useEffect(() => {
    (async () => {
      const { status: cameraStatus } = await Camera.requestCameraPermissionsAsync();
      const { status: locationStatus } = await Location.requestForegroundPermissionsAsync();
      setHasPermission(cameraStatus === "granted" && locationStatus === "granted");
    })();
  }, []);

  useEffect(() => {
    let interval: any;
    if (isRecording) {
      interval = setInterval(() => {
        // Simulate WiFi signal changes
        const newSignal = -40 - Math.floor(Math.random() * 50);
        setSignalStrength(newSignal);
        
        dispatch(addMeasurement({
          rssi: newSignal,
          latency_ms: 20 + Math.floor(Math.random() * 30),
          packet_loss_percent: Math.random() * 2,
          device_orientation: { pitch: 0, roll: 0, yaw: 0 },
          position_approximation: { x: 0, y: 0, z: 0 },
          location_accuracy_meters: 5,
          timestamp: new Date().toISOString(),
        }));
      }, 2000);
    }
    return () => clearInterval(interval);
  }, [isRecording]);

  const handleToggleHunt = () => {
    if (isRecording) {
      dispatch(endSession());
      Alert.alert("Hunt Ended", "Your telemetry data has been saved.");
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
            <Text style={styles.statLabel}>WiFi Signal</Text>
            <Text style={[styles.statValue, { color: signalStrength > -60 ? "#4ECDC4" : "#FF6B6B" }]}>
              {signalStrength} dBm
            </Text>
          </View>
          
          <View style={styles.ghostRadar}>
            <View style={styles.radarCircle}>
              <View style={[styles.radarPing, { opacity: isRecording ? 1 : 0.3 }]} />
            </View>
            <Text style={styles.radarText}>{isRecording ? "Scanning for Ghosts..." : "Radar Offline"}</Text>
          </View>

          <Button
            title={isRecording ? "Stop Hunting" : "Start Hunting"}
            onPress={handleToggleHunt}
            variant={isRecording ? "secondary" : "primary"}
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
  overlay: { flex: 1, backgroundColor: "rgba(0,0,0,0.3)", padding: 20, justifyContent: "space-between" },
  statsContainer: { backgroundColor: "rgba(0,0,0,0.6)", padding: 15, borderRadius: 12, alignSelf: "flex-start", marginTop: 40 },
  statLabel: { color: "#FFFFFF", fontSize: 14 },
  statValue: { fontSize: 24, fontWeight: "bold" },
  ghostRadar: { alignItems: "center", justifyContent: "center" },
  radarCircle: { width: 200, height: 200, borderRadius: 100, borderWidth: 2, borderColor: "#4ECDC4", justifyContent: "center", alignItems: "center" },
  radarPing: { width: 10, height: 10, borderRadius: 5, backgroundColor: "#4ECDC4" },
  radarText: { color: "#FFFFFF", marginTop: 20, fontSize: 18, fontWeight: "bold" },
  huntButton: { marginBottom: 40 },
});
