import React from "react";
import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, ViewStyle, TextStyle } from "react-native";

interface ButtonProps {
  title: string;
  onPress: () => void;
  isLoading?: boolean;
  disabled?: boolean;
  variant?: "primary" | "secondary" | "outline";
  style?: ViewStyle;
  textStyle?: TextStyle;
}

export const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  isLoading = false,
  disabled = false,
  variant = "primary",
  style,
  textStyle,
}) => {
  const getVariantStyle = () => {
    switch (variant) {
      case "secondary": return styles.secondary;
      case "outline": return styles.outline;
      default: return styles.primary;
    }
  };

  const getTextStyle = () => {
    switch (variant) {
      case "outline": return styles.outlineText;
      default: return styles.text;
    }
  };

  return (
    <TouchableOpacity
      style={[styles.button, getVariantStyle(), style, (disabled || isLoading) && styles.disabled]}
      onPress={onPress}
      disabled={disabled || isLoading}
    >
      {isLoading ? (
        <ActivityIndicator color={variant === "outline" ? "#FF6B6B" : "#FFFFFF"} />
      ) : (
        <Text style={[getTextStyle(), textStyle]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    height: 50,
    borderRadius: 12,
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 20,
    marginVertical: 10,
  },
  primary: { backgroundColor: "#FF6B6B" },
  secondary: { backgroundColor: "#4ECDC4" },
  outline: {
    backgroundColor: "transparent",
    borderWidth: 2,
    borderColor: "#FF6B6B",
  },
  disabled: { opacity: 0.6 },
  text: {
    color: "#FFFFFF",
    fontSize: 16,
    fontWeight: "bold",
  },
  outlineText: {
    color: "#FF6B6B",
    fontSize: 16,
    fontWeight: "bold",
  },
});
