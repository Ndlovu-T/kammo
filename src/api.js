import Constants from "expo-constants";
import { createKammoApi, mergeDeals } from "./core/api.js";

const envUrl = process.env.EXPO_PUBLIC_API_BASE_URL;
const extraUrl = Constants.expoConfig?.extra?.apiBaseUrl;

export const apiBaseUrl = envUrl || extraUrl || "http://localhost:8080";
export const kammoApi = createKammoApi(apiBaseUrl);
export { mergeDeals };
