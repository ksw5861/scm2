import productInbound from './dw/productInbound';
import orderApproval from './dw/orderApproval';
import orderInquiry from './dw/orderInquiry';
import orderShipment from './dw/orderShipment';

const product = {
  label: '제품',
  items: [
    productInbound,
    orderApproval,
    orderInquiry,
    orderShipment,



  ]
};

export default product;
