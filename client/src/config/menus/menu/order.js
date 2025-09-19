import insertOrder from "./eg/insertOrder";
import insertReturn from "./eg/insertReturn";
import orderList from "./eg/orderList";
import returnList from "./eg/returnList";
import insertPay from "./eg/insertPay";
import payList from "./eg/payList";
import branchDash from "./eg/branchDash";

const order =
  {
    label: '주문',
    items: [
        insertOrder,
        orderList,
        insertReturn,
        returnList,
        insertPay,
        payList,
        branchDash
    ]
  };

export default order;
