import * as SQLite from 'expo-sqlite';

const db = SQLite.openDatabase('ghost_hunter.db');

export interface OfflineTelemetry {
  id?: number;
  user_id: string;
  latitude: number;
  longitude: number;
  rssi: number;
  wifi_ssid: string;
  timestamp: string;
  synced: number;
}

export const initDatabase = () => {
  return new Promise((resolve, reject) => {
    db.transaction(tx => {
      tx.executeSql(
        'CREATE TABLE IF NOT EXISTS telemetry (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT, latitude REAL, longitude REAL, rssi INTEGER, wifi_ssid TEXT, timestamp TEXT, synced INTEGER DEFAULT 0)',
        [],
        () => resolve(),
        (_, error) => { reject(error); return false; }
      );
    });
  });
};

export const saveTelemetryOffline = (telemetry) => {
  return new Promise((resolve, reject) => {
    db.transaction(tx => {
      tx.executeSql(
        'INSERT INTO telemetry (user_id, latitude, longitude, rssi, wifi_ssid, timestamp, synced) VALUES (?, ?, ?, ?, ?, ?, 0)',
        [telemetry.user_id, telemetry.latitude, telemetry.longitude, telemetry.rssi, telemetry.wifi_ssid, telemetry.timestamp],
        () => resolve(),
        (_, error) => { reject(error); return false; }
      );
    });
  });
};

export const getUnsyncedTelemetry = () => {
  return new Promise((resolve, reject) => {
    db.transaction(tx => {
      tx.executeSql(
        'SELECT * FROM telemetry WHERE synced = 0',
        [],
        (_, { rows: { _array } }) => resolve(_array),
        (_, error) => { reject(error); return false; }
      );
    });
  });
};

export const markAsSynced = (ids) => {
  if (ids.length === 0) return Promise.resolve();
  const placeholders = ids.map(() => '?').join(',');
  return new Promise((resolve, reject) => {
    db.transaction(tx => {
      tx.executeSql(
        'UPDATE telemetry SET synced = 1 WHERE id IN (' + placeholders + ')',
        ids,
        () => resolve(),
        (_, error) => { reject(error); return false; }
      );
    });
  });
};
