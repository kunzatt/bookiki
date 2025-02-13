import { instance } from './instance';

export const getFavoriteStatus = async (bookItemId: number) => {
  const response = await instance.get(`/api/favorites/${bookItemId}`);
  return response.data;
};

export const toggleFavorite = async (bookItemId: number) => {
  const response = await instance.post(`/api/favorites/${bookItemId}`);
  return response.data;
}; 