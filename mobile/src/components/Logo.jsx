import { Image, StyleSheet } from "react-native";

const sizes = { sm: 28, md: 48, lg: 72, hero: 120 };

export default function Logo({ size = "md", style }) {
  return (
    <Image
      source={require("../../assets/kammo-logo.png")}
      style={[styles.logo, { height: sizes[size] || sizes.md }, style]}
      resizeMode="contain"
    />
  );
}

const styles = StyleSheet.create({
  logo: {
    width: 180,
    alignSelf: "center",
  },
});
