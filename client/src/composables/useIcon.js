const iconMap = {
  add: 'pi pi-plus',
  edit: 'pi pi-pencil',
  delete: 'pi pi-trash',
  refresh: 'pi pi-refresh',
  list: 'pi pi-bars',
  download: 'pi pi-download',
  print: 'pi pi-print',
  search: 'pi pi-search',
  pdf: 'pi pi-file-pdf',
  calendar: 'pi pi-calendar',
  login: 'pi pi-sign-in',
  save: 'pi pi-save',
  key: 'pi pi-key',
  check: 'pi pi-check'
};

export const useIcon = (name) => {
  return iconMap[name];
};
