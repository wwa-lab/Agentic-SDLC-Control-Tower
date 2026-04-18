export const REPORT_CENTER_CHILD_ROUTES = [
  {
    path: '',
    name: 'reports',
    component: () => import('./views/ReportCatalogView.vue'),
  },
  {
    path: ':reportKey',
    name: 'report-detail',
    component: () => import('./views/ReportDetailView.vue'),
  },
];
