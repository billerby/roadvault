// services/bathTemperature.service.js
import api from './api';

const bathTemperatureService = {
  getLatestTemperature() {
    return api.get('/v1/bath-temperatures/latest');
  },

  getLatestTemperatureForDevice(deviceId) {
    return api.get(`/v1/bath-temperatures/latest/device/${deviceId}`);
  },
  
  getTemperaturesForDevice(deviceId) {
    return api.get(`/v1/bath-temperatures/device/${deviceId}`);
  },
  
  getTemperaturesInTimeRange(start, end) {
    return api.get('/v1/bath-temperatures/time-range', {
      params: { start, end }
    });
  },
  
  getDailyAverageTemperatures(deviceId, start, end) {
    return api.get(`/v1/bath-temperatures/daily-averages/device/${deviceId}`, {
      params: { start, end }
    });
  },
  
  getHourlyAverageTemperatures(deviceId, start, end) {
    return api.get(`/v1/bath-temperatures/hourly-averages/device/${deviceId}`, {
      params: { start, end }
    });
  },
  
  // Endast för admin
  pollTemperatureData() {
    return api.post('/v1/bath-temperatures/poll');
  }
};

export default bathTemperatureService;