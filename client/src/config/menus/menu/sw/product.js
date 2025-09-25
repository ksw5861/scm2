import { useIcon } from '@/composables/useIcon';

const Product = {
  label: '제품 관리',
  icon: useIcon('info'),
  to: '/product',
  roles: ['admin']
};

export default Product;
