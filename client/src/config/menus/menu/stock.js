import matLotStock from "./ms/matLotStock";
import matStockadj from "./ms/matStockadj";
import StockByProduct from "./dw/stockByProduct";

const stock = {
  label: '재고 관리',
  items: [ matLotStock, matStockadj, StockByProduct ]
};

export default stock;
