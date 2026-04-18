import { configureStore } from '@reduxjs/toolkit';
import userReducer from './slices/userSlice';
import telemetryReducer from './slices/telemetrySlice';
import arReducer from './slices/arSlice';
import uiReducer from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    user: userReducer,
    telemetry: telemetryReducer,
    ar: arReducer,
    ui: uiReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['user/setAuthTokens'],
        ignoredPaths: ['user.tokens'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
