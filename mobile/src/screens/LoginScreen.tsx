import React, { useState } from "react";
import { View, Text, StyleSheet, KeyboardAvoidingView, Platform, ScrollView, TouchableOpacity, Alert } from "react-native";
import { useDispatch } from "react-redux";
import { Input } from "../components/Input";
import { Button } from "../components/Button";
import { authService } from "../services/authService";
import { setUser, setAuthTokens, setLoading, setError } from "../store/slices/userSlice";

export const LoginScreen = ({ navigation }: any) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const dispatch = useDispatch();

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert("Error", "Please fill in all fields");
      return;
    }

    setIsLoading(true);
    dispatch(setLoading(true));
    try {
      const response = await authService.login(email, password);
      dispatch(setUser(response.user));
      dispatch(setAuthTokens({
        access_token: response.access_token,
        refresh_token: response.refresh_token,
        expires_in: response.expires_in,
      }));
    } catch (error: any) {
      const message = error.response?.data?.message || "Login failed";
      dispatch(setError(message));
      Alert.alert("Login Failed", message);
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
          <Text style={styles.title}>Welcome Back</Text>
          <Text style={styles.subtitle}>Sign in to continue hunting ghosts</Text>
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
            label="Password"
            value={password}
            onChangeText={setPassword}
            placeholder="Enter your password"
            secureTextEntry
          />

          <Button
            title="Login"
            onPress={handleLogin}
            isLoading={isLoading}
            style={styles.loginButton}
          />

          <View style={styles.footer}>
            <Text style={styles.footerText}>Don't have an account? </Text>
            <TouchableOpacity onPress={() => navigation.navigate("Register")}>
              <Text style={styles.linkText}>Register</Text>
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
  header: { marginBottom: 40, alignItems: "center" },
  title: { fontSize: 28, fontWeight: "bold", color: "#333333", marginBottom: 10 },
  subtitle: { fontSize: 16, color: "#666666" },
  form: { width: "100%" },
  loginButton: { marginTop: 20 },
  footer: { flexDirection: "row", justifyContent: "center", marginTop: 20 },
  footerText: { color: "#666666", fontSize: 14 },
  linkText: { color: "#FF6B6B", fontSize: 14, fontWeight: "bold" },
});
