import React from "react";
import { View, Text, StyleSheet, FlatList } from "react-native";
import { Card } from "../components/Card";

const MOCK_LEADERBOARD = [
  { id: "1", username: "GhostBuster99", points: 1250, rank: 1 },
  { id: "2", username: "WiFiWizard", points: 1100, rank: 2 },
  { id: "3", username: "SignalSeeker", points: 950, rank: 3 },
  { id: "4", username: "RouterRanger", points: 800, rank: 4 },
  { id: "5", username: "NetNinja", points: 750, rank: 5 },
];

export const LeaderboardScreen = () => {
  const renderItem = ({ item }: any) => (
    <Card style={styles.rankCard}>
      <View style={styles.rankInfo}>
        <Text style={styles.rankText}>#{item.rank}</Text>
        <Text style={styles.usernameText}>{item.username}</Text>
      </View>
      <Text style={styles.pointsText}>{item.points} pts</Text>
    </Card>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Leaderboard</Text>
        <Text style={styles.subtitle}>Top WiFi Hunters in your area</Text>
      </View>
      <FlatList
        data={MOCK_LEADERBOARD}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#F8F9FA" },
  header: { padding: 20, backgroundColor: "#FFFFFF", borderBottomWidth: 1, borderBottomColor: "#F0F0F0" },
  title: { fontSize: 28, fontWeight: "bold", color: "#333333" },
  subtitle: { fontSize: 16, color: "#666666", marginTop: 5 },
  listContent: { padding: 20 },
  rankCard: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", paddingVertical: 15 },
  rankInfo: { flexDirection: "row", alignItems: "center" },
  rankText: { fontSize: 18, fontWeight: "bold", color: "#FF6B6B", width: 40 },
  usernameText: { fontSize: 16, fontWeight: "600", color: "#333333" },
  pointsText: { fontSize: 16, fontWeight: "bold", color: "#4ECDC4" },
});
