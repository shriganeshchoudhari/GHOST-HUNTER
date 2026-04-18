import React from "react";
import { View, Text, StyleSheet, TouchableOpacity, Alert } from "react-native";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store/store";
import { logout } from "../store/slices/userSlice";
import { Card } from "../components/Card";
import { Button } from "../components/Button";

export const ProfileScreen = () => {
  const { user } = useSelector((state: RootState) => state.user);
  const dispatch = useDispatch();

  const handleLogout = () => {
    Alert.alert(
      "Logout",
      "Are you sure you want to logout?",
      [
        { text: "Cancel", style: "cancel" },
        { text: "Logout", style: "destructive", onPress: () => dispatch(logout()) },
      ]
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.avatarPlaceholder}>
          <Text style={styles.avatarText}>{user?.username?.charAt(0).toUpperCase() || "U"}</Text>
        </View>
        <Text style={styles.username}>{user?.username || "User"}</Text>
        <Text style={styles.email}>{user?.email || "email@example.com"}</Text>
      </View>

      <Card style={styles.infoCard}>
        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>Subscription</Text>
          <Text style={styles.infoValue}>{user?.subscription_tier || "Free"}</Text>
        </View>
        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>Status</Text>
          <Text style={styles.infoValue}>{user?.subscription_status || "Active"}</Text>
        </View>
        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>Joined</Text>
          <Text style={styles.infoValue}>{user?.created_at ? new Date(user.created_at).toLocaleDateString() : "N/A"}</Text>
        </View>
      </Card>

      <Button
        title="Logout"
        onPress={handleLogout}
        variant="outline"
        style={styles.logoutButton}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#F8F9FA", padding: 20 },
  header: { alignItems: "center", marginVertical: 30 },
  avatarPlaceholder: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: "#FF6B6B",
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 15,
  },
  avatarText: { fontSize: 40, fontWeight: "bold", color: "#FFFFFF" },
  username: { fontSize: 24, fontWeight: "bold", color: "#333333" },
  email: { fontSize: 16, color: "#666666", marginTop: 5 },
  infoCard: { marginBottom: 30 },
  infoRow: { flexDirection: "row", justifyContent: "space-between", paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: "#F0F0F0" },
  infoLabel: { fontSize: 16, color: "#666666" },
  infoValue: { fontSize: 16, fontWeight: "bold", color: "#333333" },
  logoutButton: { marginTop: "auto", marginBottom: 20 },
});
