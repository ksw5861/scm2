import matShipment from "./supply/matShipment";
import matSupply from "./supply/matSupply";
import requestList from "./supply/requestList";
import supplyList from "./supply/supplyList";
import spDashboard from "./supply/dashboard";

const supplier = {
  label: '공급처',
  items: [
    requestList,
    matSupply,
    matShipment,
    supplyList,
    spDashboard
  ]
};

export default supplier;
