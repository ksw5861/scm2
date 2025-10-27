import accountLedger from "./dw/accountLedger";
import orderApproval from "./dw/orderApproval";
import returnHistory from "./dw/returnHistory";
import returnProcess from "./dw/returnProcess";
import purchase from "./ms/purchase";
import purchaseList from "./ms/purchaseList";
import purchaseOrder from "./ms/purchaseOrder";

const customerOrder = {
  label: '주문 관리',
  items: [purchase, purchaseList, orderApproval, returnProcess, accountLedger]
};

export default customerOrder;
