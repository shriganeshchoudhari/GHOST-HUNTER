import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Ghost {
  id: string;
  position: { x: number; y: number; z: number };
  rssi: number;
  intensity: 'aggressive' | 'active' | 'weakening' | 'fading';
  isVisible: boolean;
}

export interface SafeZone {
  id: string;
  position: { x: number; y: number; z: number };
  radius: number;
  rssi: number;
}

interface ARState {
  isARSupported: boolean;
  isARActive: boolean;
  ghosts: Ghost[];
  safeZones: SafeZone[];
  currentRSSI: number | null;
  cameraPermissionGranted: boolean;
  locationPermissionGranted: boolean;
  error: string | null;
}

const initialState: ARState = {
  isARSupported: false,
  isARActive: false,
  ghosts: [],
  safeZones: [],
  currentRSSI: null,
  cameraPermissionGranted: false,
  locationPermissionGranted: false,
  error: null,
};

const arSlice = createSlice({
  name: 'ar',
  initialState,
  reducers: {
    setARSupported: (state, action: PayloadAction<boolean>) => {
      state.isARSupported = action.payload;
    },
    setARActive: (state, action: PayloadAction<boolean>) => {
      state.isARActive = action.payload;
    },
    addGhost: (state, action: PayloadAction<Ghost>) => {
      state.ghosts.push(action.payload);
    },
    updateGhost: (state, action: PayloadAction<Ghost>) => {
      const index = state.ghosts.findIndex((g) => g.id === action.payload.id);
      if (index !== -1) {
        state.ghosts[index] = action.payload;
      }
    },
    removeGhost: (state, action: PayloadAction<string>) => {
      state.ghosts = state.ghosts.filter((g) => g.id !== action.payload);
    },
    addSafeZone: (state, action: PayloadAction<SafeZone>) => {
      state.safeZones.push(action.payload);
    },
    removeSafeZone: (state, action: PayloadAction<string>) => {
      state.safeZones = state.safeZones.filter((z) => z.id !== action.payload);
    },
    setCurrentRSSI: (state, action: PayloadAction<number>) => {
      state.currentRSSI = action.payload;
    },
    setCameraPermission: (state, action: PayloadAction<boolean>) => {
      state.cameraPermissionGranted = action.payload;
    },
    setLocationPermission: (state, action: PayloadAction<boolean>) => {
      state.locationPermissionGranted = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
    clearARState: (state) => {
      state.ghosts = [];
      state.safeZones = [];
      state.currentRSSI = null;
      state.isARActive = false;
    },
  },
});

export const {
  setARSupported,
  setARActive,
  addGhost,
  updateGhost,
  removeGhost,
  addSafeZone,
  removeSafeZone,
  setCurrentRSSI,
  setCameraPermission,
  setLocationPermission,
  setError,
  clearARState,
} = arSlice.actions;

export default arSlice.reducer;
