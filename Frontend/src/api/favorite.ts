import axios from './axios';

export const toggleFavorite = async (bookItemId: number) => {
  try {
    console.log('Sending favorite toggle request for bookItemId:', bookItemId);
    const response = await axios.post(`/api/favorites/${bookItemId}`);
    console.log('Favorite toggle response:', response.data);
    return response.data;
  } catch (error: any) {
    console.error('Favorite toggle error:', error.response?.data || error);
    throw error;
  }
}; 