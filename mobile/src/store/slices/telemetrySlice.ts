import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface WifiMeasurement {
  rssi: number;
  latency_ms: number;
  packet_loss_percent: number;
  device_orientation: {
    pitch: number;
    roll: number;
    yaw: number;
  };
  position_approximation: {
    x: number;
    y: number;
    z: number;
  };
  location_accuracy_meters: number;
  timestamp: string;
}

interface TelemetryState {
  currentSession: {
    id: string;
    startTime: string;
    measurements: WifiMeasurement[];
  } | null;
  isRecording: boolean;
  isSyncing: boolean;
  lastSyncTime: string | null;
  error: string | null;
}

const initialState: TelemetryState = {
  currentSession: null,
  isRecording: false,
  isSyncing: false,
  lastSyncTime: null,
  error: null,
};

const telemetrySlice = createSlice({
  name: 'telemetry',
  initialState,
  reducers: {
    startSession: (state, action: PayloadAction<string>) => {
      state.currentSession = {
        id: action.payload,
        startTime: new Date().toISOString(),
        measurements: [],
      };
      state.isRecording = true;
    },
    addMeasurement: (state, action: PayloadAction<WifiMeasurement>) => {
      if (state.currentSession) {
        state.currentSession.measurements.push(action.payload);
      }
    },
    endSession: (state) => {
      state.isRecording = false;
    },
    setSyncing: (state, action: PayloadAction<boolean>) => {
      state.isSyncing = action.payload;
    },
    setLastSyncTime: (state, action: PayloadAction<string>) => {
      state.lastSyncTime = action.payload;
    },
    clearSession: (state) => {
      state.currentSession = null;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const {
  startSession,
  addMeasurement,
  endSession,
  setSyncing,
  setLastSyncTime,
  clearSession,
  setError,
} = telemetrySlice.actions;

export default telemetrySlice.reducer;
