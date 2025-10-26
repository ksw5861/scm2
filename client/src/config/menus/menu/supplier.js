import matShipment from "./supply/matShipment";
import matSupply from "./supply/matSupply";
import requestList from "./supply/requestList";
import supplyList from "./supply/supplyList";
import spDashboard from "./supply/dashboard";
import requestResultList from "./supply/requestResult";

const supplier = {
  label: '공급처',
  items: [
    requestList,
    requestResultList,
    matSupply,
    matShipment,
    supplyList,
    spDashboard
  ]
};

export default supplier;
