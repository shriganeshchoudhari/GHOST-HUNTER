import NetInfo from '@react-native-community/netinfo';
import { getUnsyncedTelemetry, markAsSynced } from './offlineStorage';
import { telemetryService } from './telemetryService';

export const syncOfflineData = async () => {
  const state = await NetInfo.fetch();
  if (!state.isConnected) return;

  try {
    const unsynced = await getUnsyncedTelemetry();
    if (unsynced.length === 0) return;

    console.log('Syncing ' + unsynced.length + ' offline measurements...');

    for (const item of unsynced) {
      try {
        await telemetryService.syncTelemetry({
          userId: item.user_id,
          latitude: item.latitude,
          longitude: item.longitude,
          rssi: item.rssi,
          wifiSsid: item.wifi_ssid,
          timestamp: item.timestamp
        });
        await markAsSynced([item.id]);
      } catch (err) {
        console.error('Failed to sync item ' + item.id, err);
      }
    }
    console.log('Sync completed.');
  } catch (error) {
    console.error('Sync service error:', error);
  }
};

export const startSyncInterval = (intervalMs = 60000) => {
  return setInterval(syncOfflineData, intervalMs);
};
