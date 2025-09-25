import employees from './dh/employees';
import wareHouse from './dh/wareHouse';
import Bom from './sw/bom'; // 기준정보bom
import material from './sw/material'; // 기준정보자재
import product from './sw/product'; // 기준정보제품
import wareHouse1 from './sw/wareHouse1'; // 기준정보창고

const master = {
  label: '기준 정보',
  items: [wareHouse, employees, material, product, wareHouse1, Bom]
};

export default master;
