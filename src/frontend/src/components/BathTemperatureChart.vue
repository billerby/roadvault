<template>
  <v-card class="bath-temperature-chart-widget">
    <v-card-title class="d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-icon class="mr-2" color="primary">mdi-chart-line</v-icon>
        <span>Badtemperaturutveckling</span>
      </div>
      <v-chip
        v-if="!loading && Array.isArray(chartData) && chartData.length > 0"
        color="primary"
        variant="outlined"
        size="small"
      >
        {{ chartData.length }} mätningar
      </v-chip>
    </v-card-title>

    <v-card-text>
      <!-- Period väljare -->
      <div class="mb-4">
        <v-chip-group
          v-model="selectedPeriod"
          selected-class="text-primary"
          mandatory
          @update:model-value="onPeriodChange"
        >
          <v-chip value="day" variant="outlined">
            <v-icon class="mr-1">mdi-clock-outline</v-icon>
            Senaste dagen
          </v-chip>
          <v-chip value="week" variant="outlined">
            <v-icon class="mr-1">mdi-calendar-week</v-icon>
            Senaste veckan
          </v-chip>
          <v-chip value="month" variant="outlined">
            <v-icon class="mr-1">mdi-calendar-month</v-icon>
            Senaste månaden
          </v-chip>
          <v-chip value="year" variant="outlined">
            <v-icon class="mr-1">mdi-calendar</v-icon>
            Senaste året
          </v-chip>
        </v-chip-group>
      </div>

      <!-- Chart Container -->
      <div class="chart-container">
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular
            indeterminate
            color="primary"
            size="50"
          />
          <div class="text-body-1 mt-2">Laddar temperaturdata...</div>
        </div>

        <div v-else-if="error" class="text-center py-8">
          <v-icon color="warning" size="large">mdi-alert</v-icon>
          <div class="text-body-1 mt-2 text-warning">{{ error }}</div>
          <v-btn
            variant="outlined"
            color="primary"
            class="mt-2"
            @click="fetchTemperatureData"
          >
            <v-icon class="mr-1">mdi-refresh</v-icon>
            Försök igen
          </v-btn>
        </div>

        <div v-else-if="!Array.isArray(chartData) || chartData.length === 0" class="text-center py-8">
          <v-icon color="grey" size="large">mdi-chart-line-stacked</v-icon>
          <div class="text-body-1 mt-2 text-medium-emphasis">
            Ingen temperaturdata tillgänglig för vald period
          </div>
        </div>

        <div v-else>
          <!-- Chart canvas -->
          <div style="position: relative; height: 400px; width: 100%;">
            <canvas ref="chartCanvas"></canvas>
          </div>
          
          <!-- Statistik -->
          <div class="mt-4">
            <v-row>
              <v-col cols="6" md="3">
                <div class="text-center">
                  <div class="text-h6 text-primary">{{ minTemp }}°C</div>
                  <div class="text-caption text-medium-emphasis">Lägsta</div>
                </div>
              </v-col>
              <v-col cols="6" md="3">
                <div class="text-center">
                  <div class="text-h6 text-error">{{ maxTemp }}°C</div>
                  <div class="text-caption text-medium-emphasis">Högsta</div>
                </div>
              </v-col>
              <v-col cols="6" md="3">
                <div class="text-center">
                  <div class="text-h6 text-success">{{ avgTemp }}°C</div>
                  <div class="text-caption text-medium-emphasis">Medel</div>
                </div>
              </v-col>
              <v-col cols="6" md="3">
                <div class="text-center">
                  <div class="text-h6">{{ latestTemp }}°C</div>
                  <div class="text-caption text-medium-emphasis">Senaste</div>
                </div>
              </v-col>
            </v-row>
          </div>
        </div>
      </div>
    </v-card-text>

    <v-card-actions>
      <v-spacer />
      <v-btn
        variant="text"
        color="primary"
        size="small"
        @click="fetchTemperatureData"
        :disabled="loading"
      >
        <v-icon class="mr-1">mdi-refresh</v-icon>
        Uppdatera
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import Chart from 'chart.js/auto';
import { format, subDays, subWeeks, subMonths, subYears } from 'date-fns';
import { sv } from 'date-fns/locale';
import bathTemperatureService from '../services/bathTemperature.service';

