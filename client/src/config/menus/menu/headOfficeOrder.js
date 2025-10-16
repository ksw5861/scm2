import purchase from "./ms/purchase";
import purchaseList from "./ms/purchaseList";

const headOfficeOrder =
  {
    label: '발주 관리',
    items: [
        purchase,
        purchaseList
    ]
  };

export default headOfficeOrder;
