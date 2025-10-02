import employees from './dh/employees'; // 사원
import Bom from './sw/bom'; // bom
import material from './sw/material'; // 자재
import product from './sw/product'; // 제품
import wareHouse from './sw/wareHouse'; // 창고
import vendor from './dh/vendor'; // 거래처
import account from './dh/account'; // 계정

const master = {
  label: '기준 정보',
  items: [employees, vendor, account, material, product, Bom, wareHouse]
};

export default master;