export default {
  name: 'BathTemperatureChart',
  
  data() {
    return {
      selectedPeriod: 'week',
      chartData: [],
      loading: false,
      error: null,
      chart: null
    };
  },
  
  computed: {
    minTemp() {
      if (!Array.isArray(this.chartData) || this.chartData.length === 0) return '--';
      return Math.min(...this.chartData.map(d => d.temperature)).toFixed(1);
    },
    
    maxTemp() {
      if (!Array.isArray(this.chartData) || this.chartData.length === 0) return '--';
      return Math.max(...this.chartData.map(d => d.temperature)).toFixed(1);
    },
    
    avgTemp() {
      if (!Array.isArray(this.chartData) || this.chartData.length === 0) return '--';
      const sum = this.chartData.reduce((acc, d) => acc + d.temperature, 0);
      return (sum / this.chartData.length).toFixed(1);
    },
    
    latestTemp() {
      if (!Array.isArray(this.chartData) || this.chartData.length === 0) return '--';
      return this.chartData[this.chartData.length - 1].temperature.toFixed(1);
    }
  },
  
  mounted() {
    this.fetchTemperatureData();
  },
  
  beforeUnmount() {
    if (this.chart) {
      this.chart.destroy();
    }
  },
  
  methods: {
    onPeriodChange() {
      this.fetchTemperatureData();
    },
    
    getDateRange() {
      const now = new Date();
      let startDate;
      
      switch (this.selectedPeriod) {
        case 'day':
          startDate = subDays(now, 1);
          break;
        case 'week':
          startDate = subWeeks(now, 1);
          break;
        case 'month':
          startDate = subMonths(now, 1);
          break;
        case 'year':
          startDate = subYears(now, 1);
          break;
        default:
          startDate = subWeeks(now, 1);
      }
      
      return {
        start: startDate.toISOString(),
        end: now.toISOString()
      };
    },
    
    async fetchTemperatureData() {
      this.loading = true;
      this.error = null;
      
      try {
        const { start, end } = this.getDateRange();
        
        // Använd olika API:er beroende på period för bättre prestanda
        let response;
        if (this.selectedPeriod === 'day') {
          // För senaste dagen, hämta alla mätningar
          response = await bathTemperatureService.getTemperaturesInTimeRange(start, end);
          this.chartData = (response.data || []).sort((a, b) => 
            new Date(a.receivedAt) - new Date(b.receivedAt)
          );
        } else if (this.selectedPeriod === 'week') {
          // För vecka, använd timaverages
          response = await bathTemperatureService.getHourlyAverageTemperatures('eui-a840411f8182f655', start, end);
          // Convert the aggregated data format to chartData format
          this.chartData = this.convertAggregatedData(response.data || []);
        } else {
          // För månad och år, använd dagaverages
          response = await bathTemperatureService.getDailyAverageTemperatures('eui-a840411f8182f655', start, end);
          // Convert the aggregated data format to chartData format
          this.chartData = this.convertAggregatedData(response.data || []);
        }
        
        console.log('Processed chartData:', this.chartData); // Debug log
        this.updateChart();
        
      } catch (err) {
        console.error('Error fetching temperature data:', err);
        this.error = 'Kunde inte hämta temperaturdata: ' + (err.response?.data?.message || err.message);
      } finally {
        this.loading = false;
      }
    },
    
    convertAggregatedData(aggregatedData) {
      // Convert the Object[] format from JPA native queries to the expected format
      // The PostgreSQL queries return Object[] where:
      // - aggregatedData[0] = timestamp (hour/date)
      // - aggregatedData[1] = average temperature
      
      console.log('Raw aggregated data:', aggregatedData);
      
      if (Array.isArray(aggregatedData)) {
        // Handle array format (Object[])
        return aggregatedData.map(item => {
          if (Array.isArray(item) && item.length >= 2) {
            return {
              receivedAt: item[0], // timestamp
              temperature: parseFloat(item[1]) // average temperature
            };
          } else {
            console.warn('Unexpected array item format:', item);
            return null;
          }
        }).filter(item => item !== null);
      } else if (typeof aggregatedData === 'object' && aggregatedData !== null) {
        // Handle object format {timestamp: temperature, ...}
        return Object.entries(aggregatedData).map(([timestamp, temperature]) => {
          return {
            receivedAt: timestamp,
            temperature: parseFloat(temperature)
          };
        }).sort((a, b) => new Date(a.receivedAt) - new Date(b.receivedAt)); // Sort by timestamp
      } else {
        console.warn('Aggregated data is not an array or object:', aggregatedData);
        return [];
      }
    },
    
    updateChart() {
      if (this.chart) {
        this.chart.destroy();
      }
      
      if (!Array.isArray(this.chartData) || this.chartData.length === 0) {
        return;
      }
      
      // Wait for DOM to be updated before accessing the canvas
      this.$nextTick(() => {
        // Add a small delay to ensure the v-else block has been rendered
        setTimeout(() => {
          if (!this.$refs.chartCanvas) {
            console.warn('Chart canvas not available, retrying...');
            // Retry once more after another tick
            this.$nextTick(() => {
              setTimeout(() => {
                if (!this.$refs.chartCanvas) {
                  console.error('Chart canvas still not available after retries');
                  return;
                }
                this.createChart();
              }, 50);
            });
            return;
          }
          this.createChart();
        }, 10);
      });
    },
    
    createChart() {
      if (!this.$refs.chartCanvas) {
        console.error('Cannot create chart: canvas ref not available');
        return;
      }
      
      const ctx = this.$refs.chartCanvas.getContext('2d');
      
      // Debug information
      console.log('Creating chart with data:', this.chartData);
      console.log('Data length:', this.chartData.length);
      if (this.chartData.length > 0) {
        console.log('First item:', this.chartData[0]);
        console.log('Last item:', this.chartData[this.chartData.length - 1]);
      }
      
      // Förbered data för Chart.js
      const labels = this.chartData.map(item => {
        const date = new Date(item.receivedAt || item.date);
        
        // Formatera labels baserat på period
        switch (this.selectedPeriod) {
          case 'day':
            return format(date, 'HH:mm', { locale: sv });
          case 'week':
            return format(date, 'EEE HH:mm', { locale: sv });
          case 'month':
            return format(date, 'd MMM', { locale: sv });
          case 'year':
            return format(date, 'MMM yyyy', { locale: sv });
          default:
            return format(date, 'd MMM HH:mm', { locale: sv });
        }
      });
      
      const temperatures = this.chartData.map(item => item.temperature);
      
      console.log('Labels:', labels);
      console.log('Temperatures:', temperatures);
      
      // Beräkna temperaturområde för bättre skalning
      const minTemp = Math.min(...temperatures);
      const maxTemp = Math.max(...temperatures);
      const tempRange = maxTemp - minTemp;
      const padding = Math.max(0.5, tempRange * 0.1); // Minst 0.5 grad padding
      
      const yAxisMin = Math.floor(minTemp - padding);
      const yAxisMax = Math.ceil(maxTemp + padding);
      
      console.log(`Temperature range: ${minTemp}°C - ${maxTemp}°C`);
      console.log(`Y-axis will be: ${yAxisMin}°C - ${yAxisMax}°C`);
      
      // Skapa gradient för linjen
      const gradient = ctx.createLinearGradient(0, 0, 0, 400);
      gradient.addColorStop(0, 'rgba(33, 150, 243, 0.8)');
      gradient.addColorStop(1, 'rgba(33, 150, 243, 0.1)');
      
      this.chart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: 'Temperatur (°C)',
            data: temperatures,
            borderColor: '#2196F3',
            backgroundColor: gradient,
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#2196F3',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          layout: {
            padding: {
              top: 20,
              right: 20,
              bottom: 20,
              left: 20
            }
          },
          interaction: {
            intersect: false,
            mode: 'index'
          },
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              backgroundColor: 'rgba(0, 0, 0, 0.8)',
              titleColor: '#fff',
              bodyColor: '#fff',
              cornerRadius: 8,
              displayColors: false,
              callbacks: {
                title: (context) => {
                  const dataIndex = context[0].dataIndex;
                  const date = new Date(this.chartData[dataIndex].receivedAt || this.chartData[dataIndex].date);
                  return format(date, 'EEEE d MMMM yyyy HH:mm', { locale: sv });
                },
                label: (context) => {
                  return `Temperatur: ${context.parsed.y.toFixed(1)}°C`;
                }
              }
            }
          },
          scales: {
            x: {
              grid: {
                display: true,
                color: 'rgba(0, 0, 0, 0.05)'
              },
              ticks: {
                maxTicksLimit: 8,
                color: '#666'
              }
            },
            y: {
              beginAtZero: false,
              min: yAxisMin,
              max: yAxisMax,
              grid: {
                color: 'rgba(0, 0, 0, 0.1)'
              },
              ticks: {
                color: '#666',
                maxTicksLimit: 8,
                stepSize: Math.max(0.5, (yAxisMax - yAxisMin) / 6), // Ungefär 6-8 steg
                callback: function(value) {
                  return value.toFixed(1) + '°C';
                }
              }
            }
          },
          elements: {
            point: {
              hoverBorderWidth: 3
            }
          }
        }
      });
      
      // Force a resize to ensure proper rendering
      setTimeout(() => {
        if (this.chart) {
          this.chart.resize();
        }
      }, 100);
    }
  }
};
</script>

<style scoped>
.bath-temperature-chart-widget {
  min-height: 500px;
}

.chart-container {
  position: relative;
  height: 450px;
}

.v-chip-group {
  justify-content: center;
}

@media (max-width: 600px) {
  .chart-container {
    height: 350px;
  }
  
  .v-chip-group .v-chip {
    margin: 2px;
  }
}
</style>
