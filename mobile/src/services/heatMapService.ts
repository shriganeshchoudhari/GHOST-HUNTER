import api from "./api";

export interface HeatMapRequest {
  session_id?: string;
  min_rssi?: number;
  max_rssi?: number;
  grid_size?: number;
}

export interface HeatMapResponse {
  id: string;
  user_id: string;
  session_id?: string;
  data_points: any[];
  statistics: any;
  created_at: string;
}

export const heatMapService = {
  generate: async (request: HeatMapRequest): Promise<HeatMapResponse> => {
    const response = await api.post("/heatmap/generate", request);
    return response.data;
  },
  getSaved: async (limit: number = 10): Promise<HeatMapResponse[]> => {
    const response = await api.get(`/heatmap/saved?limit=${limit}`);
    return response.data;
  },
  getRecommendations: async (request: HeatMapRequest): Promise<any> => {
    const response = await api.post("/heatmap/recommendations", request);
    return response.data;
  },
};
