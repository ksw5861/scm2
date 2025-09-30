import { useIcon } from '@/composables/useIcon';

const Product = {
  label: '제품 관리',
  icon: useIcon('tags'),
  to: '/product',
  roles: ['admin']
};

export default Product;
