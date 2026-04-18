import api from './api';

export const getLeaderboard = async (limit: number = 10) => {
  const response = await api.get(/leaderboard, { params: { limit } });
  return response.data;
};
