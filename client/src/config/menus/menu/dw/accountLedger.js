import { useIcon } from '@/composables/useIcon';

const accountLedger = {
  label: '거래처 원장',
  icon: useIcon('openfolder'),
  to: '/account-ledger',
  roles: ['admin', 'employee']
};
export default accountLedger;
