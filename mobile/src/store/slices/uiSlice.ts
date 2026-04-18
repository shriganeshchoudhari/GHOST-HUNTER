import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UIState {
  isLoading: boolean;
  isRefreshing: boolean;
  notification: {
    type: 'success' | 'error' | 'info' | 'warning';
    message: string;
    visible: boolean;
  } | null;
  theme: 'light' | 'dark';
  language: string;
}

const initialState: UIState = {
  isLoading: false,
  isRefreshing: false,
  notification: null,
  theme: 'light',
  language: 'en',
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setRefreshing: (state, action: PayloadAction<boolean>) => {
      state.isRefreshing = action.payload;
    },
    showNotification: (
      state,
      action: PayloadAction<{
        type: 'success' | 'error' | 'info' | 'warning';
        message: string;
      }>
    ) => {
      state.notification = {
        ...action.payload,
        visible: true,
      };
    },
    hideNotification: (state) => {
      state.notification = null;
    },
    setTheme: (state, action: PayloadAction<'light' | 'dark'>) => {
      state.theme = action.payload;
    },
    setLanguage: (state, action: PayloadAction<string>) => {
      state.language = action.payload;
    },
  },
});

export const {
  setLoading,
  setRefreshing,
  showNotification,
  hideNotification,
  setTheme,
  setLanguage,
} = uiSlice.actions;

export default uiSlice.reducer;
