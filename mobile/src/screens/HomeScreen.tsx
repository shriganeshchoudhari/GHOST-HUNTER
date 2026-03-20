import React from "react";
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from "react-native";
import { useSelector } from "react-redux";
import { RootState } from "../store/store";
import { Card } from "../components/Card";
import { Button } from "../components/Button";

export const HomeScreen = ({ navigation }: any) => {
  const { user } = useSelector((state: RootState) => state.user);
  const { currentSession, isRecording } = useSelector((state: RootState) => state.telemetry);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.welcome}>Welcome,</Text>
        <Text style={styles.username}>{user?.username || "Hunter"}!</Text>
      </View>

      <Card style={styles.statusCard}>
        <Text style={styles.cardTitle}>Current Status</Text>
        <View style={styles.statusRow}>
          <View style={[styles.statusDot, { backgroundColor: isRecording ? "#4ECDC4" : "#999999" }]} />
          <Text style={styles.statusText}>{isRecording ? "Hunting in progress" : "Ready for the hunt"}</Text>
        </View>
        {isRecording && (
          <Text style={styles.sessionInfo}>Session: {currentSession?.id.substring(0, 8)}...</Text>
        )}
        <Button
          title={isRecording ? "Continue Hunt" : "Start New Hunt"}
          onPress={() => navigation.navigate("ARHunting")}
          variant={isRecording ? "secondary" : "primary"}
          style={styles.actionButton}
        />
      </Card>

      <View style={styles.statsRow}>
        <Card style={styles.statCard}>
          <Text style={styles.statValue}>0</Text>
          <Text style={styles.statLabel}>Ghosts Caught</Text>
        </Card>
        <Card style={styles.statCard}>
          <Text style={styles.statValue}>0</Text>
          <Text style={styles.statLabel}>Points</Text>
        </Card>
      </View>

      <Text style={styles.sectionTitle}>Recent Activity</Text>
      <Card style={styles.activityCard}>
        <Text style={styles.emptyText}>No recent hunts. Start your first one!</Text>
      </Card>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#F8F9FA" },
  content: { padding: 20 },
  header: { marginBottom: 30 },
  welcome: { fontSize: 18, color: "#666666" },
  username: { fontSize: 32, fontWeight: "bold", color: "#333333" },
  statusCard: { marginBottom: 20 },
  cardTitle: { fontSize: 18, fontWeight: "bold", color: "#333333", marginBottom: 15 },
  statusRow: { flexDirection: "row", alignItems: "center", marginBottom: 10 },
  statusDot: { width: 12, height: 12, borderRadius: 6, marginRight: 10 },
  statusText: { fontSize: 16, color: "#333333" },
  sessionInfo: { fontSize: 14, color: "#666666", marginBottom: 15 },
  actionButton: { marginTop: 10 },
  statsRow: { flexDirection: "row", justifyContent: "space-between", marginBottom: 20 },
  statCard: { flex: 0.48, alignItems: "center", padding: 15 },
  statValue: { fontSize: 24, fontWeight: "bold", color: "#FF6B6B" },
  statLabel: { fontSize: 12, color: "#666666", marginTop: 5 },
  sectionTitle: { fontSize: 20, fontWeight: "bold", color: "#333333", marginVertical: 15 },
  activityCard: { alignItems: "center", padding: 30 },
  emptyText: { color: "#999999", textAlign: "center" },
});
