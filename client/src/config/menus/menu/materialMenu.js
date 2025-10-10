import matOutboundRegistration from './ms/matOutboundRegistration';
import productionPlan from './ms/productionPlan';
import purchase from './ms/purchase';
import purchaseList from './ms/purchaseList';
import MaterialReceip from './ms/MaterialReceip';
import MatUnload from './ms/MatUnload';
import matLotStock from './ms/matLotStock';
import matStockadj from './ms/matStockadj';

const materialMenu = {
  label: '자재',
  items: [purchase, purchaseList, MatUnload, MaterialReceip, productionPlan, matOutboundRegistration, matLotStock, matStockadj]
};

export default materialMenu;
