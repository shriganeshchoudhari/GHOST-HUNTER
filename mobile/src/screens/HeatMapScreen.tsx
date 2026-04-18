import React, { useState, useEffect } from "react";
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, Alert } from "react-native";
import { Card } from "../components/Card";
import { Button } from "../components/Button";
import { heatMapService, HeatMapResponse } from "../services/heatMapService";

export const HeatMapScreen = () => {
  const [loading, setLoading] = useState(false);
  const [savedMaps, setSavedMaps] = useState<HeatMapResponse[]>([]);

  useEffect(() => {
    fetchSavedMaps();
  }, []);

  const fetchSavedMaps = async () => {
    setLoading(true);
    try {
      const maps = await heatMapService.getSaved();
      setSavedMaps(maps);
    } catch (error) {
      console.error("Failed to fetch saved maps:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateNew = async () => {
    setLoading(true);
    try {
      await heatMapService.generate({});
      Alert.alert("Success", "New heat map generated!");
      fetchSavedMaps();
    } catch (error) {
      Alert.alert("Error", "Failed to generate heat map. Ensure you have telemetry data.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.title}>WiFi Heat Maps</Text>
        <Text style={styles.subtitle}>Visualize your signal coverage</Text>
      </View>

      <Button
        title="Generate New Heat Map"
        onPress={handleGenerateNew}
        isLoading={loading}
        style={styles.generateButton}
      />

      <Text style={styles.sectionTitle}>Saved Maps</Text>
      
      {loading && savedMaps.length === 0 ? (
        <ActivityIndicator size="large" color="#FF6B6B" style={styles.loader} />
      ) : savedMaps.length > 0 ? (
        savedMaps.map((map) => (
          <Card key={map.id} style={styles.mapCard}>
            <View style={styles.mapHeader}>
              <Text style={styles.mapDate}>{new Date(map.created_at).toLocaleDateString()}</Text>
              <Text style={styles.mapPoints}>{map.data_points.length} Points</Text>
            </View>
            <View style={styles.mapPreview}>
              <Text style={styles.previewText}>Map Visualization Placeholder</Text>
            </View>
          </Card>
        ))
      ) : (
        <Card style={styles.emptyCard}>
          <Text style={styles.emptyText}>No saved heat maps yet.</Text>
        </Card>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#F8F9FA" },
  content: { padding: 20 },
  header: { marginBottom: 20 },
  title: { fontSize: 28, fontWeight: "bold", color: "#333333" },
  subtitle: { fontSize: 16, color: "#666666", marginTop: 5 },
  generateButton: { marginBottom: 30 },
  sectionTitle: { fontSize: 20, fontWeight: "bold", color: "#333333", marginBottom: 15 },
  loader: { marginTop: 50 },
  mapCard: { marginBottom: 15 },
  mapHeader: { flexDirection: "row", justifyContent: "space-between", marginBottom: 10 },
  mapDate: { fontSize: 16, fontWeight: "bold", color: "#333333" },
  mapPoints: { fontSize: 14, color: "#666666" },
  mapPreview: { height: 150, backgroundColor: "#F0F0F0", borderRadius: 8, justifyContent: "center", alignItems: "center" },
  previewText: { color: "#999999" },
  emptyCard: { padding: 40, alignItems: "center" },
  emptyText: { color: "#999999" },
});
