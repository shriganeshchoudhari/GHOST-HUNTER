import React, { useState } from "react";
import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView, TouchableOpacity, Alert } from "react-native";
import { useDispatch } from "react-redux";
import { Input } from "../components/Input";
import { Button } from "../components/Button";
import { authService } from "../services/authService";
import { setUser, setAuthTokens, setLoading, setError } from "../store/slices/userSlice";

export const RegisterScreen = ({ navigation }: any) => {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const dispatch = useDispatch();

  const handleRegister = async () => {
    if (!email || !username || !password || !confirmPassword) {
      Alert.alert("Error", "Please fill in all fields");
      return;
    }
    if (password !== confirmPassword) {
      Alert.alert("Error", "Passwords do not match");
      return;
    }

    setIsLoading(true);
    dispatch(setLoading(true));
    try {
      const response = await authService.register(email, username, password);
      dispatch(setUser(response.user));
      dispatch(setAuthTokens({
        access_token: response.access_token,
        refresh_token: response.refresh_token,
        expires_in: response.expires_in,
      }));
    } catch (error: any) {
      const message = error.response?.data?.message || "Registration failed";
      dispatch(setError(message));
      Alert.alert("Registration Failed", message);
    } finally {
      setIsLoading(false);
      dispatch(setLoading(false));
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      style={styles.container}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>Create Account</Text>
          <Text style={styles.subtitle}>Join the hunt for WiFi ghosts</Text>
        </View>

        <View style={styles.form}>
          <Input
            label="Email"
            value={email}
            onChangeText={setEmail}
            placeholder="Enter your email"
            keyboardType="email-address"
          />
          <Input
            label="Username"
            value={username}
            onChangeText={setUsername}
            placeholder="Choose a username"
          />
          <Input
            label="Password"
            value={password}
            onChangeText={setPassword}
            placeholder="Create a password"
            secureTextEntry
          />
          <Input
            label="Confirm Password"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            placeholder="Confirm your password"
            secureTextEntry
          />

          <Button
            title="Register"
            onPress={handleRegister}
            isLoading={isLoading}
            style={styles.registerButton}
          />

          <View style={styles.footer}>
            <Text style={styles.footerText}>Already have an account? </Text>
            <TouchableOpacity onPress={() => navigation.navigate("Login")}>
              <Text style={styles.linkText}>Login</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#FFFFFF" },
  scrollContent: { flexGrow: 1, padding: 20, justifyContent: "center" },
  header: { marginBottom: 30, alignItems: "center" },
  title: { fontSize: 28, fontWeight: "bold", color: "#333333", marginBottom: 10 },
  subtitle: { fontSize: 16, color: "#666666" },
  form: { width: "100%" },
  registerButton: { marginTop: 20 },
  footer: { flexDirection: "row", justifyContent: "center", marginTop: 20 },
  footerText: { color: "#666666", fontSize: 14 },
  linkText: { color: "#FF6B6B", fontSize: 14, fontWeight: "bold" },
});
