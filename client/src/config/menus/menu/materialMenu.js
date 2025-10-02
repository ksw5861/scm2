import matOutboundRegistration from './ms/matOutboundRegistration';
import productionPlan from './ms/productionPlan';
import purchase from './ms/purchase';

const materialMenu = {
  label: '구매',
  items: [productionPlan, purchase, matOutboundRegistration]
};

export default materialMenu;
