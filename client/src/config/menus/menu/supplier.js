import requestList from './supply/requestList';
import matSupply from './supply/matSupply';
import matShipment from './supply/matShipment'

const supplier = {
  label: '공급처',
  items: [requestList, matSupply, matShipment]
};

export default supplier;
