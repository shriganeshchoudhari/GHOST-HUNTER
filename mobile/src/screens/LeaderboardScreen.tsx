import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator } from 'react-native';
import { Card } from '../components/Card';
import { getLeaderboard } from '../services/leaderboardService';

export const LeaderboardScreen = () => {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const fetchLeaderboard = async () => {
    try {
      const data = await getLeaderboard();
      setLeaderboard(data);
    } catch (error) {
      console.error('Failed to fetch leaderboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const renderItem = ({ item, index }: any) => (
    <Card style={styles.rankCard}>
      <View style={styles.rankInfo}>
        <Text style={styles.rankText}>#{index + 1}</Text>
        <Text style={styles.usernameText}>{item.username}</Text>
      </View>
      <Text style={styles.pointsText}>{item.score || 0} pts</Text>
    </Card>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#FF6B6B" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Leaderboard</Text>
        <Text style={styles.subtitle}>Top WiFi Hunters in your area</Text>
      </View>
      <FlatList
        data={leaderboard}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        onRefresh={fetchLeaderboard}
        refreshing={loading}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8F9FA' },
  loadingContainer: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { padding: 20, backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#F0F0F0' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#333333' },
  subtitle: { fontSize: 16, color: '#666666', marginTop: 5 },
  listContent: { padding: 20 },
  rankCard: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 15 },
  rankInfo: { flexDirection: 'row', alignItems: 'center' },
  rankText: { fontSize: 18, fontWeight: 'bold', color: '#FF6B6B', width: 40 },
  usernameText: { fontSize: 16, fontWeight: '600', color: '#333333' },
  pointsText: { fontSize: 16, fontWeight: 'bold', color: '#4ECDC4' },
});
