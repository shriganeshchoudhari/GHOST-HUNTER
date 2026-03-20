import api from "./api";
import { WifiMeasurement } from "../store/slices/telemetrySlice";

export const telemetryService = {
  syncMeasurements: async (sessionId: string, measurements: WifiMeasurement[]): Promise<void> => {
    await api.post(`/telemetry/${sessionId}/batch`, { measurements });
  },
  getHistory: async (): Promise<any[]> => {
    const response = await api.get("/telemetry/history");
    return response.data;
  },
  getStatistics: async (): Promise<any> => {
    const response = await api.get("/telemetry/statistics");
    return response.data;
  },
};
