import axios from 'axios';

export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1', timeout: 12000 });
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('twin-token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function login(credentials) {
  const { data } = await api.post('/auth/login', credentials);
  sessionStorage.setItem('twin-token', data.token);
  sessionStorage.setItem('twin-user', JSON.stringify({ email: data.email, role: data.role, studentId: data.studentId }));
  return data;
}
export async function register(input) {
  const { data } = await api.post('/auth/register', input);
  sessionStorage.setItem('twin-token', data.token);
  sessionStorage.setItem('twin-user', JSON.stringify({ email: data.email, role: data.role, studentId: data.studentId }));
  return data;
}
export async function getTwin(studentId) { return (await api.get(`/students/${studentId}/digital-twin`)).data; }
export async function getProfile() { return (await api.get('/auth/me')).data; }
