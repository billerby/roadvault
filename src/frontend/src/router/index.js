// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import authService from '../services/auth.service';

// Importera views
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import PropertiesView from '../views/PropertiesView.vue';
import PropertyView from '../views/PropertyView.vue';
import BillingView from '../views/BillingView.vue';
import AssociationView from '../views/AssociationView.vue';
import InvoiceManagementView from '../views/InvoiceManagementView.vue';

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
  },
  {
    path: '/billings',
    name: 'billings',
    component: BillingView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/billings/:id',
    name: 'billing-view',
    component: BillingView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/billings/:id/invoices',
    name: 'billing-invoices',
    component: BillingView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/association',
    name: 'association',
    component: AssociationView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/invoices',
    name: 'invoices',
    component: InvoiceManagementView,
    meta: { requiresAuth: true }
  },
  {
    path: '/invoices/:id',
    name: 'invoice-view',
    component: InvoiceManagementView,
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
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);
  const isAuthenticated = authService.isAuthenticated();
  const isAdmin = authService.hasRole('ADMIN');

  if (requiresAuth && !isAuthenticated) {
    next('/login');
  } else if (requiresAdmin && !isAdmin) {
    next('/'); // Redirect to home if user is not admin but tries to access admin page
  } else {
    next();
  }
});

export default router;