// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import authService from '../services/auth.service';

// Importera views
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import PropertiesView from '../views/PropertiesView.vue';
import PropertyView from '../views/PropertyView.vue';

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/properties',
    name: 'properties',
    component: PropertiesView,
    meta: { requiresAuth: true }
  },
  {
    path: '/properties/:id',
    name: 'property-view',
    component: PropertyView,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Navigation guard för att skydda routes
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isAuthenticated = authService.isAuthenticated();

  if (requiresAuth && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;