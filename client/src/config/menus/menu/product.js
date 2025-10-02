import productInbound from './dw/productInbound';
import orderApproval from './dw/orderApproval';
import orderInquiry from './dw/orderInquiry';
// import orderShipment from './dw/orderShipment';
import returnProcess from './dw/returnProcess';
import returnHistory from './dw/returnHistory';
import shipPlan from './dw/shipPlan';
import shipRegister from './dw/shipRegister';

const product = {
  label: '제품',
  items: [productInbound, orderApproval, orderInquiry,  returnProcess, returnHistory, shipPlan,shipRegister ]
};

export default product;
