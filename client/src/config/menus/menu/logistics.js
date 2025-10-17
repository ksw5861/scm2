
import productInbound from "./dw/productInbound";
import shipPlan from "./dw/shipPlan";
import shipRegister from "./dw/shipRegister";
import materialReceip from "./ms/materialReceip";
import matOutboundRegistration from "./ms/matOutboundRegistration";
import matUnload from "./ms/matUnload";

const logistics = {
  label: '물류 관리',
  items: [matUnload, materialReceip, matOutboundRegistration, productInbound, shipPlan, shipRegister]
};

export default logistics;
