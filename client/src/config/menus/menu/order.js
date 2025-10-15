import insertOrder from "./eg/insertOrder";
import insertReturn from "./eg/insertReturn";
import orderList from "./eg/orderList";
import returnList from "./eg/returnList";
import insertPay from "./eg/insertPay";
import payList from "./eg/payList";
import branchDash from "./eg/branchDash";
import goDel from "./eg/goDel";
import goneDel from "./eg/goneDel";
import branchPos from "./eg/branchPos";
import posSetting from "./eg/posSetting";


const order =
  {
    label: '판매처',
    items: [
        branchPos,
        posSetting,
        branchDash,
        insertOrder,
        orderList,
        insertReturn,
        returnList,
        insertPay,
        payList,
        goDel,
        goneDel
       
    ]
  };

export default order;
