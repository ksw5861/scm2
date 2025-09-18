// src/composables/useAppToast.js
import { useToast } from 'primevue/usetoast';

export const useAppToast = () => {
  const t = useToast();

  const toast = (severity = 'info', summary = '', detail = '', life = 3000) => {
    t.add({
      severity,
      summary,
      detail,
      life
    });
  };

  return { toast };
};
